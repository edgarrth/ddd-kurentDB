package com.example.agilepm.application.port.out;

import com.example.agilepm.application.service.ProductView;
import java.util.Optional;

public interface ProductReadModelRepository {
    void upsert(ProductView view);
    Optional<ProductView> findById(String tenantId, String productId);
}
