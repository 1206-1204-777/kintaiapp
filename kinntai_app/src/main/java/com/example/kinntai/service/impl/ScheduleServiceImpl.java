package com.example.kinntai.service.impl;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // トランザクション管理を追加

import com.example.kinntai.dto.ScheduleRequestDto;
import com.example.kinntai.dto.SubmittedScheduleResponseDto;
import com.example.kinntai.entity.RequestStatus;
import com.example.kinntai.entity.Schedule;
import com.example.kinntai.entity.WorkType;
import com.example.kinntai.repository.ScheduleRepository;
import com.example.kinntai.service.ScheduleService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ScheduleServiceImpl implements ScheduleService {

    private final ScheduleRepository scheduleRepository;

    @Override
    @Transactional // トランザクションを追加し、操作の原子性を保証
    public void saveSchedule(ScheduleRequestDto request) {
        // リクエストに含まれる日付の月を特定
        if (request.getDays() != null && !request.getDays().isEmpty()) {
            LocalDate firstDateOfMonth = request.getDays().get(0).getDate().withDayOfMonth(1);
            LocalDate lastDateOfMonth = firstDateOfMonth.withDayOfMonth(firstDateOfMonth.lengthOfMonth());

            // 指定されたuserIdと月の既存のPENDINGステータスのスケジュールを削除
            // APPROVEDやREJECTEDのスケジュールは削除しないようにする（ビジネスロジックによる）
            // 現状では全てのスケジュールを削除するシンプルな実装だが、必要に応じてステータスでフィルタリングする
            scheduleRepository.deleteByUserIdAndDateBetween(request.getUserId(), firstDateOfMonth, lastDateOfMonth);
        }

        List<Schedule> schedules = new ArrayList<>();
        for (ScheduleRequestDto.ScheduleDayDto day : request.getDays()) {
            Schedule schedule = new Schedule();
            schedule.setUserId(request.getUserId());
            schedule.setDate(day.getDate());
            // 🚨 修正点: WorkType.valueOf() の前に大文字に変換
            // フロントエンドから "work" や "holiday" のように小文字で来る可能性があるため
            try {
                schedule.setType(WorkType.valueOf(day.getType().toUpperCase()));
            } catch (IllegalArgumentException e) {
                // 不正なWorkTypeが来た場合のハンドリング
                System.err.println("Invalid WorkType received: " + day.getType());
                // 例外をスローするか、デフォルト値を設定するか、ビジネスロジックに応じて処理
                throw new RuntimeException("Invalid WorkType: " + day.getType(), e);
            }
            schedule.setStatus(RequestStatus.PENDING); // 新規保存時はPENDING
            schedules.add(schedule);
        }
        scheduleRepository.saveAll(schedules);
    }

    @Override
    public List<Schedule> getWeeklySchedule(Long userId, LocalDate weekStart) {
        LocalDate weekEnd = weekStart.plusDays(6);
        return scheduleRepository.findByUserIdAndDateBetween(userId, weekStart, weekEnd);
    }

    @Override
    @Transactional
    public void approveSchedule(Long scheduleId) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
            .orElseThrow(() -> new RuntimeException("Schedule not found with id: " + scheduleId)); // エラーメッセージを改善
        schedule.setStatus(RequestStatus.APPROVED);
        scheduleRepository.save(schedule);
    }

    @Override
    public List<SubmittedScheduleResponseDto> getSubmittedSchedules(Long userId) {
        // 特定のユーザーの全てのスケジュールを取得
        // 実際には、提出された月のスケジュールのみを対象とするのが一般的
        // ここでは、現在の月から過去12ヶ月分のスケジュールを取得する例に修正
        LocalDate now = LocalDate.now();
        LocalDate twelveMonthsAgo = now.minusMonths(11).withDayOfMonth(1); // 過去12ヶ月の最初の日

        // 🚨 修正点: findByUserIdAndDateBetween を使用して期間を限定
        List<Schedule> allUserSchedules = scheduleRepository.findByUserIdAndDateBetween(userId, twelveMonthsAgo, now.withDayOfMonth(now.lengthOfMonth()));


        // 月ごとにスケジュールをグループ化
        Map<String, List<Schedule>> schedulesByMonth = allUserSchedules.stream()
            .collect(Collectors.groupingBy(schedule ->
                schedule.getDate().format(DateTimeFormatter.ofPattern("yyyy-MM"))
            ));

        List<SubmittedScheduleResponseDto> submittedSchedules = new ArrayList<>();

        schedulesByMonth.forEach((month, schedulesInMonth) -> {
            int workDays = 0;
            int holidayDays = 0;
            RequestStatus status = RequestStatus.PENDING; // 月のステータスを決定するための仮の初期値
            String submittedAt = null; // 提出日時を初期化
            String approverName = "-"; // 仮の承認者名

            // 月内のスケジュールを集計
            for (Schedule schedule : schedulesInMonth) {
                if (schedule.getType() == WorkType.WORK) {
                    workDays++;
                } else if (schedule.getType() == WorkType.HOLIDAY) {
                    holidayDays++;
                }
                // 月のステータスを決定するロジック
                // 例: 1つでもREJECTEDがあればREJECTED、全てAPPROVEDならAPPROVED、それ以外はPENDING
                if (schedule.getStatus() == RequestStatus.REJECTED) {
                    status = RequestStatus.REJECTED;
                    // 承認者名もここで設定するロジックが必要（例: schedule.getApproverName()があれば）
                    // 現在のScheduleエンティティにはapproverNameがないため、仮の値
                    approverName = "管理者B"; // 仮の承認者名
                    break; // REJECTEDが見つかったらそれ以上チェックする必要はない
                } else if (schedule.getStatus() == RequestStatus.APPROVED) {
                    status = RequestStatus.APPROVED;
                    approverName = "管理者A"; // 仮の承認者名
                }
                // 提出日時を、実際に提出された日時を記録するフィールドがあればそれを使用
                // 現在のScheduleエンティティにはsubmittedAtがないため、ここでは最も古い日付を提出日と仮定
                if (submittedAt == null || schedule.getDate().isBefore(LocalDate.parse(submittedAt))) {
                    submittedAt = schedule.getDate().toString();
                }
            }

            // 提出日時が設定されていない場合、月の最初の日を仮の提出日時とする
            if (submittedAt == null) {
                submittedAt = month + "-01";
            }

            // id は、月ごとの提出履歴を一意に識別できるものが必要
            String id = userId + "-" + month;

            submittedSchedules.add(new SubmittedScheduleResponseDto(
                id,
                month,
                submittedAt,
                status.toString().toLowerCase(), // フロントエンドは小文字を期待
                approverName,
                workDays,
                holidayDays,
                userId
            ));
        });

        // 最新の提出が上位に来るようにソート (月を降順にソート)
        submittedSchedules.sort((s1, s2) -> s2.getMonth().compareTo(s1.getMonth()));

        return submittedSchedules;
    }
}
