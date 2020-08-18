package com.danielbulger.raft.rpc;

import java.nio.ByteBuffer;

public class LogEntryFactory {

	private static final LogEntry EMPTY_LOG_ENTRY = new LogEntry(0L, 0L, null);

	public static LogEntry emptyEntry() {
		return EMPTY_LOG_ENTRY;
	}

	public static LogEntry makeEntry(long term, long index, ByteBuffer data) {
		return new LogEntry(term, index, data);
	}

	private LogEntryFactory() {
	}
}
