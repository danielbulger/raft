package com.danielbulger.raft;

public interface StateMachine {

	void apply(final byte[] payload);
}
