package com.danielbulger.raft.service;

import com.danielbulger.raft.NodeConfiguration;
import com.danielbulger.raft.rpc.LogEntry;
import com.danielbulger.raft.rpc.MetaData;

import java.util.Collection;
import java.util.Optional;

public interface LogPersistence {

	/**
	 * Initialise this persistence with the given configuration.
	 *
	 * @param configuration The configuration for the Node.
	 */
	void initialise(NodeConfiguration configuration);

	/**
	 * Update the current metadata.
	 *
	 * @param metaData The new metadata state.
	 * @throws Exception If the metadata could not be updated for any reason.
	 */
	void updateMetaData(MetaData metaData) throws Exception;

	/**
	 * Get the latest state of the metadata.
	 *
	 * @return The metadata if any exists, otherwise an empty {@link Optional}.
	 */
	Optional<MetaData> getLatestMetaData();

	/**
	 * Get all the stored {@link LogEntry}s
	 *
	 * @return A {@link Collection} containing the {@link LogEntry}s if any exist
	 */
	Collection<LogEntry> getAll();

	/**
	 * Get the {@link LogEntry} by its index.
	 *
	 * @param index The index of the {@link LogEntry}.
	 * @return The {@link LogEntry} if one exists with the
	 * given {@code index}, or an empty {@link Optional} otherwise.
	 */
	Optional<LogEntry> getEntryByIndex(long index);

	/**
	 * Get the most recent {@link LogEntry}.
	 *
	 * @return The last {@link LogEntry} if one exists  or an empty {@link Optional} otherwise.
	 */
	Optional<LogEntry> getLastEntry();

	/**
	 * Store the given {@link LogEntry}.
	 *
	 * @param entry The {@link LogEntry} that will be saved.
	 * @throws Exception If the {@link LogEntry} could not be saved for what ever reason.
	 */
	void save(LogEntry entry) throws Exception;

	/**
	 * Deletes the given {@link LogEntry}.
	 *
	 * @param entry The {@link LogEntry} that will be deleted.
	 * @throws Exception If the {@link LogEntry} could not be deleted for what ever reason.
	 */
	void delete(LogEntry entry) throws Exception;
}
