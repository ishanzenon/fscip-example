package com.fscip.security;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * Custom security expressions and annotations for FSCIP application
 * Provides reusable security expressions for method-level authorization
 */
@Component("securityExpressions")
public class SecurityExpressions {

    /**
     * Check if user can access account data
     */
    public boolean canAccessAccount(String accountId) {
        return SecurityUtils.isAdmin() || 
               SecurityUtils.hasAnyRole("OPERATOR", "ANALYST") ||
               isAccountOwner(accountId);
    }

    /**
     * Check if user can modify account data
     */
    public boolean canModifyAccount(String accountId) {
        return SecurityUtils.isAdmin() || 
               SecurityUtils.hasRole("OPERATOR") ||
               (isAccountOwner(accountId) && SecurityUtils.hasRole("USER"));
    }

    /**
     * Check if user can access transaction data
     */
    public boolean canAccessTransaction(String transactionId) {
        return SecurityUtils.isAdmin() || 
               SecurityUtils.hasAnyRole("OPERATOR", "ANALYST") ||
               isTransactionOwner(transactionId);
    }

    /**
     * Check if user can perform compliance operations
     */
    public boolean canPerformCompliance() {
        return SecurityUtils.hasAnyRole("ADMIN", "COMPLIANCE_OFFICER", "ANALYST");
    }

    /**
     * Check if user can access search functionality
     */
    public boolean canSearch() {
        return SecurityUtils.hasAnyRole("ADMIN", "ANALYST", "INVESTIGATOR");
    }

    /**
     * Check if user can manage rules
     */
    public boolean canManageRules() {
        return SecurityUtils.hasAnyRole("ADMIN", "RULES_MANAGER");
    }

    /**
     * Check if user can access documents
     */
    public boolean canAccessDocuments(String documentId) {
        return SecurityUtils.isAdmin() || 
               SecurityUtils.hasAnyRole("OPERATOR", "ANALYST") ||
               isDocumentOwner(documentId);
    }

    /**
     * Check if user belongs to same organization as the resource
     */
    public boolean sameOrganization(String resourceOrgId) {
        return SecurityUtils.getCurrentOrganizationId()
            .map(userOrgId -> userOrgId.equals(resourceOrgId))
            .orElse(false);
    }

    /**
     * Check if user is the owner of the account
     */
    private boolean isAccountOwner(String accountId) {
        // This would typically query the database to check ownership
        // For now, return false - implement based on your data access layer
        return false;
    }

    /**
     * Check if user is the owner of the transaction
     */
    private boolean isTransactionOwner(String transactionId) {
        // This would typically query the database to check ownership
        // For now, return false - implement based on your data access layer
        return false;
    }

    /**
     * Check if user is the owner of the document
     */
    private boolean isDocumentOwner(String documentId) {
        // This would typically query the database to check ownership
        // For now, return false - implement based on your data access layer
        return false;
    }

    // Security Annotations for common use cases

    /**
     * Requires admin role
     */
    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @PreAuthorize("hasRole('ADMIN')")
    public @interface AdminOnly {
    }

    /**
     * Requires operator or admin role
     */
    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    public @interface OperatorAccess {
    }

    /**
     * Requires analyst, admin, or investigator role
     */
    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST', 'INVESTIGATOR')")
    public @interface AnalystAccess {
    }

    /**
     * Requires compliance officer or admin role
     */
    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPLIANCE_OFFICER')")
    public @interface ComplianceAccess {
    }

    /**
     * Requires user to access their own account or have admin/operator role
     */
    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @PreAuthorize("@securityExpressions.canAccessAccount(#accountId)")
    public @interface CanAccessAccount {
    }

    /**
     * Requires user to modify their own account or have admin/operator role
     */
    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @PreAuthorize("@securityExpressions.canModifyAccount(#accountId)")
    public @interface CanModifyAccount {
    }

    /**
     * Requires user to access transaction or have appropriate role
     */
    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @PreAuthorize("@securityExpressions.canAccessTransaction(#transactionId)")
    public @interface CanAccessTransaction {
    }

    /**
     * Requires search permissions
     */
    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @PreAuthorize("@securityExpressions.canSearch()")
    public @interface CanSearch {
    }

    /**
     * Requires rules management permissions
     */
    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @PreAuthorize("@securityExpressions.canManageRules()")
    public @interface CanManageRules {
    }

    /**
     * Requires same organization access
     */
    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @PreAuthorize("@securityExpressions.sameOrganization(#orgId)")
    public @interface SameOrganization {
    }
}