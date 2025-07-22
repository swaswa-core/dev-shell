package io.joshuasalcedo.homelab.devshell.presentation.shell;


import io.joshuasalcedo.commonlibs.text.TextUtility;
import io.joshuasalcedo.homelab.devshell.infrastructure.InteractiveCommand;
import io.joshuasalcedo.homelab.devshell.infrastructure.InteractiveCommandService;
import io.joshuasalcedo.homelab.devshell.utils.AliasSetter;
import org.jline.reader.LineReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.util.List;

/**
 * OSCommand class.
 *
 * @author JoshuaSalcedo
 * @created 7/22/2025 7:22 PM
 * @since ${PROJECT.version}
 */
@ShellComponent
public class OSCommand {


    private final LineReader lineReader;
    
    @Autowired
    private InteractiveCommandService interactiveCommandService;

    public OSCommand(@Lazy LineReader lineReader) {
        this.lineReader = lineReader;
    }

    @ShellMethod(key = "set-alias", value = "Set alias for the dev-cli app.")
    public String setAlias(){
        String alias = lineReader.readLine("Alias: ");

        String aliasCommand = AliasSetter.printAliasCommand( alias );

        if (aliasCommand == null) {
            return TextUtility.of("❌ Failed to create alias command")
                    .color(TextUtility.Color.RED)
                    .bold()
                    .format();
        }

        // Determine shell profile file
        String shellProfile = determineShellProfile();
        
        StringBuilder instructions = new StringBuilder();
        instructions.append("🔧 Complete Alias Setup Instructions:\n\n");
        
        instructions.append("1️⃣ Add alias to your shell profile:\n");
        instructions.append("   echo \"").append(aliasCommand).append("\" >> ").append(shellProfile).append("\n\n");
        
        instructions.append("2️⃣ Reload your shell profile:\n");
        instructions.append("   source ").append(shellProfile).append("\n\n");
        
        instructions.append("3️⃣ Test your alias:\n");
        instructions.append("   ").append(alias).append(" --help\n\n");
        
        instructions.append("💡 Or run all commands at once:\n");
        instructions.append("echo \"").append(aliasCommand).append("\" >> ").append(shellProfile)
                   .append(" && source ").append(shellProfile)
                   .append(" && echo '✅ Alias \"").append(alias).append("\" created successfully!'\n\n");
        
        instructions.append("📍 Manual option - Add this line to ").append(shellProfile).append(":\n");
        instructions.append(aliasCommand);

        return TextUtility.of(instructions.toString())
                .color(TextUtility.Color.BRIGHT_GREEN)
                .format();
    }
    
    private String determineShellProfile() {
        String shell = System.getenv("SHELL");
        String home = System.getProperty("user.home");
        
        if (shell != null) {
            if (shell.contains("zsh")) {
                return home + "/.zshrc";
            } else if (shell.contains("fish")) {
                return home + "/.config/fish/config.fish";
            } else if (shell.contains("bash")) {
                return home + "/.bashrc";
            }
        }
        
        // Default fallback
        return home + "/.bashrc";
    }
    
    @ShellMethod(key = "command-iadd", value = "Register a command as interactive (requires TTY)")
    public String addInteractiveCommand(@ShellOption(value = "command") String commandName) {
        interactiveCommandService.registerCommand(commandName);
        return TextUtility.of("✅ Command '" + commandName + "' registered as interactive")
                .color(TextUtility.Color.GREEN)
                .format();
    }

    @ShellMethod(key = "command-ilist", value = "List all registered interactive commands")
    public String listInteractiveCommands() {
        List<InteractiveCommand> commands = interactiveCommandService.getAllCommands();
        if (commands.isEmpty()) {
            return "No interactive commands registered";
        }
        
        StringBuilder sb = new StringBuilder("📋 Registered Interactive Commands:\n");
        commands.forEach(cmd -> sb.append("  • ").append(cmd.getCommandName()).append("\n"));
        return sb.toString();
    }

    @ShellMethod(key = "command-iremove", value = "Remove a command from interactive list")
    public String removeInteractiveCommand(@ShellOption(value = "command") String commandName) {
        interactiveCommandService.removeCommand(commandName);
        return TextUtility.of("✅ Command '" + commandName + "' removed from interactive list")
                .color(TextUtility.Color.RED)
                .format();
    }
}