package com.example.kinntai.batch.config;

import jakarta.persistence.EntityManagerFactory;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import com.example.kinntai.batch.WeeklySummaryProcessor;
import com.example.kinntai.entity.User;
import com.example.kinntai.entity.WeeklySummary;

/*
 * Excel出力するバッチの設定を記述*/
@Configuration
public class WeeklyBatchConfig {

	private final EntityManagerFactory entityManagerFactory;

	@Autowired
	private WeeklySummaryProcessor summaryProcessor;

	/*
	 * コンストラクタ*/
	public WeeklyBatchConfig(EntityManagerFactory entityManagerFactory) {
		this.entityManagerFactory = entityManagerFactory;
	}

	/*
	 * 社員の勤怠情報を取得*/
	@Bean
	@StepScope
	JpaPagingItemReader<User> userItemReader() {
		return new JpaPagingItemReaderBuilder<User>()
				.name("userItemReader")
				.entityManagerFactory(entityManagerFactory)
				.queryString("SELECT u FROM User u ORDER BY u.id") // ユーザー情報を検索するJPQL
				.pageSize(100)
				.build();

	}

	//勤怠情報の処理（データの組み立て）を行う
	@Bean
	ItemProcessor<User, WeeklySummary> UserToWeekSummaryProccessor() {
		return summaryProcessor;
	}

	//組み立てた勤怠情報をデータベースに書き込む
	@Bean
	JpaItemWriter<WeeklySummary> userItemWriter() {
		return new JpaItemWriterBuilder<WeeklySummary>()
				.entityManagerFactory(entityManagerFactory)
				.build();

	}

	//チャンク方式のバッチステップ（処理内容）の定義
	@Bean
	Step weeklySummaryStep(
			JobRepository jobRepository,
			PlatformTransactionManager transactionManager) {

		return new StepBuilder("weeklySummaryStep", jobRepository)
				.<User, WeeklySummary> chunk(10, transactionManager)
				.reader(userItemReader())
				.processor(summaryProcessor)
				.writer(userItemWriter())
				.build();

	}

	//ジョブ内容（ステップの実行）を定義
	@Bean
	Job weeklySummaryJob(JobRepository jobRepository, Step weeklySummaryStep) {
		return new JobBuilder("weeklySummaryJob", jobRepository)
				.start(weeklySummaryStep)
				.build();

	}
}
