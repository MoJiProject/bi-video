<template>
  <section class="app-empty-state" :class="{ 'app-empty-state--compact': compact }" role="status">
    <div class="app-empty-state__icon" aria-hidden="true">
      <svg viewBox="0 0 48 48" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <template v-if="kind === 'login'">
          <rect x="9" y="20" width="30" height="22" rx="6"/>
          <path d="M16 20v-7a8 8 0 0 1 16 0v7"/>
          <circle cx="24" cy="30" r="2"/>
          <path d="M24 32v4"/>
        </template>
        <template v-else-if="kind === 'search'">
          <circle cx="21" cy="21" r="13"/>
          <path d="m31 31 10 10M16 21h10"/>
        </template>
        <template v-else-if="kind === 'unavailable'">
          <circle cx="24" cy="24" r="17"/>
          <path d="M24 15v11M24 33h.01"/>
        </template>
        <template v-else>
          <path d="m8 25 5-14h22l5 14v12a4 4 0 0 1-4 4H12a4 4 0 0 1-4-4Z"/>
          <path d="M8 25h10l3 5h6l3-5h10M19 18h10"/>
        </template>
      </svg>
    </div>
    <h2 class="app-empty-state__title">{{ title }}</h2>
    <p v-if="description" class="app-empty-state__description">{{ description }}</p>
    <div v-if="actionText || $slots.actions" class="app-empty-state__actions">
      <slot name="actions">
        <button class="app-empty-state__button" type="button" @click="$emit('action')">{{ actionText }}</button>
      </slot>
    </div>
  </section>
</template>

<script setup>
defineProps({
  title: { type: String, default: '暂无内容' },
  description: { type: String, default: '' },
  kind: { type: String, default: 'empty', validator: value => ['empty', 'login', 'search', 'unavailable'].includes(value) },
  actionText: { type: String, default: '' },
  compact: { type: Boolean, default: false },
});
defineEmits(['action']);
</script>

<style scoped>
.app-empty-state {
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  grid-column: 1 / -1;
  width: 100%;
  min-width: 0;
  min-height: 280px;
  padding: 40px 24px;
  border: 1px solid var(--line);
  border-radius: var(--radius-lg);
  background: #fff;
  color: var(--ink);
  text-align: center;
  line-height: 1.6;
}
.app-empty-state__icon {
  display: grid;
  place-items: center;
  flex: none;
  width: 76px;
  height: 76px;
  margin-bottom: 20px;
  border-radius: 24px;
  background: var(--brand-light-9);
  color: var(--brand-hover);
}
.app-empty-state__icon svg { display: block; width: 44px; height: 44px; }
.app-empty-state__title {
  max-width: 100%;
  margin: 0;
  color: var(--ink);
  font-size: 18px;
  font-weight: 600;
  line-height: 1.5;
  overflow-wrap: anywhere;
}
.app-empty-state__description {
  max-width: 420px;
  margin: 8px 0 0;
  color: var(--ink-2);
  font-size: 14px;
  line-height: 1.8;
  overflow-wrap: anywhere;
}
.app-empty-state__actions {
  display: flex;
  justify-content: center;
  flex-wrap: wrap;
  gap: 12px;
  max-width: 100%;
  margin-top: 24px;
}
.app-empty-state__button,
.app-empty-state__actions :deep(.el-button) {
  box-sizing: border-box;
  min-width: 112px;
  min-height: 40px;
  height: auto;
  margin: 0;
  padding: 9px 24px;
  border: 1px solid var(--brand);
  border-radius: var(--radius-md);
  background: var(--brand);
  color: #fff;
  font: inherit;
  font-size: 14px;
  font-weight: 600;
  line-height: 1.5;
  white-space: normal;
  cursor: pointer;
  transition: background-color .15s, border-color .15s;
}
.app-empty-state__button:hover,
.app-empty-state__actions :deep(.el-button:hover) {
  border-color: var(--brand-hover);
  background: var(--brand-hover);
}
.app-empty-state__button:focus-visible,
.app-empty-state__actions :deep(.el-button:focus-visible) {
  outline: 2px solid var(--brand-hover);
  outline-offset: 3px;
}
.app-empty-state__actions :deep(.el-button:not(.el-button--primary)) {
  border-color: var(--line);
  background: #fff;
  color: var(--ink-2);
}
.app-empty-state__actions :deep(.el-button:not(.el-button--primary):hover) {
  border-color: var(--brand);
  background: var(--brand-light-9);
  color: var(--brand-hover);
}
.app-empty-state--compact { min-height: 180px; padding: 24px 16px; }
.app-empty-state--compact .app-empty-state__icon { width: 56px; height: 56px; margin-bottom: 12px; border-radius: 18px; }
.app-empty-state--compact .app-empty-state__icon svg { width: 32px; height: 32px; }
.app-empty-state--compact .app-empty-state__title { font-size: 16px; }
@media (max-width: 600px) {
  .app-empty-state { min-height: 240px; padding: 32px 16px; }
  .app-empty-state__title { font-size: 17px; }
  .app-empty-state__description { font-size: 13px; }
  .app-empty-state--compact { min-height: 160px; padding: 20px 12px; }
  .app-empty-state--compact .app-empty-state__title { font-size: 15px; }
}
</style>
