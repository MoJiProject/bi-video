<template>
    <div class="image-list">
       <div class="image-item" v-for="image in data" :key="image.history.id">
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

.image-list{
position: relative;
width: 258px;
column-gap: 16px;
row-gap: 49.5px;
display: grid;
grid-template-columns: repeat(4,258px);
   .image-item{
       height: 220px;
   }   
}

@media (max-width: 1400px) {
  .image-list {
    grid-template-columns: repeat(3, 258px);
  }
}

@media (max-width: 1085px) {
  .image-list {
    grid-template-columns: repeat(2, 258px);
  }
}

@media (max-width: 820px) {
  .image-list {
    grid-template-columns: repeat(1, 258px);
  }
}

</style>