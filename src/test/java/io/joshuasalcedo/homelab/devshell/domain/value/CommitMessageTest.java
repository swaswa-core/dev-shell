package io.joshuasalcedo.homelab.devshell.domain.value;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;

/**
 * Unit tests for CommitMessage value object
 */
class CommitMessageTest {

    @Test
    void testValidCommitMessage() {
        String message = "Add new feature";
        CommitMessage commitMessage = CommitMessage.of(message);
        
        assertEquals(message, commitMessage.getValue());
        assertEquals(message, commitMessage.getSummary());
        assertFalse(commitMessage.isMultiline());
    }

    @Test
    void testCommitMessageWithWhitespace() {
        String message = "  Fix bug in authentication  ";
        CommitMessage commitMessage = CommitMessage.of(message);
        
        assertEquals("Fix bug in authentication", commitMessage.getValue());
    }

    @Test
    void testMultilineCommitMessage() {
        String message = "Fix authentication bug\n\nThis fixes the issue where users couldn't login";
        CommitMessage commitMessage = CommitMessage.of(message);
        
        assertEquals(message, commitMessage.getValue());
        assertEquals("Fix authentication bug", commitMessage.getSummary());
        assertTrue(commitMessage.isMultiline());
    }

    @Test
    void testCommitMessageWithFileList() {
        String originalMessage = "Update user management";
        List<String> files = List.of("src/User.java", "src/UserController.java", "test/UserTest.java");
        
        CommitMessage commitMessage = CommitMessage.withFileList(originalMessage, files);
        
        assertTrue(commitMessage.getValue().contains(originalMessage));
        assertTrue(commitMessage.getValue().contains("Files changed:"));
        assertTrue(commitMessage.getValue().contains("- src/User.java"));
        assertTrue(commitMessage.getValue().contains("- src/UserController.java"));
        assertTrue(commitMessage.getValue().contains("- test/UserTest.java"));
        assertTrue(commitMessage.isMultiline());
    }

    @Test
    void testCommitMessageWithEmptyFileList() {
        String originalMessage = "Update documentation";
        List<String> files = List.of();
        
        CommitMessage commitMessage = CommitMessage.withFileList(originalMessage, files);
        
        assertEquals(originalMessage, commitMessage.getValue());
        assertFalse(commitMessage.isMultiline());
    }

    @Test
    void testInvalidCommitMessage_Null() {
        assertThrows(NullPointerException.class, () -> CommitMessage.of(null));
    }

    @Test
    void testInvalidCommitMessage_Empty() {
        assertThrows(IllegalArgumentException.class, () -> CommitMessage.of(""));
    }

    @Test
    void testInvalidCommitMessage_Blank() {
        assertThrows(IllegalArgumentException.class, () -> CommitMessage.of("   "));
    }

    @Test
    void testInvalidCommitMessage_TooShort() {
        assertThrows(IllegalArgumentException.class, () -> CommitMessage.of("ab"));
    }

    @Test
    void testInvalidCommitMessage_TooLong() {
        String longMessage = "a".repeat(501);
        assertThrows(IllegalArgumentException.class, () -> CommitMessage.of(longMessage));
    }

    @Test
    void testCommitMessageEquality() {
        CommitMessage msg1 = CommitMessage.of("Same message");
        CommitMessage msg2 = CommitMessage.of("Same message");
        CommitMessage msg3 = CommitMessage.of("Different message");
        
        assertEquals(msg1, msg2);
        assertEquals(msg1.hashCode(), msg2.hashCode());
        assertNotEquals(msg1, msg3);
    }

    @Test
    void testToString() {
        String message = "Test commit message";
        CommitMessage commitMessage = CommitMessage.of(message);
        
        assertEquals(message, commitMessage.toString());
    }
}