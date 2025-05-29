package com.example.kinntai.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.kinntai.repository.AttendanceRepository;
import com.example.kinntai.repository.UserRepository;

import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class CorrectionRequestServiceImpl {
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private AttendanceRepository attendanceRepository;
	
	
	
}
