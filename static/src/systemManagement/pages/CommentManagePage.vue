<template>
  <div class="comment-page">
    <div class="sys-panel">
      <div class="sys-panel-title">
        <span class="title">
          评论管理
          <span class="title-tip">删除后不可恢复，评论计数与视频/动态统计会同步扣减</span>
        </span>
      </div>

      <div class="sys-filter">
        <el-input
          v-model="query.keyword"
          placeholder="搜索评论内容"
          clearable
          style="width: 240px"
          @keyup.enter="handleSearch"
          @clear="handleSearch"
        />
        <el-select v-model="query.type" style="width: 130px" @change="handleSearch">
          <el-option label="全部评论" :value="-1" />
          <el-option label="主评论" :value="0" />
          <el-option label="回复评论" :value="1" />
        </el-select>
        <el-input
          v-model="query.videoId"
          placeholder="视频ID或标题"
          clearable
          style="width: 170px"
          @keyup.enter="handleSearch"
          @clear="handleSearch"
        />
        <el-input
          v-model="query.userId"
          placeholder="评论人ID或昵称"
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
        :data="comments"
        class="sys-table"
        row-key="id"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="46" />

        <el-table-column label="评论人" width="150" align="center">
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

        <el-table-column label="评论内容" min-width="240" align="left">
          <template #default="scope">
            <!-- 评论正文本身就是富文本HTML，必须用v-html渲染，否则会把标签当原文显示 -->
            <div
              class="sys-comment-html jump-comment"
              v-html="scope.row.content || '（内容已清空）'"
              @click="goComment(scope.row)"
            ></div>
            <div class="jump-hint">
              <el-icon><TopRight /></el-icon>点击跳转到{{ scope.row.videoId ? "视频" : "动态" }}
            </div>
            <div v-if="scope.row.replyUserName" class="sub-text">
              回复 {{ scope.row.replyUserName }}
            </div>
            <div v-if="scope.row.imgAddress" class="sub-text">含图片</div>
          </template>
        </el-table-column>

        <el-table-column label="所属内容" min-width="170" align="center">
          <template #default="scope">
            <div
              v-if="scope.row.videoId"
              class="jump-cell"
              @click="goVideoComment(scope.row)"
            >
              <el-icon class="jump-icon"><VideoCamera /></el-icon>
              <span class="sys-clip">{{ scope.row.videoTitle || "视频已删除" }}</span>
            </div>
            <div
              v-else-if="scope.row.dynamicId"
              class="jump-cell"
              @click="goDynamicDetail(scope.row)"
            >
              <el-icon class="jump-icon"><Promotion /></el-icon>
              <span>动态 #{{ scope.row.dynamicId }}</span>
            </div>
            <span v-else class="sub-text">-</span>
          </template>
        </el-table-column>

        <el-table-column label="点赞" width="66" align="right">
          <template #default="scope">{{ scope.row.likeCommentNumber || 0 }}</template>
        </el-table-column>

        <el-table-column label="时间" width="140" align="center">
          <template #default="scope">
            <span class="sub-text">{{ shortTime(scope.row.commentTime) }}</span>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="88" align="center" fixed="right" class-name="sys-actions">
          <template #default="scope">
            <el-button size="small" type="danger" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>

        <template #empty>
          <div class="sys-empty">没有符合条件的评论</div>
        </template>
      </el-table>

      <SystemPagination
        :total="total"
        :page-size="10"
        :current-page="query.pageNum"
        @current-change="handlePageChange"
      />
    </div>

    <!-- 删除原因弹窗 -->
    <el-dialog v-model="deleteDialogVisible" title="移入回收站" width="450px" align-center>
      <el-alert
        type="warning"
        :closable="false"
        show-icon
        title="可以随时还原"
        description="评论移入回收站后内容会被清空并对其他人隐藏，误删可在回收站一键还原。"
        style="margin-bottom: 14px"
      />
      <div class="warn-text">
        即将移入回收站 <strong>{{ pendingIds.length }}</strong> 条评论。
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
import { VideoCamera, Promotion, TopRight } from "@element-plus/icons-vue";
import { useGlobalStore } from "../../store/store";
import SystemPagination from "../components/SystemPagination.vue";
import { recycleComment, searchComments } from "../../api/systemManagement/index";
import { goUserHome, msgOf, shortTime, toast } from "../systemCommon";

export default {
  name: "CommentManagePage",
  components: { SystemPagination, VideoCamera, Promotion, TopRight },
  emits: ["refresh"],
  setup() {
    const store = useGlobalStore();
    const comments = ref([]);
    const total = ref(0);
    const selected = ref([]);
    const tableRef = ref(null);
    const submitting = ref(false);

    const query = reactive({
      pageNum: 1,
      keyword: "",
      type: -1,
      videoId: "",
      userId: "",
    });

    const deleteDialogVisible = ref(false);
    const deleteReason = ref("");
    const pendingIds = ref([]);

    onMounted(() => {
      loadComments();
    });

    async function loadComments() {
      try {
        const res = await searchComments(store.token, {
          operatorId: store.userId,
          pageNum: query.pageNum,
          keyword: query.keyword,
          type: query.type,
          videoId: query.videoId || null,
          userId: query.userId || null,
        });
        if (res.data.code === 1) {
          comments.value = res.data.data.records;
          total.value = res.data.data.total;
          selected.value = [];
        } else {
          toast(msgOf(res));
        }
      } catch (error) {
        toast("加载评论列表失败");
      }
    }

    function handleSearch() {
      query.pageNum = 1;
      loadComments();
    }

    function handlePageChange(page) {
      query.pageNum = page;
      loadComments();
    }

    //跳转到动态详情
    function goDynamicDetail(row) {
      const dynamicId = row.dynamicId;
      if (!dynamicId) return;
      window.open(`/dynamicDetail?dynamicId=${dynamicId}`, "_blank");
    }

    //跳转到视频详情
    function goVideoComment(row) {
      const videoId = row.videoId;
      if (!videoId) return;
      window.open(`/video?videoId=BV${videoId}`, "_blank");
    }

    //评论正文点击：有归属就跳到对应位置，没有归属就提示
    function goComment(row) {
      if (row.videoId) {
        goVideoComment(row);
        return;
      }
      if (row.dynamicId) {
        goDynamicDetail(row);
        return;
      }
      toast("该评论没有关联的视频或动态，无法跳转");
    }

    function handleReset() {
      query.pageNum = 1;
      query.keyword = "";
      query.type = -1;
      query.videoId = "";
      query.userId = "";
      loadComments();
    }

    function handleSelectionChange(rows) {
      selected.value = rows;
    }

    //单条删除
    function handleDelete(row) {
      pendingIds.value = [row.id];
      deleteReason.value = "";
      deleteDialogVisible.value = true;
    }

    //批量删除
    function handleBatchDelete() {
      if (!selected.value.length) return;
      pendingIds.value = selected.value.map((item) => item.id);
      deleteReason.value = "";
      deleteDialogVisible.value = true;
    }

    async function submitDelete() {
      submitting.value = true;
      try {
        const res = await recycleComment(store.token, {
          operatorId: store.userId,
          reason: deleteReason.value.trim(),
          ids: pendingIds.value,
        });
        if (res.data.code === 1) {
          toast(`已将 ${res.data.data.number} 条评论移入回收站，可在回收站还原`, "success");
          deleteDialogVisible.value = false;
          loadComments();
        } else {
          toast(msgOf(res));
        }
      } catch (error) {
        toast("操作失败");
      } finally {
        submitting.value = false;
      }
    }

    return {
      store,
      tableRef,
      comments,
      total,
      selected,
      submitting,
      query,
      deleteDialogVisible,
      deleteReason,
      pendingIds,
      loadComments,
      handleSearch,
      handleReset,
      handlePageChange,
      goDynamicDetail,
      goVideoComment,
      goComment,
      handleSelectionChange,
      handleDelete,
      handleBatchDelete,
      submitDelete,
      goUserHome,
      shortTime,
    };
  },
};
</script>

<style scoped lang="scss">
@use "../systemPanel.scss" as *;

.jump-comment {
  cursor: pointer;

  &:hover {
    color: #00a1d6;
  }
}

.jump-hint {
  display: flex;
  align-items: center;
  gap: 3px;
  font-size: 12px;
  color: #c0c4cc;
  margin-top: 3px;
}

.jump-cell {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 5px;
  font-size: 13px;
  color: #00a1d6;
  cursor: pointer;
  min-width: 0;

  &:hover {
    color: #00b6e3;
  }
}

.jump-icon {
  flex-shrink: 0;
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
