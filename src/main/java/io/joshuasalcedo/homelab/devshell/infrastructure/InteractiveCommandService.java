package io.joshuasalcedo.homelab.devshell.infrastructure;

import io.joshuasalcedo.commonlibs.text.TextUtility;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

@Service
public class InteractiveCommandService {
    private static final Logger logger = LoggerFactory.getLogger(InteractiveCommandService.class);
    private final InteractiveCommandRepository repository;
    PrintStream out = new PrintStream(System.out, true, StandardCharsets.UTF_8);

    public InteractiveCommandService(InteractiveCommandRepository repository) {
        this.repository = repository;
    }
    
    public void registerCommand(String commandName) {
        if (!repository.existsByCommandName(commandName)) {
            InteractiveCommand command = new InteractiveCommand();
            command.setCommandName(commandName);
            repository.save(command);
            out.println(TextUtility.of(String.format("Registered interactive command: %s", commandName)).bold().color(TextUtility.Color.YELLOW).format());
        } else {
            out.println(TextUtility.of(String.format("InteractiveCommand already registered: %s", commandName)).bold().color(TextUtility.Color.BRIGHT_WHITE).format());
        }
    }
    
    public boolean isInteractiveCommand(String commandName) {
        String baseCommand = commandName.split("\\s+")[0];
        return repository.existsByCommandName(baseCommand);
    }
    
    public List<InteractiveCommand> getAllCommands() {
        return repository.findAll();
    }
    
    public void removeCommand(String commandName) {
        repository.findByCommandName(commandName)
            .ifPresent(repository::delete);
    }
    

}