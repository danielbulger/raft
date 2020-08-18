package com.danielbulger.raft.example;

import com.danielbulger.raft.StateMachine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EmptyStateMachine implements StateMachine {

	private static final Logger LOG = LoggerFactory.getLogger(EmptyStateMachine.class);

	@Override
	public void apply(byte[] data) {
		final String msg = new String(data);

		LOG.info("Received msg={}", msg);
	}
}
