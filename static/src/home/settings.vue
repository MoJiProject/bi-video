<template>
  <div class="settings">
    <h2>隐私设置</h2>
    <loadingIndicator v-if="loading" min-height="160px"/>
    <div v-else>
      <div v-for="setting in privacySettings" :key="setting.key" class="setting">
        <span>{{ setting.label }}</span>
        <el-switch :model-value="store.homeUserInformation[setting.key] === 1"
          :disabled="saving" @change="savePrivacy(setting.key, $event)"/>
      </div>
      <p>投币、点赞记录和生日仅自己可见，追剧功能暂未开放。</p>
    </div>
    <h2>排序设置</h2>
    <div ref="sortElement">
      <div v-for="item in divs" :key="item.class" :data-section="item.class" class="sort-item">
        {{ item.label }} <span>拖动排序</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, onBeforeUnmount, ref, watch } from 'vue';
import { ElMessage } from 'element-plus';
import Sortable from 'sortablejs';
import { useGlobalStore } from '@/store/store';
import { putHomeSettings } from '@/api/home/user';
import loadingIndicator from '@/components/loadingIndicator.vue';

const store = useGlobalStore();
const loading = ref(true);
const saving = ref(false);
const sortElement = ref(null);
let sortable;
const privacySettings = [
  { key: 'publicCollect', label: '公开我的收藏' },
  { key: 'publicFollowList', label: '公开我的关注列表' },
  { key: 'publicFansList', label: '公开我的粉丝列表' },
];
const sections = [
  { class: 'magnum-video', label: '代表作' },
  { class: 'tribute-video', label: '视频' },
  { class: 'throw-coin-video', label: '最近投币的视频' },
  { class: 'collect', label: '收藏夹' },
  { class: 'follow-anime', label: '订阅剧集' },
  { class: 'love-video', label: '最近点赞的视频' },
];
const saved = JSON.parse(localStorage.getItem('homeSort'));
const validOrder = Array.isArray(saved) && saved.length === sections.length &&
  new Set(saved.map(item => item.class)).size === sections.length &&
  saved.every(item => sections.some(section => section.class === item.class));
const divs = validOrder ? saved.map(item => sections.find(section => section.class === item.class)) : sections;

watch(() => store.homeUserInformation.id, (id) => {
  if (id !== store.userId) return;
  loading.value = false;
  store.setHomeLoad(true, 'homeBody');
  store.setHomeLoadMenuList(7);
}, { immediate: true });

async function savePrivacy(key, value) {
  saving.value = true;
  const updated = { ...store.homeUserInformation, [key]: value ? 1 : 0 };
  try {
    await putHomeSettings(store.token, updated);
    store.setHomeUserInformation(updated);
    store.setUserInformation({ ...store.userInformation, ...updated });
  } catch (error) {
    ElMessage.error(error.message);
  } finally {
    saving.value = false;
  }
}

onMounted(() => {
  sortable = Sortable.create(sortElement.value, {
    animation: 150,
    onEnd() {
      const order = Array.from(sortElement.value.children, element => ({ class: element.dataset.section }));
      localStorage.setItem('homeSort', JSON.stringify(order));
    },
  });
});
onBeforeUnmount(() => sortable?.destroy());
</script>

<style scoped>
.settings { padding: 30px 60px; }
h2 { font-size: 24px; margin: 0 0 24px; }
.setting, .sort-item { display: flex; justify-content: space-between; width: 320px; padding: 12px 0; }
p, .sort-item span { color: #8D9794; font-size: 13px; }
.sort-item { cursor: grab; border-bottom: 1px solid #EFF3F2; }
@media (max-width: 720px) {
  .settings { padding: 20px 16px; }
  .setting, .sort-item { width: 100%; max-width: 320px; }
}
</style>
