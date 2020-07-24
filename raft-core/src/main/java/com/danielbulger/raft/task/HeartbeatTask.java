package com.danielbulger.raft.task;

import com.danielbulger.raft.LocalNode;

public class HeartbeatTask implements Runnable {

	private final LocalNode leader;

	public HeartbeatTask(LocalNode leader) {

		if (leader == null) {
			throw new IllegalArgumentException();
		}

		this.leader = leader;
	}

	@Override
	public void run() {
		leader.sendHeartbeat();
	}
}
