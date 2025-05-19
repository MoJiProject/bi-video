package com.moji.po;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

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
   private Integer top;
   private Integer allDisplayTop;


}
