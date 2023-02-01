package com.webapp.cloudapp.Util;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import com.webapp.cloudapp.Entity.User;
import com.webapp.cloudapp.Entity.UserDTO;


@Component
@Primary
public class MapperImpl implements MapperClass {

    @Override
    public UserDTO userToUserDTO(User user) {
        if ( user == null ) {
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
    
}