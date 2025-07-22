package io.joshuasalcedo.homelab.devshell.configuration;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.shell.result.CommandNotFoundMessageProvider;

/**
 * Configuration for Spring Shell
 *
 * @author JoshuaSalcedo
 * @created 7/22/2025
 */
@Configuration
public class ShellConfiguration {

    /**
     * Custom ApplicationRunner that suppresses the default Spring Shell startup message
     */
    @Bean
    public ApplicationRunner applicationRunner() {
        return args -> {
            // Suppress all startup messages - the shell prompt will appear automatically
        };
    }
    
    /**
     * Custom command not found message provider that executes system commands
     */
    @Bean
    public CommandNotFoundMessageProvider commandNotFoundMessageProvider() {
        return new CommandNotFoundMessageProviderImpl();
    }
}