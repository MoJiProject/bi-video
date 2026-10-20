package com.qingmang.interfaces;

import com.qingmang.application.HomeService;
import com.qingmang.common.api.PageResult;
import com.qingmang.common.exception.GlobalExceptionHandler;
import com.qingmang.interfaces.dto.HomeQueryRequest;
import com.qingmang.interfaces.vo.VideoListItemVO;
import com.qingmang.support.AuthContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.util.List;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

class HomeControllerTest {
    private HomeService service;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        service = mock(HomeService.class);
        mvc = MockMvcBuilders.standaloneSetup(new HomeController(service))
                .setControllerAdvice(new GlobalExceptionHandler()).build();
    }

    @Test
    void contributionRouteMatchesHomeEnvelopeAndNeverTrustsViewerId() throws Exception {
        VideoListItemVO video = new VideoListItemVO();
        video.setId(11L);
        video.setTitle("Test");
        video.setWatchLater(true);
        when(service.videos(any(), isNull())).thenReturn(PageResult.of(List.of(video), 31, 2, 20));
        try (var context = mockStatic(AuthContext.class)) {
            context.when(AuthContext::currentUserIdOrNull).thenReturn(null);
            mvc.perform(get("/api/home/contribute").param("homeUserId", "7").param("userId", "7")
                            .param("sort", "3").param("pageNum", "2").param("keyWord", "Test"))
                    .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(1))
                    .andExpect(jsonPath("$.data.total").value(31))
                    .andExpect(jsonPath("$.data.records[0].id").value(11))
                    .andExpect(jsonPath("$.data.records[0].watchLater").value(true));
        }
        var request = ArgumentCaptor.forClass(HomeQueryRequest.class);
        verify(service).videos(request.capture(), isNull());
        assertEquals(7L, request.getValue().getHomeUserId());
        assertEquals(3, request.getValue().getSort());
        assertEquals(2, request.getValue().getPageNum());
        assertEquals("Test", request.getValue().getKeyWord());
    }

    @Test
    void invalidHomeParametersReturnBusinessValidationErrors() throws Exception {
        mvc.perform(get("/api/home/contribute").param("homeUserId", "7").param("pageNum", "0"))
                .andExpect(jsonPath("$.code").value(10001));
        mvc.perform(get("/api/home/dynamic"))
                .andExpect(jsonPath("$.code").value(10001));
        verifyNoInteractions(service);
    }

    @Test
    void publicRelationsDoNotRequireLoginOrUseLegacyEncryptedPayloads() throws Exception {
        when(service.relations(7L, null, 9, 1, null)).thenReturn(PageResult.empty(1, 24));
        try (var context = mockStatic(AuthContext.class)) {
            context.when(AuthContext::currentUserIdOrNull).thenReturn(null);
            mvc.perform(get("/api/home/relations").param("homeUserId", "7").param("type", "9"))
                    .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(1))
                    .andExpect(jsonPath("$.data.pageSize").value(24))
                    .andExpect(jsonPath("$.data.records").isArray());
        }
    }
}
