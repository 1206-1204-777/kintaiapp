package com.example.kinntai.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.kinntai.entity.AttendanceCorrectionRequest;
import com.example.kinntai.entity.RequestStatus;

@Repository
public interface AttendanceCorrectionRquestsRepository
		extends JpaRepository<AttendanceCorrectionRequest, Long> {

	/**
	 * 承認待ちユーザ－の一覧を表示するために
	 * ユーザーの申請ステータスをリストで返す
	 * @param status 検索するステータス
	 * @return 見つかったステータスのリスト*/
	List<AttendanceCorrectionRequest> findByStatus(RequestStatus status);

	/**
	 * 申請者をユーザーIDで検索する
	 * @param username 検索するユーザー名
	 * @return 検索したユーザー名のリスト*/
	List<AttendanceCorrectionRequest> findByUserId(Long username);
}
