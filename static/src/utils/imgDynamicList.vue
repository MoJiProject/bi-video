<template>
  <div class="img-dynamic-list">
    <img-dynamic v-for="(item, index) in data" :key="index" :data="item" />
  </div>
</template>

<script setup>
import imgDynamic from './imgDynamic.vue';
defineProps({
  data: {
    type: Array,
    required: true,
  },
});

</script>

<style lang="scss" scoped>

*{
    margin: 0;
    padding: 0;
    box-sizing: border-box;
}


</style>