package com.example.kinntai.scheduler;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableScheduling
@RequiredArgsConstructor
public class CsvJobScheduler {
    private final JobLauncher jobLauncher;
    private final Job attendanceCsvJob;

    @Scheduled(cron = "0 0 8 ? * MON")
    public void launchJob() throws Exception {
        var params = new JobParametersBuilder()
            .addLong("time", System.currentTimeMillis())
            .toJobParameters();
        jobLauncher.run(attendanceCsvJob, params);
    }
}