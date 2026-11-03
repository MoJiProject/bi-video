<template>
  <svg class="follow-icon-svg" :width="size" :height="size" viewBox="0 0 24 24" aria-hidden="true">
    <template v-if="!followed">
      <path d="M12 5v14M5 12h14" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" />
    </template>
    <template v-else>
      <path d="M5 12.5l4.5 4.5L19 7.5" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round" />
    </template>
  </svg>
</template>

<script setup>
defineProps({
  followed: { type: Boolean, default: false },
  size: { type: [Number, String], default: 16 },
});
</script>

<style scoped>
.follow-icon-svg { display: inline-block; vertical-align: middle; color: currentColor; margin-right: 4px; }
</style>
