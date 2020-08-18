package com.danielbulger.raft.service;

import com.danielbulger.raft.NodeConfiguration;
import com.danielbulger.raft.RemoteNode;

import java.io.Reader;
import java.util.Collection;
import java.util.Optional;

public interface RaftConfigParser {

	void load(Reader reader) throws Exception;

	Optional<NodeConfiguration> getLocalNode(int localNodeId);

	Collection<RemoteNode> getPeers(int localNodeId);
}
