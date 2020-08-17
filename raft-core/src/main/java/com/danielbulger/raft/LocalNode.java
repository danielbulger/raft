package com.danielbulger.raft;

import com.danielbulger.raft.async.AppendEntriesResponseAsyncHandler;
import com.danielbulger.raft.async.VoteResponseAsyncHandler;
import com.danielbulger.raft.rpc.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class LocalNode extends Node {

	private static final Logger LOG = LoggerFactory.getLogger(LocalNode.class);

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
		NodeConfiguration config,
		ScheduledExecutorService executorService,
		Collection<RemoteNode> peers
	) {

		super(config.getId(), config.getAddress());

		if (stateMachine == null) {
			throw new IllegalArgumentException("No state machine");
		}

		if (executorService == null) {
			throw new IllegalArgumentException("No executor service");
		}

		if (peers == null || peers.isEmpty()) {
			throw new IllegalArgumentException("No peers");
		}

		this.heartBeat = config.getHeartBeat();

		this.heartBeatTimeout = config.getHeartBeatTimeout();

		this.stateMachine = stateMachine;

		this.executorService = executorService;

		this.addPeers(peers);
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

		LOG.debug("Scheduling heart beat...");

		// Send an initial empty heart beat without delay to establish ourselves as the leader
		heartBeatFuture = executorService.scheduleWithFixedDelay(
			this::emitHeartBeat,
			0L,
			this.heartBeat,
			TimeUnit.MILLISECONDS
		);
	}

	public void scheduleElection() {

		cancelElection();

		electionFuture = executorService.scheduleWithFixedDelay(
			this::startElection,
			this.heartBeatTimeout,
			this.heartBeatTimeout,
			TimeUnit.MILLISECONDS
		);
	}

	public void addPeers(Collection<RemoteNode> nodes) {
		for (final RemoteNode peer : nodes) {
			if (peer != null) {
				peers.put(peer.getId(), peer);
			}
		}
	}

	private void deleteNewerEntries(long logIndex) {
		Long index = logIndex;
		do {
			logEntries.remove(index);
		} while ((index = logEntries.higherKey(logIndex)) != null);
	}

	public boolean appendEntry(AppendEntriesRequest request) {

		if (!peers.containsKey(request.getLeaderId())) {
			return false;
		}

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

		voteLock.lock();

		// Reschedule the election task since we have received something
		this.scheduleElection();

		try {

			// If we were part of an election and received a
			// valid append RPC then we must revert to a follower state
			if (state == NodeState.CANDIDATE) {
				LOG.debug(
					"Stepping down as candidate as {}/{} is a valid leader",
					request.getLeaderId(),
					peers.get(request.getLeaderId()).getAddress()
				);
				stepDown(request.getTerm());
			}

			leaderId = request.getLeaderId();

			votedFor = -1;

		} finally {
			voteLock.unlock();
		}

		if (request.getEntriesSize() == 0) {
			LOG.debug("Heartbeat received from {}", request.getLeaderId());

			applyLogEntry(request);

			return true;
		}

		// Append any missing log entries
		for (final LogEntry entry : request.getEntries()) {
			if (!logEntries.containsKey(entry.getIndex())) {
				logEntries.put(entry.getIndex(), entry);
			}
		}

		applyLogEntry(request);

		return true;
	}

	private void applyLogEntry(AppendEntriesRequest request) {

		commitIndex = Math.min(
			request.getLeaderCommitIndex(),
			request.getPrevLogIndex() + request.getEntriesSize()
		);

		if (lastAppliedEntry < commitIndex) {

			for (long index = lastAppliedEntry + 1; index <= commitIndex; ++index) {

				final LogEntry entry = logEntries.get(index);

				if (entry == null) {
					LOG.error("No log entry for index {}", index);
					continue;
				}

				stateMachine.apply(entry.getData());

				LOG.debug(
					"Applied entry={}, leader={}, term={}, lastAppliedIndex={}",
					index,
					request.getLeaderId(),
					currentTerm,
					lastAppliedEntry
				);

				lastAppliedEntry = index;
			}

		}

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

		final LogEntry logEntry = getLastLogEntry();

		if (logEntry.getTerm() == 0) {

			votedFor = request.getCandidateId();

			LOG.debug(
				"Granting vote for {} as they are up to date",
				request.getCandidateId()
			);
			return true;
		}

		if (logEntry.getTerm() <= request.getLastLogTerm() && logEntry.getIndex() <= request.getLastLogIndex()) {

			votedFor = request.getCandidateId();

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

		final LogEntry lastEntry = getLastLogEntry();

		return new VoteRequest(
			this.currentTerm,
			super.getId(),
			lastEntry.getTerm(),
			lastEntry.getIndex()
		);
	}

	private void startElection() {

		this.cancelElection();

		state = NodeState.CANDIDATE;

		++currentTerm;

		// Vote for ourselves as the leader
		votes = 1;

		votedFor = super.getId();

		final VoteRequest request = buildVoteRequest();

		for (final RemoteNode peer : peers.values()) {
			try {
				peer.getClient().askForVote(
					request,
					new VoteResponseAsyncHandler(request, this, peer)
				);
			} catch (Exception error) {
				LOG.error(
					"Unable to request vote from {}/{} due to {}",
					peer.getId(),
					peer.getAddress(),
					error
				);
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
					"Stepping down as peer {}/{} term {} does not match ours {}",
					peer.getId(),
					peer.getAddress(),
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
					"Vote denied from {}/{} theirs={},ours={}",
					peer.getId(),
					peer.getAddress(),
					response.getTerm(),
					currentTerm
				);
			}
		} finally {
			voteLock.unlock();
		}
	}

	private AppendEntriesRequest buildAppendEntryRequest() {

		final LogEntry lastEntry = getLastLogEntry();

		return new AppendEntriesRequest(
			currentTerm,
			super.getId(),
			lastEntry.getIndex(),
			lastEntry.getTerm(),
			Collections.emptyList(),
			commitIndex
		);
	}

	private void emitHeartBeat() {

		if (state != NodeState.LEADER) {
			LOG.warn("Tried to emit heart beat when we are not the leader");
			return;
		}

		LOG.debug("Sending heartbeat...");

		final AppendEntriesRequest request = buildAppendEntryRequest();

		for (final RemoteNode node : peers.values()) {
			try {
				node.getClient().appendEntries(
					request,
					new AppendEntriesResponseAsyncHandler(request, this, node)
				);
			} catch (Exception error) {
				LOG.warn("Unable to send append entries request to {} due to {}", node.getId(), error);
			}
		}
	}

	private boolean isValidAppendEntryResponse(
		AppendEntriesRequest request,
		AppendEntriesResponse response
	) {
		if (request.getTerm() != currentTerm) {
			LOG.debug(
				"Response is not for current term: {}/{}",
				request.getTerm(),
				currentTerm
			);
			return false;
		}

		if (state != NodeState.LEADER) {
			LOG.debug(
				"Response is ignored as we are no longer the leader"
			);

			return false;
		}

		return true;
	}

	public void onAppendEntryResponse(
		AppendEntriesRequest request,
		AppendEntriesResponse response,
		RemoteNode peer
	) {

		if (!isValidAppendEntryResponse(request, response)) {
			return;
		}

		if (response.getTerm() > currentTerm) {

			LOG.debug(
				"Stepping down from leader as {}/{} term {} is newer than ours {}",
				peer.getId(),
				peer.getAddress(),
				response.getTerm(),
				currentTerm
			);

			this.stepDown(request.getTerm());
		}
	}

	private void becomeLeader() {
		LOG.debug("Becoming leader...");

		state = NodeState.LEADER;

		votes = 0;

		leaderId = super.getId();

		for (final RemoteNode peer : peers.values()) {

			peer.setMatchIndex(0);

			peer.setNextIndex(getLastLogEntry().getIndex() + 1);
		}

		cancelElection();

		scheduleHeartBeat();
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

	private LogEntry getLastLogEntry() {
		final Map.Entry<Long, LogEntry> entry = logEntries.lastEntry();

		return entry == null ?
			new LogEntry(0L, 0L, null) :
			entry.getValue();
	}

	public long getCurrentTerm() {
		return currentTerm;
	}

	@Override
	public String toString() {
		return "LocalNode{" +
			"id=" + super.getId() +
			", address=" + super.getAddress() +
			'}';
	}
}
