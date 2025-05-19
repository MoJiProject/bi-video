<template>
   <div class="home-body">
      <template v-if="store.homeMenu === 1"><home/></template>
      <template v-else-if="store.homeMenu === 2"><dynamic/></template>
      <template v-else-if="store.homeMenu === 3"><contribute/></template>
      <template v-else-if="store.homeMenu === 4"><lists/></template>
      <template v-else-if="store.homeMenu === 5&&(store.homeUserInformation.publicCollect===1||(store.userId!==null&&store.userId===userId))"><collect/></template>
      <template v-else-if="store.homeMenu === 6&&(store.homeUserInformation.publicAnime===1||(store.userId!==null&&store.userId===userId))"><followAnime/></template>
      <template v-else-if="store.homeMenu === 7&&store.userId!==null&&store.userId===userId"><settings/></template>
      <template v-else-if="store.homeMenu === 8&&(store.homeUserInformation.publicFollowList===1||(store.userId!==null&&store.userId===userId))"><follow/></template>
      <template v-else-if="store.homeMenu === 9&&(store.homeUserInformation.publicFansList===1||(store.userId!==null&&store.userId===userId))"><fans/></template>
      <template v-else-if="store.homeMenu === 10"><search/></template>
   </div>
</template>

<script setup>
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

const store= useGlobalStore();
const userId = parseInt(new URL(window.location).searchParams.get("userId"))|| null;

</script>

<style scoped lang="scss">

.home-body{
   position: relative;
   z-index: 1;
}


</style>