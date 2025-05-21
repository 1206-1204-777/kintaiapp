package com.example.kinntai.service;

import com.example.kinntai.entity.Break;

public interface BreakService {
	public Break startBreak(Long userId);
	public Break endBreak(Long userId);
	public Long getTotalBreakMinutesForToday(Long userId);
	public boolean isOngoingBreakToday(Long userId);
}
