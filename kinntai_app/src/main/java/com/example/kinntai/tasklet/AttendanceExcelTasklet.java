package com.example.kinntai.tasklet;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Locale;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AttendanceExcelTasklet implements Tasklet {

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        String templatePath = "src/main/resources/excel/勤務表_template.xlsx";
        String outputPath = "勤務表_2024年12月_儘田一貴.xlsx";

        try (InputStream in = Files.newInputStream(Paths.get(templatePath));
             Workbook workbook = new XSSFWorkbook(in)) {

            Sheet sheet = workbook.getSheetAt(0);

            LocalDate date = LocalDate.of(2024, 12, 1);
            int startRow = 10; // A11から開始（0ベース）

            int workDays = 0;
            int totalMinutes = 0;

            for (int i = 0; i < 31; i++) {
                Row row = sheet.getRow(startRow + i);
                if (row == null) row = sheet.createRow(startRow + i);

                // 日付（A列）
                String dayStr = String.format("%d月%d日 (%s)",
                        date.getMonthValue(),
                        date.getDayOfMonth(),
                        date.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.JAPANESE));
                row.getCell(0, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).setCellValue(dayStr);

                // 初期化（B〜F列）
                for (int col = 1; col <= 5; col++) {
                    row.getCell(col, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).setBlank();
                }

                if (date.getDayOfWeek().getValue() <= 5) {
                    // 出勤データ（平日のみ）
                    row.getCell(1).setCellValue("10:00"); // 出勤
                    row.getCell(2).setCellValue("19:00"); // 退勤
                    row.getCell(3).setCellValue("1:00");  // 休憩
                    row.getCell(4).setCellValue("8:00");  // 勤務時間

                    workDays++;
                    totalMinutes += 8 * 60;
                } else {
                    // 休みの日も勤務時間欄に明示的に0:00
                    row.getCell(4).setCellValue("0:00");
                }

                date = date.plusDays(1);
            }

            // 勤務時間合計 → E43（行42, 列4）
            sheet.getRow(42).getCell(4, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK)
                    .setCellValue(String.format("%d:%02d", totalMinutes / 60, totalMinutes % 60));

            return RepeatStatus.FINISHED;
        }
    }
}
