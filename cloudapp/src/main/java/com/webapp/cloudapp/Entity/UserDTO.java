package com.webapp.cloudapp.Entity;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UserDTO {

    @JsonProperty("id")
    private Integer id;

    @JsonProperty("firstName")
    private String firstName;

    @JsonProperty("lastName")
    private String lastName;

    @JsonProperty("username")
    private String username;

    @JsonProperty("accountCreatedTime")
    private String accountCreatedTime;

    @JsonProperty("accountUpdatedTime")
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
