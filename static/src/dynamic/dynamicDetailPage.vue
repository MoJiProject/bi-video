<template>
    <div class="dynamic-detail-page-container">
        <div class="dynamic-detail-page-head-container">
            <mainHead class="header-stack-search" :head2-flag="true" :loginDialogVisibleFlag="loginDialogVisibleFlag"/>
        </div>
        <div class="dynamic-detail-page-body-container">
            <dynamicDetail/>
        </div>  
        <el-backtop :right="5"/>
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
    width: 100%;
    min-width: 0;
    min-height: 100dvh;
    background: linear-gradient(180deg, #edf5ef 0, #f5f7f5 300px);
    
    .dynamic-detail-page-head-container{
    position: sticky;
    top: 0;
    left: 0;
    width: 100%;
    background-color: white;
    height: var(--head-h);
    z-index: var(--z-head);
    }

}

</style>