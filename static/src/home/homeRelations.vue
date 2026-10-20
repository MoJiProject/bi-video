<template>
  <div class="relations">
    <aside>
      <button :class="{ active: type === 8 }" @click="changeMenu(8)">关注 {{ store.homeUserInformation.followNumber }}</button>
      <button :class="{ active: type === 9 }" @click="changeMenu(9)">粉丝 {{ store.homeUserInformation.fansNumber }}</button>
    </aside>
    <section>
      <h2>{{ store.userId === homeUserId ? '我的' : 'TA的' }}{{ type === 8 ? '关注' : '粉丝' }}</h2>
      <el-input v-model="keyword" placeholder="输入关键词" clearable maxlength="20"
        @keyup.enter="search" @clear="search"/>
      <loadingIndicator v-if="loading" min-height="200px"/>
      <template v-else>
        <div v-for="user in users" :key="user.id" class="relation">
          <a :href="'./home?userId=' + user.id" target="_blank"><img :src="user.avatarAddress" alt=""></a>
          <div class="profile">
            <a :href="'./home?userId=' + user.id" target="_blank">{{ user.userName }}</a>
            <p>{{ user.introduce }}</p>
          </div>
          <el-button v-if="user.id !== store.userId" :loading="busyId === user.id" @click="setFollow(user)">
            {{ user.isFollowFlag ? '已关注' : '关注' }}
          </el-button>
        </div>
        <div v-if="!users.length" class="no-data">暂无数据</div>
        <el-pagination v-if="total" v-model:current-page="pageNum" :page-size="24"
          layout="prev, pager, next" :total="total"/>
      </template>
    </section>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue';
import { ElMessage } from 'element-plus';
import { useGlobalStore } from '@/store/store';
import { fansOrFollow, addFollow, deleteFollow } from '@/api/home/user';
import loadingIndicator from '@/components/loadingIndicator.vue';

const props = defineProps({ type: { type: Number, required: true } });
const store = useGlobalStore();
const homeUserId = parseInt(new URL(window.location).searchParams.get('userId')) || store.userId;
const users = ref([]);
const total = ref(0);
const loading = ref(true);
const busyId = ref(null);
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

watch([() => store.homeUserInformation.id, () => props.type, pageNum, appliedKeyword], async ([id]) => {
  if (!id) return;
  const current = ++requestId;
  loading.value = true;
  try {
    const res = await fansOrFollow(homeUserId, store.userId, props.type, pageNum.value, appliedKeyword.value, store.token);
    if (current !== requestId) return;
    users.value = res.data.data.records;
    total.value = res.data.data.total;
  } catch (error) {
    ElMessage.error(error.message);
  } finally {
    if (current === requestId) {
      loading.value = false;
      store.setHomeLoad(true, 'homeBody');
      store.setHomeLoadMenuList(props.type);
    }
  }
}, { immediate: true });

async function setFollow(user) {
  if (!store.userId) {
    store.loginDialogVisible = true;
    return;
  }
  busyId.value = user.id;
  try {
    const action = user.isFollowFlag ? deleteFollow : addFollow;
    await action(store.token, store.userId, user.id);
    user.isFollowFlag = user.isFollowFlag ? 0 : 1;
  } catch (error) {
    ElMessage.error(error.message);
  } finally {
    busyId.value = null;
  }
}
</script>

<style scoped>
.relations { box-sizing: border-box; display: flex; gap: var(--gap-6); padding: var(--gap-5) var(--page-pad); }
aside { display: flex; flex: none; flex-direction: column; width: 170px; gap: 12px; }
aside button { padding: 14px; border: 0; border-radius: 6px; cursor: pointer; background: #EFF3F2; }
aside .active { color: white; background: #0FA68E; }
section { flex: 1; min-width: 0; }
h2 { margin: 0 0 20px; font-size: 24px; }
.el-input { max-width: 260px; margin-bottom: 16px; }
.relation { display: flex; align-items: center; gap: 20px; padding: 20px 0; border-bottom: 1px solid #EFF3F2; }
.relation img { width: 60px; height: 60px; border-radius: 50%; }
.profile { flex: 1; min-width: 0; overflow-wrap: anywhere; }
a { color: inherit; text-decoration: none; }
p, .no-data { color: #8D9794; }
.no-data { padding: 60px; text-align: center; }
.el-pagination { margin-top: 24px; }
@media (max-width: 720px) {
  .relations { flex-direction: column; padding: var(--gap-4) var(--page-pad); }
  aside { flex-direction: row; width: 100%; }
  aside button { flex: 1; }
  .relation { gap: 12px; }
  .relation img { width: 44px; height: 44px; }
  .relation > .el-button { flex: none; padding: 8px 12px; }
  .no-data { padding: 40px 12px; }
}
</style>
