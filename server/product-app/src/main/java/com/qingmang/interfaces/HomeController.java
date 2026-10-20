package com.qingmang.interfaces;

import com.qingmang.application.HomeService;
import com.qingmang.common.api.ApiResponse;
import com.qingmang.common.api.PageResult;
import com.qingmang.interfaces.dto.HomeQueryRequest;
import com.qingmang.interfaces.vo.FolderVO;
import com.qingmang.interfaces.vo.HomeImageItemVO;
import com.qingmang.interfaces.vo.PostVO;
import com.qingmang.interfaces.vo.UserBriefVO;
import com.qingmang.interfaces.vo.VideoListItemVO;
import com.qingmang.support.AuthContext;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/home")
public class HomeController {
    private final HomeService service;
    public HomeController(HomeService service) { this.service = service; }

    @GetMapping("/contribute")
    public ApiResponse<PageResult<VideoListItemVO>> videos(@Valid @ModelAttribute HomeQueryRequest q) {
        return ApiResponse.ok(service.videos(q, AuthContext.currentUserIdOrNull()));
    }

    @GetMapping("/love")
    public ApiResponse<List<VideoListItemVO>> likes(@Valid @ModelAttribute HomeQueryRequest q) {
        return ApiResponse.ok(service.reactions(q.getHomeUserId(), AuthContext.currentUserIdOrNull(), 1));
    }

    @GetMapping("/throwCoin")
    public ApiResponse<List<VideoListItemVO>> coins(@Valid @ModelAttribute HomeQueryRequest q) {
        return ApiResponse.ok(service.reactions(q.getHomeUserId(), AuthContext.currentUserIdOrNull(), 2));
    }

    @GetMapping("/collectsClassify")
    public ApiResponse<List<FolderVO>> folders(@Valid @ModelAttribute HomeQueryRequest q) {
        return ApiResponse.ok(service.folders(q.getHomeUserId(), AuthContext.currentUserIdOrNull()));
    }

    @GetMapping("/collectByName")
    public ApiResponse<PageResult<VideoListItemVO>> folderVideos(@Valid @ModelAttribute HomeQueryRequest q) {
        return ApiResponse.ok(service.folderVideos(q, AuthContext.currentUserIdOrNull()));
    }

    @GetMapping("/dynamic")
    public ApiResponse<PageResult<PostVO>> posts(@Valid @ModelAttribute HomeQueryRequest q) {
        return ApiResponse.ok(service.posts(q, AuthContext.currentUserIdOrNull()));
    }

    @GetMapping("/dynamicImg")
    public ApiResponse<PageResult<HomeImageItemVO>> images(@Valid @ModelAttribute HomeQueryRequest q) {
        return ApiResponse.ok(service.images(q, AuthContext.currentUserIdOrNull()));
    }

    @GetMapping("/relations")
    public ApiResponse<PageResult<UserBriefVO>> relations(@RequestParam Long homeUserId,
            @RequestParam int type, @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(required = false) String keyWord) {
        return ApiResponse.ok(service.relations(homeUserId, AuthContext.currentUserIdOrNull(), type, pageNum, keyWord));
    }
}
