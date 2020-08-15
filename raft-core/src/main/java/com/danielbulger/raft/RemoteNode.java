package com.danielbulger.raft;

import com.danielbulger.raft.net.RaftClient;

import java.net.InetSocketAddress;

public class RemoteNode extends Node {

	private final RaftClient client;

	public RemoteNode(int id, InetSocketAddress address) {
		super(id, address);
		this.client = new RaftClient(address);
	}

	public RaftClient getClient() {
		return client;
	}
}
