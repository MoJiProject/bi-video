<template>
  <svg v-if="gender === 1" class="gender-icon-svg male" :width="size" :height="size" viewBox="0 0 24 24" aria-label="男">
    <circle cx="10" cy="14" r="5" fill="none" stroke="currentColor" stroke-width="3" />
    <path d="M14 10l5-5M19 5h-4M19 5v4" fill="none" stroke="currentColor" stroke-width="3" stroke-linecap="round" stroke-linejoin="round" />
  </svg>
  <svg v-else-if="gender === 2" class="gender-icon-svg female" :width="size" :height="size" viewBox="0 0 24 24" aria-label="女">
    <circle cx="12" cy="9" r="5" fill="none" stroke="currentColor" stroke-width="3" />
    <path d="M12 14v7M9 18h6" fill="none" stroke="currentColor" stroke-width="3" stroke-linecap="round" />
  </svg>
</template>

<script setup>
defineProps({
  gender: { type: Number, default: 0 },
  size: { type: [Number, String], default: 16 },
});
</script>

<style scoped>
.gender-icon-svg { display: inline-block; vertical-align: middle; }
.gender-icon-svg.male { color: #1890ff; }
.gender-icon-svg.female { color: #ff4d4f; }
</style>
