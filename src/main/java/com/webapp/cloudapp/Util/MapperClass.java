package com.webapp.cloudapp.Util;

import com.webapp.cloudapp.Entity.User;
import com.webapp.cloudapp.Entity.UserDTO;

public interface MapperClass {    
    UserDTO userToUserDTO(User user);
}
