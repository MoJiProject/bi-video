<template>
    <div class="video-list">
       <div class="video-item" v-for="video in data" :key="video.collects.id">
           <video8 :video="video.selectVideoDto" :collect="video.collects" :batchControls="batchControls" :searchContent="searchContent"/>
       </div>
 </div>
 
</template>

<script setup>
import video8 from './video8.vue';
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
display: grid;
grid-template-columns: repeat(4,1fr);
grid-gap: 48.5px 16px;
   .video-item{
       height: 223.13px;
   }   
}

</style>