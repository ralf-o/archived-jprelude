package org.jprelude.experimental.datasource.demo;

import java.util.Map;
import java.util.Objects;
import org.jprelude.core.util.Seq;
import org.jprelude.experimental.dataexport.base.DataProvider;
import org.jprelude.experimental.dataexport.base.DataSource;

public class ProductDataSource implements DataSource<Product, TenantLanguageKey> {
    private final TenantLanguageKey key;
    private final DataProvider<Map<Long, Category>, TenantLanguageKey> catMapProvider;
    
    public ProductDataSource(
            final TenantLanguageKey key,
            final DataProvider<Map<Long, Category>, TenantLanguageKey> catMapProvider) {
        
        Objects.requireNonNull(key);
        Objects.requireNonNull(catMapProvider);
        this.key = key;
        this.catMapProvider = catMapProvider;
    }
    
    @Override
    public TenantLanguageKey getKey() {
        return this.key;
    }
    
    @Override
    public Seq<Product> get() {
        Objects.requireNonNull(key);
        
        return Seq.from(() -> {
            final Seq<?> productsFromDb = Seq.empty();

            Map<Long, Category> catMap = this.catMapProvider.provide(this.key);
            
            return productsFromDb.map(dbProd -> {
                final Product prod = new Product();

                return prod;
            });
        });
    }
}
