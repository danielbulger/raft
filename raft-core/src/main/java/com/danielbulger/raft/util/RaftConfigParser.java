package com.danielbulger.raft.util;

import com.danielbulger.raft.NodeConfiguration;
import com.danielbulger.raft.RemoteNode;

import java.util.Collection;
import java.util.Optional;

public abstract class RaftConfigParser {

	protected final int localNodeId;

	protected RaftConfigParser(int localNodeId) {
		this.localNodeId = localNodeId;
	}

	public abstract Optional<NodeConfiguration> getLocalNode();

	public abstract Collection<RemoteNode> getPeers();
}
