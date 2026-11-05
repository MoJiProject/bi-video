<template>
  <UserHoverCard
    v-if="state.visible"
    :user-id="state.userId"
    :nickname="state.nickname"
    :controlled="true"
    :style="{ left: state.x + 'px', top: state.y + 'px' }"
    @card-mouseenter="state.cardHovered = true"
    @card-mouseleave="onCardLeave"
  />
</template>

<script setup>
import { onMounted, onUnmounted } from 'vue';
import { mentionHoverState as state, showMentionHover, hideMentionHover } from '@/utils/mentionHoverState';
import UserHoverCard from '@/components/UserHoverCard.vue';

function isExcluded(link) {
  return !!link.closest('.post-editor, .editor-input');
}

function findLink(target) {
  return target && typeof target.closest === 'function'
    ? target.closest('a[data-eit-userid]')
    : null;
}

function handleLink(link) {
  if (!link || isExcluded(link)) return;
  const userId = Number(link.getAttribute('data-eit-userid'));
  if (!Number.isSafeInteger(userId) || userId <= 0) return;
  const rect = link.getBoundingClientRect();
  const cardWidth = 300;
  const cardHeight = 240;
  let x = rect.right + 8;
  if (x + cardWidth > window.innerWidth - 8) x = rect.left - cardWidth - 8;
  if (x < 8) x = 8;
  let y = rect.top - 8;
  if (y + cardHeight > window.innerHeight - 8) y = window.innerHeight - cardHeight - 8;
  if (y < 8) y = 8;
  showMentionHover({
    userId,
    nickname: (link.textContent || '').trim().replace(/^@/, ''),
    x,
    y,
  });
}

function onOver(event) {
  const link = findLink(event.target);
  if (link) handleLink(link);
}

function onOut(event) {
  const link = findLink(event.target);
  if (link) hideMentionHover(220);
}

function onCardLeave() {
  state.cardHovered = false;
  hideMentionHover(120);
}

onMounted(() => {
  document.addEventListener('mouseover', onOver);
  document.addEventListener('mouseout', onOut);
});
onUnmounted(() => {
  document.removeEventListener('mouseover', onOver);
  document.removeEventListener('mouseout', onOut);
});
</script>

<style scoped>
:deep(.user-hover-fixed) { z-index: 99999; }
</style>
