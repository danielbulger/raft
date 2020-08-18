package com.danielbulger.raft.net;

import com.danielbulger.raft.LocalNode;
import com.danielbulger.raft.rpc.*;
import org.apache.thrift.TException;
import org.apache.thrift.async.AsyncMethodCallback;

public class RaftConsensusService implements RaftConsensus.Iface, RaftConsensus.AsyncIface {

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
	public void appendEntries(
		AppendEntriesRequest request,
		AsyncMethodCallback<AppendEntriesResponse> resultHandler
	) {
		try {
			resultHandler.onComplete(this.appendEntries(request));
		} catch (Exception error) {
			resultHandler.onError(error);
		}
	}

	@Override
	public void installSnapshot(
		InstallSnapshot snapshot,
		AsyncMethodCallback<InstallSnapshotResponse> resultHandler
	) throws TException {

	}

	@Override
	public void vote(
		VoteRequest request,
		AsyncMethodCallback<VoteResponse> resultHandler
	) {
		try {
			resultHandler.onComplete(this.vote(request));
		} catch (Exception error) {
			resultHandler.onError(error);
		}
	}
}
