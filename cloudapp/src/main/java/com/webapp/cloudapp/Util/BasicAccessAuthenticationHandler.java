package com.webapp.cloudapp.Util;

import java.util.Base64;
import java.util.Optional;

import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.NativeWebRequest;

import com.webapp.cloudapp.Entity.User;
import com.webapp.cloudapp.Repository.UserRepository;

@Component
public class BasicAccessAuthenticationHandler {
    // to retrieve users associated to valid credentials
    private UserRepository userDao;

    public BasicAccessAuthenticationHandler(
            @Autowired UserRepository userDao
    ) {
        this.userDao = userDao;
    }

    public ResponseEntity<?> getUser(Integer userId,
            NativeWebRequest nativeWebRequest
    ) {
        try {
            // retrieving credentials the HTTP Authorization Header
            String authorizationCredentials = nativeWebRequest
                    .getHeader(HttpHeaders.AUTHORIZATION)
                    .substring("Basic".length())
                    .trim();

            // decoding credentials
            String[] decodedCredentials = new String(
                    Base64
                            .getDecoder()
                            .decode(authorizationCredentials)
            ).split(":");
                // user retrieving logic
                Optional<User> userOptional = userDao.findByUsername(decodedCredentials[0]);
                Optional<User> userById = userDao.findById(userId);
                if(userById.get() == null){
                    return new ResponseEntity<>( HttpStatusCode.valueOf(400));
                }
                if(userOptional.get() == null){
                    return new ResponseEntity<>( HttpStatusCode.valueOf(401));
                }
                if(userById.get().getUsername() != userOptional.get().getUsername()){
                    return new ResponseEntity<>( HttpStatusCode.valueOf(403));
                }
                if (!BCrypt.checkpw(decodedCredentials[1], userById.get().getPassword())){
                    return new ResponseEntity<>( HttpStatusCode.valueOf(401));
                }
                return null;
        } catch (Exception e) {
            return new ResponseEntity<>( HttpStatusCode.valueOf(400));
        }
    }
}
