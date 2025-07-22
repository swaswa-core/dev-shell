package io.joshuasalcedo.homelab.devshell.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InteractiveCommandRepository extends JpaRepository<InteractiveCommand, Long> {
    Optional<InteractiveCommand> findByCommandName(String commandName);
    boolean existsByCommandName(String commandName);
}