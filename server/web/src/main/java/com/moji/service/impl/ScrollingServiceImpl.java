package com.moji.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.moji.mapper.ScrollingMapper;
import com.moji.mapper.UserMapper;
import com.moji.mapper.VideosMapper;
import com.moji.po.Scrolling;
import com.moji.po.Videos;
import com.moji.serve.LoginLimiterServer;
import com.moji.service.ScrollingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class ScrollingServiceImpl extends ServiceImpl<ScrollingMapper, Scrolling> implements ScrollingService {


    @Autowired
    private VideosMapper videosMapper;

@Autowired
    private ScrollingMapper scrollingMapper;

    //弹幕纵向位置存百分比(0~100)，小屏和全屏各存一份：
    //  top             小屏用的位置
    //  all_display_top 全屏用的位置
    //
    //两者来自同一个行号，按各自轨道的行数换算，所以同一条弹幕
    //在小屏占的行数少、位置比例却和大屏对齐。
    //
    //全屏 26 行轨道：顶部占上半 13 行(0~50%)，底部占下半 13 行(50~100%)，滚动占满 26 行(0~100%)
    //小屏 12 行轨道：顶部占上半  6 行(0~50%)，底部占下半  6 行(50~100%)，滚动占满 12 行(0~100%)
    //小屏行数减半但区间划分不变，所以放大到全屏时相对位置不会跑偏。
    //
    //底部弹幕压在轨道下半部分，小屏从第 6 行起、全屏从第 13 行起
    private static final int FULL_HALF_SLOTS = 13;
    private static final int SMALL_HALF_SLOTS = 6;
    private static final int FULL_ROLL_SLOTS = 26;
    private static final int SMALL_ROLL_SLOTS = 12;
    //顶部/底部各占一半高度，滚动占满整条轨道
    private static final int HALF_PERCENT = 50;
    //判定两条弹幕是否同时显示的时间窗口（秒）
    private static final double COLLISION_SECONDS = 5;

    //该类型在两种轨道里的行数
    private static int fullSlots(int location) {
        return location == 1 ? FULL_ROLL_SLOTS : FULL_HALF_SLOTS;
    }

    private static int smallSlots(int location) {
        return location == 1 ? SMALL_ROLL_SLOTS : SMALL_HALF_SLOTS;
    }

    //百分比区间起点：底部从 50% 开始
    private static int percentBase(int location) {
        return location == 3 ? HALF_PERCENT : 0;
    }

    //百分比区间长度：顶部和底部各占一半，滚动占满
    private static int percentSpan(int location) {
        return location == 1 ? 100 : HALF_PERCENT;
    }

    //全屏行号换算成小屏行号，小屏行数更少，按比例压缩到区间两端
    private static int fullRowToSmallRow(int fullRow, int location) {
        int full = fullSlots(location);
        int small = smallSlots(location);
        return (int) Math.round(fullRow * (double) (small - 1) / (full - 1));
    }

    //小屏行号换算成该小屏行对应的全部全屏行号，
    //压缩不是一一对应的(多个全屏行会落到同一个小屏行)
    private static List<Integer> fullRowsOfSmallRow(int smallRow, int location) {
        int full = fullSlots(location);
        List<Integer> matched = new ArrayList<>();
        for (int row = 0; row < full; row++) {
            if (fullRowToSmallRow(row, location) == smallRow) {
                matched.add(row);
            }
        }
        if (matched.isEmpty()) {
            matched.add(Math.min(smallRow, full - 1));
        }
        return matched;
    }

    //全屏百分比还原成全屏行号
    private static int percentToFullRow(int percent, int location) {
        int offset = percent - percentBase(location);
        int row = (int) Math.round(offset * (double) fullSlots(location) / percentSpan(location));
        return Math.min(Math.max(row, 0), fullSlots(location) - 1);
    }

    //小屏行号换算成小屏轨道的百分比
    private static int smallRowToPercent(int smallRow, int location) {
        return percentBase(location) + (int) Math.round(smallRow * (double) percentSpan(location) / smallSlots(location));
    }

    //全屏行号换算成全屏轨道的百分比
    private static int fullRowToPercent(int fullRow, int location) {
        return percentBase(location) + (int) Math.round(fullRow * (double) percentSpan(location) / fullSlots(location));
    }

    @Override
    @Transactional
    public Boolean insterScrolling(Scrolling scrolling) {

        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedDateTime = now.format(formatter);
        scrolling.setSendTime(formattedDateTime);

        Integer location = scrolling.getLocation();
        if (location == null || location < 1 || location > 3) {
            location = 1;
            scrolling.setLocation(location);
        }

        Random random = new Random();
        int smallRow;
        int fullRow;
        if (location == 2 || location == 3) {
            //顶部和底部：先把还没被用过的全屏行填满，13 行都用过了才退回随机
            int[] rows = pickTopBottomRows(scrolling, location, random);
            smallRow = rows[0];
            fullRow = rows[1];
        } else {
            //滚动弹幕保持原样：先挑一个不与同时段弹幕冲突的小屏行，再映射回全屏行
            smallRow = nextFreeSmallRow(scrolling, location, random);
            List<Integer> fullRows = fullRowsOfSmallRow(smallRow, location);
            fullRow = fullRows.get(random.nextInt(fullRows.size()));
        }

        scrolling.setTop(smallRowToPercent(smallRow, location));
        scrolling.setAllDisplayTop(fullRowToPercent(fullRow, location));

        int insert = scrollingMapper.insert(scrolling);

        Videos videos = videosMapper.selectById(scrolling.getVideoId());
        videos.setScrollingNumber(videos.getScrollingNumber()+1);
        int i = videosMapper.updateById(videos);

        return i > 0 && insert > 0;
    }

    /**
     * 在该类型的小屏行号范围内挑一个与同时段已有弹幕不冲突的行。
     * 冲突判断以小屏为准：小屏行数最少，只要小屏不冲突，全屏铺得更开也不会冲突。
     *
     * @param scrolling  待插入的弹幕
     * @param location   弹幕类型
     * @param random     随机源
     * @return 选中的小屏行号
     */
    private int nextFreeSmallRow(Scrolling scrolling, int location, Random random) {
        List<Scrolling> nearby = nearbyScrollings(scrolling);

        List<Integer> candidates = new ArrayList<>();
        for (int row = 0; row < smallSlots(location); row++) {
            candidates.add(row);
        }
        for (Scrolling exist : nearby) {
            Integer stored = exist.getTop();
            if (stored == null) {
                continue;
            }
            candidates.remove(Integer.valueOf(percentToSmallRow(stored, location)));
        }

        if (candidates.isEmpty()) {
            return random.nextInt(smallSlots(location));
        }
        return candidates.get(random.nextInt(candidates.size()));
    }

    //小屏百分比还原成小屏行号
    private static int percentToSmallRow(int percent, int location) {
        int offset = percent - percentBase(location);
        int row = (int) Math.round(offset * (double) smallSlots(location) / percentSpan(location));
        return Math.min(Math.max(row, 0), smallSlots(location) - 1);
    }

    /**
     * 顶部/底部弹幕的选行。填充顺序：先把小屏 6 行填满，再把全屏剩下 13 行填满。
     *
     * 全屏有 13 行、小屏只有 6 行，同一瞬间全屏能放 13 条不叠字，
     * 小屏超过 6 条就会被压到同一行上。所以先把小屏的 6 行各占一条，
     * 这样小屏始终不叠字；之后再把全屏剩下的行慢慢填满。
     *
     * 优先级(从高到低)：
     * 1. 全屏行未被同时段弹幕占用，且全屏行没用过、对应的小屏行也没用过
     * 2. 全屏行未被占用，且全屏行没用过
     * 3. 全屏行未被占用
     * 4. 全部行（同时段弹幕已经把 13 行占满，只能随机硬挤）
     *
     * @return {小屏行号, 全屏行号}
     */
    private int[] pickTopBottomRows(Scrolling scrolling, int location, Random random) {
        List<Scrolling> nearby = nearbyScrollings(scrolling);
        int fullTotal = fullSlots(location);

        //同时段已经占住的全屏行
        boolean[] fullBusy = new boolean[fullTotal];
        for (Scrolling exist : nearby) {
            Integer stored = exist.getAllDisplayTop();
            if (stored == null) {
                continue;
            }
            int row = percentToFullRow(stored, location);
            if (row >= 0 && row < fullTotal) {
                fullBusy[row] = true;
            }
        }

        //这个视频下这种类型已经用过的行号
        boolean[][] used = usedRows(scrolling, location);
        boolean[] fullUsed = used[0];
        boolean[] smallUsed = used[1];

        List<Integer> fillSmallFirst = new ArrayList<>();
        List<Integer> fillFull = new ArrayList<>();
        List<Integer> freeRows = new ArrayList<>();
        for (int row = 0; row < fullTotal; row++) {
            if (fullBusy[row]) {
                continue;
            }
            freeRows.add(row);
            if (!fullUsed[row]) {
                fillFull.add(row);
                if (!smallUsed[fullRowToSmallRow(row, location)]) {
                    fillSmallFirst.add(row);
                }
            }
        }

        List<Integer> candidates;
        if (!fillSmallFirst.isEmpty()) {
            candidates = fillSmallFirst;
        } else if (!fillFull.isEmpty()) {
            candidates = fillFull;
        } else if (!freeRows.isEmpty()) {
            candidates = freeRows;
        } else {
            candidates = new ArrayList<>();
            for (int row = 0; row < fullTotal; row++) {
                candidates.add(row);
            }
        }

        int fullRow = candidates.get(random.nextInt(candidates.size()));
        return new int[]{fullRowToSmallRow(fullRow, location), fullRow};
    }

    //同时段(±COLLISION_SECONDS)内同视频同类型的弹幕
    private List<Scrolling> nearbyScrollings(Scrolling scrolling) {
        LambdaQueryWrapper<Scrolling> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Scrolling::getVideoId, scrolling.getVideoId())
                .eq(Scrolling::getLocation, scrolling.getLocation())
                .between(Scrolling::getVideoTime,
                        scrolling.getVideoTime() - COLLISION_SECONDS,
                        scrolling.getVideoTime() + COLLISION_SECONDS);
        return scrollingMapper.selectList(wrapper);
    }

    //这个视频下这种类型已经用过的行号，返回 {全屏行是否用过, 小屏行是否用过}
    private boolean[][] usedRows(Scrolling scrolling, int location) {
        int fullTotal = fullSlots(location);
        int smallTotal = smallSlots(location);

        LambdaQueryWrapper<Scrolling> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(Scrolling::getTop, Scrolling::getAllDisplayTop)
                .eq(Scrolling::getVideoId, scrolling.getVideoId())
                .eq(Scrolling::getLocation, location);
        List<Scrolling> existed = scrollingMapper.selectList(wrapper);

        boolean[] fullUsed = new boolean[fullTotal];
        boolean[] smallUsed = new boolean[smallTotal];
        for (Scrolling exist : existed) {
            Integer top = exist.getTop();
            if (top != null) {
                int row = percentToSmallRow(top, location);
                if (row >= 0 && row < smallTotal) {
                    smallUsed[row] = true;
                }
            }
            Integer full = exist.getAllDisplayTop();
            if (full != null) {
                int row = percentToFullRow(full, location);
                if (row >= 0 && row < fullTotal) {
                    fullUsed[row] = true;
                }
            }
        }
        return new boolean[][]{fullUsed, smallUsed};
    }

    @Override
    public List<Scrolling> selectScrollingList(Integer videoId) {

        LambdaQueryWrapper<Scrolling> scrollingLambdaQueryWrapper=new LambdaQueryWrapper<>();
        scrollingLambdaQueryWrapper.eq(Scrolling::getVideoId,videoId);

        return scrollingMapper.selectList(scrollingLambdaQueryWrapper);
    }

    @Override
    @Transactional
    public Boolean revocationScrolling(Integer scrollingId, String token) {

        Scrolling scrolling = scrollingMapper.selectById(scrollingId);

        if(scrolling!=null) {

            //判断token的用户和发送的弹幕用户是否一致
            LoginLimiterServer limiterServer=new LoginLimiterServer();
            if(!limiterServer.checkUser(scrolling.getUserId(),token))
                return false;

            Videos videos = videosMapper.selectById(scrolling.getVideoId());
            videos.setScrollingNumber(videos.getScrollingNumber() - 1);
            int i = videosMapper.updateById(videos);
            int i1 = scrollingMapper.deleteById(scrollingId);
            return i > 0 && i1 > 0;
        }
        else return false;
    }
}
