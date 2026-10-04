<template>
  <div class="message-page">
    <div class="sys-panel">
      <div class="sys-panel-title">
        <span class="title">
          私信管理
          <span class="title-tip">用于处理违规私信，删除后会同步清理会话缓存；管理员发私信不受「未回复只能发1条」限制</span>
        </span>
      </div>

      <div class="sys-filter">
        <el-input
          v-model="query.keyword"
          placeholder="搜索私信内容"
          clearable
          style="width: 240px"
          @keyup.enter="handleSearch"
          @clear="handleSearch"
        />
        <el-input
          v-model="query.userId"
          placeholder="发送人ID或昵称"
          clearable
          style="width: 170px"
          @keyup.enter="handleSearch"
          @clear="handleSearch"
        />
        <el-input
          v-model="query.receiverId"
          placeholder="接收人ID或昵称"
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
        :data="messages"
        class="sys-table"
        row-key="id"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="46" />

        <el-table-column label="发送人" min-width="150" align="left">
          <template #default="scope">
            <div class="sys-user-link" @click="goUserHome(scope.row.senderId)">
              <img :src="scope.row.senderAvatar" class="avatar" referrerpolicy="no-referrer" />
              <div class="meta">
                <span class="name">{{ scope.row.senderName }}</span>
                <span class="sub">ID {{ scope.row.senderId }}</span>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="接收人" min-width="150" align="left">
          <template #default="scope">
            <div class="sys-user-link" @click="goUserHome(scope.row.receiverId)">
              <img :src="scope.row.receiverAvatar" class="avatar" referrerpolicy="no-referrer" />
              <div class="meta">
                <span class="name">{{ scope.row.receiverName }}</span>
                <span class="sub">ID {{ scope.row.receiverId }}</span>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="私信内容" min-width="200" align="center">
          <template #default="scope">
            <!-- 私信正文本身就是富文本HTML，用v-html渲染，否则会把标签当原文显示 -->
            <div class="sys-comment-html clamp-3" v-html="scope.row.content"></div>
          </template>
        </el-table-column>

        <el-table-column label="类型" width="90" align="center">
          <template #default="scope">
            <span v-if="scope.row.messageType === 1" class="sys-tag tag-purple">文字</span>
            <span v-else-if="scope.row.messageType === 2" class="sys-tag tag-primary">图片</span>
            <span v-else class="sys-tag tag-warning">视频</span>
          </template>
        </el-table-column>

        <el-table-column label="状态" width="94" align="center">
          <template #default="scope">
            <span v-if="scope.row.status === 0" class="sys-tag tag-danger">未读</span>
            <span v-else-if="scope.row.status === 1" class="sys-tag tag-success">已读</span>
            <span v-else class="sys-tag tag-grey">已撤回</span>
          </template>
        </el-table-column>

        <el-table-column label="发送时间" min-width="150" align="center">
          <template #default="scope">
            <span class="sub-text">{{ shortTime(scope.row.sendTime) }}</span>
          </template>
        </el-table-column>

        <el-table-column
          label="操作"
          width="160"
          align="center"
          fixed="right"
          class-name="sys-actions"
        >
          <template #default="scope">
            <el-button size="small" type="primary" @click="openSend(scope.row)">私信</el-button>
            <el-button size="small" type="danger" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>

        <template #empty>
          <div class="sys-empty">没有符合条件的私信</div>
        </template>
      </el-table>

      <SystemPagination
        :total="total"
        :page-size="10"
        :current-page="query.pageNum"
        @current-change="handlePageChange"
      />
    </div>

    <el-dialog v-model="deleteDialogVisible" title="删除私信" width="440px" align-center>
      <div class="warn-text">
        即将删除 <strong>{{ pendingIds.length }}</strong> 条私信，删除后不可恢复。
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
        <el-button type="danger" :loading="submitting" @click="submitDelete">确定删除</el-button>
      </template>
    </el-dialog>

    <!-- 发送私信弹窗 -->
    <el-dialog v-model="sendDialogVisible" title="发送私信" width="460px" align-center>
      <el-form label-width="90px">
        <el-form-item label="接收人">
          <el-input :model-value="sendTargetName" disabled />
        </el-form-item>
        <el-form-item label="私信内容" required>
          <el-input
            v-model="sendContent"
            type="textarea"
            :rows="4"
            maxlength="200"
            show-word-limit
            placeholder="请输入要发送的私信内容"
          />
        </el-form-item>
      </el-form>
      <el-alert
        type="info"
        :closable="false"
        show-icon
        title="管理员发送私信不受「对方回复前最多1条」的限制"
        style="margin-bottom: 14px"
      />
      <template #footer>
        <el-button @click="sendDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitSend">发送</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { computed, onMounted, reactive, ref } from "vue";
import { useGlobalStore } from "../../store/store";
import SystemPagination from "../components/SystemPagination.vue";
import {
  deleteMessage,
  searchMessages,
  sendPrivateMessage,
} from "../../api/systemManagement/index";
import { goUserHome, msgOf, shortTime, toast } from "../systemCommon";

export default {
  name: "MessageManagePage",
  components: { SystemPagination },
  emits: ["refresh"],
  setup() {
    const store = useGlobalStore();
    const messages = ref([]);
    const total = ref(0);
    const selected = ref([]);
    const tableRef = ref(null);
    const submitting = ref(false);

    const query = reactive({
      pageNum: 1,
      keyword: "",
      userId: "",
      receiverId: "",
    });

    const deleteDialogVisible = ref(false);
    const deleteReason = ref("");
    const pendingIds = ref([]);

    const sendDialogVisible = ref(false);
    const sendContent = ref("");
    const sendTarget = ref(null);

    const sendTargetName = computed(
      () => (sendTarget.value ? `${sendTarget.value.senderName}` : "")
    );

    onMounted(() => {
      loadMessages();
    });

    async function loadMessages() {
      try {
        const res = await searchMessages(store.token, {
          operatorId: store.userId,
          pageNum: query.pageNum,
          keyword: query.keyword,
          userId: query.userId || null,
          receiverId: query.receiverId || null,
        });
        if (res.data.code === 1) {
          messages.value = res.data.data.records;
          total.value = res.data.data.total;
          selected.value = [];
        } else {
          toast(msgOf(res));
        }
      } catch (error) {
        toast("加载私信列表失败");
      }
    }

    function handleSearch() {
      query.pageNum = 1;
      loadMessages();
    }

    function handlePageChange(page) {
      query.pageNum = page;
      loadMessages();
    }

    function handleReset() {
      query.pageNum = 1;
      query.keyword = "";
      query.userId = "";
      query.receiverId = "";
      loadMessages();
    }

    function handleSelectionChange(rows) {
      selected.value = rows;
    }

    function handleDelete(row) {
      pendingIds.value = [row.id];
      deleteReason.value = "";
      deleteDialogVisible.value = true;
    }

    //打开发送私信弹窗，默认发给发送人
    function openSend(row) {
      sendTarget.value = row;
      sendContent.value = "";
      sendDialogVisible.value = true;
    }

    async function submitSend() {
      const content = sendContent.value.trim();
      if (!content) {
        toast("请输入私信内容");
        return;
      }
      submitting.value = true;
      try {
        const res = await sendPrivateMessage(store.token, {
          senderId: store.userId,
          receiverId: sendTarget.value.senderId,
          content,
          //messageType 必须传，1是文字消息，后端会直接拆箱这个字段，不传会抛异常
          messageType: 1,
        });
        if (res.data.code === 1) {
          toast(msgOf(res), "success");
          sendDialogVisible.value = false;
        } else {
          toast(msgOf(res));
        }
      } catch (error) {
        toast("发送失败");
      } finally {
        submitting.value = false;
      }
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
        const res = await deleteMessage(store.token, {
          operatorId: store.userId,
          reason: deleteReason.value.trim(),
          ids: pendingIds.value,
        });
        if (res.data.code === 1) {
          toast(`成功删除 ${res.data.data.deleteNumber} 条私信`, "success");
          deleteDialogVisible.value = false;
          loadMessages();
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
      messages,
      total,
      selected,
      submitting,
      query,
      deleteDialogVisible,
      deleteReason,
      pendingIds,
      sendDialogVisible,
      sendContent,
      sendTarget,
      sendTargetName,
      loadMessages,
      handleSearch,
      handleReset,
      handlePageChange,
      openSend,
      submitSend,
      goUserHome,
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

/* 私信正文按行数截断，长内容不会把表格行撑高 */
.clamp-3 {
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
  max-height: none;
}

.sub-text {
  font-size: 12px;
  color: #8D9794;
  margin-top: 2px;
  display: block;
}

.warn-text {
  font-size: 14px;
  color: #5C6664;
  margin-bottom: 12px;
}
</style>
