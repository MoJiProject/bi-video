package com.moji.po;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Scrolling implements Serializable {

   private Integer id;
   private Integer userId;
   private Integer videoId;
   private String sendTime;
   private Integer size;
   private String color;
   private String content;
   private Integer location;
   private Double videoTime;
   //纵向位置百分比(0~100)：顶部 0~50，底部 50~100，滚动 0~100。
   //与分辨率无关，前端乘播放器高度即可，缩放时相对位置保持正确
   private Integer top;
   //冗余字段，值与 top 相同，保留给仍在读它的旧版本前端
   private Integer allDisplayTop;
   //是否彩色弹幕：1 彩色，0 普通
   private Integer colorful;

}