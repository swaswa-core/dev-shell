package io.joshuasalcedo.homelab.devshell.domain.service;

import io.joshuasalcedo.homelab.devshell.domain.exception.DomainExceptions;
import io.joshuasalcedo.homelab.devshell.domain.model.*;
import io.joshuasalcedo.homelab.devshell.domain.repository.GitRepository;
import io.joshuasalcedo.homelab.devshell.domain.value.BranchName;
import io.joshuasalcedo.homelab.devshell.domain.value.CommitMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * Domain service that orchestrates the smart commit workflow.
 * Implements the business logic for creating safe commits with temporary branches.
 *
 * @author JoshuaSalcedo
 * @created 7/22/2025
 */
public class SmartCommitService {
    private static final Logger logger = LoggerFactory.getLogger(SmartCommitService.class);
    
    private final GitRepository gitRepository;
    private final GitValidationService validationService;

    public SmartCommitService(GitRepository gitRepository, GitValidationService validationService) {
        this.gitRepository = Objects.requireNonNull(gitRepository, "Git repository cannot be null");
        this.validationService = Objects.requireNonNull(validationService, "Validation service cannot be null");
    }

    /**
     * Executes the smart commit workflow as defined in the project requirements.
     * 
     * @param repository The repository to commit to
     * @param message The commit message
     * @return The created commit
     * @throws DomainExceptions.CommitMessageRequiredException if message is null or empty
     * @throws DomainExceptions.NotARepositoryException if not in a git repository
     * @throws DomainExceptions.NoChangesToCommitException if no changes to commit
     */
    public Commit executeSmartCommit(Repository repository, String message) {
        logger.info("Starting smart commit workflow for repository: {}", repository.getName());
        
        // Validate inputs
        if (message == null || message.trim().isEmpty()) {
            throw DomainExceptions.Factory.commitMessageRequired();
        }
        
        CommitMessage commitMessage = CommitMessage.of(message);
        
        // Step 1: Check if in git repository
        validationService.validateRepository(repository);
        
        // Step 2: Check if there are changes to commit (including untracked files)
        WorkingDirectory workingDir = gitRepository.getWorkingDirectoryStatus(repository);
        if (!workingDir.hasAnythingToShow()) {
            throw new DomainExceptions.NoChangesToCommitException();
        }
        
        // Get current branch
        Branch currentBranch = gitRepository.getCurrentBranch(repository);
        
        // Step 3: Create temporary branch
        BranchName tempBranchName = BranchName.temporary();
        Branch tempBranch = gitRepository.createBranch(repository, tempBranchName);
        logger.info("Created temporary branch: {}", tempBranchName);
        
        try {
            // Step 4: Switch to temporary branch
            gitRepository.switchToBranch(repository, tempBranch);
            
            // Step 5: Stage all files (tracked changes + untracked files)
            gitRepository.stageTrackedFiles(repository);
            
            // Also stage all untracked files
            WorkingDirectory currentWorkingDir = gitRepository.getWorkingDirectoryStatus(repository);
            if (!currentWorkingDir.getUntrackedFiles().isEmpty()) {
                gitRepository.stageFiles(repository, currentWorkingDir.getUntrackedFiles());
                logger.info("Staged {} untracked files", currentWorkingDir.getUntrackedFiles().size());
            }
            
            // Step 6: Get updated working directory status to get file list
            WorkingDirectory updatedWorkingDir = gitRepository.getWorkingDirectoryStatus(repository);
            
            // Step 7: Generate enhanced commit message with file list
            CommitMessage enhancedMessage = CommitMessage.withFileList(
                commitMessage.getValue(), 
                updatedWorkingDir.getAllModifiedFiles()
            );
            
            // Step 8: Commit on temporary branch
            Commit commit = gitRepository.createCommit(repository, enhancedMessage, tempBranch.getName());
            logger.info("Created commit on temporary branch: {}", commit.getHash());
            
            // Step 9: Switch back to original branch
            gitRepository.switchToBranch(repository, currentBranch);
            
            // Step 10: Merge temporary branch
            gitRepository.mergeBranch(repository, tempBranch, currentBranch);
            logger.info("Merged temporary branch into: {}", currentBranch.getName());
            
            // Step 11: Delete temporary branch
            gitRepository.deleteBranch(repository, tempBranch);
            logger.info("Deleted temporary branch: {}", tempBranchName);
            
            logger.info("Smart commit workflow completed successfully");
            return commit;
            
        } catch (Exception e) {
            logger.error("Smart commit workflow failed, attempting cleanup", e);
            
            // Cleanup: try to switch back to original branch and delete temp branch
            try {
                gitRepository.switchToBranch(repository, currentBranch);
                gitRepository.deleteBranch(repository, tempBranch);
            } catch (Exception cleanupException) {
                logger.warn("Cleanup failed after smart commit error", cleanupException);
            }
            
            throw e;
        }
    }

    /**
     * Executes smart commit with optional push to remote
     */
    public Commit executeSmartCommitWithPush(Repository repository, String message, boolean pushToRemote) {
        Commit commit = executeSmartCommit(repository, message);
        
        if (pushToRemote && repository.hasRemote()) {
            try {
                Branch currentBranch = gitRepository.getCurrentBranch(repository);
                gitRepository.pushBranch(repository, currentBranch);
                logger.info("Pushed changes to remote repository");
            } catch (Exception e) {
                logger.warn("Failed to push to remote repository", e);
                // Don't fail the entire operation if push fails
            }
        }
        
        return commit;
    }
}