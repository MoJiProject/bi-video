<template>
  <div class="post-editor" :class="{ 'is-focused': focused, 'is-disabled': disabled, compact }">
    <el-popover :visible="mentionOpen" placement="bottom-start" :width="300"
      :show-arrow="false" :popper-style="panelStyle">
      <template #reference>
        <div ref="editor" class="editor-input" :class="{ 'is-empty': !length }" :contenteditable="!disabled"
          role="textbox" aria-multiline="true" aria-autocomplete="list"
          :aria-label="`${placeholder}，输入 @ 提及用户`" :aria-disabled="disabled"
          :aria-controls="mentionOpen ? menuId : undefined"
          :aria-activedescendant="mentionOpen && candidates.length ? `${menuId}-${selectedIndex}` : undefined"
          :data-placeholder="placeholder" @input="updateContent" @beforeinput="onBeforeInput" @keydown="onKeydown"
          @keyup="onCaretMove" @mouseup="onCaretMove" @focus="focused = true"
          @blur="onBlur" @click="onEditorClick" @scroll="closeMentions" @paste.prevent="paste"
          @compositionstart="onCompositionStart" @compositionend="onCompositionEnd"/>
      </template>
      <div class="mention-panel">
        <div class="panel-heading"><strong>提及用户</strong><span>↑↓ 选择 · Enter 确认</span></div>
        <p v-if="mentionLoading" class="panel-status" role="status">正在加载用户…</p>
        <div v-else-if="mentionError" class="panel-status" role="alert">
          <p>{{ mentionError }}</p><el-button text type="primary" @mousedown.prevent @click="loadMentions">重试</el-button>
        </div>
        <div v-else ref="mentionList" :id="menuId" class="mention-list" role="listbox" aria-label="可提及的用户">
          <button v-for="(user, index) in candidates" :id="`${menuId}-${index}`" :key="user.id"
            type="button" role="option" :aria-selected="index === selectedIndex"
            :class="{ selected: index === selectedIndex }" @mousedown.prevent @click="insertMention(user)">
            <img :src="user.avatarAddress" alt="">
            <span>{{ user.userName }}</span><small v-if="user.isFollowFlag">已关注</small>
          </button>
          <p v-if="!candidates.length" class="panel-status">没有找到相关用户</p>
        </div>
      </div>
    </el-popover>
    <div class="editor-toolbar">
      <div class="editor-tools">
        <el-popover v-model:visible="emojiOpen" trigger="click" placement="bottom-start"
          :width="320" :show-arrow="false" :popper-style="panelStyle" @show="closeMentions">
          <template #reference>
            <button type="button" class="editor-tool" aria-label="选择表情" :disabled="disabled" @mousedown.prevent>
              <svg viewBox="0 0 24 24" aria-hidden="true"><circle cx="12" cy="12" r="9"/><path d="M8 14a4 4 0 0 0 8 0M8 9h.01M16 9h.01"/></svg><span>表情</span>
            </button>
          </template>
          <div class="emoji-panel">
            <div class="panel-heading"><strong>表情</strong></div>
            <div class="emoji-grid">
              <button v-for="number in 176" :key="number" type="button" :title="`表情 ${number}`"
                :aria-label="`表情 ${number}`" @mousedown.prevent @click="insertEmoji(number)">
                <img :src="`/img/emoji/${number}.png`" alt="" loading="lazy">
              </button>
            </div>
          </div>
        </el-popover>
        <slot name="tools"/>
        <button type="button" class="editor-tool mention-tool" aria-label="提及用户" title="提及用户"
          :disabled="disabled" @mousedown.prevent @click="insertMentionPrefix">
          <svg viewBox="0 0 24 24" aria-hidden="true"><circle cx="12" cy="12" r="4"/><path d="M16 8v5a3 3 0 0 0 6 0v-1a10 10 0 1 0-3.92 7.94"/></svg>
        </button>
      </div>
      <span class="editor-count" :class="{ 'is-over-limit': length > maxLength }">{{ length }} / {{ maxLength }}</span>
      <div v-if="$slots.actions" class="editor-actions"><slot name="actions"/></div>
    </div>
  </div>
</template>

<script setup>
import { computed, getCurrentInstance, nextTick, onMounted, onUnmounted, ref, watch } from 'vue';
import { fetchMentionUsers } from '@/api/product';
import { useGlobalStore } from '@/store/store';
import { sanitizeRichText } from '@/utils/richText';

const props = defineProps({
  modelValue: { type: String, default: '' },
  placeholder: { type: String, default: '分享此刻的想法…' },
  maxLength: { type: Number, default: 2000 },
  compact: { type: Boolean, default: false },
  disabled: { type: Boolean, default: false },
});
const emit = defineEmits(['update:modelValue', 'length']);
const store = useGlobalStore();
const editor = ref(null);
const focused = ref(false);
const composing = ref(false);
const length = ref(0);
const emojiOpen = ref(false);
const mentionOpen = ref(false);
const mentionList = ref(null);
const mentionLoading = ref(false);
const mentionError = ref('');
const mentionUsers = ref([]);
const mentionQuery = ref('');
const selectedIndex = ref(0);
const menuId = `post-editor-mentions-${getCurrentInstance().uid}`;
const panelStyle = { maxWidth: 'calc(100vw - 24px)', padding: '12px', borderRadius: '12px' };
const candidates = computed(() => mentionUsers.value
  .filter(user => user.userName.toLocaleLowerCase().includes(mentionQuery.value.toLocaleLowerCase()))
  .slice(0, 20));
let savedRange;
let mentionRange;
let lastEmitted = '';
let mentionsLoaded = false;
let generation = 0;

function signedIn() {
  if (store.token) return true;
  store.loginDialogVisible = true;
  return false;
}
function saveSelection() {
  const selection = window.getSelection();
  if (selection?.rangeCount && editor.value?.contains(selection.anchorNode)) {
    savedRange = selection.getRangeAt(0).cloneRange();
  }
}
function selectRange(range) {
  const selection = window.getSelection();
  selection.removeAllRanges();
  selection.addRange(range);
  savedRange = range.cloneRange();
}
function focus() {
  if (props.disabled || !editor.value) return;
  editor.value.focus();
  const range = savedRange && editor.value.contains(savedRange.commonAncestorContainer)
    ? savedRange.cloneRange() : document.createRange();
  if (!savedRange || !editor.value.contains(savedRange.commonAncestorContainer)) {
    range.selectNodeContents(editor.value);
    range.collapse(false);
  }
  selectRange(range);
}
function updateLength() {
  length.value = (editor.value?.innerText.trim().length || 0) + (editor.value?.querySelectorAll('img').length || 0) * 5;
  emit('length', length.value);
}
function updateContent() {
  if (composing.value) return;
  lastEmitted = sanitizeRichText(editor.value?.innerHTML || '');
  emit('update:modelValue', lastEmitted);
  updateLength();
  saveSelection();
  updateMention();
}
function closeMentions() {
  mentionOpen.value = false;
  mentionRange = null;
}
async function loadMentions() {
  if (mentionLoading.value || mentionsLoaded) return;
  if (!signedIn()) { closeMentions(); return; }
  const current = generation;
  mentionLoading.value = true;
  mentionError.value = '';
  try {
    const users = await fetchMentionUsers();
    if (current !== generation) return;
    mentionUsers.value = [...new Map([...users.friendList, ...users.otherList]
      .filter(user => user.id !== store.userId).map(user => [user.id, user])).values()];
    mentionsLoaded = true;
  } catch (error) {
    if (current === generation) mentionError.value = error.message || '用户列表加载失败';
  } finally {
    if (current === generation) mentionLoading.value = false;
  }
}
function updateMention() {
  const selection = window.getSelection();
  if (props.disabled || composing.value || !selection?.rangeCount || !selection.isCollapsed ||
    !editor.value?.contains(selection.anchorNode) || selection.anchorNode.nodeType !== Node.TEXT_NODE ||
    selection.anchorNode.parentElement?.closest('a')) { closeMentions(); return; }
  const node = selection.anchorNode;
  const text = node.textContent.slice(0, selection.anchorOffset);
  const start = text.lastIndexOf('@');
  const query = text.slice(start + 1);
  if (start < 0 || (start > 0 && /[a-zA-Z0-9_.+-]/.test(text[start - 1])) ||
    /[\s@]/.test(query) || query.length > 30) { closeMentions(); return; }
  const range = document.createRange();
  range.setStart(node, start);
  range.setEnd(node, selection.anchorOffset);
  mentionRange = range;
  mentionQuery.value = query;
  selectedIndex.value = 0;
  emojiOpen.value = false;
  mentionOpen.value = true;
  loadMentions();
}
function insertNode(node) {
  if (props.disabled || !editor.value) return;
  focus();
  const range = savedRange.cloneRange();
  range.deleteContents();
  range.insertNode(node);
  if (node.nodeType === Node.TEXT_NODE) range.setStart(node, node.textContent.length);
  else range.setStartAfter(node);
  range.collapse(true);
  selectRange(range);
  updateContent();
}
function insertEmoji(number) {
  if (!signedIn() || props.disabled) return;
  const image = document.createElement('img');
  image.src = `/img/emoji/${number}.png`;
  image.alt = '[表情]';
  insertNode(image);
}
function insertMentionPrefix() {
  if (!signedIn() || props.disabled || !editor.value) return;
  focus();
  const before = savedRange.cloneRange();
  before.selectNodeContents(editor.value);
  before.setEnd(savedRange.startContainer, savedRange.startOffset);
  const separator = /[a-zA-Z0-9_.+-]$/.test(before.toString()) ? ' ' : '';
  insertNode(document.createTextNode(`${separator}@`));
}
function insertMention(user) {
  if (!mentionRange || !editor.value?.contains(mentionRange.commonAncestorContainer)) return;
  savedRange = mentionRange.cloneRange();
  closeMentions();
  const link = document.createElement('a');
  link.href = `/home?homeMenu=1&userId=${user.id}`;
  link.setAttribute('data-eit-userid', String(user.id));
  link.textContent = `@${user.userName}`;
  link.contentEditable = 'false';
  insertNode(link);
  insertNode(document.createTextNode(' '));
}
function deleteAdjacentMention(backward) {
  const selection = window.getSelection();
  if (props.disabled || !selection?.rangeCount || !selection.isCollapsed ||
    !editor.value?.contains(selection.anchorNode)) return false;
  const range = selection.getRangeAt(0);
  let node = range.startContainer;
  if (node.nodeType === Node.TEXT_NODE) {
    const remaining = backward ? node.textContent.slice(0, range.startOffset) : node.textContent.slice(range.startOffset);
    if (remaining.length) return false;
    node = nextNode(node);
  } else {
    const child = node.childNodes[backward ? range.startOffset - 1 : range.startOffset];
    if (child) node = child;
    else node = nextNode(node);
  }
  if (node?.nodeType === Node.TEXT_NODE && !node.textContent.length) node = nextNode(node);
  while (node && node !== editor.value) {
    if (node.nodeType === Node.ELEMENT_NODE && node.matches('a[data-eit-userid]')) {
      range.selectNode(node);
      range.deleteContents();
      range.collapse(true);
      selectRange(range);
      updateContent();
      return true;
    }
    if (node.nodeType === Node.TEXT_NODE && node.textContent.length) return false;
    if (node.nodeType === Node.ELEMENT_NODE && node.tagName === 'BR') return false;
    node = (backward ? node.lastChild : node.firstChild) || nextNode(node);
  }
  return false;

  function nextNode(current) {
    while (current && current !== editor.value) {
      const sibling = backward ? current.previousSibling : current.nextSibling;
      if (sibling) return sibling;
      current = current.parentNode;
    }
    return null;
  }
}
function onBeforeInput(event) {
  if (composing.value || event.isComposing) return;
  if (event.inputType === 'deleteContentBackward' || event.inputType === 'deleteContentForward') {
    if (deleteAdjacentMention(event.inputType === 'deleteContentBackward')) event.preventDefault();
  }
}
function onKeydown(event) {
  if (composing.value || event.isComposing) return;
  if ((event.key === 'Backspace' || event.key === 'Delete') && !event.ctrlKey && !event.metaKey && !event.altKey) {
    if (deleteAdjacentMention(event.key === 'Backspace')) { event.preventDefault(); return; }
  }
  if (!mentionOpen.value) return;
  if (event.key === 'Escape') { event.preventDefault(); closeMentions(); return; }
  if (!candidates.value.length) return;
  if (event.key === 'ArrowDown' || event.key === 'ArrowUp') {
    event.preventDefault();
    selectedIndex.value = (selectedIndex.value + (event.key === 'ArrowDown' ? 1 : -1) + candidates.value.length) % candidates.value.length;
    nextTick(() => mentionList.value?.querySelector('[aria-selected="true"]')?.scrollIntoView({ block: 'nearest' }));
  } else if (event.key === 'Enter') {
    event.preventDefault();
    insertMention(candidates.value[selectedIndex.value]);
  }
}
function onCaretMove(event) {
  if (composing.value || ['ArrowUp', 'ArrowDown', 'Enter', 'Escape'].includes(event.key)) return;
  saveSelection();
  updateMention();
}
function onBlur() { focused.value = false; saveSelection(); closeMentions(); }
function onEditorClick(event) {
  if (event.target instanceof Element && event.target.closest('a')) event.preventDefault();
}
function onCompositionStart() { composing.value = true; closeMentions(); }
function onCompositionEnd() { composing.value = false; updateContent(); }
function paste(event) {
  if (props.disabled) return;
  const text = event.clipboardData.getData('text/plain');
  if (/^https?:\/\/\S+$/.test(text)) {
    const link = document.createElement('a');
    link.href = text;
    link.textContent = text;
    insertNode(link);
  } else insertNode(document.createTextNode(text));
}
function applyValue(value) {
  if (!editor.value || value === lastEmitted) return;
  editor.value.innerHTML = sanitizeRichText(value);
  for (const link of editor.value.querySelectorAll('a[data-eit-userid]')) link.contentEditable = 'false';
  lastEmitted = value;
  savedRange = null;
  closeMentions();
  updateLength();
}
watch(() => props.modelValue, applyValue);
watch(() => props.disabled, value => { if (value) { emojiOpen.value = false; closeMentions(); } });
watch(() => store.userId, () => {
  generation++;
  mentionUsers.value = [];
  mentionsLoaded = false;
  mentionLoading.value = false;
  mentionError.value = '';
  closeMentions();
});
onMounted(() => { applyValue(props.modelValue); });
onUnmounted(() => { generation++; });
defineExpose({ focus });
</script>

<style scoped>
.post-editor { box-sizing: border-box; width: 100%; min-width: 0; border: 1px solid var(--line); border-radius: 12px; background: #fff; color: var(--ink); font-family: var(--el-font-family); }
.post-editor.is-focused { border-color: var(--brand-light-5); box-shadow: 0 0 0 3px var(--brand-light-9); }
.post-editor.is-disabled { background: var(--fill-soft); }
.editor-input { position: relative; min-height: 112px; max-height: 320px; padding: 12px 14px; overflow: auto; outline: none; white-space: pre-wrap; overflow-wrap: anywhere; font-size: 14px; line-height: 1.8; cursor: text; scroll-margin-top: calc(var(--head-h) + 24px); }
.compact .editor-input { min-height: 76px; font-size: 13px; }
.editor-input.is-empty::before { position: absolute; top: 13px; left: 14px; right: 14px; content: attr(data-placeholder); color: var(--ink-3); pointer-events: none; }
.editor-input :deep(img) { width: 24px; height: 24px; vertical-align: middle; }
.editor-input :deep(a) { color: var(--brand-hover); text-decoration: none; }
.editor-input :deep(a[data-eit-userid]) { padding: 1px 3px; border-radius: 4px; background: var(--brand-light-9); }
.editor-toolbar, .editor-tools, .editor-actions { display: flex; align-items: center; flex-wrap: wrap; gap: 8px; }
.editor-toolbar { min-width: 0; padding: 7px 10px; border-top: 1px solid var(--fill); }
.editor-tools { min-width: 0; gap: 4px; }
.editor-tools :deep(.editor-tool) { box-sizing: border-box; display: inline-flex; flex: none; justify-content: center; align-items: center; gap: 5px; height: 30px; margin: 0; padding: 0 7px; border: 0; border-radius: 6px; background: transparent; color: var(--ink-2); font-family: inherit; font-size: 12px; font-weight: 400; line-height: 20px; cursor: pointer; }
.editor-tools :deep(.editor-tool:hover) { background: var(--fill-soft); color: var(--brand-hover); }
.editor-tools :deep(.editor-tool:disabled) { cursor: not-allowed; opacity: .5; }
.editor-tools :deep(.editor-tool > svg) { display: block; flex: none; width: 18px; height: 18px; margin: 0; fill: none; stroke: currentColor; stroke-width: 1.7; stroke-linecap: round; stroke-linejoin: round; }
.editor-tools :deep(.editor-tool > span) { display: inline-flex; align-items: center; height: 20px; line-height: 20px; }
.editor-tools .mention-tool { width: 30px; padding: 0; }
.editor-count { color: var(--ink-3); font-size: 11px; line-height: 20px; }
.editor-count { margin-left: auto; font-variant-numeric: tabular-nums; }
.editor-count.is-over-limit { color: var(--el-color-danger); }
.editor-actions { flex: none; max-width: 100%; }
.editor-actions :deep(.el-button) { flex: none; width: auto; max-width: 100%; height: 32px; margin: 0; padding: 0 14px; border-radius: 7px; font-size: 12px; }
.emoji-panel, .mention-panel { min-width: 0; color: var(--ink); font-family: var(--el-font-family); }
.panel-heading { display: flex; justify-content: space-between; align-items: center; gap: 8px; padding: 0 2px 10px; border-bottom: 1px solid var(--fill); }
.panel-heading strong { font-size: 13px; font-weight: 600; }
.panel-heading span { color: var(--ink-3); font-size: 11px; }
.emoji-grid { display: grid; grid-template-columns: repeat(8, minmax(0, 1fr)); gap: 3px; max-height: 224px; margin-top: 8px; overflow-y: auto; overscroll-behavior: contain; scrollbar-width: thin; }
.emoji-grid button { display: grid; place-items: center; min-width: 0; height: 32px; padding: 3px; border: 0; border-radius: 6px; background: transparent; cursor: pointer; }
.emoji-grid button:hover { background: var(--brand-light-9); }
.emoji-grid img { display: block; width: 24px; height: 24px; }
.mention-list { max-height: 248px; margin-top: 6px; overflow-y: auto; overscroll-behavior: contain; scrollbar-width: thin; }
.mention-list button { display: flex; align-items: center; gap: 9px; width: 100%; min-width: 0; min-height: 44px; padding: 7px 8px; border: 0; border-radius: 7px; background: transparent; color: var(--ink-2); font: inherit; font-size: 13px; text-align: left; cursor: pointer; }
.mention-list button:hover, .mention-list button.selected { background: var(--brand-light-9); color: var(--brand-hover); }
.mention-list img { flex: none; width: 28px; height: 28px; border-radius: 50%; object-fit: cover; }
.mention-list button > span { flex: 1; min-width: 0; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.mention-list small { flex: none; color: var(--ink-3); font-size: 11px; }
.panel-status { margin: 0; padding: 16px 8px; color: var(--ink-3); font-size: 12px; }
.panel-status p { margin: 0 0 6px; }
.editor-tool:focus-visible, .emoji-grid button:focus-visible, .mention-list button:focus-visible { outline: 2px solid var(--brand); outline-offset: -2px; }
@media (max-width: 640px) {
  .editor-input { min-height: 100px; padding: 10px 12px; }
  .editor-input.is-empty::before { top: 16px; left: 12px; right: 12px; }
  .compact .editor-input { min-height: 72px; }
  .editor-toolbar { gap: 6px; padding: 7px 8px; }
}
</style>
