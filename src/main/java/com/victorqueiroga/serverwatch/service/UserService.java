package com.victorqueiroga.serverwatch.service;

import com.victorqueiroga.serverwatch.model.User;
import com.victorqueiroga.serverwatch.repository.UserRepository;
import com.victorqueiroga.serverwatch.security.KeycloakUser;
import com.victorqueiroga.serverwatch.security.KeycloakUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Serviço que gerencia usuários locais integrados com Keycloak
 * Este serviço sincroniza dados entre Keycloak e banco local
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final KeycloakUserService keycloakUserService;

    /**
     * Obtém ou cria um usuário local baseado no usuário do Keycloak
     */
    public User getOrCreateUser() {
        KeycloakUser keycloakUser = keycloakUserService.getCurrentUser();
        
        if (keycloakUser == null) {
            throw new IllegalStateException("Usuário não está autenticado no Keycloak");
        }

        return getOrCreateUser(keycloakUser);
    }

    /**
     * Obtém ou cria um usuário local baseado nos dados do Keycloak
     */
    public User getOrCreateUser(KeycloakUser keycloakUser) {
        Optional<User> existingUser = userRepository.findByKeycloakId(keycloakUser.getId());
        
        if (existingUser.isPresent()) {
            // Atualiza dados do usuário existente
            User user = existingUser.get();
            syncUserWithKeycloak(user, keycloakUser);
            user.registerLogin();
            return userRepository.save(user);
        } else {
            // Cria novo usuário
            return createUserFromKeycloak(keycloakUser);
        }
    }

    /**
     * Cria um novo usuário local baseado nos dados do Keycloak
     */
    private User createUserFromKeycloak(KeycloakUser keycloakUser) {
        log.info("Criando novo usuário local para: {}", keycloakUser.getUsername());
        
        User user = User.builder()
            .keycloakId(keycloakUser.getId())
            .username(keycloakUser.getUsername())
            .email(keycloakUser.getEmail())
            .fullName(keycloakUser.getFullName())
            .firstLogin(LocalDateTime.now())
            .lastLogin(LocalDateTime.now())
            .active(true)
            .theme(User.Theme.AUTO)
            .language("pt-BR")
            .timezone("America/Sao_Paulo")
            .build();

        // Mapear roles do Keycloak para roles da aplicação
        Set<User.ApplicationRole> appRoles = mapKeycloakRolesToApplicationRoles(keycloakUser);
        user.setApplicationRoles(appRoles);

        return userRepository.save(user);
    }

    /**
     * Sincroniza dados do usuário local com o Keycloak
     */
    private void syncUserWithKeycloak(User user, KeycloakUser keycloakUser) {
        boolean changed = false;

        if (!keycloakUser.getUsername().equals(user.getUsername()) ||
            !keycloakUser.getEmail().equals(user.getEmail()) ||
            !keycloakUser.getFullName().equals(user.getFullName())) {
            
            user.syncWithKeycloak(
                keycloakUser.getUsername(),
                keycloakUser.getEmail(),
                keycloakUser.getFullName()
            );
            changed = true;
        }

        // Atualizar roles se necessário
        Set<User.ApplicationRole> newRoles = mapKeycloakRolesToApplicationRoles(keycloakUser);
        if (!newRoles.equals(user.getApplicationRoles())) {
            user.setApplicationRoles(newRoles);
            changed = true;
        }

        if (changed) {
            log.debug("Sincronizando dados do usuário: {}", user.getUsername());
        }
    }

    /**
     * Mapeia roles do Keycloak para roles da aplicação
     */
    private Set<User.ApplicationRole> mapKeycloakRolesToApplicationRoles(KeycloakUser keycloakUser) {
        Set<User.ApplicationRole> appRoles = Set.of();

        if (keycloakUser.hasRole("ADMIN")) {
            appRoles = Set.of(
                User.ApplicationRole.SYSTEM_ADMIN,
                User.ApplicationRole.SERVER_MANAGER,
                User.ApplicationRole.ALERT_MANAGER,
                User.ApplicationRole.REPORT_VIEWER,
                User.ApplicationRole.MONITORING_VIEWER
            );
        } else if (keycloakUser.hasRole("USER")) {
            appRoles = Set.of(
                User.ApplicationRole.MONITORING_VIEWER,
                User.ApplicationRole.REPORT_VIEWER
            );
        }

        // Roles específicas adicionais
        if (keycloakUser.hasRole("SERVER_MANAGER")) {
            appRoles = Set.of(User.ApplicationRole.SERVER_MANAGER);
        }
        
        if (keycloakUser.hasRole("ALERT_MANAGER")) {
            appRoles = Set.of(User.ApplicationRole.ALERT_MANAGER);
        }

        return appRoles;
    }

    /**
     * Busca usuário por ID do Keycloak
     */
    @Transactional(readOnly = true)
    public Optional<User> findByKeycloakId(String keycloakId) {
        return userRepository.findByKeycloakId(keycloakId);
    }

    /**
     * Busca usuário por username
     */
    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * Lista todos os usuários ativos
     */
    @Transactional(readOnly = true)
    public List<User> findActiveUsers() {
        return userRepository.findByActiveTrue();
    }

    /**
     * Atualiza preferências do usuário
     */
    public User updateUserPreferences(String keycloakId, String preferences, 
                                     User.Theme theme, String language, String timezone) {
        User user = userRepository.findByKeycloakId(keycloakId)
            .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        user.setPreferences(preferences);
        user.setTheme(theme);
        user.setLanguage(language);
        user.setTimezone(timezone);

        return userRepository.save(user);
    }

    /**
     * Desativa usuário
     */
    public void deactivateUser(String keycloakId) {
        User user = userRepository.findByKeycloakId(keycloakId)
            .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));
        
        user.setActive(false);
        userRepository.save(user);
        
        log.info("Usuário desativado: {}", user.getUsername());
    }

    /**
     * Reativa usuário
     */
    public void activateUser(String keycloakId) {
        User user = userRepository.findByKeycloakId(keycloakId)
            .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));
        
        user.setActive(true);
        userRepository.save(user);
        
        log.info("Usuário reativado: {}", user.getUsername());
    }

    /**
     * Estatísticas de usuários
     */
    @Transactional(readOnly = true)
    public UserStats getUserStats() {
        return UserStats.builder()
            .totalActiveUsers(userRepository.countByActiveTrue())
            .newUsersToday(userRepository.countNewUsersToday())
            .activeUsersLast24h(userRepository.countActiveUsersSince(LocalDateTime.now().minusDays(1)))
            .build();
    }

    /**
     * Classe para estatísticas de usuários
     */
    public record UserStats(
        long totalActiveUsers,
        long newUsersToday,
        long activeUsersLast24h
    ) {
        public static UserStatsBuilder builder() {
            return new UserStatsBuilder();
        }

        public static class UserStatsBuilder {
            private long totalActiveUsers;
            private long newUsersToday;
            private long activeUsersLast24h;

            public UserStatsBuilder totalActiveUsers(long totalActiveUsers) {
                this.totalActiveUsers = totalActiveUsers;
                return this;
            }

            public UserStatsBuilder newUsersToday(long newUsersToday) {
                this.newUsersToday = newUsersToday;
                return this;
            }

            public UserStatsBuilder activeUsersLast24h(long activeUsersLast24h) {
                this.activeUsersLast24h = activeUsersLast24h;
                return this;
            }

            public UserStats build() {
                return new UserStats(totalActiveUsers, newUsersToday, activeUsersLast24h);
            }
        }
    }
}