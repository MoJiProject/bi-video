<template>
    <div class="dynamic-detail-page-container">
        <div class="dynamic-detail-page-head-container">
            <mainHead :head2-flag="true" :loginDialogVisibleFlag="loginDialogVisibleFlag"/>
        </div>
        <div class="dynamic-detail-page-body-container">
            <dynamicDetail/>
        </div>  
        <div style="position: fixed;top: 800px;z-index: 10;">
            <el-backtop :right="5"/>
        </div>
    </div>
</template>

<script setup>
import mainHead from '@/components/mainHead.vue';
import dynamicDetail from './dynamicDetail.vue';
import { useGlobalStore } from "../store/store";
import { onMounted, ref, watch } from 'vue';

const store = useGlobalStore();
const loginDialogVisibleFlag=ref(0);
const dynamicId=new URLSearchParams(window.location.search).get("dynamicId");


onMounted(()=>{
    if(!dynamicId){
        window.location.href = './';
    }
})

watch(()=>store.loginDialogVisibleFlag,()=>{
  loginDialogVisibleFlag.value=loginDialogVisibleFlag.value===0?1:0;
})





</script>

<style lang="scss" scoped>


*{
    margin: 0;
    padding: 0;
    box-sizing: border-box;
}

.dynamic-detail-page-container{
    
    .dynamic-detail-page-head-container{
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