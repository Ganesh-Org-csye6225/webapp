package com.webapp.cloudapp.Controllers;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.NativeWebRequest;

import com.webapp.cloudapp.Entity.Product;
import com.timgroup.statsd.StatsDClient;
import com.webapp.cloudapp.Entity.Image;
import com.webapp.cloudapp.Entity.User;
import com.webapp.cloudapp.Repository.ImageRepository;
import com.webapp.cloudapp.Services.ProductService;
import com.webapp.cloudapp.Services.ImageService;
import com.webapp.cloudapp.Util.BasicAccessAuthenticationHandler;
import com.webapp.cloudapp.Util.Util;
import com.webapp.cloudapp.Util.MapperClass;

@RestController
public class ProductController {
    @Autowired
    ProductService productService;

    @Autowired
    BasicAccessAuthenticationHandler authHandler;

	@Autowired
	ImageRepository imageRepository;

	@Autowired
	ImageService imageService;

    @Autowired
    MapperClass mapperClass;

    @Autowired
    private StatsDClient statsDClient;

    Logger logger = LoggerFactory.getLogger(ProductController.class);

    @GetMapping("/v1/product/{productId}")
    public ResponseEntity<?> getProduct(@PathVariable String productId, NativeWebRequest nativeWebRequest) {
        Integer pId;
        statsDClient.incrementCounter("get.productRequest.count");
        logger.info("ProductController: Fetching product data...");
        try {
            pId = Integer.parseInt(productId);
        } catch (NumberFormatException ex) {
            logger.error("ProductController: Invalid productId format", ex.getLocalizedMessage());
            return new ResponseEntity<>(HttpStatusCode.valueOf(400));
        }
        Optional<Product> optional = productService.getProduct(pId);
        if (!optional.isPresent()) {
            logger.error("ProductController: Product not found for the given productID");
            return new ResponseEntity<>(HttpStatusCode.valueOf(404));
        }
        return new ResponseEntity<>(mapperClass.productToProductDto(optional.get()), HttpStatusCode.valueOf(200));
    }

    @PostMapping("/v1/product")
    public ResponseEntity<?> addProduct(@RequestBody Map<String, Object> product, NativeWebRequest nativeWebRequest) {
        statsDClient.incrementCounter("post.productRequest.count");
        
        User user = authHandler.getUser(nativeWebRequest);
        if (user == null) {
            logger.error("ProductController: User Authentication failed");
            return new ResponseEntity<>(HttpStatusCode.valueOf(401));
        }
        logger.info("ProductController: Adding new product");
        LocalDateTime now = LocalDateTime.now();
        if (product.size() == 0) {
            logger.error("ProductController: Missing product details");
            return new ResponseEntity<>(HttpStatusCode.valueOf(400));
        }
        try {
            if (!Util.isNullOrEmpty(product.get("name").toString())
                    || !Util.isNullOrEmpty(product.get("description").toString())
                    || !Util.isNullOrEmpty(product.get("manufacturer").toString())
                    || !Util.isNullOrEmpty(product.get("sku").toString())
                    || !Util.productQuantityCheck((int) product.get("quantity"))) {
                logger.error("ProductController: Missing or Empty product details");
                return new ResponseEntity<>(HttpStatusCode.valueOf(400));
            }
            int quantity = (int) (product.get("quantity"));
            Product p = new Product(product.get("name").toString(), product.get("description").toString(),
                    product.get("sku").toString(),
                    product.get("manufacturer").toString(), quantity, now.toString(), now.toString(), user);

            Product dbProduct = productService.addProduct(p);
            logger.info("ProductController: Successfully created a new product");
            return new ResponseEntity<>(mapperClass.productToProductDto(dbProduct), HttpStatusCode.valueOf(201));
        } catch (DataIntegrityViolationException e) {
            logger.error("ProductController: Error saving the product to DB", e.getLocalizedMessage());
            return new ResponseEntity<>(HttpStatusCode.valueOf(400));
        } catch (Exception e) {
            logger.error("ProductController: Error while creating a new product",e.getLocalizedMessage());
            return new ResponseEntity<>(HttpStatusCode.valueOf(400));
        }
    }

    @PutMapping("v1/product/{productId}")
    public ResponseEntity<?> updateProduct(@PathVariable String productId, @RequestBody Map<String, Object> product,
            NativeWebRequest nativeWebRequest) {
        statsDClient.incrementCounter("put.productRequest.count");
        Integer productid;
        User user = authHandler.getUser(nativeWebRequest);
        if (user == null) {
            logger.error("ProductController: User Authentication failed");
            return new ResponseEntity<>(HttpStatusCode.valueOf(401));
        }
        logger.info("ProductController: Updating an existing product");
        try {
            productid = Integer.parseInt(productId);
        } catch (NumberFormatException ex) {
            logger.error("ProductController: Invalid productId format", ex.getLocalizedMessage());
            return new ResponseEntity<>(HttpStatusCode.valueOf(400));
        }
        
        Optional<Product> dbProduct = productService.getProduct(productid);
        if (!dbProduct.isPresent()) {
            logger.error("ProductController: Product not found for the given productID");
            return new ResponseEntity<>(HttpStatusCode.valueOf(404));
        }
        if (dbProduct.get().getUser().getId() != user.getId()) {
            logger.error("ProductController: User does not have access for the given productID");
            return new ResponseEntity<>(HttpStatusCode.valueOf(403));
        }

        try {
            boolean check = false;
            if (product.containsKey("name") && Util.isNullOrEmpty((String) product.get("name"))) {
                dbProduct.get().setName(product.get("name").toString());
            } else {
                check = true;
            }
            if (product.containsKey("description") && Util.isNullOrEmpty((String) product.get("description"))) {
                dbProduct.get().setDescription(product.get("description").toString());
            } else {
                check = true;
            }
            if (product.containsKey("sku") && Util.isNullOrEmpty((String) product.get("sku"))) {
                dbProduct.get().setSku(product.get("sku").toString());
            } else {
                check = true;
            }
            if (product.containsKey("manufacturer") && Util.isNullOrEmpty((String) product.get("manufacturer"))) {
                dbProduct.get().setManufacturer(product.get("manufacturer").toString());
            } else {
                check = true;
            }
            if (product.containsKey("quantity") && !Util.productQuantityCheck((int) product.get("quantity"))) {
                logger.error("ProductController: Missing or Invalid format for product quantity");
                return new ResponseEntity<>(HttpStatusCode.valueOf(400));
            } else if (product.containsKey("quantity")) {
                dbProduct.get().setQuantity((int) (product.get("quantity")));
            } else if (!product.containsKey("quantity")) {
                check = true;
            }

            if (check) {
                logger.error("ProductController: Missing product details");
                return new ResponseEntity<>(HttpStatusCode.valueOf(400));
            }

            LocalDateTime now = LocalDateTime.now();
            dbProduct.get().setDate_last_updated(now.toString());

            productService.addProduct(dbProduct.get());
            logger.info("ProductController: Successfully updated product details");
            return new ResponseEntity<>(HttpStatusCode.valueOf(204));
        } catch (DataIntegrityViolationException e) {
            logger.error("ProductController: Error saving updated product details in DB");
            return new ResponseEntity<>(HttpStatusCode.valueOf(400));
        } catch (Exception e) {
            logger.error("ProductController: Error updating product details");
            return new ResponseEntity<>(HttpStatusCode.valueOf(400));
        }
    }

    @PatchMapping("v1/product/{productId}")
    public ResponseEntity<?> editProduct(@PathVariable String productId, @RequestBody Map<String, Object> product,
            NativeWebRequest nativeWebRequest) {
        Integer productid;
        statsDClient.incrementCounter("patch.productRequest.count");
        User user = authHandler.getUser(nativeWebRequest);
        if (user == null) {
            logger.error("ProductController: User Authentication failed");
            return new ResponseEntity<>(HttpStatusCode.valueOf(401));
        }
        logger.info("ProductController: Updating an existing product");
        try {
            productid = Integer.parseInt(productId);
        } catch (NumberFormatException ex) {
            logger.error("ProductController: Invalid productId format", ex.getLocalizedMessage());
            return new ResponseEntity<>(HttpStatusCode.valueOf(400));
        }
       
        Optional<Product> dbProduct = productService.getProduct(productid);
        if (!dbProduct.isPresent()) {
            logger.error("ProductController: Product not found for the given productID");
            return new ResponseEntity<>(HttpStatusCode.valueOf(404));
        }
        if (dbProduct.get().getUser().getId() != user.getId()) {
            logger.error("ProductController: User does not have access for the given productID");
            return new ResponseEntity<>(HttpStatusCode.valueOf(403));
        }
        try {

            if (product.containsKey("name") && Util.isNullOrEmpty((String) product.get("name"))) {
                dbProduct.get().setName(product.get("name").toString());
            }
            if (product.containsKey("description") && Util.isNullOrEmpty((String) product.get("description"))) {
                dbProduct.get().setDescription(product.get("description").toString());
            }
            if (product.containsKey("sku") && Util.isNullOrEmpty((String) product.get("sku"))) {
                dbProduct.get().setSku(product.get("sku").toString());
            }
            if (product.containsKey("manufacturer") && Util.isNullOrEmpty((String) product.get("manufacturer"))) {
                dbProduct.get().setManufacturer(product.get("manufacturer").toString());
            }
            if (product.containsKey("quantity") && !Util.productQuantityCheck((int) product.get("quantity"))) {
                logger.error("ProductController: Missing or Invalid format for product quantity");
                return new ResponseEntity<>(HttpStatusCode.valueOf(400));
            } else if (product.containsKey("quantity")) {
                dbProduct.get().setQuantity(((int) product.get("quantity")));
            }

            LocalDateTime now = LocalDateTime.now();
            dbProduct.get().setDate_last_updated(now.toString());

            productService.addProduct(dbProduct.get());
            logger.info("ProductController: Successfully updated product details");
            return new ResponseEntity<>(HttpStatusCode.valueOf(204));
        } catch (DataIntegrityViolationException e) {
            logger.error("ProductController: Error saving updated product details in DB");
            return new ResponseEntity<>(HttpStatusCode.valueOf(400));
        } catch (Exception e) {
            logger.error("ProductController: Error updating product details");
            return new ResponseEntity<>(HttpStatusCode.valueOf(400));
        }
    }

    @DeleteMapping("v1/product/{productId}")
    public ResponseEntity<?> deleteProduct(@PathVariable String productId, NativeWebRequest nativeWebRequest) {
        Integer productid;
        
        statsDClient.incrementCounter("delete.productRequest.count");
        User user = authHandler.getUser(nativeWebRequest);
        if (user == null) {
            logger.error("ProductController: User Authentication failed");
            return new ResponseEntity<>(HttpStatusCode.valueOf(401));
        }
        logger.info("ProductController: Deleting a product");
        try {
            productid = Integer.parseInt(productId);
        } catch (NumberFormatException ex) {
            logger.error("ProductController: Invalid productId format", ex.getLocalizedMessage());
            return new ResponseEntity<>(HttpStatusCode.valueOf(400));
        }
        Optional<Product> dbProduct = productService.getProduct(productid);
        if (!dbProduct.isPresent()) {
            logger.error("ProductController: Product not found for the given productID");
            return new ResponseEntity<>(HttpStatusCode.valueOf(404));
        }
        if (dbProduct.get().getUser().getId() != user.getId()) {
            logger.error("ProductController: User does not have access for the given productID");
            return new ResponseEntity<>(HttpStatusCode.valueOf(403));
        }

        try {
            List<Image> images = imageRepository.findAllByProductId(Integer.parseInt(productId));
            for(Image img : images){
                imageService.deleteImage(String.valueOf(img.getId()) , productId, nativeWebRequest);
            }
            productService.deleteProduct(dbProduct.get());
            logger.info("ProductController: Successfully deleted a product");
            return new ResponseEntity<>(HttpStatusCode.valueOf(204));
        } catch (Exception e) {
            logger.error("ProductController: Error deleting a product");
            return new ResponseEntity<>(HttpStatusCode.valueOf(400));
        }
    }

}
