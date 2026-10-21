<template>
  <section class="creator-dashboard">
    <div class="dashboard-heading">
      <h1>创作数据</h1>
      <router-link to="/contribute/subpage2">查看稿件 →</router-link>
    </div>
    <dl class="creator-metrics">
      <div
        v-for="metric in [
          { key: 'newFansNumber', label: '净增粉丝', icon: '/img/新增粉丝.png' },
          { key: 'playNumber', label: '播放量', icon: '/img/播放量.png' },
          { key: 'commentNumber', label: '评论', icon: '/img/评论.png' },
          { key: 'scrollingNumber', label: '弹幕', icon: '/img/弹幕.png' },
          { key: 'likeNumber', label: '点赞', icon: '/img/点赞.png' },
          { key: 'shareNumber', label: '分享', icon: '/img/分享.png' },
          { key: 'collectNumber', label: '收藏', icon: '/img/真收藏.png' },
          { key: 'coinThrowNumber', label: '青芒币', icon: '/img/投币.png' }
        ]"
        :key="metric.key"
        class="metric-card"
      >
        <dt><span class="metric-icon"><img :src="metric.icon" alt="" /></span>{{ metric.label }}</dt>
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
      coinThrowNumber: 0,
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
.creator-dashboard { min-width: 0; color: var(--ink); }
.dashboard-heading { display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 12px; margin-bottom: 24px; }
.dashboard-heading h1 { margin: 0; font-size: 24px; font-weight: 600; }
.dashboard-heading a { color: var(--brand-hover); font-size: 13px; text-decoration: none; }
.creator-metrics { display: grid; grid-template-columns: repeat(4, minmax(0, 1fr)); gap: 18px; margin: 0; }
.metric-card { min-width: 0; padding: 24px; border: 1px solid var(--line); border-radius: 16px; background: #fff; }
.metric-card dt { display: flex; align-items: center; gap: 10px; color: var(--ink-2); font-size: 13px; }
.metric-icon { display: flex; align-items: center; justify-content: center; width: 36px; height: 36px; border-radius: 10px; background: var(--brand-light-9); flex-shrink: 0; }
.metric-icon img { width: 18px; height: 18px; object-fit: contain; }
.metric-card dd { margin: 22px 0 0; color: var(--ink); font-size: clamp(24px, 2.6vw, 36px); font-weight: 600; font-variant-numeric: tabular-nums; overflow-wrap: anywhere; }
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
