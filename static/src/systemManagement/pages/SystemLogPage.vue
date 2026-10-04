<template>
  <div class="log-page">
    <div class="sys-panel">
      <div class="sys-panel-title">
        <span class="title">
          操作日志审计
          <span class="title-tip">记录管理员在本后台的每一次写操作，被拒绝的操作也会留痕</span>
        </span>
      </div>

      <div class="sys-filter">
        <el-select v-model="query.module" placeholder="全部模块" clearable style="width: 150px" @change="handleSearch">
          <el-option label="用户管理" value="user" />
          <el-option label="评论管理" value="comment" />
          <el-option label="动态管理" value="dynamic" />
          <el-option label="私信管理" value="message" />
          <el-option label="搜索热词" value="keyWord" />
        </el-select>
        <el-select v-model="query.success" placeholder="全部结果" clearable style="width: 130px" @change="handleSearch">
          <el-option label="成功" :value="1" />
          <el-option label="失败" :value="0" />
        </el-select>
        <el-button type="primary" @click="handleSearch">查询</el-button>
        <el-button @click="handleReset">重置</el-button>
        <span class="filter-summary">共 {{ logTotal }} 条记录</span>
      </div>
      
      <el-table :data="logs" class="sys-table" row-key="id">
        <el-table-column label="操作人" width="164" align="left">
          <template #default="scope">
            <div class="sys-user-link" @click="goUserHome(scope.row.operatorId)">
              <img :src="scope.row.operatorAvatar" class="avatar" referrerpolicy="no-referrer" />
              <div class="meta">
                <span class="name">{{ scope.row.operatorName || "未知" }}</span>
                <span class="sub">ID {{ scope.row.operatorId }}</span>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="时间" width="160" align="center">
          <template #default="scope">
            <span class="time-text">{{ shortTime(scope.row.createTime) }}</span>
          </template>
        </el-table-column>


        <el-table-column label="模块" width="104" align="center">
          <template #default="scope">
            <span class="sys-tag tag-primary">{{ MODULE_LABEL[scope.row.module] || scope.row.module }}</span>
          </template>
        </el-table-column>

        <el-table-column label="操作对象" min-width="160" align="center">
          <template #default="scope">
            <el-tooltip :content="scope.row.targetName || '-'" placement="top">
              <span class="sys-clip">{{ ellipsis(scope.row.targetName, 24) }}</span>
            </el-tooltip>
          </template>
        </el-table-column>

        <el-table-column label="说明" min-width="240" align="center">
          <template #default="scope">
            <span>{{ scope.row.detail || "-" }}</span>
          </template>
        </el-table-column>

        <el-table-column label="结果" width="86" align="center">
          <template #default="scope">
            <span v-if="scope.row.success === 1" class="sys-tag tag-success">成功</span>
            <span v-else class="sys-tag tag-danger">被拒绝</span>
          </template>
        </el-table-column>

        <el-table-column label="IP" width="124" prop="ip" align="center" />

        <template #empty>
          <div class="sys-empty">暂无操作日志</div>
        </template>
      </el-table>

      <SystemPagination
        :total="total"
        :page-size="10"
        :current-page="query.pageNum"
        @current-change="handlePageChange"
      />
    </div>
  </div>
</template>

<script>
import { onMounted, reactive, ref } from "vue";
import { useGlobalStore } from "../../store/store";
import SystemPagination from "../components/SystemPagination.vue";
import { searchLogs } from "../../api/systemManagement/index";
import {
  MODULE_LABEL,
  ellipsis,
  goUserHome,
  msgOf,
  shortTime,
  toast,
} from "../systemCommon";

export default {
  name: "SystemLogPage",
  components: { SystemPagination },
  emits: ["refresh"],
  setup() {
    const store = useGlobalStore();
    const logs = ref([]);
    const total = ref(0);
    const logTotal = ref(0);

    const query = reactive({
      pageNum: 1,
      module: "",
      success: undefined,
    });

    onMounted(() => {
      loadLogs();
    });

    async function loadLogs() {
      try {
        const res = await searchLogs(store.token, {
          operatorId: store.userId,
          pageNum: query.pageNum,
          module: query.module || null,
          success: query.success === undefined ? null : query.success,
        });
        if (res.data.code === 1) {
          logs.value = res.data.data.records;
          total.value = res.data.data.total;
          logTotal.value = res.data.data.total;
        } else {
          toast(msgOf(res));
        }
      } catch (error) {
        toast("加载操作日志失败");
      }
    }

    function handleSearch() {
      query.pageNum = 1;
      loadLogs();
    }

    function handlePageChange(page) {
      query.pageNum = page;
      loadLogs();
    }

    function handleReset() {
      query.pageNum = 1;
      query.module = "";
      query.success = undefined;
      loadLogs();
    }

    return {
      store,
      logs,
      total,
      logTotal,
      query,
      loadLogs,
      handleSearch,
      handleReset,
      handlePageChange,
      goUserHome,
      shortTime,
      ellipsis,
      MODULE_LABEL,
    };
  },
};
</script>

<style scoped lang="scss">
@use "../systemPanel.scss" as *;

.filter-summary {
  font-size: 13px;
  color: #9499a0;
  margin-left: auto;
}

.time-text {
  font-size: 13px;
  color: #18191c;
}

.sub-text {
  font-size: 12px;
  color: #9499a0;
}

.detail-text {
  font-size: 13px;
  color: #61666d;
  word-break: break-all;
}
</style>
