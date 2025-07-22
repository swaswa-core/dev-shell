package io.joshuasalcedo.homelab.devshell.domain.exception;

/**
 * Centralized domain exceptions for the application.
 * Contains all domain-specific exception types as inner static classes.
 *
 * @author JoshuaSalcedo
 * @created 7/22/2025
 */
public final class DomainExceptions {
    
    // Private constructor to prevent instantiation
    private DomainExceptions() {
        throw new UnsupportedOperationException("Utility class should not be instantiated");
    }
    
    /**
     * Error codes for domain exceptions
     */
    public static final class ErrorCodes {
        public static final String COMMIT_MESSAGE_REQUIRED = "E001";
        public static final String NO_CHANGES_TO_COMMIT = "E002";
        public static final String NOT_A_REPOSITORY = "E003";
        public static final String NO_REMOTE_REPOSITORY = "E004";
        public static final String UNAUTHORIZED_TO_COMMIT = "E005";
        
        private ErrorCodes() {}
    }
    
    /**
     * Exception thrown when a commit message is required but not provided.
     * Error Code: E001
     */
    public static class CommitMessageRequiredException extends BaseException {
        private static final String DEFAULT_MESSAGE = "Commit message is required";
        
        public CommitMessageRequiredException() {
            super(ErrorCodes.COMMIT_MESSAGE_REQUIRED, DEFAULT_MESSAGE);
        }
        
        public CommitMessageRequiredException(String additionalInfo) {
            super(ErrorCodes.COMMIT_MESSAGE_REQUIRED, 
                  DEFAULT_MESSAGE + ": " + additionalInfo);
        }
    }
    
    /**
     * Exception thrown when there are no changes to commit.
     * Error Code: E002
     */
    public static class NoChangesToCommitException extends BaseException {
        private static final String DEFAULT_MESSAGE = "No changes to commit";
        
        public NoChangesToCommitException() {
            super(ErrorCodes.NO_CHANGES_TO_COMMIT, DEFAULT_MESSAGE);
        }
        
        public NoChangesToCommitException(String branch) {
            super(ErrorCodes.NO_CHANGES_TO_COMMIT, 
                  DEFAULT_MESSAGE + " on branch: " + branch);
        }
    }
    
    /**
     * Exception thrown when the current directory is not a repository.
     * Error Code: E003
     */
    public static class NotARepositoryException extends BaseException {
        private static final String DEFAULT_MESSAGE = "Not a repository, please init this first";
        
        public NotARepositoryException() {
            super(ErrorCodes.NOT_A_REPOSITORY, DEFAULT_MESSAGE);
        }
        
        public NotARepositoryException(String path) {
            super(ErrorCodes.NOT_A_REPOSITORY, 
                  DEFAULT_MESSAGE + " at: " + path);
        }
        
        public NotARepositoryException(String path, Throwable cause) {
            super(ErrorCodes.NOT_A_REPOSITORY, 
                  DEFAULT_MESSAGE + " at: " + path, cause);
        }
    }
    
    /**
     * Exception thrown when no remote repository is configured.
     * Error Code: E004
     */
    public static class NoRemoteRepositoryException extends BaseException {
        private static final String DEFAULT_MESSAGE = "No remote repository";
        
        public NoRemoteRepositoryException() {
            super(ErrorCodes.NO_REMOTE_REPOSITORY, DEFAULT_MESSAGE);
        }
        
        public NoRemoteRepositoryException(String remoteName) {
            super(ErrorCodes.NO_REMOTE_REPOSITORY, 
                  DEFAULT_MESSAGE + " configured for: " + remoteName);
        }
        
        public NoRemoteRepositoryException(String remoteName, String branch) {
            super(ErrorCodes.NO_REMOTE_REPOSITORY, 
                  DEFAULT_MESSAGE + " configured for remote '" + remoteName + 
                  "' on branch '" + branch + "'");
        }
    }
    
    /**
     * Exception thrown when user is unauthorized to commit.
     * Error Code: E005
     */
    public static class UnauthorizedToCommitException extends BaseException {
        private static final String DEFAULT_MESSAGE = "UNAUTHORIZED to commit a message!";
        
        public UnauthorizedToCommitException() {
            super(ErrorCodes.UNAUTHORIZED_TO_COMMIT, DEFAULT_MESSAGE);
        }
        
        public UnauthorizedToCommitException(String username) {
            super(ErrorCodes.UNAUTHORIZED_TO_COMMIT, 
                  "User '" + username + "' is " + DEFAULT_MESSAGE);
        }
        
        public UnauthorizedToCommitException(String username, String repository) {
            super(ErrorCodes.UNAUTHORIZED_TO_COMMIT, 
                  "User '" + username + "' is UNAUTHORIZED to commit to repository: " + repository);
        }
        
        public UnauthorizedToCommitException(Throwable cause) {
            super(ErrorCodes.UNAUTHORIZED_TO_COMMIT, DEFAULT_MESSAGE, cause);
        }
    }
    
    /**
     * Factory methods for creating exceptions
     */
    public static class Factory {
        
        public static CommitMessageRequiredException commitMessageRequired() {
            return new CommitMessageRequiredException();
        }
        
        public static NoChangesToCommitException noChangesToCommit() {
            return new NoChangesToCommitException();
        }
        
        public static NotARepositoryException notARepository(String path) {
            return new NotARepositoryException(path);
        }
        
        public static NoRemoteRepositoryException noRemoteRepository() {
            return new NoRemoteRepositoryException();
        }
        
        public static UnauthorizedToCommitException unauthorizedToCommit(String username) {
            return new UnauthorizedToCommitException(username);
        }
    }
}