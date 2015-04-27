package org.jprelude.experimental.datasource.demo;

import java.util.Objects;

public class TenantLanguageKey {
    final int tenantId;
    final int languageId;
    
    public TenantLanguageKey(final int tenantId, final int languageId) {
        this.tenantId = tenantId;
        this.languageId = languageId;
    }
    
    public long getTenantId() {
        return this.tenantId;
    }
    
    public long getLangaugeId() {
        return this.languageId;
    }
    
    @Override
    public boolean equals(final Object other) {
        Objects.requireNonNull(other);
        
        final boolean ret;
        
        if (other == null || !other.getClass().equals(this.getClass())) {
            ret = false;
        } else {
            final TenantLanguageKey otherKey = (TenantLanguageKey) other;
            
            ret = otherKey.tenantId == this.tenantId
                    && otherKey.languageId == this.languageId;
        }
        
        return ret;
    }
    
    @Override
    public int hashCode() {
        return (this.languageId << 16 + this.tenantId);
    }
}
