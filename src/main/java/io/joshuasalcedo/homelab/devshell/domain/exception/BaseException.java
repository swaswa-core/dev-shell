package io.joshuasalcedo.homelab.devshell.domain.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base exception class for domain-related errors.
 * Provides consistent error handling with color-coded logging support.
 *
 * @author JoshuaSalcedo
 * @created 7/22/2025 12:56 AM
 * @since ${PROJECT.version}
 */
public abstract class BaseException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  // ANSI color codes
  private static final String ANSI_RED = "\033[31m";
  private static final String ANSI_RESET = "\033[0m";
  private static final String ANSI_YELLOW = "\033[33m";

  // Check if ANSI colors are supported
  private static final boolean ANSI_SUPPORTED = !System.getProperty("os.name").toLowerCase().contains("win")
          || System.getenv("TERM") != null;

  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  private final String errorCode;
  private final transient Object[] errorDetails;

  /**
   * Creates a new BaseException with a message and error code.
   *
   * @param errorCode unique error code for this exception type
   * @param message human-readable error message
   * @param errorDetails additional context information for logging
   */
  public BaseException(String errorCode, String message, Object... errorDetails) {
    super(message);
    this.errorCode = errorCode;
    this.errorDetails = errorDetails;
    logError(message, null);
  }

  /**
   * Creates a new BaseException with a message, cause, and error code.
   *
   * @param errorCode unique error code for this exception type
   * @param message human-readable error message
   * @param cause the underlying exception that caused this error
   * @param errorDetails additional context information for logging
   */
  public BaseException(String errorCode, String message, Throwable cause, Object... errorDetails) {
    super(message, cause);
    this.errorCode = errorCode;
    this.errorDetails = errorDetails;
    logError(message, cause);
  }

  /**
   * Logs the error with appropriate formatting and color coding.
   */
  private void logError(String message, Throwable cause) {
    String formattedMessage = formatErrorMessage(message);

    if (cause != null) {
      logger.error(formattedMessage, cause);
    } else {
      logger.error(formattedMessage);
    }

    // Log additional details if provided
    if (errorDetails != null && errorDetails.length > 0) {
      logger.error("Error details: {}", errorDetails);
    }
  }

  /**
   * Formats the error message with color coding if supported.
   */
  private String formatErrorMessage(String message) {
    if (ANSI_SUPPORTED) {
      return String.format("[%s%s] %s",
              colorize(ANSI_RED, errorCode),
              ANSI_RESET,
              message);
    } else {
      return String.format("[%s] %s", errorCode, message);
    }
  }

  /**
   * Applies color to text if ANSI is supported.
   */
  private String colorize(String color, String text) {
    return ANSI_SUPPORTED ? color + text + ANSI_RESET : text;
  }

  /**
   * Gets the error code associated with this exception.
   *
   * @return the error code
   */
  public String getErrorCode() {
    return errorCode;
  }

  /**
   * Gets additional error details if available.
   *
   * @return array of error details or empty array
   */
  public Object[] getErrorDetails() {
    return errorDetails != null ? errorDetails.clone() : new Object[0];
  }

  /**
   * Creates a structured error response suitable for APIs.
   *
   * @return a formatted error message including the error code
   */
  public String getFormattedError() {
    return String.format("Error %s: %s", errorCode, getMessage());
  }

  @Override
  public String toString() {
    return String.format("%s[errorCode=%s, message=%s]",
            this.getClass().getSimpleName(),
            errorCode,
            getMessage());
  }
}