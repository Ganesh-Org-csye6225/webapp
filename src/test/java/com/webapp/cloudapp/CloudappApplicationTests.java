package com.webapp.cloudapp;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.webapp.cloudapp.Entity.User;
import com.webapp.cloudapp.Repository.UserRepository;
import com.webapp.cloudapp.Services.UserService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class CloudappApplicationTests {

	@Test
	void contextLoads() {
	}

	@InjectMocks
	UserService userService;

	@Mock
	UserRepository dao;

	@BeforeEach
	public void init() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	public void getUserByIdTest()
	{
		LocalDateTime now = LocalDateTime.now();
		User us = new User();
		us.setId(1);
		us.setPassword("password");
		us.setUsername("user@gmail.com");
		us.setFirstName("fname");
		us.setLastName("lname");
		us.setAccountCreatedTime(now.toString());
		us.setAccountUpdatedTime(now.toString());

		Optional<User> optUser = Optional.of(us) ;

		when(dao.findById(1)).thenReturn(optUser);

		java.util.Optional<User> user = userService.getUser(1);

		assertTrue(user.isPresent());
		assertEquals("fnam", user.get().getFirstName());
		assertEquals("lname", user.get().getLastName());
		assertEquals("user@gmail.com", user.get().getUsername());
		assertEquals("password", user.get().getPassword());

	}

}
