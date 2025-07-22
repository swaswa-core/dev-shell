package io.joshuasalcedo.homelab.devshell.infrastructure;

import jakarta.persistence.*;

@Entity
@Table(name = "interactive_commands")
public class InteractiveCommand {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String commandName;
    
    public InteractiveCommand() {
    }
    
    public InteractiveCommand(String commandName) {
        this.commandName = commandName;
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getCommandName() {
        return commandName;
    }
    
    public void setCommandName(String commandName) {
        this.commandName = commandName;
    }
}