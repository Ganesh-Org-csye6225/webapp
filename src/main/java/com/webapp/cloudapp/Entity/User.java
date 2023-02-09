package com.webapp.cloudapp.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.List;

@Entity
@Table(name = "user", schema = "public")
public class User {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private int id;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "password")
    private String password;

    @Column(name = "username", unique = true)
    private String username;

    @Column(name = "account_created")
    private String accountCreatedTime;

    @Column(name = "account_updated")
    private String accountUpdatedTime;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAccountCreatedTime() {
        return accountCreatedTime;
    }

    public void setAccountCreatedTime(String accountCreatedTime) {
        this.accountCreatedTime = accountCreatedTime;
    }

    public String getAccountUpdatedTime() {
        return accountUpdatedTime;
    }

    public void setAccountUpdatedTime(String accountUpdatedTime) {
        this.accountUpdatedTime = accountUpdatedTime;
    }
    
}
