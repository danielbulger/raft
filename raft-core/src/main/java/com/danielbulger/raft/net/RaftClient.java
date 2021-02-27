
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

	/**
	 * @param address The address to connect to the client to.
	 * @throws IllegalArgumentException If {@code address} is {@code null}.
	 */
	public RaftClient(InetSocketAddress address) {

		if (address == null) {
			throw new IllegalArgumentException();
		}

		this.address = address;
	}

	/**
	 * Creates a new Client connection
	 *
	 * @return The newly created Client instance.
	 * @throws Exception If the connection could not be made for any reason.
	 */
	private RaftConsensus.AsyncClient newClient() throws Exception {
		return new RaftConsensus.AsyncClient(
			new TBinaryProtocol.Factory(),
			new TAsyncClientManager(),
			new TNonblockingSocket(address.getHostString(), address.getPort())
		);
	}

	/**
	 * @param request The request data to send to the client.
	 * @param handler The completion handler that will handle the response.
	 * @throws Exception If the connection could not be made for any reason.
	 */
	public void appendEntries(
		AppendEntriesRequest request,
		AsyncMethodCallback<AppendEntriesResponse> handler
	) throws Exception {
		final RaftConsensus.AsyncClient client = newClient();
		client.appendEntries(request, handler);
	}

	/**
	 * @param request The request data to send to the client.
	 * @param handler The completion handler that will handle the response.
	 * @throws Exception If the connection could not be made for any reason.
	 */
	public void askForVote(VoteRequest request, VoteResponseAsyncHandler handler) throws Exception {
		final RaftConsensus.AsyncClient client = newClient();
		client.vote(request, handler);
	}

	/**
	 * @param callback The completion handler that will handle the response.
	 * @throws Exception If the connection could not be made for any reason.
	 */
	public void whoIsLeader(AsyncMethodCallback<Integer> callback) throws Exception {
		final RaftConsensus.AsyncClient client = newClient();
		client.whoIsLeader(callback);
	}

	/**
	 * Send a new update for the state-machine to the client.
	 *
	 * @param data     The request data to send to the client.
	 * @param callback The completion handler that will handle the response.
	 * @throws Exception If the connection could not be made for any reason.
	 */
	public void updateData(byte[] data, AsyncMethodCallback<UpdateDataResponse> callback) throws Exception {
		final RaftConsensus.AsyncClient client = newClient();
		client.updateData(ByteBuffer.wrap(data), callback);
	}
}