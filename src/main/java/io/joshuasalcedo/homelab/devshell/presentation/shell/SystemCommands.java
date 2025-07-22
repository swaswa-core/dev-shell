package io.joshuasalcedo.homelab.devshell.presentation.shell;

import io.joshuasalcedo.homelab.devshell.utils.CliLogger;

import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Spring Shell commands for system operations.
 * Provides file system navigation commands like pwd, cd, ls.
 *
 * @author JoshuaSalcedo
 * @created 7/22/2025
 */
@ShellComponent
public class SystemCommands {

    /**
     * Shows current working directory
     */
    @ShellMethod(value = "Print working directory", key = "pwd")
    public String printWorkingDirectory() {
        try {
            String currentDir = System.getProperty("user.dir");
            Path currentPath = Paths.get(currentDir);
            
            return String.format("📂 Current directory: %s", currentPath.toAbsolutePath());
        } catch (Exception e) {
            CliLogger.error("Error getting current directory", e);
            return "❌ Error: Could not determine current directory";
        }
    }

    /**
     * Changes the current working directory
     */
    @ShellMethod(value = "Change current directory", key = "cd")
    public String changeDirectory(@ShellOption(value = "path", defaultValue = "~") String pathStr) {
        try {
            Path targetPath;
            
            // Handle special cases
            if (pathStr.equals("~")) {
                targetPath = Paths.get(System.getProperty("user.home"));
            } else if (pathStr.equals("..")) {
                String currentDir = System.getProperty("user.dir");
                targetPath = Paths.get(currentDir).getParent();
                if (targetPath == null) {
                    return "❌ Already at root directory";
                }
            } else {
                // Handle both absolute and relative paths
                Path pathInput = Paths.get(pathStr);
                if (pathInput.isAbsolute()) {
                    targetPath = pathInput;
                } else {
                    targetPath = Paths.get(System.getProperty("user.dir")).resolve(pathInput);
                }
            }
            
            // Normalize the path to resolve any ".." or "." segments
            targetPath = targetPath.normalize();
            
            // Check if directory exists
            if (!Files.exists(targetPath)) {
                return String.format("❌ Directory does not exist: %s", targetPath);
            }
            
            if (!Files.isDirectory(targetPath)) {
                return String.format("❌ Not a directory: %s", targetPath);
            }
            
            // Change the working directory
            System.setProperty("user.dir", targetPath.toAbsolutePath().toString());
            
            return String.format("📂 Changed to: %s", targetPath.toAbsolutePath());
        } catch (Exception e) {
            CliLogger.error("Error changing directory", e);
            return String.format("❌ Error changing directory: %s", e.getMessage());
        }
    }

    /**
     * Lists files in current or specified directory
     */
    @ShellMethod(value = "List directory contents", key = "ls")
    public String listDirectory(@ShellOption(value = "path", defaultValue = ".") String pathStr) {
        try {
            Path targetPath;
            if (pathStr.equals(".")) {
                targetPath = Paths.get(System.getProperty("user.dir"));
            } else if (pathStr.equals("~")) {
                targetPath = Paths.get(System.getProperty("user.home"));
            } else {
                Path pathInput = Paths.get(pathStr);
                if (pathInput.isAbsolute()) {
                    targetPath = pathInput;
                } else {
                    targetPath = Paths.get(System.getProperty("user.dir")).resolve(pathInput);
                }
            }
            
            if (!Files.exists(targetPath)) {
                return String.format("❌ Path does not exist: %s", targetPath);
            }
            
            if (!Files.isDirectory(targetPath)) {
                return String.format("❌ Not a directory: %s", targetPath);
            }
            
            StringBuilder result = new StringBuilder();
            result.append(String.format("📂 Contents of %s:\n", targetPath.toAbsolutePath()));
            
            try (Stream<Path> paths = Files.list(targetPath)) {
                String contents = paths
                    .sorted((p1, p2) -> {
                        // Directories first, then files
                        boolean isDir1 = Files.isDirectory(p1);
                        boolean isDir2 = Files.isDirectory(p2);
                        if (isDir1 && !isDir2) return -1;
                        if (!isDir1 && isDir2) return 1;
                        return p1.getFileName().toString().compareToIgnoreCase(p2.getFileName().toString());
                    })
                    .map(path -> {
                        String name = path.getFileName().toString();
                        if (Files.isDirectory(path)) {
                            return String.format("  📁 %s/", name);
                        } else {
                            return String.format("  📄 %s", name);
                        }
                    })
                    .collect(Collectors.joining("\n"));
                
                if (contents.isEmpty()) {
                    result.append("  (empty directory)");
                } else {
                    result.append(contents);
                }
            }
            
            return result.toString();
        } catch (IOException e) {
            CliLogger.error("Error listing directory", e);
            return String.format("❌ Error listing directory: %s", e.getMessage());
        }
    }
}