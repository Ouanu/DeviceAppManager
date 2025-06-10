package org.ouanu.manager.filter;

import com.auth0.jwt.exceptions.JWTVerificationException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jetbrains.annotations.NotNull;
import org.ouanu.manager.common.DeviceAuthenticationToken;
import org.ouanu.manager.common.UserAuthenticationToken;
import org.ouanu.manager.service.DeviceDetailsServiceImpl;
import org.ouanu.manager.service.DeviceService;
import org.ouanu.manager.service.EnhancedJwtService;
import org.ouanu.manager.service.UserDetailsServiceImpl;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final UserDetailsServiceImpl userDetailsService;
    private final DeviceDetailsServiceImpl deviceDetailsService;
    private final EnhancedJwtService jwtService;

    public JwtAuthFilter(UserDetailsServiceImpl userDetailsService, DeviceDetailsServiceImpl deviceDetailsService, DeviceService deviceService, EnhancedJwtService jwtService) {
        this.userDetailsService = userDetailsService;
        this.deviceDetailsService = deviceDetailsService;
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull FilterChain filterChain) throws ServletException, IOException {
        try {
            // 跳过不需要认证的路径
            String token = parseToken(request);
            if (token != null) {
                String username = jwtService.getUsernameFromToken(token);
                if (username != null) {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                    if (userDetails != null) {
                        UserAuthenticationToken authenticationToken = new UserAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );
                        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                    } else {
                        userDetails = deviceDetailsService.loadUserByUsername(username);
                        if (userDetails != null) {
                            DeviceAuthenticationToken authenticationToken = new DeviceAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );
                            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                        }
                    }
                }
            }
        } catch (JWTVerificationException e) {
            logger.warn("Invalid JWT: {}", e);
        }
        filterChain.doFilter(request, response);
    }

    private String parseToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}
