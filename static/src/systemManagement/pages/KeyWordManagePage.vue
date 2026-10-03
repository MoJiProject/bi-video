<template>
  <div class="keyword-page">
    <div class="sys-panel">
      <div class="sys-panel-title">
        <span class="title">
          搜索热词管理
          <span class="title-tip">维护搜索框联想词，搜索次数越高排序越靠前</span>
        </span>
      </div>

      <div class="sys-filter">
        <el-input
          v-model="query.keyword"
          placeholder="搜索热词"
          clearable
          style="width: 260px"
          @keyup.enter="handleSearch"
          @clear="handleSearch"
        />
        <el-button type="primary" @click="handleSearch">查询</el-button>
        <el-button @click="handleReset">重置</el-button>
        <el-button
          type="danger"
          :disabled="!selected.length"
          @click="handleBatchDelete"
          >批量删除 ({{ selected.length }})</el-button
        >
        <el-button type="success" @click="openAdd">新增热词</el-button>
      </div>

      <el-table
        ref="tableRef"
        :data="keyWords"
        class="sys-table"
        row-key="id"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="46" />

        <el-table-column label="排名" width="70" align="center">
          <template #default="scope">
            <span class="rank" :class="'rank-' + (scope.$index < 3 ? scope.$index : 'normal')">{{
              scope.$index + 1
            }}</span>
          </template>
        </el-table-column>

        <el-table-column label="搜索词" min-width="180" align="center">
          <template #default="scope">
            <span class="word-text">{{ scope.row.word }}</span>
          </template>
        </el-table-column>

        <el-table-column label="搜索次数" min-width="220" align="center">
          <template #default="scope">
            <div class="count-cell">
              <span class="count-value">{{ scope.row.count || 0 }}</span>
              <span class="count-bar-track">
                <span class="count-bar" :style="{ width: countWidth(scope.row.count) }"></span>
              </span>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="ID" width="80" prop="id" align="center" />

        <el-table-column
          label="操作"
          width="238"
          align="center"
          fixed="right"
          class-name="sys-actions"
        >
          <template #default="scope">
            <el-button size="small" type="success" @click="openEdit(scope.row)">修改</el-button>
            <el-button size="small" type="warning" @click="openCount(scope.row)">调整次数</el-button>
            <el-button size="small" type="danger" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>

        <template #empty>
          <div class="sys-empty">没有符合条件的搜索词</div>
        </template>
      </el-table>

      <SystemPagination
        :total="total"
        :page-size="10"
        :current-page="query.pageNum"
        @current-change="handlePageChange"
      />
    </div>

    <!-- 新增/修改搜索词 -->
    <el-dialog
      v-model="editDialogVisible"
      :title="editTarget ? '修改搜索词' : '新增搜索词'"
      width="440px"
      align-center
    >
      <el-form label-width="90px">
        <el-form-item label="搜索词" required>
          <el-input
            v-model="editWord"
            maxlength="50"
            show-word-limit
            placeholder="至少 2 个字，不能与已有搜索词重复"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitEdit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 调整次数 -->
    <el-dialog v-model="countDialogVisible" title="调整搜索次数" width="440px" align-center>
      <el-form label-width="90px">
        <el-form-item label="搜索词">
          <el-input :model-value="countTarget ? countTarget.word : ''" disabled />
        </el-form-item>
        <el-form-item label="搜索次数" required>
          <el-input-number v-model="editCount" :min="0" :max="99999999" :step="1" controls-position="right" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="countDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitCount">确定</el-button>
      </template>
    </el-dialog>

    <!-- 删除确认 -->
    <el-dialog v-model="deleteDialogVisible" title="删除搜索词" width="400px" align-center>
      <div class="warn-text">
        即将删除 <strong>{{ pendingIds.length }}</strong> 个搜索词，删除后不再出现在搜索联想中。
      </div>
      <template #footer>
        <el-button @click="deleteDialogVisible = false">取消</el-button>
        <el-button type="danger" :loading="submitting" @click="submitDelete">确定删除</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { computed, onMounted, reactive, ref } from "vue";
import { useGlobalStore } from "../../store/store";
import SystemPagination from "../components/SystemPagination.vue";
import {
  addKeyWord,
  deleteKeyWord,
  putKeyWord,
  putKeyWordCount,
  searchKeyWords,
} from "../../api/systemManagement/index";
import { msgOf, toast } from "../systemCommon";

export default {
  name: "KeyWordManagePage",
  components: { SystemPagination },
  emits: ["refresh"],
  setup() {
    const store = useGlobalStore();
    const keyWords = ref([]);
    const total = ref(0);
    const selected = ref([]);
    const tableRef = ref(null);
    const submitting = ref(false);

    const query = reactive({
      pageNum: 1,
      keyword: "",
    });

    const editDialogVisible = ref(false);
    const editTarget = ref(null);
    const editWord = ref("");

    const countDialogVisible = ref(false);
    const countTarget = ref(null);
    const editCount = ref(0);

    const deleteDialogVisible = ref(false);
    const pendingIds = ref([]);

    onMounted(() => {
      loadKeyWords();
    });

    async function loadKeyWords() {
      try {
        const res = await searchKeyWords(store.token, {
          operatorId: store.userId,
          pageNum: query.pageNum,
          keyword: query.keyword,
        });
        if (res.data.code === 1) {
          keyWords.value = res.data.data.records;
          total.value = res.data.data.total;
          selected.value = [];
        } else {
          toast(msgOf(res));
        }
      } catch (error) {
        toast("加载搜索词失败");
      }
    }

    const maxCount = computed(() => {
      const numbers = keyWords.value.map((item) => item.count || 0);
      return numbers.length ? Math.max(...numbers) : 1;
    });

    function countWidth(count) {
      return Math.max(4, Math.round(((count || 0) / maxCount.value) * 100)) + "%";
    }

    function handleSearch() {
      query.pageNum = 1;
      loadKeyWords();
    }

    function handlePageChange(page) {
      query.pageNum = page;
      loadKeyWords();
    }

    function handleReset() {
      query.pageNum = 1;
      query.keyword = "";
      loadKeyWords();
    }

    function handleSelectionChange(rows) {
      selected.value = rows;
    }

    function openAdd() {
      editTarget.value = null;
      editWord.value = "";
      editDialogVisible.value = true;
    }

    function openEdit(row) {
      editTarget.value = row;
      editWord.value = row.word;
      editDialogVisible.value = true;
    }

    async function submitEdit() {
      const word = editWord.value.trim();
      if (word.length <= 1) {
        toast("搜索词至少 2 个字");
        return;
      }
      submitting.value = true;
      try {
        const params = { operatorId: store.userId };
        const res = editTarget.value
          ? await putKeyWord(store.token, params, editTarget.value.id, word)
          : await addKeyWord(store.token, params, word);
        if (res.data.code === 1) {
          toast(msgOf(res), "success");
          editDialogVisible.value = false;
          loadKeyWords();
        } else {
          toast(msgOf(res));
        }
      } catch (error) {
        toast("保存失败");
      } finally {
        submitting.value = false;
      }
    }

    function openCount(row) {
      countTarget.value = row;
      editCount.value = row.count || 0;
      countDialogVisible.value = true;
    }

    async function submitCount() {
      submitting.value = true;
      try {
        const res = await putKeyWordCount(
          store.token,
          { operatorId: store.userId },
          countTarget.value.id,
          editCount.value
        );
        if (res.data.code === 1) {
          toast(msgOf(res), "success");
          countDialogVisible.value = false;
          loadKeyWords();
        } else {
          toast(msgOf(res));
        }
      } catch (error) {
        toast("保存失败");
      } finally {
        submitting.value = false;
      }
    }

    function handleDelete(row) {
      pendingIds.value = [row.id];
      deleteDialogVisible.value = true;
    }

    function handleBatchDelete() {
      if (!selected.value.length) return;
      pendingIds.value = selected.value.map((item) => item.id);
      deleteDialogVisible.value = true;
    }

    async function submitDelete() {
      submitting.value = true;
      try {
        const res = await deleteKeyWord(store.token, {
          operatorId: store.userId,
          ids: pendingIds.value,
        });
        if (res.data.code === 1) {
          toast(`成功删除 ${res.data.data.deleteNumber} 个搜索词`, "success");
          deleteDialogVisible.value = false;
          loadKeyWords();
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
      keyWords,
      total,
      selected,
      submitting,
      query,
      editDialogVisible,
      editTarget,
      editWord,
      countDialogVisible,
      countTarget,
      editCount,
      deleteDialogVisible,
      pendingIds,
      loadKeyWords,
      countWidth,
      handleSearch,
      handleReset,
      handlePageChange,
      handleSelectionChange,
      openAdd,
      openEdit,
      submitEdit,
      openCount,
      submitCount,
      handleDelete,
      handleBatchDelete,
      submitDelete,
    };
  },
};
</script>

<style scoped lang="scss">
@use "../systemPanel.scss" as *;

.rank {
  display: inline-block;
  width: 20px;
  height: 20px;
  line-height: 20px;
  border-radius: 4px;
  background-color: #f4f4f5;
  color: #909399;
  font-size: 12px;

  &.rank-0 {
    background-color: #ff6b6b;
    color: white;
  }

  &.rank-1 {
    background-color: #ff9f43;
    color: white;
  }

  &.rank-2 {
    background-color: #f7d046;
    color: #7a5b00;
  }
}

.word-text {
  font-size: 14px;
  color: #18191c;
}

.count-cell {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
}

.count-value {
  width: 46px;
  text-align: right;
  font-size: 13px;
  color: #18191c;
  flex-shrink: 0;
}

.count-bar-track {
  flex: 1;
  height: 8px;
  background-color: #f0f2f5;
  border-radius: 4px;
  overflow: hidden;
}

.count-bar {
  display: block;
  height: 100%;
  background-color: #00a1d6;
  border-radius: 4px;
}

.warn-text {
  font-size: 14px;
  color: #61666d;
  margin-bottom: 12px;
}
</style>
