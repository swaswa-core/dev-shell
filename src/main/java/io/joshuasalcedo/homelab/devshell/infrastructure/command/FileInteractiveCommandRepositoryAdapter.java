package io.joshuasalcedo.homelab.devshell.infrastructure.command;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import io.joshuasalcedo.commonlibs.logging.api.Logger;
import io.joshuasalcedo.homelab.devshell.domain.model.InteractiveCommand;
import io.joshuasalcedo.homelab.devshell.domain.repository.InteractiveCommandRepository;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * FileInteractiveCommandRepositoryAdapter class.
 * Uses GSON to persist InteractiveCommand objects in a JSON file as a database.
 *
 * @author JoshuaSalcedo
 * @created 7/22/2025 9:19 PM
 * @since ${PROJECT.version}
 */
public class FileInteractiveCommandRepositoryAdapter implements InteractiveCommandRepository {

    private final String filePath;
    private final Gson gson;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final Type commandListType = new TypeToken<List<InteractiveCommand>>(){}.getType();

    public FileInteractiveCommandRepositoryAdapter() {
        this.filePath = System.getProperty("user.home") + "/.dev-shell/commands.json";
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();
        
        initializeFile();
    }

    private void initializeFile() {
        try {
            Path path = Paths.get(filePath);
            Path parentDir = path.getParent();
            
            // Create parent directories if they don't exist
            if (parentDir != null && !Files.exists(parentDir)) {
                Files.createDirectories(parentDir);
            }
            
            // Create the file with empty array if it doesn't exist
            if (!Files.exists(path)) {
                writeCommands(new ArrayList<>());
                Logger.info("Created new commands file at: {}", filePath);
            }
        } catch (IOException e) {
            Logger.error("Failed to initialize commands file at {}: {}", filePath, e.getMessage());
            throw new RuntimeException("Could not initialize commands file", e);
        }
    }

    @Override
    public Optional<InteractiveCommand> findByCommandName(String commandName) {
        if (commandName == null || commandName.trim().isEmpty()) {
            return Optional.empty();
        }
        
        lock.readLock().lock();
        try {
            List<InteractiveCommand> commands = readCommands();
            return commands.stream()
                    .filter(cmd -> commandName.equals(cmd.getCommandName()))
                    .findFirst();
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public boolean existsByCommandName(String commandName) {
        return findByCommandName(commandName).isPresent();
    }

    @Override
    public List<InteractiveCommand> findAll() {
        lock.readLock().lock();
        try {
            return new ArrayList<>(readCommands());
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public void save(InteractiveCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("Command cannot be null");
        }
        
        lock.writeLock().lock();
        try {
            List<InteractiveCommand> commands = readCommands();
            
            // Update existing or add new
            boolean updated = false;
            for (int i = 0; i < commands.size(); i++) {
                if (commands.get(i).getCommandName().equals(command.getCommandName())) {
                    commands.set(i, command);
                    updated = true;
                    break;
                }
            }
            
            if (!updated) {
                commands.add(command);
            }
            
            writeCommands(commands);
            Logger.info("Saved command: {}", command.getCommandName());
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void delete(@NotNull InteractiveCommand interactiveCommand) {
        lock.writeLock().lock();
        try {
            List<InteractiveCommand> commands = readCommands();
            boolean removed = commands.removeIf(cmd -> 
                cmd.getCommandName().equals(interactiveCommand.getCommandName())
            );
            
            if (removed) {
                writeCommands(commands);
                Logger.info("Deleted command: {}", interactiveCommand.getCommandName());
            } else {
                Logger.warn("Command not found for deletion: {}", interactiveCommand.getCommandName());
            }
        } finally {
            lock.writeLock().unlock();
        }
    }


    private List<InteractiveCommand> readCommands() {
        try (Reader reader = new FileReader(filePath)) {
            List<InteractiveCommand> commands = gson.fromJson(reader, commandListType);
            return commands != null ? commands : new ArrayList<>();
        } catch (FileNotFoundException e) {
            Logger.warn("Commands file not found, returning empty list");
            return new ArrayList<>();
        } catch (IOException e) {
            Logger.error("Error reading commands file: {}", e.getMessage());
            throw new RuntimeException("Failed to read commands", e);
        } catch (Exception e) {
            Logger.error("Error parsing commands JSON: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    private void writeCommands(List<InteractiveCommand> commands) {
        try (Writer writer = new FileWriter(filePath)) {
            gson.toJson(commands, commandListType, writer);
            writer.flush();
        } catch (IOException e) {
            Logger.error("Error writing commands to file: {}", e.getMessage());
            throw new RuntimeException("Failed to write commands", e);
        }
    }

    /**
     * Deletes a command by name
     * @param commandName the name of the command to delete
     * @return true if the command was deleted, false if not found
     */
    @Override
    public boolean deleteByCommandName(String commandName) {
        lock.writeLock().lock();
        try {
            List<InteractiveCommand> commands = readCommands();
            boolean removed = commands.removeIf(cmd -> 
                cmd.getCommandName().equals(commandName)
            );
            
            if (removed) {
                writeCommands(commands);
                Logger.info("Deleted command by name: {}", commandName);
            }
            
            return removed;
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Updates a command if it exists, otherwise creates it
     * @param command the command to save or update
     */
    @Override
    public void saveOrUpdate(InteractiveCommand command) {
        save(command); // The save method already handles both cases
    }

    /**
     * Deletes all commands
     */
    @Override
    public void deleteAll() {
        lock.writeLock().lock();
        try {
            writeCommands(new ArrayList<>());
            Logger.info("Deleted all commands");
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Gets the total count of commands
     * @return the number of commands stored
     */
    @Override
    public long count() {
        lock.readLock().lock();
        try {
            return readCommands().size();
        } finally {
            lock.readLock().unlock();
        }
    }
}