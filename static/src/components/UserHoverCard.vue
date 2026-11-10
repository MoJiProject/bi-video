<template>
  <!-- 受控模式：父级通过 v-if 控制显隐，通过 :style 提供固定定位（left/top），
       用于 @提及等由外部状态驱动的场景（取代旧 utils/userInfo.vue） -->
  <div
    v-if="controlled"
    class="user-hover-fixed"
    @mouseenter="emit('card-mouseenter')"
    @mouseleave="emit('card-mouseleave')"
  >
    <section v-if="profile" class="user-profile-card" aria-label="个人资料" @click.stop>
      <div class="profile-cover" :style="profile.backgroundUrl ? { backgroundImage: `url(${profile.backgroundUrl})` } : {}"></div>
      <div class="profile-identity">
        <a class="profile-avatar-link" :href="homeHref" :aria-label="`${profile.nickname}的个人空间`">
          <img :src="profile.avatarUrl" alt="">
        </a>
        <div>
          <a class="profile-name" :href="homeHref" :title="profile.nickname" :class="{ 'rainbow-name': isRainbowName }" :style="selfNameColor ? { color: selfNameColor } : {}">{{ profile.nickname }}</a>
          <LevelBadge :level="profile.level" size="sm" />
          <GenderIcon :gender="profile.gender" :size="14" />
        </div>
      </div>
      <div class="profile-body">
        <div class="profile-stats">
          <a :href="`/home?userId=${userId}&homeMenu=8`" :aria-label="`${profile.nickname}的关注列表`"><strong>{{ profile.followingCount ?? '--' }}</strong><span>关注</span></a>
          <a :href="`/home?userId=${userId}&homeMenu=9`" :aria-label="`${profile.nickname}的粉丝列表`"><strong>{{ profile.followerCount ?? '--' }}</strong><span>粉丝</span></a>
          <div><strong>{{ profile.likeReceived ?? '--' }}</strong><span>获赞</span></div>
        </div>
        <p class="profile-signature">{{ profile.signature || '还没有填写个人简介' }}</p>
        <div class="profile-actions">
          <el-button size="small" :type="profile.followed ? '' : 'primary'" :loading="following"
            :disabled="isSelf || messaging" :title="isSelf ? '不能关注自己' : ''"
            :aria-pressed="!!profile.followed" @click="toggleFollow">
            <FollowIcon :followed="profile.followed" :size="12" />
            {{ profile.followed ? '已关注' : '关注' }}
          </el-button>
          <el-button size="small" :loading="messaging" :disabled="isSelf || following"
            :title="isSelf ? '不能给自己发消息' : ''" @click="sendMessage">发消息</el-button>
        </div>
      </div>
    </section>
    <p v-else-if="loading" class="profile-status" role="status">正在加载个人资料…</p>
    <div v-else-if="errorMessage" class="profile-status" role="alert">
      <p>{{ errorMessage }}</p>
      <el-button text type="primary" @click="loadProfile">重新加载</el-button>
    </div>
  </div>
  <span v-else class="user-hover" @click.stop>
    <el-popover trigger="hover" :trigger-keys="['Space']" :placement="popoverPlacement"
      :width="300" :show-after="200" :hide-after="250" :show-arrow="false" :teleported="teleported"
      :popper-style="{ maxWidth: 'calc(100vw - 32px)', padding: '0', borderRadius: '16px', overflow: 'hidden' }"
      @show="loadProfile">
      <template #reference>
        <!-- 提供默认触发器（头像+昵称），也可通过 #reference 插槽传入任意图标/用户名元素作为悬停触发器 -->
        <slot name="reference">
          <a class="user-hover-trigger" :href="homeHref"
            :style="{ '--user-avatar-size': `${avatarSize}px` }"
            :aria-label="`${nickname}，查看主页，按空格查看个人资料`">
            <img :src="avatar" alt=""><span :class="{ 'rainbow-name': isRainbowName && !nameColorDisabled }" :style="selfNameColor && !nameColorDisabled ? { color: selfNameColor } : {}">{{ nickname }}</span>
          </a>
        </slot>
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
          <a class="profile-name" :href="homeHref" :title="profile.nickname" :class="{ 'rainbow-name': isRainbowName }" :style="selfNameColor ? { color: selfNameColor } : {}">{{ profile.nickname }}</a>
          <LevelBadge :level="profile.level" size="sm" />
          <GenderIcon :gender="profile.gender" :size="14" />
        </div>
      </div>
      <div class="profile-body">
        <div class="profile-stats">
              <a :href="`/home?userId=${userId}&homeMenu=8`" :aria-label="`${profile.nickname}的关注列表`"><strong>{{ profile.followingCount ?? '--' }}</strong><span>关注</span></a>
              <a :href="`/home?userId=${userId}&homeMenu=9`" :aria-label="`${profile.nickname}的粉丝列表`"><strong>{{ profile.followerCount ?? '--' }}</strong><span>粉丝</span></a>
              <div><strong>{{ profile.likeReceived ?? '--' }}</strong><span>获赞</span></div>
            </div>
            <p class="profile-signature">{{ profile.signature || '还没有填写个人简介' }}</p>
            <div class="profile-actions">
              <el-button size="small" :type="profile.followed ? '' : 'primary'" :loading="following"
                :disabled="isSelf || messaging" :title="isSelf ? '不能关注自己' : ''"
                :aria-pressed="!!profile.followed" @click="toggleFollow">
                <FollowIcon :followed="profile.followed" :size="12" />
            {{ profile.followed ? '已关注' : '关注' }}
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
import { computed, onBeforeUnmount, onMounted, onUnmounted, ref, watch } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { authApi, socialApi } from '@/api/product';
import { addDialogue } from '@/api/privateMessage';
import { useGlobalStore } from '@/store/store';
import GenderIcon from './GenderIcon.vue';
import FollowIcon from './FollowIcon.vue';
import LevelBadge from './LevelBadge.vue';

const props = defineProps({
  userId: { type: Number, required: true },
  nickname: { type: String, default: '' },
  avatar: { type: String, default: '' },
  avatarSize: { type: Number, default: 40 },
  // 弹层是否 teleport 到 body（Element Plus 默认 true）。
  // 全屏场景需传 false：原生全屏只渲染全屏元素子树、网页全屏容器 z-index(3000) 高于弹层默认层级，
  // teleport 到 body 的卡片在全屏下不可见，需内联渲染在全屏元素内部
  teleported: { type: Boolean, default: true },
  // 受控模式：不使用 el-popover，由父级控制显隐（v-if）与定位（:style 传 left/top）
  controlled: { type: Boolean, default: false },
  // 受控模式的预填资料（兼容旧 userInfo.vue 的字段结构，如 store.eitUserInfo）
  initialProfile: { type: Object, default: null },
  // 关闭名字颜色（含彩色渐变）：私信分享卡片等不需要名字颜色的场景传 true
  nameColorDisabled: { type: Boolean, default: false },
  // 外部触发器要显示的名字颜色（数据库里存的值），不传时用 profile.nameColor 兜底
  nameColor: { type: String, default: null },
});
const emit = defineEmits(['card-mouseenter', 'card-mouseleave']);
const isSmallScreen = ref(typeof window !== "undefined" ? window.innerWidth <= 768 : false);
function updateScreen() { if (typeof window !== "undefined") isSmallScreen.value = window.innerWidth <= 768; }
onMounted(() => { if (typeof window !== "undefined") window.addEventListener("resize", updateScreen); });
onBeforeUnmount(() => { if (typeof window !== "undefined") window.removeEventListener("resize", updateScreen); });
const popoverPlacement = computed(() => (isSmallScreen.value ? "bottom-start" : "right-start"));

const store = useGlobalStore();
const router = useRouter();
const profile = ref(null);
const loading = ref(false);
const errorMessage = ref('');
const following = ref(false);
const messaging = ref(false);
const homeHref = computed(() => `/home?userId=${props.userId}`);
const isSelf = computed(() => props.userId === store.userId);
//目标用户名字颜色：prop 优先（父级数据已带颜色），资料加载后用 profile.nameColor 兜底；
//本人时本地预览值（store.userNameColor，已同步服务端值）优先，保证改色立即生效
const targetNameColor = computed(() => {
  if (isSelf.value) return store.userNameColor ?? null;
  return props.nameColor ?? profile.value?.nameColor ?? null;
});
//彩色名字用 rainbow-name 渐变 class 展示
const isRainbowName = computed(() => targetNameColor.value === store.userNameColorRainbow);
const selfNameColor = computed(() => (targetNameColor.value && targetNameColor.value !== store.userNameColorRainbow ? targetNameColor.value : null));
// nameColorDisabled：仅去掉外部触发器名字的颜色，hover 个人资料卡里的名字照常显示
let generation = 0;

// 受控模式：把旧 userInfo.vue 的字段结构（store.eitUserInfo）映射为卡片资料
function applyLegacyProfile(raw) {
  profile.value = {
    nickname: raw.userName ?? raw.nickname,
    avatarUrl: raw.avatarAddress ?? raw.avatarUrl,
    backgroundUrl: raw.backgroundAddress ?? raw.backgroundUrl,
    level: raw.grade ?? raw.level,
    gender: raw.gender,
    nameColor: raw.nameColor ?? null,
    followingCount: raw.followNumber ?? raw.followingCount,
    followerCount: raw.fansNumber ?? raw.followerCount,
    likeReceived: raw.likeNumber ?? raw.likeReceived,
    signature: raw.introduce ?? raw.signature,
    followed: raw.isFansFlag === 1 || raw.followed === true,
  };
}
watch(() => props.initialProfile, (raw) => {
  if (props.controlled && raw) applyLegacyProfile(raw);
}, { immediate: true });

async function loadProfile() {
  // 视频数据未就绪时 userId 可能为 0（NaN 兜底），静默跳过不报错
  if (!props.userId || loading.value || profile.value) return;
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
  if (props.controlled && props.initialProfile) {
    applyLegacyProfile(props.initialProfile);
    return;
  }
  profile.value = null;
  loading.value = false;
  errorMessage.value = '';
  following.value = false;
  messaging.value = false;
  if (props.controlled) loadProfile();
});
onMounted(() => {
  if (props.controlled) loadProfile();
});
onUnmounted(() => { generation++; });
</script>

<style scoped>
.user-hover { display: inline-flex; align-items: center; min-width: 0; max-width: 100%; font-family: var(--el-font-family); }
/* 受控模式：固定定位卡片容器（left/top 由父级 :style 透传） */
.user-hover-fixed { position: fixed; z-index: 1000; width: 300px; max-width: calc(100vw - 32px); border-radius: 16px; box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12); background: #fff; font-family: var(--el-font-family); }
.user-hover-trigger { display: inline-flex; align-items: center; gap: 10px; min-width: 0; max-width: 100%; color: inherit; text-decoration: none; font-size: 14px; font-weight: 500; line-height: 1.5; }
.user-hover-trigger > img { flex: none; width: var(--user-avatar-size); height: var(--user-avatar-size); border-radius: 50%; object-fit: cover; background: var(--fill-soft); }
.user-hover-trigger > span { min-width: 0; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.user-hover-trigger:hover { color: var(--brand-hover); transition: all 0.3s ease; }
.user-hover-trigger:focus-visible, .user-profile-card a:focus-visible { outline: 2px solid var(--brand); outline-offset: 3px; border-radius: 6px; }
.user-profile-card { color: var(--ink); background: #fff; font-family: var(--el-font-family); font-size: 13px; line-height: 1.5; cursor: auto; }
.user-profile-card * { box-sizing: border-box; }
.profile-cover { height: 76px; background: var(--brand-light-9); background-position: center; background-size: cover; }
.profile-identity { position: relative; display: flex; align-items: flex-end; gap: 10px; min-width: 0; margin: -22px 16px 0; }
.profile-identity a { color: inherit; text-decoration: none; }
.profile-avatar-link { flex: none; border-radius: 50%; }
.profile-avatar-link > img { display: block; width: 56px; height: 56px; border: 3px solid #fff; border-radius: 50%; background: var(--fill-soft); object-fit: cover; }
.profile-identity > div { display: flex; align-items: center; gap: 6px; min-width: 0; padding-bottom: 3px; }
.profile-name { min-width: 0; overflow: hidden; font-size: 15px; font-weight: 600; text-overflow: ellipsis; white-space: nowrap; }
.profile-name:hover { color: var(--brand-hover); transition: all 0.3s ease; }
.profile-gender { font-size: 14px; margin-left: 4px; vertical-align: middle; font-weight: 700; }
.profile-gender.male { color: #1890ff; }
.profile-gender.female { color: #ff4d4f; }
.profile-body { padding: 8px 16px 10px; }
.profile-stats { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 8px; }
.profile-stats > div, .profile-stats > a { display: flex; align-items: center; gap: 5px; min-width: 0; color: var(--ink-3); font-size: 12px; }
.profile-stats a { color: inherit; text-decoration: none; }
.profile-stats a:hover strong { color: var(--brand-hover); transition: all 0.3s ease; }
.profile-stats strong { color: var(--ink-2); font-size: 14px; font-weight: 600; overflow-wrap: anywhere; }
.profile-signature { display: -webkit-box; margin: 10px 0 12px; overflow: hidden; -webkit-box-orient: vertical; -webkit-line-clamp: 2; color: var(--ink-2); font-size: 12px; line-height: 1.7; overflow-wrap: anywhere; }
.profile-actions { display: flex; flex-wrap: wrap; justify-content: center; gap: 8px; }
.profile-actions .el-button { flex: none; min-width: 94px; max-width: 100%; height: 32px; margin: 0; padding: 0 14px; border-radius: 8px; font-size: 12px; }
.profile-status { margin: 0; padding: 20px 16px; color: var(--ink-2); font-size: 13px; }
.profile-status p { margin: 0 0 8px; }
</style>
