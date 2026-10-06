<template>
<div v-if="store.token" class="dynamic-page">
    <div class="dynamic-head-container">
        <mainHead :head2-flag="true"/>
    </div>
    <div class="dynamic-body-container">
        <dynamicBody/>
    </div>  
    <el-backtop :right="5"/>
</div>
</template>

<script setup>
import mainHead from '@/components/mainHead.vue';
import dynamicBody from './dynamicBody.vue';
import { getEitList } from '../api/user/index';
import { useGlobalStore } from '@/store/store';
import { onMounted } from 'vue';

const store = useGlobalStore();


onMounted(async()=>{

    await getUserIp();
    getEitListF();
})

//获取用户ip和token
// token 由 /auth/login 返回后存在本地，这里不再向后端要 IP + token。
async function getUserIp(){
  store.setUserIp("0.0.0.0");
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