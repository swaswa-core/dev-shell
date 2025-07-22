package io.joshuasalcedo.homelab.devshell.domain.value;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for BranchName value object
 */
class BranchNameTest {

    @Test
    void testValidBranchName() {
        String name = "feature/user-auth";
        BranchName branchName = BranchName.of(name);
        
        assertEquals(name, branchName.getValue());
        assertFalse(branchName.isTemporary());
        assertFalse(branchName.isMainBranch());
    }

    @Test
    void testMainBranchDetection() {
        BranchName mainBranch = BranchName.of("main");
        BranchName masterBranch = BranchName.of("master");
        BranchName featureBranch = BranchName.of("feature/test");
        
        assertTrue(mainBranch.isMainBranch());
        assertTrue(masterBranch.isMainBranch());
        assertFalse(featureBranch.isMainBranch());
    }

    @Test
    void testTemporaryBranchCreation() {
        BranchName tempBranch = BranchName.temporary();
        
        assertTrue(tempBranch.getValue().startsWith("temp-"));
        assertTrue(tempBranch.isTemporary());
        assertFalse(tempBranch.isMainBranch());
        
        // Should match pattern: temp-YYYYMMDD-HHMMSS
        assertTrue(tempBranch.getValue().matches("temp-\\d{8}-\\d{6}"));
    }

    @Test
    void testTemporaryBranchWithPrefix() {
        String prefix = "feature";
        BranchName tempBranch = BranchName.temporary(prefix);
        
        assertTrue(tempBranch.getValue().startsWith(prefix + "-"));
        assertFalse(tempBranch.isTemporary()); // Only "temp-" prefix is considered temporary
        
        // Should match pattern: feature-YYYYMMDD-HHMMSS
        assertTrue(tempBranch.getValue().matches("feature-\\d{8}-\\d{6}"));
    }

    @Test
    void testBranchNameWithWhitespace() {
        String name = "  feature/auth  ";
        BranchName branchName = BranchName.of(name);
        
        assertEquals("feature/auth", branchName.getValue());
    }

    @Test
    void testValidBranchNameCharacters() {
        // Valid characters: alphanumeric, dots, underscores, hyphens, forward slashes
        assertDoesNotThrow(() -> BranchName.of("feature-123"));
        assertDoesNotThrow(() -> BranchName.of("hotfix/v1.2.3"));
        assertDoesNotThrow(() -> BranchName.of("develop_branch"));
        assertDoesNotThrow(() -> BranchName.of("release/2023.01"));
    }

    @Test
    void testInvalidBranchName_Null() {
        assertThrows(NullPointerException.class, () -> BranchName.of(null));
    }

    @Test
    void testInvalidBranchName_Empty() {
        assertThrows(IllegalArgumentException.class, () -> BranchName.of(""));
    }

    @Test
    void testInvalidBranchName_Blank() {
        assertThrows(IllegalArgumentException.class, () -> BranchName.of("   "));
    }

    @Test
    void testInvalidBranchName_InvalidCharacters() {
        assertThrows(IllegalArgumentException.class, () -> BranchName.of("feature@branch"));
        assertThrows(IllegalArgumentException.class, () -> BranchName.of("feature branch")); // space
        assertThrows(IllegalArgumentException.class, () -> BranchName.of("feature\\branch"));
    }

    @Test
    void testInvalidBranchName_InvalidSequences() {
        assertThrows(IllegalArgumentException.class, () -> BranchName.of("feature..branch")); // double dots
        assertThrows(IllegalArgumentException.class, () -> BranchName.of("feature//branch")); // double slashes
        assertThrows(IllegalArgumentException.class, () -> BranchName.of(".feature")); // starts with dot
        assertThrows(IllegalArgumentException.class, () -> BranchName.of("feature.")); // ends with dot
        assertThrows(IllegalArgumentException.class, () -> BranchName.of("/feature")); // starts with slash
        assertThrows(IllegalArgumentException.class, () -> BranchName.of("feature/")); // ends with slash
    }

    @Test
    void testInvalidBranchName_TooLong() {
        String longName = "a".repeat(251);
        assertThrows(IllegalArgumentException.class, () -> BranchName.of(longName));
    }

    @Test
    void testTemporaryBranchPrefix_Null() {
        assertThrows(NullPointerException.class, () -> BranchName.temporary(null));
    }

    @Test
    void testBranchNameEquality() {
        BranchName branch1 = BranchName.of("feature/auth");
        BranchName branch2 = BranchName.of("feature/auth");
        BranchName branch3 = BranchName.of("feature/payment");
        
        assertEquals(branch1, branch2);
        assertEquals(branch1.hashCode(), branch2.hashCode());
        assertNotEquals(branch1, branch3);
    }

    @Test
    void testToString() {
        String name = "feature/user-management";
        BranchName branchName = BranchName.of(name);
        
        assertEquals(name, branchName.toString());
    }

    @Test
    void testTemporaryBranchesAreUnique() {
        BranchName temp1 = BranchName.temporary();
        // Small delay to ensure different timestamps
        try {
            Thread.sleep(1001); // Increase delay to ensure different seconds
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        BranchName temp2 = BranchName.temporary();
        
        assertNotEquals(temp1, temp2);
        assertNotEquals(temp1.getValue(), temp2.getValue());
    }
}