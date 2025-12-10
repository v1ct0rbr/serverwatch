package com.victorqueiroga.serverwatch.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.victorqueiroga.serverwatch.model.User;
import com.victorqueiroga.serverwatch.repository.UserRepository;
import com.victorqueiroga.serverwatch.security.AuthDebugService;
import com.victorqueiroga.serverwatch.security.KeycloakUser;
import com.victorqueiroga.serverwatch.security.KeycloakUserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Serviço que gerencia usuários locais integrados com Keycloak
 * Este serviço sincroniza dados entre Keycloak e banco local
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
@Profile("!dev") // Exclui do profile dev
public class UserService {

    private final UserRepository userRepository;
    private final KeycloakUserService keycloakUserService;
    private final AuthDebugService authDebugService;

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
        log.info("getOrCreateUser: Buscando usuário com Keycloak ID: {}", keycloakUser.getId());

        Optional<User> existingUser = userRepository.findByKeycloakId(keycloakUser.getId());

        if (existingUser.isPresent()) {
            // Atualiza dados do usuário existente
            User user = existingUser.get();
            log.info("Usuário existente encontrado: {}", user.getUsername());
            syncUserWithKeycloak(user, keycloakUser);
            user.registerLogin();
            User savedUser = userRepository.save(user);
            log.info("Usuário atualizado e salvo com roles: {}", savedUser.getApplicationRoles());
            return savedUser;
        } else {
            // Cria novo usuário
            log.info("Usuário não encontrado, criando novo usuário: {}", keycloakUser.getUsername());
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
        log.debug("Iniciando sincronização de usuário: {}", user.getUsername());

        authDebugService.logKeycloakUserDetails(keycloakUser);

        boolean changed = false;

        // Sincronizar dados básicos
        if (!keycloakUser.getUsername().equals(user.getUsername()) ||
                !keycloakUser.getEmail().equals(user.getEmail()) ||
                !keycloakUser.getFullName().equals(user.getFullName())) {

            log.debug("Dados do usuário mudaram, atualizando...");
            user.syncWithKeycloak(
                    keycloakUser.getUsername(),
                    keycloakUser.getEmail(),
                    keycloakUser.getFullName());
            changed = true;
        }

        // Sincronizar roles
        Set<User.ApplicationRole> newRoles = mapKeycloakRolesToApplicationRoles(keycloakUser);
        Set<User.ApplicationRole> oldRoles = user.getApplicationRoles();

        if (!newRoles.equals(oldRoles)) {
            log.info("Roles mudaram para usuário {}: {} -> {}",
                    user.getUsername(), oldRoles, newRoles);
            user.setApplicationRoles(newRoles);
            changed = true;
        } else {
            log.debug("Roles não mudaram para usuário {}", user.getUsername());
        }

        if (changed) {
            log.info("Usuário {} sincronizado com sucesso. Roles: {}",
                    user.getUsername(), user.getApplicationRoles());
        }
    }

    /**
     * Mapeia roles do Keycloak para roles da aplicação
     */
    private Set<User.ApplicationRole> mapKeycloakRolesToApplicationRoles(KeycloakUser keycloakUser) {
        Set<User.ApplicationRole> appRoles = new java.util.HashSet<>();

        if (keycloakUser.hasRole("ADMIN")) {
            appRoles.add(User.ApplicationRole.SYSTEM_ADMIN);
            appRoles.add(User.ApplicationRole.SERVER_MANAGER);
            appRoles.add(User.ApplicationRole.ALERT_MANAGER);
            appRoles.add(User.ApplicationRole.REPORT_VIEWER);
            appRoles.add(User.ApplicationRole.MONITORING_VIEWER);
        } else {
            if (keycloakUser.hasRole("USER")) {
                appRoles.add(User.ApplicationRole.MONITORING_VIEWER);
                appRoles.add(User.ApplicationRole.REPORT_VIEWER);
            }

            // Roles específicas adicionais (podem ser combinadas)
            if (keycloakUser.hasRole("SERVER_MANAGER")) {
                appRoles.add(User.ApplicationRole.SERVER_MANAGER);
            }

            if (keycloakUser.hasRole("ALERT_MANAGER")) {
                appRoles.add(User.ApplicationRole.ALERT_MANAGER);
            }
        }

        if (appRoles.isEmpty()) {
            // Adicionar role padrão se nenhuma for encontrada
            appRoles.add(User.ApplicationRole.MONITORING_VIEWER);
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
        LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);

        return UserStats.builder()
                .totalActiveUsers(userRepository.countByActiveTrue())
                .newUsersToday(userRepository.countNewUsersToday(startOfDay, endOfDay))
                .activeUsersLast24h(userRepository.countActiveUsersSince(LocalDateTime.now().minusDays(1)))
                .build();
    }

    /**
     * Classe para estatísticas de usuários
     */
    public record UserStats(
            long totalActiveUsers,
            long newUsersToday,
            long activeUsersLast24h) {
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