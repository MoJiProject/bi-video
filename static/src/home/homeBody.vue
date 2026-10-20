<template>
  <div class="home-body">
    <div v-if="store.homeMenu === 6" class="unavailable">追剧功能暂未开放</div>
    <div v-else-if="!currentComponent && store.homeLoad.homeHead" class="unavailable">该页面不可访问</div>
    <keep-alive>
      <component class="home-component" :is="currentComponent" />
    </keep-alive>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, watch } from 'vue';
import home from './home.vue'
import dynamic from './dynamic.vue';
import contribute from './contribute.vue';
import lists from './lists.vue';
import collect from './collect.vue';
import settings from './settings.vue';
import follow from './follow.vue';
import fans from './fans.vue';
import search from './search.vue';
import { useGlobalStore } from '../store/store';

const store = useGlobalStore();
const userId = parseInt(new URL(window.location).searchParams.get("userId")) || store.userId;
const currentComponent = computed(() => {
  switch (store.homeMenu) {
    case 1: return home;
    case 2: return dynamic;
    case 3: return contribute;
    case 4: return lists;
    case 5:
      return (store.homeUserInformation.publicCollect === 1 ||
              (store.userId !== null && store.userId === userId)) ? collect : null;
    case 6: return null;
    case 7:
      return (store.userId !== null && store.userId === userId) ? settings : null;
    case 8:
      return (store.homeUserInformation.publicFollowList === 1 ||
              (store.userId !== null && store.userId === userId)) ? follow : null;
    case 9:
      return (store.homeUserInformation.publicFansList === 1 ||
              (store.userId !== null && store.userId === userId)) ? fans : null;
    case 10: return search;
    default: return null;
  }
});

// 请求失败或返回非 1 时子组件不会置位，超时兜底结束加载，避免进度条卡住
const BODY_LOAD_TIMEOUT = 10000;
let bodyTimer = null;

watch(() => store.homeMenu, (newVal) => {
  clearTimeout(bodyTimer);
  bodyTimer = setTimeout(() => {
    store.setHomeLoad(true,"homeBody");
  }, BODY_LOAD_TIMEOUT);

  if (store.homeLoadMenuList.includes(newVal)) {
    setTimeout(() => {
      store.setHomeLoad(true,"homeBody");
    }, 50);
  }
},{immediate:true});

// 无权限时菜单对应组件为空，需要直接结束加载，避免顶部进度条一直挂着
watch([currentComponent, () => store.homeLoad.homeHead], ([component, headLoaded]) => {
  if (!component && headLoaded) {
    clearTimeout(bodyTimer);
    store.setHomeLoad(true, "homeBody");
  }
}, { immediate: true });

onBeforeUnmount(()=>{
  clearTimeout(bodyTimer);
});


</script>

<style scoped lang="scss">
.home-body{
   position: relative;
   z-index: 1;
}

.home-component{
  width: 100%;
}
.unavailable { padding: 60px; text-align: center; color: #8D9794; }
</style>