package com.danielbulger.raft.service;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.ServiceLoader;

public class LogPersistenceProvider {

	private static ServiceLoader<LogPersistence> serviceLoader = ServiceLoader.load(LogPersistence.class);

	public static LogPersistence service() {
		final Optional<LogPersistence> persistence = serviceLoader.findFirst();

		if (persistence.isEmpty()) {
			throw new NoSuchElementException("No implementation for LogPersistence");
		}

		return persistence.get();
	}

	private LogPersistenceProvider() {
	}
}
