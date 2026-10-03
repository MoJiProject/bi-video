<template>
  <div class="recycle-page">
    <div class="sys-panel">
      <div class="sys-panel-title">
        <span class="title">
          回收站
          <span class="title-tip">
            删除的数据会先存到这里，随时可以还原；「彻底清除」不可恢复，请谨慎操作
          </span>
        </span>
      </div>

      <div class="sys-filter">
        <el-select v-model="query.bizType" placeholder="全部类型" clearable style="width: 140px" @change="handleSearch">
          <el-option label="视频" value="video" />
          <el-option label="动态" value="dynamic" />
          <el-option label="评论" value="comment" />
        </el-select>
        <el-select v-model="query.status" placeholder="全部状态" clearable style="width: 130px" @change="handleSearch">
          <el-option label="在回收站" :value="0" />
          <el-option label="已还原" :value="1" />
        </el-select>
        <el-input
          v-model="query.keyword"
          placeholder="搜索标题"
          clearable
          style="width: 220px"
          @keyup.enter="handleSearch"
          @clear="handleSearch"
        />
        <el-button type="primary" @click="handleSearch">查询</el-button>
        <el-button @click="handleReset">重置</el-button>
        <el-button
          type="danger"
          :disabled="!selectedPurge.length"
          @click="handlePurge"
          >彻底清除 ({{ selectedPurge.length }})</el-button
        >
        <el-button type="warning" plain @click="handleCleanRestored">
          清理已还原记录
        </el-button>
      </div>

      <el-alert
        type="info"
        :closable="false"
        show-icon
        title="视频被移入回收站时，磁盘上的视频与封面文件会先备份到 upload/video/recycle_bin 目录，还原时自动移回原位。"
        style="margin-bottom: 14px"
      />

      <el-table
        ref="tableRef"
        :data="list"
        class="sys-table"
        row-key="id"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="46" />

        <el-table-column label="类型" width="82" align="center">
          <template #default="scope">
            <span v-if="scope.row.bizType === 'video'" class="sys-tag tag-primary">视频</span>
            <span v-else-if="scope.row.bizType === 'dynamic'" class="sys-tag tag-warning">动态</span>
            <span v-else class="sys-tag tag-success">评论</span>
          </template>
        </el-table-column>

        <el-table-column label="内容" min-width="260" align="center">
          <template #default="scope">
            <div class="content-cell">
              <img
                v-if="scope.row.coverAddress"
                :src="scope.row.coverAddress"
                class="cover"
                referrerpolicy="no-referrer"
                @error="onCoverError"
              />
              <div class="content-meta">
                <span class="content-title">{{ scope.row.title || "(无标题)" }}</span>
                <span class="sub-text">
                  {{ bizTypeLabel(scope.row.bizType) }} #{{ scope.row.bizId }}
                  <template v-if="scope.row.ownerName"> · {{ scope.row.ownerName }}</template>
                </span>
                <span v-if="scope.row.reason" class="reason-text">
                  原因：{{ scope.row.reason }}
                </span>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="删除信息" width="170" align="center">
          <template #default="scope">
            <div class="time-text">{{ shortTime(scope.row.deleteTime) }}</div>
            <div class="sub-text">{{ scope.row.operatorName || "未知" }}</div>
          </template>
        </el-table-column>

        <el-table-column label="状态" width="94" align="center">
          <template #default="scope">
            <span v-if="scope.row.status === 0" class="sys-tag tag-warning">回收站中</span>
            <span v-else class="sys-tag tag-grey">已还原</span>
          </template>
        </el-table-column>

        <el-table-column label="还原时间" width="150" align="center">
          <template #default="scope">
            <span class="sub-text">{{ shortTime(scope.row.restoreTime) }}</span>
          </template>
        </el-table-column>

        <el-table-column
          label="操作"
          width="196"
          align="center"
          fixed="right"
          class-name="sys-actions"
        >
          <template #default="scope">
            <template v-if="scope.row.status === 0">
              <el-button size="small" type="success" @click="handleRestore(scope.row)">还原</el-button>
              <el-button size="small" type="danger" @click="handlePurgeOne(scope.row)">彻底清除</el-button>
            </template>
            <span v-else class="sub-text">已还原</span>
          </template>
        </el-table-column>

        <template #empty>
          <div class="sys-empty">回收站是空的</div>
        </template>
      </el-table>

      <SystemPagination
        :total="total"
        :page-size="10"
        :current-page="query.pageNum"
        @current-change="handlePageChange"
      />
    </div>

    <!-- 彻底确认弹窗 -->
    <el-dialog v-model="purgeDialogVisible" title="彻底清除" width="460px" align-center>
      <el-alert
        type="error"
        :closable="false"
        show-icon
        title="此操作不可恢复"
        description="清除后快照数据与备份文件都会被删除，业务数据无法再还原。如果只是想隐藏，请使用「还原」或「审核退回/强制下架」。"
        style="margin-bottom: 14px"
      />
      <div class="warn-text">即将彻底清除 <strong>{{ pendingIds.length }}</strong> 条记录。</div>
      <el-input
        v-model="purgeReason"
        type="textarea"
        :rows="3"
        maxlength="200"
        show-word-limit
        placeholder="请填写清除原因（选填），会记录到操作日志"
      />
      <template #footer>
        <el-button @click="purgeDialogVisible = false">取消</el-button>
        <el-button type="danger" :loading="submitting" @click="submitPurge">确定彻底清除</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { onMounted, reactive, ref } from "vue";
import { useGlobalStore } from "../../store/store";
import SystemPagination from "../components/SystemPagination.vue";
import {
  cleanRestored,
  purgeRecycleBin,
  restoreComment,
  restoreDynamic,
  restoreVideo,
  searchRecycleBin,
} from "../../api/systemManagement/index";
import { confirmDanger, msgOf, shortTime, toast } from "../systemCommon";

export default {
  name: "RecycleBinPage",
  components: { SystemPagination },
  emits: ["refresh"],
  setup() {
    const store = useGlobalStore();
    const list = ref([]);
    const total = ref(0);
    const selected = ref([]);
    const selectedPurge = ref([]);
    const tableRef = ref(null);
    const submitting = ref(false);

    const query = reactive({
      pageNum: 1,
      bizType: "",
      status: 0,
      keyword: "",
    });

    const purgeDialogVisible = ref(false);
    const purgeReason = ref("");
    const pendingIds = ref([]);

    onMounted(() => {
      loadList();
    });

    async function loadList() {
      try {
        const res = await searchRecycleBin(store.token, {
          operatorId: store.userId,
          pageNum: query.pageNum,
          bizType: query.bizType || null,
          status: query.status === null || query.status === undefined ? null : query.status,
          keyword: query.keyword,
        });
        if (res.data.code === 1) {
          list.value = res.data.data.records;
          total.value = res.data.data.total;
          selected.value = [];
        } else {
          toast(msgOf(res));
        }
      } catch (error) {
        toast("加载回收站失败");
      }
    }

    function handleSearch() {
      query.pageNum = 1;
      loadList();
    }

    function handlePageChange(page) {
      query.pageNum = page;
      loadList();
    }

    function handleReset() {
      query.pageNum = 1;
      query.bizType = "";
      query.status = 0;
      query.keyword = "";
      loadList();
    }

    function handleSelectionChange(rows) {
      //只允许选择还在回收站里的记录做彻底清除
      selected.value = rows;
      selectedPurge.value = rows.filter((item) => item.status === 0);
    }

    function bizTypeLabel(type) {
      if (type === "video") return "视频";
      if (type === "dynamic") return "动态";
      return "评论";
    }

    //还原
    async function handleRestore(row) {
      try {
        await confirmDanger(
          `确定还原这条${bizTypeLabel(row.bizType)}吗？还原后会重新出现在站点上。`,
          "确定还原"
        );
      } catch (error) {
        return;
      }

      submitting.value = true;
      try {
        let res;
        if (row.bizType === "video")
          res = await restoreVideo(store.token, { operatorId: store.userId, ids: [row.bizId] });
        else if (row.bizType === "dynamic")
          res = await restoreDynamic(store.token, { operatorId: store.userId, ids: [row.bizId] });
        else
          res = await restoreComment(store.token, { operatorId: store.userId, ids: [row.bizId] });

        if (res.data.code === 1) {
          toast(`已还原 ${res.data.data.number} 条`, "success");
          loadList();
        } else {
          toast(msgOf(res));
        }
      } catch (error) {
        toast("还原失败");
      } finally {
        submitting.value = false;
      }
    }

    function handlePurgeOne(row) {
      pendingIds.value = [row.id];
      purgeReason.value = "";
      purgeDialogVisible.value = true;
    }

    function handlePurge() {
      if (!selectedPurge.value.length) return;
      pendingIds.value = selectedPurge.value.map((item) => item.id);
      purgeReason.value = "";
      purgeDialogVisible.value = true;
    }

    async function submitPurge() {
      submitting.value = true;
      try {
        const res = await purgeRecycleBin(store.token, {
          operatorId: store.userId,
          reason: purgeReason.value.trim(),
          ids: pendingIds.value,
        });
        if (res.data.code === 1) {
          toast(`已彻底清除 ${res.data.data.number} 条记录`, "success");
          purgeDialogVisible.value = false;
          loadList();
        } else {
          toast(msgOf(res));
        }
      } catch (error) {
        toast("清除失败");
      } finally {
        submitting.value = false;
      }
    }

    //清理已还原的记录
    async function handleCleanRestored() {
      try {
        await confirmDanger("确定清理所有「已还原」的回收站记录吗？此操作不可恢复。", "确定清理");
      } catch (error) {
        return;
      }
      try {
        const res = await cleanRestored(store.token, { operatorId: store.userId });
        if (res.data.code === 1) {
          toast(`已清理 ${res.data.data.number} 条记录`, "success");
          loadList();
        } else {
          toast(msgOf(res));
        }
      } catch (error) {
        toast("清理失败");
      }
    }

    //回收站里的封面文件可能已被移到备份目录，盖图裂开时隐藏占位
    function onCoverError(event) {
      event.target.style.visibility = "hidden";
    }

    return {
      store,
      tableRef,
      list,
      total,
      selected,
      selectedPurge,
      submitting,
      query,
      purgeDialogVisible,
      purgeReason,
      pendingIds,
      loadList,
      handleSearch,
      handleReset,
      handlePageChange,
      handleSelectionChange,
      bizTypeLabel,
      handleRestore,
      handlePurgeOne,
      handlePurge,
      submitPurge,
      handleCleanRestored,
      onCoverError,
      shortTime,
    };
  },
};
</script>

<style scoped lang="scss">
@use "../systemPanel.scss" as *;

.content-cell {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
}

.cover {
  width: 74px;
  height: 46px;
  object-fit: cover;
  border-radius: 4px;
  background-color: #f0f2f5;
  flex-shrink: 0;
}

.content-meta {
  min-width: 0;
}

.content-title {
  display: block;
  font-size: 13px;
  color: #18191c;
  word-break: break-all;
  line-height: 1.5;
  max-height: 40px;
  overflow: hidden;
}

.sub-text {
  display: block;
  font-size: 12px;
  color: #9499a0;
  margin-top: 2px;
}

.reason-text {
  display: block;
  font-size: 12px;
  color: #f56c6c;
  margin-top: 2px;
  word-break: break-all;
}

.time-text {
  font-size: 13px;
  color: #18191c;
}

.warn-text {
  font-size: 14px;
  color: #61666d;
  margin-bottom: 12px;
}
</style>
