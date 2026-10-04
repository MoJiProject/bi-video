<template>
  <div class="video-page">
    <!-- 状态页签 -->
    <div class="tab-bar">
      <div
        v-for="tab in tabs"
        :key="tab.value"
        class="tab-item"
        :class="{ active: query.status === tab.value }"
        @click="handleTab(tab.value)"
      >
        <span class="tab-label">{{ tab.label }}</span>
        <span class="tab-count">{{ tab.count }}</span>
      </div>
    </div>

    <div class="sys-panel">
      <div class="sys-panel-title">
        <span class="title">
          视频管理
          <span class="title-tip">
            审核通过会按创作中心规则重置发布时间并向粉丝推送动态；下架会同步回滚这些影响
          </span>
        </span>
      </div>

      <div class="sys-filter">
        <el-input
          v-model="query.keyword"
          placeholder="搜索视频标题或UP主昵称"
          clearable
          style="width: 240px"
          @keyup.enter="handleSearch"
          @clear="handleSearch"
        />
        <el-select
          v-model="query.subZoneKey"
          placeholder="全部分区"
          clearable
          style="width: 140px"
          @change="handleSearch"
        >
          <el-option v-for="zone in subZones" :key="zone" :label="zone" :value="zone" />
        </el-select>
        <el-input
          v-model="query.userId"
          placeholder="按UP主ID筛选"
          clearable
          style="width: 150px"
          @keyup.enter="handleSearch"
          @clear="handleSearch"
        />
        <el-select v-model="query.sortWay" style="width: 130px" @change="handleSearch">
          <el-option label="最新发布" :value="0" />
          <el-option label="播放量最多" :value="1" />
          <el-option label="点赞最多" :value="2" />
        </el-select>
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
        :data="videos"
        class="sys-table"
        row-key="id"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="46" />

        <el-table-column label="UP主" width="150" align="left">
          <template #default="scope">
            <div class="sys-user-link" @click="goUserHome(scope.row.userId)">
              <img :src="scope.row.userAvatar" class="avatar" referrerpolicy="no-referrer" />
              <div class="meta">
                <span class="name">{{ scope.row.userName }}</span>
                <span class="sub">ID {{ scope.row.userId }}</span>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="封面" width="120" align="center">
          <template #default="scope">
            <img
              :src="scope.row.coverAddress"
              class="cover"
              referrerpolicy="no-referrer"
              @click="goVideoDetail(scope.row.id)"
            />
          </template>
        </el-table-column>

        <el-table-column label="标题" min-width="220" align="center">
          <template #default="scope">
            <span class="title-link sys-clip" @click="goVideoDetail(scope.row.id)">
              {{ scope.row.title }}
            </span>
            <div class="sub-text">
              <span v-if="scope.row.subZoneKey"
                >{{ scope.row.subZoneKey
                }}<template v-if="scope.row.subZoneValue">
                  / {{ scope.row.subZoneValue }}</template
                ></span
              >
              <span class="type-tag">{{ scope.row.type === 1 ? "转载" : "自制" }}</span>
              <span v-if="scope.row.videoTime" class="time-tag">{{ scope.row.videoTime }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="播放" width="70" align="right">
          <template #default="scope">{{ compactNumber(scope.row.playNumber) }}</template>
        </el-table-column>

        <el-table-column label="点赞" width="70" align="right">
          <template #default="scope">{{ compactNumber(scope.row.likeNumber) }}</template>
        </el-table-column>

        <el-table-column label="评论" width="70" align="right">
          <template #default="scope">{{ scope.row.commentNumber || 0 }}</template>
        </el-table-column>

        <el-table-column label="收藏" width="70" align="right">
          <template #default="scope">{{ compactNumber(scope.row.collectNumber) }}</template>
        </el-table-column>

        <el-table-column label="状态" width="130" align="center">
          <template #default="scope">
            <!-- 驳回原因不再单独占一行，鼠标悬停状态标签时才提示 -->
            <el-tooltip
              :disabled="!scope.row.examineFiledMessage"
              :content="scope.row.examineFiledMessage"
              placement="top"
            >
              <span class="sys-tag" :class="'tag-' + videoStatusType(scope.row.status)">
                {{ videoStatusLabel(scope.row.status) }}
              </span>
            </el-tooltip>
          </template>
        </el-table-column>

        <el-table-column label="发布时间" width="140" align="center">
          <template #default="scope">
            <span class="sub-text">{{ shortTime(scope.row.createTime) }}</span>
          </template>
        </el-table-column>

        <el-table-column
          label="操作"
          width="200"
          align="center"
          fixed="right"
          class-name="sys-actions"
        >
          <template #default="scope">
            <el-button
              v-if="scope.row.status === 0"
              size="small"
              type="success"
              @click="handleExamine(scope.row)"
              >通过</el-button
            >
            <el-button
              v-if="scope.row.status === 0"
              size="small"
              type="warning"
              @click="openReject(scope.row)"
              >退回</el-button
            >
            <el-button
              v-if="scope.row.status === 1"
              size="small"
              type="warning"
              @click="openTakeDown(scope.row)"
              >下架</el-button
            >
            <!-- 已下架/已退回的视频可以恢复到已通过 -->
            <el-button
              v-if="scope.row.status === 2"
              size="small"
              type="success"
              @click="handleRestore(scope.row)"
              >上架</el-button
            >
            <el-button size="small" type="danger" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>

        <template #empty>
          <div class="sys-empty">没有符合条件的视频</div>
        </template>
      </el-table>

      <SystemPagination
        :total="total"
        :page-size="10"
        :current-page="query.pageNum"
        @current-change="handlePageChange"
      />
    </div>

    <!-- 退回/下架原因弹窗 -->
    <el-dialog v-model="reasonDialogVisible" :title="reasonDialogTitle" width="460px" align-center>
      <div class="target-row">
        <img :src="currentTarget?.coverAddress" class="cover-sm" referrerpolicy="no-referrer" />
        <div class="target-meta">
          <div class="target-title">{{ currentTarget?.title }}</div>
          <div class="sub-text">UP：{{ currentTarget?.userName }}</div>
        </div>
      </div>
      <el-alert
        v-if="reasonAction === 'takeDown'"
        type="warning"
        :closable="false"
        show-icon
        title="下架后该视频将从首页移除，UP 主的投稿数与动态数会同步回滚，已推送给粉丝的动态副本会被删除。"
        style="margin-bottom: 14px"
      />
      <el-alert
        v-else
        type="info"
        :closable="false"
        show-icon
        title="退回原因会展示给 UP 主，建议写明具体问题。"
        style="margin-bottom: 14px"
      />
      <el-input
        v-model="reasonText"
        type="textarea"
        :rows="3"
        maxlength="200"
        show-word-limit
        placeholder="请填写原因（必填）"
      />
      <template #footer>
        <el-button @click="reasonDialogVisible = false">取消</el-button>
        <el-button type="danger" :loading="submitting" @click="submitReasonAction">
          确定{{ reasonAction === "takeDown" ? "下架" : "退回" }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 删除确认弹窗 -->
    <el-dialog v-model="deleteDialogVisible" title="删除视频" width="470px" align-center>
      <el-alert
        type="error"
        :closable="false"
        show-icon
        title="此操作不可恢复"
        description="将永久删除视频记录、磁盘上的封面与视频文件，并级联清理该视频下的弹幕、评论、收藏、观看历史、点赞与投币记录。如果只是想隐藏，请使用「下架」。"
        style="margin-bottom: 14px"
      />
      <div class="warn-text">
        即将删除 <strong>{{ pendingIds.length }}</strong> 个视频。
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
        <el-button type="danger" :loading="submitting" @click="submitDelete">
          确定删除
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { computed, onMounted, reactive, ref } from "vue";
import { useGlobalStore } from "../../store/store";
import SystemPagination from "../components/SystemPagination.vue";
import {
  deleteVideo,
  examineVideo,
  getSubZoneKeys,
  rejectVideo,
  restoreVideo,
  searchVideos,
  takeDownVideo,
} from "../../api/systemManagement/index";
import {
  compactNumber,
  confirmDanger,
  goUserHome,
  goVideoDetail,
  msgOf,
  shortTime,
  toast,
  videoStatusLabel,
  videoStatusType,
} from "../systemCommon";

export default {
  name: "VideoManagePage",
  components: { SystemPagination },
  emits: ["refresh"],
  setup() {
    const store = useGlobalStore();
    const videos = ref([]);
    const total = ref(0);
    const selected = ref([]);
    const tableRef = ref(null);
    const submitting = ref(false);
    const subZones = ref([]);

    const counts = reactive({ wait: 0, pass: 0, reject: 0 });

    const query = reactive({
      pageNum: 1,
      keyword: "",
      subZoneKey: "",
      userId: undefined,
      status: -1,
      sortWay: 0,
    });

    const reasonDialogVisible = ref(false);
    const reasonDialogTitle = ref("");
    const reasonAction = ref("");
    const reasonText = ref("");
    const currentTarget = ref(null);

    const deleteDialogVisible = ref(false);
    const deleteReason = ref("");
    const pendingIds = ref([]);

    const tabs = computed(() => [
      { value: -1, label: "全部", count: counts.wait + counts.pass + counts.reject },
      { value: 0, label: "待审核", count: counts.wait },
      { value: 1, label: "已通过", count: counts.pass },
      { value: 2, label: "未通过/已下架", count: counts.reject },
    ]);

    onMounted(() => {
      loadVideos();
      loadSubZones();
    });

    async function loadSubZones() {
      try {
        const res = await getSubZoneKeys(store.token, store.userId);
        if (res.data.code === 1) {
          subZones.value = res.data.data || [];
        }
      } catch (error) {
        toast("加载分区列表失败");
      }
    }

    async function loadVideos() {
      try {
        const res = await searchVideos(store.token, {
          operatorId: store.userId,
          pageNum: query.pageNum,
          keyword: query.keyword,
          subZoneKey: query.subZoneKey,
          userId: query.userId || null,
          status: query.status,
          sortWay: query.sortWay,
        });
        if (res.data.code === 1) {
          videos.value = res.data.data.records;
          total.value = res.data.data.total;
          counts.wait = res.data.data.waitNumber;
          counts.pass = res.data.data.passNumber;
          counts.reject = res.data.data.rejectNumber;
          selected.value = [];
        } else {
          toast(msgOf(res));
        }
      } catch (error) {
        toast("加载视频列表失败");
      }
    }

    function handleSearch() {
      query.pageNum = 1;
      loadVideos();
    }

    function handleReset() {
      query.pageNum = 1;
      query.keyword = "";
      query.subZoneKey = "";
      query.userId = undefined;
      query.status = -1;
      query.sortWay = 0;
      loadVideos();
    }

    function handlePageChange(page) {
      query.pageNum = page;
      loadVideos();
    }

    function handleTab(value) {
      query.status = value;
      query.pageNum = 1;
      loadVideos();
    }

    function handleSelectionChange(rows) {
      selected.value = rows;
    }

    //审核通过
    async function handleExamine(row) {
      try {
        await confirmDanger(
          `确定让「${row.title}」审核通过吗？通过后会重置发布时间并向该 UP 的所有粉丝推送一条动态。`,
          "确定通过"
        );
      } catch (error) {
        return;
      }
      submitting.value = true;
      try {
        const res = await examineVideo(store.token, { operatorId: store.userId }, row.id);
        if (res.data.code === 1) {
          toast(msgOf(res), "success");
          loadVideos();
        } else {
          toast(msgOf(res));
        }
      } catch (error) {
        toast("审核失败");
      } finally {
        submitting.value = false;
      }
    }

    //上架：把已下架/已退回的视频恢复为已通过。
    //必须与下架严格互逆——下架时回退了UP计数、删除了粉丝动态副本、软删了收藏，
    //这里要一并还原，否则计数会和实际数据对不上。
    async function handleRestore(row) {
      try {
        await confirmDanger(
          `确定将「${row.title}」重新上架吗？上架后会重新对外展示。`,
          "确定上架"
        );
      } catch (error) {
        return;
      }

      submitting.value = true;
      try {
        const res = await restoreVideo(
          store.token,
          { operatorId: store.userId, reason: "管理员重新上架" },
          row.id
        );
        if (res.data.code === 1) {
          toast(msgOf(res), "success");
          loadVideos();
        } else {
          toast(msgOf(res));
        }
      } catch (error) {
        toast("上架失败");
      } finally {
        submitting.value = false;
      }
    }

    function openReject(row) {
      currentTarget.value = row;
      reasonAction.value = "reject";
      reasonDialogTitle.value = "审核退回";
      reasonText.value = "";
      reasonDialogVisible.value = true;
    }

    function openTakeDown(row) {
      currentTarget.value = row;
      reasonAction.value = "takeDown";
      reasonDialogTitle.value = "下架";
      reasonText.value = "";
      reasonDialogVisible.value = true;
    }

    async function submitReasonAction() {
      const reason = reasonText.value.trim();
      if (!reason) {
        toast("请填写原因");
        return;
      }
      submitting.value = true;
      try {
        const isTakeDown = reasonAction.value === "takeDown";
        const res = isTakeDown
          ? await takeDownVideo(
              store.token,
              { operatorId: store.userId, reason },
              currentTarget.value.id
            )
          : await rejectVideo(
              store.token,
              { operatorId: store.userId, reason },
              currentTarget.value.id
            );
        if (res.data.code === 1) {
          toast(msgOf(res), "success");
          reasonDialogVisible.value = false;
          loadVideos();
        } else {
          toast(msgOf(res));
        }
      } catch (error) {
        toast("操作失败");
      } finally {
        submitting.value = false;
      }
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
        const res = await deleteVideo(store.token, {
          operatorId: store.userId,
          reason: deleteReason.value.trim(),
          ids: pendingIds.value,
        });
        if (res.data.code === 1) {
          toast(`已删除 ${res.data.data.deleteNumber} 个视频，此操作不可恢复`);
          deleteDialogVisible.value = false;
          loadVideos();
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
      videos,
      total,
      selected,
      submitting,
      subZones,
      tabs,
      query,
      reasonDialogVisible,
      reasonDialogTitle,
      reasonAction,
      reasonText,
      currentTarget,
      deleteDialogVisible,
      deleteReason,
      pendingIds,
      loadVideos,
      loadSubZones,
      handleSearch,
      handleReset,
      handlePageChange,
      handleTab,
      handleSelectionChange,
      handleExamine,
      openReject,
      handleRestore,
      openTakeDown,
      submitReasonAction,
      handleDelete,
      handleBatchDelete,
      submitDelete,
      goUserHome,
      goVideoDetail,
      shortTime,
      compactNumber,
      videoStatusLabel,
      videoStatusType,
    };
  },
};
</script>

<style scoped lang="scss">
@use "../systemPanel.scss" as *;

.video-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* ============ 状态页签 ============ */
.tab-bar {
  display: flex;
  gap: 10px;
}

.tab-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 18px;
  background-color: white;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  color: #61666d;
  border: 1px solid transparent;
  transition: all 0.2s ease;
  white-space: nowrap;

  &:hover {
    color: #00a1d6;
  }

  &.active {
    color: #00a1d6;
    border-color: #00a1d6;
    background-color: #eaf7fc;
    font-weight: 600;
  }
}

.tab-count {
  min-width: 20px;
  height: 20px;
  line-height: 20px;
  padding: 0 6px;
  border-radius: 10px;
  background-color: #f0f2f5;
  color: #909399;
  font-size: 12px;
  text-align: center;
}

.tab-item.active .tab-count {
  background-color: #00a1d6;
  color: white;
}

/* ============ 视频条目 ============ */
.cover {
  width: 92px;
  height: 54px;
  object-fit: cover;
  border-radius: 4px;
  background-color: #f0f2f5;
  cursor: pointer;
}

.cover-sm {
  width: 84px;
  height: 50px;
  object-fit: cover;
  border-radius: 4px;
  background-color: #f0f2f5;
  flex-shrink: 0;
}

.title-link {
  color: #18191c;
  font-size: 13px;
  cursor: pointer;
  transition: color 0.2s ease;

  &:hover {
    color: #00a1d6;
  }
}

.type-tag {
  display: inline-block;
  margin: 0 6px;
  padding: 0 5px;
  font-size: 11px;
  line-height: 16px;
  border-radius: 3px;
  background-color: #f4f4f5;
  color: #909399;
}

.time-tag {
  margin-left: 4px;
  font-size: 11px;
  color: #c0c4cc;
}

.sub-text {
  font-size: 12px;
  color: #9499a0;
  margin-top: 2px;
  display: block;
}

/* ============ 弹窗 ============ */
.target-row {
  display: flex;
  gap: 12px;
  align-items: center;
  padding: 10px 12px;
  background-color: #f8fafc;
  border-radius: 6px;
  margin-bottom: 14px;
}

.target-title {
  font-size: 13px;
  color: #18191c;
  word-break: break-all;
  line-height: 1.5;
}

.warn-text {
  font-size: 14px;
  color: #61666d;
  margin-bottom: 12px;
}
</style>
