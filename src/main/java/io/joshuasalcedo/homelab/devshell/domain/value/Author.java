package io.joshuasalcedo.homelab.devshell.domain.value;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Value object representing a Git author.
 * Encapsulates author information including name and email validation.
 *
 * @author JoshuaSalcedo
 * @created 7/22/2025
 */
public class Author {
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );
    
    private final String name;
    private final String email;

    private Author(String name, String email) {
        this.name = Objects.requireNonNull(name, "Author name cannot be null").trim();
        this.email = Objects.requireNonNull(email, "Author email cannot be null").trim();
        validate();
    }

    /**
     * Creates an Author with name and email
     */
    public static Author of(String name, String email) {
        return new Author(name, email);
    }

    /**
     * Creates an Author from Git config format "Name <email>"
     */
    public static Author fromGitFormat(String gitAuthor) {
        Objects.requireNonNull(gitAuthor, "Git author string cannot be null");
        
        int lastAngleIndex = gitAuthor.lastIndexOf('<');
        if (lastAngleIndex == -1 || !gitAuthor.endsWith(">")) {
            throw new IllegalArgumentException("Invalid git author format. Expected 'Name <email>'");
        }
        
        String name = gitAuthor.substring(0, lastAngleIndex).trim();
        String email = gitAuthor.substring(lastAngleIndex + 1, gitAuthor.length() - 1).trim();
        
        return new Author(name, email);
    }

    private void validate() {
        if (name.isBlank()) {
            throw new IllegalArgumentException("Author name cannot be empty or blank");
        }
        
        if (email.isBlank()) {
            throw new IllegalArgumentException("Author email cannot be empty or blank");
        }
        
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("Invalid email format");
        }
        
        if (name.length() > 100) {
            throw new IllegalArgumentException("Author name too long (max 100 characters)");
        }
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    /**
     * Returns the author in Git format "Name <email>"
     */
    public String toGitFormat() {
        return String.format("%s <%s>", name, email);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Author)) return false;
        Author author = (Author) o;
        return Objects.equals(name, author.name) && Objects.equals(email, author.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, email);
    }

    @Override
    public String toString() {
        return toGitFormat();
    }
}