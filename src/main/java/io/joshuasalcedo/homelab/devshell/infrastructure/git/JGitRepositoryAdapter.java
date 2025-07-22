package io.joshuasalcedo.homelab.devshell.infrastructure.git;

import io.joshuasalcedo.homelab.devshell.utils.CliLogger;

import io.joshuasalcedo.homelab.devshell.domain.exception.DomainExceptions;
import io.joshuasalcedo.homelab.devshell.domain.model.*;
import io.joshuasalcedo.homelab.devshell.domain.repository.GitRepository;
import io.joshuasalcedo.homelab.devshell.domain.value.BranchName;
import io.joshuasalcedo.homelab.devshell.domain.value.CommitMessage;
import io.joshuasalcedo.homelab.devshell.domain.value.Author;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.RemoteConfig;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/**
 * JGit implementation of the GitRepository interface.
 * This adapter translates domain operations into JGit API calls.
 *
 * @author JoshuaSalcedo
 * @created 7/22/2025
 */
@Component
public class JGitRepositoryAdapter implements GitRepository {

    @Override
    public Optional<io.joshuasalcedo.homelab.devshell.domain.model.Repository> findRepository(Path repositoryPath) {
        try {
            File gitDir = repositoryPath.resolve(".git").toFile();
            if (!gitDir.exists()) {
                CliLogger.debug("No .git directory found at: {}", repositoryPath);
                return Optional.empty();
            }

            Repository jgitRepo = new FileRepositoryBuilder()
                .setGitDir(gitDir)
                .readEnvironment()
                .findGitDir()
                .build();

            if (jgitRepo.getObjectDatabase().exists()) {
                String name = repositoryPath.getFileName().toString();
                boolean hasRemote = !getRemotes(repositoryPath, jgitRepo).isEmpty();
                String defaultBranch = jgitRepo.getBranch();

                return Optional.of(io.joshuasalcedo.homelab.devshell.domain.model.Repository.existing(
                    repositoryPath, name, hasRemote, defaultBranch));
            }

            jgitRepo.close();
            return Optional.empty();

        } catch (IOException e) {
            CliLogger.debug("Error checking repository at {}: {}", repositoryPath, e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public io.joshuasalcedo.homelab.devshell.domain.model.Repository initializeRepository(Path repositoryPath, String repositoryName) {
        try {
            Files.createDirectories(repositoryPath);
            Git git = Git.init().setDirectory(repositoryPath.toFile()).call();

            // Create initial commit to establish the main branch
            // Create a README.md file if it doesn't exist
            Path readmePath = repositoryPath.resolve("README.md");
            if (!Files.exists(readmePath)) {
                Files.writeString(readmePath, "# " + repositoryName + "\n\nInitialized repository");
                git.add().addFilepattern("README.md").call();

                // Configure user for initial commit if not already configured
                if (git.getRepository().getConfig().getString("user", null, "name") == null) {
                    try {
                        // Use ProcessBuilder to set git config since JGit doesn't provide a simple way to save config
                        ProcessBuilder pbName = new ProcessBuilder("git", "config", "user.name", "Dev Shell");
                        pbName.directory(repositoryPath.toFile());
                        Process pName = pbName.start();
                        pName.waitFor();

                        ProcessBuilder pbEmail = new ProcessBuilder("git", "config", "user.email", "dev-shell@example.com");
                        pbEmail.directory(repositoryPath.toFile());
                        Process pEmail = pbEmail.start();
                        pEmail.waitFor();
                    } catch (InterruptedException ie) {
                        CliLogger.warn("Interrupted while configuring git user: {}", ie.getMessage());
                        Thread.currentThread().interrupt(); // Restore the interrupted status
                    }
                }

                // Create initial commit to establish the main branch
                git.commit()
                   .setMessage("Initial commit")
                   .call();

                // Rename the default branch to main if it's not already
                String currentBranch = git.getRepository().getBranch();
                if (!currentBranch.equals("main")) {
                    git.branchRename().setOldName(currentBranch).setNewName("main").call();
                }
            }

            git.close();

            CliLogger.info("Initialized git repository at: {}", repositoryPath);
            return io.joshuasalcedo.homelab.devshell.domain.model.Repository.existing(
                repositoryPath, repositoryName, false, "main");

        } catch (GitAPIException | IOException e) {
            CliLogger.error("Failed to initialize repository at {}: {}", repositoryPath, e.getMessage());
            throw new DomainExceptions.NotARepositoryException(repositoryPath.toString(), e);
        }
    }

    @Override
    public WorkingDirectory getWorkingDirectoryStatus(io.joshuasalcedo.homelab.devshell.domain.model.Repository repository) {
        try (Git git = openGit(repository.getRootPath())) {
            Status status = git.status().call();

            List<String> stagedFiles = new ArrayList<>(status.getAdded());
            stagedFiles.addAll(status.getChanged());
            stagedFiles.addAll(status.getRemoved());

            List<String> unstagedFiles = new ArrayList<>(status.getModified());
            unstagedFiles.addAll(status.getMissing());

            List<String> untrackedFiles = new ArrayList<>(status.getUntracked());

            // Debug logging
            CliLogger.debug("Git Status - Added: {}, Changed: {}, Removed: {}, Modified: {}, Missing: {}, Untracked: {}", 
                status.getAdded().size(), status.getChanged().size(), status.getRemoved().size(),
                status.getModified().size(), status.getMissing().size(), status.getUntracked().size());

            return WorkingDirectory.withChanges(stagedFiles, unstagedFiles, untrackedFiles);

        } catch (GitAPIException | IOException e) {
            CliLogger.error("Failed to get working directory status: {}", e.getMessage());
            throw new RuntimeException("Failed to get repository status", e);
        }
    }

    @Override
    public Branch getCurrentBranch(io.joshuasalcedo.homelab.devshell.domain.model.Repository repository) {
        try (Git git = openGit(repository.getRootPath())) {
            String branchName = git.getRepository().getBranch();
            String commitHash = git.getRepository().resolve(Constants.HEAD).getName();

            return Branch.current(branchName, commitHash);

        } catch (IOException e) {
            CliLogger.error("Failed to get current branch: {}", e.getMessage());
            throw new RuntimeException("Failed to get current branch", e);
        }
    }

    @Override
    public List<Branch> getAllBranches(io.joshuasalcedo.homelab.devshell.domain.model.Repository repository) {
        try (Git git = openGit(repository.getRootPath())) {
            List<Ref> branches = git.branchList().call();
            String currentBranch = git.getRepository().getBranch();

            return branches.stream()
                .map(ref -> {
                    String name = ref.getName().replace("refs/heads/", "");
                    String commitHash = ref.getObjectId().getName();
                    boolean isCurrent = name.equals(currentBranch);
                    return Branch.regular(name, isCurrent, commitHash);
                })
                .collect(Collectors.toList());

        } catch (GitAPIException | IOException e) {
            CliLogger.error("Failed to get branches: {}", e.getMessage());
            throw new RuntimeException("Failed to get branches", e);
        }
    }

    @Override
    public Branch createBranch(io.joshuasalcedo.homelab.devshell.domain.model.Repository repository, BranchName branchName) {
        try (Git git = openGit(repository.getRootPath())) {
            Ref ref = git.branchCreate()
                .setName(branchName.getValue())
                .call();

            String commitHash = ref.getObjectId().getName();
            return Branch.temporary(branchName.getValue(), commitHash);

        } catch (GitAPIException | IOException e) {
            CliLogger.error("Failed to create branch {}: {}", branchName, e.getMessage());
            throw new RuntimeException("Failed to create branch: " + branchName, e);
        }
    }

    @Override
    public void switchToBranch(io.joshuasalcedo.homelab.devshell.domain.model.Repository repository, Branch branch) {
        try (Git git = openGit(repository.getRootPath())) {
            git.checkout()
                .setName(branch.getName())
                .call();

            CliLogger.debug("Switched to branch: {}", branch.getName());

        } catch (GitAPIException | IOException e) {
            CliLogger.error("Failed to switch to branch {}: {}", branch.getName(), e.getMessage());
            throw new RuntimeException("Failed to switch to branch: " + branch.getName(), e);
        }
    }

    @Override
    public void deleteBranch(io.joshuasalcedo.homelab.devshell.domain.model.Repository repository, Branch branch) {
        try (Git git = openGit(repository.getRootPath())) {
            git.branchDelete()
                .setBranchNames(branch.getName())
                .setForce(true)
                .call();

            CliLogger.debug("Deleted branch: {}", branch.getName());

        } catch (GitAPIException | IOException e) {
            CliLogger.error("Failed to delete branch {}: {}", branch.getName(), e.getMessage());
            throw new RuntimeException("Failed to delete branch: " + branch.getName(), e);
        }
    }

    @Override
    public void stageTrackedFiles(io.joshuasalcedo.homelab.devshell.domain.model.Repository repository) {
        try (Git git = openGit(repository.getRootPath())) {
            Status status = git.status().call();

            // Stage modified and deleted files (tracked files only)
            for (String modified : status.getModified()) {
                git.add().addFilepattern(modified).call();
            }

            for (String missing : status.getMissing()) {
                git.rm().addFilepattern(missing).call();
            }

            CliLogger.debug("Staged all tracked files");

        } catch (GitAPIException | IOException e) {
            CliLogger.error("Failed to stage tracked files: {}", e.getMessage());
            throw new RuntimeException("Failed to stage tracked files", e);
        }
    }

    @Override
    public void stageFiles(io.joshuasalcedo.homelab.devshell.domain.model.Repository repository, List<String> files) {
        try (Git git = openGit(repository.getRootPath())) {
            for (String file : files) {
                git.add().addFilepattern(file).call();
            }

            CliLogger.debug("Staged {} files", files.size());

        } catch (GitAPIException | IOException e) {
            CliLogger.error("Failed to stage files: {}", e.getMessage());
            throw new RuntimeException("Failed to stage files", e);
        }
    }

    @Override
    public Commit createCommit(io.joshuasalcedo.homelab.devshell.domain.model.Repository repository, 
                              CommitMessage message, String branchName) {
        try (Git git = openGit(repository.getRootPath())) {
            String authorString = getConfiguredAuthor(repository);
            Author author = Author.fromGitFormat(authorString);

            RevCommit jgitCommit = git.commit()
                .setMessage(message.getValue())
                .setAuthor(author.getName(), author.getEmail())
                .call();

            LocalDateTime timestamp = LocalDateTime.ofInstant(
                Instant.ofEpochSecond(jgitCommit.getCommitTime()), 
                ZoneId.systemDefault()
            );

            // Get the list of changed files from the current status
            Status status = git.status().call();
            List<String> changedFiles = new ArrayList<>(status.getAdded());
            changedFiles.addAll(status.getChanged());
            changedFiles.addAll(status.getRemoved());

            return Commit.fromHistory(
                jgitCommit.getId().getName(),
                message,
                author.toGitFormat(),
                timestamp,
                changedFiles,
                branchName
            );

        } catch (GitAPIException | IOException e) {
            CliLogger.error("Failed to create commit: {}", e.getMessage());
            throw new RuntimeException("Failed to create commit", e);
        }
    }

    @Override
    public void mergeBranch(io.joshuasalcedo.homelab.devshell.domain.model.Repository repository, 
                           Branch sourceBranch, Branch targetBranch) {
        try (Git git = openGit(repository.getRootPath())) {
            // Ensure we're on the target branch
            git.checkout().setName(targetBranch.getName()).call();

            // Merge source branch into target
            git.merge()
                .include(git.getRepository().resolve(sourceBranch.getName()))
                .setMessage("Merge branch '" + sourceBranch.getName() + "' into " + targetBranch.getName())
                .call();

            CliLogger.debug("Merged branch {} into {}", sourceBranch.getName(), targetBranch.getName());

        } catch (GitAPIException | IOException e) {
            CliLogger.error("Failed to merge branch {} into {}: {}", 
                sourceBranch.getName(), targetBranch.getName(), e.getMessage());
            throw new RuntimeException("Failed to merge branches", e);
        }
    }

    @Override
    public void pushBranch(io.joshuasalcedo.homelab.devshell.domain.model.Repository repository, Branch branch) {
        try (Git git = openGit(repository.getRootPath())) {
            // Check if the branch exists locally
            List<Ref> branches = git.branchList().call();
            boolean branchExists = branches.stream()
                .anyMatch(ref -> ref.getName().equals("refs/heads/" + branch.getName()));

            if (!branchExists) {
                CliLogger.error("Branch {} does not exist locally", branch.getName());
                throw new RuntimeException("Cannot push branch '" + branch.getName() + "' because it does not exist locally. " +
                    "Make sure you have created and committed to this branch first.");
            }

            // Try to push - if it fails due to authentication, provide a helpful error message
            git.push()
                .setRemote("origin")
                .add(branch.getName())
                .call();

            CliLogger.debug("Pushed branch {} to origin", branch.getName());

        } catch (org.eclipse.jgit.api.errors.TransportException e) {
            if (e.getMessage().contains("Authentication is required") || e.getMessage().contains("CredentialsProvider")) {
                CliLogger.error("Push failed due to authentication: {}", e.getMessage());
                throw new RuntimeException("Push failed: Git credentials not configured. " +
                    "Please configure your git credentials (git config --global user.name/user.email) " +
                    "or use SSH keys for authentication.", e);
            } else {
                CliLogger.error("Push failed due to transport error: {}", e.getMessage());
                throw new RuntimeException("Push failed: " + e.getMessage(), e);
            }
        } catch (GitAPIException | IOException e) {
            CliLogger.error("Failed to push branch {}: {}", branch.getName(), e.getMessage());
            throw new RuntimeException("Failed to push branch: " + branch.getName() + ". " + e.getMessage(), e);
        }
    }

    @Override
    public List<Commit> getCommitHistory(io.joshuasalcedo.homelab.devshell.domain.model.Repository repository, int maxCount) {
        try (Git git = openGit(repository.getRootPath())) {
            List<Commit> commits = new ArrayList<>();

            Iterable<RevCommit> jgitCommits = git.log().setMaxCount(maxCount).call();

            for (RevCommit jgitCommit : jgitCommits) {
                LocalDateTime timestamp = LocalDateTime.ofInstant(
                    Instant.ofEpochSecond(jgitCommit.getCommitTime()), 
                    ZoneId.systemDefault()
                );

                CommitMessage message = CommitMessage.of(jgitCommit.getFullMessage());
                String authorString = jgitCommit.getAuthorIdent().getName() + 
                    " <" + jgitCommit.getAuthorIdent().getEmailAddress() + ">";

                commits.add(Commit.fromHistory(
                    jgitCommit.getId().getName(),
                    message,
                    authorString,
                    timestamp,
                    List.of(), // File list would require additional API calls
                    git.getRepository().getBranch()
                ));
            }

            return commits;

        } catch (GitAPIException | IOException e) {
            CliLogger.error("Failed to get commit history: {}", e.getMessage());
            throw new RuntimeException("Failed to get commit history", e);
        }
    }

    @Override
    public String getConfiguredAuthor(io.joshuasalcedo.homelab.devshell.domain.model.Repository repository) {
        try (Git git = openGit(repository.getRootPath())) {
            org.eclipse.jgit.lib.Config config = git.getRepository().getConfig();
            String name = config.getString("user", null, "name");
            String email = config.getString("user", null, "email");

            if (name == null || email == null) {
                // Fall back to global config or default
                name = name != null ? name : "Unknown User";
                email = email != null ? email : "user@unknown.com";
            }

            return name + " <" + email + ">";

        } catch (IOException e) {
            CliLogger.warn("Failed to get configured author: {}", e.getMessage());
            return "Unknown User <user@unknown.com>";
        }
    }

    @Override
    public boolean hasUncommittedChanges(io.joshuasalcedo.homelab.devshell.domain.model.Repository repository) {
        WorkingDirectory workingDir = getWorkingDirectoryStatus(repository);
        return workingDir.hasChanges();
    }

    @Override
    public List<String> getRemotes(io.joshuasalcedo.homelab.devshell.domain.model.Repository repository) {
        try (Git git = openGit(repository.getRootPath())) {
            return getRemotes(repository.getRootPath(), git.getRepository());
        } catch (IOException e) {
            CliLogger.error("Failed to get remotes: {}", e.getMessage());
            return List.of();
        }
    }

    @Override
    public boolean hasRemote(io.joshuasalcedo.homelab.devshell.domain.model.Repository repository, String remoteName) {
        return getRemotes(repository).contains(remoteName);
    }

    private Git openGit(Path repositoryPath) throws IOException {
        File gitDir = repositoryPath.resolve(".git").toFile();
        Repository jgitRepo = new FileRepositoryBuilder()
            .setGitDir(gitDir)
            .readEnvironment()
            .findGitDir()
            .build();
        return new Git(jgitRepo);
    }

    private List<String> getRemotes(Path repositoryPath, Repository jgitRepo) {
        try {
            List<RemoteConfig> remotes = RemoteConfig.getAllRemoteConfigs(jgitRepo.getConfig());
            return remotes.stream()
                .map(RemoteConfig::getName)
                .collect(Collectors.toList());
        } catch (Exception e) {
            CliLogger.debug("Failed to get remotes for {}: {}", repositoryPath, e.getMessage());
            return List.of();
        }
    }
}
