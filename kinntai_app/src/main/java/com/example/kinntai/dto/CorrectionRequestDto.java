package com.example.kinntai.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CorrectionRequestDto {

    private Long userId;

    @JsonProperty("date")  // フロントの "date" を "targetDate" にマッピング
    private LocalDate targetDate;

    @JsonProperty("startTime")
    private LocalTime requestedClockIn;

    @JsonProperty("endTime")
    private LocalTime requestedClockOut;

    private String reason;
    private String comment; // 任意
}