<template>
  <div class="user-page">
    <div class="sys-panel">
      <div class="sys-panel-title">
        <span class="title">
          用户管理
          <span class="title-tip">支持按用户名/手机号搜索，可封禁用户、调整管理员身份</span>
        </span>
      </div>

      <!-- 筛选 -->
      <div class="sys-filter">
        <el-input
          v-model="query.keyword"
          placeholder="搜索用户名或手机号"
          clearable
          style="width: 240px"
          @keyup.enter="handleSearch"
          @clear="handleSearch"
        />
        <el-select v-model="query.type" style="width: 130px" @change="handleSearch">
          <el-option label="全部用户" :value="-1" />
          <el-option label="普通用户" :value="0" />
          <el-option label="管理员" :value="1" />
          <el-option label="已封禁" :value="2" />
        </el-select>
        <el-select v-model="query.loginFlag" style="width: 160px" @change="handleSearch">
          <el-option label="全部登录状态" :value="-1" />
          <el-option label="7天内有登录" :value="1" />
          <el-option label="7天内未登录" :value="0" />
        </el-select>
        <el-button type="primary" @click="handleSearch">查询</el-button>
        <el-button @click="handleReset">重置</el-button>
        <el-button v-if="query.type === 2" size="small" @click="openBanList">
          封禁记录
        </el-button>
      </div>

      <!-- 列表 -->
      <el-table :data="users" class="sys-table" row-key="id">
        <el-table-column label="用户" min-width="180" align="left">
          <template #default="scope">
            <div class="sys-user-link" @click="goUserHome(scope.row.id)">
              <img :src="scope.row.avatarAddress" class="avatar" referrerpolicy="no-referrer" />
              <div class="meta">
                <span class="name">{{ scope.row.userName }}</span>
                <span class="sub">{{ scope.row.phone }}</span>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="投稿" width="70" align="right">
          <template #default="scope">{{ scope.row.videoNumber || 0 }}</template>
        </el-table-column>

        <el-table-column label="粉丝" width="78" align="right">
          <template #default="scope">{{ compactNumber(scope.row.fansNumber) }}</template>
        </el-table-column>

        <el-table-column label="获赞" width="78" align="right">
          <template #default="scope">{{ compactNumber(scope.row.likeNumber) }}</template>
        </el-table-column>

        <el-table-column label="注册 / 最近登录" min-width="170" align="center">
          <template #default="scope">
            <div class="time-text">注册 {{ shortTime(scope.row.createTime) }}</div>
            <div class="time-text">登录 {{ shortTime(scope.row.loginDateTime) }}</div>
          </template>
        </el-table-column>

        <el-table-column label="身份" width="96" align="center" fixed="right">
          <template #default="scope">
            <span v-if="scope.row.banFlag" class="sys-tag tag-danger">已封禁</span>
            <span v-else-if="scope.row.adminFlag" class="sys-tag tag-primary">管理员</span>
            <span v-else class="sys-tag tag-grey">普通用户</span>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="216" align="center" fixed="right" class-name="sys-actions">
          <template #default="scope">
            <el-button
              v-if="scope.row.banFlag"
              size="small"
              type="success"
              @click="handleUnban(scope.row)"
              >解封</el-button
            >
            <el-button v-else size="small" type="danger" @click="handleBan(scope.row)">封禁</el-button>
            <el-button
              size="small"
              :disabled="scope.row.adminFlag && scope.row.id === store.userId"
              @click="handleAdmin(scope.row)"
              >{{ scope.row.adminFlag ? "取消管理员" : "设为管理员" }}</el-button
            >
          </template>
        </el-table-column>

        <template #empty>
          <div class="sys-empty">没有符合条件的用户</div>
        </template>
      </el-table>

      <SystemPagination
        :total="total"
        :page-size="10"
        :current-page="query.pageNum"
        @current-change="handlePageChange"
      />
    </div>

    <!-- 封禁弹窗 -->
    <el-dialog v-model="banDialogVisible" title="封禁用户" width="440px" align-center>
      <div v-if="currentTarget" class="ban-target">
        <img :src="currentTarget.avatarAddress" class="avatar-lg" referrerpolicy="no-referrer" />
        <span>{{ currentTarget.userName }}</span>
      </div>
      <el-form label-width="80px">
        <el-form-item label="封禁原因" required>
          <el-input
            v-model="banReason"
            type="textarea"
            :rows="3"
            maxlength="200"
            show-word-limit
            placeholder="请填写封禁原因，将记录到操作日志"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="banDialogVisible = false">取消</el-button>
        <el-button type="danger" :loading="submitting" @click="submitBan">确定封禁</el-button>
      </template>
    </el-dialog>

    <!-- 封禁记录弹窗 -->
    <el-dialog v-model="banLogDialogVisible" title="封禁记录" width="880px" align-center>
      <el-table :data="banList" size="small" class="sys-table">
        <el-table-column prop="userName" label="用户" width="150" />
        <el-table-column prop="reason" label="原因" min-width="200" show-overflow-tooltip />
        <el-table-column label="操作人" width="130" prop="operatorName" align="center" />
        <el-table-column label="封禁时间" width="160" align="center">
          <template #default="scope">{{ shortTime(scope.row.banTime) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="90" align="center">
          <template #default="scope">
            <span v-if="scope.row.status === 1" class="sys-tag tag-danger">封禁中</span>
            <span v-else class="sys-tag tag-success">已解除</span>
          </template>
        </el-table-column>
        <el-table-column label="解除时间" width="160" align="center">
          <template #default="scope">{{ shortTime(scope.row.unbanTime) }}</template>
        </el-table-column>
      </el-table>
      <SystemPagination
        :total="banTotal"
        :page-size="10"
        :current-page="banPageNum"
        @current-change="handleBanPageChange"
      />
    </el-dialog>
  </div>
</template>

<script>
import { onMounted, reactive, ref } from "vue";
import { useGlobalStore } from "../../store/store";
import SystemPagination from "../components/SystemPagination.vue";
import {
  banUser,
  putAdmin,
  searchBanList,
  searchUsers,
  unbanUser,
} from "../../api/systemManagement/index";
import {
  compactNumber,
  confirmDanger,
  goUserHome,
  msgOf,
  shortTime,
  toast,
} from "../systemCommon";

export default {
  name: "UserManagePage",
  components: { SystemPagination },
  emits: ["refresh"],
  setup() {
    const store = useGlobalStore();
    const users = ref([]);
    const total = ref(0);
    const submitting = ref(false);

    const query = reactive({
      pageNum: 1,
      keyword: "",
      type: -1,
      loginFlag: -1,
    });

    const banDialogVisible = ref(false);
    const banLogDialogVisible = ref(false);
    const banReason = ref("");
    const currentTarget = ref(null);
    const banList = ref([]);
    const banTotal = ref(0);
    const banPageNum = ref(1);

    onMounted(() => {
      loadUsers();
    });

    //查询用户列表
    async function loadUsers() {
      try {
        const res = await searchUsers(store.token, {
          operatorId: store.userId,
          pageNum: query.pageNum,
          keyword: query.keyword,
          type: query.type,
          loginFlag: query.loginFlag,
        });
        if (res.data.code === 1) {
          users.value = res.data.data.records;
          total.value = res.data.data.total;
        } else {
          toast(msgOf(res));
        }
      } catch (error) {
        toast("加载用户列表失败");
      }
    }

    function handleSearch() {
      query.pageNum = 1;
      loadUsers();
    }

    //分页切页
    function handlePageChange(page) {
      query.pageNum = page;
      loadUsers();
    }

    function handleReset() {
      query.pageNum = 1;
      query.keyword = "";
      query.type = -1;
      query.loginFlag = -1;
      loadUsers();
    }

    //打开封禁弹窗
    function handleBan(row) {
      currentTarget.value = row;
      banReason.value = "";
      banDialogVisible.value = true;
    }

    //提交封禁
    async function submitBan() {
      if (!banReason.value.trim()) {
        toast("请填写封禁原因");
        return;
      }
      submitting.value = true;
      try {
        const res = await banUser(
          store.token,
          { operatorId: store.userId, reason: banReason.value.trim() },
          currentTarget.value.id
        );
        if (res.data.code === 1) {
          toast(msgOf(res), "success");
          banDialogVisible.value = false;
          loadUsers();
        } else {
          toast(msgOf(res));
        }
      } catch (error) {
        toast("封禁失败");
      } finally {
        submitting.value = false;
      }
    }

    //解除封禁
    async function handleUnban(row) {
      try {
        await confirmDanger(
          `确定解除对「${row.userName}」的封禁吗？解除后该用户可以重新登录。`,
          "确定解封"
        );
      } catch (error) {
        return;
      }
      try {
        const res = await unbanUser(
          store.token,
          { operatorId: store.userId, reason: "管理员手动解封" },
          row.id
        );
        if (res.data.code === 1) {
          toast(msgOf(res), "success");
          loadUsers();
        } else {
          toast(msgOf(res));
        }
      } catch (error) {
        toast("解封失败");
      }
    }

    //设置/取消管理员
    async function handleAdmin(row) {
      const action = row.adminFlag ? "取消管理员" : "设为管理员";
      try {
        await confirmDanger(`确定将「${row.userName}」${action}吗？`, "确定");
      } catch (error) {
        return;
      }
      try {
        const res = await putAdmin(
          store.token,
          { operatorId: store.userId, reason: action },
          row.id
        );
        if (res.data.code === 1) {
          toast(`已将「${row.userName}」${action}`, "success");
          loadUsers();
        } else {
          toast(msgOf(res));
        }
      } catch (error) {
        toast("操作失败");
      }
    }

    //打开封禁记录
    async function openBanList() {
      banPageNum.value = 1;
      banLogDialogVisible.value = true;
      loadBanList();
    }

    //封禁记录分页
    function handleBanPageChange(page) {
      banPageNum.value = page;
      loadBanList();
    }

    async function loadBanList() {
      try {
        const res = await searchBanList(store.token, {
          operatorId: store.userId,
          pageNum: banPageNum.value,
        });
        if (res.data.code === 1) {
          banList.value = res.data.data.records;
          banTotal.value = res.data.data.total;
        } else {
          toast(msgOf(res));
        }
      } catch (error) {
        toast("加载封禁记录失败");
      }
    }

    return {
      store,
      users,
      total,
      submitting,
      query,
      banDialogVisible,
      banLogDialogVisible,
      banReason,
      currentTarget,
      banList,
      banTotal,
      banPageNum,
      loadUsers,
      handleSearch,
      handleReset,
      handlePageChange,
      handleBan,
      submitBan,
      handleUnban,
      handleAdmin,
      openBanList,
      loadBanList,
      handleBanPageChange,
      goUserHome,
      shortTime,
      compactNumber,
    };
  },
};
</script>

<style scoped lang="scss">
@use "../systemPanel.scss" as *;

.avatar-lg {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
}

.time-text {
  font-size: 12px;
  color: #5C6664;
  line-height: 1.7;
}

.ban-target {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 14px;
  background-color: #f8fafc;
  border-radius: 6px;
  margin-bottom: 16px;
  font-size: 14px;
}
</style>
