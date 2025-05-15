package com.example.kinntai.batch.config;

import static com.example.kinntai.entity.UserRole.*;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.kinntai.entity.User;
import com.example.kinntai.entity.UserRole;
import com.example.kinntai.repository.UserRepository;

@Configuration
public class BatchConfig {

	@Bean
	CommandLineRunner initAdmin(UserRepository repository) {
		return args ->{
			if(repository.findByEmail("gribeiku@gmail.com").isEmpty()) {
				User admin = new User();
		
				repository.findByRole(UserRole.ADMIN);
				
				admin.setEmail("gribeiku@gmail.com");
				admin.setUsername("Test_Admin");
				admin.setPassword("1234");
				admin.setRole(ADMIN);
				repository.save(admin);
			}
		};
		
	}
}
