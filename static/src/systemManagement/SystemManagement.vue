<template>
  <div v-if="pageLoad && isAdmin" class="system-layout">
    <header class="system-header">
      <div class="brand">
        <img src="/img/logo.png" alt="logo" class="logo" />
        <span class="brand-name">系统管理后台</span>
      </div>

      <nav class="header-nav">
        <a class="nav-link" href="/" target="_blank">
          <el-icon><HomeFilled /></el-icon>主站
        </a>
        <a class="nav-link" href="/contribute" target="_blank">
          <el-icon><VideoCameraFilled /></el-icon>创作中心
        </a>
      </nav>

      <div class="header-right">
        <span class="refresh-time">{{ refreshTime }}</span>
        <el-button size="small" text @click="handleRefresh">刷新</el-button>
        <div class="user-box" @click="goHome" title="进入我的主页">
          <img :src="userAvatar" alt="头像" class="avatar" referrerpolicy="no-referrer" />
          <el-tooltip :content="userName" placement="bottom">
            <span class="user-name">{{ userName }}</span>
          </el-tooltip>
        </div>
      </div>
    </header>

    <div class="system-body">
      <aside class="system-aside">
        <div class="aside-title">功能菜单</div>
        <router-link
          v-for="item in menus"
          :key="item.path"
          :to="item.path"
          class="menu-item"
          :class="{ active: isActive(item.path) }"
        >
          <el-icon class="menu-icon"><component :is="item.icon" /></el-icon>
          <span class="menu-text">{{ item.label }}</span>
        </router-link>
      </aside>

      <main class="system-main">
        <component :is="currentComponent" :key="refreshKey" @refresh="handleRefresh" />
      </main>
    </div>
  </div>

  <div v-else-if="pageLoad" class="system-forbidden">
    <h1>您没有访问权限</h1>
    <p>该页面仅限管理员访问，如需权限请联系其他管理员。</p>
    <el-button type="primary" @click="goBack">返回主站</el-button>
  </div>
</template>

<script>
import { computed, onMounted, ref } from "vue";
import { useRoute } from "vue-router";
import {
  HomeFilled,
  VideoCameraFilled,
  DataAnalysis,
  UserFilled,
  VideoCamera,
  ChatDotRound,
  Promotion,
  Message,
  Key,
  Document,
} from "@element-plus/icons-vue";
import { useGlobalStore } from "../store/store";
import apiClient from "../services/apiClient";
import { toast } from "./systemCommon";
import OverviewPage from "./pages/OverviewPage.vue";
import UserManagePage from "./pages/UserManagePage.vue";
import VideoManagePage from "./pages/VideoManagePage.vue";
import CommentManagePage from "./pages/CommentManagePage.vue";
import DynamicManagePage from "./pages/DynamicManagePage.vue";
import MessageManagePage from "./pages/MessageManagePage.vue";
import KeyWordManagePage from "./pages/KeyWordManagePage.vue";
import SystemLogPage from "./pages/SystemLogPage.vue";

export default {
  name: "SystemManagement",
  components: {
    OverviewPage,
    UserManagePage,
    VideoManagePage,
    CommentManagePage,
    DynamicManagePage,
    MessageManagePage,
    KeyWordManagePage,
    SystemLogPage,
  },
  setup() {
    const store = useGlobalStore();
    const route = useRoute();
    const pageLoad = ref(false);
    const isAdmin = ref(false);
    const refreshTime = ref("");
    const refreshKey = ref(0);

    const menus = [
      { path: "/systemManagement/overview", label: "数据概览", icon: DataAnalysis, component: OverviewPage },
      { path: "/systemManagement/user", label: "用户管理", icon: UserFilled, component: UserManagePage },
      { path: "/systemManagement/video", label: "视频管理", icon: VideoCamera, component: VideoManagePage },
      { path: "/systemManagement/comment", label: "评论管理", icon: ChatDotRound, component: CommentManagePage },
      { path: "/systemManagement/dynamic", label: "动态管理", icon: Promotion, component: DynamicManagePage },
      { path: "/systemManagement/message", label: "私信管理", icon: Message, component: MessageManagePage },
      { path: "/systemManagement/keyWord", label: "搜索热词", icon: Key, component: KeyWordManagePage },
      { path: "/systemManagement/log", label: "操作日志", icon: Document, component: SystemLogPage },
    ];

    const currentComponent = computed(() => {
      const current = menus.find((item) => item.path === route.path);
      return current ? current.component : OverviewPage;
    });

    const isActive = (path) => route.path === path;

    const userName = computed(
      () => (store.userInformation && store.userInformation.userName) || "管理员"
    );
    const userAvatar = computed(
      () => (store.userInformation && store.userInformation.avatarAddress) || "/img/avatar-default.png"
    );

    onMounted(async () => {
      document.title = "系统管理后台 - 青芒视频";
      window.scrollTo({ top: 0 });
      await getUserIp();
      await checkLogin();
      pageLoad.value = true;
      stampTime();
    });

    //刷新时间戳
    function stampTime() {
      const now = new Date();
      refreshTime.value = `${String(now.getHours()).padStart(2, "0")}:${String(
        now.getMinutes()
      ).padStart(2, "0")}:${String(now.getSeconds()).padStart(2, "0")}`;
    }

    //获取用户ip和token
    async function getUserIp() {
      try {
        const response = await apiClient.get("/userIp/getUserIp");
        if (response.data.code === 1) {
          store.setUserIp(response.data.data.userIp);
          store.setToken(response.data.data.token);
        }
      } catch (error) {
        toast("网络异常，获取登录信息失败");
      }
    }

    //检查登录并确认管理员身份
    async function checkLogin() {
      try {
        const response = await apiClient.get(`/user/checkLoginFlag/${store.userIp}`);
        if (response.data.code === 1) {
          store.setUserId(response.data.data.id);
          store.setUserInformation(response.data.data);
          isAdmin.value = !!response.data.data.adminFlag;
        } else {
          isAdmin.value = false;
        }
      } catch (error) {
        isAdmin.value = false;
      }
    }

    //父组件通知子组件刷新数据
    function handleRefresh() {
      refreshKey.value += 1;
      stampTime();
    }

    function goHome() {
      window.open(`/home?homeMenu=1&userId=${store.userId}`, "_blank");
    }

    function goBack() {
      window.location.href = "/";
    }

    return {
      store,
      route,
      menus,
      pageLoad,
      isAdmin,
      refreshTime,
      currentComponent,
      isActive,
      userName,
      userAvatar,
      handleRefresh,
      goHome,
      goBack,
    };
  },
};
</script>

<style scoped>
.system-layout {
  min-height: 100vh;
  background-color: #f4f6f9;
  user-select: none;
  width: 98%;
}

/* ============ 顶部导航 ============ */
.system-header {
  height: 56px;
  background-color: white;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  display: flex;
  align-items: center;
  /* 左右留白收窄，让整体内容往左靠，右侧用户名也有足够空间 */
  padding: 0 16px;
  position: sticky;
  top: 0;
  z-index: 100;
  box-sizing: border-box;
  min-width: 0;
}

.brand {
  display: flex;
  align-items: center;
  flex-shrink: 0;
}

/* logo按原始比例完整显示，不做裁剪或拉伸 */
.logo {
  width: auto;
  height: 34px;
  max-width: 120px;
  object-fit: contain;
  object-position: left center;
  display: block;
}

.brand-name {
  margin-left: 10px;
  font-size: 18px;
  font-weight: 700;
  color: #0E9C85;
  letter-spacing: 1px;
  white-space: nowrap;
}

.header-nav {
  display: flex;
  margin-left: 32px;
  flex-shrink: 0;
}

.nav-link {
  display: flex;
  align-items: center;
  margin-right: 20px;
  color: #5C6664;
  font-size: 14px;
  text-decoration: none;
  white-space: nowrap;
  transition: color 0.2s ease;
}

.nav-link:hover {
  color: #0E9C85;
}

.nav-icon {
  width: 16px;
  height: 16px;
  margin-right: 5px;
}

.header-right {
  margin-left: auto;
  display: flex;
  align-items: center;
  min-width: 0;
  flex-shrink: 1;
  /* 右侧整体不参与收缩，保证用户名始终有完整显示空间 */
  flex-grow: 0;
}

.refresh-time {
  font-size: 12px;
  color: #8D9794;
  margin-right: 10px;
  white-space: nowrap;
}

.user-box {
  display: flex;
  align-items: center;
  margin-left: 12px;
  cursor: pointer;
  min-width: 0;
  flex-shrink: 1;
}

.avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background-color: #eee;
  object-fit: cover;
  flex-shrink: 0;
}

/* 用户名不再限死 160px，改为不换行 + 省略，收缩时优先保住右侧头像与刷新 */
.user-name {
  margin-left: 8px;
  font-size: 14px;
  color: #1C2321;
  max-width: 220px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* ============ 侧边菜单 ============ */
.system-body {
  display: flex;
  align-items: flex-start;
}

.system-aside {
  width: 200px;
  min-height: calc(100vh - 56px);
  background-color: white;
  padding: 16px 0;
  position: sticky;
  top: 56px;
  flex-shrink: 0;
}

.aside-title {
  font-size: 12px;
  color: #8D9794;
  padding: 0 20px 10px;
}

.menu-item {
  display: flex;
  align-items: center;
  height: 44px;
  padding: 0 20px;
  color: #5C6664;
  font-size: 14px;
  text-decoration: none;
  border-left: 3px solid transparent;
  transition: all 0.2s ease;
}

.menu-item:hover {
  background-color: #f4f6f9;
  color: #0E9C85;
}

.menu-item.active {
  background-color: #E7F5F1;
  border-left-color: #0E9C85;
  color: #0E9C85;
  font-weight: 600;
}

.menu-icon {
  width: 18px;
  margin-right: 10px;
  font-size: 16px;
  flex-shrink: 0;
}

/* ============ 内容区 ============ */
.system-main {
  flex: 1;
  padding: 20px 16px 40px;
  min-width: 0;
}

/* ============ 无权限 ============ */
.system-forbidden {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100vh;
  color: #5C6664;
}

.system-forbidden p {
  margin: 0 0 20px;
  color: #8D9794;
  font-size: 14px;
}
</style>

<!--
  el-table-column 的 class-name 是加在 Element Plus 自己渲染的 <td> 上的，
  那不是本组件模板里的元素，拿不到 scoped 的属性标记，
  所以「操作列不换行」这类规则必须放在非 scoped 样式里才生效。
-->
<style>
.el-table .sys-actions {
  white-space: nowrap;
}

.el-table .sys-actions .el-button {
  margin-left: 8px;
}

.el-table .sys-actions .el-button:first-child {
  margin-left: 0;
}
</style>
