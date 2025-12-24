<template>
  <div class="home-body">
    <keep-alive>
      <component class="home-component" :is="currentComponent" />
    </keep-alive>
  </div>
</template>

<script setup>
import { computed } from 'vue';
import home from './home.vue'
import dynamic from './dynamic.vue';
import contribute from './contribute.vue';
import lists from './lists.vue';
import collect from './collect.vue';
import followAnime from './followAnime.vue';
import settings from './settings.vue';
import follow from './follow.vue';
import fans from './fans.vue';
import search from './search.vue';
import { useGlobalStore } from '../store/store';

const store = useGlobalStore();
const userId = parseInt(new URL(window.location).searchParams.get("userId"))|| null;
const currentComponent = computed(() => {
  switch (store.homeMenu) {
    case 1: return home;
    case 2: return dynamic;
    case 3: return contribute;
    case 4: return lists;
    case 5:
      return (store.homeUserInformation.publicCollect === 1 ||
              (store.userId !== null && store.userId === userId)) ? collect : home;
    case 6:
      return (store.homeUserInformation.publicAnime === 1 ||
              (store.userId !== null && store.userId === userId)) ? followAnime : home;
    case 7:
      return (store.userId !== null && store.userId === userId) ? settings : home;
    case 8:
      return (store.homeUserInformation.publicFollowList === 1 ||
              (store.userId !== null && store.userId === userId)) ? follow : home;
    case 9:
      return (store.homeUserInformation.publicFansList === 1 ||
              (store.userId !== null && store.userId === userId)) ? fans : home;
    case 10: return search;
    default: return home;
  }
});
</script>

<style scoped lang="scss">
.home-body{
   position: relative;
   z-index: 1;
}

.home-component{
  width: 100vw;
}
</style>