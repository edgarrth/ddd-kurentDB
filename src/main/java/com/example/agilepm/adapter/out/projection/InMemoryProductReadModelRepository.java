package com.example.agilepm.adapter.out.projection;

import com.example.agilepm.application.port.out.ProductReadModelRepository;
import com.example.agilepm.application.service.ProductView;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryProductReadModelRepository implements ProductReadModelRepository {
    private final ConcurrentHashMap<String, ProductView> views = new ConcurrentHashMap<>();
    @Override public void upsert(ProductView view) { views.put(key(view.tenantId(), view.productId()), view); }
    @Override public Optional<ProductView> findById(String tenantId, String productId) { return Optional.ofNullable(views.get(key(tenantId, productId))); }
    private static String key(String tenantId, String productId) { return tenantId + ":" + productId; }
}
