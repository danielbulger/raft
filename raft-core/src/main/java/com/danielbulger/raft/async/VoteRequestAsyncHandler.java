package com.danielbulger.raft.async;

import com.danielbulger.raft.LocalNode;
import com.danielbulger.raft.rpc.VoteResponse;
import org.apache.thrift.async.AsyncMethodCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VoteRequestAsyncHandler implements AsyncMethodCallback<VoteResponse> {

	private static final Logger LOG = LoggerFactory.getLogger(VoteRequestAsyncHandler.class);

	private final LocalNode node;

	public VoteRequestAsyncHandler(LocalNode node) {

		if (node == null) {
			throw new IllegalArgumentException();
		}

		this.node = node;
	}

	@Override
	public void onComplete(VoteResponse response) {

		node.onVoteResponse(
			response.isGranted(),
			response.getTerm()
		);
	}

	@Override
	public void onError(Exception exception) {
		LOG.error("Vote request failed ", exception);
	}
}
