package com.example.kinntai.config;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import com.example.kinntai.tasklet.AttendanceExcelTasklet;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class JobConfig {
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final AttendanceExcelTasklet tasklet;

//    @Bean
//    public Step attendanceWeeklyStep() {
//        return new StepBuilder("attendanceWeeklyStep", jobRepository)
//                .tasklet(tasklet, transactionManager)
//                .build();
//    }
//
//    @Bean
//    public Job attendanceCsvJob() {
//        return new JobBuilder("attendanceCsvJob", jobRepository)
//                .start(attendanceWeeklyStep())
//                .build();
//    }
    
    @Bean
    public Step weeklySummaryStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("weeklySummaryStep", jobRepository)
                .tasklet(tasklet, transactionManager)
                .build();
    }

    @Bean
    public Job weeklySummaryJob(JobRepository jobRepository) {
        return new JobBuilder("weeklySummaryJob", jobRepository)
                .start(weeklySummaryStep(jobRepository, transactionManager))
                .build();
    }
}