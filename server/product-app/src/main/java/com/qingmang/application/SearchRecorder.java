package com.qingmang.application;

import com.qingmang.infrastructure.mapper.SearchStatMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 搜索词计数。
 *
 * <p>单独一个 bean 是有原因的：{@code @CacheEvict} 靠 Spring 代理生效，
 * 如果记录逻辑写在 SearchService 内部由 search() 直接调用，会走 this 调用绕过代理，
 * 热榜缓存永远不失效。</p>
 */
@Service
public class SearchRecorder {

    private final SearchStatMapper statMapper;

    public SearchRecorder(SearchStatMapper statMapper) {
        this.statMapper = statMapper;
    }

    @Transactional
    @CacheEvict(cacheNames = "hotKeyword", allEntries = true)
    public void record(String rawKeyword) {
        String keyword = rawKeyword.length() > 64 ? rawKeyword.substring(0, 64) : rawKeyword;
        LocalDateTime now = LocalDateTime.now();

        if (statMapper.bumpTotal(keyword, now) == 0) {
            try {
                statMapper.insertTotal(keyword, now);
            } catch (DuplicateKeyException ignored) {
                statMapper.bumpTotal(keyword, now);
            }
        }

        LocalDate today = LocalDate.now();
        if (statMapper.bumpDaily(today, keyword) == 0) {
            try {
                statMapper.insertDaily(today, keyword);
            } catch (DuplicateKeyException ignored) {
                statMapper.bumpDaily(today, keyword);
            }
        }
    }
}