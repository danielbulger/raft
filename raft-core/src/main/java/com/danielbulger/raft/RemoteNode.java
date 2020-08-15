package com.danielbulger.raft;

import com.danielbulger.raft.net.RaftClient;

import java.net.InetSocketAddress;

public class RemoteNode extends Node {

	private final RaftClient client;

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

	@Override
	public String toString() {
		return "RemoteNode{" +
			"id=" + super.getId() +
			", address=" + super.getAddress() +
			'}';
	}
}
