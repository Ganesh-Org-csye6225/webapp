package com.webapp.cloudapp.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.webapp.cloudapp.Entity.User;

@Repository
public interface UserRepository extends JpaRepository<User,Integer> {

    Optional<User> findByUsernameAndPassword(String string, String string2);

    Optional<User> findByUsername(String string);
    
}
