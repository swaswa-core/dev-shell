# Dev Shell - Interactive Command Registration Plan

## User Request
The user wants to implement a system for registering interactive commands that require TTY. The current approach of automatically detecting TTY errors and retrying is not working properly.

### Current Problem
When running commands like `claude` or `nano pom.xml`, the system shows errors:
- `❌ Error: Input must be provided either through stdin or as a prompt argument when using --print`
- The automatic retry mechanism is not functioning correctly

### User's Proposed Solution
Instead of automatic detection, implement a registration system where:
1. Users can register commands as interactive using: `command-iadd "commandName"`
2. Store registered commands in a database
3. Check this database when executing unknown commands

## Implementation Plan

### 1. Add Dependencies
Add H2 and Spring Data JPA to `pom.xml`:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>runtime</scope>
</dependency>
```

### 2. Create InteractiveCommand Entity
Create entity at `/src/main/java/io/joshuasalcedo/homelab/devshell/domain/entity/InteractiveCommand.java`:
```java
@Entity
@Table(name = "interactive_commands")
public class InteractiveCommand {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String commandName;
    
    // Constructors, getters, setters
}
```

### 3. Create Repository
Create repository at `/src/main/java/io/joshuasalcedo/homelab/devshell/domain/repository/InteractiveCommandRepository.java`:
```java
@Repository
public interface InteractiveCommandRepository extends JpaRepository<InteractiveCommand, Long> {
    Optional<InteractiveCommand> findByCommandName(String commandName);
    boolean existsByCommandName(String commandName);
}
```

### 4. Configure H2 Database
Update `application.properties` to save database in `~/.dev-shell/`:
```properties
# H2 Database Configuration
spring.datasource.url=jdbc:h2:file:${user.home}/.dev-shell/interactive-commands;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

# JPA Configuration
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=update
spring.h2.console.enabled=false
```

### 5. Create Service
Create service at `/src/main/java/io/joshuasalcedo/homelab/devshell/domain/service/InteractiveCommandService.java`:
```java
@Service
public class InteractiveCommandService {
    private final InteractiveCommandRepository repository;
    
    public InteractiveCommandService(InteractiveCommandRepository repository) {
        this.repository = repository;
    }
    
    public void registerCommand(String commandName) {
        if (!repository.existsByCommandName(commandName)) {
            InteractiveCommand command = new InteractiveCommand();
            command.setCommandName(commandName);
            repository.save(command);
        }
    }
    
    public boolean isInteractiveCommand(String commandName) {
        // Extract base command from full command string
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
```

### 6. Update OSCommand
Add new commands to `OSCommand.java`:
```java
@Autowired
private InteractiveCommandService interactiveCommandService;

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
```

### 7. Revert CommandNotFoundMessageProviderImpl
Update to use the service instead of automatic detection:
```java
@Autowired
private InteractiveCommandService interactiveCommandService;

@Override
public String apply(ProviderContext providerContext) {
    String text = providerContext.text().trim();
    
    if (text.isEmpty()) {
        return formatError("Empty command");
    }
    
    // Check if this is a registered interactive command
    if (interactiveCommandService.isInteractiveCommand(text)) {
        return executeInteractiveCommand(text);
    }
    
    // Execute as regular command with streaming...
    try {
        // Original streaming execution code
    } catch (Exception e) {
        // If command not found, suggest registering as interactive
        return formatError(String.format(
            "Command '%s' not found. If this is an interactive command, register it with: command-iadd \"%s\"", 
            text.split("\\s+")[0], text.split("\\s+")[0]
        ));
    }
}
```

### 8. Initialize Default Commands
Create a `@PostConstruct` method to register common interactive commands:
```java
@PostConstruct
public void initializeDefaultCommands() {
    List<String> defaultCommands = Arrays.asList(
        "nano", "vim", "vi", "emacs", "less", "more", 
        "htop", "top", "ssh", "telnet", "mysql", "psql",
        "python", "python3", "node", "claude"
    );
    
    defaultCommands.forEach(this::registerCommand);
}
```

## Benefits of This Approach
1. **User Control**: Users can register any command as interactive
2. **Persistence**: Commands are saved in H2 database at `~/.dev-shell/`
3. **Flexibility**: Can add/remove commands at runtime
4. **Clear Feedback**: Tells users how to register unrecognized interactive commands
5. **No Guessing**: System knows exactly which commands need TTY

## Usage Example
```bash
# Register claude as interactive
command-iadd "claude"

# Now claude will work properly
claude

# List all interactive commands
command-ilist

# Remove a command
command-iremove "claude"
```