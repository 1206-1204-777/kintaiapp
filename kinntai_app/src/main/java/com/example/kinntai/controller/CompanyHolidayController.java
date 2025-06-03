package com.example.kinntai.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.kinntai.dto.CompanyHolidayRequest;
import com.example.kinntai.dto.CompanyHolidayResponse;
import com.example.kinntai.entity.CompanyHoliday;
import com.example.kinntai.entity.User;
import com.example.kinntai.service.CompanyHolidayService;

@RestController
@RequestMapping("/api/company-holidays")
@CrossOrigin(origins = "*")
/**
 * 会社休日に関する操作を提供するコントローラークラスです。
 * 管理者が会社休日を登録、取得、更新、削除するための機能を提供します。
 */
public class CompanyHolidayController {

	@Autowired
	private CompanyHolidayService companyHolidayService;

	/**
	 * 新しい会社休日を登録するエンドポイントです。
	 *
	 * @param request 登録する会社休日の情報
	 * @return 登録結果またはエラーメッセージ
	 */
	@PostMapping
	public ResponseEntity<?> createCompanyHoliday(@RequestBody CompanyHolidayRequest request) {
		try {
			CompanyHoliday companyHoliday = companyHolidayService.createCompanyHoliday(request);
			return ResponseEntity.status(HttpStatus.CREATED).body(convertToCompanyHolidayResponse(companyHoliday));
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("会社休日の登録に失敗しました: " + e.getMessage());
		}
	}

	/**
	 * すべての会社休日を取得するエンドポイントです。
	 *
	 * @return 会社休日のリスト
	 */
	@GetMapping
	public ResponseEntity<List<CompanyHolidayResponse>> getAllCompanyHolidays() {
		List<CompanyHoliday> holidays = companyHolidayService.getAllCompanyHolidays();
		List<CompanyHolidayResponse> responses = holidays.stream()
				.map(this::convertToCompanyHolidayResponse)
				.collect(Collectors.toList());
		return ResponseEntity.ok(responses);
	}

	/**
	 * 指定されたIDの会社休日を更新するエンドポイントです。
	 *
	 * @param id 更新対象のID
	 * @param request 更新内容
	 * @return 更新結果またはエラーメッセージ
	 */
	@PutMapping("/{id}")
	public ResponseEntity<?> updateCompanyHoliday(@PathVariable Long id,
			@RequestBody CompanyHolidayRequest request) {
		try {
			CompanyHoliday updatedHoliday = companyHolidayService.updateCompanyHoliday(id, request);
			return ResponseEntity.ok(convertToCompanyHolidayResponse(updatedHoliday));
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("会社休日の更新に失敗しました: " + e.getMessage());
		}
	}

	/**
	 * 指定されたIDの会社休日を削除するエンドポイントです。
	 *
	 * @param id 削除対象のID
	 * @return 削除結果またはエラーメッセージ
	 */
	@DeleteMapping("/{id}")
	public ResponseEntity<?> deleteCompanyHoliday(@PathVariable Long id, @AuthenticationPrincipal User currentUser) {
		try {
			boolean deleted = companyHolidayService.deleteCompanyHoliday(id, currentUser);
			if (deleted) {
				return ResponseEntity.ok("会社休日が削除されました。");
			} else {
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body("指定された会社休日が見つかりませんでした。");
			}
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("会社休日の削除に失敗しました: " + e.getMessage());
		}
	}

	/**
	 * CompanyHolidayエンティティをレスポンスDTOに変換します。
	 *
	 * @param companyHoliday 会社休日エンティティ
	 * @return DTO形式の会社休日情報
	 */
	private CompanyHolidayResponse convertToCompanyHolidayResponse(CompanyHoliday companyHoliday) {
		CompanyHolidayResponse response = new CompanyHolidayResponse();
		response.setId(companyHoliday.getId());
		response.setHolidayDate(companyHoliday.getHolidayDate());
		response.setHolidayName(companyHoliday.getHolidayName());
		response.setCreatedByUsername(companyHoliday.getCreatedByUser().getUsername());
		response.setCreatedAt(companyHoliday.getCreatedAt());
		response.setUpdatedAt(companyHoliday.getUpdatedAt());
		return response;
	}
}
