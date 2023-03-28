package com.webapp.cloudapp.Controllers;


import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.NativeWebRequest;

import com.timgroup.statsd.StatsDClient;
import com.webapp.cloudapp.Entity.User;
import com.webapp.cloudapp.Services.UserService;
import com.webapp.cloudapp.Util.BasicAccessAuthenticationHandler;
import com.webapp.cloudapp.Util.MapperClass;
import com.webapp.cloudapp.Util.Util;


@RestController
public class UserController {
    @Autowired 
    UserService userService;
    
    @Autowired
    private StatsDClient statsDClient;

    private final MapperClass mapperClass;

    Logger logger = LoggerFactory.getLogger(UserController.class);

    public UserController(MapperClass mapperClass){
        this.mapperClass = mapperClass;
    }

    @Autowired
    BasicAccessAuthenticationHandler authHandler;

    @GetMapping("healthz")
    public ResponseEntity<?> healthCheck(){
        return new ResponseEntity<>(HttpStatusCode.valueOf(200));
    }    

    @GetMapping("/v1/user/{userId}")
    public ResponseEntity<?> getUser(@PathVariable String userId, NativeWebRequest nativeWebRequest ){
    Integer userid;
    statsDClient.incrementCounter("get.userRequest.count");
    logger.info("UserController: Fetching user data...");
    try{
        userid = Integer.parseInt(userId);
    }catch(NumberFormatException ex){
        logger.error("UserController: Invalid userId format", ex.getLocalizedMessage());
        return new ResponseEntity<>( HttpStatusCode.valueOf(400));
    }
    ResponseEntity<?> auth = AuthenticateUser(userid, nativeWebRequest);
    if(auth.getBody() == null){
        logger.error("UserController: User Authentication failed");
        return auth; 
    } 
    Optional <User> optional = userService.getUser(userid);
    if(!optional.isPresent()){
        logger.error("UserController: User not found for the given userID");
        return new ResponseEntity<>( HttpStatusCode.valueOf(400));
    }
    logger.info("UserController: Successfully fetched user data");
    return new ResponseEntity<>(mapperClass.userToUserDTO(optional.get()), HttpStatusCode.valueOf(200));
    }

    @PostMapping("/v1/user")
    public ResponseEntity<?> addUser(@RequestBody Map<String,String> user){
        statsDClient.incrementCounter("post.userRequest.count");
        logger.info("UserController: Adding new user");
        LocalDateTime now = LocalDateTime.now();  
        if(user.size() ==0){
            logger.error("UserController: Empty user body");
            return new ResponseEntity<>( HttpStatusCode.valueOf(400));
        }
        User us = new User();
        if(!Util.isNullOrEmpty(user.get("first_name")) || !Util.isNullOrEmpty(user.get("last_name")) || !Util.isNullOrEmpty(user.get("password")) || !Util.isNullOrEmpty(user.get("username")) ){
            logger.error("UserController: Missing or Empty user details");
            return new ResponseEntity<>(HttpStatusCode.valueOf(400));
        }

        String regexPattern = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        if(!Pattern.compile(regexPattern).matcher(user.get("username")).matches()){
            logger.error("UserController: Has invalid characters in the username");
            return new ResponseEntity<>(HttpStatusCode.valueOf(400));
        }


        us.setFirstName(user.get("first_name"));
        us.setLastName(user.get("last_name"));
        us.setUsername(user.get("username"));
        us.setPassword(Util.hashPassword(user.get("password")));
        us.setAccountCreatedTime(now.toString());
        us.setAccountUpdatedTime(now.toString());
    
        try {
            User dbUser = userService.addUser(us);
            logger.info("UserController: Successfully created a new user");
            return new ResponseEntity<>(mapperClass.userToUserDTO(dbUser), HttpStatusCode.valueOf(201));
        } catch (DataIntegrityViolationException e) {
            logger.error("UserController: Error saving the user to DB", e.getLocalizedMessage());
            return new ResponseEntity<>( HttpStatusCode.valueOf(400));
        } catch (Exception ex){
            logger.error("UserController: Error while creating a new user",ex.getLocalizedMessage());
            return new ResponseEntity<>( HttpStatusCode.valueOf(400));
        }
    }


    @PutMapping("v1/user/{userId}")
    public ResponseEntity<?> updateUser(@PathVariable String userId, @RequestBody Map<String,String> user, NativeWebRequest nativeWebRequest ){
        Integer userid;
        statsDClient.incrementCounter("put.userRequest.count");
        logger.info("UserController: Editing an existing user");
        try{
            userid = Integer.parseInt(userId);
        }catch(NumberFormatException ex){
            logger.error("UserController: Invalid userId format", ex.getLocalizedMessage());
            return new ResponseEntity<>(HttpStatusCode.valueOf(400));
        }
        ResponseEntity<?> auth = AuthenticateUser(userid, nativeWebRequest);
        if(auth.getBody() == null){
            logger.error("UserController: User Authentication failed");
            return auth; 
        } 
        Optional<User> optional = userService.getUser(userid);
        if(!optional.isPresent()){
            return new ResponseEntity<>( HttpStatusCode.valueOf(400));
        }

        if(user.containsKey("username")){
            logger.error("UserController: Cannot edit the username");
            return new ResponseEntity<>( HttpStatusCode.valueOf(400));
        }

        if(user.containsKey("first_name")){
            optional.get().setFirstName(user.get("first_name"));
        }

        if(user.containsKey("last_name")){
            optional.get().setLastName(user.get("last_name"));
        }
        if(user.containsKey("password")){
            optional.get().setPassword(Util.hashPassword(user.get("password")));
        }
        LocalDateTime now = LocalDateTime.now();  
        optional.get().setAccountUpdatedTime(now.toString());
        try {
            userService.addUser(optional.get());
            logger.info("UserController: Successfully updated user details");
            return new ResponseEntity<>( HttpStatusCode.valueOf(204));
        } catch (DataIntegrityViolationException e) {
            logger.error("UserController: Error saving updated user details in DB");
            return new ResponseEntity<>( HttpStatusCode.valueOf(400));
        }
    }

    private ResponseEntity<?> AuthenticateUser(Integer userid, NativeWebRequest nativeWebRequest ){
        return authHandler.getUser(userid, nativeWebRequest);
    }

}
