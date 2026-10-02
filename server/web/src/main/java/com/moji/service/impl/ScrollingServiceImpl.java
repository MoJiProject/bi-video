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

    //弹幕轨道以百分比存储，与播放器实际分辨率无关，前端按视频高度均分成行
    //小屏(未全屏)轨道共 13 行：顶部弹幕 0~12 行，滚动弹幕 0~12 行随机，底部弹幕不显示
    private static final int SMALL_ROWS = 13;
    //全屏轨道共 26 行：顶部弹幕 0~12 行，滚动弹幕 0~25 行随机，底部弹幕 13~25 行
    private static final int FULL_ROWS = 26;
    //顶部弹幕在两种模式下都占用轨道上方 13 行
    private static final int TOP_ROW_MAX = 12;
    //全屏下底部弹幕占用轨道下方的 13 行(第 13~25 行)
    private static final int FULL_BOTTOM_ROW_MIN = 13;
    //判定两条弹幕是否同时显示的时间窗口（秒）
    private static final double COLLISION_SECONDS = 5;

    //行号换算成轨道百分比，行高 = 100% / 该模式的行数
    private static int rowToPercent(int row, int totalRows) {
        return (int) Math.round(row * 100.0 / totalRows);
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
        //小屏行号，小屏共 13 行
        int smallRow;
        //全屏行号，全屏共 26 行
        int fullRow;
        if (location == 1) {
            //滚动弹幕在整个轨道内随机取一行
            smallRow = random.nextInt(SMALL_ROWS);
            fullRow = random.nextInt(FULL_ROWS);
        } else if (location == 2) {
            smallRow = nextFreeRow(scrolling, 0, TOP_ROW_MAX, SMALL_ROWS, random);
            fullRow = nextFreeRow(scrolling, 0, TOP_ROW_MAX, FULL_ROWS, random);
        } else {
            //底部弹幕小屏不显示，行号给 0 占位，避免出现负值
            smallRow = 0;
            fullRow = nextFreeRow(scrolling, FULL_BOTTOM_ROW_MIN, FULL_ROWS - 1, FULL_ROWS, random);
        }

        scrolling.setTop(rowToPercent(smallRow, SMALL_ROWS));
        scrolling.setAllDisplayTop(rowToPercent(fullRow, FULL_ROWS));

        int insert = scrollingMapper.insert(scrolling);

        Videos videos = videosMapper.selectById(scrolling.getVideoId());
        videos.setScrollingNumber(videos.getScrollingNumber()+1);
        int i = videosMapper.updateById(videos);

        return i > 0 && insert > 0;
    }

    /**
     * 在指定行范围内挑选一个与同时段已有弹幕不冲突的行号。
     * 小屏和全屏行数不同，各自按对应网格挑选并换算成百分比，
     * 前端再按容器高度均分成行，因此不同分辨率下行距一致。
     *
     * @param scrolling  待插入的弹幕
     * @param minRow     允许的最小行号
     * @param maxRow     允许的最大行号
     * @param totalRows  该模式的轨道总行数
     * @param random     随机源
     * @return 选中的行号
     */
    private int nextFreeRow(Scrolling scrolling, int minRow, int maxRow, int totalRows, Random random) {
        LambdaQueryWrapper<Scrolling> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Scrolling::getVideoId, scrolling.getVideoId())
                .eq(Scrolling::getLocation, scrolling.getLocation())
                .between(Scrolling::getVideoTime,
                        scrolling.getVideoTime() - COLLISION_SECONDS,
                        scrolling.getVideoTime() + COLLISION_SECONDS);
        List<Scrolling> nearby = scrollingMapper.selectList(wrapper);

        List<Integer> candidates = new ArrayList<>();
        for (int row = minRow; row <= maxRow; row++) {
            candidates.add(row);
        }
        //已存在弹幕记录的是它自己模式的百分比，按本次网格还原行号才有可比性
        boolean fullDisplay = totalRows == FULL_ROWS;
        for (Scrolling exist : nearby) {
            Integer stored = fullDisplay ? exist.getAllDisplayTop() : exist.getTop();
            if (stored == null) {
                continue;
            }
            int row = percentToRow(stored, totalRows);
            candidates.remove(Integer.valueOf(row));
        }

        if (candidates.isEmpty()) {
            return minRow + random.nextInt(maxRow - minRow + 1);
        }
        return candidates.get(random.nextInt(candidates.size()));
    }

    //轨道百分比还原成行号
    private static int percentToRow(int percent, int totalRows) {
        return (int) Math.round(percent * totalRows / 100.0);
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
