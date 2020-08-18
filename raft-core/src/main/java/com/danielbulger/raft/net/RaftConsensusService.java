package com.danielbulger.raft.net;

import com.danielbulger.raft.LocalNode;
import com.danielbulger.raft.rpc.*;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;

public class RaftConsensusService implements RaftConsensus.Iface {

	private static final Logger LOG = LoggerFactory.getLogger(RaftConsensusService.class.getName());

	private final LocalNode node;

	public RaftConsensusService(LocalNode node) {
		this.node = node;
	}

	@Override
	public AppendEntriesResponse appendEntries(AppendEntriesRequest request) throws TException {
		return new AppendEntriesResponse(
			node.getCurrentTerm(),
			node.getLastLogIndex(),
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
			node.voteFor(request)
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
		if (!node.isLeader()) {
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
