<template>
  <div v-if="store.token" class="history-page">
      <div class="history-head-container">
          <mainHead :head2-flag="true"/>
      </div>
      <div class="history-body-container">
          <historyBody/>
      </div>  
      <el-backtop :right="5"/>
  </div>
</template>

<script setup>
import historyBody from './historyBody.vue';
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

//检查是否登录 - 由于 ChecklLogin 接口已移除，此函数保留为空以避免错误
async function ChecklLoginF(){
  // 由于后端移除了 /user/checkLoginFlag/{userIp} 接口，跳过登录状态检查
  // 实际登录状态由 mainHead.vue 中的 getToken() 和 store.token 决定
  console.debug("ChecklLoginF 已停用：后端接口已移除，跳过检查");
}
</script>

<style lang="scss" scoped>

*{
    margin: 0;
    padding: 0;
    box-sizing: border-box;
}

.history-page{
    .history-head-container{
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