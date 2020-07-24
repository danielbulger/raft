package com.danielbulger.raft;

import com.danielbulger.raft.net.RaftClient;
import org.apache.thrift.TException;

import java.net.InetSocketAddress;

public class RemoteNode extends Node {

	private final RaftClient client;

	private long matchIndex = 0L;

	private long nextIndex = 0L;

	public RemoteNode(int id, InetSocketAddress address) throws TException {
		super(id, address);
		this.client = new RaftClient(address);
	}

	public RaftClient getClient() {
		return client;
	}
}
