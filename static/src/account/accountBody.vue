<template>
  <div v-if="store.userInformation.id" class="account-body">
      <template v-if="store.accountMenu === 1"><userInfo/></template>
      <template v-else-if="store.accountMenu === 2"><avatar/></template>
      <template v-else-if="store.accountMenu === 3"><background/></template>
  </div>
</template>

<script setup>
import userInfo from './userInfo.vue';
import avatar from './avatar.vue';
import background from './background.vue';
import { useGlobalStore } from '../store/store';

const store = useGlobalStore();


</script>

<style lang="scss" scoped>

*{
    margin: 0;
    padding: 0;
    box-sizing: border-box;
}

.account-body{
  position: relative;
  width: 832px;
  border: 1px solid #e1e2e5;
  height: 500px;
  background-color: rgba(255, 255, 255, 0.7);
}

</style>