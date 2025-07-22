package io.joshuasalcedo.homelab.devshell.domain.repository;


import io.joshuasalcedo.homelab.devshell.domain.model.InteractiveCommand;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

/**
 * InteractiveCommandRepository class.
 *
 * @author JoshuaSalcedo
 * @created 7/22/2025 9:13 PM
 * @since ${PROJECT.version}
 */
public interface InteractiveCommandRepository {
    Optional<InteractiveCommand> findByCommandName(String commandName);
    boolean existsByCommandName(String commandName);

    List<InteractiveCommand> findAll();

    void save(InteractiveCommand command);

    void delete(@NotNull InteractiveCommand interactiveCommand);

    boolean deleteByCommandName(String commandName);

    void saveOrUpdate(InteractiveCommand command);

    void deleteAll();

    long count();
}