package com.qingmang.interfaces.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PublishingRequest {
    private Long id;
    @Size(max = 100)
    private java.util.List<@jakarta.validation.constraints.Positive Long> mentionUserIds;
    @NotBlank @Size(max = 200)
    private String title;
    @NotBlank @Size(min = 5, max = 10000)
    private String content;
    @Size(max = 100000)
    private String contentHtml;
    @NotBlank @Size(max = 32)
    private String subZoneKey;
    @Size(max = 64)
    private String subZoneValue;
    @NotNull @Min(0) @Max(1)
    private Integer videoSource;
    @Size(max = 1024)
    private String remoteUrl;
    @NotNull @Min(0) @Max(1)
    private Integer type;
    @NotNull @Min(0) @Max(1)
    private Integer allowTwo;
    @NotBlank @Pattern(regexp = "\\d{1,6}:\\d{2}")
    private String videoTime;
}
