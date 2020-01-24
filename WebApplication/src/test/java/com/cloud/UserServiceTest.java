package com.cloud;

import com.cloud.dao.UserDao;
import com.cloud.entity.User;
import com.cloud.service.UserService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.UUID;

@RunWith(SpringRunner.class)
public class UserServiceTest {

	@InjectMocks
	private UserService userService;

	@Mock
	private UserDao userdao;

	private static User user;

	@Before
	public void setUp() {
		this.user = new User(UUID.randomUUID(),"Dhaval","Suthar","suthadhaval@gmail.com","Dhaval$12345",new Date(),new Date());
	}

	@Test
	public void userSignUpTest(){
		userService.saveUser(user);
		Mockito.verify(userdao).save(user);
	}
}
