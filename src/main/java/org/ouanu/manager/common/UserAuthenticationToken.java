package org.ouanu.manager.common;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

public class UserAuthenticationToken extends UsernamePasswordAuthenticationToken {
    private final UserDetails principal;
    private final Object credentials;

    public UserAuthenticationToken(UserDetails principal, Object credentials) {
        super(principal, credentials);
        this.principal = principal;
        this.credentials = credentials;
    }

    public UserAuthenticationToken(UserDetails principal, Object credentials, Collection<? extends GrantedAuthority> authorities) {
        super(principal, credentials, authorities);
        this.principal = principal;
        this.credentials = credentials;
    }


    @Override
    public Object getCredentials() {
        return credentials;
    }

    @Override
    public UserDetails getPrincipal() {
        return principal;
    }
}
