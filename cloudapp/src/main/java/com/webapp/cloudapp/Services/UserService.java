package com.webapp.cloudapp.Services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.webapp.cloudapp.Entity.User;
import com.webapp.cloudapp.Repository.UserRepository;


@Service
public class UserService {
    @Autowired UserRepository userRepository;

    public UserService(){
    }

    public List<User> getUsers(){
        return userRepository.findAll();
    }

    public Optional<User> getUser(Integer id){
        return userRepository.findById(id);
    }

    public User addUser(User user) throws DataIntegrityViolationException{
        return userRepository.save(user);   
    }

}
