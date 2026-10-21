<template><div class="rich-content" v-html="safeContent"/></template>
<script setup>
import { computed } from 'vue';
import { sanitizeRichText } from '@/utils/richText';
const props = defineProps({ content: { type: String, default: '' } });
const safeContent = computed(() => sanitizeRichText(props.content));
</script>
<style scoped>
.rich-content { white-space: pre-wrap; overflow-wrap: anywhere; line-height: 1.7; }
.rich-content :deep(p), .rich-content :deep(div) { margin: 0; }
.rich-content :deep(a) { color: #0a8271; }
.rich-content :deep(.inline-emoji) { display: inline-block; width: 28px; height: 28px; vertical-align: middle; }
</style>
