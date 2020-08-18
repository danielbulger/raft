package com.danielbulger.raft.service.persistence;

import com.danielbulger.raft.NodeConfiguration;
import com.danielbulger.raft.rpc.LogEntry;
import com.danielbulger.raft.rpc.LogEntryFactory;
import com.danielbulger.raft.service.LogPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

public class Sqlite3LogPersistence implements LogPersistence {

	private static final Logger LOG = LoggerFactory.getLogger(Sqlite3LogPersistence.class);

	private String database;

	@Override
	public void initialise(NodeConfiguration configuration) {
		database = configuration.getStorageFile();
	}

	private Connection connect() throws Exception {

		final URL url = getClass().getClassLoader().getResource(database);

		if (url == null) {
			throw new Exception("Unable to find log database");
		}

		final Connection connection = DriverManager.getConnection("jdbc:sqlite:" + url.getFile());

		System.out.println(url.getFile());

		return connection;
	}

	@Override
	public Collection<LogEntry> getAll() {
		final String sql = "SELECT id, term, data FROM log";

		final Collection<LogEntry> entries = new ArrayList<>();

		try (final Connection connection = connect();
			 final PreparedStatement stmt = connection.prepareStatement(sql)) {

			final ResultSet rs = stmt.executeQuery();

			if (rs.next()) {

				final long id = rs.getLong(1);

				final long term = rs.getLong(2);

				final byte[] data = rs.getBytes(3);

				entries.add(LogEntryFactory.makeEntry(term, id, data));
			}

		} catch (Exception exception) {
			LOG.error("Unable to fetch log entries", exception);
		}

		return entries;
	}

	@Override
	public Optional<LogEntry> getEntryByIndex(long index) {
		final String sql = "SELECT id, term, data FROM log WHERE id = ?";

		try (final Connection connection = connect();
			 final PreparedStatement stmt = connection.prepareStatement(sql)) {

			stmt.setLong(1, index);

			final ResultSet rs = stmt.executeQuery();

			if (rs.next()) {

				final long id = rs.getLong(1);

				final long term = rs.getLong(2);

				final byte[] data = rs.getBytes(3);

				return Optional.of(LogEntryFactory.makeEntry(term, id, data));
			}

		} catch (Exception exception) {
			LOG.error(
				"Unable to fetch log entry id={} due to {}",
				index,
				exception.getMessage()
			);
		}

		return Optional.empty();
	}

	@Override
	public void save(LogEntry entry) throws Exception {

		final String sql = "INSERT INTO log(id, term, data) VALUES(?, ?, ?)";

		try (final Connection connection = connect()) {

			try (final PreparedStatement stmt = connection.prepareStatement(sql)) {

				stmt.setLong(1, entry.getIndex());

				stmt.setLong(2, entry.getTerm());

				stmt.setBytes(3, entry.getData());

				stmt.executeUpdate();

				connection.commit();

			} catch (Exception exception) {
				connection.rollback();

				throw exception;
			}
		}
	}

	@Override
	public void delete(LogEntry entry) throws Exception {

		final String sql = "DELETE FROM log WHERE id = ?";

		try (final Connection connection = connect()) {

			try (final PreparedStatement stmt = connection.prepareStatement(sql)) {

				stmt.setLong(1, entry.getIndex());

				stmt.executeUpdate();

				connection.commit();

			} catch (Exception exception) {
				connection.rollback();

				throw exception;
			}
		}
	}
}
