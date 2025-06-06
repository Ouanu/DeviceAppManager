package org.ouanu.manager.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.ouanu.manager.iface.PermissionCheck;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Aspect
@Component
public class PermissionAspect {
    @Around("@annotation(permissionCheck)")
    public Object checkUserAccess(ProceedingJoinPoint joinPoint, PermissionCheck permissionCheck) throws Throwable {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication.getPrincipal() instanceof User principal) {
            String[] roles = permissionCheck.roles();
            String role = principal.getAuthorities().toString().replace("ROLE_", "").replace("[", "").replace("]", "");
            System.out.println("checkUserAccess: " + Arrays.toString(roles));
            System.out.println("checkUserAccess: " + role);
            if (!Arrays.toString(roles).contains(role)) {
                System.out.println("Access Denied.");
                throw new AccessDeniedException("Access Denied.");
            }
            return joinPoint.proceed();
        } else if (authentication.getPrincipal() instanceof String)
            System.out.println("permission check = " + authentication.getPrincipal());
        throw new AccessDeniedException("Access Denied.");
    }
}
