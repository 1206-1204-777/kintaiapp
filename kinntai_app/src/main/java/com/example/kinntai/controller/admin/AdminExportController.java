package com.example.kinntai.controller.admin;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.kinntai.service.admin.AdminExportService;

/**
 * 管理者向けデータエクスポートAPIコントローラー
 * 勤怠データ、社員情報、休暇申請データのCSV/Excel出力機能を提供
 */
@RestController
@RequestMapping("/api/admin/export")
@CrossOrigin(origins = "*")
public class AdminExportController {

    @Autowired
    private AdminExportService adminExportService;

    /**
     * 勤怠データをCSV形式でエクスポート
     *
     * @param startDate 開始日
     * @param endDate 終了日
     * @param userId ユーザーID（指定時は特定ユーザーのみ）
     * @param department 部署名（指定時は特定部署のみ）
     * @return CSVファイル
     */
    @GetMapping("/attendance/csv")
    public ResponseEntity<byte[]> exportAttendanceCSV(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String department) {
        try {
            byte[] csvData = adminExportService.exportAttendanceCSV(startDate, endDate, userId, department);
            
            String filename = String.format("attendance_%s_%s.csv", startDate, endDate);
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                    .body(csvData);
        } catch (Exception e) {
            System.err.println("勤怠データCSVエクスポートエラー: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 勤怠データをExcel形式でエクスポート
     *
     * @param startDate 開始日
     * @param endDate 終了日
     * @param userId ユーザーID（指定時は特定ユーザーのみ）
     * @param department 部署名（指定時は特定部署のみ）
     * @return Excelファイル
     */
    @GetMapping("/attendance/excel")
    public ResponseEntity<byte[]> exportAttendanceExcel(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String department) {
        try {
            byte[] excelData = adminExportService.exportAttendanceExcel(startDate, endDate, userId, department);
            
            String filename = String.format("attendance_%s_%s.xlsx", startDate, endDate);
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(excelData);
        } catch (Exception e) {
            System.err.println("勤怠データExcelエクスポートエラー: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 社員情報をCSV形式でエクスポート
     *
     * @param includePersonalInfo 個人情報を含めるかどうか
     * @return CSVファイル
     */
    @GetMapping("/employees/csv")
    public ResponseEntity<byte[]> exportEmployeesCSV(
            @RequestParam(defaultValue = "false") boolean includePersonalInfo) {
        try {
            byte[] csvData = adminExportService.exportEmployeesCSV(includePersonalInfo);
            
            String filename = "employees_" + LocalDate.now() + ".csv";
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                    .body(csvData);
        } catch (Exception e) {
            System.err.println("社員情報CSVエクスポートエラー: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 残業時間レポートをCSV形式でエクスポート
     *
     * @param startDate 開始日
     * @param endDate 終了日
     * @param department 部署名（指定時は特定部署のみ）
     * @return CSVファイル
     */
    @GetMapping("/overtime/csv")
    public ResponseEntity<byte[]> exportOvertimeCSV(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String department) {
        try {
            byte[] csvData = adminExportService.exportOvertimeReportCSV(startDate, endDate, department);
            
            String filename = String.format("overtime_report_%s_%s.csv", startDate, endDate);
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                    .body(csvData);
        } catch (Exception e) {
            System.err.println("残業レポートCSVエクスポートエラー: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 休暇申請データをCSV形式でエクスポート
     *
     * @param startDate 開始日
     * @param endDate 終了日
     * @param status 申請ステータス
     * @return CSVファイル
     */
    @GetMapping("/holiday-requests/csv")
    public ResponseEntity<byte[]> exportHolidayRequestsCSV(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String status) {
        try {
            byte[] csvData = adminExportService.exportHolidayRequestsCSV(startDate, endDate, status);
            
            String filename = String.format("holiday_requests_%s_%s.csv", startDate, endDate);
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                    .body(csvData);
        } catch (Exception e) {
            System.err.println("休暇申請CSVエクスポートエラー: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 部署別勤怠サマリーをCSV形式でエクスポート
     *
     * @param startDate 開始日
     * @param endDate 終了日
     * @return CSVファイル
     */
    @GetMapping("/department-summary/csv")
    public ResponseEntity<byte[]> exportDepartmentSummaryCSV(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            byte[] csvData = adminExportService.exportDepartmentSummaryCSV(startDate, endDate);
            
            String filename = String.format("department_summary_%s_%s.csv", startDate, endDate);
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                    .body(csvData);
        } catch (Exception e) {
            System.err.println("部署別サマリーCSVエクスポートエラー: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 今日の勤怠状況をCSV形式でエクスポート
     * クイックエクスポート機能として提供
     *
     * @return CSVファイル
     */
    @GetMapping("/today/csv")
    public ResponseEntity<byte[]> exportTodayAttendanceCSV() {
        try {
            LocalDate today = LocalDate.now();
            byte[] csvData = adminExportService.exportTodayAttendanceCSV(today);
            
            String filename = "today_attendance_" + today + ".csv";
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                    .body(csvData);
        } catch (Exception e) {
            System.err.println("今日の勤怠CSVエクスポートエラー: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }
}