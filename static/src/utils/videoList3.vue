<template>
  <div class="video-list">
        <div class="video-item" v-for="video in data" :key="video.videoId"> 
            <videoC3 :video="video" :size="size"/>
        </div>
        <div class="no-data" v-if="data.length===0">
            <img src="../img/home_nodata.svg">
            <div v-if="store.userId!==null&&store.userId==userId">你还没有发布视频内容</div>
            <div v-else>空间主人还没投过视频内容，这里什么也没有...</div>
        </div>
  </div>
</template>

<script setup>
import videoC3 from '@/utils/video3';
import { ref } from 'vue';
import { useGlobalStore } from "@/store/store";
defineProps({
    data:{
        type: Array,
        required:true,
    }
})
const store = useGlobalStore();
const size=ref({width:215,height:120.94});
const userId = parseInt(new URL(window.location).searchParams.get("userId")) || null;


</script>

<style lang="scss" scoped>

*{
    margin: 0;
    padding: 0;
    box-sizing: border-box;
}

.video-list{
column-gap: 16px;
row-gap: 23px;
display: grid;
grid-template-columns: repeat(5,1fr);
    .video-item{
        height: 185px;
    }
    .no-data{
        user-select: none;
        width: 1139px;
        height: 42vh;
        display: flex;
        justify-content: center;
        align-items: center;
        flex-direction: column;  
        img{
            width: 140px;
            height: 140px;
        }
        div{
            margin-top: 6px;
            font-size: 14px;
            line-height: 20px;
            font-weight: 400;
            color: #9499A0;
            
        }
    }    
}

</style>