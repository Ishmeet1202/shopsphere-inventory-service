package com.shopsphere.inventory.tenant.context;

import com.shopsphere.inventory.exception.MissingTenantContextException;
import org.springframework.util.StringUtils;

public final class TenantContext {
    private static final ThreadLocal<String> CURRENT_TENANT = new ThreadLocal<>();

    public static void setCurrentTenant(String tenantId) {
        CURRENT_TENANT.set(tenantId);
    }

    private static String getCurrentTenant() {
        return CURRENT_TENANT.get();
    }

    public static String requireTenantId() {
        String tenantId = getCurrentTenant();

        if (!StringUtils.hasText(tenantId)) {
            throw new MissingTenantContextException("Tenant context is missing.");
        }

        return tenantId;
    }

    public static void clear() {
        CURRENT_TENANT.remove();
    }
}
