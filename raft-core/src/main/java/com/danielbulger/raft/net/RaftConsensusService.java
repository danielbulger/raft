package com.danielbulger.raft.net;

import com.danielbulger.raft.LocalNode;
import com.danielbulger.raft.NodeLog;
import com.danielbulger.raft.rpc.*;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.Objects;

public class RaftConsensusService implements RaftConsensus.Iface {

	private static final Logger LOG = LoggerFactory.getLogger(RaftConsensusService.class.getName());

	private final LocalNode node;
	private final NodeLog nodeLog;

	/**
	 *
	 * @param node
	 * @param nodeLog
	 * @throws IllegalArgumentException If {@code node} or {@code nodeLog} are {@code null}.
	 */
	public RaftConsensusService(LocalNode node, NodeLog nodeLog) {
		if(node == null) {
			throw new IllegalArgumentException();
		}
		if(nodeLog == null) {
			throw new IllegalArgumentException();
		}
		this.node = node;
		this.nodeLog = nodeLog;
	}

	@Override
	public AppendEntriesResponse appendEntries(AppendEntriesRequest request) throws TException {
		return new AppendEntriesResponse(
			node.getCurrentTerm(),
			nodeLog.getLastEntryIndex(),
			node.appendEntry(request)
		);
	}

	@Override
	public InstallSnapshotResponse installSnapshot(InstallSnapshot snapshot) throws TException {
		return null;
	}

	@Override
	public VoteResponse vote(VoteRequest request) {
		return new VoteResponse(
			node.getCurrentTerm(),
			node.grantVoteFor(request)
		);
	}

	@Override
	public int whoIsLeader() throws TException {
		return node.getLeaderId();
	}

	@Override
	public UpdateDataResponse updateData(ByteBuffer data) throws TException {

		// If we are not the leader, direct the client to who
		// we think the current leader is.
		if (node.isFollower()) {
			return new UpdateDataResponse(false, node.getLeaderId());
		}

		try {
			node.replicate(data.array());
			return new UpdateDataResponse(true, 0);
		} catch (Exception exception) {
			LOG.error("Failed to replicate message", exception);
			return new UpdateDataResponse(false, 0);
		}
	}
}
