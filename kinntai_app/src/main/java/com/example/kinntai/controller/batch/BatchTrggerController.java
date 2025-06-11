package com.example.kinntai.controller.batch;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/*Excel出力を手動で行うコントローラー*/
@RestController
@RequestMapping("/api/batch")
class BatchTrggerController {
	@Autowired
	private JobLauncher jobLauncher;

	@Autowired
	@Qualifier("weeklySummaryJob") //実行するバッチ処理を処理名を使い指定する
	private Job weeklySummaryJob;

	@GetMapping("/start-weekly-job")
	String startWeeklyJob() {
		try {

			JobParameters jobParameters = new JobParametersBuilder()
					.addLong("time", System.currentTimeMillis())
					.toJobParameters();

			//非同期設定にしてジョブを実行
			jobLauncher.run(weeklySummaryJob, jobParameters);

			return "ジョブを開始しました";
		} catch (Exception e) {
			e.printStackTrace();
			return e.getMessage();
		}

	}

}
