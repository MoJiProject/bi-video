<template>
  <div v-show="total > 0" class="page-container">
    <el-pagination
      :current-page="currentPage"
      :page-size="pageSize"
      layout="prev, pager, next"
      :total="total"
      :background="true"
      @current-change="handleCurrentChange"
    />
    <span
      >共 {{ Math.ceil(total / pageSize) }} 页 / {{ total }} 个，跳至<input
        type="number"
        v-model="jumpValue"
        @keydown.enter="handleJump"
      />页</span
    >
  </div>
</template>

<script>
import { computed, ref, watch } from "vue";
import { scrollToTop } from "../systemCommon";

/**
 * 系统管理后台分页组件。
 * 结构与收藏夹页(home/collect.vue)的分页保持一致：
 * prev/pager/next + 共N页/跳至输入框。
 * 切页后自动把页面滚动位置带回顶部。
 */
export default {
  name: "SystemPagination",
  props: {
    total: { type: Number, default: 0 },
    pageSize: { type: Number, default: 10 },
    currentPage: { type: Number, default: 1 },
  },
  emits: ["current-change"],
  setup(props, { emit }) {
    const jumpValue = ref("");
    const totalPages = computed(() => Math.ceil(props.total / props.pageSize));

    //切页或总数变化时清空输入，避免残留上一页的页码
    watch(
      () => [props.currentPage, props.total],
      () => {
        jumpValue.value = "";
      }
    );

    function handleCurrentChange(page) {
      emit("current-change", page);
      scrollToTop();
    }

    function handleJump() {
      const page = parseInt(jumpValue.value, 10);
      if (!page || Number.isNaN(page)) return;
      if (page < 1 || page > totalPages.value) return;
      if (page === props.currentPage) return;
      handleCurrentChange(page);
    }

    return {
      jumpValue,
      totalPages,
      handleCurrentChange,
      handleJump,
    };
  },
};
</script>

<style scoped lang="scss">
@use "../systemPanel.scss" as *;
</style>
