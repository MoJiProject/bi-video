package com.qingmang.interfaces.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
public class UserLibraryQuery {
    @Min(1)
    private Long cursorId;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime cursorTime;

    @Size(max = 100)
    private String keyword;

    @Min(1)
    @Max(100)
    private int size = 20;

    @Pattern(regexp = "newest|oldest")
    private String order = "newest";

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime startTime;

    /** Exclusive upper bound: the day after the selected end date, at midnight. */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime endTime;
}
