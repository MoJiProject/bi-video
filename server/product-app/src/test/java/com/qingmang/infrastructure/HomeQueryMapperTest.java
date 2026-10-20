package com.qingmang.infrastructure;

import com.qingmang.interfaces.dto.HomeQueryRequest;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.Test;
import java.util.HashMap;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class HomeQueryMapperTest {
    @Test
    void allHomeSqlBranchesResolveSharedJoinsAndParameters() throws Exception {
        Configuration configuration = new Configuration();
        for (String file : new String[]{"UserContentQueryMapper.xml", "HomeQueryMapper.xml"}) {
            try (var input = Resources.getResourceAsStream("mapper/" + file)) {
                new XMLMapperBuilder(input, configuration, file, configuration.getSqlFragments()).parse();
            }
        }
        HomeQueryRequest request = new HomeQueryRequest();
        request.setHomeUserId(7L);
        request.setKeyWord("search");
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("q", request);
        parameters.put("viewerId", null);
        parameters.put("ownerId", 7L);
        parameters.put("reactionType", 2);
        parameters.put("folderId", null);
        parameters.put("offset", 20);
        parameters.put("fans", true);
        parameters.put("keyword", "name");
        for (int type = 1; type <= 3; type++) {
            request.setType(type);
            request.setSort(type);
            for (String id : new String[]{"videos", "videoCount", "reactions", "folderVideos",
                    "folderVideoCount", "posts", "postCount", "images", "imageCount",
                    "relations", "relationCount"}) {
                String sql = configuration.getMappedStatement("com.qingmang.infrastructure.mapper.HomeQueryMapper." + id)
                        .getBoundSql(parameters).getSql().replaceAll("\\s+", " ");
                assertFalse(sql.contains("${"), id);
                assertTrue(sql.contains("SELECT"), id);
                if (id.equals("reactions")) assertTrue(sql.contains("v.id = r.video_id"));
                if (id.equals("folderVideos")) assertTrue(sql.contains("v.id = saved.video_id"));
                if (id.equals("images")) {
                    assertEquals(type == 1, sql.contains("UNION ALL"));
                    assertEquals(type != 3, sql.contains("FROM `comment`"));
                    assertEquals(type != 2, sql.contains("FROM user_post"));
                }
            }
        }
    }
}
