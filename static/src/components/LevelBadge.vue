<template>
  <span
    v-if="level != null && level !== '' && Number(level) >= 0"
    class="level-badge"
    :class="['level-badge-' + safeLevel, sizeClass]"
    :title="safeLevel + ' 级'"
  >Lv.{{ safeLevel }}</span>
</template>

<script setup>
import { computed } from 'vue';

const props = defineProps({
  // 用户等级（0-10），对应旧版 /img/N级.png 图标
  level: { type: [Number, String], default: 0 },
  // 尺寸：xs 评论列表 / sm 悬浮卡 / md 个人空间头部
  size: { type: String, default: 'sm' },
});

const safeLevel = computed(() => Math.min(10, Math.max(0, Number(props.level) || 0)));
const sizeClass = computed(() => 'level-badge--' + (['xs', 'sm', 'md'].includes(props.size) ? props.size : 'sm'));
</script>

<style lang="scss" scoped>
// 统一等级徽章：0-10 级各有配色，随 size 控制大小
.level-badge {
  display: inline-flex;
  align-items: center;
  flex: none;
  padding: 0 5px;
  border-radius: 4px;
  font-family: inherit;
  font-size: 10px;
  font-weight: 600;
  line-height: 15px;
  vertical-align: middle;
  white-space: nowrap;
  user-select: none;
}

.level-badge--xs { font-size: 9px; line-height: 14px; padding: 0 4px; border-radius: 3.5px; }
.level-badge--sm { font-size: 10px; line-height: 15px; }
.level-badge--md { font-size: 12px; line-height: 19px; padding: 0 7px; border-radius: 5px; }

// 分级配色：与资料卡 profile-level 系列一致
.level-badge-0  { background: #eef1f4; color: #6b7683; }
.level-badge-1  { background: #e8f4ff; color: #2f7fd6; }
.level-badge-2  { background: #e6f7ef; color: #23a06c; }
.level-badge-3  { background: #eaf7e6; color: #6faf2f; }
.level-badge-4  { background: #fdf3e0; color: #d98a1f; }
.level-badge-5  { background: #fdeee0; color: #e2703d; }
.level-badge-6  { background: #fde9ec; color: #dd4a68; }
.level-badge-7  { background: #f3e9fd; color: #8a4fd3; }
.level-badge-8  { background: #e9e9fd; color: #5557d9; }
.level-badge-9  { background: #33343d; color: #d8b15a; }
.level-badge-10 {
  background: linear-gradient(135deg, #2b2c35, #4a3a1d);
  color: #ffd873;
  box-shadow: 0 0 4px rgba(255, 216, 115, .45);
}
</style>
