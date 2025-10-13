package com.victorqueiroga.serverwatch.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority;
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
        
        // Extrair authorities customizadas dos claims do token
        Set<GrantedAuthority> authorities = new HashSet<>(oidcUser.getAuthorities());
        
        // Extrair roles do ID Token
        OidcIdToken idToken = oidcUser.getIdToken();
        Collection<GrantedAuthority> keycloakAuthorities = extractAuthorities(idToken);
        authorities.addAll(keycloakAuthorities);
        
        log.info("Usuário OIDC carregado: {}", oidcUser.getPreferredUsername());
        log.info("Authorities extraídas: {}", authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList()));
        
        // Retornar um novo OidcUser com as authorities atualizadas
        return new CustomOidcUser(oidcUser, authorities);
    }
    
    /**
     * Extrai authorities dos claims do token ID do Keycloak
     */
    private Collection<GrantedAuthority> extractAuthorities(OidcIdToken idToken) {
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        
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
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + clientId.toUpperCase() + "_" + role.toUpperCase()))
                        .collect(Collectors.toList()));
                    
                    log.debug("Roles do client '{}' encontradas: {}", clientId, clientRoles);
                }
            }
        }
        
        // Adicionar authority baseada na verificação de email
        if (idToken.getEmailVerified() != null && idToken.getEmailVerified()) {
            authorities.add(new SimpleGrantedAuthority("ROLE_EMAIL_VERIFIED"));
        }
        
        // Adicionar roles padrão baseadas em grupos ou outros claims se necessário
        addDefaultRoles(authorities, idToken);
        
        return authorities;
    }
    
    /**
     * Adiciona roles padrão baseadas em lógica de negócio específica
     */
    private void addDefaultRoles(Collection<GrantedAuthority> authorities, OidcIdToken idToken) {
        // Verificar se o usuário tem pelo menos uma role
        boolean hasAnyRole = authorities.stream()
                .anyMatch(auth -> auth.getAuthority().startsWith("ROLE_") && 
                         !auth.getAuthority().equals("ROLE_EMAIL_VERIFIED"));
        
        // Se não tem nenhuma role específica, adicionar USER como padrão
        if (!hasAnyRole) {
            authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
            log.info("Adicionando ROLE_USER padrão para usuário: {}", idToken.getPreferredUsername());
        }
        
        // Lógica adicional baseada em email ou outros atributos
        String email = idToken.getEmail();
        if (email != null && email.endsWith("@der.pb.gov.br")) {
            authorities.add(new SimpleGrantedAuthority("ROLE_DERPB_USER"));
        }
    }
}