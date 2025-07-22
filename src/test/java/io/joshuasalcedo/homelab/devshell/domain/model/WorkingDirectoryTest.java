package io.joshuasalcedo.homelab.devshell.domain.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;

/**
 * Unit tests for WorkingDirectory domain entity
 */
class WorkingDirectoryTest {

    @Test
    void testCleanWorkingDirectory() {
        WorkingDirectory workingDir = WorkingDirectory.clean();
        
        assertTrue(workingDir.getStagedFiles().isEmpty());
        assertTrue(workingDir.getUnstagedFiles().isEmpty());
        assertTrue(workingDir.getUntrackedFiles().isEmpty());
        assertFalse(workingDir.hasChanges());
        assertFalse(workingDir.hasUnstagedChanges());
        assertFalse(workingDir.hasStagedChanges());
        assertTrue(workingDir.getAllModifiedFiles().isEmpty());
        assertEquals(0, workingDir.getTotalChangeCount());
    }

    @Test
    void testWorkingDirectoryWithStagedFiles() {
        List<String> stagedFiles = List.of("src/Main.java", "test/MainTest.java");
        List<String> unstagedFiles = List.of();
        List<String> untrackedFiles = List.of("temp.txt");
        
        WorkingDirectory workingDir = WorkingDirectory.withChanges(stagedFiles, unstagedFiles, untrackedFiles);
        
        assertEquals(stagedFiles, workingDir.getStagedFiles());
        assertEquals(unstagedFiles, workingDir.getUnstagedFiles());
        assertEquals(untrackedFiles, workingDir.getUntrackedFiles());
        assertTrue(workingDir.hasChanges());
        assertFalse(workingDir.hasUnstagedChanges());
        assertTrue(workingDir.hasStagedChanges());
        assertEquals(stagedFiles, workingDir.getAllModifiedFiles());
        assertEquals(2, workingDir.getTotalChangeCount());
    }

    @Test
    void testWorkingDirectoryWithUnstagedFiles() {
        List<String> stagedFiles = List.of();
        List<String> unstagedFiles = List.of("src/Service.java", "README.md");
        List<String> untrackedFiles = List.of();
        
        WorkingDirectory workingDir = WorkingDirectory.withChanges(stagedFiles, unstagedFiles, untrackedFiles);
        
        assertEquals(stagedFiles, workingDir.getStagedFiles());
        assertEquals(unstagedFiles, workingDir.getUnstagedFiles());
        assertEquals(untrackedFiles, workingDir.getUntrackedFiles());
        assertTrue(workingDir.hasChanges());
        assertTrue(workingDir.hasUnstagedChanges());
        assertFalse(workingDir.hasStagedChanges());
        assertEquals(unstagedFiles, workingDir.getAllModifiedFiles());
        assertEquals(2, workingDir.getTotalChangeCount());
    }

    @Test
    void testWorkingDirectoryWithMixedFiles() {
        List<String> stagedFiles = List.of("src/Controller.java");
        List<String> unstagedFiles = List.of("src/Service.java", "pom.xml");
        List<String> untrackedFiles = List.of("newfile.txt", "temp/");
        
        WorkingDirectory workingDir = WorkingDirectory.withChanges(stagedFiles, unstagedFiles, untrackedFiles);
        
        assertEquals(stagedFiles, workingDir.getStagedFiles());
        assertEquals(unstagedFiles, workingDir.getUnstagedFiles());
        assertEquals(untrackedFiles, workingDir.getUntrackedFiles());
        assertTrue(workingDir.hasChanges());
        assertTrue(workingDir.hasUnstagedChanges());
        assertTrue(workingDir.hasStagedChanges());
        assertEquals(3, workingDir.getTotalChangeCount());
        
        List<String> allModified = workingDir.getAllModifiedFiles();
        assertTrue(allModified.contains("src/Controller.java"));
        assertTrue(allModified.contains("src/Service.java"));
        assertTrue(allModified.contains("pom.xml"));
        assertEquals(3, allModified.size());
    }

    @Test
    void testWorkingDirectoryWithOnlyUntrackedFiles() {
        List<String> stagedFiles = List.of();
        List<String> unstagedFiles = List.of();
        List<String> untrackedFiles = List.of("untracked1.txt", "untracked2.txt");
        
        WorkingDirectory workingDir = WorkingDirectory.withChanges(stagedFiles, unstagedFiles, untrackedFiles);
        
        assertEquals(untrackedFiles, workingDir.getUntrackedFiles());
        assertFalse(workingDir.hasChanges()); // Untracked files don't count as changes
        assertFalse(workingDir.hasUnstagedChanges());
        assertFalse(workingDir.hasStagedChanges());
        assertTrue(workingDir.getAllModifiedFiles().isEmpty());
        assertEquals(0, workingDir.getTotalChangeCount());
    }

    @Test
    void testWorkingDirectoryWithDuplicateFiles() {
        // Test case where same file appears in both staged and unstaged (edge case)
        List<String> stagedFiles = List.of("src/Main.java", "src/Utils.java");
        List<String> unstagedFiles = List.of("src/Main.java", "src/Service.java");
        List<String> untrackedFiles = List.of();
        
        WorkingDirectory workingDir = WorkingDirectory.withChanges(stagedFiles, unstagedFiles, untrackedFiles);
        
        List<String> allModified = workingDir.getAllModifiedFiles();
        assertTrue(allModified.contains("src/Main.java"));
        assertTrue(allModified.contains("src/Utils.java"));
        assertTrue(allModified.contains("src/Service.java"));
        // Should handle duplicates by removing them
        assertEquals(3, allModified.size());
    }

    @Test
    void testWorkingDirectoryConstructor_NullLists() {
        assertThrows(NullPointerException.class, () -> 
            new WorkingDirectory(null, List.of(), List.of()));
        assertThrows(NullPointerException.class, () -> 
            new WorkingDirectory(List.of(), null, List.of()));
        assertThrows(NullPointerException.class, () -> 
            new WorkingDirectory(List.of(), List.of(), null));
    }

    @Test
    void testWorkingDirectoryImmutability() {
        List<String> stagedFiles = List.of("file1.java");
        List<String> unstagedFiles = List.of("file2.java");
        List<String> untrackedFiles = List.of("file3.txt");
        
        WorkingDirectory workingDir = WorkingDirectory.withChanges(stagedFiles, unstagedFiles, untrackedFiles);
        
        // Returned lists should be immutable
        assertThrows(UnsupportedOperationException.class, () -> 
            workingDir.getStagedFiles().add("new-file.java"));
        assertThrows(UnsupportedOperationException.class, () -> 
            workingDir.getUnstagedFiles().clear());
        assertThrows(UnsupportedOperationException.class, () -> 
            workingDir.getUntrackedFiles().remove(0));
    }

    @Test
    void testToString() {
        WorkingDirectory cleanDir = WorkingDirectory.clean();
        String cleanString = cleanDir.toString();
        assertTrue(cleanString.contains("staged=0"));
        assertTrue(cleanString.contains("unstaged=0"));
        assertTrue(cleanString.contains("untracked=0"));
        assertTrue(cleanString.contains("hasChanges=false"));
        
        WorkingDirectory dirtyDir = WorkingDirectory.withChanges(
            List.of("staged.java"), 
            List.of("unstaged1.java", "unstaged2.java"), 
            List.of("untracked.txt")
        );
        String dirtyString = dirtyDir.toString();
        assertTrue(dirtyString.contains("staged=1"));
        assertTrue(dirtyString.contains("unstaged=2"));
        assertTrue(dirtyString.contains("untracked=1"));
        assertTrue(dirtyString.contains("hasChanges=true"));
    }
}