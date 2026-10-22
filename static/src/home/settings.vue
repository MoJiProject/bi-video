<template>
  <div class="settings">
    <div class="settings-content">
    <h2>隐私设置</h2>
    <loadingIndicator v-if="loading" min-height="160px"/>
    <div v-else>
      <div v-for="setting in privacySettings" :key="setting.key" class="setting">
        <span>{{ setting.label }}</span>
        <el-switch :model-value="store.homeUserInformation[setting.key] === 1"
          :disabled="saving" @change="savePrivacy(setting.key, $event)"/>
      </div>
    </div>
    <h2>排序设置</h2>
    <div ref="sortElement" :aria-busy="sorting">
      <div v-for="item in divs" :key="item.class" :data-section="item.class" class="sort-item">
        <span class="sort-label">{{ item.label }}</span>
        <span class="sort-handle">
          <svg viewBox="0 0 16 16" fill="currentColor" aria-hidden="true">
            <circle cx="5" cy="4" r="1"/><circle cx="11" cy="4" r="1"/>
            <circle cx="5" cy="8" r="1"/><circle cx="11" cy="8" r="1"/>
            <circle cx="5" cy="12" r="1"/><circle cx="11" cy="12" r="1"/>
          </svg>
          拖动排序
        </span>
      </div>
    </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, onBeforeUnmount, ref, watch } from 'vue';
import { ElMessage } from 'element-plus';
import Sortable from 'sortablejs';
import { useGlobalStore } from '@/store/store';
import { putHomeSettings, putHomeSectionOrder } from '@/api/home/user';
import loadingIndicator from '@/components/loadingIndicator.vue';

const store = useGlobalStore();
const loading = ref(true);
const saving = ref(false);
const sorting = ref(false);
const sortElement = ref(null);
let sortable;
const privacySettings = [
  { key: 'publicCollect', label: '公开我的收藏' },
  { key: 'publicCoin', label: '公开最近投币的视频' },
  { key: 'publicLove', label: '公开最近点赞的视频' },
  { key: 'publicFollowList', label: '公开我的关注列表' },
  { key: 'publicFansList', label: '公开我的粉丝列表' },
  { key: 'publicBirthday', label: '公开我的生日' },
];
const sections = [
  { class: 'magnum-video', label: '代表作' },
  { class: 'tribute-video', label: '视频' },
  { class: 'throw-coin-video', label: '最近投币的视频' },
  { class: 'collect', label: '收藏夹' },
  { class: 'follow-anime', label: '订阅剧集' },
  { class: 'love-video', label: '最近点赞的视频' },
];
const divs = ref([...sections]);

watch(() => store.homeUserInformation.id, (id) => {
  if (id !== store.userId) return;
  const saved = store.homeUserInformation.homeSectionOrder;
  if (Array.isArray(saved) && saved.length === sections.length &&
      new Set(saved).size === sections.length && saved.every(key => sections.some(section => section.class === key))) {
    divs.value = saved.map(key => sections.find(section => section.class === key));
  }
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
    dataIdAttr: 'data-section',
    async onEnd() {
      if (sorting.value) return;
      sorting.value = true;
      sortable.option('disabled', true);
      const previous = divs.value.map(item => item.class);
      const order = Array.from(sortElement.value.children, element => element.dataset.section);
      try {
        await putHomeSectionOrder(order);
        sortable.sort(previous);
        divs.value = order.map(key => sections.find(section => section.class === key));
        store.setHomeUserInformation({ ...store.homeUserInformation, homeSectionOrder: order });
        store.setUserInformation({ ...store.userInformation, homeSectionOrder: order });
        ElMessage.success('首页排序已保存');
      } catch (error) {
        sortable.sort(previous);
        ElMessage.error(error.message);
      } finally {
        sorting.value = false;
        sortable.option('disabled', false);
      }
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
