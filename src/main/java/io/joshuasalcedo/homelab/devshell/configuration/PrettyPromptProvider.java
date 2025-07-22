package io.joshuasalcedo.homelab.devshell.configuration;

import org.eclipse.jgit.api.Git;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;
import org.springframework.shell.jline.PromptProvider;
import org.springframework.stereotype.Component;

import java.io.File;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

@Component
public class PrettyPromptProvider implements PromptProvider {

  // Define style constants
  private static final AttributedStyle BOLD = AttributedStyle.DEFAULT.bold();
  private static final AttributedStyle DIM = AttributedStyle.DEFAULT.faint();
  private static final AttributedStyle CYAN =
      AttributedStyle.DEFAULT.foreground(AttributedStyle.CYAN);
  private static final AttributedStyle YELLOW =
      AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW);
  private static final AttributedStyle GREEN =
      AttributedStyle.DEFAULT.foreground(AttributedStyle.GREEN);
  private static final AttributedStyle MAGENTA =
      AttributedStyle.DEFAULT.foreground(AttributedStyle.MAGENTA);
  private static final AttributedStyle RED =
      AttributedStyle.DEFAULT.foreground(AttributedStyle.RED);

  @Override
  public AttributedString getPrompt() {
    AttributedStringBuilder builder = new AttributedStringBuilder();

    // Get current directory and parse it
    String pwd = System.getProperty("user.dir");
    String home = System.getProperty("user.home");
    String displayPath = pwd;

    // Handle home directory substitution
    if (pwd.startsWith(home)) {
      displayPath = "~" + pwd.substring(home.length());
    }

    // Split path into components
    String separator = File.separator;
    String[] pathComponents = displayPath.split(separator.equals("\\") ? "\\\\" : separator);
    List<String> components = new ArrayList<>();

    // Clean up components
    for (String comp : pathComponents) {
      if (!comp.isEmpty() && !comp.equals("~")) {
        components.add(comp);
      }
    }

    // Determine OS
    String os = System.getProperty("os.name").toLowerCase();
    String osDisplay;
    if (os.contains("win")) {
      osDisplay = "win";
    } else if (os.contains("mac")) {
      osDisplay = "mac";
    } else if (os.contains("nix") || os.contains("nux")) {
      osDisplay = "linux";
    } else {
      osDisplay = "unix";
    }

    // If more than 3 directories, show full path on first line
    int totalComponents = components.size();
    int maxVisible = 3;
    int startIndex = Math.max(0, totalComponents - maxVisible);

    if (totalComponents > maxVisible) {
      builder.append("→ ", BOLD);
      builder.append(displayPath, DIM);
      builder.append("\n");
    } else {
      builder.append("→\n");
    }

    // Add OS info as first line of tree
    builder.append("└[", DIM);
    builder.append(osDisplay, CYAN);
    builder.append("]\n", DIM);

    // Build the tree for last 3 directories (or less)
    int visibleCount = Math.min(totalComponents, maxVisible);
    for (int i = 0; i < visibleCount; i++) {
      int componentIndex = startIndex + i;

      // Add indentation (one level deeper since OS is at level 0)
      for (int j = 0; j <= i; j++) {
        builder.append("    ");
      }

      // Add tree branch
      builder.append("└", DIM);

      // Add directory name
      String dirName = components.get(componentIndex);
      builder.append(dirName, YELLOW);

      builder.append("\n");
    }

    // Add final prompt line with user@host
    for (int j = 0; j <= visibleCount; j++) {
      builder.append("    ");
    }

    builder.append("└[", BOLD);

    // Username
    String username = System.getProperty("user.name");
    builder.append(username, CYAN);

    builder.append("@", BOLD);

    // Hostname
    try {
      String hostname = InetAddress.getLocalHost().getHostName();
      builder.append(hostname, GREEN);
    } catch (Exception e) {
      builder.append("localhost", GREEN);
    }

    builder.append("]", BOLD);

    // Git branch if available
    String gitBranch = getGitBranch();
    if (gitBranch != null) {
      builder.append(" ", AttributedStyle.DEFAULT);
      builder.append("git:(", MAGENTA);
      builder.append(gitBranch, RED.bold());
      builder.append(")", MAGENTA);
    }

    // Prompt symbol
    builder.append("$ ", BOLD);

    return builder.toAttributedString();
  }

  private String getGitBranch() {
    try (Git git = Git.open(new File("."))) {
      return git.getRepository().getBranch();
    } catch (Exception e) {
      return null;
    }
  }
}
