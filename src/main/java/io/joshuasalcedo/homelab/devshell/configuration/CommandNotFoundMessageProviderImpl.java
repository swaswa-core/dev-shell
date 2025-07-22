package io.joshuasalcedo.homelab.devshell.configuration;

import io.joshuasalcedo.commonlibs.text.TextUtility;
import io.joshuasalcedo.homelab.devshell.infrastructure.InteractiveCommandService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.result.CommandNotFoundMessageProvider;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.stream.LogOutputStream;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Custom command not found message provider that attempts to execute unknown commands
 * as system commands in the current working directory with real-time streaming output.
 *
 * @author JoshuaSalcedo
 * @created 7/22/2025 6:04 PM
 * @since 1.0.0
 */
public class CommandNotFoundMessageProviderImpl implements CommandNotFoundMessageProvider {
    
    private static final Logger logger = LoggerFactory.getLogger(CommandNotFoundMessageProviderImpl.class);
    PrintStream out = new PrintStream(System.out, true, StandardCharsets.UTF_8);


    private final InteractiveCommandService interactiveCommandService;

    public CommandNotFoundMessageProviderImpl(InteractiveCommandService interactiveCommandService) {
        this.interactiveCommandService = interactiveCommandService;
    }

    private void initializeDefaultCommands() {
        List<String> defaultCommands = Arrays.asList(
                "nano", "vim", "vi", "emacs", "less", "more",
                "htop", "top", "ssh", "telnet", "mysql", "psql",
                "python", "python3", "node", "claude"
        );

        defaultCommands.forEach(interactiveCommandService::registerCommand);
    }

    @Override
    public String apply(ProviderContext providerContext) {
        String text = providerContext.text().trim();
        if(!interactiveCommandService.isInteractiveCommand("nano")){
            initializeDefaultCommands();
        }
        if (text.isEmpty()) {
            return formatError("Empty command");
        }
        
        // Check if this is a registered interactive command
        if (interactiveCommandService.isInteractiveCommand(text)) {
            return executeInteractiveCommand(text);
        }
        
        // Try to execute as a regular command with streaming output
        StringBuilder errorOutput = new StringBuilder();
        try {
            // Execute the command with streaming output
            int exitCode = new ProcessExecutor()
                    .command("/bin/bash", "-c", text)
                    .directory(new File(System.getProperty("user.dir")))
                    .timeout(30, TimeUnit.SECONDS) // 30 second timeout
                    .redirectOutput(new LogOutputStream() {
                        @Override
                        protected void processLine(String line) {
                            // Stream output directly to console
                           out.println(formatSuccess(line));
                        }
                    })
                    .redirectError(new LogOutputStream() {
                        @Override
                        protected void processLine(String line) {
                            // Collect error output to check for TTY errors
                            errorOutput.append(line).append("\n");
                            // Stream error output to console in red
                           out.println(formatError(line));
                        }
                    })
                    .exitValueNormal() // Accept any exit value
                    .execute()
                    .getExitValue();
            
            // Check if the error suggests it needs interactive mode
            String errorText = errorOutput.toString().toLowerCase();
            if (exitCode != 0 && (errorText.contains("not a terminal") || 
                                  errorText.contains("no tty") || 
                                  errorText.contains("stdin") ||
                                  errorText.contains("interactive") ||
                                  errorText.contains("input must be provided"))) {
                // Suggest registering as interactive command
                String baseCommand = text.split("\\s+")[0];
                logger.debug("Command failed with TTY error: {}", text);
                return formatError(String.format(
                    "Command '%s' requires TTY/interactive mode. Register it with: command-iadd \"%s\"", 
                    baseCommand, baseCommand
                ));
            }
            
            // Return empty string since output was already streamed
            if (exitCode == 0) {
                return ""; // Success - output already displayed
            } else {
                return formatError(String.format("Command exited with code %d", exitCode));
            }
            
        } catch (IOException e) {
            logger.debug("Command not found: {}", text, e);
            String baseCommand = text.split("\\s+")[0];
            // Command not found - show error message with suggestion
            return formatError(String.format(
                "Command '%s' not found. If this is an interactive command, register it with: command-iadd \"%s\"", 
                baseCommand, baseCommand
            ));
        } catch (InterruptedException _) {
            Thread.currentThread().interrupt();
            return formatError("Command interrupted");

        }catch (Exception e){
            logger.debug("Failed to execute system command: {}", text, e);
            return formatError(String.format("Failed to execute command: %s", e.getMessage()));
        }
    }
    
    private String formatError(String message) {
        return TextUtility.of("❌ " + message)
                .bold()
                .color(TextUtility.Color.RED)
                .format();
    }
    
    private String formatSuccess(String message) {
        return TextUtility.of(message)
                .color(TextUtility.Color.GREEN)
                .format();
    }
    
    private String executeInteractiveCommand(String command) {
        try {
            // Clear any previous error output and show retrying message
            out.println(formatInfo("🔄 Retrying as interactive command..."));
            
            // Use ProcessBuilder to inherit IO for interactive commands
            ProcessBuilder pb = new ProcessBuilder("/bin/bash", "-c", command);
            pb.directory(new File(System.getProperty("user.dir")));
            pb.inheritIO(); // This allows the subprocess to use the parent's stdin/stdout/stderr
            
            Process process = pb.start();
            int exitCode = process.waitFor();
            
            if (exitCode == 0) {
                return ""; // Success
            } else {
                return formatError(String.format("Interactive command exited with code %d", exitCode));
            }
        } catch (IOException e) {
            logger.debug("Failed to execute interactive command: {}", command, e);
            return formatError(String.format("Failed to execute interactive command: %s", e.getMessage()));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return formatError("Command interrupted");
        }
    }
    
    private String formatInfo(String message) {
        return TextUtility.of(message)
                .color(TextUtility.Color.CYAN)
                .format();
    }
}