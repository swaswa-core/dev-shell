package io.joshuasalcedo.homelab.devshell.domain.exception;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the BaseException class.
 */
class BaseExceptionTest {

    // Concrete implementation of BaseException for testing
    private static class TestException extends BaseException {
        public TestException(String errorCode, String message, Object... errorDetails) {
            super(errorCode, message, errorDetails);
        }

        public TestException(String errorCode, String message, Throwable cause, Object... errorDetails) {
            super(errorCode, message, cause, errorDetails);
        }
    }

    @Test
    void testConstructorWithoutCause() {
        // Given
        String errorCode = "TEST001";
        String message = "Test error message";
        Object[] details = new Object[]{"detail1", "detail2"};

        // When
        TestException exception = new TestException(errorCode, message, details);

        // Then
        assertEquals(errorCode, exception.getErrorCode());
        assertEquals(message, exception.getMessage());
        assertArrayEquals(details, exception.getErrorDetails());
    }

    @Test
    void testConstructorWithCause() {
        // Given
        String errorCode = "TEST002";
        String message = "Test error with cause";
        Throwable cause = new RuntimeException("Original cause");
        Object[] details = new Object[]{"detail3"};

        // When
        TestException exception = new TestException(errorCode, message, cause, details);

        // Then
        assertEquals(errorCode, exception.getErrorCode());
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
        assertArrayEquals(details, exception.getErrorDetails());
    }

    @Test
    void testGetFormattedError() {
        // Given
        String errorCode = "TEST003";
        String message = "Formatted error test";
        TestException exception = new TestException(errorCode, message);

        // When
        String formattedError = exception.getFormattedError();

        // Then
        assertEquals("Error TEST003: Formatted error test", formattedError);
    }

    @Test
    void testToString() {
        // Given
        String errorCode = "TEST004";
        String message = "ToString test";
        TestException exception = new TestException(errorCode, message);

        // When
        String result = exception.toString();

        // Then
        assertEquals("TestException[errorCode=TEST004, message=ToString test]", result);
    }

    @Test
    void testGetErrorDetailsWithNullDetails() {
        // Given
        TestException exception = new TestException("TEST005", "No details");

        // When
        Object[] details = exception.getErrorDetails();

        // Then
        assertNotNull(details);
        assertEquals(0, details.length);
    }
}