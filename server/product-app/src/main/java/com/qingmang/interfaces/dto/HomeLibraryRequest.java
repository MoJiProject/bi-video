package com.qingmang.interfaces.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Data;

@Data
public class HomeLibraryRequest {
    @NotNull
    @Pattern(regexp = "delete|copy|move")
    private String operation;
    @NotEmpty
    @Size(max = 100)
    private List<@NotNull @Positive Long> videoIds;
    @Positive
    private Long targetFolderId;
}
