package io.joshuasalcedo.homelab.devshell.domain.value;

import java.util.List;
import java.util.Objects;

/**
 * Value object representing a Git commit message.
 * Encapsulates the business rules for creating valid commit messages.
 *
 * @author JoshuaSalcedo
 * @created 7/22/2025
 */
public class CommitMessage {
    private static final int MIN_LENGTH = 3;
    
    private final String value;

    private CommitMessage(String value) {
        this.value = Objects.requireNonNull(value, "Commit message cannot be null");
        validate(value);
    }

    /**
     * Creates a commit message from a user-provided string
     */
    public static CommitMessage of(String message) {
        return new CommitMessage(message.trim());
    }

    /**
     * Creates an enhanced commit message that includes the original message plus file listing
     */
    public static CommitMessage withFileList(String originalMessage, List<String> changedFiles) {
        Objects.requireNonNull(originalMessage, "Original message cannot be null");
        Objects.requireNonNull(changedFiles, "Changed files list cannot be null");
        
        if (changedFiles.isEmpty()) {
            return new CommitMessage(originalMessage.trim());
        }
        
        StringBuilder enhanced = new StringBuilder(originalMessage.trim());
        enhanced.append("\n\nFiles changed:");
        
        for (String file : changedFiles) {
            enhanced.append("\n- ").append(file);
        }
        
        return new CommitMessage(enhanced.toString());
    }

    private void validate(String message) {
        if (message.isBlank()) {
            throw new IllegalArgumentException("Commit message cannot be empty or blank");
        }
        
        if (message.length() < MIN_LENGTH) {
            throw new IllegalArgumentException(
                String.format("Commit message must be at least %d characters long", MIN_LENGTH)
            );
        }
    }

    public String getValue() {
        return value;
    }

    /**
     * Gets the first line of the commit message (summary)
     */
    public String getSummary() {
        return value.split("\n")[0];
    }

    /**
     * Checks if this is a multiline commit message
     */
    public boolean isMultiline() {
        return value.contains("\n");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CommitMessage)) return false;
        CommitMessage that = (CommitMessage) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}