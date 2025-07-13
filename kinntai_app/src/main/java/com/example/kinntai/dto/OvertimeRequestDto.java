package com.example.kinntai.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OvertimeRequestDto {
    private Long userId;
    private LocalDateTime requestDate;
    private Integer overtimeMinutes;
    private String reason;
}