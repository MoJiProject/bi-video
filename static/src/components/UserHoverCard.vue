<template>
  <span class="user-hover" @click.stop>
    <el-popover trigger="hover" :trigger-keys="['Space']" placement="bottom-start"
      :width="300" :show-after="200" :hide-after="250" :show-arrow="false"
      :popper-style="{ maxWidth: 'calc(100vw - 32px)', padding: '0', borderRadius: '16px', overflow: 'hidden' }"
      @show="loadProfile">
      <template #reference>
        <a class="user-hover-trigger" :href="homeHref"
          :style="{ '--user-avatar-size': `${avatarSize}px` }"
          :aria-label="`${nickname}，查看主页，按空格查看个人资料`">
          <img :src="avatar" alt=""><span>{{ nickname }}</span>
        </a>
      </template>
      <section class="user-profile-card" aria-label="个人资料" @click.stop>
        <p v-if="loading" class="profile-status" role="status">正在加载个人资料…</p>
        <div v-else-if="errorMessage" class="profile-status" role="alert">
          <p>{{ errorMessage }}</p>
          <el-button text type="primary" @click="loadProfile">重新加载</el-button>
        </div>
        <template v-else-if="profile">
          <div class="profile-cover" :style="profile.backgroundUrl ? { backgroundImage: `url(${profile.backgroundUrl})` } : {}"></div>
          <div class="profile-identity">
            <a class="profile-avatar-link" :href="homeHref" :aria-label="`${profile.nickname}的个人空间`">
              <img :src="profile.avatarUrl" alt="">
            </a>
            <div>
              <a class="profile-name" :href="homeHref" :title="profile.nickname">{{ profile.nickname }}</a>
              <span v-if="profile.level != null" class="profile-level">Lv.{{ profile.level }}</span>
            </div>
          </div>
          <div class="profile-body">
            <div class="profile-stats">
              <div><strong>{{ profile.followingCount ?? '--' }}</strong><span>关注</span></div>
              <div><strong>{{ profile.followerCount ?? '--' }}</strong><span>粉丝</span></div>
              <div><strong>{{ profile.likeReceived ?? '--' }}</strong><span>获赞</span></div>
            </div>
            <p class="profile-signature">{{ profile.signature || '还没有填写个人简介' }}</p>
            <div class="profile-actions">
              <el-button size="small" :type="profile.followed ? '' : 'primary'" :loading="following"
                :disabled="isSelf || messaging" :title="isSelf ? '不能关注自己' : ''"
                :aria-pressed="!!profile.followed" @click="toggleFollow">
                {{ profile.followed ? '已关注' : '+ 关注' }}
              </el-button>
              <el-button size="small" :loading="messaging" :disabled="isSelf || following"
                :title="isSelf ? '不能给自己发消息' : ''" @click="sendMessage">发消息</el-button>
            </div>
          </div>
        </template>
      </section>
    </el-popover>
  </span>
</template>

<script setup>
import { computed, onUnmounted, ref, watch } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { authApi, socialApi } from '@/api/product';
import { addDialogue } from '@/api/privateMessage';
import { useGlobalStore } from '@/store/store';

const props = defineProps({
  userId: { type: Number, required: true },
  nickname: { type: String, default: '' },
  avatar: { type: String, default: '' },
  avatarSize: { type: Number, default: 40 },
});
const store = useGlobalStore();
const router = useRouter();
const profile = ref(null);
const loading = ref(false);
const errorMessage = ref('');
const following = ref(false);
const messaging = ref(false);
const homeHref = computed(() => `/home?userId=${props.userId}`);
const isSelf = computed(() => props.userId === store.userId);
let generation = 0;

async function loadProfile() {
  if (loading.value || profile.value) return;
  const current = generation;
  loading.value = true;
  errorMessage.value = '';
  try {
    const result = await authApi.profile(props.userId);
    if (current === generation) profile.value = result;
  } catch (error) {
    if (current === generation) errorMessage.value = error.message || '个人资料加载失败';
  } finally {
    if (current === generation) loading.value = false;
  }
}
function signedIn() {
  if (store.token) return true;
  store.loginDialogVisible = true;
  return false;
}
async function toggleFollow() {
  if (following.value || messaging.value || isSelf.value || !profile.value || !signedIn()) return;
  const current = generation;
  following.value = true;
  try {
    const result = await socialApi.follow(props.userId);
    if (current !== generation) return;
    const previous = !!profile.value.followed;
    profile.value.followed = result.following;
    if (typeof profile.value.followerCount === 'number') {
      profile.value.followerCount = Math.max(0, profile.value.followerCount + Number(result.following) - Number(previous));
    }
    store.setAddFollowFlag(result.following ? 1 : 2);
  } catch (error) {
    if (current === generation) ElMessage.error(error.message);
  } finally {
    if (current === generation) following.value = false;
  }
}
async function sendMessage() {
  if (messaging.value || following.value || isSelf.value || !signedIn()) return;
  const current = generation;
  const userId = props.userId;
  messaging.value = true;
  try {
    await addDialogue(store.token, { dialogueId: userId });
    if (current === generation) await router.push({ path: '/message', query: { dialogueId: userId } });
  } catch (error) {
    if (current === generation) ElMessage.error(error.message);
  } finally {
    if (current === generation) messaging.value = false;
  }
}
watch([() => props.userId, () => store.userId], () => {
  generation++;
  profile.value = null;
  loading.value = false;
  errorMessage.value = '';
  following.value = false;
  messaging.value = false;
});
onUnmounted(() => { generation++; });
</script>

<style scoped>
.user-hover { display: inline-flex; align-items: center; min-width: 0; max-width: 100%; font-family: var(--el-font-family); }
.user-hover-trigger { display: inline-flex; align-items: center; gap: 10px; min-width: 0; max-width: 100%; color: inherit; text-decoration: none; font-size: 14px; font-weight: 500; line-height: 1.5; }
.user-hover-trigger > img { flex: none; width: var(--user-avatar-size); height: var(--user-avatar-size); border-radius: 50%; object-fit: cover; background: var(--fill-soft); }
.user-hover-trigger > span { min-width: 0; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.user-hover-trigger:hover { color: var(--brand-hover); }
.user-hover-trigger:focus-visible, .user-profile-card a:focus-visible { outline: 2px solid var(--brand); outline-offset: 3px; border-radius: 6px; }
.user-profile-card { color: var(--ink); background: #fff; font-family: var(--el-font-family); font-size: 13px; line-height: 1.5; cursor: auto; }
.user-profile-card * { box-sizing: border-box; }
.profile-cover { height: 60px; background: var(--brand-light-9); background-position: center; background-size: cover; }
.profile-identity { position: relative; display: flex; align-items: flex-end; gap: 10px; min-width: 0; margin: -22px 16px 0; }
.profile-identity a { color: inherit; text-decoration: none; }
.profile-avatar-link { flex: none; border-radius: 50%; }
.profile-avatar-link > img { display: block; width: 56px; height: 56px; border: 3px solid #fff; border-radius: 50%; background: var(--fill-soft); object-fit: cover; }
.profile-identity > div { display: flex; align-items: center; gap: 6px; min-width: 0; padding-bottom: 3px; }
.profile-name { min-width: 0; overflow: hidden; font-size: 15px; font-weight: 600; text-overflow: ellipsis; white-space: nowrap; }
.profile-name:hover { color: var(--brand-hover); }
.profile-level { flex: none; padding: 1px 5px; border-radius: 4px; background: var(--brand-light-9); color: var(--brand-hover); font-size: 10px; }
.profile-body { padding: 14px 16px 16px; }
.profile-stats { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 8px; }
.profile-stats > div { display: flex; align-items: center; gap: 5px; min-width: 0; color: var(--ink-3); font-size: 12px; }
.profile-stats strong { color: var(--ink-2); font-size: 14px; font-weight: 600; overflow-wrap: anywhere; }
.profile-signature { display: -webkit-box; margin: 12px 0 14px; overflow: hidden; -webkit-box-orient: vertical; -webkit-line-clamp: 2; color: var(--ink-2); font-size: 12px; line-height: 1.7; overflow-wrap: anywhere; }
.profile-actions { display: flex; flex-wrap: wrap; gap: 8px; }
.profile-actions .el-button { flex: none; min-width: 94px; max-width: 100%; height: 32px; margin: 0; padding: 0 14px; border-radius: 8px; font-size: 12px; }
.profile-status { margin: 0; padding: 20px 16px; color: var(--ink-2); font-size: 13px; }
.profile-status p { margin: 0 0 8px; }
</style>
