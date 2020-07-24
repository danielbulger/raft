package com.danielbulger.raft.task;

import com.danielbulger.raft.LocalNode;

public class ElectionTask implements Runnable {

	private final LocalNode candidate;

	public ElectionTask(LocalNode candidate) {
		if (candidate == null) {
			throw new IllegalArgumentException();
		}

		this.candidate = candidate;
	}

	@Override
	public void run() {
		this.candidate.askForVotes();
	}
}
