<template>
  <div class="creator-center">
    <header class="creator-header">
      <mainHead class="header-stack-search" :head2-flag="true"/>
    </header>
    <el-container v-if="store.userId !== null && pageLoad" class="creator-workspace common-layout">
      <el-aside width="100%" class="contribute-aside">
        <AsideComponent />
      </el-aside>
      <el-main class="contribute-main">
        <component :is="currentContentComponent" class="contribute-content" />
      </el-main>
    </el-container>
    <div v-else class="contribute-empty">
      <AppEmptyState :kind="loginError && store.token ? 'unavailable' : 'login'"
        :title="loginError || '登录后进入创作中心'">
        <template #actions>
          <el-button type="primary" @click="store.loginDialogVisible = true">立即登录</el-button>
          <el-button v-if="store.token" @click="loadUser">重新加载</el-button>
        </template>
      </AppEmptyState>
    </div>
  </div>
</template>

<script>
import { onMounted, computed, ref, watch } from "vue";
import { useRoute } from "vue-router";
import { authApi } from "../api/product";
import { mapUser } from "../api/home/user";
import { useGlobalStore } from "../store/store";
import "element-plus/theme-chalk/el-message.css";
import { ElMessage } from "element-plus";
import AsideComponent from './AsideComponent.vue';
import MainComponent from "./MainComponent.vue";
import EditContribute from "./EditContribute.vue";
import SubPage1 from "./SubPage1.vue";
import SubPage2 from "./SubPage2.vue";
import mainHead from "../components/mainHead.vue";

export default {
  name: "ContributePage",
  components: {
    AsideComponent,
    MainComponent,
    EditContribute,
    SubPage1,
    SubPage2,
    mainHead,
  },
  setup() {
    const pageLoad = ref(false);
    const loginError = ref('');
    const store = useGlobalStore();
    const route = useRoute();
    const contentComponentMap = {
      "/contribute/edit": EditContribute,
      "/contribute/subpage1": SubPage1,
      "/contribute/subpage2": SubPage2,
    };
    const currentContentComponent = computed(() => contentComponentMap[route.path] || MainComponent);

    onMounted(() => {
      document.title = "创作中心 - 青芒视频";
      window.scrollTo({ top: 0, behavior: "smooth" });
      if (store.token) loadUser();
    });
    watch(() => store.token, token => { if (token) loadUser(); else pageLoad.value = false; });

    async function loadUser() {
      loginError.value = '';
      try {
        const profile = mapUser(await authApi.me());
        store.setUserId(profile.id);
        store.setUserInformation(profile);
        pageLoad.value = true;
      } catch (error) {
        pageLoad.value = false;
        loginError.value = error.message || "请先登录创作中心";
        store.loginDialogVisible = true;
        ElMessage.error(loginError.value);
      }
    }

    return { store, pageLoad, loginError, loadUser, currentContentComponent };
  },
};
</script>

<style scoped lang="scss">
.creator-header { position: sticky; top: 0; z-index: var(--z-head); background: #fff; }
.contribute-empty { width: calc(100% - var(--page-pad) - var(--page-pad)); max-width: 560px; margin: clamp(32px, 8vh, 88px) auto; }
.common-layout { width: 100%; max-width: var(--page-max); margin: 0 auto; }
.contribute-aside, .contribute-main, .contribute-content { position: relative; z-index: 10; }
.contribute-main { overflow: visible; }
</style>
<style lang="scss" scoped src="./creatorLayout.scss"></style>
