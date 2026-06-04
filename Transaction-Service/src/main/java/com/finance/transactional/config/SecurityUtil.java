package com.finance.transactional.config;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtil {

    public Jwt getUserJwt() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getPrincipal() instanceof Jwt jwt ? jwt : null;
    }

    public String getUserToken() {
        Jwt jwt = getUserJwt();
        if (jwt == null) {
            return "";
        }
        return String.format("Bearer %s", jwt.getTokenValue());
    }

    public String getUserId() {
        Jwt jwt = getUserJwt();
        return jwt != null ? jwt.getSubject() : null;
    }

    public String getClaim(String claim) {
        Jwt jwt = getUserJwt();
        return jwt != null && jwt.hasClaim(claim) ? jwt.getClaim(claim).toString() : null;
    }

    public String getTenantId() {
        String tenantId = getClaim("tenant_id");
        if (tenantId == null) {
            tenantId = getClaim("tenantId");
        }
        return tenantId != null ? tenantId : "3fa85f64-5717-4562-b3fc-2c963f66afa6";
    }

    public String getName() {
        return getClaim("name");
    }

    public String getUsername() {
        String username = getClaim("preferred_username");
        if (username == null || username.isBlank()) {
            username = getClaim("username");
        }
        return username;
    }

    /** Display name for audit fields (name, then username, then subject). */
    public String getActorDisplayName() {
        String name = getName();
        if (name != null && !name.isBlank()) {
            return name;
        }
        String username = getUsername();
        if (username != null && !username.isBlank()) {
            return username;
        }
        String userId = getUserId();
        return userId != null ? userId : "unknown";
    }
}
