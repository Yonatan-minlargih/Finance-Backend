package com.example.payroll_service.utility;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class SecurityUtil {
    
    /**
     * Get the current JWT from SecurityContextHolder
     */
    public Jwt getUserJwt() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Jwt) {
            return (Jwt) authentication.getPrincipal();
        }
        return null;
    }
    
    /**
     * Get the JWT token value
     */
    public String getUserToken() {
        Jwt jwt = getUserJwt();
        return jwt != null ? jwt.getTokenValue() : null;
    }
    
    /**
     * Get user ID from JWT claims
     */
    public UUID getUserId() {
        Jwt jwt = getUserJwt();
        if (jwt != null && jwt.hasClaim("sub")) {
            String subject = jwt.getClaimAsString("sub");
            try {
                return UUID.fromString(subject);
            } catch (IllegalArgumentException e) {
                // Handle case where subject is not a UUID
                return null;
            }
        }
        return null;
    }
    
    /**
     * Get tenant ID from JWT claims
     */
    public UUID getTenantId() {
        Jwt jwt = getUserJwt();
        System.out.println("DEBUG: SecurityUtil getTenantId - JWT object: " + (jwt != null ? "present" : "null"));
        if (jwt != null) {
            System.out.println("DEBUG: SecurityUtil getTenantId - JWT claims: " + jwt.getClaims());
            System.out.println("DEBUG: SecurityUtil getTenantId - has tenant_id claim: " + jwt.hasClaim("tenant_id"));
            
            // Try tenant_id claim first
            if (jwt.hasClaim("tenant_id")) {
                String tenantIdStr = jwt.getClaimAsString("tenant_id");
                System.out.println("DEBUG: SecurityUtil getTenantId - tenant_id string: " + tenantIdStr);
                try {
                    UUID result = UUID.fromString(tenantIdStr);
                    System.out.println("DEBUG: SecurityUtil getTenantId - parsed UUID: " + result);
                    return result;
                } catch (IllegalArgumentException e) {
                    System.out.println("DEBUG: SecurityUtil getTenantId - failed to parse tenant_id as UUID: " + e.getMessage());
                    return null;
                }
            } else {
                System.out.println("DEBUG: SecurityUtil getTenantId - NO tenant_id claim found");
                
                // FALLBACK: Use sub claim as tenant ID for testing
                if (jwt.hasClaim("sub")) {
                    String subStr = jwt.getClaimAsString("sub");
                    System.out.println("DEBUG: SecurityUtil getTenantId - using sub as fallback: " + subStr);
                    try {
                        UUID result = UUID.fromString(subStr);
                        System.out.println("DEBUG: SecurityUtil getTenantId - parsed UUID from sub: " + result);
                        return result;
                    } catch (IllegalArgumentException e) {
                        System.out.println("DEBUG: SecurityUtil getTenantId - failed to parse sub as UUID: " + e.getMessage());
                        return null;
                    }
                }
            }
        } else {
            System.out.println("DEBUG: SecurityUtil getTenantId - JWT is null");
        }
        return null;
    }
    
    /**
     * Get any claim from JWT
     */
    public Object getClaim(String claimName) {
        Jwt jwt = getUserJwt();
        return jwt != null ? jwt.getClaim(claimName) : null;
    }
    
    /**
     * Get user's full name from JWT claims
     */
    public String getName() {
        Jwt jwt = getUserJwt();
        if (jwt != null) {
            // Try different claim names for name
            if (jwt.hasClaim("name")) {
                return jwt.getClaimAsString("name");
            }
            if (jwt.hasClaim("preferred_username")) {
                return jwt.getClaimAsString("preferred_username");
            }
            if (jwt.hasClaim("given_name") && jwt.hasClaim("family_name")) {
                return jwt.getClaimAsString("given_name") + " " + jwt.getClaimAsString("family_name");
            }
        }
        return null;
    }
    
    /**
     * Get username from JWT claims
     */
    public String getUsername() {
        Jwt jwt = getUserJwt();
        if (jwt != null) {
            // Try different claim names for username
            if (jwt.hasClaim("preferred_username")) {
                return jwt.getClaimAsString("preferred_username");
            }
            if (jwt.hasClaim("username")) {
                return jwt.getClaimAsString("username");
            }
            if (jwt.hasClaim("email")) {
                return jwt.getClaimAsString("email");
            }
        }
        return null;
    }
}
