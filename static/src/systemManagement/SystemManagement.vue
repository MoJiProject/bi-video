<template>
  <div v-if="pageLoad && isAdmin" class="system-layout">
    <header class="system-header">
      <div class="brand">
        <img src="/img/logo.png" alt="logo" class="logo" />
        <span class="brand-name">系统管理后台</span>
      </div>

      <nav class="header-nav" aria-label="站点导航">
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
        <button type="button" class="user-box" @click="goHome" title="进入我的主页" aria-label="进入我的主页">
          <img :src="userAvatar" alt="头像" class="avatar" referrerpolicy="no-referrer" />
          <el-tooltip :content="userName" placement="bottom">
            <span class="user-name">{{ userName }}</span>
          </el-tooltip>
        </button>
      </div>
    </header>

    <div class="system-body">
      <aside ref="menuRef" class="system-aside" aria-label="后台功能菜单">
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
import { computed, onMounted, ref, watch } from "vue";
import { ElMessage } from "element-plus";
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
  Picture,
} from "@element-plus/icons-vue";
import { useGlobalStore } from "../store/store";
import { authApi } from "../api/product";
import { mapUser } from "../api/home/user";

import OverviewPage from "./pages/OverviewPage.vue";
import UserManagePage from "./pages/UserManagePage.vue";
import VideoManagePage from "./pages/VideoManagePage.vue";
import CommentManagePage from "./pages/CommentManagePage.vue";
import DynamicManagePage from "./pages/DynamicManagePage.vue";
import MessageManagePage from "./pages/MessageManagePage.vue";
import KeyWordManagePage from "./pages/KeyWordManagePage.vue";
import SystemLogPage from "./pages/SystemLogPage.vue";
import BannerManagePage from "./pages/BannerManagePage.vue";

export default {
  name: "SystemManagement",
  components: {
    HomeFilled,
    VideoCameraFilled,
    OverviewPage,
    UserManagePage,
    VideoManagePage,
    CommentManagePage,
    DynamicManagePage,
    MessageManagePage,
    KeyWordManagePage,
    SystemLogPage,
    BannerManagePage,
  },
  setup() {
    const store = useGlobalStore();
    const route = useRoute();
    const pageLoad = ref(false);
    const isAdmin = ref(false);
    const refreshTime = ref("");
    const refreshKey = ref(0);
    const menuRef = ref(null);

    watch([() => route.path, pageLoad], () => {
      const menu = menuRef.value;
      const active = menu?.querySelector(".active");
      if (!active) return;
      if (window.matchMedia("(max-width: 900px)").matches) {
        menu.scrollLeft = active.offsetLeft - (menu.clientWidth - active.offsetWidth) / 2;
      } else {
        menu.scrollTop = active.offsetTop - (menu.clientHeight - active.offsetHeight) / 2;
      }
    }, { flush: "post" });

    const menus = [
      { path: "/systemManagement/overview", label: "数据概览", icon: DataAnalysis, component: OverviewPage },
      { path: "/systemManagement/user", label: "用户管理", icon: UserFilled, component: UserManagePage },
      { path: "/systemManagement/video", label: "视频管理", icon: VideoCamera, component: VideoManagePage },
      { path: "/systemManagement/comment", label: "评论管理", icon: ChatDotRound, component: CommentManagePage },
      { path: "/systemManagement/dynamic", label: "动态管理", icon: Promotion, component: DynamicManagePage },
      { path: "/systemManagement/message", label: "私信管理", icon: Message, component: MessageManagePage },
      { path: "/systemManagement/keyWord", label: "搜索热词", icon: Key, component: KeyWordManagePage },
      { path: "/systemManagement/banner", label: "Banner管理", icon: Picture, component: BannerManagePage },
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
    // token 由 /auth/login 返回后存在本地，这里不再向后端要 IP + token。
    async function getUserIp(){
      store.setUserIp("0.0.0.0");
    }

    //检查登录并确认管理员身份
    // ChecklLogin 接口已移除，改用 /auth/me 判断是否登录及是否为管理员
    async function checkLogin() {
      try {
        const me = await authApi.me();
        store.setUserId(me.userId);
        store.setUserInformation(mapUser(me));
        isAdmin.value = me.role === 1;
      } catch (error) {
        isAdmin.value = false;
        ElMessage.error(error.message || "管理员身份验证失败");
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
      refreshKey,
      menuRef,
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
  min-width: 0;
  min-height: 100vh;
  background-color: #f4f6f9;
  user-select: none;
  width: 100%;
  max-width: var(--page-max);
  margin: 0 auto;
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
  border: 0;
  padding: 0;
  background: transparent;
  font: inherit;
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
  box-sizing: border-box;
  max-height: calc(100vh - 56px);
  overflow-y: auto;
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
  box-sizing: border-box;
  max-width: 100%;
}

.system-main > * {
  min-width: 0;
  max-width: 100%;
}

.menu-item:focus-visible,
.nav-link:focus-visible,
.user-box:focus-visible {
  outline: 2px solid #0E9C85;
  outline-offset: -2px;
}

@media (max-width: 1200px) {
  .system-aside { width: 168px; }
  .header-nav { margin-left: 20px; }
  .user-name { max-width: 120px; }
}

@media (max-width: 900px) {
  .system-header { position: relative; }
  .refresh-time { display: none; }
  .header-nav { margin-left: 16px; }
  .nav-link { margin-right: 12px; }
  .system-body { flex-direction: column; }
  .system-aside {
    display: flex;
    align-items: center;
    flex-wrap: wrap;
    gap: 6px;
    width: 100%;
    min-height: 0;
    max-height: none;
    padding: 8px 12px;
    top: 0;
    z-index: 90;
    overflow-x: visible;
    overflow-y: visible;
    overscroll-behavior-x: contain;
    border-bottom: 1px solid #e5e7eb;
  }
  .aside-title { display: none; }
  .menu-item {
    flex: 0 0 auto;
    height: 40px;
    padding: 0 12px;
    border-left: 0;
    border-bottom: 2px solid transparent;
    border-radius: 6px;
    white-space: nowrap;
  }
  .menu-item.active { border-bottom-color: #0E9C85; }
  .menu-icon { margin-right: 6px; }
  .system-main { width: 100%; padding: 16px 12px 32px; }
}

@media (max-width: 640px) {
  .system-header {
    display: grid;
    grid-template-columns: minmax(0, 1fr) auto;
    gap: 8px 12px;
    height: auto;
    padding: 10px 12px 8px;
  }
  .brand { min-width: 0; }
  .logo { max-width: 56px; height: 28px; }
  .brand-name { font-size: 15px; letter-spacing: 0; margin-left: 6px; }
  .header-right { grid-column: 2; grid-row: 1; margin-left: 0; flex-shrink: 0; }
  .header-right :deep(.el-button) { min-height: 36px; padding: 8px; }
  .user-box { margin-left: 6px; }
  .user-name { display: none; }
  .header-nav { grid-column: 1 / -1; gap: 20px; margin-left: 0; }
  .nav-link { min-height: 32px; margin-right: 0; gap: 4px; font-size: 13px; }
  .system-main { padding: 12px 12px 24px; }
  .system-forbidden { height: auto; min-height: 100vh; padding: 24px; box-sizing: border-box; text-align: center; }
  .system-forbidden h1 { font-size: 24px; }
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
/* index.html 给全站 body 设了 min-width: 1280px，会把媒体查询的断口
   和实际布局宽度脱钩（视口已到 640px 以下，body 仍是 1280px 宽，
   响应式规则全部命中但不产生任何视觉效果）。
   后台管理已按 900px / 640px 断点做了完整适配，
   这里仿照站内消息页(body:has(.message-page))的写法解除最小宽度限制。 */
body:has(.system-layout),
body:has(.system-forbidden) {
  min-width: 0;
}

.system-layout .el-table .sys-actions {
  white-space: nowrap;
}

.system-layout .el-table .sys-actions .el-button {
  margin-left: 8px;
}

.system-layout .el-table .sys-actions .el-button:first-child {
  margin-left: 0;
}

/* Dialogs are teleported outside the management layout. */
.sys-dialog {
  max-width: calc(100vw - 24px);
  max-height: calc(100vh - 24px);
  max-height: calc(100dvh - 24px);
  display: flex;
  flex-direction: column;
  box-sizing: border-box;
}

.sys-dialog .el-dialog__header,
.sys-dialog .el-dialog__footer {
  flex-shrink: 0;
}

.sys-dialog .el-dialog__body {
  min-height: 0;
  overflow-y: auto;
  overflow-wrap: anywhere;
  overscroll-behavior: contain;
}

.sys-dialog .el-dialog__footer {
  display: flex;
  justify-content: flex-end;
  flex-wrap: wrap;
  gap: 8px;
}

.sys-dialog .el-dialog__footer .el-button + .el-button {
  margin-left: 0;
}

.sys-confirm-dialog {
  max-width: min(420px, calc(100vw - 24px));
  max-height: calc(100vh - 24px);
  max-height: calc(100dvh - 24px);
  overflow-y: auto;
  box-sizing: border-box;
}

@media (max-width: 640px) {
  .sys-dialog .el-form-item { flex-direction: column; align-items: stretch; }
  .sys-dialog .el-form-item__label { width: auto !important; height: auto; justify-content: flex-start; margin-bottom: 6px; }
  .sys-dialog .el-form-item__content { min-width: 0; margin-left: 0 !important; }
  .sys-dialog .el-input-number { width: 100%; }
  .sys-dialog .el-button { min-height: 36px; }
  .sys-dialog .ban-target { flex-wrap: wrap; }
}
</style>
