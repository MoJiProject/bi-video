<template>
  <details class="offline-preview-toolbar" open>
    <summary>离线预览 · 示例账号与数据</summary>
    <p>未连接后端；提交、上传不保存。视频仅展示示例封面和控件。</p>
    <nav aria-label="预览页面">
      <a v-for="item in pages" :key="item.href" :href="item.href">{{
        item.label
      }}</a>
      <a class="exit-preview" href="/?preview=0">退出预览</a>
    </nav>
  </details>
</template>

<script setup>
const pages = [
  { label: "首页", href: "/" },
  { label: "消息", href: "/message" },
  { label: "投稿", href: "/contribute" },
  { label: "稿件管理", href: "/contribute/subpage2" },
  { label: "创作数据", href: "/contribute/subpage1" },
  { label: "视频详情", href: "/video?videoId=BV101" },
  { label: "个人空间", href: "/home?homeMenu=1&userId=1001" },
  { label: "个人账户", href: "/account" },
];
</script>

<style scoped>
.offline-preview-toolbar {
  position: fixed;
  right: 12px;
  bottom: 12px;
  z-index: 30000;
  width: min(520px, calc(100vw - 24px));
  max-height: min(240px, 50dvh);
  overflow: auto;
  box-sizing: border-box;
  padding: 12px 16px;
  border: 1px solid var(--brand);
  border-radius: 12px;
  background: #fff;
  color: var(--ink);
  box-shadow: 0 8px 32px #0002;
  font: 13px/1.6 sans-serif;
}
summary {
  cursor: pointer;
  color: var(--brand-hover);
  font-weight: 600;
}
p {
  margin: 8px 0;
  color: var(--ink-2);
  font-size: 12px;
}
nav {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}
a {
  padding: 4px 9px;
  border-radius: 6px;
  background: var(--brand-light-9);
  color: var(--brand-hover);
  text-decoration: none;
}
a:hover,
a:focus-visible {
  outline: 2px solid var(--brand);
  outline-offset: 1px;
}
.exit-preview {
  color: #a54332;
  background: #fff1ec;
}
</style>
