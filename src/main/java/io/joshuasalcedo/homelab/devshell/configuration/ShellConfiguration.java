package io.joshuasalcedo.homelab.devshell.configuration;

import io.joshuasalcedo.commonlibs.text.BannerGenerator;
import io.joshuasalcedo.commonlibs.text.TextUtility;
import io.joshuasalcedo.homelab.devshell.infrastructure.InteractiveCommandService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.shell.result.CommandNotFoundMessageProvider;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

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
            PrintStream printStream = new PrintStream(System.out, true, StandardCharsets.UTF_8);
           printStream.println(BannerGenerator.create("SHUTDOWN")
                    .titleColor(TextUtility.Color.BRIGHT_YELLOW)
                    .showTimestamp()
                           .addMetadata("Created by", "Joshua Salcedo")
                    .generateWithAsciiArt());
        };
    }
    
    /**
     * Custom command not found message provider that executes system commands
     */
    @Bean
    public CommandNotFoundMessageProvider commandNotFoundMessageProvider(InteractiveCommandService interactiveCommandService) {
        return new CommandNotFoundMessageProviderImpl(interactiveCommandService);
    }


}