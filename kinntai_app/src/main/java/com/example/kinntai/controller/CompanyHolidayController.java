package com.example.kinntai.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
import com.example.kinntai.service.CompanyHolidayService;
import com.example.kinntai.service.UserService; // ユーザー情報を取得するサービス

@RestController
@RequestMapping("/api/company-holidays")
@CrossOrigin(origins = "*") // CORS対応
public class CompanyHolidayController {

    @Autowired
    private CompanyHolidayService companyHolidayService;

    @Autowired
    private UserService userService; // 登録ユーザーのユーザー名を取得するため

    // 会社休日の登録
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

    // すべての会社休日を取得
    @GetMapping
    public ResponseEntity<List<CompanyHolidayResponse>> getAllCompanyHolidays() {
        List<CompanyHoliday> holidays = companyHolidayService.getAllCompanyHolidays();
        List<CompanyHolidayResponse> responses = holidays.stream()
                .map(this::convertToCompanyHolidayResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    // 会社休日の更新
    @PutMapping("/{id}")
    public ResponseEntity<?> updateCompanyHoliday(@PathVariable Long id, @RequestBody CompanyHolidayRequest request) {
        try {
            CompanyHoliday updatedHoliday = companyHolidayService.updateCompanyHoliday(id, request);
            return ResponseEntity.ok(convertToCompanyHolidayResponse(updatedHoliday));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("会社休日の更新に失敗しました: " + e.getMessage());
        }
    }

    // 会社休日の削除
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCompanyHoliday(@PathVariable Long id) {
        try {
            boolean deleted = companyHolidayService.deleteCompanyHoliday(id);
            if (deleted) {
                return ResponseEntity.ok("会社休日が削除されました。");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("指定された会社休日が見つかりませんでした。");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("会社休日の削除に失敗しました: " + e.getMessage());
        }
    }

    // CompanyHoliday エンティティを CompanyHolidayResponse DTO に変換するヘルパーメソッド
    private CompanyHolidayResponse convertToCompanyHolidayResponse(CompanyHoliday companyHoliday) {
        CompanyHolidayResponse response = new CompanyHolidayResponse();
        response.setId(companyHoliday.getId());
        response.setHolidayDate(companyHoliday.getHolidayDate());
        response.setHolidayName(companyHoliday.getHolidayName());
        response.setCreatedByUsername(companyHoliday.getCreatedByUser().getUsername()); // 登録者名
        response.setCreatedAt(companyHoliday.getCreatedAt());
        response.setUpdatedAt(companyHoliday.getUpdatedAt());
        return response;
    }
}