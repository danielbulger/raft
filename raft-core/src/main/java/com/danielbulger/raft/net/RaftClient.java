package com.danielbulger.raft.net;

import com.danielbulger.raft.rpc.RaftConsensus;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.net.InetSocketAddress;

public class RaftClient implements Closeable {

	private static final Logger LOG = LoggerFactory.getLogger(RaftClient.class);

	private RaftConsensus.Client client;

	private TSocket socket;

	public void open(InetSocketAddress address) throws TException {

		if (address == null) {
			throw new IllegalArgumentException();
		}

		this.socket = new TSocket(address.getHostString(), address.getPort());

		final TProtocol protocol = new TBinaryProtocol(socket);

		this.client = new RaftConsensus.Client(protocol);

		LOG.debug("Connecting to {}", address);

		this.socket.open();
	}

	@Override
	public void close() {
		if (socket.isOpen()) {

			LOG.debug("Shutting down client");

			socket.close();
		}
	}
}
