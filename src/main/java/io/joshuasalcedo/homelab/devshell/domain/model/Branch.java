package io.joshuasalcedo.homelab.devshell.domain.model;

import java.util.Objects;

/**
 * Domain entity representing a Git branch.
 *
 * @author JoshuaSalcedo
 * @created 7/22/2025
 */
public class Branch {
    private final String name;
    private final boolean isTemporary;
    private final boolean isCurrent;
    private final String commitHash;

    public Branch(String name, boolean isTemporary, boolean isCurrent, String commitHash) {
        this.name = Objects.requireNonNull(name, "Branch name cannot be null");
        this.isTemporary = isTemporary;
        this.isCurrent = isCurrent;
        this.commitHash = commitHash;
    }

    /**
     * Creates a new regular branch
     */
    public static Branch regular(String name, boolean isCurrent, String commitHash) {
        return new Branch(name, false, isCurrent, commitHash);
    }

    /**
     * Creates a new temporary branch for smart commits
     */
    public static Branch temporary(String name, String commitHash) {
        return new Branch(name, true, false, commitHash);
    }

    /**
     * Creates the current active branch
     */
    public static Branch current(String name, String commitHash) {
        return new Branch(name, false, true, commitHash);
    }

    public String getName() {
        return name;
    }

    public boolean isTemporary() {
        return isTemporary;
    }

    public boolean isCurrent() {
        return isCurrent;
    }

    public String getCommitHash() {
        return commitHash;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Branch)) return false;
        Branch branch = (Branch) o;
        return Objects.equals(name, branch.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return String.format("Branch{name='%s', temporary=%s, current=%s}",
                name, isTemporary, isCurrent);
    }
}