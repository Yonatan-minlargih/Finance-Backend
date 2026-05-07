package com.finance.transactional.model;

import com.finance.transactional.config.SecurityUtil;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class BaseEntityListener {

    private final SecurityUtil securityUtil;

    @PrePersist
    public void setCreatedBy(BaseTenantEntity base) {
        String name = securityUtil.getName();
        base.setCreatedBy(name != null ? name : "unknown");
    }

    @PreUpdate
    public void setUpdatedBy(BaseTenantEntity base) {
        String name = securityUtil.getName();
        base.setUpdatedBy(name != null ? name : "unknown");
    }
}
