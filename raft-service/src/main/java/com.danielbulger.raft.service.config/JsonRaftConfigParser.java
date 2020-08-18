package com.danielbulger.raft.service.config;

import com.danielbulger.raft.NodeConfiguration;
import com.danielbulger.raft.RemoteNode;
import com.danielbulger.raft.service.RaftConfigParser;
import com.google.gson.Gson;

import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class JsonRaftConfigParser implements RaftConfigParser {

	private NodeConfiguration[] configs;

	public void load(Reader reader) throws Exception {
		final Gson gson = new Gson();

		configs = gson.fromJson(
			reader,
			NodeConfiguration[].class
		);
	}

	@Override
	public Optional<NodeConfiguration> getLocalNode(int localNodeId) {

		for (final NodeConfiguration config : configs) {
			if (config.getId() == localNodeId) {
				return Optional.of(config);
			}
		}

		return Optional.empty();
	}

	@Override
	public Collection<RemoteNode> getPeers(int localNodeId) {

		final List<RemoteNode> nodes = new ArrayList<>(configs.length - 1);

		for (final NodeConfiguration config : configs) {

			if (config.getId() != localNodeId) {

				nodes.add(new RemoteNode(config));
			}

		}

		return nodes;
	}
}
