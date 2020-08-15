
package com.danielbulger.raft.net;

import com.danielbulger.raft.async.VoteResponseAsyncHandler;
import com.danielbulger.raft.rpc.RaftConsensus;
import com.danielbulger.raft.rpc.VoteRequest;
import org.apache.thrift.async.TAsyncClientManager;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.transport.TNonblockingSocket;

import java.net.InetSocketAddress;

public class RaftClient {

	private final InetSocketAddress address;

	public RaftClient(InetSocketAddress address) {

		if (address == null) {
			throw new IllegalArgumentException();
		}

		this.address = address;
	}

	private RaftConsensus.AsyncClient newClient() throws Exception {
		return new RaftConsensus.AsyncClient(
			new TBinaryProtocol.Factory(),
			new TAsyncClientManager(),
			new TNonblockingSocket(address.getHostString(), address.getPort())
		);
	}

	public void askForVote(VoteRequest request, VoteResponseAsyncHandler handler) throws Exception {

		final RaftConsensus.AsyncClient client = newClient();

		client.vote(request, handler);
	}
}