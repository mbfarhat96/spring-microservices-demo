package com.farhat.product_service.repository;

import com.farhat.product_service.model.Product;
import org.springframework.data.mongodb.repository.*;

public interface ProductRepository extends MongoRepository<Product, String> {
}
