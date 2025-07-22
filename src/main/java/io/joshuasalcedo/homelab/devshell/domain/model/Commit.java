package io.joshuasalcedo.homelab.devshell.domain.model;

import io.joshuasalcedo.homelab.devshell.domain.value.CommitMessage;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * Domain entity representing a Git commit.
 *
 * @author JoshuaSalcedo
 * @created 7/22/2025
 */
public class Commit {
    private final String hash;
    private final CommitMessage message;
    private final String author;
    private final LocalDateTime timestamp;
    private final List<String> changedFiles;
    private final String branchName;

    public Commit(String hash, CommitMessage message, String author, LocalDateTime timestamp,
                  List<String> changedFiles, String branchName) {
        this.hash = hash;
        this.message = Objects.requireNonNull(message, "Commit message cannot be null");
        this.author = Objects.requireNonNull(author, "Author cannot be null");
        this.timestamp = Objects.requireNonNull(timestamp, "Timestamp cannot be null");
        this.changedFiles = List.copyOf(Objects.requireNonNull(changedFiles, "Changed files cannot be null"));
        this.branchName = Objects.requireNonNull(branchName, "Branch name cannot be null");
    }

    /**
     * Creates a new commit for the smart commit workflow
     */
    public static Commit forSmartCommit(CommitMessage message, String author, List<String> changedFiles, String branchName) {
        return new Commit(null, message, author, LocalDateTime.now(), changedFiles, branchName);
    }

    /**
     * Creates a commit with all details (typically from git history)
     */
    public static Commit fromHistory(String hash, CommitMessage message, String author, LocalDateTime timestamp,
                                   List<String> changedFiles, String branchName) {
        return new Commit(hash, message, author, timestamp, changedFiles, branchName);
    }

    public String getHash() {
        return hash;
    }

    public CommitMessage getMessage() {
        return message;
    }

    public String getAuthor() {
        return author;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public List<String> getChangedFiles() {
        return changedFiles;
    }

    public String getBranchName() {
        return branchName;
    }

    public boolean hasChanges() {
        return !changedFiles.isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Commit)) return false;
        Commit commit = (Commit) o;
        return Objects.equals(hash, commit.hash) || 
               (hash == null && commit.hash == null && 
                Objects.equals(message, commit.message) && 
                Objects.equals(timestamp, commit.timestamp));
    }

    @Override
    public int hashCode() {
        return hash != null ? Objects.hash(hash) : Objects.hash(message, timestamp);
    }

    @Override
    public String toString() {
        return String.format("Commit{hash='%s', message='%s', author='%s', files=%d}",
                hash, message.getValue(), author, changedFiles.size());
    }
}