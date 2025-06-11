package com.example.kinntai;

import javax.sql.DataSource;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableAsync
@EnableBatchProcessing
public class KintaiAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(KintaiAppApplication.class, args);
		
	}
	
	//バッチ処理に必要なテーブルの自動生成をするコード
	@Bean
     DataSourceInitializer batchDataSourceInitializer(DataSource dataSource) {
        DataSourceInitializer initializer = new DataSourceInitializer();
        initializer.setDataSource(dataSource);
        
        // Spring BatchのPostgreSQL用スキーマ作成スクリプトを指定
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.addScript(new ClassPathResource("org/springframework/batch/core/schema-postgresql.sql"));
        
        populator.setContinueOnError(true);
        initializer.setDatabasePopulator(populator);
        // 常に初期化を実行する（もしテーブルが存在していてもエラーにならない）
        initializer.setEnabled(true); 
        
        return initializer;
    }
}

