package com.danielbulger.raft.service;

import com.danielbulger.raft.NodeConfiguration;
import com.danielbulger.raft.rpc.LogEntry;

import java.util.Collection;
import java.util.Optional;

public interface LogPersistence {

	void initialise(NodeConfiguration configuration);

	Collection<LogEntry> getAll();

	Optional<LogEntry> getEntryByIndex(long index);

	void save(LogEntry entry) throws Exception;

	void delete(LogEntry entry) throws Exception;
}
