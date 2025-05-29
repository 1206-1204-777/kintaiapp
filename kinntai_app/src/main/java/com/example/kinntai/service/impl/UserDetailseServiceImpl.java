package com.example.kinntai.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.kinntai.entity.User;
import com.example.kinntai.repository.UserRepository;

@Service
public class UserDetailseServiceImpl implements UserDetailsService {

	@Autowired
	private UserRepository userRepository;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		User user = userRepository.findByUsername(username)
				.orElseThrow(() -> new UsernameNotFoundException(username));

		// ロール名を "ROLE_USER" の形式にする
		String role = "ROLE_" + user.getRole().name();

		return new org.springframework.security.core.userdetails.User(
				user.getUsername(),
				user.getPassword(),
				List.of(new SimpleGrantedAuthority(role)));
	}
}
