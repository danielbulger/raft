package com.danielbulger.raft.service;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.ServiceLoader;

public class RaftConfigParserProvider {

	private static ServiceLoader<RaftConfigParser> serviceLoader = ServiceLoader.load(RaftConfigParser.class);

	public static RaftConfigParser service() {
		final Optional<RaftConfigParser> persistence = serviceLoader.findFirst();

		if (persistence.isEmpty()) {
			throw new NoSuchElementException("No implementation for RaftConfigParser");
		}

		return persistence.get();
	}

	private RaftConfigParserProvider() {
	}
}
