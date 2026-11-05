import { reactive } from 'vue';

export const mentionHoverState = reactive({
  visible: false,
  userId: 0,
  nickname: '',
  x: 0,
  y: 0,
  cardHovered: false,
});

let hideTimer = null;

export function showMentionHover(payload) {
  clearTimeout(hideTimer);
  mentionHoverState.userId = payload.userId;
  mentionHoverState.nickname = payload.nickname || '';
  mentionHoverState.x = payload.x;
  mentionHoverState.y = payload.y;
  mentionHoverState.cardHovered = false;
  mentionHoverState.visible = true;
}

export function hideMentionHover(delay = 220) {
  clearTimeout(hideTimer);
  hideTimer = setTimeout(() => {
    if (!mentionHoverState.cardHovered) mentionHoverState.visible = false;
  }, delay);
}
