package com.danielbulger.raft.net;

import com.danielbulger.raft.LocalNode;
import com.danielbulger.raft.async.AppendEntriesRequestAsyncHandler;
import com.danielbulger.raft.async.VoteRequestAsyncHandler;
import com.danielbulger.raft.rpc.AppendEntriesRequest;
import com.danielbulger.raft.rpc.LogEntry;
import com.danielbulger.raft.rpc.RaftConsensus;
import com.danielbulger.raft.rpc.VoteRequest;
import org.apache.thrift.TException;
import org.apache.thrift.async.TAsyncClientManager;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TNonblockingSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;

public class RaftClient implements Closeable {

	private static final Logger LOG = LoggerFactory.getLogger(RaftClient.class);

	private RaftConsensus.AsyncClient client;

	private TNonblockingSocket socket;

	private final InetSocketAddress address;

	public RaftClient(InetSocketAddress address) {

		if (address == null) {
			throw new IllegalArgumentException();
		}

		this.address = address;
	}

	private void connect() throws TException {
		if (socket == null || !socket.isOpen()) {

			try {
				this.socket = new TNonblockingSocket(address.getHostString(), address.getPort());

				final TProtocol protocol = new TBinaryProtocol(socket);

				this.client = new RaftConsensus.AsyncClient(
					new TBinaryProtocol.Factory(),
					new TAsyncClientManager(),
					this.socket
				);

				LOG.debug("Connecting to {}", address);

				this.socket.open();

			} catch (IOException e) {
				throw new TException(e);
			}
		}
	}


	public void sendAppendLogEntryRequest(
		LocalNode leader,
		List<LogEntry> entries
	) throws TException {

		if (leader == null) {
			throw new IllegalArgumentException("Leader must not be null");
		}

		if (!leader.isLeader()) {
			throw new IllegalStateException();
		}

		this.connect();

		client.appendEntries(new AppendEntriesRequest(
			leader.getCurrentTerm(),
			leader.getId(),
			leader.getLastLogIndex(),
			leader.getLastLogTerm(),
			entries,
			leader.getCommitIndex()
		), new AppendEntriesRequestAsyncHandler(leader));
	}

	public void sendVoteRequest(LocalNode candidate) throws TException {

		if (candidate == null) {
			throw new IllegalArgumentException();
		}

		this.connect();

		client.vote(new VoteRequest(
			candidate.getCurrentTerm(),
			candidate.getId(),
			candidate.getLastLogIndex(),
			candidate.getLastLogTerm()
		), new VoteRequestAsyncHandler(candidate));
	}

	@Override
	public void close() {
		if (socket.isOpen()) {

			LOG.debug("Shutting down client {}", address);

			socket.close();

			socket = null;
		}
	}
}
