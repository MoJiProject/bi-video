<template>
  <nav class="creator-nav" aria-label="创作中心导航">
    <router-link to="/contribute" :class="{ active: isActive('/contribute') || isActive('/contribute/edit') }">
      <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M12 16V3m-5 5 5-5 5 5M4 15v5a1 1 0 0 0 1 1h14a1 1 0 0 0 1-1v-5"/></svg>
      视频投稿
    </router-link>
    <router-link to="/contribute/subpage2" :class="{ active: isActive('/contribute/subpage2') }">
      <svg viewBox="0 0 24 24" aria-hidden="true"><rect x="3" y="4" width="18" height="16" rx="3"/><path d="m10 9 5 3-5 3Z"/></svg>
      稿件管理
    </router-link>
    <router-link to="/contribute/subpage1" :class="{ active: isActive('/contribute/subpage1') }">
      <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M4 3v18h17M8 16v-5m5 5V7m5 9v-8"/></svg>
      创作数据
    </router-link>
  </nav>
</template>

<script>
import { useRoute } from "vue-router";
import {useGlobalStore} from "../store/store";
export default {
  name: "AsideComponent",
  setup() {
    const store = useGlobalStore(); // 获取全局状态管理对象
    const route = useRoute(); // 获取当前路由对象
    const isActive = (path) => {
      return route.path === path; // 判断当前路径是否匹配
    };

    return { isActive,store };
  },
};
</script>

<style scoped>
.creator-nav { display: flex; flex-wrap: wrap; gap: 6px; padding: 6px; }
.creator-nav a { display: inline-flex; align-items: center; justify-content: center; gap: 8px; min-height: 42px; padding: 8px 20px; border-radius: 10px; color: var(--ink-2); text-decoration: none; font-size: 14px; }
.creator-nav a.active { background: var(--brand-light-9); color: var(--brand-hover); font-weight: 600; }
.creator-nav a:hover { background: var(--fill-soft); }
.creator-nav a:focus-visible { outline: 2px solid var(--brand); outline-offset: -2px; }
.creator-nav svg { width: var(--icon-sm); height: var(--icon-sm); fill: none; stroke: currentColor; stroke-width: 1.6; stroke-linecap: round; stroke-linejoin: round; }
@media (max-width: 640px) {
  .creator-nav { gap: 4px; }
  .creator-nav a { flex: 1; padding: 8px 6px; font-size: 12px; }
}
</style>
