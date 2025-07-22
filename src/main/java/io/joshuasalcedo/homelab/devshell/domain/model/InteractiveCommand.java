package io.joshuasalcedo.homelab.devshell.domain.model;



/**
 * InteractiveCommand class.
 *
 * @author JoshuaSalcedo
 * @created 7/22/2025 9:12 PM
 * @since ${PROJECT.version}
 */
public class InteractiveCommand {

    private String commandName;

    public InteractiveCommand() {
    }

    public InteractiveCommand(String commandName) {
        this.commandName = commandName;
    }

    public String getCommandName() {
        return commandName;
    }

    public void setCommandName(String commandName) {
        this.commandName = commandName;
    }
}