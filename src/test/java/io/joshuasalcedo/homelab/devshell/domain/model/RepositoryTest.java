package io.joshuasalcedo.homelab.devshell.domain.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;
import java.nio.file.Path;

/**
 * Unit tests for Repository domain entity
 */
class RepositoryTest {

    @TempDir
    Path tempDir;

    @Test
    void testExistingRepository() {
        String name = "test-repo";
        String defaultBranch = "main";
        
        Repository repo = Repository.existing(tempDir, name, true, defaultBranch);
        
        assertEquals(tempDir, repo.getRootPath());
        assertEquals(name, repo.getName());
        assertTrue(repo.isInitialized());
        assertTrue(repo.hasRemote());
        assertEquals(defaultBranch, repo.getDefaultBranch());
    }

    @Test
    void testExistingRepositoryWithoutRemote() {
        String name = "local-repo";
        String defaultBranch = "master";
        
        Repository repo = Repository.existing(tempDir, name, false, defaultBranch);
        
        assertEquals(tempDir, repo.getRootPath());
        assertEquals(name, repo.getName());
        assertTrue(repo.isInitialized());
        assertFalse(repo.hasRemote());
        assertEquals(defaultBranch, repo.getDefaultBranch());
    }

    @Test
    void testUninitializedRepository() {
        String name = "new-repo";
        
        Repository repo = Repository.uninitialized(tempDir, name);
        
        assertEquals(tempDir, repo.getRootPath());
        assertEquals(name, repo.getName());
        assertFalse(repo.isInitialized());
        assertFalse(repo.hasRemote());
        assertNull(repo.getDefaultBranch());
    }

    @Test
    void testRepositoryConstructor_NullPath() {
        assertThrows(NullPointerException.class, () -> 
            new Repository(null, "test", true, false, "main"));
    }

    @Test
    void testRepositoryConstructor_NullName() {
        assertThrows(NullPointerException.class, () -> 
            new Repository(tempDir, null, true, false, "main"));
    }

    @Test
    void testRepositoryEquality() {
        Repository repo1 = Repository.existing(tempDir, "repo1", true, "main");
        Repository repo2 = Repository.existing(tempDir, "repo2", false, "master");
        Repository repo3 = Repository.existing(tempDir.resolve("other"), "repo1", true, "main");
        
        // Equality is based on path only
        assertEquals(repo1, repo2);
        assertEquals(repo1.hashCode(), repo2.hashCode());
        assertNotEquals(repo1, repo3);
    }

    @Test
    void testToString() {
        String name = "my-project";
        Repository repo = Repository.existing(tempDir, name, true, "main");
        
        String toString = repo.toString();
        assertTrue(toString.contains(name));
        assertTrue(toString.contains(tempDir.toString()));
        assertTrue(toString.contains("initialized=true"));
        assertTrue(toString.contains("hasRemote=true"));
    }

    @Test
    void testUninitializedToString() {
        String name = "uninitialized-project";
        Repository repo = Repository.uninitialized(tempDir, name);
        
        String toString = repo.toString();
        assertTrue(toString.contains(name));
        assertTrue(toString.contains("initialized=false"));
        assertTrue(toString.contains("hasRemote=false"));
    }
}