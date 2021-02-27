package com.danielbulger.raft;

import com.danielbulger.raft.rpc.LogEntry;
import com.danielbulger.raft.service.LogPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class NodeLog {

	private static final Logger LOG = LoggerFactory.getLogger(NodeLog.class);

	private static final LogEntry EMPTY_ENTRY = new LogEntry(0, 0, null);

	private final LogPersistence persistence;
	private final NavigableMap<Long, LogEntry> entries = new TreeMap<>();

	private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
	private final Lock readLock = lock.readLock();
	private final Lock writeLock = lock.writeLock();

	public NodeLog(LogPersistence persistence) {
		this.persistence = persistence;
	}

	public void loadLogs() throws Exception {
		final Collection<LogEntry> logs = persistence.getAll();

		writeLock.lock();
		try {
			for (final LogEntry entry : logs) {
				entries.put(entry.getIndex(), entry);
			}
		} finally {
			writeLock.unlock();
		}
	}

	public void appendEntry(long term, byte[] data) {
		writeLock.lock();
		try {
			final long index = getLastEntryIndex() + 1;

			final LogEntry entry = new LogEntry(
				term,
				index,
				ByteBuffer.wrap(data)
			);

			entries.put(index, entry);
		} finally {
			writeLock.unlock();
		}
	}

	public void appendEntries(List<LogEntry> newEntries) {
		writeLock.lock();
		try {
			for (final LogEntry entry : newEntries) {
				entries.put(entry.getIndex(), entry);
			}
		} finally {
			writeLock.unlock();
		}
	}

	public void deleteNewer(long logIndex) {
		writeLock.lock();

		try {
			Long index = logIndex;
			do {
				try {
					entries.remove(index);

					persistence.delete(entries.get(index));
				} catch (Exception error) {
					LOG.error("Unable to delete entry {}", index);
				}

			} while ((index = entries.higherKey(logIndex)) != null);
		} finally {
			writeLock.unlock();
		}
	}

	public LogEntry getByIndex(long index) {
		readLock.lock();
		try {
			final LogEntry entry = entries.get(index);

			return entry == null ? EMPTY_ENTRY : entry;
		} finally {
			readLock.unlock();
		}
	}

	public List<LogEntry> getOlder(long index) {
		final long lastEntryIndex = getLastEntryIndex();
		final List<LogEntry> logs = new ArrayList<>();

		readLock.lock();
		try {
			for (long i = index; i <= lastEntryIndex; ++i) {
				logs.add(entries.get(i));
			}
		} finally {
			readLock.unlock();
		}

		return logs;
	}

	public long getFirstEntryIndex() {
		readLock.lock();
		try {
			final Map.Entry<Long, LogEntry> entry = entries.firstEntry();

			return entry == null ? 0L : entry.getValue().getIndex();
		} finally {
			readLock.unlock();
		}
	}

	public long getLastEntryIndex() {
		return getLastEntry().getIndex();
	}

	public LogEntry getLastEntry() {
		readLock.lock();
		try {
			final Map.Entry<Long, LogEntry> entry = entries.lastEntry();

			return entry == null ? EMPTY_ENTRY : entry.getValue();
		} finally {
			readLock.unlock();
		}
	}
}
