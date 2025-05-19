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
import java.util.List;
import java.util.Random;

@Service
public class ScrollingServiceImpl extends ServiceImpl<ScrollingMapper, Scrolling> implements ScrollingService {


    @Autowired
    private VideosMapper videosMapper;

    @Autowired
    private ScrollingMapper scrollingMapper;


    @Override
    @Transactional
    public Boolean insterScrolling(Scrolling scrolling) {

        LambdaQueryWrapper<Scrolling> scrollingLambdaQueryWrapper=new LambdaQueryWrapper<>();
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedDateTime = now.format(formatter);
        scrolling.setSendTime(formattedDateTime);
        Random random = new Random();
        //设置滚动（未进入全屏）0-330px
        if(scrolling.getLocation()==1)
         scrolling.setTop(random.nextInt(330));
        //设置滚动0-750px
        if(scrolling.getLocation()==1)
            scrolling.setAllDisplayTop(random.nextInt(750));

        //设置top（未进入全屏）0-150px +30  进入全屏 0-360px
         else if(scrolling.getLocation()==2){

           scrollingLambdaQueryWrapper.eq(Scrolling::getVideoId,scrolling.getVideoId())
                   .eq(Scrolling::getLocation,2)
                   .orderByDesc(Scrolling::getSendTime);//安照最新的
            List<Scrolling> scrollings = scrollingMapper.selectList(scrollingLambdaQueryWrapper);
            if(!scrollings.isEmpty()){
                for (Scrolling scrolling1 : scrollings) {
                   if(Math.abs(scrolling.getVideoTime()-scrolling1.getVideoTime())<5 && scrolling1.getTop()!=150)
                       scrolling.setTop(scrolling1.getTop()+30);
                   else if(Math.abs(scrolling.getVideoTime()-scrolling1.getVideoTime())<5 && scrolling1.getTop()==150)
                   {
                       scrolling.setTop(0);
                   }
                   else scrolling.setTop(0);
                    if(Math.abs(scrolling.getVideoTime()-scrolling1.getVideoTime())<5 && scrolling1.getAllDisplayTop()!=360)
                        scrolling.setAllDisplayTop(scrolling1.getAllDisplayTop()+30);
                    else if(Math.abs(scrolling.getVideoTime()-scrolling1.getVideoTime())<5 && scrolling1.getAllDisplayTop()==360)
                    {
                        scrolling.setAllDisplayTop(0);
                    }
                    else scrolling.setAllDisplayTop(0);
                   break;
                }
            }
            else
            {
                scrolling.setTop(0);
                scrolling.setAllDisplayTop(0);
            }
        }//设置top（未进入全屏）330-180px +30  进入全屏 750-360px
         else if(scrolling.getLocation()==3) {

            scrollingLambdaQueryWrapper.eq(Scrolling::getVideoId, scrolling.getVideoId())
                    .eq(Scrolling::getLocation, 3)
                    .orderByDesc(Scrolling::getSendTime);//安照最新的
            List<Scrolling> scrollings = scrollingMapper.selectList(scrollingLambdaQueryWrapper);
            if (!scrollings.isEmpty()) {
                for (Scrolling scrolling1 : scrollings) {
                    if (Math.abs(scrolling.getVideoTime() - scrolling1.getVideoTime()) < 5 && scrolling1.getTop() != 180)
                        scrolling.setTop(scrolling1.getTop() - 30);
                    else if (Math.abs(scrolling.getVideoTime() - scrolling1.getVideoTime()) < 5 && scrolling1.getTop() == 180) {
                        scrolling.setTop(330);
                    } else scrolling.setTop(330);
                    if (Math.abs(scrolling.getVideoTime() - scrolling1.getVideoTime()) < 5 && scrolling1.getAllDisplayTop() != 390)
                        scrolling.setAllDisplayTop(scrolling1.getAllDisplayTop() - 30);
                    else if (Math.abs(scrolling.getVideoTime() - scrolling1.getVideoTime()) < 5 && scrolling1.getAllDisplayTop() == 390) {
                        scrolling.setAllDisplayTop(750);
                    } else scrolling.setAllDisplayTop(750);
                    break;
                }
            }
            else {
                scrolling.setTop(330);
                scrolling.setAllDisplayTop(750);
            }
        }

        int insert = scrollingMapper.insert(scrolling);

        Videos videos = videosMapper.selectById(scrolling.getVideoId());
        videos.setScrollingNumber(videos.getScrollingNumber()+1);
        int i = videosMapper.updateById(videos);

        return i > 0 && insert > 0;
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
