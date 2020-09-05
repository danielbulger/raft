package com.danielbulger.raft.service;

import com.danielbulger.raft.NodeConfiguration;
import com.danielbulger.raft.rpc.LogEntry;
import com.danielbulger.raft.rpc.MetaData;

import java.util.Collection;
import java.util.Optional;

public interface LogPersistence {

	void initialise(NodeConfiguration configuration);

	void updateMetaData(MetaData metaData) throws Exception;

	Optional<MetaData> getLatestMetaData();

	Collection<LogEntry> getAll();

	Optional<LogEntry> getEntryByIndex(long index);

	void save(LogEntry entry) throws Exception;

	void delete(LogEntry entry) throws Exception;
}
