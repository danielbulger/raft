package com.danielbulger.raft.net;

import com.danielbulger.raft.LocalNode;
import com.danielbulger.raft.NodeLog;
import com.danielbulger.raft.rpc.RaftConsensus;
import org.apache.thrift.TProcessor;
import org.apache.thrift.server.TNonblockingServer;
import org.apache.thrift.server.TServer;
import org.apache.thrift.transport.TNonblockingServerSocket;
import org.apache.thrift.transport.TNonblockingServerTransport;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RaftServer implements Runnable {

	private static final Logger LOG = LoggerFactory.getLogger(RaftServer.class);

	private final TServer server;

	public RaftServer(LocalNode node, NodeLog log) throws TTransportException {

		if (node == null) {
			throw new IllegalArgumentException("node must not be null");
		}

		final TNonblockingServerTransport transport = new TNonblockingServerSocket(node.getAddress());

		final TProcessor processor = new RaftConsensus.Processor<>(
			new RaftConsensusService(node, log)
		);

		this.server = new TNonblockingServer(
			new TNonblockingServer.Args(transport).processor(processor)
		);
	}

	@Override
	public void run() {

		LOG.debug("Starting server {}", server);

		this.server.serve();
	}

	public void shutdown() {
		if (server.isServing()) {

			LOG.debug("Shutting down the server");

			server.stop();
		}
	}
}