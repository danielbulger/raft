package com.danielbulger.raft;

import java.net.InetSocketAddress;

public class LocalNodeConfiguration {

	private final int id;

	private final InetSocketAddress address;

	private final long heartBeat;

	private final long heartBeatTimeout;

	public LocalNodeConfiguration(int id, InetSocketAddress address, long heartBeat, long heartBeatTimeout) {
		if (address == null) {
			throw new IllegalArgumentException("address is null");
		}

		if (heartBeat <= 0) {
			throw new IllegalArgumentException("heart beat must be > 0 " + heartBeat);
		}

		if (heartBeatTimeout <= 0) {
			throw new IllegalArgumentException("heart beat timeout must be > 0 " + heartBeatTimeout);
		}

		this.id = id;
		this.address = address;
		this.heartBeatTimeout = heartBeatTimeout;
		this.heartBeat = heartBeat;
	}


	public int getId() {
		return id;
	}

	public InetSocketAddress getAddress() {
		return address;
	}

	public long getHeartBeat() {
		return heartBeat;
	}

	public long getHeartBeatTimeout() {
		return heartBeatTimeout;
	}
}