package com.danielbulger.raft.net;

import com.danielbulger.raft.LocalNode;
import com.danielbulger.raft.rpc.RaftConsensus;
import org.apache.thrift.TProcessor;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TSimpleServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

public class RaftServer {

	private static final Logger LOG = LoggerFactory.getLogger(RaftServer.class);

	private TServer server;

	public void start(LocalNode node, InetSocketAddress address) throws TTransportException {

		if (node == null) {
			throw new IllegalArgumentException("node must not be null");
		}

		if (address == null) {
			throw new IllegalArgumentException("address must not be null");
		}

		final TServerTransport transport = new TServerSocket(address);

		final TProcessor processor = new RaftConsensus.Processor<>(
			new RaftConsensusService(node)
		);

		this.server = new TSimpleServer(
			new TServer.Args(transport).processor(processor)
		);

		LOG.debug("Starting server on {}", address);

		this.server.serve();
	}

	public void shutdown() {
		if (server.isServing()) {

			LOG.debug("Shutting down the server");

			server.stop();
		}
	}
}
