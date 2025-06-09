package org.ouanu.manager.common;

import org.ouanu.manager.model.Device;
import org.ouanu.manager.model.User;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;


public class DeviceAuthentication extends AbstractAuthenticationToken {
    private final String principal;
    private final String credentials;

    public DeviceAuthentication(String principal, String credentials, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.principal = principal;
        this.credentials = credentials;
        super.setAuthenticated(true);
    }

    @Override
    public String getCredentials() {
        return credentials;
    }

    @Override
    public String getPrincipal() {
        return principal;
    }
}
