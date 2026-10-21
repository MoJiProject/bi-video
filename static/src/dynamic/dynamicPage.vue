<template>
  <div class="dynamic-page">
    <header class="dynamic-page-head">
      <mainHead class="header-stack-search" :head2-flag="true"/>
    </header>
    <dynamicBody/>
  </div>
</template>
<script setup>
import mainHead from '@/components/mainHead.vue';
import dynamicBody from './dynamicBody.vue';
</script>
<style scoped>
.dynamic-page { width: 100%; min-width: 0; min-height: 100dvh; background: linear-gradient(180deg, #edf5ef 0, #f5f7f5 300px); }
.dynamic-page-head { position: sticky; top: 0; height: var(--head-h); background: #fff; z-index: var(--z-head); }
</style>
