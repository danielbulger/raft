package com.danielbulger.raft.async;

import com.danielbulger.raft.LocalNode;
import com.danielbulger.raft.rpc.AppendEntriesResponse;
import org.apache.thrift.async.AsyncMethodCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AppendEntriesRequestAsyncHandler implements AsyncMethodCallback<AppendEntriesResponse> {

	private static final Logger LOG = LoggerFactory.getLogger(AppendEntriesRequestAsyncHandler.class);

	private final LocalNode node;

	public AppendEntriesRequestAsyncHandler(LocalNode node) {
		if (node == null) {
			throw new IllegalArgumentException();
		}

		this.node = node;
	}

	@Override
	public void onComplete(AppendEntriesResponse response) {
		
	}

	@Override
	public void onError(Exception exception) {
		LOG.error("AppendEntriesRequest failed ", exception);
	}
}
