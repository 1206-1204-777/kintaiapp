package com.example.kinntai.service.admin;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.kinntai.entity.Attendance;
import com.example.kinntai.entity.PersonalHoliday;
import com.example.kinntai.entity.User;
import com.example.kinntai.repository.AttendanceRepository;
import com.example.kinntai.repository.PersonalHolidayRepository;
import com.example.kinntai.repository.UserRepository;

/**
 * 管理者向けデータエクスポートサービス
 * 勤怠データ、社員情報、休暇申請データのCSV/Excel出力機能を提供
 */
@Service
public class AdminExportService {

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PersonalHolidayRepository personalHolidayRepository;

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    /**
     * 勤怠データをCSV形式でエクスポート
     */
    public byte[] exportAttendanceCSV(LocalDate startDate, LocalDate endDate, Long userId, String department) {
        List<Attendance> attendances = getAttendanceData(startDate, endDate, userId, department);
        
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             PrintWriter writer = new PrintWriter(new OutputStreamWriter(baos, StandardCharsets.UTF_8))) {
            
            // BOM（Byte Order Mark）を追加してExcelで正しく表示されるようにする
            baos.write(0xEF);
            baos.write(0xBB);
            baos.write(0xBF);
            
            // CSVヘッダー
            writer.println("日付,社員名,部署,出勤時刻,退勤時刻,勤務時間,休憩時間,残業時間,勤務タイプ");
            
            // データ行
            for (Attendance attendance : attendances) {
                StringBuilder line = new StringBuilder();
                
                line.append(attendance.getDate().format(dateFormatter)).append(",");
                line.append(escapeCSV(attendance.getUser().getUsername())).append(",");
                line.append(escapeCSV(attendance.getUser().getLocation() != null ? 
                    attendance.getUser().getLocation().getName() : "未設定")).append(",");
                line.append(attendance.getClockIn() != null ? 
                    attendance.getClockIn().format(timeFormatter) : "").append(",");
                line.append(attendance.getClockOut() != null ? 
                    attendance.getClockOut().format(timeFormatter) : "").append(",");
                line.append(formatMinutes(attendance.getTotalWorkMin())).append(",");
                line.append(formatMinutes(attendance.getTotalBreakMin())).append(",");
                line.append(formatMinutes(attendance.getOvertimeMinutes())).append(",");
                line.append(attendance.getWorkType() != null ? 
                    attendance.getWorkType().name() : "");
                
                writer.println(line.toString());
            }
            
            writer.flush();
            return baos.toByteArray();
            
        } catch (Exception e) {
            throw new RuntimeException("CSVエクスポートに失敗しました", e);
        }
    }

    /**
     * 勤怠データをExcel形式でエクスポート
     */
    public byte[] exportAttendanceExcel(LocalDate startDate, LocalDate endDate, Long userId, String department) {
        List<Attendance> attendances = getAttendanceData(startDate, endDate, userId, department);
        
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            
            Sheet sheet = workbook.createSheet("勤怠データ");
            
            // ヘッダースタイル
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            
            // ヘッダー行
            Row headerRow = sheet.createRow(0);
            String[] headers = {"日付", "社員名", "部署", "出勤時刻", "退勤時刻", "勤務時間", "休憩時間", "残業時間", "勤務タイプ"};
            
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // データ行
            int rowNum = 1;
            for (Attendance attendance : attendances) {
                Row row = sheet.createRow(rowNum++);
                
                row.createCell(0).setCellValue(attendance.getDate().format(dateFormatter));
                row.createCell(1).setCellValue(attendance.getUser().getUsername());
                row.createCell(2).setCellValue(attendance.getUser().getLocation() != null ? 
                    attendance.getUser().getLocation().getName() : "未設定");
                row.createCell(3).setCellValue(attendance.getClockIn() != null ? 
                    attendance.getClockIn().format(timeFormatter) : "");
                row.createCell(4).setCellValue(attendance.getClockOut() != null ? 
                    attendance.getClockOut().format(timeFormatter) : "");
                row.createCell(5).setCellValue(formatMinutes(attendance.getTotalWorkMin()));
                row.createCell(6).setCellValue(formatMinutes(attendance.getTotalBreakMin()));
                row.createCell(7).setCellValue(formatMinutes(attendance.getOvertimeMinutes()));
                row.createCell(8).setCellValue(attendance.getWorkType() != null ? 
                    attendance.getWorkType().name() : "");
            }
            
            // 列幅自動調整
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            workbook.write(baos);
            return baos.toByteArray();
            
        } catch (Exception e) {
            throw new RuntimeException("Excelエクスポートに失敗しました", e);
        }
    }

    /**
     * 社員情報をCSV形式でエクスポート
     */
    public byte[] exportEmployeesCSV(boolean includePersonalInfo) {
        List<User> users = userRepository.findAll();
        
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             PrintWriter writer = new PrintWriter(new OutputStreamWriter(baos, StandardCharsets.UTF_8))) {
            
            // BOM追加
            baos.write(0xEF);
            baos.write(0xBB);
            baos.write(0xBF);
            
            // CSVヘッダー
            if (includePersonalInfo) {
                writer.println("ID,ユーザー名,メールアドレス,部署,勤務地,権限,登録日,勤務開始時刻,勤務終了時刻");
            } else {
                writer.println("ID,ユーザー名,部署,勤務地,権限");
            }
            
            // データ行
            for (User user : users) {
                StringBuilder line = new StringBuilder();
                
                line.append(user.getId()).append(",");
                line.append(escapeCSV(user.getUsername())).append(",");
                
                if (includePersonalInfo) {
                    line.append(escapeCSV(user.getEmail())).append(",");
                }
                
                line.append(escapeCSV(user.getLocation() != null ? 
                    user.getLocation().getName() : "未設定")).append(",");
                line.append(escapeCSV(user.getLocation() != null ? 
                    user.getLocation().getName() : "未設定")).append(",");
                line.append(user.getRole().displayName()).append(",");
                
                if (includePersonalInfo) {
                    line.append(user.getCreatedAt() != null ? 
                        user.getCreatedAt().format(dateFormatter) : "").append(",");
                    line.append(user.getDefaultStartTime() != null ? 
                        user.getDefaultStartTime().format(timeFormatter) : "").append(",");
                    line.append(user.getDefaultEndTime() != null ? 
                        user.getDefaultEndTime().format(timeFormatter) : "");
                } else {
                    // 最後のカンマを削除
                    if (line.length() > 0 && line.charAt(line.length() - 1) == ',') {
                        line.setLength(line.length() - 1);
                    }
                }
                
                writer.println(line.toString());
            }
            
            writer.flush();
            return baos.toByteArray();
            
        } catch (Exception e) {
            throw new RuntimeException("社員情報CSVエクスポートに失敗しました", e);
        }
    }

    /**
     * 残業時間レポートをCSV形式でエクスポート
     */
    public byte[] exportOvertimeReportCSV(LocalDate startDate, LocalDate endDate, String department) {
        List<Attendance> attendances = getAttendanceData(startDate, endDate, null, department);
        
        // 残業時間がある勤怠のみをフィルタリング
        List<Attendance> overtimeAttendances = attendances.stream()
                .filter(a -> a.getOvertimeMinutes() != null && a.getOvertimeMinutes() > 0)
                .collect(Collectors.toList());
        
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             PrintWriter writer = new PrintWriter(new OutputStreamWriter(baos, StandardCharsets.UTF_8))) {
            
            // BOM追加
            baos.write(0xEF);
            baos.write(0xBB);
            baos.write(0xBF);
            
            // CSVヘッダー
            writer.println("日付,社員名,部署,残業時間,出勤時刻,退勤時刻,総勤務時間");
            
            // データ行
            for (Attendance attendance : overtimeAttendances) {
                StringBuilder line = new StringBuilder();
                
                line.append(attendance.getDate().format(dateFormatter)).append(",");
                line.append(escapeCSV(attendance.getUser().getUsername())).append(",");
                line.append(escapeCSV(attendance.getUser().getLocation() != null ? 
                    attendance.getUser().getLocation().getName() : "未設定")).append(",");
                line.append(formatMinutes(attendance.getOvertimeMinutes())).append(",");
                line.append(attendance.getClockIn() != null ? 
                    attendance.getClockIn().format(timeFormatter) : "").append(",");
                line.append(attendance.getClockOut() != null ? 
                    attendance.getClockOut().format(timeFormatter) : "").append(",");
                line.append(formatMinutes(attendance.getTotalWorkMin()));
                
                writer.println(line.toString());
            }
            
            writer.flush();
            return baos.toByteArray();
            
        } catch (Exception e) {
            throw new RuntimeException("残業レポートCSVエクスポートに失敗しました", e);
        }
    }

    /**
     * 休暇申請データをCSV形式でエクスポート
     */
    public byte[] exportHolidayRequestsCSV(LocalDate startDate, LocalDate endDate, String status) {
        List<PersonalHoliday> holidays = personalHolidayRepository.findAll().stream()
                .filter(h -> !h.getHolidayDate().isBefore(startDate) && !h.getHolidayDate().isAfter(endDate))
                .collect(Collectors.toList());
        
        if (status != null && !status.isEmpty()) {
            holidays = holidays.stream()
                    .filter(h -> h.getStatus().name().equalsIgnoreCase(status))
                    .collect(Collectors.toList());
        }
        
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             PrintWriter writer = new PrintWriter(new OutputStreamWriter(baos, StandardCharsets.UTF_8))) {
            
            // BOM追加
            baos.write(0xEF);
            baos.write(0xBB);
            baos.write(0xBF);
            
            // CSVヘッダー
            writer.println("申請ID,申請者,休暇日,休暇種別,理由,ステータス,申請日,承認者,更新日");
            
            // データ行
            for (PersonalHoliday holiday : holidays) {
                StringBuilder line = new StringBuilder();
                
                line.append(holiday.getId()).append(",");
                line.append(escapeCSV(holiday.getUser().getUsername())).append(",");
                line.append(holiday.getHolidayDate().format(dateFormatter)).append(",");
                line.append(holiday.getHolidayType().name()).append(",");
                line.append(escapeCSV(holiday.getReason() != null ? holiday.getReason() : "")).append(",");
                line.append(holiday.getStatus().name()).append(",");
                line.append(holiday.getCreatedAt().format(dateTimeFormatter)).append(",");
                line.append(escapeCSV(holiday.getApprover() != null ? 
                    holiday.getApprover().getUsername() : "")).append(",");
                line.append(holiday.getUpdatedAt() != null ? 
                    holiday.getUpdatedAt().format(dateTimeFormatter) : "");
                
                writer.println(line.toString());
            }
            
            writer.flush();
            return baos.toByteArray();
            
        } catch (Exception e) {
            throw new RuntimeException("休暇申請CSVエクスポートに失敗しました", e);
        }
    }

    /**
     * 部署別勤怠サマリーをCSV形式でエクスポート
     */
    public byte[] exportDepartmentSummaryCSV(LocalDate startDate, LocalDate endDate) {
        List<Attendance> attendances = attendanceRepository.findByDateBetween(startDate, endDate);
        
        // 部署別に集計
        Map<String, List<Attendance>> departmentGroups = attendances.stream()
                .collect(Collectors.groupingBy(a -> 
                    a.getUser().getLocation() != null ? 
                    a.getUser().getLocation().getName() : "未設定"));
        
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             PrintWriter writer = new PrintWriter(new OutputStreamWriter(baos, StandardCharsets.UTF_8))) {
            
            // BOM追加
            baos.write(0xEF);
            baos.write(0xBB);
            baos.write(0xBF);
            
            // CSVヘッダー
            writer.println("部署名,社員数,出勤日数,総勤務時間,平均勤務時間,総残業時間,平均残業時間");
            
            // データ行
            for (Map.Entry<String, List<Attendance>> entry : departmentGroups.entrySet()) {
                String department = entry.getKey();
                List<Attendance> deptAttendances = entry.getValue();
                
                long employeeCount = deptAttendances.stream()
                        .map(a -> a.getUser().getId())
                        .distinct()
                        .count();
                
                long workDays = deptAttendances.stream()
                        .filter(a -> a.getClockIn() != null)
                        .count();
                
                long totalWorkMinutes = deptAttendances.stream()
                        .filter(a -> a.getTotalWorkMin() != null)
                        .mapToLong(Attendance::getTotalWorkMin)
                        .sum();
                
                long totalOvertimeMinutes = deptAttendances.stream()
                        .filter(a -> a.getOvertimeMinutes() != null)
                        .mapToLong(Attendance::getOvertimeMinutes)
                        .sum();
                
                double avgWorkHours = workDays > 0 ? (double) totalWorkMinutes / workDays / 60.0 : 0.0;
                double avgOvertimeHours = workDays > 0 ? (double) totalOvertimeMinutes / workDays / 60.0 : 0.0;
                
                StringBuilder line = new StringBuilder();
                line.append(escapeCSV(department)).append(",");
                line.append(employeeCount).append(",");
                line.append(workDays).append(",");
                line.append(String.format("%.2f", totalWorkMinutes / 60.0)).append(",");
                line.append(String.format("%.2f", avgWorkHours)).append(",");
                line.append(String.format("%.2f", totalOvertimeMinutes / 60.0)).append(",");
                line.append(String.format("%.2f", avgOvertimeHours));
                
                writer.println(line.toString());
            }
            
            writer.flush();
            return baos.toByteArray();
            
        } catch (Exception e) {
            throw new RuntimeException("部署別サマリーCSVエクスポートに失敗しました", e);
        }
    }

    /**
     * 今日の勤怠状況をCSV形式でエクスポート
     */
    public byte[] exportTodayAttendanceCSV(LocalDate today) {
        List<User> allUsers = userRepository.findAll();
        List<Attendance> todayAttendances = attendanceRepository.findByDateBetween(today, today);
        
        Map<Long, Attendance> attendanceMap = todayAttendances.stream()
                .collect(Collectors.toMap(a -> a.getUser().getId(), a -> a));
        
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             PrintWriter writer = new PrintWriter(new OutputStreamWriter(baos, StandardCharsets.UTF_8))) {
            
            // BOM追加
            baos.write(0xEF);
            baos.write(0xBB);
            baos.write(0xBF);
            
            // CSVヘッダー
            writer.println("社員名,部署,出勤時刻,退勤時刻,勤務状況,残業時間");
            
            // データ行
            for (User user : allUsers) {
                Attendance attendance = attendanceMap.get(user.getId());
                StringBuilder line = new StringBuilder();
                
                line.append(escapeCSV(user.getUsername())).append(",");
                line.append(escapeCSV(user.getLocation() != null ? 
                    user.getLocation().getName() : "未設定")).append(",");
                
                if (attendance != null) {
                    line.append(attendance.getClockIn() != null ? 
                        attendance.getClockIn().format(timeFormatter) : "").append(",");
                    line.append(attendance.getClockOut() != null ? 
                        attendance.getClockOut().format(timeFormatter) : "").append(",");
                    
                    // 勤務状況判定
                    String status;
                    if (attendance.getClockIn() != null && attendance.getClockOut() != null) {
                        status = (attendance.getOvertimeMinutes() != null && attendance.getOvertimeMinutes() > 0) 
                                ? "残業中" : "勤務完了";
                    } else if (attendance.getClockIn() != null) {
                        status = "勤務中";
                    } else {
                        status = "未出勤";
                    }
                    line.append(status).append(",");
                    line.append(formatMinutes(attendance.getOvertimeMinutes()));
                } else {
                    line.append(",,未出勤,0:00");
                }
                
                writer.println(line.toString());
            }
            
            writer.flush();
            return baos.toByteArray();
            
        } catch (Exception e) {
            throw new RuntimeException("今日の勤怠CSVエクスポートに失敗しました", e);
        }
    }

    /**
     * 勤怠データを取得（フィルタリング対応）
     */
    private List<Attendance> getAttendanceData(LocalDate startDate, LocalDate endDate, Long userId, String department) {
        List<Attendance> attendances;
        
        if (userId != null) {
            attendances = attendanceRepository.findByUser_IdAndDateBetweenOrderByDateAsc(userId, startDate, endDate);
        } else {
            attendances = attendanceRepository.findByDateBetween(startDate, endDate);
        }
        
        // 部署でフィルタリング
        if (department != null && !department.isEmpty()) {
            attendances = attendances.stream()
                    .filter(a -> a.getUser().getLocation() != null && 
                           a.getUser().getLocation().getName().equals(department))
                    .collect(Collectors.toList());
        }
        
        return attendances;
    }

    /**
     * CSV用文字列エスケープ
     */
    private String escapeCSV(String value) {
        if (value == null) return "";
        
        // カンマ、改行、ダブルクォートが含まれている場合はダブルクォートで囲む
        if (value.contains(",") || value.contains("\n") || value.contains("\"")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        
        return value;
    }

    /**
     * 分を時間:分形式にフォーマット
     */
    private String formatMinutes(Long minutes) {
        if (minutes == null) return "0:00";
        
        long hours = minutes / 60;
        long mins = minutes % 60;
        
        return String.format("%d:%02d", hours, mins);
    }
}