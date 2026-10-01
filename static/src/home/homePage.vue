<template>
  <div v-if="userId" class="home-page">
    <loadingBar :active="barActive" :percent="barPercent" :min-duration="initialLoadFinished?260:400"/>
    <home-head/>
    <homeAside/>
    <homeBody/>
    <div style="position: fixed;top: 800px;z-index: 10;">
      <el-backtop :right="5"/>
    </div>
  </div>
    
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue';
import {useGlobalStore} from "../store/store";
import homeHead from './homeHead.vue';
import homeAside from './aside.vue';
import homeBody from './homeBody.vue';
import loadingBar from '@/components/loadingBar.vue';

const store = useGlobalStore();
const initialLoadFinished = ref(false);
const menuLoading = ref(false);
const userId = parseInt(new URL(window.location).searchParams.get("userId")) || null;

// 头部与侧栏先完成，正文占剩余权重，整体不会过早显示为 100%
const loadPercent = computed(() => {
  const load = store.homeLoad;
  if (load.homeHead && load.homeAside && load.homeBody)
    return 100;
  let percent = 0;
  percent += load.homeHead ? 25 : 0;
  percent += load.homeAside ? 20 : 0;
  percent += load.homeBody ? 55 : 0;
  return percent;
});

// 首屏显示整体进度，切换子页面只跟踪正文加载
const barActive = computed(() => !initialLoadFinished.value || menuLoading.value);
const barPercent = computed(() => {
  if (!initialLoadFinished.value)
    return loadPercent.value;
  return store.homeLoad.homeBody ? 100 : 70;
});

const handlePopState = () => {
  const urlParams = new URLSearchParams(window.location.search);
  store.setHomeMenu(parseInt(urlParams.get("homeMenu")) || 1,false);
};

onMounted(() => {

  if(!userId){
    window.location.href = './';
    return;
  }

  document.body.style.display = 'flex';
  document.body.style.justifyContent = 'center';
  
  const urlParams = new URLSearchParams(window.location.search);
  store.setHomeMenu(parseInt(urlParams.get("homeMenu")) || 1,true);
  window.addEventListener('popstate', handlePopState);
  
});

onBeforeUnmount(()=>{
  window.removeEventListener('popstate', handlePopState);
});

watch(()=>[store.homeLoad.homeHead,store.homeLoad.homeAside,store.homeLoad.homeBody],()=>{
    if(!initialLoadFinished.value&&store.homeLoad.homeHead&&store.homeLoad.homeAside&&store.homeLoad.homeBody)
        initialLoadFinished.value=true;
},{immediate:true});

// 切换子页面时重新显示进度条，正文加载完成后再结束
watch(()=>store.homeMenu,()=>{
    if(!initialLoadFinished.value)
        return;
    menuLoading.value=!store.homeLoad.homeBody;
});

watch(()=>store.homeLoad.homeBody,(value)=>{
    if(value)
        menuLoading.value=false;
});


</script>

<style lang="scss" scoped>
*{	
    margin: 0;
    padding: 0;
    box-sizing: border-box;
}

.home-page{
    position: relative;
    width: 100%;
}

</style>
