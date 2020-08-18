namespace java com.danielbulger.raft.rpc

typedef i64 Term

typedef i64 LogIndex

typedef i32 LeaderId

struct LogEntry {
	1: Term term;
	2: LogIndex index;
	3: binary data;
}

struct AppendEntriesRequest {
	1: Term term;
	2: LeaderId leaderId;
	3: LogIndex prevLogIndex;
	4: Term prevLogTerm;
	5: list<LogEntry> entries;
	6: i64 leaderCommitIndex;
}

struct AppendEntriesResponse {
	1: Term term;
	2: LogIndex lastLogIndex;
	3: bool success;
}

struct VoteRequest {
	1: Term term;
	2: i32 candidateId;
	3: LogIndex lastLogIndex;
	4: Term lastLogTerm;
}

struct VoteResponse {
	1: Term term;
	2: bool granted;
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

struct UpdateDataResponse {
	1: bool success;
	2: LeaderId leaderId;
}

service RaftConsensus {

	AppendEntriesResponse appendEntries(1: AppendEntriesRequest request),

	InstallSnapshotResponse installSnapshot(1: InstallSnapshot snapshot),

	VoteResponse vote(1: VoteRequest request)

	LeaderId whoIsLeader();

	UpdateDataResponse updateData(1: binary data);
}