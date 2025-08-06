package com.fscip.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.audit.AuditEvent;
import org.springframework.boot.actuate.audit.listener.AuditApplicationEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.access.event.AbstractAuthorizationEvent;
import org.springframework.security.access.event.AuthorizationFailureEvent;
import org.springframework.security.access.event.AuthorizedEvent;
import org.springframework.security.authentication.event.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Security Audit Event Listener for FSCIP application
 * Captures and logs security-related events for monitoring and compliance
 */
@Component
public class SecurityAuditEventListener {

    private static final Logger logger = LoggerFactory.getLogger(SecurityAuditEventListener.class);
    private static final Logger auditLogger = LoggerFactory.getLogger("AUDIT");
    private static final Logger securityLogger = LoggerFactory.getLogger("SECURITY");

    /**
     * Handle authentication success events
     */
    @EventListener
    public void handleAuthenticationSuccess(AuthenticationSuccessEvent event) {
        Authentication auth = event.getAuthentication();
        String username = auth.getName();
        String authType = auth.getClass().getSimpleName();

        Map<String, Object> auditData = new HashMap<>();
        auditData.put("event", "AUTHENTICATION_SUCCESS");
        auditData.put("username", username);
        auditData.put("authenticationType", authType);
        auditData.put("authorities", auth.getAuthorities().toString());
        auditData.put("timestamp", Instant.now());

        auditLogger.info("Authentication successful for user '{}' using {}", username, authType);
        logSecurityEvent("AUTHENTICATION_SUCCESS", auditData);
    }

    /**
     * Handle authentication failure events
     */
    @EventListener
    public void handleAuthenticationFailure(AbstractAuthenticationFailureEvent event) {
        String username = event.getAuthentication().getName();
        String reason = event.getException().getMessage();
        String eventType = event.getClass().getSimpleName();

        Map<String, Object> auditData = new HashMap<>();
        auditData.put("event", "AUTHENTICATION_FAILURE");
        auditData.put("username", username);
        auditData.put("reason", reason);
        auditData.put("eventType", eventType);
        auditData.put("timestamp", Instant.now());

        securityLogger.warn("Authentication failed for user '{}': {} ({})", username, reason, eventType);
        logSecurityEvent("AUTHENTICATION_FAILURE", auditData);
    }

    /**
     * Handle bad credentials events
     */
    @EventListener
    public void handleBadCredentials(AuthenticationFailureBadCredentialsEvent event) {
        String username = event.getAuthentication().getName();
        String clientDetails = getClientDetails(event);

        Map<String, Object> auditData = new HashMap<>();
        auditData.put("event", "BAD_CREDENTIALS");
        auditData.put("username", username);
        auditData.put("clientDetails", clientDetails);
        auditData.put("timestamp", Instant.now());

        securityLogger.warn("Bad credentials attempt for user '{}' from {}", username, clientDetails);
        logSecurityEvent("BAD_CREDENTIALS", auditData);
    }

    /**
     * Handle account disabled events
     */
    @EventListener
    public void handleAccountDisabled(AuthenticationFailureDisabledEvent event) {
        String username = event.getAuthentication().getName();

        Map<String, Object> auditData = new HashMap<>();
        auditData.put("event", "ACCOUNT_DISABLED_LOGIN_ATTEMPT");
        auditData.put("username", username);
        auditData.put("timestamp", Instant.now());

        securityLogger.warn("Login attempt on disabled account: '{}'", username);
        logSecurityEvent("ACCOUNT_DISABLED_LOGIN_ATTEMPT", auditData);
    }

    /**
     * Handle account locked events
     */
    @EventListener
    public void handleAccountLocked(AuthenticationFailureLockedEvent event) {
        String username = event.getAuthentication().getName();

        Map<String, Object> auditData = new HashMap<>();
        auditData.put("event", "ACCOUNT_LOCKED_LOGIN_ATTEMPT");
        auditData.put("username", username);
        auditData.put("timestamp", Instant.now());

        securityLogger.warn("Login attempt on locked account: '{}'", username);
        logSecurityEvent("ACCOUNT_LOCKED_LOGIN_ATTEMPT", auditData);
    }

    /**
     * Handle authorization success events
     */
    @EventListener
    public void handleAuthorizationSuccess(AuthorizedEvent event) {
        Authentication auth = event.getAuthentication();
        Object resource = event.getSource();

        Map<String, Object> auditData = new HashMap<>();
        auditData.put("event", "AUTHORIZATION_SUCCESS");
        auditData.put("username", auth.getName());
        auditData.put("resource", resource.toString());
        auditData.put("authorities", auth.getAuthorities().toString());
        auditData.put("timestamp", Instant.now());

        logger.debug("Authorization successful for user '{}' accessing '{}'", 
            auth.getName(), resource.toString());
    }

    /**
     * Handle authorization failure events
     */
    @EventListener
    public void handleAuthorizationFailure(AuthorizationFailureEvent event) {
        Authentication auth = event.getAuthentication();
        Object resource = event.getSource();
        String reason = event.getAccessDeniedException().getMessage();

        Map<String, Object> auditData = new HashMap<>();
        auditData.put("event", "AUTHORIZATION_FAILURE");
        auditData.put("username", auth.getName());
        auditData.put("resource", resource.toString());
        auditData.put("reason", reason);
        auditData.put("authorities", auth.getAuthorities().toString());
        auditData.put("timestamp", Instant.now());

        securityLogger.warn("Authorization failed for user '{}' accessing '{}': {}", 
            auth.getName(), resource.toString(), reason);
        logSecurityEvent("AUTHORIZATION_FAILURE", auditData);
    }

    /**
     * Handle general audit events
     */
    @EventListener
    public void handleAuditEvent(AuditApplicationEvent event) {
        AuditEvent auditEvent = event.getAuditEvent();
        String eventType = auditEvent.getType();
        String principal = auditEvent.getPrincipal();

        Map<String, Object> auditData = new HashMap<>();
        auditData.put("event", eventType);
        auditData.put("principal", principal);
        auditData.put("data", auditEvent.getData());
        auditData.put("timestamp", auditEvent.getTimestamp());

        auditLogger.info("Audit event '{}' for principal '{}' with data: {}", 
            eventType, principal, auditEvent.getData());
    }

    /**
     * Log security events in structured format
     */
    private void logSecurityEvent(String eventType, Map<String, Object> auditData) {
        try {
            // In production, this could be sent to a SIEM system or security analytics platform
            auditLogger.info("SECURITY_EVENT: {} - {}", eventType, auditData);
        } catch (Exception e) {
            logger.error("Failed to log security event", e);
        }
    }

    /**
     * Extract client details from authentication event
     */
    private String getClientDetails(AbstractAuthenticationFailureEvent event) {
        try {
            Object details = event.getAuthentication().getDetails();
            return details != null ? details.toString() : "unknown";
        } catch (Exception e) {
            return "unknown";
        }
    }
}