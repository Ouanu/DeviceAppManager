package org.ouanu.manager.config;

import jakarta.servlet.http.HttpServletRequest;
import org.ouanu.manager.debug.RequestDebugUtil;
import org.ouanu.manager.filter.JwtAuthFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Value("${security.allowed.ips}")
    private List<String> allowedIps;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                // 授权规则
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/auth/register",
                                "/api/auth/login",
                                "/api/device/register",
                                "/api/device/login",
                                "/public/**",
                                "/error",
                                "/favicon.ico"
                        )
                        .permitAll()
                        // 管理接口需要特定IP和MANAGER角色
                        .requestMatchers("/api/manager/list", "/api/manager/delete").access(this::hasIpAndManagerRole) // 限定指定IP和Token
                        .requestMatchers("/api/manager/register").access(this::hasIp) // 限定指定IP管理数据库
                        .requestMatchers("/api/app/**").access(this::hasIp) // 限定指定IP管理数据库
                        .anyRequest()
                        .authenticated()
                )
                // 关闭CSRF和Session（API服务标准配置）
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 添加Jwt过滤器
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    private AuthorizationDecision hasIp(Supplier<Authentication> authenticationSupplier, RequestAuthorizationContext requestAuthorizationContext) {
        // 获取客户端IP
        String clientIp = getClientIp(requestAuthorizationContext.getRequest());
        // 检查IP是否在白名单中
        boolean ipAllowed = allowedIps.contains(clientIp);
        return new AuthorizationDecision(ipAllowed);
    }

    private AuthorizationDecision hasIpAndManagerRole(Supplier<Authentication> authentication,
                                                      RequestAuthorizationContext context) {
        // 获取客户端IP
        String clientIp = getClientIp(context.getRequest());

        // 检查IP是否在白名单中
        boolean ipAllowed = allowedIps.contains(clientIp);
        // 检查用户是否有MANAGER角色
        boolean hasRequiredRole = authentication.get().getAuthorities().stream()
                .anyMatch(grantedAuthority ->
                        grantedAuthority.getAuthority().equals("ROLE_MANAGER") ||
                        grantedAuthority.getAuthority().equals("ADMIN") ||
                                grantedAuthority.getAuthority().equals("ROLE_ADMIN"));

        return new AuthorizationDecision(ipAllowed && hasRequiredRole);
//        return new AuthorizationDecision(ipAllowed);
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        System.out.println(RequestDebugUtil.formatRequestDump(request));
        return ip.split(",")[0].trim(); // 处理多级代理情况
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config
    ) throws Exception {
//        System.out.println("Loading AuthenticationManager......");
        return config.getAuthenticationManager();
    }


}
