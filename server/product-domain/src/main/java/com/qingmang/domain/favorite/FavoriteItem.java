package com.qingmang.domain.favorite;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 收藏夹内容
 *
 * <p>由 sql/product/01_schema.sql 生成，对应表 {@code favorite_item}。</p>
 */
@Data
@Accessors(chain = true)
@TableName("favorite_item")
public class FavoriteItem implements Serializable {

    private static final long serialVersionUID = 1L;

    /** id */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 收藏夹ID */
    @TableField("folder_id")
    private Long folderId;

    /** 所属用户（冗余，便于按用户查） */
    @TableField("user_id")
    private Long userId;

    /** 视频ID */
    @TableField("video_id")
    private Long videoId;

    /** created_at */
    @TableField("created_at")
    private LocalDateTime createdAt;
}
