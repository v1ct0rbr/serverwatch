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
        log.info("════════════════════════════════════════════════════════════════");
        log.info("[OIDC USER SERVICE] INICIANDO CARREGAMENTO DE USUÁRIO OIDC");
        log.info("════════════════════════════════════════════════════════════════");

        // Carregar o usuário OIDC padrão
        OidcUser oidcUser = delegate.loadUser(userRequest);

        log.debug("Authorities padrão do OIDC (incluem scopes): {}",
                oidcUser.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toList()));

        // Extrair APENAS roles reais do ID Token (ignorar scopes padrão)
        OidcIdToken idToken = oidcUser.getIdToken();

        // Log do token bruto
        log.info("[OIDC TOKEN] Token Value (primeiros 100 chars): {}...",
                idToken.getTokenValue().substring(0, Math.min(100, idToken.getTokenValue().length())));

        Set<GrantedAuthority> keycloakAuthorities = extractAuthorities(idToken);

        log.info("Usuário OIDC carregado: {}", oidcUser.getPreferredUsername());
        log.info("Authorities extraídas do Keycloak: {}", keycloakAuthorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList()));

        log.info("════════════════════════════════════════════════════════════════");
        log.info("[OIDC USER SERVICE] CONCLUSÃO DO CARREGAMENTO");
        log.info("════════════════════════════════════════════════════════════════");

        // Retornar um novo OidcUser com APENAS as roles do Keycloak (sem scopes)
        return new CustomOidcUser(oidcUser, keycloakAuthorities);
    }

    /**
     * Extrai APENAS as roles do token, ignorando scopes e outras authorities padrão
     */
    private Set<GrantedAuthority> extractAuthorities(OidcIdToken idToken) {
        Set<GrantedAuthority> authorities = new HashSet<>();

        // DEBUG: Log todos os claims para ver o que tem
        log.info("═══════════════════════════════════════════════════════════════════");
        log.info("[TOKEN CLAIMS DEBUG] TODOS OS CLAIMS DO TOKEN JWT:");
        log.info("═══════════════════════════════════════════════════════════════════");

        Map<String, Object> allClaims = idToken.getClaims();
        log.info("Total de claims no token: {}", allClaims.size());

        allClaims.forEach((key, value) -> {
            String valueStr = String.valueOf(value);
            // Se o valor for muito longo, trunca para 500 chars
            if (valueStr.length() > 500) {
                valueStr = valueStr.substring(0, 500) + "...";
            }
            log.info("  '{}' = {}", key, valueStr);
        });
        log.info("═══════════════════════════════════════════════════════════════════");

        // Extrair roles do realm_access
        Map<String, Object> realmAccess = idToken.getClaimAsMap("realm_access");
        log.info("[TOKEN DEBUG] realm_access claim type: {} | value: {}",
                realmAccess != null ? realmAccess.getClass().getSimpleName() : "NULL", realmAccess);

        if (realmAccess != null && realmAccess.containsKey("roles")) {
            List<String> realmRoles = (List<String>) realmAccess.get("roles");
            authorities.addAll(realmRoles.stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                    .collect(Collectors.toList()));

            log.info("✓ Roles do realm encontradas: {}", realmRoles);
        } else {
            log.warn("✗ realm_access ou realm_access.roles NÃO ENCONTRADOS");
        }

        // Extrair roles do resource_access (client-specific roles)
        Map<String, Object> resourceAccess = idToken.getClaimAsMap("resource_access");
        log.info("[TOKEN DEBUG] resource_access claim type: {} | value: {}",
                resourceAccess != null ? resourceAccess.getClass().getSimpleName() : "NULL", resourceAccess);

        if (resourceAccess != null) {
            log.info("[TOKEN DEBUG] Clientes em resource_access: {}", resourceAccess.keySet());
            for (Map.Entry<String, Object> entry : resourceAccess.entrySet()) {
                String clientId = entry.getKey();
                Map<String, Object> resource = (Map<String, Object>) entry.getValue();
                log.info("[TOKEN DEBUG] Roles do cliente '{}': {}", clientId, resource);

                if (resource != null && resource.containsKey("roles")) {
                    List<String> clientRoles = (List<String>) resource.get("roles");
                    authorities.addAll(clientRoles.stream()
                            .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                            .collect(Collectors.toList()));

                    log.info("✓ Roles do cliente {} encontradas: {}", clientId, clientRoles);
                }
            }
        } else {
            log.warn("✗ resource_access NÃO ENCONTRADO no token");
        }

        // Procurar por outras locations possíveis
        log.info("[TOKEN DEBUG] Procurando alternativas de roles...");

        // Verificar se há roles como claim simples
        Object rolesClain = allClaims.get("roles");
        if (rolesClain != null) {
            log.info("✓ Encontrado claim 'roles' direto: {}", rolesClain);
            if (rolesClain instanceof List) {
                List<String> directRoles = (List<String>) rolesClain;
                authorities.addAll(directRoles.stream()
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                        .collect(Collectors.toList()));
            }
        }

        // Verificar grupos
        Object groups = allClaims.get("groups");
        if (groups != null) {
            log.info("✓ Encontrado claim 'groups': {}", groups);
        }

        if (authorities.isEmpty()) {
            log.error("════════════════════════════════════════════════════════════════════");
            log.error("✗✗✗ NENHUMA ROLE ENCONTRADA NO TOKEN DO KEYCLOAK! ✗✗✗");
            log.error("════════════════════════════════════════════════════════════════════");
            log.error("Possíveis soluções:");
            log.error("  1. Adicione ROLES ao usuário no Keycloak Admin Console");
            log.error("  2. Configure mappers para incluir realm_access.roles no token");
            log.error("  3. Configure mappers para incluir resource_access.roles no token");
            log.error("  4. Verifique se o client scope 'roles' está vinculado ao cliente");
            log.error("════════════════════════════════════════════════════════════════════");
        } else {
            log.info("[TOKEN DEBUG] ✓ Total de roles extraídas: {}", authorities.size());
        }

        return authorities;
    }
}