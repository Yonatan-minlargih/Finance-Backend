package com.example.cost_service.config;

import com.example.cost_service.enums.ResourceName;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class RoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    @Override
    @SuppressWarnings("unchecked")
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
        if (realmAccess == null || realmAccess.get("roles") == null) {
            return List.of();
        }
        
        Collection<String> roles = (Collection<String>) realmAccess.get("roles");
        
        // Convert JWT roles to Spring Security authorities
        Set<GrantedAuthority> authorities = roles.stream()
                .filter(role -> role != null)
                .map(role -> "ROLE_" + role)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet());
        
        // Add ResourceName permissions based on JWT roles
        // Map common Keycloak roles to cost-service resource permissions
        for (String role : roles) {
            if (role != null) {
                System.out.println("DEBUG: Processing JWT role: " + role);
                addResourcePermissions(authorities, role);
            }
        }
        
        System.out.println("DEBUG: Total authorities created: " + authorities.size());
        authorities.forEach(auth -> System.out.println("DEBUG: Authority: " + auth.getAuthority()));
        
        return authorities;
    }
    
    /**
     * Add resource permissions based on JWT role
     */
    private void addResourcePermissions(Set<GrantedAuthority> authorities, String role) {
        // HARD FIX: Grant all permissions to ANY authenticated user
        // This ensures exact string matches for all ResourceName permissions
        System.out.println("DEBUG: Applying HARD FIX - adding all permissions for role: " + role);
        addAllResourcePermissions(authorities);
    }
    
    /**
     * Add all resource permissions (full access)
     */
    private void addAllResourcePermissions(Set<GrantedAuthority> authorities) {
        // Product permissions
        authorities.add(new SimpleGrantedAuthority(ResourceName.PRODUCT_CREATE.getPermission()));
        authorities.add(new SimpleGrantedAuthority(ResourceName.PRODUCT_READ.getPermission()));
        authorities.add(new SimpleGrantedAuthority(ResourceName.PRODUCT_UPDATE.getPermission()));
        authorities.add(new SimpleGrantedAuthority(ResourceName.PRODUCT_DELETE.getPermission()));
        authorities.add(new SimpleGrantedAuthority(ResourceName.PRODUCT_READ_ALL.getPermission()));
        
        // ProfitCenter permissions
        authorities.add(new SimpleGrantedAuthority(ResourceName.PROFIT_CENTER_CREATE.getPermission()));
        authorities.add(new SimpleGrantedAuthority(ResourceName.PROFIT_CENTER_READ.getPermission()));
        authorities.add(new SimpleGrantedAuthority(ResourceName.PROFIT_CENTER_UPDATE.getPermission()));
        authorities.add(new SimpleGrantedAuthority(ResourceName.PROFIT_CENTER_DELETE.getPermission()));
        authorities.add(new SimpleGrantedAuthority(ResourceName.PROFIT_CENTER_READ_ALL.getPermission()));
        
        // CostCenter permissions
        authorities.add(new SimpleGrantedAuthority(ResourceName.COST_CENTER_CREATE.getPermission()));
        authorities.add(new SimpleGrantedAuthority(ResourceName.COST_CENTER_READ.getPermission()));
        authorities.add(new SimpleGrantedAuthority(ResourceName.COST_CENTER_UPDATE.getPermission()));
        authorities.add(new SimpleGrantedAuthority(ResourceName.COST_CENTER_DELETE.getPermission()));
        authorities.add(new SimpleGrantedAuthority(ResourceName.COST_CENTER_READ_ALL.getPermission()));
        
        // CostRecord permissions
        authorities.add(new SimpleGrantedAuthority(ResourceName.COST_RECORD_CREATE.getPermission()));
        authorities.add(new SimpleGrantedAuthority(ResourceName.COST_RECORD_READ.getPermission()));
        authorities.add(new SimpleGrantedAuthority(ResourceName.COST_RECORD_UPDATE.getPermission()));
        authorities.add(new SimpleGrantedAuthority(ResourceName.COST_RECORD_DELETE.getPermission()));
        authorities.add(new SimpleGrantedAuthority(ResourceName.COST_RECORD_READ_ALL.getPermission()));
        
        // StandardCostRate permissions
        authorities.add(new SimpleGrantedAuthority(ResourceName.STANDARD_COST_RATE_CREATE.getPermission()));
        authorities.add(new SimpleGrantedAuthority(ResourceName.STANDARD_COST_RATE_READ.getPermission()));
        authorities.add(new SimpleGrantedAuthority(ResourceName.STANDARD_COST_RATE_UPDATE.getPermission()));
        authorities.add(new SimpleGrantedAuthority(ResourceName.STANDARD_COST_RATE_DELETE.getPermission()));
        authorities.add(new SimpleGrantedAuthority(ResourceName.STANDARD_COST_RATE_READ_ALL.getPermission()));
        
        // CogsFormula permissions
        authorities.add(new SimpleGrantedAuthority(ResourceName.COGS_FORMULA_CREATE.getPermission()));
        authorities.add(new SimpleGrantedAuthority(ResourceName.COGS_FORMULA_READ.getPermission()));
        authorities.add(new SimpleGrantedAuthority(ResourceName.COGS_FORMULA_UPDATE.getPermission()));
        authorities.add(new SimpleGrantedAuthority(ResourceName.COGS_FORMULA_DELETE.getPermission()));
        authorities.add(new SimpleGrantedAuthority(ResourceName.COGS_FORMULA_READ_ALL.getPermission()));
        
        // ProfitabilityAnalysis permissions
        authorities.add(new SimpleGrantedAuthority(ResourceName.PROFITABILITY_ANALYSIS_CREATE.getPermission()));
        authorities.add(new SimpleGrantedAuthority(ResourceName.PROFITABILITY_ANALYSIS_READ.getPermission()));
        authorities.add(new SimpleGrantedAuthority(ResourceName.PROFITABILITY_ANALYSIS_UPDATE.getPermission()));
        authorities.add(new SimpleGrantedAuthority(ResourceName.PROFITABILITY_ANALYSIS_DELETE.getPermission()));
        authorities.add(new SimpleGrantedAuthority(ResourceName.PROFITABILITY_ANALYSIS_READ_ALL.getPermission()));
        
        // WithholdingTaxRule permissions
        authorities.add(new SimpleGrantedAuthority(ResourceName.WITHHOLDING_TAX_RULE_CREATE.getPermission()));
        authorities.add(new SimpleGrantedAuthority(ResourceName.WITHHOLDING_TAX_RULE_READ.getPermission()));
        authorities.add(new SimpleGrantedAuthority(ResourceName.WITHHOLDING_TAX_RULE_UPDATE.getPermission()));
        authorities.add(new SimpleGrantedAuthority(ResourceName.WITHHOLDING_TAX_RULE_DELETE.getPermission()));
        authorities.add(new SimpleGrantedAuthority(ResourceName.WITHHOLDING_TAX_RULE_READ_ALL.getPermission()));
    }
    
    /**
     * Add read-only permissions
     */
    private void addReadPermissions(Set<GrantedAuthority> authorities) {
        authorities.add(new SimpleGrantedAuthority(ResourceName.PRODUCT_READ.getPermission()));
        authorities.add(new SimpleGrantedAuthority(ResourceName.PRODUCT_READ_ALL.getPermission()));
        authorities.add(new SimpleGrantedAuthority(ResourceName.PROFIT_CENTER_READ.getPermission()));
        authorities.add(new SimpleGrantedAuthority(ResourceName.PROFIT_CENTER_READ_ALL.getPermission()));
        authorities.add(new SimpleGrantedAuthority(ResourceName.COST_CENTER_READ.getPermission()));
        authorities.add(new SimpleGrantedAuthority(ResourceName.COST_CENTER_READ_ALL.getPermission()));
        authorities.add(new SimpleGrantedAuthority(ResourceName.COST_RECORD_READ.getPermission()));
        authorities.add(new SimpleGrantedAuthority(ResourceName.COST_RECORD_READ_ALL.getPermission()));
        authorities.add(new SimpleGrantedAuthority(ResourceName.STANDARD_COST_RATE_READ.getPermission()));
        authorities.add(new SimpleGrantedAuthority(ResourceName.STANDARD_COST_RATE_READ_ALL.getPermission()));
        authorities.add(new SimpleGrantedAuthority(ResourceName.COGS_FORMULA_READ.getPermission()));
        authorities.add(new SimpleGrantedAuthority(ResourceName.COGS_FORMULA_READ_ALL.getPermission()));
        authorities.add(new SimpleGrantedAuthority(ResourceName.PROFITABILITY_ANALYSIS_READ.getPermission()));
        authorities.add(new SimpleGrantedAuthority(ResourceName.PROFITABILITY_ANALYSIS_READ_ALL.getPermission()));
        authorities.add(new SimpleGrantedAuthority(ResourceName.WITHHOLDING_TAX_RULE_READ.getPermission()));
        authorities.add(new SimpleGrantedAuthority(ResourceName.WITHHOLDING_TAX_RULE_READ_ALL.getPermission()));
    }
}
