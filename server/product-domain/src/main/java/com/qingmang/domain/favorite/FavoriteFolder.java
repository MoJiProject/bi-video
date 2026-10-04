package com.qingmang.domain.favorite;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 收藏夹。旧库用「收藏夹名字」当关联键，换个名字历史数据就全对不上，这里改成外键
 *
 * <p>由 sql/product/01_schema.sql 生成，对应表 {@code favorite_folder}。</p>
 */
@Data
@Accessors(chain = true)
@TableName("favorite_folder")
public class FavoriteFolder implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 收藏夹ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属用户 */
    @TableField("owner_id")
    private Long ownerId;

    /** 收藏夹名称 */
    private String name;

    /** 简介 */
    private String description;

    /** 封面 */
    @TableField("cover_url")
    private String coverUrl;

    /** 可见性 0私密 1公开 */
    private Integer visibility;

    /** 是否默认收藏夹（删默认夹时要转移内容） */
    @TableField("is_default")
    private Boolean isDefault;

    /** 排序 */
    @TableField("sort_order")
    private Integer sortOrder;

    /** 视频数（派生） */
    @TableField("item_count")
    private Integer itemCount;

    /** created_at */
    @TableField("created_at")
    private LocalDateTime createdAt;

    /** updated_at */
    @TableField("updated_at")
    private LocalDateTime updatedAt;

    /** 软删除 */
    @TableLogic
    @TableField("deleted_at")
    private LocalDateTime deletedAt;
}
