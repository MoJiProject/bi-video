<template>
<div v-if="store.token" class="dynamic-page">
    <div class="dynamic-head-container">
        <mainHead :head2-flag="true"/>
    </div>
    <div class="dynamic-body-container">
        <dynamicBody/>
    </div>  
    <div style="position: fixed;top: 800px;z-index: 10;">
        <el-backtop :right="5"/>
    </div>
</div>
</template>

<script setup>
import mainHead from '@/components/mainHead.vue';
import dynamicBody from './dynamicBody.vue';
import { ChecklLogin, getEitList } from '../api/user/index';
import { useGlobalStore } from '@/store/store';
import { onMounted } from 'vue';

const store = useGlobalStore();


onMounted(async()=>{

    await getUserIp();
    await ChecklLoginF();
    getEitListF();
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

//获取eit列表
async function getEitListF(){
    await getEitList(store).then(response => {
        if(response.data.code === 1)
        Object.assign(store.eitList, response.data.data);
    });
}

</script>

<style lang="scss" scoped>

*{
    margin: 0;
    padding: 0;
    box-sizing: border-box;
}

.dynamic-page{
    .dynamic-head-container{
    position: fixed;
    top: 0;
    width: 103%;
    left: -21px;
    background-color: white;
    height: 64px;
    z-index: 1000;
    box-shadow: 0 2px 4px #00000014;
    }

}

</style>