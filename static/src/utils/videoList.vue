<template>
  <div class="video-list">
        <div class="video-item" v-for="video in data" :key="video.videoId"> 
            <videoC :video="video"/>
        </div>
        <div class="no-data" v-if="data.length===0">
            <img src="../img/home_nodata.svg">
            <div>暂无数据</div>
        </div>
  </div>
</template>

<script setup>
import videoC from '@/utils/video';
defineProps({
    data:{
        type: Array,
        required:true,
    }
})

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
        height: 173.5px;
    }
    .no-data{
        user-select: none;
        width: 1090px;
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