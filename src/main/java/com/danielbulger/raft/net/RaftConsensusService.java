package com.danielbulger.raft.net;

import com.danielbulger.raft.rpc.*;
import org.apache.thrift.TException;

public class RaftConsensusService implements RaftConsensus.Iface {

	@Override
	public AppendEntriesResponse appendEntries(AppendEntriesRequest request) throws TException {
		return null;
	}

	@Override
	public VoteResponse vote(VoteRequest request) throws TException {
		return null;
	}

	@Override
	public InstallSnapshotResponse installSnapshot(InstallSnapshot snapshot) throws TException {
		return null;
	}
}
