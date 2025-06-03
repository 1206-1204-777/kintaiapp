package com.example.kinntai.service.impl;

import java.nio.file.AccessDeniedException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.kinntai.dto.CompanyHolidayRequest; // DTOは後で作成します
import com.example.kinntai.entity.CompanyHoliday;
import com.example.kinntai.entity.User;
import com.example.kinntai.entity.UserRole;
import com.example.kinntai.repository.CompanyHolidayRepository;
import com.example.kinntai.repository.UserRepository; // Userエンティティを使うため
import com.example.kinntai.service.CompanyHolidayService;

@Service
public class CompanyHolidayServiceImpl implements CompanyHolidayService {

	@Autowired
	private CompanyHolidayRepository companyHolidayRepository;

	@Autowired
	private UserRepository userRepository; // 登録ユーザーを取得するため

	@Override
	@Transactional
	public CompanyHoliday createCompanyHoliday(CompanyHolidayRequest request) {
		// 登録ユーザーの存在チェック (管理者であることのチェックも追加可能)
		User createdByUser = userRepository.findById(request.getCreatedByUserId())
				.orElseThrow(() -> new IllegalArgumentException("指定された登録ユーザーが見つかりません。"));

		// 日付の重複チェック
		Optional<CompanyHoliday> existingHoliday = companyHolidayRepository.findByHolidayDate(request.getHolidayDate());
		if (existingHoliday.isPresent()) {
			throw new IllegalArgumentException("指定された日付は既に会社休日として登録されています。");
		}

		CompanyHoliday companyHoliday = new CompanyHoliday();
		companyHoliday.setHolidayDate(request.getHolidayDate());
		companyHoliday.setHolidayName(request.getHolidayName());
		companyHoliday.setCreatedByUser(createdByUser);
		companyHoliday.setCreatedAt(LocalDateTime.now());
		companyHoliday.setUpdatedAt(LocalDateTime.now());

		return companyHolidayRepository.save(companyHoliday);
	}

	@Override
	public List<CompanyHoliday> getAllCompanyHolidays() {
		return companyHolidayRepository.findAllByOrderByHolidayDateAsc();
	}

	@Override
	public Optional<CompanyHoliday> getCompanyHolidayById(Long id) {
		return companyHolidayRepository.findById(id);
	}

	@Override
	public Optional<CompanyHoliday> getCompanyHolidayByDate(LocalDate date) {
		return companyHolidayRepository.findByHolidayDate(date);
	}

	@Override
	@Transactional
	public CompanyHoliday updateCompanyHoliday(Long id, CompanyHolidayRequest request) {
		CompanyHoliday companyHoliday = companyHolidayRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("指定された会社休日が見つかりません。"));

		// 日付が変更された場合の重複チェック
		if (!companyHoliday.getHolidayDate().equals(request.getHolidayDate())) {
			Optional<CompanyHoliday> existingHoliday = companyHolidayRepository
					.findByHolidayDate(request.getHolidayDate());
			if (existingHoliday.isPresent() && !existingHoliday.get().getId().equals(id)) {
				throw new IllegalArgumentException("指定された日付は既に他の会社休日として登録されています。");
			}
		}

		companyHoliday.setHolidayDate(request.getHolidayDate());
		companyHoliday.setHolidayName(request.getHolidayName());
		companyHoliday.setUpdatedAt(LocalDateTime.now());

		// 登録ユーザーは通常変更しないが、必要であれば更新ロジックを追加
		// if (request.getCreatedByUserId() != null) {
		//     User createdByUser = userRepository.findById(request.getCreatedByUserId())
		//             .orElseThrow(() -> new IllegalArgumentException("指定された登録ユーザーが見つかりません。"));
		//     companyHoliday.setCreatedByUser(createdByUser);
		// }

		return companyHolidayRepository.save(companyHoliday);
	}

	@Override
	@Transactional
	public boolean deleteCompanyHoliday(Long id, User currentUser) throws AccessDeniedException {

		//ユーザーがログインしているか確認
		if (currentUser == null) {
			throw new AccessDeniedException("ログインしていません。");
		}
		//削除する休日ががあるか確認
		CompanyHoliday companyHoliday = companyHolidayRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("指定した日に休日登録がありません。"));

		boolean isAdmin = (currentUser.getRole() == UserRole.ADMIN);
		boolean isCreator = false;

		//登録者の存在確認と休日登録した本人か確認
		if (companyHoliday.getCreatedByUser() != null) {
			isCreator = companyHoliday.getCreatedByUser().getId().equals(currentUser.getId());
		}
		if (isAdmin || isCreator) {
			companyHolidayRepository.deleteById(id);
			return true;
		}else {
			throw new AccessDeniedException("この会社休日を削除する権限がありません。"); 
		}
	}
}