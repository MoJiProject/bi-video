<template>
    <div class="video-list">
       <div class="video-item" v-for="video in data" :key="video.collects.id">
           <video6 :video="video.selectVideoDto" :collect="video.collects" :batchControls="batchControls" :searchContent="searchContent"/>
       </div>
       <div class="no-data" v-if="data.length===0">
           <img src="../img/home_nodata.svg">
           <div>这里还什么都没有呢～</div>
       </div>
 </div>
 
</template>

<script setup>
import video6 from './video6.vue';
defineProps({
   data:{
       type: Array,
       required:true,
   },
   batchControls:{
       type: Boolean,
       required: true,
   },
   searchContent:{
       type: String,
       required: true,
   },
})



</script>

<style lang="scss" scoped>

*{
   margin: 0;
   padding: 0;
   box-sizing: border-box;
}

.video-list{
position: relative;
column-gap: 16px;
row-gap: 19px;
display: grid;
grid-template-columns: repeat(5,1fr);
   .video-item{
       height: 183px;
   }
   .no-data{
       user-select: none;
       width: 1090px;
       height: 37.5vh;
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