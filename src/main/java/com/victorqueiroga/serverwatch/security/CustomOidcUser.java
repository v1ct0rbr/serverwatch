package com.victorqueiroga.serverwatch.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import java.util.Collection;
import java.util.Map;

/**
 * Implementação customizada de OidcUser que permite authorities personalizadas
 */
public class CustomOidcUser implements OidcUser {
    
    private final OidcUser delegate;
    private final Collection<? extends GrantedAuthority> authorities;
    
    public CustomOidcUser(OidcUser delegate, Collection<? extends GrantedAuthority> authorities) {
        this.delegate = delegate;
        this.authorities = authorities;
    }
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }
    
    @Override
    public Map<String, Object> getAttributes() {
        return delegate.getAttributes();
    }
    
    @Override
    public String getName() {
        return delegate.getName();
    }
    
    @Override
    public Map<String, Object> getClaims() {
        return delegate.getClaims();
    }
    
    @Override
    public OidcUserInfo getUserInfo() {
        return delegate.getUserInfo();
    }
    
    @Override
    public OidcIdToken getIdToken() {
        return delegate.getIdToken();
    }
    
    // Métodos de conveniência para acessar claims comuns
    public String getPreferredUsername() {
        return delegate.getPreferredUsername();
    }
    
    public String getEmail() {
        return delegate.getEmail();
    }
    
    public String getGivenName() {
        return delegate.getGivenName();
    }
    
    public String getFamilyName() {
        return delegate.getFamilyName();
    }
    
    public String getFullName() {
        return delegate.getFullName();
    }
}