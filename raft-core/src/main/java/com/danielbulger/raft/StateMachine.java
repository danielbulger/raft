package com.danielbulger.raft;

public interface StateMachine {

	void apply(byte[] data);
}
