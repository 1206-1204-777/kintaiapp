package com.example.kinntai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class KintaiAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(KintaiAppApplication.class, args);
		
		
	}

}

