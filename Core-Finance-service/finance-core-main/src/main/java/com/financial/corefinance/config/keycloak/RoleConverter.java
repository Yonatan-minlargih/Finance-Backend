package com.financial.corefinance.config.keycloak;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import lombok.NonNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
public class RoleConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private static final String REALM_ACCESS_CLAIM = "realm_access";
    private static final String ROLES_CLAIM = "roles";
    private static final String ROLE_PREFIX = "ROLE_";

    @Override
    public AbstractAuthenticationToken convert(@NonNull Jwt jwt) {
        return new JwtAuthenticationToken(jwt, extractAuthorities(jwt));
    }

    private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
        Map<String, Object> realmAccess = jwt.getClaim(REALM_ACCESS_CLAIM);
        if (realmAccess == null || realmAccess.isEmpty()) {
            return new ArrayList<>();
        }

        ObjectMapper mapper = new ObjectMapper();
        List<String> keycloakRoles = mapper.convertValue(
                realmAccess.get(ROLES_CLAIM),
                new TypeReference<List<String>>() {
                }
        );

        List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        for (String keycloakRole : keycloakRoles) {
            String normalized = normalizeRole(keycloakRole);
            if (!normalized.isBlank()) {
                grantedAuthorities.add(new SimpleGrantedAuthority(normalized));
            }
        }
        return grantedAuthorities;
    }

    private String normalizeRole(String keycloakRole) {
        if (keycloakRole == null) {
            return "";
        }
        String normalized = keycloakRole.trim().toUpperCase(Locale.ROOT);
        if (normalized.isBlank()) {
            return "";
        }
        return normalized.startsWith(ROLE_PREFIX) ? normalized : ROLE_PREFIX + normalized;
    }
}
