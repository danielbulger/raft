package com.danielbulger.raft;

import com.danielbulger.raft.net.RaftClient;

import java.net.InetSocketAddress;

public class RemoteNode extends Node {

	private final RaftClient client;

	private long nextIndex;
	private long matchIndex = 0;

	public RemoteNode(int id, InetSocketAddress address) {
		super(id, address);
		this.client = new RaftClient(address);
	}

	public RemoteNode(NodeConfiguration config) {
		this(config.getId(), config.getAddress());
	}

	public RaftClient getClient() {
		return client;
	}

	public long getPrevIndex() {
		return nextIndex - 1;
	}

	public long getNextIndex() {
		return nextIndex;
	}

	public void setNextIndex(long nextIndex) {
		this.nextIndex = nextIndex;
	}

	public long getMatchIndex() {
		return matchIndex;
	}

	public void setMatchIndex(long matchIndex) {
		this.matchIndex = matchIndex;
	}

	@Override
	public String toString() {
		return "RemoteNode{" +
			"id=" + super.getId() +
			", address=" + super.getAddress() +
			'}';
	}
}
