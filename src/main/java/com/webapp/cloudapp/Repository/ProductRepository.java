package com.webapp.cloudapp.Repository;


import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.webapp.cloudapp.Entity.Product;


public interface  ProductRepository extends JpaRepository<Product,Integer> {
    
}
