package com.qingmang.interfaces.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class UserLibraryBatchRequest {
    @NotEmpty
    @Size(max = 1000)
    private List<@NotNull @Positive Long> videoIds;
}
