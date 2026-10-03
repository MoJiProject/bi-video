<template>
  <div class="dynamic-page">
    <div class="sys-panel">
      <div class="sys-panel-title">
          <span class="title">
            动态管理
            <span class="title-tip">
              同一个视频动态会存多条（UP主自己发布的1条 + 每个粉丝各1条副本），默认只显示UP主发布的
            </span>
          </span>
      </div>

      <div class="sys-filter">
        <el-input
          v-model="query.keyword"
          placeholder="搜索动态标题、内容或发布者昵称"
          clearable
          style="width: 260px"
          @keyup.enter="handleSearch"
          @clear="handleSearch"
        />
        <el-select v-model="query.type" style="width: 140px" @change="handleSearch">
          <el-option label="全部动态" :value="-1" />
          <el-option label="视频动态" :value="0" />
          <el-option label="评论动态" :value="1" />
        </el-select>
        <el-select v-model="query.source" style="width: 160px" @change="handleSearch">
          <el-option label="UP主发布的" :value="0" />
          <el-option label="粉丝收到的副本" :value="1" />
          <el-option label="全部来源" :value="-1" />
        </el-select>
        <el-input
          v-model="query.userId"
          placeholder="发布者ID或昵称"
          clearable
          style="width: 170px"
          @keyup.enter="handleSearch"
          @clear="handleSearch"
        />
        <el-button type="primary" @click="handleSearch">查询</el-button>
        <el-button @click="handleReset">重置</el-button>
        <el-button
          type="danger"
          size="small"
          :disabled="!selected.length"
          @click="handleBatchDelete"
          >批量删除 ({{ selected.length }})</el-button
        >
      </div>

      <el-table
        ref="tableRef"
        :data="dynamics"
        class="sys-table"
        row-key="id"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="46" />

        <el-table-column label="发布者" width="150" align="center">
          <template #default="scope">
            <div class="sys-user-link" @click="goUserHome(scope.row.followId)">
              <img :src="scope.row.followUserAvatar" class="avatar" referrerpolicy="no-referrer" />
              <div class="meta">
                <span class="name">{{ scope.row.followUserName }}</span>
                <span class="sub">ID {{ scope.row.followId }}</span>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="类型" width="86" align="center">
          <template #default="scope">
            <span v-if="scope.row.dynamicFlag === 0" class="sys-tag tag-primary">视频</span>
            <span v-else-if="scope.row.dynamicFlag === 1" class="sys-tag tag-success">评论</span>
            <span v-else class="sys-tag tag-purple">图文</span>
          </template>
        </el-table-column>

        <el-table-column label="标题" min-width="240" align="center">
          <template #default="scope">
            <!-- 整块内容都可点，跳到该动态的详情页（视频/评论/图文三类都跳这里） -->
            <div class="title-cell" @click="goDynamicDetail(scope.row.id)">
              <!-- 视频动态显示视频标题 -->
              <span v-if="scope.row.videoId" class="title-link sys-clip">
                {{ scope.row.videoTitle || "视频已删除" }}
              </span>
              <!-- 图文动态显示动态自身标题，没有标题则占位，不展示正文 -->
              <span v-else class="title-link sys-clip">
                {{ scope.row.title || "无标题" }}
              </span>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="点赞" width="68" align="right">
          <template #default="scope">{{ scope.row.likeNumber || 0 }}</template>
        </el-table-column>

        <el-table-column label="评论" width="68" align="right">
          <template #default="scope">{{ scope.row.commentNumber || 0 }}</template>
        </el-table-column>

        <el-table-column label="发布时间" width="140" align="center">
          <template #default="scope">
            <span class="sub-text">{{ shortTime(scope.row.publishTime) }}</span>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="88" align="center" fixed="right" class-name="sys-actions">
          <template #default="scope">
            <el-button size="small" type="danger" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>

        <template #empty>
          <div class="sys-empty">没有符合条件的动态</div>
        </template>
      </el-table>

      <SystemPagination
        :total="total"
        :page-size="10"
        :current-page="query.pageNum"
        @current-change="handlePageChange"
      />
    </div>

    <el-dialog v-model="deleteDialogVisible" title="移入回收站" width="460px" align-center>
      <el-alert
        type="warning"
        :closable="false"
        show-icon
        title="可以随时还原"
        description="动态及其下评论会先备份进回收站，误删可在回收站一键还原，还原时评论缓存会同步刷新。"
        style="margin-bottom: 14px"
      />
      <div class="warn-text">
        即将移入回收站 <strong>{{ pendingIds.length }}</strong> 条动态。
      </div>
      <el-input
        v-model="deleteReason"
        type="textarea"
        :rows="3"
        maxlength="200"
        show-word-limit
        placeholder="请填写删除原因（选填），会记录到操作日志"
      />
      <template #footer>
        <el-button @click="deleteDialogVisible = false">取消</el-button>
        <el-button type="warning" :loading="submitting" @click="submitDelete">
          确定移入回收站
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { onMounted, reactive, ref } from "vue";
import { useGlobalStore } from "../../store/store";
import SystemPagination from "../components/SystemPagination.vue";
import { recycleDynamic, searchDynamics } from "../../api/systemManagement/index";
import { goUserHome, msgOf, shortTime, toast } from "../systemCommon";

export default {
  name: "DynamicManagePage",
  components: { SystemPagination },
  emits: ["refresh"],
  setup() {
    const store = useGlobalStore();
    const dynamics = ref([]);
    const total = ref(0);
    const selected = ref([]);
    const tableRef = ref(null);
    const submitting = ref(false);

    const query = reactive({
      pageNum: 1,
      keyword: "",
      type: -1,
      userId: "",
      //默认只看UP主自己发布的动态，粉丝收到的是同一动态的副本，全部显示会出现看起来重复的行
      source: 0,
    });

    const deleteDialogVisible = ref(false);
    const deleteReason = ref("");
    const pendingIds = ref([]);

    onMounted(() => {
      loadDynamics();
    });

    async function loadDynamics() {
      try {
        const res = await searchDynamics(store.token, {
          operatorId: store.userId,
          pageNum: query.pageNum,
          keyword: query.keyword,
          type: query.type,
          userId: query.userId || null,
          source: query.source,
        });
        if (res.data.code === 1) {
          dynamics.value = res.data.data.records;
          total.value = res.data.data.total;
          selected.value = [];
        } else {
          toast(msgOf(res));
        }
      } catch (error) {
        toast("加载动态列表失败");
      }
    }

    function handleSearch() {
      query.pageNum = 1;
      loadDynamics();
    }

    function handlePageChange(page) {
      query.pageNum = page;
      loadDynamics();
    }

    function handleReset() {
      query.pageNum = 1;
      query.keyword = "";
      query.type = -1;
      query.userId = "";
      query.source = 0;
      loadDynamics();
    }

    function handleSelectionChange(rows) {
      selected.value = rows;
    }

    //跳转到动态详情
    function goDynamicDetail(dynamicId) {
      if (!dynamicId) {
        toast("该动态缺少id，无法跳转");
        return;
      }
      window.open(`/dynamicDetail?dynamicId=${dynamicId}`, "_blank");
    }

    function handleDelete(row) {
      pendingIds.value = [row.id];
      deleteReason.value = "";
      deleteDialogVisible.value = true;
    }

    function handleBatchDelete() {
      if (!selected.value.length) return;
      pendingIds.value = selected.value.map((item) => item.id);
      deleteReason.value = "";
      deleteDialogVisible.value = true;
    }

    async function submitDelete() {
      submitting.value = true;
      try {
        const res = await deleteDynamic(store.token, {
          operatorId: store.userId,
          reason: deleteReason.value.trim(),
          ids: pendingIds.value,
        });
        if (res.data.code === 1) {
          toast(`已将 ${res.data.data.number} 条动态移入回收站，可在回收站还原`, "success");
          deleteDialogVisible.value = false;
          loadDynamics();
        } else {
          toast(msgOf(res));
        }
      } catch (error) {
        toast("删除失败");
      } finally {
        submitting.value = false;
      }
    }

    return {
      store,
      tableRef,
      dynamics,
      total,
      selected,
      submitting,
      query,
      deleteDialogVisible,
      deleteReason,
      pendingIds,
      loadDynamics,
      handleSearch,
      handleReset,
      handlePageChange,
      goUserHome,
      goDynamicDetail,
      handleSelectionChange,
      handleDelete,
      handleBatchDelete,
      submitDelete,
      shortTime,
    };
  },
};
</script>

<style scoped lang="scss">
@use "../systemPanel.scss" as *;

.title-link {
  color: #18191c;
  font-size: 13px;
  transition: color 0.2s ease;
}

/* 标题整块可点，跳动态详情 */
.title-cell {
  cursor: pointer;
  min-width: 0;

  &:hover .title-link {
    color: #00a1d6;
  }
}

.sub-text {
  font-size: 12px;
  color: #9499a0;
  margin-top: 2px;
  display: block;
}

.warn-text {
  font-size: 14px;
  color: #61666d;
  margin-bottom: 12px;
}
</style>
