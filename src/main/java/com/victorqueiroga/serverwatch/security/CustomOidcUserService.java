package com.victorqueiroga.serverwatch.security;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

/**
 * Serviço customizado para carregar usuários OIDC do Keycloak
 * e extrair suas roles/authorities adequadamente
 */
@Slf4j
@Service
public class CustomOidcUserService implements OAuth2UserService<OidcUserRequest, OidcUser> {

    private final OidcUserService delegate = new OidcUserService();

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        // Carregar o usuário OIDC padrão
        OidcUser oidcUser = delegate.loadUser(userRequest);

        log.debug("Authorities padrão do OIDC (incluem scopes): {}",
                oidcUser.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toList()));

        // Extrair APENAS roles reais do ID Token (ignorar scopes padrão)
        OidcIdToken idToken = oidcUser.getIdToken();
        Set<GrantedAuthority> keycloakAuthorities = extractAuthorities(idToken);

        log.info("Usuário OIDC carregado: {}", oidcUser.getPreferredUsername());
        log.info("Authorities extraídas do Keycloak: {}", keycloakAuthorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList()));

        // Retornar um novo OidcUser com APENAS as roles do Keycloak (sem scopes)
        return new CustomOidcUser(oidcUser, keycloakAuthorities);
    }

    /**
     * Extrai APENAS as roles do token, ignorando scopes e outras authorities padrão
     */
    private Set<GrantedAuthority> extractAuthorities(OidcIdToken idToken) {
        Set<GrantedAuthority> authorities = new HashSet<>();

        // Extrair roles do realm_access
        Map<String, Object> realmAccess = idToken.getClaimAsMap("realm_access");
        if (realmAccess != null && realmAccess.containsKey("roles")) {
            List<String> realmRoles = (List<String>) realmAccess.get("roles");
            authorities.addAll(realmRoles.stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                    .collect(Collectors.toList()));

            log.debug("Roles do realm encontradas: {}", realmRoles);
        }

        // Extrair roles do resource_access (client-specific roles)
        Map<String, Object> resourceAccess = idToken.getClaimAsMap("resource_access");
        if (resourceAccess != null) {
            for (Map.Entry<String, Object> entry : resourceAccess.entrySet()) {
                String clientId = entry.getKey();
                Map<String, Object> resource = (Map<String, Object>) entry.getValue();

                if (resource != null && resource.containsKey("roles")) {
                    List<String> clientRoles = (List<String>) resource.get("roles");
                    authorities.addAll(clientRoles.stream()
                            .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                            .collect(Collectors.toList()));

                    log.debug("Roles do cliente {} encontradas: {}", clientId, clientRoles);
                }
            }
        }

        if (authorities.isEmpty()) {
            log.warn("Nenhuma role encontrada no token do Keycloak!");
            log.warn("Adicione roles ao usuário no Keycloak Admin Console");
        } else {
            log.info("Total de roles extraídas: {}", authorities.size());
        }

        return authorities;
    }
}