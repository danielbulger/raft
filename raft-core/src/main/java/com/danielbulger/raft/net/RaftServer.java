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

	/**
	 * @param node
	 * @param log
	 * @throws TTransportException
	 */
	public RaftServer(LocalNode node, NodeLog log) throws TTransportException {

		final TProcessor processor = new RaftConsensus.Processor<>(
			new RaftConsensusService(node, log)
		);

		final TNonblockingServerTransport transport = new TNonblockingServerSocket(node.getAddress());
		this.server = new TNonblockingServer(
			new TNonblockingServer.Args(transport).processor(processor)
		);
	}

	@Override
	public void run() {
		LOG.debug("Starting server {}", server);
		this.server.serve();
	}

	/**
	 * Shutdown the server and don't process anymore incoming
	 * connections.
	 */
	public void shutdown() {
		if (server.isServing()) {
			LOG.debug("Shutting down the server");
			server.stop();
		}
	}
}