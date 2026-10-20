package com.qingmang.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qingmang.common.exception.BizException;
import com.qingmang.common.exception.ErrorCode;
import com.qingmang.domain.user.UserPrivacy;
import com.qingmang.domain.user.mapper.UserPrivacyMapper;
import com.qingmang.infrastructure.mapper.HomeQueryMapper;
import com.qingmang.infrastructure.mapper.UserContentQueryMapper;
import com.qingmang.interfaces.dto.HomeQueryRequest;
import com.qingmang.interfaces.vo.FolderVO;
import com.qingmang.interfaces.vo.HomeImageItemVO;
import com.qingmang.interfaces.vo.PostVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class HomeServiceTest {
    private AuthService auth;
    private UserPrivacyMapper privacy;
    private UserContentQueryMapper content;
    private HomeQueryMapper query;
    private HomeService service;

    @BeforeEach
    void setUp() {
        auth = mock(AuthService.class);
        privacy = mock(UserPrivacyMapper.class);
        content = mock(UserContentQueryMapper.class);
        query = mock(HomeQueryMapper.class);
        service = new HomeService(auth, privacy, content, query, new ObjectMapper());
    }

    @Test
    void publicFoldersExcludePrivateFolders() {
        when(privacy.selectOne(any())).thenReturn(new UserPrivacy().setShowFavoriteList(true));
        FolderVO visible = folder(1L, 1);
        when(content.selectFolders(7L)).thenReturn(List.of(visible, folder(2L, 0)));
        assertEquals(List.of(visible), service.folders(7L, null));
    }

    @Test
    void hiddenListsCannotBeOpenedBySpoofingViewerParameters() {
        when(privacy.selectOne(any())).thenReturn(new UserPrivacy().setShowFavoriteList(false));
        BizException error = assertThrows(BizException.class, () -> service.folders(7L, 9L));
        assertEquals(ErrorCode.NO_PERMISSION.getCode(), error.getCode());
        verifyNoInteractions(content);
    }

    @Test
    void ownerCanReadPrivateFoldersWithoutPublicPrivacy() {
        when(content.selectFolders(7L)).thenReturn(List.of(folder(1L, 0)));
        assertEquals(1, service.folders(7L, 7L).size());
        verifyNoInteractions(privacy);
    }

    @Test
    void receivedCoinVisibilityDoesNotPublishGivenCoinHistory() {
        assertThrows(BizException.class, () -> service.reactions(7L, null, 2));
        assertThrows(BizException.class, () -> service.reactions(7L, 8L, 1));
        verifyNoInteractions(query);
    }

    @Test
    void contributionPaginationPreservesTotalAndOffset() {
        HomeQueryRequest request = request();
        request.setPageNum(3);
        when(query.videos(request, null, 40)).thenReturn(List.of());
        when(query.videoCount(request)).thenReturn(57L);
        var page = service.videos(request, null);
        assertEquals(57, page.getTotal());
        assertEquals(3, page.getPages());
        verify(query).videos(request, null, 40);
    }

    @Test
    void postsDecodeImagesAndOnlyOwnerCanDelete() {
        HomeQueryRequest request = request();
        PostVO post = new PostVO();
        post.setAuthorId(7L);
        post.setImageUrls("[\"image.png\"]");
        when(query.posts(request, null, 0)).thenReturn(List.of(post));
        service.posts(request, null);
        assertEquals(List.of("image.png"), post.getImageList());
        assertFalse(post.getIsSelf());
        assertNull(post.getImageUrls());
    }

    @Test
    void imageCommentsNeverBecomeDeletablePosts() {
        HomeQueryRequest request = request();
        HomeImageItemVO comment = new HomeImageItemVO();
        comment.setId(1L);
        comment.setAuthorId(7L);
        comment.setSourceType(2);
        when(query.images(request, 7L, 0)).thenReturn(List.of(comment));
        service.images(request, 7L);
        assertFalse(comment.getIsSelf());
    }

    @Test
    void publicFansUseTargetPrivacyAndTwentyFourItemPages() {
        when(privacy.selectOne(any())).thenReturn(new UserPrivacy().setShowFansList(true));
        when(query.relations(7L, null, true, "name", 24)).thenReturn(List.of());
        when(query.relationCount(7L, true, "name")).thenReturn(30L);
        var page = service.relations(7L, null, 9, 2, "name");
        assertEquals(24, page.getPageSize());
        assertEquals(30, page.getTotal());
        verify(query).relations(7L, null, true, "name", 24);
    }

    private HomeQueryRequest request() {
        HomeQueryRequest request = new HomeQueryRequest();
        request.setHomeUserId(7L);
        return request;
    }

    private FolderVO folder(Long id, int visibility) {
        FolderVO folder = new FolderVO();
        folder.setId(id);
        folder.setOwnerId(7L);
        folder.setVisibility(visibility);
        return folder;
    }
}
