package io.joshuasalcedo.homelab.devshell.domain.model;

import java.nio.file.Path;
import java.util.Objects;

/**
 * Domain entity representing a Git repository.
 * This is the aggregate root for all git operations within the repository context.
 *
 * @author JoshuaSalcedo
 * @created 7/22/2025
 */
public class Repository {
    private final Path rootPath;
    private final String name;
    private final boolean isInitialized;
    private final boolean hasRemote;
    private final String defaultBranch;

    public Repository(Path rootPath, String name, boolean isInitialized, boolean hasRemote, String defaultBranch) {
        this.rootPath = Objects.requireNonNull(rootPath, "Root path cannot be null");
        this.name = Objects.requireNonNull(name, "Repository name cannot be null");
        this.isInitialized = isInitialized;
        this.hasRemote = hasRemote;
        this.defaultBranch = defaultBranch;
    }

    /**
     * Creates a new Repository instance for an existing git repository
     */
    public static Repository existing(Path rootPath, String name, boolean hasRemote, String defaultBranch) {
        return new Repository(rootPath, name, true, hasRemote, defaultBranch);
    }

    /**
     * Creates a new Repository instance for a non-initialized directory
     */
    public static Repository uninitialized(Path rootPath, String name) {
        return new Repository(rootPath, name, false, false, null);
    }

    public Path getRootPath() {
        return rootPath;
    }

    public String getName() {
        return name;
    }

    public boolean isInitialized() {
        return isInitialized;
    }

    public boolean hasRemote() {
        return hasRemote;
    }

    public String getDefaultBranch() {
        return defaultBranch;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Repository)) return false;
        Repository that = (Repository) o;
        return Objects.equals(rootPath, that.rootPath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rootPath);
    }

    @Override
    public String toString() {
        return String.format("Repository{name='%s', path='%s', initialized=%s, hasRemote=%s}",
                name, rootPath, isInitialized, hasRemote);
    }
}