package com.webapp.cloudapp.Services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.webapp.cloudapp.Entity.Product;
import com.webapp.cloudapp.Repository.ProductRepository;

@Service
public class ProductService {
    @Autowired ProductRepository productRepository;

    public ProductService(){
    }

    public List<Product> getProducts(){
        return productRepository.findAll();
    }

    public Optional<Product> getProduct(Integer id){
        return productRepository.findById(id);
    }

    public Product addProduct(Product product) throws DataIntegrityViolationException{
        return productRepository.save(product);   
    }

    public void deleteProduct(Product product){
        productRepository.delete(product);
    }
}
