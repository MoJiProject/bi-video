<template>
  <div class="relations">
    <aside>
      <button :class="{ active: type === 8 }" @click="changeMenu(8)">关注 {{ store.homeUserInformation.followNumber }}</button>
      <button :class="{ active: type === 9 }" @click="changeMenu(9)">粉丝 {{ store.homeUserInformation.fansNumber }}</button>
    </aside>
    <section>
      <h2>{{ store.userId === homeUserId ? '我的' : 'TA的' }}{{ type === 8 ? '关注' : '粉丝' }}</h2>
      <div class="space-search">
        <el-input v-model="keyword" placeholder="输入关键词" clearable maxlength="20"
          @keyup.enter="search" @clear="search"/>
        <el-button type="primary" @click="search">搜索</el-button>
      </div>
      <loadingIndicator v-if="loading" min-height="200px"/>
      <AppEmptyState v-else-if="loadError" kind="unavailable" title="暂时无法加载列表"
        :description="loadError" action-text="重新加载" @action="loadRelations"/>
      <template v-else>
        <div v-if="users.length" class="relations-grid" role="list">
          <div v-for="user in users" :key="user.id" class="relation" role="listitem">
            <a :href="'./home?userId=' + user.id" target="_blank" rel="noopener noreferrer" :aria-label="`${user.userName}的个人空间`"><img :src="user.avatarAddress" alt=""></a>
            <div class="profile">
              <a :href="'./home?userId=' + user.id" target="_blank" rel="noopener noreferrer" :title="user.userName">{{ user.userName }}</a>
              <p :title="user.introduce">{{ user.introduce || '还没有填写个人简介' }}</p>
            </div>
            <div v-if="user.id !== store.userId" class="relation-actions">
              <el-button :loading="busyId === user.id" :disabled="busyId !== null || messagingId !== null" @click="setFollow(user)">
                {{ user.isFollowFlag ? '取消关注' : (type === 9 && store.userId === homeUserId ? '回关' : '关注') }}
              </el-button>
              <el-button :loading="messagingId === user.id" :disabled="busyId !== null || messagingId !== null" @click="sendMessage(user)">发消息</el-button>
            </div>
          </div>
        </div>
        <AppEmptyState v-if="!users.length" :kind="appliedKeyword ? 'search' : 'empty'"
          :title="appliedKeyword ? '没有找到相关用户' : type === 8 ? '暂无关注' : '暂无粉丝'"
          :description="appliedKeyword ? '试试其他昵称或关键词' : '与喜欢的创作者和朋友建立更多连接'"/>
        <el-pagination v-if="total" v-model:current-page="pageNum" :page-size="24"
          layout="total, prev, pager, next, jumper" :total="total"/>
      </template>
    </section>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { useGlobalStore } from '@/store/store';
import { fansOrFollow, addFollow, deleteFollow, selectUserInfo } from '@/api/home/user';
import { addDialogue } from '@/api/privateMessage';
import loadingIndicator from '@/components/loadingIndicator.vue';

const props = defineProps({ type: { type: Number, required: true } });
const store = useGlobalStore();
const router = useRouter();
const homeUserId = parseInt(new URL(window.location).searchParams.get('userId')) || store.userId;
const users = ref([]);
const total = ref(0);
const loading = ref(true);
const loadError = ref('');
const busyId = ref(null);
const messagingId = ref(null);
const pageNum = ref(1);
const keyword = ref('');
const appliedKeyword = ref('');
let requestId = 0;

function search() {
  pageNum.value = 1;
  appliedKeyword.value = keyword.value.trim();
}

function changeMenu(menu) {
  const profile = store.homeUserInformation;
  if (store.userId !== homeUserId && !(menu === 8 ? profile.publicFollowList : profile.publicFansList)) {
    ElMessage.info('由于该用户隐私设置，列表不可见');
    return;
  }
  store.setHomeMenu(menu, homeUserId, true);
}

async function loadRelations() {
  if (!store.homeUserInformation.id) return;
  const current = ++requestId;
  loading.value = true;
  loadError.value = '';
  try {
    const res = await fansOrFollow(homeUserId, store.userId, props.type, pageNum.value, appliedKeyword.value, store.token);
    if (current !== requestId) return;
    users.value = res.data.data.records;
    total.value = res.data.data.total;
  } catch (error) {
    if (current === requestId) {
      users.value = [];
      total.value = 0;
      loadError.value = error.message;
    }
  } finally {
    if (current === requestId) {
      loading.value = false;
      store.setHomeLoad(true, 'homeBody');
      store.setHomeLoadMenuList(props.type);
    }
  }
}
watch(() => props.type, () => {
  pageNum.value = 1;
  keyword.value = '';
  appliedKeyword.value = '';
}, { flush: 'sync' });
watch([() => store.homeUserInformation.id, () => props.type, pageNum, appliedKeyword], loadRelations, { immediate: true });

async function setFollow(user) {
  if (busyId.value !== null || messagingId.value !== null) return;
  if (!store.userId) {
    store.loginDialogVisible = true;
    return;
  }
  busyId.value = user.id;
  try {
    const wasFollowing = !!user.isFollowFlag;
    const action = user.isFollowFlag ? deleteFollow : addFollow;
    await action(store.token, store.userId, user.id);
    user.isFollowFlag = user.isFollowFlag ? 0 : 1;
    ElMessage.success(wasFollowing ? '已取消关注' : '关注成功');
    const profile = await selectUserInfo(homeUserId);
    store.setHomeUserInformation(profile.data.data);
    if (store.userId === homeUserId) {
      store.setUserInformation({ ...store.userInformation, ...profile.data.data });
      if (props.type === 8 && wasFollowing) {
        if (users.value.length === 1 && pageNum.value > 1) pageNum.value--;
        else await loadRelations();
      }
    }
  } catch (error) {
    ElMessage.error(error.message);
  } finally {
    busyId.value = null;
  }
}

async function sendMessage(user) {
  if (busyId.value !== null || messagingId.value !== null) return;
  if (!store.userId) {
    store.loginDialogVisible = true;
    return;
  }
  messagingId.value = user.id;
  try {
    await addDialogue(store.token, { dialogueId: user.id });
    await router.push({ path: '/message', query: { dialogueId: user.id } });
  } catch (error) {
    ElMessage.error(error.message);
  } finally {
    messagingId.value = null;
  }
}
</script>

<style scoped>
.relations { box-sizing: border-box; display: flex; gap: var(--gap-6); padding: var(--gap-5) var(--page-pad); font-family: var(--el-font-family); }
aside { display: flex; flex: none; flex-direction: column; width: 170px; gap: 12px; }
aside button { padding: 14px; border: 0; border-radius: 6px; cursor: pointer; background: #EFF3F2; }
aside .active { color: white; background: #0FA68E; }
section { flex: 1; min-width: 0; }
h2 { margin: 0 0 20px; font-size: 24px; }
.el-input { max-width: 260px; margin-bottom: 16px; }
.relations-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(min(100%, 250px), 1fr)); gap: 14px; }
.relation { box-sizing: border-box; display: grid; grid-template-columns: 48px minmax(0, 1fr); align-content: start; align-items: start; gap: 10px 12px; min-width: 0; padding: 16px; border: 1px solid var(--line); border-radius: 12px; background: #fff; }
.relation > a { grid-column: 1; grid-row: 1 / span 2; border-radius: 50%; }
.relation img { display: block; width: 48px; height: 48px; border-radius: 50%; object-fit: cover; }
.profile { grid-column: 2; min-width: 0; }
.profile > a { display: block; overflow: hidden; color: var(--ink); font-size: 14px; font-weight: 500; line-height: 22px; text-overflow: ellipsis; white-space: nowrap; }
.profile > a:hover { color: var(--brand-hover); }
.profile > p { margin: 6px 0 0; overflow: hidden; color: var(--ink-3); font-size: 12px; line-height: 20px; text-overflow: ellipsis; white-space: nowrap; }
.relation-actions { grid-column: 2; display: flex; flex-wrap: wrap; gap: 8px; min-width: 0; }
.relation-actions > .el-button { flex: none; width: auto; max-width: 100%; height: 30px; margin: 0; padding: 0 12px; border-radius: 7px; font-size: 12px; }
.relation a:focus-visible { outline: 2px solid var(--brand); outline-offset: 3px; }
a { color: inherit; text-decoration: none; }
p, .no-data { color: #8D9794; }
.no-data { padding: 60px; text-align: center; }
.el-pagination { margin-top: 24px; }
@media (max-width: 720px) {
  .relations { flex-direction: column; padding: var(--gap-4) var(--page-pad); }
  aside { flex-direction: row; width: 100%; }
  aside { flex-wrap: wrap; }
  aside button { flex: none; }
  .relations-grid { gap: 12px; }
  .relation { padding: 12px; }
  .no-data { padding: 40px 12px; }
}
</style>
