package com.example.kinntai.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.kinntai.dto.AdminRegister;
import com.example.kinntai.entity.User;
import com.example.kinntai.entity.UserRole;
import com.example.kinntai.repository.UserRepository;
import com.example.kinntai.service.AdminService;

@Service
public class AdminServiceImpl implements AdminService {

	@Autowired
	private UserRepository repository;

	@Autowired
	private PasswordEncoder encorder;

	@Override
	public void registerAdmin(AdminRegister dto) {
		User user = new User();

		user.setUsername(dto.getUsername());
		user.setEmail(dto.getEmail());
		user.setRole(UserRole.ADMIN);
		user.setPassword(encorder.encode(dto.getPassword()));

		repository.save(user);

	}

}
