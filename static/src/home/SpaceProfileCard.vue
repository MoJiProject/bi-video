<template>
  <aside class="space-profile" aria-label="个人资料">
    <header>
      <h3>个人资料</h3>
      <a v-if="store.homeUserInformation.id === store.userId" href="/account" target="_blank" rel="noopener noreferrer">编辑</a>
    </header>
    <dl>
      <div><dt>UID</dt><dd>{{ store.homeUserInformation.id }}</dd></div>
      <div v-if="store.homeUserInformation.birthday">
        <dt>生日</dt><dd>{{ store.homeUserInformation.birthday.split(' ')[0] }}</dd>
      </div>
    </dl>
  </aside>
</template>

<script setup>
import { useGlobalStore } from '@/store/store';

const store = useGlobalStore();
</script>

<style scoped>
.space-profile { box-sizing: border-box; position: sticky; top: 20px; width: 100%; min-width: 0; padding: 18px; border: 1px solid var(--line); border-radius: 16px; background: #fff; color: var(--ink-2); font-family: var(--el-font-family); }
header { display: flex; align-items: center; justify-content: space-between; gap: 12px; padding-bottom: 12px; border-bottom: 1px solid var(--line); }
h3 { margin: 0; color: var(--ink); font-size: 15px; font-weight: 600; line-height: 1.5; }
header a { padding: 4px 8px; border-radius: 6px; background: var(--brand-light-9); color: var(--brand-hover); font-size: 12px; line-height: 20px; text-decoration: none; }
header a:hover { background: var(--brand-soft); }
header a:focus-visible { outline: 2px solid var(--brand); outline-offset: 2px; }
dl { display: grid; gap: 12px; margin: 14px 0 0; font-size: 13px; line-height: 1.6; }
dl > div { display: grid; grid-template-columns: 40px minmax(0, 1fr); gap: 8px; }
dt { color: var(--ink-3); }
dd { min-width: 0; margin: 0; overflow-wrap: anywhere; }
@media (max-width: 960px) {
  .space-profile { position: static; }
  dl { display: flex; flex-wrap: wrap; gap: 12px 28px; }
}
@media (max-width: 640px) {
  .space-profile { padding: 16px; border-radius: 14px; }
}
</style>
