<template>
    <div class="video-list">
       <div class="video-item" v-for="image in data" :key="image.history.id">
           <imageC :video="image.selectVideoDto" :history="image.history" :batchControls="batchControls" :searchContent="searchContent"/>
       </div>
 </div>
 
</template>

<script setup>
import imageC from './imageC.vue';
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
width: 258px;
column-gap: 16px;
row-gap: 49.5px;
display: grid;
grid-template-columns: repeat(4,1fr);
   .video-item{
       height: 220px;
   }   
}

</style>