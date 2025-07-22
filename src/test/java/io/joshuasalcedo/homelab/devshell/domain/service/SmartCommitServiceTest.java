package io.joshuasalcedo.homelab.devshell.domain.service;

import io.joshuasalcedo.homelab.devshell.domain.exception.DomainExceptions;
import io.joshuasalcedo.homelab.devshell.domain.model.*;
import io.joshuasalcedo.homelab.devshell.domain.repository.GitRepository;
import io.joshuasalcedo.homelab.devshell.domain.value.BranchName;
import io.joshuasalcedo.homelab.devshell.domain.value.CommitMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for SmartCommitService
 */
@ExtendWith(MockitoExtension.class)
class SmartCommitServiceTest {

    @Mock
    private GitRepository gitRepository;

    @Mock
    private GitValidationService validationService;

    private SmartCommitService smartCommitService;
    
    @TempDir
    Path tempDir;

    private Repository testRepository;
    private Branch mainBranch;
    private Branch tempBranch;

    @BeforeEach
    void setUp() {
        smartCommitService = new SmartCommitService(gitRepository, validationService);
        
        testRepository = Repository.existing(tempDir, "test-repo", true, "main");
        mainBranch = Branch.current("main", "abc123");
        tempBranch = Branch.temporary("temp-20250122-120000", "def456");
    }

    @Test
    void testSuccessfulSmartCommit() {
        // Arrange
        String commitMessage = "Add user authentication";
        List<String> changedFiles = List.of("src/User.java", "src/AuthService.java");
        WorkingDirectory workingDir = WorkingDirectory.withChanges(
            List.of(), changedFiles, List.of()
        );
        
        Commit expectedCommit = Commit.forSmartCommit(
            CommitMessage.withFileList(commitMessage, changedFiles),
            "Test Author",
            changedFiles,
            tempBranch.getName()
        );

        // Mock behavior - getWorkingDirectoryStatus is called twice
        doNothing().when(validationService).validateRepository(testRepository);
        when(gitRepository.getWorkingDirectoryStatus(testRepository)).thenReturn(workingDir).thenReturn(workingDir);
        when(gitRepository.getCurrentBranch(testRepository)).thenReturn(mainBranch);
        when(gitRepository.createBranch(eq(testRepository), any(BranchName.class))).thenReturn(tempBranch);
        when(gitRepository.createCommit(eq(testRepository), any(CommitMessage.class), eq(tempBranch.getName())))
            .thenReturn(expectedCommit);

        // Act
        Commit result = smartCommitService.executeSmartCommit(testRepository, commitMessage);

        // Assert
        assertNotNull(result);
        assertEquals(expectedCommit, result);
        
        // Verify workflow steps - getWorkingDirectoryStatus called twice
        verify(validationService).validateRepository(testRepository);
        verify(gitRepository, times(2)).getWorkingDirectoryStatus(testRepository);
        verify(gitRepository).getCurrentBranch(testRepository);
        verify(gitRepository).createBranch(eq(testRepository), any(BranchName.class));
        verify(gitRepository).switchToBranch(testRepository, tempBranch);
        verify(gitRepository).stageTrackedFiles(testRepository);
        verify(gitRepository).createCommit(eq(testRepository), any(CommitMessage.class), eq(tempBranch.getName()));
        verify(gitRepository).switchToBranch(testRepository, mainBranch);
        verify(gitRepository).mergeBranch(testRepository, tempBranch, mainBranch);
        verify(gitRepository).deleteBranch(testRepository, tempBranch);
    }

    @Test
    void testSmartCommitWithPush() {
        // Arrange
        String commitMessage = "Fix authentication bug";
        List<String> changedFiles = List.of("src/AuthService.java");
        WorkingDirectory workingDir = WorkingDirectory.withChanges(
            List.of(), changedFiles, List.of()
        );
        
        Commit expectedCommit = Commit.forSmartCommit(
            CommitMessage.withFileList(commitMessage, changedFiles),
            "Test Author",
            changedFiles,
            tempBranch.getName()
        );

        // Mock behavior
        doNothing().when(validationService).validateRepository(testRepository);
        when(gitRepository.getWorkingDirectoryStatus(testRepository)).thenReturn(workingDir);
        when(gitRepository.getCurrentBranch(testRepository)).thenReturn(mainBranch).thenReturn(mainBranch);
        when(gitRepository.createBranch(eq(testRepository), any(BranchName.class))).thenReturn(tempBranch);
        when(gitRepository.createCommit(eq(testRepository), any(CommitMessage.class), eq(tempBranch.getName())))
            .thenReturn(expectedCommit);

        // Act
        Commit result = smartCommitService.executeSmartCommitWithPush(testRepository, commitMessage, true);

        // Assert
        assertNotNull(result);
        assertEquals(expectedCommit, result);
        
        // Verify push was called
        verify(gitRepository).pushBranch(testRepository, mainBranch);
    }

    @Test
    void testSmartCommitWithoutPush() {
        // Arrange
        String commitMessage = "Update documentation";
        List<String> changedFiles = List.of("README.md");
        WorkingDirectory workingDir = WorkingDirectory.withChanges(
            List.of(), changedFiles, List.of()
        );
        
        Commit expectedCommit = Commit.forSmartCommit(
            CommitMessage.withFileList(commitMessage, changedFiles),
            "Test Author",
            changedFiles,
            tempBranch.getName()
        );

        // Mock behavior
        doNothing().when(validationService).validateRepository(testRepository);
        when(gitRepository.getWorkingDirectoryStatus(testRepository)).thenReturn(workingDir);
        when(gitRepository.getCurrentBranch(testRepository)).thenReturn(mainBranch);
        when(gitRepository.createBranch(eq(testRepository), any(BranchName.class))).thenReturn(tempBranch);
        when(gitRepository.createCommit(eq(testRepository), any(CommitMessage.class), eq(tempBranch.getName())))
            .thenReturn(expectedCommit);

        // Act
        Commit result = smartCommitService.executeSmartCommitWithPush(testRepository, commitMessage, false);

        // Assert
        assertNotNull(result);
        assertEquals(expectedCommit, result);
        
        // Verify push was NOT called
        verify(gitRepository, never()).pushBranch(any(), any());
    }

    @Test
    void testSmartCommit_NullMessage() {
        // Act & Assert
        assertThrows(DomainExceptions.CommitMessageRequiredException.class, () ->
            smartCommitService.executeSmartCommit(testRepository, null));
        
        verify(validationService, never()).validateRepository(any());
        verify(gitRepository, never()).getWorkingDirectoryStatus(any());
    }

    @Test
    void testSmartCommit_EmptyMessage() {
        // Act & Assert
        assertThrows(DomainExceptions.CommitMessageRequiredException.class, () ->
            smartCommitService.executeSmartCommit(testRepository, ""));
        
        verify(validationService, never()).validateRepository(any());
        verify(gitRepository, never()).getWorkingDirectoryStatus(any());
    }

    @Test
    void testSmartCommit_BlankMessage() {
        // Act & Assert
        assertThrows(DomainExceptions.CommitMessageRequiredException.class, () ->
            smartCommitService.executeSmartCommit(testRepository, "   "));
        
        verify(validationService, never()).validateRepository(any());
        verify(gitRepository, never()).getWorkingDirectoryStatus(any());
    }

    @Test
    void testSmartCommit_InvalidRepository() {
        // Arrange
        String commitMessage = "Test commit";
        doThrow(new DomainExceptions.NotARepositoryException(tempDir.toString()))
            .when(validationService).validateRepository(testRepository);

        // Act & Assert
        assertThrows(DomainExceptions.NotARepositoryException.class, () ->
            smartCommitService.executeSmartCommit(testRepository, commitMessage));
        
        verify(validationService).validateRepository(testRepository);
        verify(gitRepository, never()).getWorkingDirectoryStatus(any());
    }

    @Test
    void testSmartCommit_NoChangesToCommit() {
        // Arrange
        String commitMessage = "Test commit";
        WorkingDirectory cleanWorkingDir = WorkingDirectory.clean();
        
        doNothing().when(validationService).validateRepository(testRepository);
        when(gitRepository.getWorkingDirectoryStatus(testRepository)).thenReturn(cleanWorkingDir);

        // Act & Assert
        assertThrows(DomainExceptions.NoChangesToCommitException.class, () ->
            smartCommitService.executeSmartCommit(testRepository, commitMessage));
        
        verify(validationService).validateRepository(testRepository);
        verify(gitRepository).getWorkingDirectoryStatus(testRepository);
        verify(gitRepository, never()).getCurrentBranch(any());
    }

    @Test
    void testSmartCommit_FailureWithCleanup() {
        // Arrange
        String commitMessage = "Test commit";
        List<String> changedFiles = List.of("src/Test.java");
        WorkingDirectory workingDir = WorkingDirectory.withChanges(
            List.of(), changedFiles, List.of()
        );
        
        doNothing().when(validationService).validateRepository(testRepository);
        when(gitRepository.getWorkingDirectoryStatus(testRepository)).thenReturn(workingDir);
        when(gitRepository.getCurrentBranch(testRepository)).thenReturn(mainBranch);
        when(gitRepository.createBranch(eq(testRepository), any(BranchName.class))).thenReturn(tempBranch);
        doThrow(new RuntimeException("Commit failed")).when(gitRepository)
            .createCommit(eq(testRepository), any(CommitMessage.class), eq(tempBranch.getName()));

        // Act & Assert
        assertThrows(RuntimeException.class, () ->
            smartCommitService.executeSmartCommit(testRepository, commitMessage));
        
        // Verify cleanup was attempted
        verify(gitRepository).switchToBranch(testRepository, mainBranch);
        verify(gitRepository).deleteBranch(testRepository, tempBranch);
    }

    @Test
    void testSmartCommit_CleanupFailure() {
        // Arrange
        String commitMessage = "Test commit";
        List<String> changedFiles = List.of("src/Test.java");
        WorkingDirectory workingDir = WorkingDirectory.withChanges(
            List.of(), changedFiles, List.of()
        );
        
        doNothing().when(validationService).validateRepository(testRepository);
        when(gitRepository.getWorkingDirectoryStatus(testRepository)).thenReturn(workingDir).thenReturn(workingDir);
        when(gitRepository.getCurrentBranch(testRepository)).thenReturn(mainBranch);
        when(gitRepository.createBranch(eq(testRepository), any(BranchName.class))).thenReturn(tempBranch);
        doThrow(new RuntimeException("Commit failed")).when(gitRepository)
            .createCommit(eq(testRepository), any(CommitMessage.class), eq(tempBranch.getName()));
        
        // Mock cleanup to fail on the switchToBranch call in cleanup, but allow deleteBranch
        doThrow(new RuntimeException("Cleanup switchToBranch failed")).when(gitRepository)
            .switchToBranch(testRepository, mainBranch);
        doNothing().when(gitRepository).switchToBranch(testRepository, tempBranch);
        lenient().doThrow(new RuntimeException("Cleanup deleteBranch failed")).when(gitRepository)
            .deleteBranch(testRepository, tempBranch);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
            smartCommitService.executeSmartCommit(testRepository, commitMessage));
        
        assertEquals("Commit failed", exception.getMessage());
        
        // Verify cleanup was attempted despite failure - but deleteBranch might not be called if switchToBranch fails
        verify(gitRepository, atLeastOnce()).switchToBranch(eq(testRepository), any(Branch.class));
        // Don't verify deleteBranch since cleanup can fail before reaching it
    }

    @Test
    void testConstructor_NullGitRepository() {
        assertThrows(NullPointerException.class, () ->
            new SmartCommitService(null, validationService));
    }

    @Test
    void testConstructor_NullValidationService() {
        assertThrows(NullPointerException.class, () ->
            new SmartCommitService(gitRepository, null));
    }
}