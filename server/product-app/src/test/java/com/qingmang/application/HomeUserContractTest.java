package com.qingmang.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qingmang.domain.favorite.mapper.FavoriteFolderMapper;
import com.qingmang.domain.social.UserFollow;
import com.qingmang.domain.social.mapper.*;
import com.qingmang.domain.user.mapper.*;
import com.qingmang.infrastructure.mapper.*;
import com.qingmang.interfaces.vo.UserProfileVO;
import com.qingmang.support.AuthContext;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DuplicateKeyException;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class HomeUserContractTest {
    @Test
    void publicProfilesDoNotExposeBirthdayOrCoinBalance() {
        UserQueryMapper query = mock(UserQueryMapper.class);
        UserProfileVO profile = new UserProfileVO();
        profile.setBirthday(LocalDate.of(2000, 1, 1));
        profile.setCoinBalance(40);
        when(query.selectProfile(7L, null)).thenReturn(profile);
        AuthService service = new AuthService(mock(UserMapper.class), mock(UserPrivacyMapper.class),
                mock(UserStatsMapper.class), mock(FavoriteFolderMapper.class), query, mock(CounterMapper.class));
        try (var context = mockStatic(AuthContext.class)) {
            context.when(AuthContext::currentUserIdOrNull).thenReturn(null);
            UserProfileVO result = service.profile(7L);
            assertNull(result.getBirthday());
            assertNull(result.getCoinBalance());
            assertFalse(result.getIsSelf());
        }
    }

    @Test
    void duplicateFollowAndRepeatedUnfollowDoNotChangeCounters() {
        UserFollowMapper follows = mock(UserFollowMapper.class);
        CounterMapper counters = mock(CounterMapper.class);
        SocialService service = new SocialService(follows, mock(UserPostMapper.class),
                mock(PrivateMessageMapper.class), mock(UserStatsMapper.class), counters,
                mock(SocialQueryMapper.class), new ObjectMapper(), mock(AuthService.class));
        when(follows.insert(any(UserFollow.class))).thenThrow(new DuplicateKeyException("exists"));
        service.setFollowing(7L, 8L, true);
        when(follows.delete(any())).thenReturn(0);
        service.setFollowing(7L, 8L, false);
        verifyNoInteractions(counters);
    }

    @Test
    void changedFollowCountersAreUpdatedExactlyOnce() {
        UserFollowMapper follows = mock(UserFollowMapper.class);
        CounterMapper counters = mock(CounterMapper.class);
        SocialService service = new SocialService(follows, mock(UserPostMapper.class),
                mock(PrivateMessageMapper.class), mock(UserStatsMapper.class), counters,
                mock(SocialQueryMapper.class), new ObjectMapper(), mock(AuthService.class));
        when(follows.insert(any(UserFollow.class))).thenReturn(1);
        service.setFollowing(7L, 8L, true);
        verify(counters).increaseUserStat(8L, "follower_count", 1);
        verify(counters).increaseUserStat(7L, "following_count", 1);
    }
}
