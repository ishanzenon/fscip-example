package com.fscip.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Custom JWT Authentication Converter for FSCIP application
 * Handles role extraction and mapping from JWT claims
 */
@Component
public class JwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private static final String ROLES_CLAIM = "roles";
    private static final String REALM_ACCESS_CLAIM = "realm_access";
    private static final String RESOURCE_ACCESS_CLAIM = "resource_access";
    private static final String FSCIP_CLIENT_ID = "fscip-backend";
    private static final String ROLE_PREFIX = "ROLE_";

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        Collection<GrantedAuthority> authorities = extractAuthorities(jwt);
        return new JwtAuthenticationToken(jwt, authorities, getPrincipalClaimName(jwt));
    }

    /**
     * Extract authorities from JWT token
     */
    private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
        return Stream.of(
                extractRealmRoles(jwt),
                extractResourceRoles(jwt),
                extractDirectRoles(jwt)
            )
            .flatMap(Collection::stream)
            .distinct()
            .collect(Collectors.toList());
    }

    /**
     * Extract roles from realm_access claim (Keycloak format)
     */
    @SuppressWarnings("unchecked")
    private Collection<GrantedAuthority> extractRealmRoles(Jwt jwt) {
        Map<String, Object> realmAccess = jwt.getClaimAsMap(REALM_ACCESS_CLAIM);
        if (realmAccess == null || realmAccess.get(ROLES_CLAIM) == null) {
            return Collections.emptyList();
        }

        Collection<String> realmRoles = (Collection<String>) realmAccess.get(ROLES_CLAIM);
        return mapRolesToAuthorities(realmRoles);
    }

    /**
     * Extract roles from resource_access claim (Keycloak format)
     */
    @SuppressWarnings("unchecked")
    private Collection<GrantedAuthority> extractResourceRoles(Jwt jwt) {
        Map<String, Object> resourceAccess = jwt.getClaimAsMap(RESOURCE_ACCESS_CLAIM);
        if (resourceAccess == null || resourceAccess.get(FSCIP_CLIENT_ID) == null) {
            return Collections.emptyList();
        }

        Map<String, Object> clientAccess = (Map<String, Object>) resourceAccess.get(FSCIP_CLIENT_ID);
        if (clientAccess == null || clientAccess.get(ROLES_CLAIM) == null) {
            return Collections.emptyList();
        }

        Collection<String> clientRoles = (Collection<String>) clientAccess.get(ROLES_CLAIM);
        return mapRolesToAuthorities(clientRoles);
    }

    /**
     * Extract roles from direct roles claim (custom format)
     */
    private Collection<GrantedAuthority> extractDirectRoles(Jwt jwt) {
        Collection<String> roles = jwt.getClaimAsStringList(ROLES_CLAIM);
        if (roles == null) {
            return Collections.emptyList();
        }
        return mapRolesToAuthorities(roles);
    }

    /**
     * Map role strings to GrantedAuthority objects
     */
    private Collection<GrantedAuthority> mapRolesToAuthorities(Collection<String> roles) {
        return roles.stream()
            .filter(StringUtils::hasText)
            .map(role -> role.startsWith(ROLE_PREFIX) ? role : ROLE_PREFIX + role.toUpperCase())
            .map(SimpleGrantedAuthority::new)
            .collect(Collectors.toList());
    }

    /**
     * Get the principal claim name (username or subject)
     */
    private String getPrincipalClaimName(Jwt jwt) {
        String username = jwt.getClaimAsString("preferred_username");
        if (StringUtils.hasText(username)) {
            return username;
        }
        
        String email = jwt.getClaimAsString("email");
        if (StringUtils.hasText(email)) {
            return email;
        }
        
        return jwt.getSubject();
    }
}