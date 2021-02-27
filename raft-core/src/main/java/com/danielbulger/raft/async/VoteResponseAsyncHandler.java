package com.danielbulger.raft.async;

import com.danielbulger.raft.LocalNode;
import com.danielbulger.raft.RemoteNode;
import com.danielbulger.raft.rpc.VoteRequest;
import com.danielbulger.raft.rpc.VoteResponse;
import org.apache.thrift.async.AsyncMethodCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VoteResponseAsyncHandler implements AsyncMethodCallback<VoteResponse> {

	private static final Logger LOG = LoggerFactory.getLogger(VoteResponseAsyncHandler.class);

	private final VoteRequest request;
	private final LocalNode node;
	private final RemoteNode peer;

	/**
	 *
	 * @param request The request that is been responded to.
	 * @param node The local node that sent the {@code request}.
	 * @param peer The peer that processed and responded to the {@code request}.
	 * @throws IllegalArgumentException If {@code request}, {@code node} or {@code peer} are {@code null}.
	 */
	public VoteResponseAsyncHandler(VoteRequest request, LocalNode node, RemoteNode peer) {

		if (request == null) {
			throw new IllegalArgumentException("VoteRequest must not be null");
		}

		if (node == null) {
			throw new IllegalArgumentException("Node must not be null");
		}

		if (peer == null) {
			throw new IllegalArgumentException("Peer must not be null");
		}

		this.request = request;
		this.node = node;
		this.peer = peer;
	}

	@Override
	public void onComplete(VoteResponse response) {
		node.handleVoteResponse(request, response, peer);
	}

	@Override
	public void onError(Exception exception) {
		LOG.error(
			"Vote from {} failed due to {}/{}",
			peer.getAddress(),
			peer.getId(),
			exception.getMessage()
		);
	}
}
