package com.fscip.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.Optional;

/**
 * Security utility class for FSCIP application
 * Provides common security operations and user context retrieval
 */
@Component
public class SecurityUtils {

    /**
     * Get the current authenticated user's username
     */
    public static Optional<String> getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }

        if (authentication instanceof JwtAuthenticationToken) {
            JwtAuthenticationToken jwtToken = (JwtAuthenticationToken) authentication;
            Jwt jwt = jwtToken.getToken();
            
            // Try preferred_username first (Keycloak standard)
            String username = jwt.getClaimAsString("preferred_username");
            if (StringUtils.hasText(username)) {
                return Optional.of(username);
            }
            
            // Fall back to email
            String email = jwt.getClaimAsString("email");
            if (StringUtils.hasText(email)) {
                return Optional.of(email);
            }
            
            // Fall back to subject
            return Optional.of(jwt.getSubject());
        }
        
        return Optional.of(authentication.getName());
    }

    /**
     * Get the current authenticated user's ID from JWT subject claim
     */
    public static Optional<String> getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication instanceof JwtAuthenticationToken) {
            JwtAuthenticationToken jwtToken = (JwtAuthenticationToken) authentication;
            return Optional.of(jwtToken.getToken().getSubject());
        }
        
        return Optional.empty();
    }

    /**
     * Get the current user's email from JWT claims
     */
    public static Optional<String> getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication instanceof JwtAuthenticationToken) {
            JwtAuthenticationToken jwtToken = (JwtAuthenticationToken) authentication;
            String email = jwtToken.getToken().getClaimAsString("email");
            return StringUtils.hasText(email) ? Optional.of(email) : Optional.empty();
        }
        
        return Optional.empty();
    }

    /**
     * Get the current user's full name from JWT claims
     */
    public static Optional<String> getCurrentUserFullName() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication instanceof JwtAuthenticationToken) {
            JwtAuthenticationToken jwtToken = (JwtAuthenticationToken) authentication;
            Jwt jwt = jwtToken.getToken();
            
            String name = jwt.getClaimAsString("name");
            if (StringUtils.hasText(name)) {
                return Optional.of(name);
            }
            
            // Construct from first and last name
            String firstName = jwt.getClaimAsString("given_name");
            String lastName = jwt.getClaimAsString("family_name");
            
            if (StringUtils.hasText(firstName) && StringUtils.hasText(lastName)) {
                return Optional.of(firstName + " " + lastName);
            }
        }
        
        return Optional.empty();
    }

    /**
     * Check if the current user has a specific role
     */
    public static boolean hasRole(String role) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        String roleWithPrefix = role.startsWith("ROLE_") ? role : "ROLE_" + role.toUpperCase();
        
        return authentication.getAuthorities().stream()
            .anyMatch(authority -> authority.getAuthority().equals(roleWithPrefix));
    }

    /**
     * Check if the current user has any of the specified roles
     */
    public static boolean hasAnyRole(String... roles) {
        for (String role : roles) {
            if (hasRole(role)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if the current user has all of the specified roles
     */
    public static boolean hasAllRoles(String... roles) {
        for (String role : roles) {
            if (!hasRole(role)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Get all current user's authorities
     */
    public static Collection<? extends GrantedAuthority> getCurrentUserAuthorities() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null ? authentication.getAuthorities() : null;
    }

    /**
     * Check if the current user is authenticated
     */
    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated() && 
               !"anonymousUser".equals(authentication.getName());
    }

    /**
     * Get the JWT token from current authentication
     */
    public static Optional<Jwt> getCurrentJwtToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication instanceof JwtAuthenticationToken) {
            return Optional.of(((JwtAuthenticationToken) authentication).getToken());
        }
        
        return Optional.empty();
    }

    /**
     * Get custom claim from current JWT token
     */
    public static Optional<String> getJwtClaim(String claimName) {
        return getCurrentJwtToken()
            .map(jwt -> jwt.getClaimAsString(claimName))
            .filter(StringUtils::hasText);
    }

    /**
     * Check if the current user has admin privileges
     */
    public static boolean isAdmin() {
        return hasRole("ADMIN");
    }

    /**
     * Check if the current user is a system user (service account)
     */
    public static boolean isSystemUser() {
        return getJwtClaim("typ")
            .map("Bearer"::equals)
            .orElse(false) && 
            getJwtClaim("azp")
            .map(azp -> azp.contains("system") || azp.contains("service"))
            .orElse(false);
    }

    /**
     * Get the organization/tenant ID from JWT claims
     */
    public static Optional<String> getCurrentOrganizationId() {
        return getJwtClaim("org_id")
            .or(() -> getJwtClaim("organization_id"))
            .or(() -> getJwtClaim("tenant_id"));
    }

    /**
     * Check if user belongs to specific organization
     */
    public static boolean belongsToOrganization(String organizationId) {
        return getCurrentOrganizationId()
            .map(orgId -> orgId.equals(organizationId))
            .orElse(false);
    }
}