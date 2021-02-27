package com.danielbulger.raft.async;

import com.danielbulger.raft.LocalNode;
import com.danielbulger.raft.RemoteNode;
import com.danielbulger.raft.rpc.AppendEntriesRequest;
import com.danielbulger.raft.rpc.AppendEntriesResponse;
import org.apache.thrift.async.AsyncMethodCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AppendEntriesResponseAsyncHandler implements AsyncMethodCallback<AppendEntriesResponse> {

	private static final Logger LOG = LoggerFactory.getLogger(AppendEntriesResponseAsyncHandler.class);

	private final AppendEntriesRequest request;
	private final LocalNode node;
	private final RemoteNode peer;

	/**
	 *
	 * @param request The request that is been responded to.
	 * @param node The local node that sent the {@code request}.
	 * @param peer The peer that processed and responded to the {@code request}.
	 * @throws IllegalArgumentException If {@code request}, {@code node} or {@code peer} are {@code null}.
	 */
	public AppendEntriesResponseAsyncHandler(AppendEntriesRequest request, LocalNode node, RemoteNode peer) {

		if (request == null) {
			throw new IllegalArgumentException("AppendEntriesRequest must not be null");
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
	public void onComplete(AppendEntriesResponse response) {
		node.handleAppendEntryResponse(request, response, peer);
	}

	@Override
	public void onError(Exception exception) {
		LOG.error(
			"AppendEntryRequest from {} failed due to {}/{}",
			peer.getAddress(),
			peer.getId(),
			exception.getMessage()
		);
	}
}
