package com.danielbulger.raft.net;

import com.danielbulger.raft.LocalNode;
import com.danielbulger.raft.rpc.*;
import org.apache.thrift.TException;
import org.apache.thrift.async.AsyncMethodCallback;

public class RaftConsensusService implements RaftConsensus.Iface, RaftConsensus.AsyncIface {

	private final LocalNode node;

	protected RaftConsensusService(LocalNode node) {
		this.node = node;
	}

	@Override
	public AppendEntriesResponse appendEntries(AppendEntriesRequest request) throws TException {

		final boolean success = node.appendLogEntry(
			request.getLeaderId(),
			request.getTerm(),
			request.getPrevLogIndex(),
			request.getPrevLogTerm(),
			request.getLeaderCommitIndex(),
			request.getEntries()
		);

		return new AppendEntriesResponse(
			node.getCurrentTerm(),
			success
		);
	}

	@Override
	public InstallSnapshotResponse installSnapshot(InstallSnapshot snapshot) throws TException {
		return null;
	}

	@Override
	public VoteResponse vote(VoteRequest request) throws TException {

		final boolean granted = node.vote(
			request.getCandidateId(),
			request.getTerm(),
			request.getLastLogIndex(),
			request.getLastLogTerm()
		);

		return new VoteResponse(node.getCurrentTerm(), granted);
	}

	@Override
	public void appendEntries(
		AppendEntriesRequest request,
		AsyncMethodCallback<AppendEntriesResponse> resultHandler
	) {
		try {
			resultHandler.onComplete(appendEntries(request));
		} catch (Exception e) {
			resultHandler.onError(e);
		}
	}

	@Override
	public void installSnapshot(
		InstallSnapshot snapshot,
		AsyncMethodCallback<InstallSnapshotResponse> resultHandler
	) throws TException {
		try {
			resultHandler.onComplete(installSnapshot(snapshot));
		} catch (Exception e) {
			resultHandler.onError(e);
		}
	}

	@Override
	public void vote(
		VoteRequest request,
		AsyncMethodCallback<VoteResponse> resultHandler
	) throws TException {
		try {
			resultHandler.onComplete(vote(request));
		} catch (Exception e) {
			resultHandler.onError(e);
		}
	}
}
