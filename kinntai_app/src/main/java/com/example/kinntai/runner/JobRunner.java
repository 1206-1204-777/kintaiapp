package com.example.kinntai.runner;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JobRunner implements CommandLineRunner {

    private final JobLauncher jobLauncher;
    private final Job attendanceWeeklyJob; // Job名はJobConfigで定義した名前と一致するものを指定

    @Override
    public void run(String... args) throws Exception {
        JobParameters params = new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis()) // 同一JobInstanceを避けるため
                .toJobParameters();

        jobLauncher.run(attendanceWeeklyJob, params);
        System.out.println("✅ バッチジョブを手動実行しました。");
    }
}
