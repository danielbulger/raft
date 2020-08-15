package com.danielbulger.raft;

import com.danielbulger.raft.async.VoteResponseAsyncHandler;
import com.danielbulger.raft.rpc.AppendEntriesRequest;
import com.danielbulger.raft.rpc.LogEntry;
import com.danielbulger.raft.rpc.VoteRequest;
import com.danielbulger.raft.rpc.VoteResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class LocalNode extends Node {

	private static final Logger LOG = LoggerFactory.getLogger(LocalNode.class);

	public enum NodeState {
		FOLLOWER,

		CANDIDATE,

		LEADER
	}

	private NodeState state = NodeState.FOLLOWER;

	private long currentTerm = 0;

	private int leaderId = -1;

	private int votedFor = -1;

	private int votes = 0;

	private final Lock voteLock = new ReentrantLock();

	private long commitIndex = 0;

	private long lastAppliedEntry = 0;

	private final long heartBeat;

	private final long heartBeatTimeout;

	private final StateMachine stateMachine;

	private final NavigableMap<Long, LogEntry> logEntries = new TreeMap<>();

	private final Map<Integer, RemoteNode> peers = new ConcurrentHashMap<>();

	private final ScheduledExecutorService executorService;

	private ScheduledFuture<?> electionFuture;

	private ScheduledFuture<?> heartBeatFuture;

	public LocalNode(
		StateMachine stateMachine,
		LocalNodeConfiguration config,
		ScheduledExecutorService executorService
	) {

		super(config.getId(), config.getAddress());

		if (stateMachine == null) {
			throw new IllegalArgumentException("No state machine");
		}

		if (executorService == null) {
			throw new IllegalArgumentException("No executor service");
		}

		this.heartBeat = config.getHeartBeat();

		this.heartBeatTimeout = config.getHeartBeatTimeout();

		this.stateMachine = stateMachine;

		this.executorService = executorService;
	}

	private void cancelHeartBeat() {
		if (heartBeatFuture != null && !heartBeatFuture.isDone()) {
			heartBeatFuture.cancel(true);
		}

		heartBeatFuture = null;
	}

	private void cancelElection() {
		// If there is already a pending election task
		// cancel that and reschedule it
		if (electionFuture != null && !electionFuture.isDone()) {
			electionFuture.cancel(true);
		}

		electionFuture = null;
	}

	private void scheduleHeartBeat() {
		cancelHeartBeat();

		heartBeatFuture = executorService.schedule(
			this::emitHeartBeat,
			this.heartBeat,
			TimeUnit.MILLISECONDS
		);
	}

	private void scheduleElection() {

		cancelElection();

		electionFuture = executorService.schedule(
			this::startElection,
			this.heartBeatTimeout,
			TimeUnit.MILLISECONDS
		);
	}

	public void addRemoteNode(RemoteNode node) {
		if (node == null) {
			throw new IllegalArgumentException();
		}

		peers.put(node.getId(), node);
	}

	private void deleteNewerEntries(long logIndex) {
		Long index = logIndex;
		do {
			logEntries.remove(index);
		} while ((index = logEntries.higherKey(logIndex)) != null);
	}

	public boolean appendEntry(AppendEntriesRequest request) {

		if (request.getTerm() < currentTerm) {
			LOG.debug("Rejecting entry from {} as not up to date term={},ours={}",
				request.getLeaderId(),
				request.getTerm(),
				currentTerm
			);
			return false;
		}

		final LogEntry logEntry = logEntries.get(request.getPrevLogIndex());

		if (logEntry != null && logEntry.getTerm() != request.getPrevLogTerm()) {
			LOG.debug("Rejecting entry from {} as mismatching log entry index={},term={},ours={}",
				request.getLeaderId(),
				request.getPrevLogIndex(),
				request.getPrevLogTerm(),
				logEntry.getTerm()
			);

			deleteNewerEntries(logEntry.getIndex());

			return false;
		}

		// Append any missing log entries
		for (final LogEntry entry : request.getEntries()) {
			if (!logEntries.containsKey(entry.getIndex())) {
				logEntries.put(entry.getIndex(), entry);
			}
		}

		if (request.getLeaderCommitIndex() > commitIndex) {
			if (request.getEntries().isEmpty()) {
				commitIndex = request.getLeaderCommitIndex();
			} else {

				final LogEntry lastEntry = request.getEntries().get(request.getEntriesSize() - 1);

				commitIndex = Math.min(
					request.getLeaderCommitIndex(),
					lastEntry.getIndex()
				);
			}
		}

		LOG.debug("Applied entry from {}", request.getLeaderId());

		return true;
	}

	public boolean voteFor(VoteRequest request) {

		if (request.getLastLogIndex() < 0 || request.getLastLogTerm() < 0) {
			LOG.debug("Not granting vote for {} due to invalid log entries {}/{}",
				request.getCandidateId(),
				request.getLastLogIndex(),
				request.getLastLogTerm()
			);
			return false;
		}

		if (request.getTerm() < currentTerm) {
			LOG.debug("Not granting vote for {} as they are not up to date term=[{}],our-term=[{}]",
				request.getCandidateId(),
				request.getTerm(),
				this.currentTerm
			);
			return false;
		}

		if (votedFor != -1 && votedFor != request.getCandidateId()) {
			LOG.debug("Not granting vote for {} as already voted for {}",
				request.getCandidateId(),
				this.votedFor
			);
			return false;
		}

		final Optional<LogEntry> optionalLogEntry = getLastLogEntry();

		if (optionalLogEntry.isEmpty()) {
			LOG.debug(
				"Granting vote for {} as they are up to date",
				request.getCandidateId()
			);
			return true;
		}

		final LogEntry logEntry = optionalLogEntry.get();

		if (logEntry.getTerm() <= request.getLastLogTerm() && logEntry.getIndex() <= request.getLastLogIndex()) {
			LOG.debug("Granting vote for {} as they are up to date",
				request.getCandidateId()
			);
			return true;
		}

		LOG.debug("Not granting vote for {} as they are not up to date index=[{}],term=[{}]",
			request.getCandidateId(),
			request.getLastLogIndex(),
			request.getLastLogTerm()
		);

		return false;
	}

	private VoteRequest buildVoteRequest() {

		final Optional<LogEntry> logEntryOptional = getLastLogEntry();

		long lastTerm = 0L, lastIndex = 0;

		if (logEntryOptional.isPresent()) {
			lastTerm = logEntryOptional.get().getIndex();
			lastIndex = logEntryOptional.get().getTerm();
		}

		return new VoteRequest(
			this.currentTerm,
			this.getId(),
			lastTerm,
			lastIndex
		);
	}

	private void startElection() {

		state = NodeState.CANDIDATE;

		// Vote for ourselves as the leader
		votes = 1;

		votedFor = this.getId();

		final VoteRequest request = buildVoteRequest();

		for (final RemoteNode peer : peers.values()) {
			try {
				peer.getClient().askForVote(
					request,
					new VoteResponseAsyncHandler(request, this, peer)
				);
			} catch (Exception error) {
				LOG.error("Unable to request vote from {} due to {}", peer.getId(), error);
			}
		}
	}

	private boolean isValidVoteResponse(VoteRequest request, VoteResponse response) {

		if (request.getTerm() != currentTerm) {
			LOG.debug(
				"Response is not for current term: {}/{}",
				request.getTerm(),
				currentTerm
			);
			return false;
		}

		if (state != NodeState.CANDIDATE) {
			LOG.debug(
				"Response ignored as we are no longer a candidate"
			);
			return false;
		}

		return true;
	}

	public void onVoteResponse(VoteRequest request, VoteResponse response, RemoteNode peer) {

		voteLock.lock();

		if (!isValidVoteResponse(request, response)) {
			return;
		}

		try {

			if (response.getTerm() > currentTerm) {
				LOG.debug(
					"Stepping down as peer {} term {} does not match ours {}",
					peer.getId(),
					response.getTerm(),
					currentTerm
				);
				stepDown(response.getTerm());
				return;
			}

			if (response.isGranted()) {

				// if we now have enough votes to become the leader
				if (++votes > peers.size() / 2) {
					this.becomeLeader();
				}

			} else {
				LOG.debug(
					"Vote denied from {} theirs={},ours={}",
					peer.getId(),
					response.getTerm(),
					currentTerm
				);
			}
		} finally {
			voteLock.unlock();
		}
	}

	private void emitHeartBeat() {

	}

	private void becomeLeader() {
		state = NodeState.LEADER;

		votes = 0;

		leaderId = this.getId();

		this.cancelElection();

		this.scheduleHeartBeat();
	}

	private void stepDown(long newTerm) {

		if (currentTerm < newTerm) {
			currentTerm = newTerm;
			votedFor = -1;
		}

		votes = 0;

		state = NodeState.FOLLOWER;

		cancelHeartBeat();

		scheduleElection();
	}

	private Optional<LogEntry> getLastLogEntry() {
		final Map.Entry<Long, LogEntry> entry = logEntries.lastEntry();
		if (entry == null) {
			return Optional.empty();
		}
		return Optional.of(entry.getValue());
	}

	public long getCurrentTerm() {
		return currentTerm;
	}
}
