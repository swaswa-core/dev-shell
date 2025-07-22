package io.joshuasalcedo.homelab.devshell.domain.exception;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the DomainExceptions class and its inner exception classes.
 */
class DomainExceptionsTest {

    @Test
    void testUtilityClassConstructor() {
        // The constructor should throw UnsupportedOperationException, wrapped in InvocationTargetException
        var exception = assertThrows(java.lang.reflect.InvocationTargetException.class, () -> {
            // Using reflection to access the private constructor
            var constructor = DomainExceptions.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            constructor.newInstance();
        });
        
        // Verify the cause is UnsupportedOperationException
        assertTrue(exception.getCause() instanceof UnsupportedOperationException);
        assertEquals("Utility class should not be instantiated", exception.getCause().getMessage());
    }

    @Test
    void testErrorCodes() {
        // Verify error codes are defined correctly
        assertEquals("E001", DomainExceptions.ErrorCodes.COMMIT_MESSAGE_REQUIRED);
        assertEquals("E002", DomainExceptions.ErrorCodes.NO_CHANGES_TO_COMMIT);
        assertEquals("E003", DomainExceptions.ErrorCodes.NOT_A_REPOSITORY);
        assertEquals("E004", DomainExceptions.ErrorCodes.NO_REMOTE_REPOSITORY);
        assertEquals("E005", DomainExceptions.ErrorCodes.UNAUTHORIZED_TO_COMMIT);
    }

    // Tests for CommitMessageRequiredException
    @Test
    void testCommitMessageRequiredException_DefaultConstructor() {
        // When
        var exception = new DomainExceptions.CommitMessageRequiredException();
        
        // Then
        assertEquals(DomainExceptions.ErrorCodes.COMMIT_MESSAGE_REQUIRED, exception.getErrorCode());
        assertEquals("Commit message is required", exception.getMessage());
    }

    @Test
    void testCommitMessageRequiredException_WithAdditionalInfo() {
        // Given
        String additionalInfo = "for feature branch";
        
        // When
        var exception = new DomainExceptions.CommitMessageRequiredException(additionalInfo);
        
        // Then
        assertEquals(DomainExceptions.ErrorCodes.COMMIT_MESSAGE_REQUIRED, exception.getErrorCode());
        assertEquals("Commit message is required: for feature branch", exception.getMessage());
    }

    // Tests for NoChangesToCommitException
    @Test
    void testNoChangesToCommitException_DefaultConstructor() {
        // When
        var exception = new DomainExceptions.NoChangesToCommitException();
        
        // Then
        assertEquals(DomainExceptions.ErrorCodes.NO_CHANGES_TO_COMMIT, exception.getErrorCode());
        assertEquals("No changes to commit", exception.getMessage());
    }

    @Test
    void testNoChangesToCommitException_WithBranch() {
        // Given
        String branch = "feature/new-ui";
        
        // When
        var exception = new DomainExceptions.NoChangesToCommitException(branch);
        
        // Then
        assertEquals(DomainExceptions.ErrorCodes.NO_CHANGES_TO_COMMIT, exception.getErrorCode());
        assertEquals("No changes to commit on branch: feature/new-ui", exception.getMessage());
    }

    // Tests for NotARepositoryException
    @Test
    void testNotARepositoryException_DefaultConstructor() {
        // When
        var exception = new DomainExceptions.NotARepositoryException();
        
        // Then
        assertEquals(DomainExceptions.ErrorCodes.NOT_A_REPOSITORY, exception.getErrorCode());
        assertEquals("Not a repository, please init this first", exception.getMessage());
    }

    @Test
    void testNotARepositoryException_WithPath() {
        // Given
        String path = "/home/user/project";
        
        // When
        var exception = new DomainExceptions.NotARepositoryException(path);
        
        // Then
        assertEquals(DomainExceptions.ErrorCodes.NOT_A_REPOSITORY, exception.getErrorCode());
        assertEquals("Not a repository, please init this first at: /home/user/project", exception.getMessage());
    }

    @Test
    void testNotARepositoryException_WithPathAndCause() {
        // Given
        String path = "/home/user/project";
        Throwable cause = new RuntimeException("Original error");
        
        // When
        var exception = new DomainExceptions.NotARepositoryException(path, cause);
        
        // Then
        assertEquals(DomainExceptions.ErrorCodes.NOT_A_REPOSITORY, exception.getErrorCode());
        assertEquals("Not a repository, please init this first at: /home/user/project", exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    // Tests for NoRemoteRepositoryException
    @Test
    void testNoRemoteRepositoryException_DefaultConstructor() {
        // When
        var exception = new DomainExceptions.NoRemoteRepositoryException();
        
        // Then
        assertEquals(DomainExceptions.ErrorCodes.NO_REMOTE_REPOSITORY, exception.getErrorCode());
        assertEquals("No remote repository", exception.getMessage());
    }

    @Test
    void testNoRemoteRepositoryException_WithRemoteName() {
        // Given
        String remoteName = "origin";
        
        // When
        var exception = new DomainExceptions.NoRemoteRepositoryException(remoteName);
        
        // Then
        assertEquals(DomainExceptions.ErrorCodes.NO_REMOTE_REPOSITORY, exception.getErrorCode());
        assertEquals("No remote repository configured for: origin", exception.getMessage());
    }

    @Test
    void testNoRemoteRepositoryException_WithRemoteNameAndBranch() {
        // Given
        String remoteName = "origin";
        String branch = "main";
        
        // When
        var exception = new DomainExceptions.NoRemoteRepositoryException(remoteName, branch);
        
        // Then
        assertEquals(DomainExceptions.ErrorCodes.NO_REMOTE_REPOSITORY, exception.getErrorCode());
        assertEquals("No remote repository configured for remote 'origin' on branch 'main'", exception.getMessage());
    }

    // Tests for UnauthorizedToCommitException
    @Test
    void testUnauthorizedToCommitException_DefaultConstructor() {
        // When
        var exception = new DomainExceptions.UnauthorizedToCommitException();
        
        // Then
        assertEquals(DomainExceptions.ErrorCodes.UNAUTHORIZED_TO_COMMIT, exception.getErrorCode());
        assertEquals("UNAUTHORIZED to commit a message!", exception.getMessage());
    }

    @Test
    void testUnauthorizedToCommitException_WithUsername() {
        // Given
        String username = "guest";
        
        // When
        var exception = new DomainExceptions.UnauthorizedToCommitException(username);
        
        // Then
        assertEquals(DomainExceptions.ErrorCodes.UNAUTHORIZED_TO_COMMIT, exception.getErrorCode());
        assertEquals("User 'guest' is UNAUTHORIZED to commit a message!", exception.getMessage());
    }

    @Test
    void testUnauthorizedToCommitException_WithUsernameAndRepository() {
        // Given
        String username = "guest";
        String repository = "main-repo";
        
        // When
        var exception = new DomainExceptions.UnauthorizedToCommitException(username, repository);
        
        // Then
        assertEquals(DomainExceptions.ErrorCodes.UNAUTHORIZED_TO_COMMIT, exception.getErrorCode());
        assertEquals("User 'guest' is UNAUTHORIZED to commit to repository: main-repo", exception.getMessage());
    }

    @Test
    void testUnauthorizedToCommitException_WithCause() {
        // Given
        Throwable cause = new RuntimeException("Authentication failed");
        
        // When
        var exception = new DomainExceptions.UnauthorizedToCommitException(cause);
        
        // Then
        assertEquals(DomainExceptions.ErrorCodes.UNAUTHORIZED_TO_COMMIT, exception.getErrorCode());
        assertEquals("UNAUTHORIZED to commit a message!", exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    // Tests for Factory methods
    @Test
    void testFactoryMethods() {
        // Test commitMessageRequired factory method
        var commitException = DomainExceptions.Factory.commitMessageRequired();
        assertTrue(commitException instanceof DomainExceptions.CommitMessageRequiredException);
        assertEquals(DomainExceptions.ErrorCodes.COMMIT_MESSAGE_REQUIRED, commitException.getErrorCode());
        
        // Test noChangesToCommit factory method
        var noChangesException = DomainExceptions.Factory.noChangesToCommit();
        assertTrue(noChangesException instanceof DomainExceptions.NoChangesToCommitException);
        assertEquals(DomainExceptions.ErrorCodes.NO_CHANGES_TO_COMMIT, noChangesException.getErrorCode());
        
        // Test notARepository factory method
        String path = "/test/path";
        var notRepoException = DomainExceptions.Factory.notARepository(path);
        assertTrue(notRepoException instanceof DomainExceptions.NotARepositoryException);
        assertEquals(DomainExceptions.ErrorCodes.NOT_A_REPOSITORY, notRepoException.getErrorCode());
        
        // Test noRemoteRepository factory method
        var noRemoteException = DomainExceptions.Factory.noRemoteRepository();
        assertTrue(noRemoteException instanceof DomainExceptions.NoRemoteRepositoryException);
        assertEquals(DomainExceptions.ErrorCodes.NO_REMOTE_REPOSITORY, noRemoteException.getErrorCode());
        
        // Test unauthorizedToCommit factory method
        String username = "testuser";
        var unauthorizedException = DomainExceptions.Factory.unauthorizedToCommit(username);
        assertTrue(unauthorizedException instanceof DomainExceptions.UnauthorizedToCommitException);
        assertEquals(DomainExceptions.ErrorCodes.UNAUTHORIZED_TO_COMMIT, unauthorizedException.getErrorCode());
    }
}