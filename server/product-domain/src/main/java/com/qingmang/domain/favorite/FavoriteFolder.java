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

/** 收藏夹。旧库用「收藏夹名字」当关联键，换个名字历史数据就全对不上，这里改成外键 */
@Data
@Accessors(chain = true)
@TableName("favorite_folder")
public class FavoriteFolder implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("owner_id")
    private Long ownerId;

    private String name;

    private String description;

    @TableField("cover_url")
    private String coverUrl;

    private Integer visibility;

    @TableField("is_default")
    private Boolean isDefault;

    @TableField("sort_order")
    private Integer sortOrder;

    @TableField("item_count")
    private Integer itemCount;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;

    @TableLogic
    @TableField("deleted_at")
    private LocalDateTime deletedAt;
}
