package io.joshuasalcedo.homelab.devshell.configuration;

import io.joshuasalcedo.homelab.devshell.domain.repository.GitRepository;
import io.joshuasalcedo.homelab.devshell.domain.service.GitValidationService;
import io.joshuasalcedo.homelab.devshell.domain.service.SmartCommitService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring configuration for Git-related services.
 * Wires together the domain services and repository implementations.
 *
 * @author JoshuaSalcedo
 * @created 7/22/2025
 */
@Configuration
public class GitConfiguration {

    @Bean
    public GitValidationService gitValidationService() {
        return new GitValidationService();
    }

    @Bean
    public SmartCommitService smartCommitService(GitRepository gitRepository, 
                                               GitValidationService validationService) {
        return new SmartCommitService(gitRepository, validationService);
    }
}