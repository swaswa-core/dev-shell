package io.joshuasalcedo.homelab.devshell.domain.value;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Author value object
 */
class AuthorTest {

    @Test
    void testValidAuthor() {
        String name = "John Doe";
        String email = "john.doe@example.com";
        Author author = Author.of(name, email);
        
        assertEquals(name, author.getName());
        assertEquals(email, author.getEmail());
        assertEquals("John Doe <john.doe@example.com>", author.toGitFormat());
    }

    @Test
    void testAuthorWithWhitespace() {
        String name = "  Jane Smith  ";
        String email = "  jane.smith@company.org  ";
        Author author = Author.of(name, email);
        
        assertEquals("Jane Smith", author.getName());
        assertEquals("jane.smith@company.org", author.getEmail());
    }

    @Test
    void testFromGitFormat_Valid() {
        String gitFormat = "John Doe <john.doe@example.com>";
        Author author = Author.fromGitFormat(gitFormat);
        
        assertEquals("John Doe", author.getName());
        assertEquals("john.doe@example.com", author.getEmail());
        assertEquals(gitFormat, author.toGitFormat());
    }

    @Test
    void testFromGitFormat_WithExtraSpaces() {
        String gitFormat = "  Jane Smith  <  jane.smith@company.org  >";
        Author author = Author.fromGitFormat(gitFormat);
        
        assertEquals("Jane Smith", author.getName());
        assertEquals("jane.smith@company.org", author.getEmail());
    }

    @Test
    void testFromGitFormat_NameWithAngleBrackets() {
        // Handle case where name might contain angle brackets
        String gitFormat = "John <Developer> Smith <john.smith@example.com>";
        Author author = Author.fromGitFormat(gitFormat);
        
        assertEquals("John <Developer> Smith", author.getName());
        assertEquals("john.smith@example.com", author.getEmail());
    }

    @Test
    void testValidEmailFormats() {
        assertDoesNotThrow(() -> Author.of("User", "user@example.com"));
        assertDoesNotThrow(() -> Author.of("User", "user.name@example.com"));
        assertDoesNotThrow(() -> Author.of("User", "user+tag@example.com"));
        assertDoesNotThrow(() -> Author.of("User", "user123@sub.example.co.uk"));
        assertDoesNotThrow(() -> Author.of("User", "test.email-with+symbol@example.com"));
    }

    @Test
    void testInvalidAuthor_NullName() {
        assertThrows(NullPointerException.class, () -> Author.of(null, "test@example.com"));
    }

    @Test
    void testInvalidAuthor_NullEmail() {
        assertThrows(NullPointerException.class, () -> Author.of("Test User", null));
    }

    @Test
    void testInvalidAuthor_EmptyName() {
        assertThrows(IllegalArgumentException.class, () -> Author.of("", "test@example.com"));
    }

    @Test
    void testInvalidAuthor_BlankName() {
        assertThrows(IllegalArgumentException.class, () -> Author.of("   ", "test@example.com"));
    }

    @Test
    void testInvalidAuthor_EmptyEmail() {
        assertThrows(IllegalArgumentException.class, () -> Author.of("Test User", ""));
    }

    @Test
    void testInvalidAuthor_BlankEmail() {
        assertThrows(IllegalArgumentException.class, () -> Author.of("Test User", "   "));
    }

    @Test
    void testInvalidAuthor_InvalidEmail() {
        assertThrows(IllegalArgumentException.class, () -> Author.of("User", "invalid-email"));
        assertThrows(IllegalArgumentException.class, () -> Author.of("User", "user@"));
        assertThrows(IllegalArgumentException.class, () -> Author.of("User", "@example.com"));
        assertThrows(IllegalArgumentException.class, () -> Author.of("User", "user@.com"));
        assertThrows(IllegalArgumentException.class, () -> Author.of("User", "user@example"));
        assertThrows(IllegalArgumentException.class, () -> Author.of("User", "user.example.com"));
    }

    @Test
    void testInvalidAuthor_NameTooLong() {
        String longName = "a".repeat(101);
        assertThrows(IllegalArgumentException.class, () -> Author.of(longName, "test@example.com"));
    }

    @Test
    void testFromGitFormat_Invalid_Null() {
        assertThrows(NullPointerException.class, () -> Author.fromGitFormat(null));
    }

    @Test
    void testFromGitFormat_Invalid_NoAngleBrackets() {
        assertThrows(IllegalArgumentException.class, () -> Author.fromGitFormat("John Doe"));
        assertThrows(IllegalArgumentException.class, () -> Author.fromGitFormat("John Doe john@example.com"));
    }

    @Test
    void testFromGitFormat_Invalid_NoClosingBracket() {
        assertThrows(IllegalArgumentException.class, () -> Author.fromGitFormat("John Doe <john@example.com"));
    }

    @Test
    void testFromGitFormat_Invalid_NoOpeningBracket() {
        assertThrows(IllegalArgumentException.class, () -> Author.fromGitFormat("John Doe john@example.com>"));
    }

    @Test
    void testAuthorEquality() {
        Author author1 = Author.of("John Doe", "john@example.com");
        Author author2 = Author.of("John Doe", "john@example.com");
        Author author3 = Author.of("Jane Smith", "jane@example.com");
        Author author4 = Author.of("John Doe", "john.doe@example.com");
        
        assertEquals(author1, author2);
        assertEquals(author1.hashCode(), author2.hashCode());
        assertNotEquals(author1, author3);
        assertNotEquals(author1, author4); // Different email
    }

    @Test
    void testToString() {
        Author author = Author.of("John Doe", "john@example.com");
        assertEquals("John Doe <john@example.com>", author.toString());
    }

    @Test
    void testToGitFormat() {
        Author author = Author.of("Development Team", "dev@company.com");
        assertEquals("Development Team <dev@company.com>", author.toGitFormat());
    }
}