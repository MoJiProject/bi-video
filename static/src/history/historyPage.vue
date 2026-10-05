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
import mainHead from '@/components/mainHead.vue';
import historyBody from './historyBody.vue';
import { ChecklLogin } from '../api/user/index';
import { useGlobalStore } from '@/store/store';
import { onMounted } from 'vue';

const store = useGlobalStore();

onMounted(async()=>{

    await getUserIp();
    ChecklLoginF();
})

//获取用户ip和token
// token 由 /auth/login 返回后存在本地，这里不再向后端要 IP + token。
async function getUserIp(){
  store.setUserIp("");
}
//检查是否登录
async function ChecklLoginF(){

    await ChecklLogin(store.userIp).then(response=>{
    if (response.data.code === 1) {
        store.setUserId(response.data.data.id);
    } else {
        window.location.href = "./";
    }
    })
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