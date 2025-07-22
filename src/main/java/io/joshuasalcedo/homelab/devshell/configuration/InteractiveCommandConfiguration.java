package io.joshuasalcedo.homelab.devshell.configuration;


import io.joshuasalcedo.commonlibs.text.TextUtility;
import io.joshuasalcedo.homelab.devshell.domain.service.InteractiveCommandService;
import io.joshuasalcedo.homelab.devshell.infrastructure.command.FileInteractiveCommandRepositoryAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;

/**
 * InteractiveCommandConfiguration class.
 *
 * @author JoshuaSalcedo
 * @created 7/22/2025 9:18 PM
 * @since ${PROJECT.version}
 */
@Configuration
public class InteractiveCommandConfiguration {

    private final PrintStream printStream;

    public InteractiveCommandConfiguration(PrintStream printStream) {
        this.printStream = printStream;
    }

    @Bean
    public InteractiveCommandService interactiveCommandService() {

        InteractiveCommandService interactiveCommandService=  new InteractiveCommandService(new FileInteractiveCommandRepositoryAdapter());
        if(!interactiveCommandService.isInteractiveCommand("nano")){
            initializeDefaultCommands(interactiveCommandService);
        }else{
            printStream.println(TextUtility.of("Nano command already registered. Skipping default commands...").color(TextUtility.Color.BRIGHT_YELLOW).format());
            return interactiveCommandService;
        }

        return interactiveCommandService;

    }

    private void initializeDefaultCommands(InteractiveCommandService interactiveCommandService) {
        List<String> defaultCommands = Arrays.asList(
                "nano", "vim", "vi", "emacs", "less", "more",
                "htop", "top", "ssh", "telnet", "mysql", "psql",
                "python", "python3", "node", "claude"
        );

        defaultCommands.forEach(interactiveCommandService::registerCommand);
    }
}