package com.danielbulger.raft.util;

import com.danielbulger.raft.NodeConfiguration;
import com.danielbulger.raft.RemoteNode;
import com.google.gson.Gson;

import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class JsonRaftConfigParser extends RaftConfigParser {

	private final NodeConfiguration[] configs;

	public JsonRaftConfigParser(int localNodeId, Reader reader) throws Exception {
		super(localNodeId);

		this.configs = load(reader);
	}

	private NodeConfiguration[] load(Reader reader) throws Exception {
		final Gson gson = new Gson();

		return gson.fromJson(
			reader,
			NodeConfiguration[].class
		);
	}

	@Override
	public Optional<NodeConfiguration> getLocalNode() {

		for (final NodeConfiguration config : configs) {
			if (config.getId() == localNodeId) {
				return Optional.of(config);
			}
		}

		return Optional.empty();
	}

	@Override
	public Collection<RemoteNode> getPeers() {

		final List<RemoteNode> nodes = new ArrayList<>(configs.length - 1);

		for (final NodeConfiguration config : configs) {

			if (config.getId() != localNodeId) {

				nodes.add(new RemoteNode(config));
			}

		}

		return nodes;
	}
}
