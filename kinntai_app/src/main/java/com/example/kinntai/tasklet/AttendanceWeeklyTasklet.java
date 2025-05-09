package com.example.kinntai.tasklet;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

import com.example.kinntai.entity.Attendance;
import com.example.kinntai.repository.AttendanceRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AttendanceWeeklyTasklet implements Tasklet {
    private final AttendanceRepository repository;

    @SuppressWarnings({ "null", "deprecation" })
	@Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.with(DayOfWeek.MONDAY);
        LocalDate endOfWeek = today.with(DayOfWeek.SUNDAY);
        int weekNumber = today.get(WeekFields.ISO.weekOfWeekBasedYear());

        // 月曜〜日曜の勤怠データを取得（出退勤が両方あるデータのみ）
        List<Attendance> attendances = repository.findByDateBetween(startOfWeek, endOfWeek)
                .stream()
                .filter(a -> a.getClockIn() != null && a.getClockOut() != null)
                .collect(Collectors.toList());

        // ユーザーごとにグループ化
        Map<Long, List<Attendance>> grouped = attendances.stream()
                .collect(Collectors.groupingBy(Attendance::getUserId));

        // CSVファイル出力
        String fileName = "weekly_summary_" + today.toString() + ".csv";
        
        // UTF-8 with BOMで出力
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                Files.newOutputStream(Paths.get(fileName)), StandardCharsets.UTF_8))) {
            // BOMを追加
            writer.write('\uFEFF');
            
            CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT
                    .withHeader("ユーザーID", "週番号", "勤務日数", "総勤務時間", "平均勤務時間", "残業時間"));

            for (Map.Entry<Long, List<Attendance>> entry : grouped.entrySet()) {
                Long userId = entry.getKey();
                List<Attendance> records = entry.getValue();

                double totalHours = records.stream()
                        .mapToDouble(a -> Duration.between(a.getClockIn(), a.getClockOut()).toMinutes() / 60.0)
                        .sum();

                int workDays = records.size();
                double average = workDays > 0 ? totalHours / workDays : 0;

                csvPrinter.printRecord(
                        userId,
                        weekNumber,
                        workDays,
                        String.format("%.2f", totalHours),
                        String.format("%.2f", average),
                        String.format("%.2f", Math.max(0, totalHours - 40)) // 残業基準は週40時間
                );
            }
            
            csvPrinter.close();
        }

        return RepeatStatus.FINISHED;
    }
}