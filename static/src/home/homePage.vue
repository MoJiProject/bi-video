<template>
  <div v-if="userId" class="home-page">
    <div v-if="!animationDelayFlag" class="home-loading" :class="{animationDelayFlag:animationDelayFlag}"></div>
    <div v-else class="home-loading" :class="{animationDelayFlag:animationDelayFlag}"></div>
    <home-head/>
    <homeAside/>
    <homeBody/>
    <div style="position: fixed;top: 800px;z-index: 10;">
      <el-backtop :right="5"/>
    </div>
  </div>
    
</template>

<script setup>
import { onMounted, ref, watch } from 'vue';
import {useGlobalStore} from "../store/store";
import homeHead from './homeHead.vue';
import homeAside from './aside.vue';
import homeBody from './homeBody.vue';

const store = useGlobalStore();
const animationDelayFlag = ref(true);
const userId = parseInt(new URL(window.location).searchParams.get("userId")) || null;


onMounted(() => {

  if(!userId){
    window.location.href = './';
    return;
  }

  document.body.style.display = 'flex';
  document.body.style.justifyContent = 'center';
  
  const urlParams = new URLSearchParams(window.location.search);
  store.setHomeMenu(parseInt(urlParams.get("homeMenu")) || 1,true);
  window.addEventListener('popstate', () => {
  const urlParams = new URLSearchParams(window.location.search);
  store.setHomeMenu(parseInt(urlParams.get("homeMenu")) || 1,false);
  });
  
  
});

watch(()=>store.homeLoad,()=>{
    animationDelayFlag.value=!store.homeLoad.homeHead||!store.homeLoad.homeAside||!store.homeLoad.homeBody;
},{deep:true});


</script>

<style lang="scss" scoped>
*{
    margin: 0;
    padding: 0;
    box-sizing: border-box;
}

.home-page{
    position: relative;
    left: 13px;
    width: 100%;

    .home-loading{
        position: absolute;
        z-index: 10000;
        width: 0px;
        height: 3px;
        background-color: #00AEEC;
        animation: load2 0.3s ease-in-out backwards;
    }
    .animationDelayFlag{
      animation: load 60s ease backwards;
    }
}

@keyframes load{
  0%{
    width: 0px;
  }
  100%{
    width: 100%;
  }
}

@keyframes load2{
  0%{
    width: 67%;
  }
  100%{
    width: 100%;
  }
}

</style>