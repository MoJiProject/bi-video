<template>
  <div class="overview-page">
    <!-- 核心指标卡片 -->
    <div class="stat-grid">
      <div v-for="card in statCards" :key="card.label" class="stat-card">
        <div class="stat-icon" :style="{ backgroundColor: card.color }">
          <el-icon :size="22" color="#ffffff"><component :is="card.icon" /></el-icon>
        </div>
        <div class="stat-body">
          <div class="stat-label">{{ card.label }}</div>
          <div class="stat-value">{{ card.value }}</div>
          <div v-if="card.tip" class="stat-tip">{{ card.tip }}</div>
        </div>
      </div>
    </div>

    <!-- 近7日趋势 -->
    <div class="sys-panel trend-panel">
      <div class="sys-panel-title">
        <span class="title">
          近 7 日新增趋势
          <span class="title-tip">按自然日统计，当天数据会随使用实时变化</span>
        </span>
        <el-radio-group v-model="trendType" size="small">
          <el-radio-button value="userTrend">新增用户</el-radio-button>
          <el-radio-button value="videoTrend">新增视频</el-radio-button>
          <el-radio-button value="commentTrend">新增评论</el-radio-button>
        </el-radio-group>
      </div>

      <div v-if="currentTrend.length" class="chart-wrap">
        <div class="chart-bars">
          <div v-for="point in currentTrend" :key="point.date" class="chart-col">
            <div class="chart-value">{{ point.number }}</div>
            <div class="chart-bar-track">
              <div
                class="chart-bar"
                :style="{ height: barHeight(point.number), backgroundColor: barColor(point.number) }"
              ></div>
            </div>
            <div class="chart-label">{{ shortDate(point.date) }}</div>
          </div>
        </div>
      </div>
      <div v-else class="sys-empty">暂无数据</div>
    </div>

    <div class="bottom-grid">
      <!-- 热门视频 -->
      <div class="sys-panel">
        <div class="sys-panel-title">
          <span class="title">播放量 Top 10</span>
          <span class="title-tip">共 {{ overview.videoNumber || 0 }} 个视频</span>
        </div>
        <el-table :data="overview.hotVideos || []" class="sys-table" size="small">
          <el-table-column label="封面" width="80" align="center">
            <template #default="scope">
              <img :src="scope.row.coverAddress" class="cover" referrerpolicy="no-referrer" />
            </template>
          </el-table-column>
          <el-table-column label="标题" min-width="220" align="center">
            <template #default="scope">
              <a :href="'/video?videoId=' + scope.row.id" target="_blank" class="link">
                {{ scope.row.title }}
              </a>
              <div class="sub-text">UP：{{ scope.row.userName }}</div>
            </template>
          </el-table-column>
          <el-table-column label="播放" width="90" align="right">
            <template #default="scope">{{ compactNumber(scope.row.playNumber) }}</template>
          </el-table-column>
          <el-table-column label="点赞" width="80" align="right">
            <template #default="scope">{{ compactNumber(scope.row.likeNumber) }}</template>
          </el-table-column>
          <el-table-column label="评论" width="80" align="right">
            <template #default="scope">{{ scope.row.commentNumber || 0 }}</template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 热门搜索词 -->
      <div class="sys-panel">
        <div class="sys-panel-title">
          <span class="title">热门搜索词 Top 10</span>
          <span class="title-tip">共 {{ overview.keyWordNumber || 0 }} 个搜索词</span>
        </div>
        <div v-if="(overview.hotKeyWords || []).length" class="keyword-list">
          <div
            v-for="(item, index) in overview.hotKeyWords"
            :key="item.id"
            class="keyword-row"
          >
            <span class="rank" :class="'rank-' + (index < 3 ? index : 'normal')">{{ index + 1 }}</span>
            <span class="keyword-word">{{ item.word }}</span>
            <span class="keyword-bar-track">
              <span
                class="keyword-bar"
                :style="{ width: keywordWidth(item.count) }"
              ></span>
            </span>
            <span class="keyword-count">{{ item.count }}</span>
          </div>
        </div>
        <div v-else class="sys-empty">暂无搜索词</div>
      </div>
    </div>

    <!-- 需要关注 -->
    <div class="sys-panel">
      <div class="sys-panel-title"><span class="title">需要关注</span></div>
      <div class="alert-list">
        <router-link to="/systemManagement/user" class="alert-item">
          <span class="alert-num">{{ overview.banNumber || 0 }}</span>
          <span class="alert-text">名用户处于封禁状态</span>
        </router-link>
        <router-link to="/systemManagement/video" class="alert-item">
          <span class="alert-num">{{ overview.pendingVideoNumber || 0 }}</span>
          <span class="alert-text">个视频待审核</span>
        </router-link>
        <router-link to="/systemManagement/message" class="alert-item">
          <span class="alert-num">{{ overview.messageNumber || 0 }}</span>
          <span class="alert-text">条私信记录</span>
        </router-link>
        <router-link to="/systemManagement/comment" class="alert-item">
          <span class="alert-num">{{ overview.commentNumber || 0 }}</span>
          <span class="alert-text">条评论</span>
        </router-link>
      </div>
    </div>
  </div>
</template>

<script>
import { computed, onMounted, reactive, ref } from "vue";
import {
  UserFilled,
  VideoCamera,
  Clock,
  ChatDotRound,
  Promotion,
  Message,
  VideoPlay,
  Lock,
} from "@element-plus/icons-vue";
import { useGlobalStore } from "../../store/store";
import { getOverview } from "../../api/systemManagement/index";
import { compactNumber, msgOf, toast } from "../systemCommon";

export default {
  name: "OverviewPage",
  emits: ["refresh"],
  setup() {
    const store = useGlobalStore();
    const overview = reactive({});
    const trendType = ref("userTrend");

    const statCards = computed(() => [
      {
        label: "用户总数",
        value: overview.userNumber || 0,
        tip: `今日新增 ${overview.todayUserNumber || 0}`,
        icon: UserFilled,
        color: "#0E9C85",
      },
      {
        label: "视频总数",
        value: overview.videoNumber || 0,
        tip: `今日新增 ${overview.todayVideoNumber || 0}`,
        icon: VideoCamera,
        color: "#00b07c",
      },
      {
        label: "待审核视频",
        value: overview.pendingVideoNumber || 0,
        tip: "需尽快处理",
        icon: Clock,
        color: "#e6a23c",
      },
      {
        label: "评论总数",
        value: overview.commentNumber || 0,
        tip: `今日新增 ${overview.todayCommentNumber || 0}`,
        icon: ChatDotRound,
        color: "#7c5cff",
      },
      {
        label: "动态总数",
        value: overview.dynamicNumber || 0,
        tip: "含粉丝收到的动态",
        icon: Promotion,
        color: "#ff7a45",
      },
      {
        label: "私信总数",
        value: overview.messageNumber || 0,
        tip: "全站私信记录",
        icon: Message,
        color: "#f56c6c",
      },
      {
        label: "总播放量",
        value: compactNumber(overview.totalPlayNumber),
        tip: `点赞合计 ${compactNumber(overview.totalLikeNumber)}`,
        icon: VideoPlay,
        color: "#13c2c2",
      },
      {
        label: "管理员",
        value: overview.adminNumber || 0,
        tip: `封禁中 ${overview.banNumber || 0}`,
        icon: Lock,
        color: "#722ed1",
      },
    ]);

    const currentTrend = computed(() => overview[trendType.value] || []);

    const maxTrend = computed(() => {
      const numbers = currentTrend.value.map((item) => Number(item.number) || 0);
      return numbers.length ? Math.max(...numbers) : 0;
    });

    onMounted(() => {
      loadOverview();
    });

    //加载概览数据
    async function loadOverview() {
      try {
        const res = await getOverview(store.token, store.userId);
        if (res.data.code === 1) {
          Object.assign(overview, res.data.data);
        } else {
          toast(msgOf(res));
        }
      } catch (error) {
        toast("加载概览数据失败");
      }
    }

    //柱状图高度，按当前最大值等比缩放
    function barHeight(number) {
      const max = maxTrend.value;
      if (!max) return "4px";
      return Math.max(4, Math.round((Number(number) / max) * 160)) + "px";
    }

    function barColor(number) {
      if (!number) return "#e4e7ed";
      return trendType.value === "userTrend"
        ? "#0E9C85"
        : trendType.value === "videoTrend"
        ? "#00b07c"
        : "#7c5cff";
    }

    function keywordWidth(count) {
      const max = Math.max(...(overview.hotKeyWords || []).map((i) => i.count || 0), 1);
      return Math.max(6, Math.round(((count || 0) / max) * 100)) + "%";
    }

    function shortDate(date) {
      if (!date) return "";
      const parts = String(date).split("-");
      return parts.length === 3 ? `${Number(parts[1])}/${Number(parts[2])}` : date;
    }

    return {
      store,
      overview,
      trendType,
      statCards,
      currentTrend,
      loadOverview,
      barHeight,
      barColor,
      keywordWidth,
      shortDate,
      compactNumber,
    };
  },
};
</script>

<style scoped lang="scss">
@use "../systemPanel.scss" as *;

.overview-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* ============ 指标卡片 ============ */
.stat-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.stat-card {
  background-color: white;
  border-radius: 8px;
  padding: 18px;
  display: flex;
  align-items: center;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
}

.stat-icon {
  width: 46px;
  height: 46px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.stat-body {
  margin-left: 14px;
  min-width: 0;
}

.stat-label {
  font-size: 13px;
  color: #8D9794;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: #1C2321;
  line-height: 1.3;
}

.stat-tip {
  font-size: 12px;
  color: #c0c4cc;
}

/* ============ 趋势图 ============ */
.chart-wrap {
  padding: 6px 0 0;
}

.chart-bars {
  display: flex;
  align-items: flex-end;
  height: 210px;
  border-bottom: 1px solid #ebeef5;
}

.chart-col {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  height: 100%;
}

.chart-value {
  font-size: 12px;
  color: #5C6664;
  margin-bottom: 4px;
}

.chart-bar-track {
  flex: 1;
  width: 100%;
  display: flex;
  align-items: flex-end;
  justify-content: center;
}

.chart-bar {
  width: 46%;
  border-radius: 4px 4px 0 0;
  transition: height 0.3s ease;
}

.chart-label {
  font-size: 12px;
  color: #8D9794;
  padding-top: 8px;
}

/* ============ 底部两栏 ============ */
.bottom-grid {
  display: grid;
  grid-template-columns: 3fr 2fr;
  gap: 16px;
}

.cover {
  width: 60px;
  height: 38px;
  object-fit: cover;
  border-radius: 4px;
  background-color: #f0f2f5;
}

.link {
  color: #1C2321;
  text-decoration: none;

  &:hover {
    color: #0E9C85;
  }
}

.sub-text {
  font-size: 12px;
  color: #8D9794;
  margin-top: 2px;
}

/* ============ 搜索词榜 ============ */
.keyword-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.keyword-row {
  display: flex;
  align-items: center;
  gap: 10px;
}

.rank {
  width: 20px;
  height: 20px;
  border-radius: 4px;
  background-color: #f4f4f5;
  color: #909399;
  font-size: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;

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

.keyword-word {
  width: 110px;
  font-size: 13px;
  color: #1C2321;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex-shrink: 0;
}

.keyword-bar-track {
  flex: 1;
  height: 8px;
  background-color: #f0f2f5;
  border-radius: 4px;
  overflow: hidden;
}

.keyword-bar {
  display: block;
  height: 100%;
  background-color: #0E9C85;
  border-radius: 4px;
}

.keyword-count {
  width: 40px;
  text-align: right;
  font-size: 13px;
  color: #5C6664;
  flex-shrink: 0;
}

/* ============ 关注项 ============ */
.alert-list {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 14px;
}

.alert-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 16px;
  border-radius: 6px;
  background-color: #f8fafc;
  text-decoration: none;
  transition: background-color 0.2s ease;

  &:hover {
    background-color: #E7F5F1;
  }
}

.alert-num {
  font-size: 20px;
  font-weight: 700;
  color: #0E9C85;
}

.alert-text {
  font-size: 13px;
  color: #5C6664;
}
</style>
