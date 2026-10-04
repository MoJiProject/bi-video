<template>
    <div class="video-list7">
       <div class="video-item" v-for="video in data" :key="video.collects.id">
           <video6 :video="video.selectVideoDto" :collect="video.collects" :batchControls="batchControls" :searchContent="searchContent"/>
       </div>
       <div class="no-data" v-if="data.length===0">
           <img src="/img/home_nodata.svg">
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

.video-list7{
position: relative;
column-gap: 16px;
row-gap: 19px;
display: grid;
grid-template-columns: repeat(5,206px);
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
           color: #8D9794;
           
       }
   }    
}
@media (max-width: 1420px) {
  .video-list7 {
    grid-template-columns: repeat(4, 206px);
  }
}

@media (max-width: 1200px) {
  .video-list7 {
    grid-template-columns: repeat(3, 206px);
  }
}

@media (max-width: 985px) {
  .video-list7 {
    grid-template-columns: repeat(2, 206px);
  }
}
@media (max-width: 760px) {
  .video-list7 {
    grid-template-columns: repeat(1, 206px);
  }
}
</style>