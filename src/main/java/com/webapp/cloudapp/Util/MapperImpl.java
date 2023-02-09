package com.webapp.cloudapp.Util;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import com.webapp.cloudapp.Entity.User;
import com.webapp.cloudapp.Entity.UserDTO;
import com.webapp.cloudapp.Entity.Product;
import com.webapp.cloudapp.Entity.ProductDto;

@Component
@Primary
public class MapperImpl implements MapperClass {

    @Override
    public UserDTO userToUserDTO(User user) {
        if (user == null) {
            return null;
        }

        UserDTO userDto = new UserDTO();

        userDto.setId(user.getId());
        userDto.setFirstName(user.getFirstName());
        userDto.setLastName(user.getLastName());
        userDto.setUsername(user.getUsername());
        userDto.setAccountCreatedTime(user.getAccountCreatedTime());
        userDto.setAccountUpdatedTime(user.getAccountUpdatedTime());

        return userDto;

    }

    @Override
    public ProductDto productToProductDto(Product product) {
        if (product == null) {
            return null;
        }
        ProductDto productDto = new ProductDto(product.getId(), product.getName(), product.getDescription(),
                product.getSku(), product.getManufacturer(), product.getQuantity(), product.getDate_added(),
                product.getDate_last_updated(), product.getUser().getId());

        return productDto;

    }

}