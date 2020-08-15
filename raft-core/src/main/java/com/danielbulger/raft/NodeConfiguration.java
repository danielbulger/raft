package com.danielbulger.raft;

import java.net.InetSocketAddress;

public class NodeConfiguration {

	private final int id;

	private final String host;

	private final int port;

	private final long heartBeat;

	private final long heartBeatTimeout;

	public NodeConfiguration(int id, String host, int port, long heartBeat, long heartBeatTimeout) {

		if (heartBeat <= 0) {
			throw new IllegalArgumentException("heart beat must be > 0 " + heartBeat);
		}

		if (heartBeatTimeout <= 0) {
			throw new IllegalArgumentException("heart beat timeout must be > 0 " + heartBeatTimeout);
		}

		this.id = id;
		this.heartBeatTimeout = heartBeatTimeout;
		this.heartBeat = heartBeat;
		this.host = host;
		this.port = port;
	}


	public int getId() {
		return id;
	}

	public InetSocketAddress getAddress() {
		return new InetSocketAddress(host, port);
	}

	public long getHeartBeat() {
		return heartBeat;
	}

	public long getHeartBeatTimeout() {
		return heartBeatTimeout;
	}

	@Override
	public String toString() {
		return "NodeConfiguration{" +
			"id=" + id +
			", host='" + host + '\'' +
			", port=" + port +
			", heartBeat=" + heartBeat +
			", heartBeatTimeout=" + heartBeatTimeout +
			'}';
	}
}
