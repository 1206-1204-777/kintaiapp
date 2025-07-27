package com.example.kinntai.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import com.fasterxml.jackson.annotation.JsonFormat; // JsonFormatをインポート

import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "personal_holidays")
@Data
@NoArgsConstructor
public class PersonalHoliday {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne // 個人休日は特定のユーザーに紐づく
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // 申請ユーザー

    @Column(nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd") // ★追加: 日付フォーマットを指定
    private LocalDate holidayDate; // 休日日付

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private HolidayType holidayType; // 休日種別 (有給、特別休暇など)

    @Column(length = 500) // 理由
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequestStatus status; // 申請ステータス (申請中、承認済、却下済)

    @ManyToOne // 承認者
    @JoinColumn(name = "approver_id") // 承認者がいない場合はnullを許容
    private User approver;

    @Column(nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") // ★追加: 申請日時にもフォーマットを指定
    private LocalDateTime createdAt; // 申請日時

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") // ★追加: 更新日時にもフォーマットを指定
    private LocalDateTime updatedAt; // 更新日時

    // コンストラクタ (必要な場合にLombokの@AllArgsConstructorと併用)
    public PersonalHoliday(User user, LocalDate holidayDate, HolidayType holidayType, String reason, RequestStatus status, LocalDateTime createdAt) {
        this.user = user;
        this.holidayDate = holidayDate;
        this.holidayType = holidayType;
        this.reason = reason;
        this.status = status;
        this.createdAt = createdAt;
    }
}
