import { showMentionHover, hideMentionHover } from '../utils/mentionHoverState';

function isExcluded(link) {
  return !!link.closest('.post-editor, .editor-input');
}

function handleLink(link) {
  if (!link || isExcluded(link)) return;
  const userId = Number(link.getAttribute('data-eit-userid'));
  if (!Number.isSafeInteger(userId) || userId <= 0) return;
  const rect = link.getBoundingClientRect();
  showMentionHover({
    userId,
    nickname: (link.textContent || '').trim().replace(/^@/, ''),
    x: Math.min(rect.right + 8, window.innerWidth - 320),
    y: Math.max(8, rect.top - 8),
  });
}

function findLink(target) {
  return target && typeof target.closest === 'function'
    ? target.closest('a[data-eit-userid]')
    : null;
}

function onOver(event) {
  const link = findLink(event.target);
  if (link) handleLink(link);
}

function onOut(event) {
  const link = findLink(event.target);
  if (link) hideMentionHover(220);
}

export default {
  mounted(el) {
    el.addEventListener('mouseover', onOver);
    el.addEventListener('mouseout', onOut);
  },
  unmounted(el) {
    el.removeEventListener('mouseover', onOver);
    el.removeEventListener('mouseout', onOut);
  },
};
