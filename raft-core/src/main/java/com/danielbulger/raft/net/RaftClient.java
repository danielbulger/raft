
package com.danielbulger.raft.net;

import com.danielbulger.raft.async.VoteResponseAsyncHandler;
import com.danielbulger.raft.rpc.*;
import org.apache.thrift.async.AsyncMethodCallback;
import org.apache.thrift.async.TAsyncClientManager;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.transport.TNonblockingSocket;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

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

	public void appendEntries(
		AppendEntriesRequest request,
		AsyncMethodCallback<AppendEntriesResponse> handler
	) throws Exception {

		final RaftConsensus.AsyncClient client = newClient();

		client.appendEntries(request, handler);
	}

	public void askForVote(VoteRequest request, VoteResponseAsyncHandler handler) throws Exception {

		final RaftConsensus.AsyncClient client = newClient();

		client.vote(request, handler);
	}

	public void whoIsLeader(AsyncMethodCallback<Integer> callback) throws Exception {
		final RaftConsensus.AsyncClient client = newClient();

		client.whoIsLeader(callback);
	}

	public void updateData(
		byte[] data,
		AsyncMethodCallback<UpdateDataResponse> callback
	) throws Exception {
		final RaftConsensus.AsyncClient client = newClient();

		client.updateData(ByteBuffer.wrap(data), callback);
	}
}