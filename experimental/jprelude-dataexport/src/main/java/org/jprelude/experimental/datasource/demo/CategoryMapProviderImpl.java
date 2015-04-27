package org.jprelude.experimental.datasource.demo;

import java.util.Map;
import org.jprelude.experimental.dataexport.base.DataProvider;
import org.jprelude.experimental.datasource.demo.Category;
import org.jprelude.experimental.datasource.demo.TenantLanguageKey;

public class CategoryMapProviderImpl implements DataProvider<Map<Long, Category>, TenantLanguageKey> {
    @Override
    public Map<Long, Category> provide(final TenantLanguageKey key) {
        return null; // TODO - rerived Data and return.
    }
}
