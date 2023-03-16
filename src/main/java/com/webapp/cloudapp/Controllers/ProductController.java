package com.webapp.cloudapp.Controllers;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.List;

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

    @GetMapping("/v1/product/{productId}")
    public ResponseEntity<?> getProduct(@PathVariable String productId, NativeWebRequest nativeWebRequest) {
        Integer pId;
        try {
            pId = Integer.parseInt(productId);
        } catch (NumberFormatException ex) {
            return new ResponseEntity<>(HttpStatusCode.valueOf(400));
        }
        Optional<Product> optional = productService.getProduct(pId);
        if (!optional.isPresent()) {
            return new ResponseEntity<>(HttpStatusCode.valueOf(404));
        }
        return new ResponseEntity<>(mapperClass.productToProductDto(optional.get()), HttpStatusCode.valueOf(200));
    }

    @PostMapping("/v1/product")
    public ResponseEntity<?> addProduct(@RequestBody Map<String, Object> product, NativeWebRequest nativeWebRequest) {
        User user = authHandler.getUser(nativeWebRequest);
        if (user == null) {
            return new ResponseEntity<>(HttpStatusCode.valueOf(401));
        }
        LocalDateTime now = LocalDateTime.now();
        if (product.size() == 0) {
            return new ResponseEntity<>(HttpStatusCode.valueOf(400));
        }
        try {
            if (!Util.isNullOrEmpty(product.get("name").toString())
                    || !Util.isNullOrEmpty(product.get("description").toString())
                    || !Util.isNullOrEmpty(product.get("manufacturer").toString())
                    || !Util.isNullOrEmpty(product.get("sku").toString())
                    || !Util.productQuantityCheck((int) product.get("quantity"))) {
                return new ResponseEntity<>(HttpStatusCode.valueOf(400));
            }
            int quantity = (int) (product.get("quantity"));
            Product p = new Product(product.get("name").toString(), product.get("description").toString(),
                    product.get("sku").toString(),
                    product.get("manufacturer").toString(), quantity, now.toString(), now.toString(), user);

            Product dbProduct = productService.addProduct(p);
            return new ResponseEntity<>(mapperClass.productToProductDto(dbProduct), HttpStatusCode.valueOf(201));
        } catch (DataIntegrityViolationException e) {
            return new ResponseEntity<>(HttpStatusCode.valueOf(400));
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatusCode.valueOf(400));
        }
    }

    @PutMapping("v1/product/{productId}")
    public ResponseEntity<?> updateProduct(@PathVariable String productId, @RequestBody Map<String, Object> product,
            NativeWebRequest nativeWebRequest) {
        Integer productid;
        try {
            productid = Integer.parseInt(productId);
        } catch (NumberFormatException ex) {
            return new ResponseEntity<>(HttpStatusCode.valueOf(400));
        }
        User user = authHandler.getUser(nativeWebRequest);
        if (user == null) {
            return new ResponseEntity<>(HttpStatusCode.valueOf(401));
        }
        Optional<Product> dbProduct = productService.getProduct(productid);
        if (!dbProduct.isPresent()) {
            return new ResponseEntity<>(HttpStatusCode.valueOf(404));
        }
        if (dbProduct.get().getUser().getId() != user.getId()) {
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
                return new ResponseEntity<>(HttpStatusCode.valueOf(400));
            } else if (product.containsKey("quantity")) {
                dbProduct.get().setQuantity((int) (product.get("quantity")));
            } else if (!product.containsKey("quantity")) {
                check = true;
            }

            if (check) {
                return new ResponseEntity<>(HttpStatusCode.valueOf(400));
            }

            LocalDateTime now = LocalDateTime.now();
            dbProduct.get().setDate_last_updated(now.toString());

            productService.addProduct(dbProduct.get());
            return new ResponseEntity<>(HttpStatusCode.valueOf(204));
        } catch (DataIntegrityViolationException e) {
            return new ResponseEntity<>(HttpStatusCode.valueOf(400));
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatusCode.valueOf(400));
        }
    }

    @PatchMapping("v1/product/{productId}")
    public ResponseEntity<?> editProduct(@PathVariable String productId, @RequestBody Map<String, Object> product,
            NativeWebRequest nativeWebRequest) {
        Integer productid;
        try {
            productid = Integer.parseInt(productId);
        } catch (NumberFormatException ex) {
            return new ResponseEntity<>(HttpStatusCode.valueOf(400));
        }
        User user = authHandler.getUser(nativeWebRequest);
        if (user == null) {
            return new ResponseEntity<>(HttpStatusCode.valueOf(401));
        }
        Optional<Product> dbProduct = productService.getProduct(productid);
        if (!dbProduct.isPresent()) {
            return new ResponseEntity<>(HttpStatusCode.valueOf(404));
        }
        if (dbProduct.get().getUser().getId() != user.getId()) {
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
                return new ResponseEntity<>(HttpStatusCode.valueOf(400));
            } else if (product.containsKey("quantity")) {
                dbProduct.get().setQuantity(((int) product.get("quantity")));
            }

            LocalDateTime now = LocalDateTime.now();
            dbProduct.get().setDate_last_updated(now.toString());

            productService.addProduct(dbProduct.get());
            return new ResponseEntity<>(HttpStatusCode.valueOf(204));
        } catch (DataIntegrityViolationException e) {
            return new ResponseEntity<>(HttpStatusCode.valueOf(400));
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatusCode.valueOf(400));
        }
    }

    @DeleteMapping("v1/product/{productId}")
    public ResponseEntity<?> deleteProduct(@PathVariable String productId, NativeWebRequest nativeWebRequest) {
        Integer productid;
        try {
            productid = Integer.parseInt(productId);
        } catch (NumberFormatException ex) {
            return new ResponseEntity<>(HttpStatusCode.valueOf(400));
        }
        User user = authHandler.getUser(nativeWebRequest);
        if (user == null) {
            return new ResponseEntity<>(HttpStatusCode.valueOf(401));
        }
        Optional<Product> dbProduct = productService.getProduct(productid);
        if (!dbProduct.isPresent()) {
            return new ResponseEntity<>(HttpStatusCode.valueOf(404));
        }
        if (dbProduct.get().getUser().getId() != user.getId()) {
            return new ResponseEntity<>(HttpStatusCode.valueOf(403));
        }

        try {
            List<Image> images = imageRepository.findAllByProductId(Integer.parseInt(productId));
            for(Image img : images){
                imageService.deleteImage(String.valueOf(img.getId()) , productId, nativeWebRequest);
            }
            productService.deleteProduct(dbProduct.get());
            return new ResponseEntity<>(HttpStatusCode.valueOf(204));
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatusCode.valueOf(400));
        }
    }

}
