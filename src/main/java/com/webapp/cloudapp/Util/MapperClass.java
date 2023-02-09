package com.webapp.cloudapp.Util;

import com.webapp.cloudapp.Entity.User;
import com.webapp.cloudapp.Entity.UserDTO;
import com.webapp.cloudapp.Entity.Product;
import com.webapp.cloudapp.Entity.ProductDto;


public interface MapperClass {    
    UserDTO userToUserDTO(User user);

    ProductDto productToProductDto(Product product);
}
