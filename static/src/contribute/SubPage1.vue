<template>
  <section class="creator-dashboard">
    <div class="dashboard-heading">
      <h1>创作数据</h1>
      <router-link to="/contribute/subpage2">查看稿件 →</router-link>
    </div>
    <dl class="creator-metrics">
      <div
        v-for="metric in [
          { key: 'newFansNumber', label: '净增粉丝' },
          { key: 'playNumber', label: '播放量' },
          { key: 'commentNumber', label: '评论' },
          { key: 'scrollingNumber', label: '弹幕' },
          { key: 'likeNumber', label: '点赞' },
          { key: 'shareNumber', label: '分享' },
          { key: 'collectNumber', label: '收藏' }
        ]"
        :key="metric.key"
        class="metric-card"
      >
        <dt>
          <span class="metric-icon">
            <svg viewBox="0 0 24 24" aria-hidden="true">
              <template v-if="metric.key === 'newFansNumber'"><circle cx="9" cy="8" r="3"/><path d="M3 21v-2a6 6 0 0 1 12 0v2M19 8v6M16 11h6"/></template>
              <path v-else-if="metric.key === 'playNumber'" d="m8 4 12 8-12 8V4Z"/>
              <template v-else-if="metric.key === 'commentNumber'"><path d="M7 18 3 21V6a3 3 0 0 1 3-3h12a3 3 0 0 1 3 3v9a3 3 0 0 1-3 3H7Z"/><path d="M7 8h10M7 12h7"/></template>
              <template v-else-if="metric.key === 'scrollingNumber'"><rect x="3" y="5" width="18" height="14" rx="3"/><path d="M6 9h5M15 9h3M6 13h3M12 13h6"/></template>
              <path v-else-if="metric.key === 'likeNumber'" d="M8 10H4v10h4m0-10 4-7h1a2 2 0 0 1 2 2l-1 5h5a2 2 0 0 1 2 2l-2 7a2 2 0 0 1-2 1H8V10Z"/>
              <template v-else-if="metric.key === 'shareNumber'"><circle cx="6" cy="12" r="3"/><circle cx="18" cy="5" r="3"/><circle cx="18" cy="19" r="3"/><path d="m9 10 6-3M9 14l6 3"/></template>
              <path v-else-if="metric.key === 'collectNumber'" d="m12 3 2.8 5.7 6.2.9-4.5 4.4 1.1 6.2-5.6-3-5.6 3 1.1-6.2L3 9.6l6.2-.9L12 3Z"/>
            </svg>
          </span>
          {{ metric.label }}
        </dt>
        <dd>{{ videoDto[metric.key] }}</dd>
      </div>
    </dl>
  </section>
</template>

<script>
import { reactive, onMounted } from "vue";
import apiClient from "../services/apiClient";
import { ElMessage } from "element-plus";
import {useGlobalStore} from "../store/store";
export default {
  name: "SubPage1",

  setup() {
    const store = useGlobalStore();
    const videoDto = reactive({
      userId: 0,
      likeNumber: 0,
      commentNumber: 0,
      collectNumber: 0,
      shareNumber: 0,
      playNumber: 0,
      newFansNumber: 0,
      scrollingNumber: 0,
    });


    onMounted(() => {
     getVideoData();
    });

    async function getVideoData() {
      try {
        const response = await apiClient.post("/video/selectVideoData", {
          userId: store.userId, // 确保包装成对象
        },
        {
            headers: {
            "Content-Type": "application/json",
            "Authorization": store.token,
          },        
        }
        );

        if (response.data.code === 1) {
          Object.assign(videoDto, response.data.data);
        } else {
          ElMessage({
            message: response.data.msg,
            type: "info",
            plain: true,
            duration: 1700,
          });
        }
      } catch (error) {
        ElMessage({
          message: error.message || "加载投稿统计失败",
          type: "info",
          plain: true,
          duration: 1700,
        });
      }
    }

    return {
      videoDto,
    };
  },
};
</script>
<style scoped lang="scss">
.creator-dashboard { min-width: 0; color: var(--ink); font-family: var(--el-font-family); }
.dashboard-heading { display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 12px; margin-bottom: 24px; }
.dashboard-heading h1 { margin: 0; font-size: 24px; font-weight: 600; }
.dashboard-heading a { color: var(--brand-hover); font-size: 13px; text-decoration: none; }
.creator-metrics { display: grid; grid-template-columns: repeat(4, minmax(0, 1fr)); gap: 16px; margin: 0; }
.metric-card { min-width: 0; padding: 20px; border: 1px solid var(--line); border-radius: 14px; background: #fff; }
.metric-card dt { display: flex; align-items: center; gap: 10px; color: var(--ink-2); font-size: 13px; }
.metric-icon { display: flex; align-items: center; justify-content: center; width: 32px; height: 32px; border-radius: 9px; background: var(--brand-light-9); color: var(--brand-hover); flex-shrink: 0; }
.metric-icon svg { width: 18px; height: 18px; fill: none; stroke: currentColor; stroke-width: 1.6; stroke-linecap: round; stroke-linejoin: round; }
.metric-card dd { margin: 18px 0 0; color: var(--ink-2); font-size: clamp(22px, 2.2vw, 30px); font-weight: 500; line-height: 1.3; font-variant-numeric: tabular-nums; overflow-wrap: anywhere; }
@media (max-width: 1100px) { .creator-metrics { grid-template-columns: repeat(2, minmax(0, 1fr)); } }
@media (max-width: 640px) {
  .dashboard-heading { margin-bottom: 18px; }
  .dashboard-heading h1 { font-size: 21px; }
  .creator-metrics { gap: 12px; }
  .metric-card { padding: 16px; border-radius: 12px; }
  .metric-card dt { gap: 8px; font-size: 12px; }
  .metric-icon { width: 28px; height: 28px; border-radius: 8px; }
  .metric-card dd { margin-top: 16px; }
}
@media (max-width: 360px) { .creator-metrics { grid-template-columns: minmax(0, 1fr); } }
</style>
