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
  flex: 1 1 720px;
  max-width: 870px;
  width: min(100%, 870px);
  border: 1px solid rgba(224,229,227,0.72);
  min-height: 500px;
  background-color: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(14px);
  border-radius: 18px;
  box-shadow: 0 18px 45px rgba(16, 41, 37, 0.08);
  box-sizing: border-box;
  overflow: hidden;
}

@media (max-width: 900px){
  .account-body{
    flex-basis: 100%;
    width: 100%;
    max-width: none;
    min-height: calc(100dvh - 160px);
  }
}

</style>