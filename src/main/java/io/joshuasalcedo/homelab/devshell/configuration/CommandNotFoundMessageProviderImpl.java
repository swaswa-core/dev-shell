package io.joshuasalcedo.homelab.devshell.configuration;

import io.joshuasalcedo.commonlibs.text.TextUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.shell.result.CommandNotFoundMessageProvider;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.ProcessResult;

import java.io.File;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * Custom command not found message provider that attempts to execute unknown commands
 * as system commands in the current working directory.
 *
 * @author JoshuaSalcedo
 * @created 7/22/2025 6:04 PM
 * @since 1.0.0
 */
public class CommandNotFoundMessageProviderImpl implements CommandNotFoundMessageProvider {
    
    private static final Logger logger = LoggerFactory.getLogger(CommandNotFoundMessageProviderImpl.class);
    
    @Override
    public String apply(ProviderContext providerContext) {
        String text = providerContext.text().trim();
        
        if (text.isEmpty()) {
            return formatError("Empty command");
        }
        


        try {
            // Try to execute the command as a system command using shell
            ProcessResult result = new ProcessExecutor()
                    .command("/bin/bash", "-c", text)
                    .directory(new File(System.getProperty("user.dir")))
                    .timeout(30, TimeUnit.SECONDS) // 30 second timeout
                    .readOutput(true)
                    .execute();
            
            // Get output and error streams
            String output = result.outputUTF8().trim();
            String errorOutput = result.getOutput().getString();
            
            if (result.getExitValue() == 0) {
                // Success - return the output or success message
                if (!output.isEmpty()) {
                    return output;
                } else {
                    return formatSuccess("Command executed successfully");
                }
            } else {
                // Command failed - show error output
                String errorMsg = !errorOutput.isEmpty() ? errorOutput : "Command failed with exit code " + result.getExitValue();
                return formatError(String.format("Command '%s' failed: %s", text, errorMsg.trim()));
            }
            
        } catch (Exception e) {
            logger.debug("Failed to execute system command: {}", text, e);
            
            // If system command execution fails, show command not found message
            return formatError(String.format("Command '%s' not found. Available commands: help, git-help", text));
        }
    }
    
    private String formatError(String message) {
        return TextUtility.of("❌ " + message)
                .bold()
                .color(TextUtility.Color.RED)
                .format();
    }
    
    private String formatSuccess(String message) {
        return TextUtility.of("✅ " + message)
                .color(TextUtility.Color.GREEN)
                .format();
    }
}