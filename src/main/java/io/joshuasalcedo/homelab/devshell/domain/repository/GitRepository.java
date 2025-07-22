package io.joshuasalcedo.homelab.devshell.domain.repository;

import io.joshuasalcedo.homelab.devshell.domain.model.*;
import io.joshuasalcedo.homelab.devshell.domain.value.BranchName;
import io.joshuasalcedo.homelab.devshell.domain.value.CommitMessage;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/**
 * Domain repository interface for Git operations.
 * Defines the contract for all Git-related persistence and retrieval operations.
 * This interface belongs to the domain layer and will be implemented by infrastructure adapters.
 *
 * @author JoshuaSalcedo
 * @created 7/22/2025
 */
public interface GitRepository {

    /**
     * Discovers and loads repository information from a given path
     * 
     * @param repositoryPath The path to check for a git repository
     * @return Repository instance if found, empty if not a git repository
     */
    Optional<Repository> findRepository(Path repositoryPath);

    /**
     * Initializes a new git repository at the given path
     * 
     * @param repositoryPath The path where to initialize the repository
     * @param repositoryName The name for the repository
     * @return The newly created Repository instance
     */
    Repository initializeRepository(Path repositoryPath, String repositoryName);

    /**
     * Gets the current working directory status including staged, unstaged, and untracked files
     * 
     * @param repository The repository to check
     * @return WorkingDirectory status
     */
    WorkingDirectory getWorkingDirectoryStatus(Repository repository);

    /**
     * Gets the currently active branch
     * 
     * @param repository The repository to check
     * @return The current Branch
     */
    Branch getCurrentBranch(Repository repository);

    /**
     * Gets all branches in the repository
     * 
     * @param repository The repository to check
     * @return List of all branches
     */
    List<Branch> getAllBranches(Repository repository);

    /**
     * Creates a new branch
     * 
     * @param repository The repository to create the branch in
     * @param branchName The name for the new branch
     * @return The newly created Branch
     */
    Branch createBranch(Repository repository, BranchName branchName);

    /**
     * Switches to the specified branch
     * 
     * @param repository The repository to operate on
     * @param branch The branch to switch to
     */
    void switchToBranch(Repository repository, Branch branch);

    /**
     * Deletes the specified branch
     * 
     * @param repository The repository to operate on
     * @param branch The branch to delete
     */
    void deleteBranch(Repository repository, Branch branch);

    /**
     * Stages all tracked files that have modifications
     * 
     * @param repository The repository to operate on
     */
    void stageTrackedFiles(Repository repository);

    /**
     * Stages specific files
     * 
     * @param repository The repository to operate on
     * @param files List of file paths to stage
     */
    void stageFiles(Repository repository, List<String> files);

    /**
     * Creates a commit with the given message
     * 
     * @param repository The repository to commit to
     * @param message The commit message
     * @param branchName The branch to commit on
     * @return The created Commit
     */
    Commit createCommit(Repository repository, CommitMessage message, String branchName);

    /**
     * Merges the source branch into the target branch
     * 
     * @param repository The repository to operate on
     * @param sourceBranch The branch to merge from
     * @param targetBranch The branch to merge into
     */
    void mergeBranch(Repository repository, Branch sourceBranch, Branch targetBranch);

    /**
     * Pushes the specified branch to the remote repository
     * 
     * @param repository The repository to operate on
     * @param branch The branch to push
     */
    void pushBranch(Repository repository, Branch branch);

    /**
     * Gets commit history for the repository
     * 
     * @param repository The repository to get history from
     * @param maxCount Maximum number of commits to retrieve
     * @return List of commits ordered by date (newest first)
     */
    List<Commit> getCommitHistory(Repository repository, int maxCount);

    /**
     * Gets the configured git author for the repository
     * 
     * @param repository The repository to check
     * @return The configured author name and email
     */
    String getConfiguredAuthor(Repository repository);

    /**
     * Checks if the repository has uncommitted changes
     * 
     * @param repository The repository to check
     * @return true if there are uncommitted changes
     */
    boolean hasUncommittedChanges(Repository repository);

    /**
     * Gets the list of configured remotes
     * 
     * @param repository The repository to check
     * @return List of remote names
     */
    List<String> getRemotes(Repository repository);

    /**
     * Checks if a specific remote exists
     * 
     * @param repository The repository to check
     * @param remoteName The remote name to check for
     * @return true if the remote exists
     */
    boolean hasRemote(Repository repository, String remoteName);
}