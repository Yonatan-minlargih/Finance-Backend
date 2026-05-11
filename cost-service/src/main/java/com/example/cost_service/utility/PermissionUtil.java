package com.example.cost_service.utility;

import com.example.cost_service.enums.ResourceName;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.UUID;

@Component
@Slf4j
public class PermissionUtil {

    /**
     * Check if current user has the specified permission
     */
    public static boolean hasPermission(String permission) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            System.out.println("DEBUG: PermissionUtil checking permission: " + permission);
            
            if (authentication != null && authentication.getAuthorities() != null) {
                System.out.println("DEBUG: User authorities count: " + authentication.getAuthorities().size());
                authentication.getAuthorities().forEach(auth -> 
                    System.out.println("DEBUG: User authority: " + auth.getAuthority()));
                
                boolean hasPermission = authentication.getAuthorities().stream()
                        .anyMatch(authority -> authority.getAuthority().equals(permission));
                System.out.println("DEBUG: PermissionUtil result for " + permission + ": " + hasPermission);
                return hasPermission;
            }
            System.out.println("DEBUG: No authentication or authorities found");
            return false;
        } catch (Exception e) {
            System.out.println("DEBUG: Exception in PermissionUtil: " + e.getMessage());
            log.error("Error checking permission: {}", permission, e);
            return false;
        }
    }

    /**
     * Check if current user has any of the specified permissions
     */
    public static boolean hasAnyPermission(String... permissions) {
        for (String permission : permissions) {
            if (hasPermission(permission)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if current user has all of the specified permissions
     */
    public static boolean hasAllPermissions(String... permissions) {
        for (String permission : permissions) {
            if (!hasPermission(permission)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Get current user's authorities
     */
    public static Collection<String> getCurrentUserPermissions() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getAuthorities() != null) {
            return authentication.getAuthorities().stream()
                    .map(authority -> authority.getAuthority())
                    .toList();
        }
        return java.util.Collections.emptyList();
    }

    /**
     * Check if current user is authenticated
     */
    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated();
    }

    /**
     * Check if current user has admin role
     */
    public static boolean isAdmin() {
        return hasPermission("ROLE_ADMIN");
    }

    /**
     * Check if current user has manager role
     */
    public static boolean isManager() {
        return hasPermission("ROLE_MANAGER") || isAdmin();
    }

    /**
     * Check if current user has user role
     */
    public static boolean isUser() {
        return hasPermission("ROLE_USER") || isManager();
    }

    /**
     * Check permission for specific resource and action
     */
    public static boolean hasResourcePermission(String resource, String action) {
        String permission = String.format("ROLE_%s_%s", resource.toUpperCase(), action.toUpperCase());
        return hasPermission(permission);
    }

    /**
     * Check permission using ResourceName enum
     */
    public static boolean hasPermission(ResourceName resourceName) {
        return hasPermission(resourceName.getPermission());
    }

    /**
     * Check permission for tenant-specific resource
     */
    public static boolean hasTenantResourcePermission(String resource, String action, String tenantId) {
        // Check general resource permission first
        if (!hasResourcePermission(resource, action)) {
            return false;
        }

        // Additional tenant-specific check if needed
        // This could be enhanced with more sophisticated tenant-based permissions
        return true;
    }

    /**
     * Check if user can access their own resources (self-access)
     */
    public static boolean canAccessOwnResource(UUID resourceUserId, UUID currentUserId) {
        return resourceUserId != null && resourceUserId.equals(currentUserId);
    }

    /**
     * Check if user can access tenant-specific resource
     */
    public static boolean canAccessTenantResource(UUID resourceTenantId, UUID userTenantId) {
        return resourceTenantId != null && resourceTenantId.equals(userTenantId);
    }

    /**
     * Check if user has admin-level access to resource
     */
    public static boolean hasAdminAccess() {
        return isAdmin() || hasPermission("ROLE_SUPER_ADMIN");
    }

    /**
     * Check if user can perform action on resource (combines role + ownership + tenant checks)
     */
    public static boolean canPerformAction(ResourceName permission, UUID resourceUserId, UUID resourceTenantId, UUID currentUserId, UUID userTenantId) {
        // Admin can access everything
        if (hasAdminAccess()) {
            return true;
        }

        // Check permission
        if (!hasPermission(permission)) {
            return false;
        }

        // Check tenant access
        if (!canAccessTenantResource(resourceTenantId, userTenantId)) {
            return false;
        }

        // Check self-access for user-specific resources
        if (resourceUserId != null && !canAccessOwnResource(resourceUserId, currentUserId)) {
            return false;
        }

        return true;
    }
}
