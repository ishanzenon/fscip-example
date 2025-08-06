package com.fscip.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;

/**
 * Security configuration properties for FSCIP application
 * Binds security-related configuration from application.properties
 * Follows coding standards for configuration management
 */
@Component
@ConfigurationProperties(prefix = "fscip.security")
public class SecurityProperties {

    private Jwt jwt = new Jwt();
    private Cors cors = new Cors();
    private RateLimit rateLimit = new RateLimit();
    private Session session = new Session();

    // Getters and setters
    public Jwt getJwt() {
        return jwt;
    }

    public void setJwt(Jwt jwt) {
        this.jwt = jwt;
    }

    public Cors getCors() {
        return cors;
    }

    public void setCors(Cors cors) {
        this.cors = cors;
    }

    public RateLimit getRateLimit() {
        return rateLimit;
    }

    public void setRateLimit(RateLimit rateLimit) {
        this.rateLimit = rateLimit;
    }

    public Session getSession() {
        return session;
    }

    public void setSession(Session session) {
        this.session = session;
    }

    public static class Jwt {
        private Duration clockSkew = Duration.ofMinutes(1);
        private Duration cacheDuration = Duration.ofMinutes(5);
        private String authoritiesClaimName = "roles";
        private String authorityPrefix = "ROLE_";

        // Getters and setters
        public Duration getClockSkew() {
            return clockSkew;
        }

        public void setClockSkew(Duration clockSkew) {
            this.clockSkew = clockSkew;
        }

        public Duration getCacheDuration() {
            return cacheDuration;
        }

        public void setCacheDuration(Duration cacheDuration) {
            this.cacheDuration = cacheDuration;
        }

        public String getAuthoritiesClaimName() {
            return authoritiesClaimName;
        }

        public void setAuthoritiesClaimName(String authoritiesClaimName) {
            this.authoritiesClaimName = authoritiesClaimName;
        }

        public String getAuthorityPrefix() {
            return authorityPrefix;
        }

        public void setAuthorityPrefix(String authorityPrefix) {
            this.authorityPrefix = authorityPrefix;
        }
    }

    public static class Cors {
        private List<String> allowedOriginPatterns = List.of(
            "http://localhost:3000",
            "http://localhost:5173",
            "https://*.fscip.com"
        );
        private List<String> allowedMethods = List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS");
        private List<String> allowedHeaders = List.of("Authorization", "Content-Type", "X-Requested-With", "X-CSRF-Token", "Cache-Control");
        private List<String> exposedHeaders = List.of("X-Total-Count", "X-Page-Number", "X-Page-Size");
        private boolean allowCredentials = true;
        private long maxAge = 3600L;

        // Getters and setters
        public List<String> getAllowedOriginPatterns() {
            return allowedOriginPatterns;
        }

        public void setAllowedOriginPatterns(List<String> allowedOriginPatterns) {
            this.allowedOriginPatterns = allowedOriginPatterns;
        }

        public List<String> getAllowedMethods() {
            return allowedMethods;
        }

        public void setAllowedMethods(List<String> allowedMethods) {
            this.allowedMethods = allowedMethods;
        }

        public List<String> getAllowedHeaders() {
            return allowedHeaders;
        }

        public void setAllowedHeaders(List<String> allowedHeaders) {
            this.allowedHeaders = allowedHeaders;
        }

        public List<String> getExposedHeaders() {
            return exposedHeaders;
        }

        public void setExposedHeaders(List<String> exposedHeaders) {
            this.exposedHeaders = exposedHeaders;
        }

        public boolean isAllowCredentials() {
            return allowCredentials;
        }

        public void setAllowCredentials(boolean allowCredentials) {
            this.allowCredentials = allowCredentials;
        }

        public long getMaxAge() {
            return maxAge;
        }

        public void setMaxAge(long maxAge) {
            this.maxAge = maxAge;
        }
    }

    public static class RateLimit {
        private boolean enabled = true;
        private int requestsPerMinute = 100;
        private int burstCapacity = 200;

        // Getters and setters
        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public int getRequestsPerMinute() {
            return requestsPerMinute;
        }

        public void setRequestsPerMinute(int requestsPerMinute) {
            this.requestsPerMinute = requestsPerMinute;
        }

        public int getBurstCapacity() {
            return burstCapacity;
        }

        public void setBurstCapacity(int burstCapacity) {
            this.burstCapacity = burstCapacity;
        }
    }

    public static class Session {
        private Duration timeout = Duration.ofMinutes(30);
        private int maxConcurrentSessions = 1;
        private boolean preventSessionFixation = true;

        // Getters and setters
        public Duration getTimeout() {
            return timeout;
        }

        public void setTimeout(Duration timeout) {
            this.timeout = timeout;
        }

        public int getMaxConcurrentSessions() {
            return maxConcurrentSessions;
        }

        public void setMaxConcurrentSessions(int maxConcurrentSessions) {
            this.maxConcurrentSessions = maxConcurrentSessions;
        }

        public boolean isPreventSessionFixation() {
            return preventSessionFixation;
        }

        public void setPreventSessionFixation(boolean preventSessionFixation) {
            this.preventSessionFixation = preventSessionFixation;
        }
    }
}