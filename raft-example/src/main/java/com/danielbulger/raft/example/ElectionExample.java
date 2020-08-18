package com.danielbulger.raft.example;

import com.danielbulger.raft.LocalNode;
import com.danielbulger.raft.NodeConfiguration;
import com.danielbulger.raft.net.RaftServer;
import com.danielbulger.raft.util.JsonRaftConfigParser;
import com.danielbulger.raft.util.RaftConfigParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileReader;
import java.io.Reader;
import java.net.URL;
import java.util.Optional;
import java.util.Scanner;
import java.util.concurrent.Executors;

public class ElectionExample {

	private static final Logger LOG = LoggerFactory.getLogger(ElectionExample.class);

	public static void main(final String[] args) throws Exception {

		int localNodeId;

		try (Scanner scanner = new Scanner(System.in)) {
			localNodeId = scanner.nextInt();
		}

		LOG.info("Initialising with local node {}", localNodeId);

		final RaftConfigParser parser;

		final URL url = ElectionExample.class.getClassLoader().getResource("config.json");

		if(url == null) {
			LOG.error("No config file found");
			System.exit(1);
		}

		try (final Reader reader = new FileReader(url.getFile())) {
			parser = new JsonRaftConfigParser(localNodeId, reader);
		}

		final Optional<NodeConfiguration> optional = parser.getLocalNode();

		if (optional.isEmpty()) {
			LOG.error("No node with id {}", localNodeId);

			System.exit(1);
		}

		final LocalNode node = new LocalNode(
			new EmptyStateMachine(),
			optional.get(),
			Executors.newScheduledThreadPool(2),
			parser.getPeers()
		);

		node.scheduleElection();

		final RaftServer server = new RaftServer(node);

		server.run();
	}
}
