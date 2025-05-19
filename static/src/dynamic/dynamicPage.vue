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
import {ChecklLogin,getUserIp,getEitList} from '../api/user/index';
import { useGlobalStore } from '@/store/store';
import { onMounted } from 'vue';

const store = useGlobalStore();


onMounted(async()=>{

    await getUserIpF();
    await ChecklLoginF();
    getEitListF();
})

//获取用户ip和token
async function getUserIpF(){
    
    await getUserIp().then(response => {
        if(response.data.code === 1){ 
        store.setUserIp(response.data.data.userIp);
        store.setToken(response.data.data.token);
        }
    });
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