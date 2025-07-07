package com.example.kinntai.config;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppCofig {
	@Bean
	public ModelMapper modelMapper() {
		return new ModelMapper();
	}
}