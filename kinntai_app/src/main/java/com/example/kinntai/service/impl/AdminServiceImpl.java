package com.example.kinntai.service.impl;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.kinntai.dto.AdminRegister;
import com.example.kinntai.entity.User;
import com.example.kinntai.repository.UserRepository;
import com.example.kinntai.service.AdminService;

@Service
public class AdminServiceImpl implements AdminService {

	@Autowired
	private UserRepository repository;

	@Autowired
	private PasswordEncoder encorder;

	@Autowired
	private final ModelMapper modelMapper;

	// コンストラクタ
	public AdminServiceImpl(ModelMapper modelMapper) {
		this.modelMapper = modelMapper;
	}

	@Override
	public void registerAdmin(AdminRegister dto) {

		User user = modelMapper.map(dto, User.class);

		repository.save(user);

	}

}
