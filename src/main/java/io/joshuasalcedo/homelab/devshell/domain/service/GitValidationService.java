package io.joshuasalcedo.homelab.devshell.domain.service;

import io.joshuasalcedo.homelab.devshell.utils.CliLogger;

import io.joshuasalcedo.homelab.devshell.domain.exception.DomainExceptions;
import io.joshuasalcedo.homelab.devshell.domain.model.Repository;
import io.joshuasalcedo.homelab.devshell.domain.value.Author;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 * Domain service for validating Git operations and repository state.
 * Encapsulates all validation business rules for Git operations.
 *
 * @author JoshuaSalcedo
 * @created 7/22/2025
 */
public class GitValidationService {
    
    /**
     * Validates that a repository is properly initialized and accessible
     * 
     * @param repository The repository to validate
     * @throws DomainExceptions.NotARepositoryException if repository is not initialized or accessible
     */
    public void validateRepository(Repository repository) {
        Objects.requireNonNull(repository, "Repository cannot be null");
        
        // Check if directory exists
        if (!Files.exists(repository.getRootPath())) {
            CliLogger.error("Repository path does not exist: {}", repository.getRootPath());
            throw new DomainExceptions.NotARepositoryException(repository.getRootPath().toString());
        }
        
        // Check if it's a directory
        if (!Files.isDirectory(repository.getRootPath())) {
            CliLogger.error("Repository path is not a directory: {}", repository.getRootPath());
            throw new DomainExceptions.NotARepositoryException(repository.getRootPath().toString());
        }
        
        // Check if repository is initialized
        if (!repository.isInitialized()) {
            CliLogger.error("Repository is not initialized: {}", repository.getRootPath());
            throw new DomainExceptions.NotARepositoryException(repository.getRootPath().toString());
        }
        
        // Check if .git directory exists
        Path gitDir = repository.getRootPath().resolve(".git");
        if (!Files.exists(gitDir)) {
            CliLogger.error("Git directory not found: {}", gitDir);
            throw new DomainExceptions.NotARepositoryException(repository.getRootPath().toString());
        }
        
        CliLogger.debug("Repository validation passed for: {}", repository.getName());
    }

    /**
     * Validates author information for commit operations
     * 
     * @param author The author to validate
     * @param repository The repository context
     * @throws DomainExceptions.UnauthorizedToCommitException if author is not valid for commits
     */
    public void validateAuthor(Author author, Repository repository) {
        Objects.requireNonNull(author, "Author cannot be null");
        Objects.requireNonNull(repository, "Repository cannot be null");
        
        // Basic validation - author object itself validates name/email format
        // Additional business rules can be added here
        
        // Example: Check if author name is in blocked list (placeholder for future enhancement)
        if (isBlockedUser(author.getName())) {
            CliLogger.error("User '{}' is not authorized to commit", author.getName());
            throw new DomainExceptions.UnauthorizedToCommitException(author.getName());
        }
        
        CliLogger.debug("Author validation passed for: {}", author.getName());
    }

    /**
     * Validates remote repository configuration when needed
     * 
     * @param repository The repository to validate
     * @param remoteName The remote name to check (e.g., "origin")
     * @throws DomainExceptions.NoRemoteRepositoryException if remote is not configured
     */
    public void validateRemoteRepository(Repository repository, String remoteName) {
        Objects.requireNonNull(repository, "Repository cannot be null");
        Objects.requireNonNull(remoteName, "Remote name cannot be null");
        
        if (!repository.hasRemote()) {
            CliLogger.error("No remote repository configured for: {}", remoteName);
            throw new DomainExceptions.NoRemoteRepositoryException(remoteName);
        }
        
        CliLogger.debug("Remote repository validation passed for: {}", remoteName);
    }

    /**
     * Validates that a commit message meets business requirements
     * This is handled by the CommitMessage value object itself, but additional
     * business rules can be added here if needed.
     */
    public void validateCommitMessage(String message, Repository repository) {
        Objects.requireNonNull(message, "Commit message cannot be null");
        Objects.requireNonNull(repository, "Repository cannot be null");
        
        if (message.trim().isEmpty()) {
            throw DomainExceptions.Factory.commitMessageRequired();
        }
        
        // Additional business rules can be added here
        // For example: check for required patterns, forbidden words, etc.
        
        CliLogger.debug("Commit message validation passed");
    }

    /**
     * Placeholder method for checking blocked users
     * In a real implementation, this might check against a database or configuration
     */
    private boolean isBlockedUser(String username) {
        // Placeholder implementation - could be enhanced to check against
        // a configuration file, database, or external service
        return "blocked".equalsIgnoreCase(username) || "anonymous".equalsIgnoreCase(username);
    }
}