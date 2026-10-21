<template>
  <div class="library-page">
    <header class="page-head">
      <mainHead class="header-stack-search" :head2-flag="true"/>
    </header>
    <waitWatchBody/>
  </div>
</template>
<script setup>
import mainHead from '@/components/mainHead.vue';
import waitWatchBody from './waitWatchBody.vue';
</script>
<style scoped>
.library-page { width: 100%; min-width: 0; min-height: 100dvh; background: var(--fill-soft); }
</style>
