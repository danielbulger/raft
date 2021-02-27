package com.danielbulger.raft;

import com.danielbulger.raft.async.AppendEntriesResponseAsyncHandler;
import com.danielbulger.raft.async.VoteResponseAsyncHandler;
import com.danielbulger.raft.rpc.*;
import com.danielbulger.raft.service.LogPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

public class LocalNode extends Node {

	private static final Logger LOG = LoggerFactory.getLogger(LocalNode.class);

	private NodeState state = NodeState.FOLLOWER;

	private int leaderId = -1;
	private int votedFor = -1;
	private long currentTerm = 0;

	private long commitIndex = 0;
	private long lastApplied = 0;
	private int votes = 0;

	private final NodeLog nodeLog;
	private final StateMachine stateMachine;
	private final NodeConfiguration config;

	private final Map<Integer, RemoteNode> peers = new ConcurrentHashMap<>();

	private ScheduledFuture<?> electionFuture;
	private ScheduledFuture<?> heartBeatFuture;
	private final LogPersistence persistence;

	private final ScheduledExecutorService executor;
	private final ReentrantLock commitLock = new ReentrantLock();
	private final ReentrantLock electionLock = new ReentrantLock();

	protected LocalNode(
		int id,
		final InetSocketAddress address,
		final ScheduledExecutorService executor,
		final StateMachine stateMachine,
		final LogPersistence persistence,
		final NodeConfiguration config,
		final Collection<RemoteNode> peers
	) {
		super(id, address);

		if (peers == null || peers.isEmpty()) {
			throw new IllegalArgumentException("No peers");
		}

		this.stateMachine = Objects.requireNonNull(stateMachine);
		this.nodeLog = new NodeLog(Objects.requireNonNull(persistence));
		this.config = Objects.requireNonNull(config);
		this.executor = Objects.requireNonNull(executor);
		this.persistence = persistence;

		this.loadMetaData(persistence);
		this.addPeers(peers);

		this.resetElection();
	}

	/**
	 * Load the all the metadata from the persistence storage.
	 *
	 * @param persistence The {@link LogPersistence} to load the metadata from.
	 */
	private void loadMetaData(LogPersistence persistence) {
		final Optional<MetaData> optional = persistence.getLatestMetaData();

		if (optional.isPresent()) {
			this.currentTerm = optional.get().getCurrentTerm();
			this.votedFor = optional.get().getVotedFor();
		}

		final Optional<LogEntry> entry = persistence.getLastEntry();
		entry.ifPresent(logEntry -> this.lastApplied = logEntry.getIndex());
	}

	/**
	 * Add all the {@link RemoteNode} peers in the given {@link Collection}.
	 *
	 * @param collection The {@link RemoteNode}s that will be added.
	 */
	private void addPeers(Collection<RemoteNode> collection) {
		for (final RemoteNode node : collection) {
			if (node != null) {
				peers.put(node.getId(), node);
			}
		}
	}

	/**
	 * Cancel the pending election future if one exists.
	 */
	private void cancelElection() {
		if (electionFuture != null && !electionFuture.isDone()) {
			electionFuture.cancel(true);
		}

		electionFuture = null;
	}

	/**
	 * Reset the election state by cancelling any pending one and rescheduling a new election.
	 */
	private void resetElection() {
		cancelElection();

		LOG.debug("new election scheduled");

		electionFuture = executor.scheduleWithFixedDelay(
			this::election,
			0L,
			config.getHeartBeatTimeout(),
			TimeUnit.MILLISECONDS
		);
	}

	/**
	 * Cancel the pending heartbeat future if one exists.
	 */
	private void cancelHeartBeat() {
		if (heartBeatFuture != null && !heartBeatFuture.isDone()) {
			heartBeatFuture.cancel(true);
		}

		heartBeatFuture = null;
	}

	/**
	 * Reset the heartbeat state by cancelling any pending one and rescheduling a new heartbeat.
	 */
	private void scheduleHeartBeat() {
		cancelHeartBeat();

		LOG.debug("Scheduling heart beat...");

		// Send an initial empty heart beat without delay to establish ourselves as the leader
		heartBeatFuture = executor.scheduleWithFixedDelay(
			this::emitHeartBeat,
			0L,
			config.getHeartBeat(),
			TimeUnit.MILLISECONDS
		);
	}

	/**
	 * Emit a heartbeat to all {@link RemoteNode} peers. This will only
	 * execute if the {@link LocalNode} is a {@link NodeState#LEADER}.
	 */
	private void emitHeartBeat() {
		if (isFollower()) {
			LOG.error("Tried to emit heartbeat on follower");
			return;
		}

		LOG.debug("heartbeat");

		for (final RemoteNode node : peers.values()) {
			try {
				sendAppendRequest(node);
			} catch (Exception error) {
				LOG.warn(
					"Unable to send append entries request to {}/{} due to {}",
					node.getId(),
					node.getAddress(),
					error.getMessage()
				);
			}
		}
	}

	/**
	 * Replicates the given data across all the {@link RemoteNode} peers. This will only be successful
	 * if this {@link LocalNode} is a leader.
	 *
	 * @param data The data to replicate across the peers.
	 * @throws Exception If the {@link LocalNode} is a follower.
	 */
	public void replicate(byte[] data) throws Exception {
		if (isFollower()) {
			throw new Exception("replicate called on follower");
		}

		nodeLog.appendEntry(currentTerm, data);

		for (final RemoteNode peer : peers.values()) {
			try {
				sendAppendRequest(peer);
			} catch (Exception exception) {

				LOG.error(
					"Unable to send replicate request to {}/{} due to {}",
					peer.getId(),
					peer.getAddress(),
					exception.getMessage()
				);
			}
		}
	}

	/**
	 * Call a new election and request votes from all the {@link RemoteNode} peers. If the {@link LocalNode}
	 * is already a leader, this will be a {@code no-op}.
	 */
	private void election() {
		if (!isFollower()) {
			LOG.error("leader during election");
			return;
		}

		votes = 1;
		++currentTerm;
		votedFor = this.getId();
		state = NodeState.CANDIDATE;

		this.updateLatestMetaData();

		this.requestVote();
	}

	/**
	 * Update the metadata state with the {@link LogPersistence}.
	 */
	private void updateLatestMetaData() {

		try {
			persistence.updateMetaData(new MetaData(currentTerm, votedFor));
		} catch (Exception exception) {
			LOG.error("Unable to update the meta data", exception);
			// Not sure what the best error handling is for this, if we can't update
			// the meta data log.
		}
	}

	/**
	 * Send a {@link VoteRequest} to all {@link RemoteNode}s.
	 */
	private void requestVote() {

		final LogEntry entry = nodeLog.getLastEntry();

		final VoteRequest request = new VoteRequest(
			this.currentTerm,
			super.getId(),
			entry.getTerm(),
			entry.getIndex()
		);

		for (final RemoteNode peer : peers.values()) {
			try {
				peer.getClient().askForVote(
					request,
					new VoteResponseAsyncHandler(request, this, peer)
				);
			} catch (Exception e) {
				LOG.error(
					"Unable to request vote from {}/{} due to {}",
					peer.getId(),
					peer.getAddress(),
					e
				);
			}
		}
	}

	/**
	 * Create and send an {@link AppendEntriesRequest} to the given {@link RemoteNode}.
	 *
	 * @param peer The {@link RemoteNode} peer to send the data to.
	 * @throws Exception If the request fails for whatever reason.
	 */
	private void sendAppendRequest(RemoteNode peer) throws Exception {

		final AppendEntriesRequest request = makeAppendRequest(peer);

		peer.getClient().appendEntries(
			request,
			new AppendEntriesResponseAsyncHandler(request, this, peer)
		);
	}

	/**
	 * Make a new {@link AppendEntriesRequest}
	 *
	 * @param peer The {@link RemoteNode} that the request will be sent to.
	 * @return The newly created {@link AppendEntriesRequest}
	 */
	private AppendEntriesRequest makeAppendRequest(final RemoteNode peer) {

		final long prevIndex = peer.getPrevIndex();
		final LogEntry entry = nodeLog.getByIndex(prevIndex);
		final List<LogEntry> entries = nodeLog.getOlder(peer.getNextIndex());

		return new AppendEntriesRequest(
			currentTerm,
			super.getId(),
			prevIndex,
			entry.getTerm(),
			entries,
			getLeaderCommitIndex(prevIndex, entries.size())
		);
	}

	/**
	 * @param prevLogIndex
	 * @param numEntries
	 * @return
	 */
	private long getLeaderCommitIndex(long prevLogIndex, int numEntries) {
		return Math.min(
			nodeLog.getLastEntryIndex(),
			prevLogIndex + numEntries
		);
	}

	public void handleVoteResponse(
		VoteRequest request,
		VoteResponse response,
		RemoteNode peer
	) {
		if (currentTerm != request.getTerm() || state != NodeState.CANDIDATE) {
			LOG.debug(
				"term or state changed term={}/{}, state={}",
				currentTerm,
				request.getTerm(),
				state
			);

			return;
		}

		if (response.getTerm() > currentTerm) {
			stepDown(response.getTerm());
			return;
		}

		if (response.isGranted()) {
			LOG.debug(
				"Received vote from {}/{} for term={}",
				peer.getId(),
				peer.getAddress(),
				response.getTerm()
			);

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
	}

	public void handleAppendEntryResponse(
		AppendEntriesRequest request,
		AppendEntriesResponse response,
		RemoteNode peer
	) {
		commitLock.lock();

		try {
			if (response.getTerm() > currentTerm) {

				LOG.debug(
					"Stepping down from leader as {}/{} term {} is newer than ours {}",
					peer.getId(),
					peer.getAddress(),
					response.getTerm(),
					currentTerm
				);

				this.stepDown(request.getTerm());
			} else {
				if (!response.isSuccess()) {
					LOG.debug(
						"Append Entry failed for {}/{} prev={term={},index={}}, term={}",
						peer.getId(),
						peer.getAddress(),
						request.getPrevLogTerm(),
						request.getPrevLogIndex(),
						request.getTerm()
					);
					peer.setNextIndex(peer.getNextIndex() - 1);
					return;
				}

				peer.setMatchIndex(request.getPrevLogIndex() + request.getEntriesSize());
				peer.setNextIndex(peer.getMatchIndex() + 1);
				applyNextEntry();
			}
		} finally {
			commitLock.unlock();
		}
	}

	/**
	 * Step down as the leader and adjust the current term to the {@code term}.
	 *
	 * @param term The new term.
	 */
	private void stepDown(long term) {
		electionLock.lock();
		try {
			currentTerm = term;
			votedFor = -1;
			state = NodeState.FOLLOWER;
			votes = 0;
			this.updateLatestMetaData();
			resetElection();
		} finally {
			electionLock.unlock();
		}
	}

	/**
	 * Become a leader and {@link #emitHeartBeat()} to all {@link RemoteNode} peers.
	 */
	private void becomeLeader() {
		LOG.debug("Becoming leader...");

		electionLock.lock();

		try {
			state = NodeState.LEADER;
			votes = 0;
			leaderId = super.getId();

			final long entryIndex = nodeLog.getLastEntryIndex();

			for (final RemoteNode peer : peers.values()) {
				peer.setMatchIndex(0);
				peer.setNextIndex(entryIndex + 1);
			}

			cancelElection();
			scheduleHeartBeat();
		} finally {
			electionLock.unlock();
		}
	}

	private long getMedianIndex() {

		final List<Long> indexes = peers.values()
			.stream()
			.mapToLong(RemoteNode::getMatchIndex)
			.boxed()
			.sorted(Comparator.reverseOrder())
			.collect(Collectors.toList());

		return indexes.get(indexes.size() / 2);
	}

	private boolean shouldApplyEntry(long newCommitIndex) {

		if (commitIndex >= newCommitIndex) {
			LOG.debug(
				"Not applying log entry as commit={} is above new commit={}",
				commitIndex,
				newCommitIndex
			);
			return false;
		}

		final LogEntry commitEntry = nodeLog.getByIndex(newCommitIndex);

		if (commitEntry != null && commitEntry.getTerm() != currentTerm) {
			LOG.debug(
				"Not applying log entry as commit entry term does not match entry={}, ours={}",
				commitEntry.getTerm(),
				currentTerm
			);
			return false;
		}

		return true;
	}

	private void applyNextEntry() {

		this.commitLock.lock();
		try {

			/*
			As described by the White Paper on Page 4
			If there exists an N such that N > commitIndex, a majority
				of matchIndex[i] ≥ N, and log[N].term == currentTerm:
				set commitIndex = N
			To do this, we find the median index and if this is > the commit index
			we know that the majority of the peers are greater than the
			commit index.
			 */
			final long newCommitIndex = getMedianIndex();

			if (!shouldApplyEntry(newCommitIndex)) {
				return;
			}

			final long oldCommitIndex = commitIndex;
			commitIndex = newCommitIndex;
			updateLatestMetaData();

			for (long index = oldCommitIndex + 1; index <= commitIndex; ++index) {
				final LogEntry entry = nodeLog.getByIndex(index);
				if (entry == null) {
					LOG.error("No log entry for index {}", index);
					continue;
				}

				try {

					lastApplied = index;
					applyLogEntry(entry);
					LOG.debug(
						"Committing entry={}, term={}, lastAppliedIndex={}",
						index,
						currentTerm,
						lastApplied
					);

				} catch (Exception exception) {
					// Don't try and go any further if we are unable to apply this update
					LOG.error(
						"Failed to commit log entry {} due to {}",
						index,
						exception.getMessage()
					);

					break;
				}
			}
		} finally {
			commitLock.unlock();
		}
	}

	private void applyLogEntry(LogEntry entry) throws Exception {
		persistence.save(entry);
		stateMachine.apply(entry.getData());
	}

	public boolean grantVoteFor(VoteRequest request) {

		electionLock.lock();

		try {

			final LogEntry entry = nodeLog.getLastEntry();

			if (request.getTerm() < currentTerm) {
				LOG.debug("Not granting vote for {} as they are not up to date term=[{}],our-term=[{}]",
					request.getCandidateId(),
					request.getTerm(),
					this.currentTerm
				);

				return false;
			}

			if (votedFor != request.getCandidateId()) {
				LOG.debug("Not granting vote for {} as already voted for {}",
					request.getCandidateId(),
					this.votedFor
				);

				return false;
			}

			if (request.getLastLogTerm() < entry.getTerm() ||
				request.getLastLogIndex() < entry.getIndex()
			) {
				LOG.debug("Not granting vote for {} as they are not up to date index=[{}],term=[{}]",
					request.getCandidateId(),
					request.getLastLogIndex(),
					request.getLastLogTerm()
				);

				return false;
			}

			votedFor = request.getCandidateId();

			LOG.debug("Granting vote for {} as they are up to date",
				request.getCandidateId()
			);

			return true;
		} finally {
			electionLock.unlock();
		}
	}

	public boolean appendEntry(AppendEntriesRequest request) {
		commitLock.lock();
		try {
			// The request is from an out of date node.
			if (request.getTerm() < currentTerm) {

				LOG.debug("Rejecting entry from {} as not up to date term={},ours={}",
					request.getLeaderId(),
					request.getTerm(),
					currentTerm
				);

				return false;
			}

			// Otherwise we can trust that this is the leader.
			stepDown(request.getTerm());

			if (request.getPrevLogIndex() > nodeLog.getLastEntryIndex()) {
				return false;
			}

			final List<LogEntry> newEntries = getNewLogEntries(request.getEntries());
			nodeLog.appendEntries(newEntries);

			updateCommitIndex(request.getLeaderCommitIndex());
			updateApplied();

			return true;
		} finally {
			commitLock.unlock();
		}
	}

	/**
	 * @param requestEntries
	 * @return
	 */
	private List<LogEntry> getNewLogEntries(List<LogEntry> requestEntries) {

		final long firstIndex = nodeLog.getFirstEntryIndex();
		final long lastIndex = nodeLog.getLastEntryIndex();

		final List<LogEntry> newEntries = new ArrayList<>();

		for (final LogEntry entry : requestEntries) {
			if (entry.getIndex() < firstIndex) {
				continue;
			}

			if (entry.getIndex() <= lastIndex) {
				// Now find any that term don't match to the master.
				if (!validateLogTerm(nodeLog.getByIndex(entry.getIndex()), entry.getTerm())) {
					// Delete any after this one as they don't match.
					nodeLog.deleteNewer(entry.getIndex());
					newEntries.add(entry);
				}
			} else {
				newEntries.add(entry);
			}
		}

		return newEntries;
	}

	/**
	 * Update the commit index last on the leader's commit index.
	 *
	 * @param leaderCommitIndex The leader's commit index.
	 */
	private void updateCommitIndex(long leaderCommitIndex) {
		if (leaderCommitIndex > commitIndex) {
			commitIndex = Math.min(leaderCommitIndex, nodeLog.getLastEntryIndex());
		}
	}

	/**
	 * Apply the last log entry to the {@link StateMachine}.
	 */
	private void updateApplied() {
		if (lastApplied < commitIndex) {
			for (long index = lastApplied + 1; index <= commitIndex; ++index) {
				final LogEntry entry = nodeLog.getByIndex(index);
				stateMachine.apply(entry.getData());
				lastApplied = index;
			}
		}
	}

	/**
	 * Validates the {@link LogEntry} against the given {@code term} id.
	 *
	 * @param entry The {@link LogEntry} that will be validated.
	 * @param term  The term that will be validated against.
	 * @return {@code true} if the {@link LogEntry}s term matches the {@code term}, {@code false} otherwise.
	 */
	private boolean validateLogTerm(LogEntry entry, long term) {
		return entry != null && entry.getTerm() == term;
	}

	/**
	 * Check whether this {@link LocalNode} is a leader or not.
	 *
	 * @return {@code true} if the {@link LocalNode} is {@link NodeState#LEADER}, {@code false} otherwise.
	 */
	public boolean isFollower() {
		return state != NodeState.LEADER;
	}

	/**
	 * Get the current leader id.
	 *
	 * @return The leader id.
	 */
	public int getLeaderId() {
		return leaderId;
	}

	/**
	 * Get the current term identifier.
	 *
	 * @return The current term.
	 */
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
