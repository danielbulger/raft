package com.danielbulger.raft;

import com.danielbulger.raft.rpc.LogEntry;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class LocalNode extends Node {

	private static final Logger LOG = LoggerFactory.getLogger(LocalNode.class);

	public enum NodeRole {
		FOLLOWER,

		CANDIDATE,

		LEADER
	}

	private StateMachine stateMachine;

	private NodeRole role = NodeRole.FOLLOWER;

	private int votes = 0;

	private int votesCasted = 0;

	private int votesCounted = 0;

	private int currentLeaderId = -1;

	private int votedFor = -1;

	private long currentTerm = 0L;

	private long commitIndex = 0L;

	private long lastAppliedIndex = 0L;

	private final NavigableMap<Long, LogEntry> logEntries = new TreeMap<>();

	private final Map<Integer, RemoteNode> peers = new ConcurrentHashMap<>();

	public LocalNode(int id, InetSocketAddress address, StateMachine stateMachine) {
		super(id, address);

		if (stateMachine == null) {
			throw new IllegalArgumentException("StateMachine must not be null");
		}

		this.stateMachine = stateMachine;
	}

	private void deleteNewerEntries(Long startIndex) {

		do {

			logEntries.remove(startIndex);

			LOG.debug("Deleted newer entry {}", startIndex);

		} while ((startIndex = logEntries.higherKey(startIndex)) != null);
	}

	public boolean isLeader() {
		return this.role == NodeRole.LEADER;
	}

	public void askForVotes() {

		if (this.role != NodeRole.FOLLOWER) {
			throw new IllegalStateException();
		}

		this.votedFor = getId();
		this.votes = 1;
		this.role = NodeRole.CANDIDATE;

		for (final RemoteNode peer : peers.values()) {
			try {
				peer.getClient().sendVoteRequest(this);

				++votesCasted;
			} catch (TException e) {
				// TODO correct error handling of failing nodes
				LOG.error("Unable to send vote request", e);
			}
		}
	}

	public void sendHeartbeat() {

		if (!this.isLeader()) {
			throw new IllegalStateException();
		}

		for (final RemoteNode peer : peers.values()) {
			try {
				peer.getClient().sendAppendLogEntryRequest(this, Collections.emptyList());
			} catch (TException e) {
				// TODO correct error handling of failing nodes
				LOG.error("Unable to send heartbeat", e);
			}
		}
	}

	public void onVoteResponse(boolean granted, long term) {

		if (role != NodeRole.CANDIDATE) {
			LOG.debug(
				"Vote ignored as we are no longer a candidate {}",
				role
			);

			return;
		}

		LOG.debug("Received vote {}", granted);

		// The voter is more up to date than we are
		// we revert back to a follower role
		if (term > currentTerm) {

			LOG.debug(
				"Vote failed as we are out of date {}/{}",
				term,
				currentTerm
			);

			role = NodeRole.FOLLOWER;

			this.electionCleanup();

			return;
		}

		++votesCounted;

		if (granted) {
			++votes;
		}

		if (isElectionCompleted()) {
			onVoteCompletion();
		}
	}

	public void onVoteCompletion() {

		LOG.debug("Majority votes counted result {}", isElectionSuccessful());

		this.electionCleanup();

		if (isElectionSuccessful()) {
			this.role = NodeRole.LEADER;

			// Now we are the leader, we send a heartbeat to establish our
			// authority/history
			this.sendHeartbeat();

			// Schedule the heartbeat.
		} else {
			this.role = NodeRole.FOLLOWER;
		}
	}

	private void electionCleanup() {
		this.votedFor = -1;
		this.votes = this.votesCasted = 0;
	}

	public boolean isElectionCompleted() {
		return votesCounted / 2 >= this.votesCasted;
	}

	public boolean isElectionSuccessful() {
		return votes >= (votesCounted / 2);
	}

	public boolean vote(
		int candidate,
		long term,
		long lastLogIndex,
		long lastLogTerm
	) {
		if (term < currentTerm) {
			LOG.debug("No vote for {} as expired term ({}/{})", candidate, term, currentTerm);
			return false;
		}

		if (votedFor != -1L && votedFor != candidate) {
			LOG.debug("No vote for {} as vote already casted ({})", candidate, votedFor);
			return false;
		}

		final LogEntry entry = this.logEntries.get(lastLogIndex);

		if (entry == null) {
			LOG.debug("Vote accepted for {} as up to date", candidate);
			return true;
		}

		if (entry.getTerm() != lastLogTerm) {
			LOG.debug("No vote for {} as log terms not matched ({}/{})", candidate, entry.getTerm(), lastLogTerm);
			return false;
		}

		LOG.debug("Vote accept for {} as all checks pass", candidate);

		votedFor = candidate;

		return true;
	}

	public boolean appendLogEntry(
		int leaderId,
		long term,
		long prevLogIndex,
		long prevLogTerm,
		long leaderCommitIndex,
		Collection<LogEntry> entries
	) {

		if (isLeader()) {
			throw new IllegalStateException();
		}

		if (term < currentTerm) {

			LOG.debug(
				"No append from {} as term out of date {}/{}",
				leaderId,
				term,
				currentTerm
			);

			return false;
		}

		final LogEntry prevEntry = logEntries.get(prevLogIndex);

		if (prevEntry == null) {
			LOG.debug(
				"No append from {} as last entry not found {}",
				leaderId,
				prevLogIndex
			);

			return false;
		}

		if (prevEntry.getTerm() != prevLogTerm) {
			deleteNewerEntries(prevEntry.getIndex());
		}

		for (final LogEntry newEntry : entries) {
			if (!logEntries.containsKey(newEntry.getIndex())) {

				logEntries.put(newEntry.getIndex(), newEntry);

				LOG.debug(
					"Append from {} new entry {}",
					leaderId,
					newEntry.getIndex()
				);
			}
		}

		commitIndex = Math.min(
			leaderCommitIndex,
			prevLogIndex + logEntries.size()
		);

		// TODO apply any state machine updates from lastAppliedIndex to the commitIndex

		return true;
	}

	private LogEntry getLastLog() {
		return logEntries.get(this.logEntries.lastKey());
	}

	public long getCurrentTerm() {
		return currentTerm;
	}

	public long getCommitIndex() {
		return commitIndex;
	}

	public long getLastLogIndex() {

		LogEntry entry = getLastLog();

		return entry != null ? getLastLog().getIndex() : 0;
	}

	public long getLastLogTerm() {

		LogEntry entry = getLastLog();

		return entry != null ? getLastLog().getTerm() : 0;
	}
}
