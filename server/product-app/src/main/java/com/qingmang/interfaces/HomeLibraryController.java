package com.qingmang.interfaces;

import com.qingmang.application.HomeLibraryService;
import com.qingmang.common.api.ApiResponse;
import com.qingmang.interfaces.dto.HomeLibraryRequest;
import com.qingmang.support.AuthContext;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/home/folders")
public class HomeLibraryController {
    private final HomeLibraryService service;

    public HomeLibraryController(HomeLibraryService service) { this.service = service; }

    @PostMapping("/{folderId}/batch")
    public ApiResponse<Integer> batch(@PathVariable Long folderId, @Valid @RequestBody HomeLibraryRequest request) {
        return ApiResponse.ok(service.batch(AuthContext.requireUserId(), folderId, request));
    }

    @DeleteMapping("/{folderId}/invalid")
    public ApiResponse<Integer> clearInvalid(@PathVariable Long folderId) {
        return ApiResponse.ok(service.clearInvalid(AuthContext.requireUserId(), folderId));
    }
}
