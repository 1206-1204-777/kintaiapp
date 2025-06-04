package com.example.kinntai.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "company_holidays")
@Data
@NoArgsConstructor
public class CompanyHoliday {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate holidayDate; // 休日日付

    @Column(nullable = false, length = 100)
    private String holidayName; // 休日名 (例: 創立記念日)

    @ManyToOne(fetch = FetchType.EAGER) // 登録ユーザー (管理者など)
    @JoinColumn(name = "created_by_user_id", nullable = false)
    private User createdByUser;

    @Column(nullable = false)
    private LocalDateTime createdAt; // 登録日時

    private LocalDateTime updatedAt; // 更新日時

    // コンストラクタ (必要な場合にLombokの@AllArgsConstructorと併用)
    public CompanyHoliday(LocalDate holidayDate, String holidayName, User createdByUser, LocalDateTime createdAt) {
        this.holidayDate = holidayDate;
        this.holidayName = holidayName;
        this.createdByUser = createdByUser;
        this.createdAt = createdAt;
    }
}