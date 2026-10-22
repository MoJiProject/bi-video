<template>
  <el-dropdown ref="dropdown" class="content-more" :class="{ 'is-open': open, 'is-keyboard-focused': keyboardFocused }"
    trigger="hover" placement="bottom-end" size="small" popper-class="content-more-popper"
    :popper-options="popperOptions" :show-timeout="80" :hide-timeout="300"
    :disabled="disabled" @visible-change="onVisibleChange">
    <button ref="trigger" type="button" class="content-more-trigger" :aria-label="label"
      aria-haspopup="menu" :aria-expanded="open" :disabled="disabled" @click="dropdown?.handleOpen()"
      @pointerenter="onPointerInteraction" @pointerdown="onPointerInteraction" @pointerleave="clearPointerFocus"
      @focus="keyboardFocused = !pointerInteraction" @blur="keyboardFocused = false" @keydown="onKeyboardInteraction">
      <svg viewBox="0 0 24 24" fill="currentColor" aria-hidden="true">
        <circle cx="12" cy="5" r="1.7"/><circle cx="12" cy="12" r="1.7"/><circle cx="12" cy="19" r="1.7"/>
      </svg>
    </button>
    <template #dropdown>
      <el-dropdown-menu><slot/></el-dropdown-menu>
    </template>
  </el-dropdown>
</template>

<script setup>
import { ref } from 'vue';

defineProps({
  label: { type: String, default: '更多操作' },
  disabled: { type: Boolean, default: false },
});

const dropdown = ref(null);
const trigger = ref(null);
const open = ref(false);
const keyboardFocused = ref(false);
let pointerInteraction = false;
function onPointerInteraction() {
  pointerInteraction = true;
  keyboardFocused.value = false;
}
function onKeyboardInteraction() {
  pointerInteraction = false;
  keyboardFocused.value = true;
}
function clearPointerFocus() {
  if (!pointerInteraction) return;
  trigger.value?.blur();
  pointerInteraction = false;
}
function onVisibleChange(visible) {
  open.value = visible;
  if (!visible) clearPointerFocus();
}
const popperOptions = {
  modifiers: [{ name: 'offset', options: { offset: [0, 4] } }],
};
</script>
