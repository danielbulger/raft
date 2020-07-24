namespace java com.danielbulger.raft.rpc

typedef i64 Term

typedef i64 LogIndex

struct LogEntry {
	1: Term term;
	2: LogIndex index;
	3: binary data;
}

struct AppendEntriesRequest {
	1: Term term;
	2: i32 leaderId;
	3: LogIndex prevLogIndex;
	4: Term prevLogTerm;
	5: list<LogEntry> entries;
	6: i64 leaderCommitIndex;
}

struct AppendEntriesResponse {
	1: Term term;
	2: bool success;
}

struct VoteRequest {
	1: Term term;
	2: i32 candidateId;
	3: LogIndex lastLogIndex;
	4: Term lastLogTerm;
}

struct VoteResponse {
	1: optional Term term;
	2: optional bool granted;
}

struct InstallSnapshot {
	1: Term term;
	2: i32 leaderId;
	3: LogIndex lastIncludedIndex;
	4: Term lastIncludedTerm;
	5: i64 offset;
	6: binary data;
	7: bool completed;
}

struct InstallSnapshotResponse {
	1: Term term;
}

service RaftConsensus {

	AppendEntriesResponse appendEntries(1: AppendEntriesRequest request),

	VoteResponse vote(1: VoteRequest request),

	InstallSnapshotResponse installSnapshot(1: InstallSnapshot snapshot)
}