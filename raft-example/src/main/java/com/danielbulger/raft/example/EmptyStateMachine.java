package com.danielbulger.raft.example;

import com.danielbulger.raft.StateMachine;

public class EmptyStateMachine implements StateMachine {
	@Override
	public void apply(byte[] data) {

	}
}
