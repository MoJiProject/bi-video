<template>
  <div v-if="store.token" class="waitWatch-page">
      <div class="waitWatch-head-container">
          <mainHead :head2-flag="true"/>
      </div>
      <div class="waitWatch-body-container">
          <waitWatchBody/>
      </div>  
      <el-backtop :right="5"/>
  </div>
</template>

<script setup>
import mainHead from '@/components/mainHead.vue';
import waitWatchBody from './waitWatchBody.vue';
import { useGlobalStore } from '@/store/store';
import { onMounted } from 'vue';

const store = useGlobalStore();

onMounted(async()=>{

    await getUserIp();
})

//获取用户ip和token
// token 由 /auth/login 返回后存在本地，这里不再向后端要 IP + token。
async function getUserIp(){
  store.setUserIp("0.0.0.0");
}

</script>

<style lang="scss" scoped>

*{
    margin: 0;
    padding: 0;
    box-sizing: border-box;
}

.waitWatch-page{
    .waitWatch-head-container{
    position: sticky;
    top: 0;
    left: 0;
    width: 100%;
    background-color: white;
    height: 64px;
    z-index: 1000;
    box-shadow: 0 2px 4px #00000014;
    }


}

</style>