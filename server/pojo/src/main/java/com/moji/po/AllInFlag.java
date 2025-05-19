package com.moji.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
/**
 * 用于存放所有中更新是对象
 */
public class AllInFlag {

    private String name;
    private Boolean flag;
}
