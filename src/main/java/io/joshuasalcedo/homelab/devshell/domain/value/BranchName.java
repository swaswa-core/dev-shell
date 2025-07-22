package io.joshuasalcedo.homelab.devshell.domain.value;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Value object representing a Git branch name.
 * Enforces Git branch naming conventions and provides utilities for creating temporary branches.
 *
 * @author JoshuaSalcedo
 * @created 7/22/2025
 */
public class BranchName {
    private static final Pattern VALID_BRANCH_NAME = Pattern.compile("^[a-zA-Z0-9._/-]+$");
    private static final Pattern INVALID_SEQUENCES = Pattern.compile("(\\.\\.|//|^\\.|\\.$|^/|/$|@\\{)");
    private static final DateTimeFormatter TEMP_BRANCH_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");
    
    private final String value;

    private BranchName(String value) {
        this.value = Objects.requireNonNull(value, "Branch name cannot be null");
        validate(value);
    }

    /**
     * Creates a branch name from a string
     */
    public static BranchName of(String name) {
        return new BranchName(name.trim());
    }

    /**
     * Creates a temporary branch name for smart commits using timestamp
     */
    public static BranchName temporary() {
        String timestamp = LocalDateTime.now().format(TEMP_BRANCH_FORMATTER);
        return new BranchName("temp-" + timestamp);
    }

    /**
     * Creates a temporary branch name with custom prefix
     */
    public static BranchName temporary(String prefix) {
        Objects.requireNonNull(prefix, "Prefix cannot be null");
        String timestamp = LocalDateTime.now().format(TEMP_BRANCH_FORMATTER);
        return new BranchName(prefix + "-" + timestamp);
    }

    private void validate(String name) {
        if (name.isBlank()) {
            throw new IllegalArgumentException("Branch name cannot be empty or blank");
        }
        
        if (!VALID_BRANCH_NAME.matcher(name).matches()) {
            throw new IllegalArgumentException("Branch name contains invalid characters");
        }
        
        if (INVALID_SEQUENCES.matcher(name).find()) {
            throw new IllegalArgumentException("Branch name contains invalid sequences");
        }
        
        if (name.length() > 250) {
            throw new IllegalArgumentException("Branch name too long (max 250 characters)");
        }
    }

    public String getValue() {
        return value;
    }

    /**
     * Checks if this is a temporary branch (starts with temp-)
     */
    public boolean isTemporary() {
        return value.startsWith("temp-");
    }

    /**
     * Checks if this is a main/master branch
     */
    public boolean isMainBranch() {
        return "main".equals(value) || "master".equals(value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BranchName)) return false;
        BranchName that = (BranchName) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}