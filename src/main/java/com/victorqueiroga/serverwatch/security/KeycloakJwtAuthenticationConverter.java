package com.victorqueiroga.serverwatch.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Converter que extrai as authorities/roles dos tokens JWT do Keycloak
 */
@Component
public class KeycloakJwtAuthenticationConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        
        // Extrair roles do realm_access
        Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
        if (realmAccess != null && realmAccess.containsKey("roles")) {
            List<String> realmRoles = (List<String>) realmAccess.get("roles");
            authorities.addAll(realmRoles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                .collect(Collectors.toList()));
        }
        
        // Extrair roles do resource_access (client-specific roles)
        Map<String, Object> resourceAccess = jwt.getClaimAsMap("resource_access");
        if (resourceAccess != null) {
            for (Map.Entry<String, Object> entry : resourceAccess.entrySet()) {
                String clientId = entry.getKey();
                Map<String, Object> resource = (Map<String, Object>) entry.getValue();
                
                if (resource.containsKey("roles")) {
                    List<String> clientRoles = (List<String>) resource.get("roles");
                    authorities.addAll(clientRoles.stream()
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + clientId.toUpperCase() + "_" + role.toUpperCase()))
                        .collect(Collectors.toList()));
                }
            }
        }
        
        // Adicionar authorities padrão baseadas em claims específicos
        if (jwt.hasClaim("email_verified") && jwt.getClaimAsBoolean("email_verified")) {
            authorities.add(new SimpleGrantedAuthority("ROLE_EMAIL_VERIFIED"));
        }
        
        return authorities;
    }
}