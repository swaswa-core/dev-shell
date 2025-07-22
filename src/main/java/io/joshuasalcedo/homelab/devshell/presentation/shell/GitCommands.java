package io.joshuasalcedo.homelab.devshell.presentation.shell;

import io.joshuasalcedo.homelab.devshell.domain.exception.DomainExceptions;
import io.joshuasalcedo.homelab.devshell.domain.model.Commit;
import io.joshuasalcedo.homelab.devshell.domain.model.Repository;
import io.joshuasalcedo.homelab.devshell.domain.model.WorkingDirectory;
import io.joshuasalcedo.homelab.devshell.domain.repository.GitRepository;
import io.joshuasalcedo.homelab.devshell.domain.service.GitValidationService;
import io.joshuasalcedo.homelab.devshell.domain.service.SmartCommitService;
import io.joshuasalcedo.homelab.devshell.domain.value.Author;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Scanner;

/**
 * Spring Shell commands for Git operations.
 * Provides the command-line interface for the smart commit functionality.
 *
 * @author JoshuaSalcedo
 * @created 7/22/2025
 */
@ShellComponent
public class GitCommands {
    private static final Logger logger = LoggerFactory.getLogger(GitCommands.class);
    
    private final SmartCommitService smartCommitService;
    private final GitRepository gitRepository;
    private final GitValidationService validationService;

    public GitCommands(SmartCommitService smartCommitService, 
                      GitRepository gitRepository, 
                      GitValidationService validationService) {
        this.smartCommitService = Objects.requireNonNull(smartCommitService, "SmartCommitService cannot be null");
        this.gitRepository = Objects.requireNonNull(gitRepository, "GitRepository cannot be null");
        this.validationService = Objects.requireNonNull(validationService, "GitValidationService cannot be null");
    }

    /**
     * Smart commit command that implements the workflow defined in project-info.json
     * 
     * Usage: commit "Your commit message" [--push]
     */
    @ShellMethod(value = "Smart commit with automatic staging and file listing", key = "commit")
    public String smartCommit(
            @ShellOption(value = "message") String message,
            @ShellOption(value = "--push", defaultValue = "false", help = "Push to remote after commit") boolean push) {
        
        try {
            // Find repository in current directory
            Repository repository = findCurrentRepository();
            
            // Validate repository and commit message using validation service
            validationService.validateRepository(repository);
            validationService.validateCommitMessage(message, repository);
            
            // Validate author if available
            try {
                String authorString = gitRepository.getConfiguredAuthor(repository);
                if (!authorString.equals("Unknown User <user@unknown.com>")) {
                    validationService.validateAuthor(Author.fromGitFormat(authorString), repository);
                }
            } catch (Exception e) {
                logger.warn("Could not validate author: {}", e.getMessage());
            }
            
            logger.info("Executing smart commit with message: '{}'", message);
            
            // If push is requested, validate remote repository
            if (push) {
                validationService.validateRemoteRepository(repository, "origin");
            }
            
            // Execute smart commit workflow
            Commit commit = push ? 
                smartCommitService.executeSmartCommitWithPush(repository, message, true) :
                smartCommitService.executeSmartCommit(repository, message);
            
            String result = String.format("✅ Smart commit successful!\n" +
                "📝 Commit: %s\n" +
                "🔗 Hash: %s\n" +
                "📅 Time: %s", 
                commit.getMessage().getSummary(),
                commit.getHash() != null ? commit.getHash().substring(0, 7) : "pending",
                commit.getTimestamp());
                
            if (push) {
                result += "\n🚀 Changes pushed to remote";
            }
            
            return result;
            
        } catch (DomainExceptions.CommitMessageRequiredException e) {
            return "❌ Error: Commit message is required";
        } catch (DomainExceptions.NotARepositoryException e) {
            return "❌ Error: Not a git repository. Please run 'git init' first or navigate to a git repository.";
        } catch (DomainExceptions.NoChangesToCommitException e) {
            return "❌ Error: No changes to commit. Make some changes first!";
        } catch (DomainExceptions.UnauthorizedToCommitException e) {
            return "❌ Error: Unauthorized to commit. Check your git configuration.";
        } catch (DomainExceptions.NoRemoteRepositoryException e) {
            return "❌ Error: No remote repository configured. Cannot push changes.";
        } catch (RuntimeException e) {
            String errorMsg = e.getMessage();
            if (errorMsg != null && (errorMsg.contains("Push failed: Git credentials not configured") || 
                errorMsg.contains("Authentication is required") ||
                errorMsg.contains("CredentialsProvider has been registered"))) {
                return "❌ Push Error: Git credentials not configured.\n" +
                       "💡 To push to remote, please:\n" +
                       "   1. Configure git credentials: git config --global user.name \"Your Name\"\n" +
                       "   2. Configure git email: git config --global user.email \"your@email.com\"\n" +
                       "   3. Or use SSH keys for authentication\n" +
                       "✅ Smart commit was successful locally (changes not pushed)";
            } else if (errorMsg != null && (errorMsg.contains("Failed to push") || errorMsg.contains("Push failed"))) {
                String causeMsg = e.getCause() != null ? e.getCause().getMessage() : errorMsg;
                return "⚠️  Smart commit successful locally, but push failed: " + causeMsg + 
                       "\n💡 You can push manually later with: git push origin " + 
                       gitRepository.getCurrentBranch(findCurrentRepository()).getName();
            }
            logger.error("Unexpected error during smart commit", e);
            return "❌ Unexpected error: " + e.getMessage();
        } catch (Exception e) {
            logger.error("Unexpected error during smart commit", e);
            return "❌ Unexpected error: " + e.getMessage();
        }
    }

    /**
     * Shows the current git status
     */
    @ShellMethod(value = "Show git repository status", key = "status")
    public String status() {
        try {
            Repository repository = findCurrentRepository();
            
            // Validate repository using validation service
            validationService.validateRepository(repository);
            
            logger.debug("Checking status for repository at: {}", repository.getRootPath());
            
            WorkingDirectory workingDir = gitRepository.getWorkingDirectoryStatus(repository);
            
            StringBuilder result = new StringBuilder();
            result.append(String.format("📁 Repository: %s\n", repository.getName()));
            result.append(String.format("📍 Path: %s\n", repository.getRootPath()));
            result.append(String.format("🌿 Branch: %s\n", gitRepository.getCurrentBranch(repository).getName()));
            
            if (workingDir.hasAnythingToShow()) {
                result.append("\n📝 Changes:\n");
                
                if (workingDir.hasStagedChanges()) {
                    result.append("  Staged files:\n");
                    workingDir.getStagedFiles().forEach(file -> 
                        result.append(String.format("    ✅ %s\n", file)));
                }
                
                if (workingDir.hasUnstagedChanges()) {
                    result.append("  Modified files:\n");
                    workingDir.getUnstagedFiles().forEach(file -> 
                        result.append(String.format("    📝 %s\n", file)));
                }
                
                if (!workingDir.getUntrackedFiles().isEmpty()) {
                    result.append("  Untracked files:\n");
                    workingDir.getUntrackedFiles().forEach(file -> 
                        result.append(String.format("    ❓ %s\n", file)));
                    result.append("\n⚠️  Note: Untracked files won't be included in smart commit\n");
                    result.append("💡 Use 'add --all' or 'add --files \"file1 file2\"' to stage them first\n");
                }
                
                result.append("\n💡 Use 'commit \"your message\"' to create a smart commit");
            } else {
                result.append("\n✨ Working directory is clean - no changes to commit");
            }
            
            return result.toString().trim();
            
        } catch (DomainExceptions.NotARepositoryException e) {
            return "❌ Error: Not a git repository. Please run 'git init' first or navigate to a git repository.";
        } catch (Exception e) {
            logger.error("Error getting repository status", e);
            return "❌ Error getting status: " + e.getMessage();
        }
    }

    /**
     * Initializes a new git repository
     */
    @ShellMethod(value = "Initialize a new git repository", key = "git-init")
    public String initRepository(@ShellOption(value = "name", defaultValue = "") String name) {
        try {
            var currentPath = Paths.get(System.getProperty("user.dir"));
            String repositoryName = name.isEmpty() ? currentPath.getFileName().toString() : name;
            
            Repository repository = gitRepository.initializeRepository(currentPath, repositoryName);
            
            return String.format("✅ Initialized git repository '%s' at %s", 
                repository.getName(), repository.getRootPath());
                
        } catch (Exception e) {
            logger.error("Error initializing repository", e);
            return "❌ Error initializing repository: " + e.getMessage();
        }
    }

    /**
     * Shows recent commit history
     */
    @ShellMethod(value = "Show recent commit history", key = "log")
    public String showHistory(@ShellOption(value = "--count", defaultValue = "10") int count) {
        try {
            Repository repository = findCurrentRepository();
            
            // Validate repository using validation service
            validationService.validateRepository(repository);
            
            var commits = gitRepository.getCommitHistory(repository, count);
            
            if (commits.isEmpty()) {
                return "📝 No commits found in this repository";
            }
            
            StringBuilder result = new StringBuilder();
            result.append(String.format("📚 Recent commits (%d):\n\n", commits.size()));
            
            for (Commit commit : commits) {
                result.append(String.format("🔸 %s\n", commit.getHash().substring(0, 7)));
                result.append(String.format("   📝 %s\n", commit.getMessage().getSummary()));
                result.append(String.format("   👤 %s\n", commit.getAuthor()));
                result.append(String.format("   📅 %s\n\n", commit.getTimestamp()));
            }
            
            return result.toString().trim();
            
        } catch (DomainExceptions.NotARepositoryException e) {
            return "❌ Error: Not a git repository";
        } catch (Exception e) {
            logger.error("Error getting commit history", e);
            return "❌ Error getting history: " + e.getMessage();
        }
    }

    /**
     * Validates the current git repository and configuration
     */
    @ShellMethod(value = "Validate git repository and configuration", key = "validate")
    public String validateRepository() {
        try {
            Repository repository = findCurrentRepository();
            StringBuilder result = new StringBuilder();
            result.append("🔍 Git Repository Validation\n\n");
            
            // Validate repository
            try {
                validationService.validateRepository(repository);
                result.append("✅ Repository: Valid git repository\n");
            } catch (DomainExceptions.NotARepositoryException e) {
                result.append("❌ Repository: ").append(e.getMessage()).append("\n");
                return result.toString();
            }
            
            // Validate author configuration
            try {
                String authorString = gitRepository.getConfiguredAuthor(repository);
                if (!authorString.equals("Unknown User <user@unknown.com>")) {
                    Author author = Author.fromGitFormat(authorString);
                    validationService.validateAuthor(author, repository);
                    result.append("✅ Author: ").append(author.toGitFormat()).append("\n");
                } else {
                    result.append("⚠️  Author: Not configured (using default)\n");
                }
            } catch (Exception e) {
                result.append("❌ Author: ").append(e.getMessage()).append("\n");
            }
            
            // Validate remote repository
            try {
                validationService.validateRemoteRepository(repository, "origin");
                result.append("✅ Remote: Origin remote configured\n");
            } catch (DomainExceptions.NoRemoteRepositoryException e) {
                result.append("⚠️  Remote: No remote repository configured\n");
            }
            
            // Check working directory status
            WorkingDirectory workingDir = gitRepository.getWorkingDirectoryStatus(repository);
            if (workingDir.hasAnythingToShow()) {
                int totalFiles = workingDir.getTotalChangeCount() + workingDir.getUntrackedFiles().size();
                result.append("📝 Status: ").append(totalFiles).append(" files with changes\n");
            } else {
                result.append("✨ Status: Working directory clean\n");
            }
            
            result.append("\n💡 Repository is ready for smart commits!");
            return result.toString();
            
        } catch (DomainExceptions.NotARepositoryException e) {
            return "❌ Error: Not a git repository. Please run 'git init' first or navigate to a git repository.";
        } catch (Exception e) {
            logger.error("Error validating repository", e);
            return "❌ Validation error: " + e.getMessage();
        }
    }

    /**
     * Configures git user settings
     */
    @ShellMethod(value = "Configure git user settings", key = "config")
    public String configureGit(
            @ShellOption(value = "name", help = "Your full name") String name,
            @ShellOption(value = "email", help = "Your email address") String email) {
        
        try {
            Repository repository = findCurrentRepository();
            
            // Use native git commands to set configuration
            ProcessBuilder pb1 = new ProcessBuilder("git", "config", "user.name", name);
            pb1.directory(repository.getRootPath().toFile());
            Process p1 = pb1.start();
            p1.waitFor();
            
            ProcessBuilder pb2 = new ProcessBuilder("git", "config", "user.email", email);
            pb2.directory(repository.getRootPath().toFile());
            Process p2 = pb2.start();
            p2.waitFor();
            
            // Verify the configuration was set
            String configuredAuthor = gitRepository.getConfiguredAuthor(repository);
            
            return String.format("✅ Git configuration updated successfully!\n" +
                "👤 Author: %s\n" +
                "💡 You can now use 'commit --push' to push to remote repositories", configuredAuthor);
                
        } catch (Exception e) {
            logger.error("Failed to configure git", e);
            return "❌ Failed to configure git: " + e.getMessage() + 
                   "\n💡 You can also configure manually with:\n" +
                   "   git config user.name \"" + name + "\"\n" +
                   "   git config user.email \"" + email + "\"";
        }
    }

    /**
     * Interactive authentication setup - prompts for name and email
     */
    @ShellMethod(value = "Interactive git authentication setup", key = "auth")
    public String interactiveAuth() {
        try {
            Repository repository = findCurrentRepository();
            
            // Create scanner for user input
            Scanner scanner = new Scanner(System.in);
            
            System.out.println("🔐 Git Authentication Setup");
            System.out.println("══════════════════════════════");
            System.out.println();
            
            // Get current configuration if any
            String currentAuthor = gitRepository.getConfiguredAuthor(repository);
            if (!currentAuthor.equals("Unknown User <user@unknown.com>")) {
                System.out.println("Current configuration: " + currentAuthor);
                System.out.print("Do you want to update it? (y/N): ");
                String response = scanner.nextLine().trim().toLowerCase();
                if (!response.equals("y") && !response.equals("yes")) {
                    return "✅ Authentication setup cancelled. Current configuration unchanged.";
                }
                System.out.println();
            }
            
            // Prompt for name
            System.out.print("Enter your full name: ");
            String name = scanner.nextLine().trim();
            
            if (name.isEmpty()) {
                return "❌ Name cannot be empty. Authentication setup cancelled.";
            }
            
            // Prompt for email
            System.out.print("Enter your email address: ");
            String email = scanner.nextLine().trim();
            
            if (email.isEmpty()) {
                return "❌ Email cannot be empty. Authentication setup cancelled.";
            }
            
            // Basic email validation
            if (!email.contains("@") || !email.contains(".")) {
                return "❌ Invalid email format. Please enter a valid email address.";
            }
            
            System.out.println();
            System.out.println("Configuring git with:");
            System.out.println("👤 Name: " + name);
            System.out.println("📧 Email: " + email);
            System.out.print("Proceed with configuration? (Y/n): ");
            
            String confirm = scanner.nextLine().trim().toLowerCase();
            if (confirm.equals("n") || confirm.equals("no")) {
                return "✅ Authentication setup cancelled.";
            }
            
            // Configure git using the same method as the config command
            ProcessBuilder pb1 = new ProcessBuilder("git", "config", "user.name", name);
            pb1.directory(repository.getRootPath().toFile());
            Process p1 = pb1.start();
            p1.waitFor();
            
            ProcessBuilder pb2 = new ProcessBuilder("git", "config", "user.email", email);
            pb2.directory(repository.getRootPath().toFile());
            Process p2 = pb2.start();
            p2.waitFor();
            
            // Verify the configuration was set
            String configuredAuthor = gitRepository.getConfiguredAuthor(repository);
            
            System.out.println();
            return String.format("✅ Git authentication configured successfully!\n" +
                "👤 Author: %s\n" +
                "🚀 You can now use 'commit \"message\" --push' to push to remote repositories\n" +
                "💡 For GitHub, you may also need to set up a Personal Access Token or SSH keys", 
                configuredAuthor);
                
        } catch (Exception e) {
            logger.error("Failed to configure git authentication", e);
            return "❌ Failed to configure git authentication: " + e.getMessage();
        }
    }

    /**
     * Adds untracked files to git staging area
     */
    @ShellMethod(value = "Add untracked files to staging area", key = "add")
    public String addFiles(@ShellOption(value = "--all", defaultValue = "false", help = "Add all untracked files") boolean all,
                          @ShellOption(value = "files", defaultValue = "", help = "Specific files to add (space-separated)") String files) {
        try {
            Repository repository = findCurrentRepository();
            validationService.validateRepository(repository);
            
            WorkingDirectory workingDir = gitRepository.getWorkingDirectoryStatus(repository);
            List<String> untrackedFiles = workingDir.getUntrackedFiles();
            
            if (untrackedFiles.isEmpty()) {
                return "✅ No untracked files to add";
            }
            
            List<String> filesToAdd = new ArrayList<>();
            
            if (all) {
                filesToAdd.addAll(untrackedFiles);
            } else if (!files.isEmpty()) {
                String[] requestedFiles = files.split("\\s+");
                for (String file : requestedFiles) {
                    if (untrackedFiles.contains(file)) {
                        filesToAdd.add(file);
                    } else {
                        logger.warn("File {} is not untracked, skipping", file);
                    }
                }
            } else {
                // Show untracked files and ask user to specify
                StringBuilder result = new StringBuilder();
                result.append("📁 Untracked files found:\n");
                for (String file : untrackedFiles) {
                    result.append("   ❓ ").append(file).append("\n");
                }
                result.append("\n💡 Usage:\n");
                result.append("   add --all              # Add all untracked files\n");
                result.append("   add --files \"file1.txt file2.txt\"  # Add specific files\n");
                return result.toString();
            }
            
            if (filesToAdd.isEmpty()) {
                return "⚠️  No files to add";
            }
            
            // Add files to staging
            gitRepository.stageFiles(repository, filesToAdd);
            
            StringBuilder result = new StringBuilder();
            result.append("✅ Added ").append(filesToAdd.size()).append(" file(s) to staging:\n");
            for (String file : filesToAdd) {
                result.append("   ➕ ").append(file).append("\n");
            }
            result.append("\n💡 These files will be included in your next commit");
            
            return result.toString();
            
        } catch (Exception e) {
            logger.error("Error adding files", e);
            return "❌ Error adding files: " + e.getMessage();
        }
    }

    /**
     * Shows help for git commands
     */
    @ShellMethod(value = "Show help for git commands", key = "git-help")
    public String showHelp() {
        return """
            🛠️  Git Commands Help
            
            🔧 Git Commands:
            
            📝 commit "message" [--push]
               Smart commit with automatic branch management
               - Creates temporary branch
               - Stages all tracked files
               - Commits with file list
               - Merges back to original branch
               - Optional: push to remote
            
            📊 status
               Show current repository status
            
            ➕ add [--all] [--files "file1 file2"]
               Add untracked files to staging area
               - Use --all to add all untracked files
               - Use --files to add specific files
               - Without options, shows untracked files
            
            🔍 validate
               Validate repository and configuration
               - Check repository validity
               - Verify author configuration
               - Check remote setup
               - Show working directory status
            
            ⚙️  config --name "Your Name" --email "your@email.com"
               Configure git user settings
               - Set your name and email for commits
               - Required for pushing to remote repositories
            
            🔐 auth
               Interactive authentication setup
               - Prompts for your name and email
               - Validates input and confirms before applying
               - Shows current configuration if any
            
            🏗️  git-init [name]
               Initialize new git repository
            
            📚 log [--count N]
               Show recent commit history
            
            ❓ git-help
               Show this help message
            
            💡 Examples:
               pwd                             # Show current directory
               cd src                          # Change to src directory
               ls                              # List files
               status                          # Check what files need to be added
               add --all                       # Add all untracked files
               add --files "src/Main.java"     # Add specific file
               commit "Fix authentication bug"
               commit "Add new feature" --push
               auth                            # Interactive authentication setup
               config --name "John Doe" --email "john@example.com"
               validate
               log --count 5
            """;
    }

    private Repository findCurrentRepository() {
        var currentPath = Paths.get(System.getProperty("user.dir"));
        
        // Look for repository in current directory or parent directories
        var path = currentPath;
        while (path != null) {
            Optional<Repository> repo = gitRepository.findRepository(path);
            if (repo.isPresent()) {
                return repo.get();
            }
            path = path.getParent();
        }
        
        throw new DomainExceptions.NotARepositoryException(currentPath.toString());
    }
}