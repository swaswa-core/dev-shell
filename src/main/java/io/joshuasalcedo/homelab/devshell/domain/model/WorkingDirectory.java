package io.joshuasalcedo.homelab.devshell.domain.model;

import java.util.List;
import java.util.Objects;

/**
 * Domain entity representing the working directory state of a Git repository.
 * Contains information about staged, unstaged, and untracked files.
 *
 * @author JoshuaSalcedo
 * @created 7/22/2025
 */
public class WorkingDirectory {
    private final List<String> stagedFiles;
    private final List<String> unstagedFiles;
    private final List<String> untrackedFiles;
    private final boolean hasChanges;

    public WorkingDirectory(List<String> stagedFiles, List<String> unstagedFiles, List<String> untrackedFiles) {
        this.stagedFiles = List.copyOf(Objects.requireNonNull(stagedFiles, "Staged files cannot be null"));
        this.unstagedFiles = List.copyOf(Objects.requireNonNull(unstagedFiles, "Unstaged files cannot be null"));
        this.untrackedFiles = List.copyOf(Objects.requireNonNull(untrackedFiles, "Untracked files cannot be null"));
        this.hasChanges = !stagedFiles.isEmpty() || !unstagedFiles.isEmpty();
    }

    /**
     * Creates a clean working directory with no changes
     */
    public static WorkingDirectory clean() {
        return new WorkingDirectory(List.of(), List.of(), List.of());
    }

    /**
     * Creates a working directory with changes
     */
    public static WorkingDirectory withChanges(List<String> stagedFiles, List<String> unstagedFiles, List<String> untrackedFiles) {
        return new WorkingDirectory(stagedFiles, unstagedFiles, untrackedFiles);
    }

    public List<String> getStagedFiles() {
        return stagedFiles;
    }

    public List<String> getUnstagedFiles() {
        return unstagedFiles;
    }

    public List<String> getUntrackedFiles() {
        return untrackedFiles;
    }

    public boolean hasChanges() {
        return hasChanges;
    }

    /**
     * Checks if there's anything to show in status (including untracked files)
     */
    public boolean hasAnythingToShow() {
        return hasChanges || !untrackedFiles.isEmpty();
    }

    public boolean hasUnstagedChanges() {
        return !unstagedFiles.isEmpty();
    }

    public boolean hasStagedChanges() {
        return !stagedFiles.isEmpty();
    }

    /**
     * Gets all modified files (staged + unstaged, excluding untracked)
     */
    public List<String> getAllModifiedFiles() {
        return List.of(
            stagedFiles.stream(),
            unstagedFiles.stream()
        ).stream().flatMap(s -> s).distinct().toList();
    }

    /**
     * Gets total count of all changes
     */
    public int getTotalChangeCount() {
        return stagedFiles.size() + unstagedFiles.size();
    }

    @Override
    public String toString() {
        return String.format("WorkingDirectory{staged=%d, unstaged=%d, untracked=%d, hasChanges=%s}",
                stagedFiles.size(), unstagedFiles.size(), untrackedFiles.size(), hasChanges);
    }
}