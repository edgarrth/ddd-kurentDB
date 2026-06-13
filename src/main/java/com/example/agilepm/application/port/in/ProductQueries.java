package com.example.agilepm.application.port.in;

import com.example.agilepm.application.service.ProductView;
import java.util.Optional;

public interface ProductQueries {
    Optional<ProductView> productOf(String tenantId, String productId);
}
