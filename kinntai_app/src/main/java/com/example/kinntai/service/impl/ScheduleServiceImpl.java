package com.example.kinntai.service.impl;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.kinntai.dto.ScheduleRequestDto;
import com.example.kinntai.dto.SubmittedScheduleResponseDto; // 追加
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
    public void saveSchedule(ScheduleRequestDto request) {
        // 既存のスケジュールを削除してから保存することで、月のスケジュールを上書きする
        // ただし、ステータスがPENDINGのもののみを対象とするなど、より複雑なロジックが必要な場合もある
        // ここでは、指定されたuserIdと月の既存スケジュールを全て削除するシンプルな実装
        // まず、リクエストに含まれる日付の月を特定
        if (request.getDays() != null && !request.getDays().isEmpty()) {
            LocalDate firstDateOfMonth = request.getDays().get(0).getDate().withDayOfMonth(1);
            LocalDate lastDateOfMonth = firstDateOfMonth.withDayOfMonth(firstDateOfMonth.lengthOfMonth());

            // 既存のスケジュールを削除
            scheduleRepository.deleteByUserIdAndDateBetween(request.getUserId(), firstDateOfMonth, lastDateOfMonth);
        }

        List<Schedule> schedules = new ArrayList<>();
        for (ScheduleRequestDto.ScheduleDayDto day : request.getDays()) {
            Schedule schedule = new Schedule();
            schedule.setUserId(request.getUserId());
            schedule.setDate(day.getDate());
            schedule.setType(WorkType.valueOf(day.getType()));
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
    public void approveSchedule(Long scheduleId) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
            .orElseThrow(() -> new RuntimeException("Schedule not found"));
        schedule.setStatus(RequestStatus.APPROVED);
        scheduleRepository.save(schedule);
    }

    @Override
    public List<SubmittedScheduleResponseDto> getSubmittedSchedules(Long userId) {
        // 特定のユーザーの全てのスケジュールを取得
        // 実際には、提出された月のスケジュールのみを対象とするのが一般的
        List<Schedule> allUserSchedules = scheduleRepository.findByUserId(userId);

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
            String submittedAt = LocalDate.now().toString(); // 仮の提出日時
            String approverName = "-"; // 仮の承認者名

            // 月内のスケジュールを集計
            for (Schedule schedule : schedulesInMonth) {
                if (schedule.getType() == WorkType.WORK) {
                    workDays++;
                } else if (schedule.getType() == WorkType.HOLIDAY) {
                    holidayDays++;
                }
                // 月のステータスを決定（例: 一つでもREJECTEDがあればREJECTED、全てAPPROVEDならAPPROVEDなど）
                // ここでは最も新しいステータスを採用する、あるいは最も優先度の高いステータスを採用するロジックが必要
                // 簡単のため、ここではPENDING以外のステータスがあればそれを優先する
                if (schedule.getStatus() == RequestStatus.APPROVED) {
                    status = RequestStatus.APPROVED;
                    // 承認者名もここで設定するロジックが必要（例: schedule.getApproverName()があれば）
                    // 現在のScheduleエンティティにはapproverNameがないため、仮の値
                    approverName = "管理者A"; // 仮の承認者名
                } else if (schedule.getStatus() == RequestStatus.REJECTED) {
                    status = RequestStatus.REJECTED;
                    approverName = "管理者B"; // 仮の承認者名
                }
                // 提出日時も、実際に提出された日時を記録するフィールドがあればそれを使用
                // submittedAt = schedule.getSubmittedAt() != null ? schedule.getSubmittedAt().toString() : submittedAt;
            }

            // id は、月ごとの提出履歴を一意に識別できるものが必要
            // ここでは userId と month を組み合わせた文字列を仮のIDとする
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
