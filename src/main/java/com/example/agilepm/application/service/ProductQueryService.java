package com.example.agilepm.application.service;

import com.example.agilepm.application.port.in.ProductQueries;
import com.example.agilepm.application.port.out.ProductReadModelRepository;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class ProductQueryService implements ProductQueries {
    private final ProductReadModelRepository repository;
    public ProductQueryService(ProductReadModelRepository repository) { this.repository = repository; }
    public Optional<ProductView> productOf(String tenantId, String productId) { return repository.findById(tenantId, productId); }
}
