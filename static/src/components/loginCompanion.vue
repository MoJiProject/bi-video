<template>
  <svg
    class="login-companion"
    :class="{ 'is-private': privateMode }"
    viewBox="0 0 160 140"
    role="img"
    :aria-label="privateMode ? '青芒伙伴捂眼保护密码' : '青芒伙伴睁眼陪伴'"
  >
    <ellipse cx="80" cy="128" rx="53" ry="7" fill="#e7efdf" />
    <g :transform="side === 'right' ? 'translate(160 0) scale(-1 1)' : undefined">
      <path d="M53 113Q40 132 27 118M107 113q13 19 26 5" fill="none" stroke="#26775d" stroke-width="11" stroke-linecap="round" />
      <path d="M80 35Q49 32 48 12q26-3 32 23" fill="#8ebc65" />
      <path d="M80 35Q83 5 108 9q4 22-28 26" fill="#327b51" />
      <path d="M80 30c-34 0-53 22-53 52 0 28 21 44 53 44s53-16 53-44c0-30-19-52-53-52" fill="#c9df8a" />
      <path d="M80 35c-30 0-46 19-46 45" fill="none" stroke="#eaf4c7" stroke-width="5" stroke-linecap="round" />
      <ellipse cx="48" cy="88" rx="9" ry="5" fill="#ecac83" opacity=".65" />
      <ellipse cx="112" cy="88" rx="9" ry="5" fill="#ecac83" opacity=".65" />
      <g v-if="!privateMode" class="companion-eyes">
        <ellipse cx="61" cy="73" rx="5" ry="7" fill="#244c3c" />
        <ellipse cx="99" cy="73" rx="5" ry="7" fill="#244c3c" />
        <circle cx="62" cy="71" r="1.5" fill="#fff" />
        <circle cx="100" cy="71" r="1.5" fill="#fff" />
        <path d="M70 91q10 12 20 0" fill="none" stroke="#244c3c" stroke-width="3" stroke-linecap="round" />
        <path d="M30 101Q13 91 20 81m110 20q17-10 10-20" fill="none" stroke="#77a858" stroke-width="12" stroke-linecap="round" />
      </g>
      <g v-else class="companion-hands">
        <path d="M30 102Q29 60 61 69m69 33q1-42-31-33" fill="none" stroke="#4e8d5d" stroke-width="14" stroke-linecap="round" />
        <rect x="43" y="59" width="34" height="24" rx="11" fill="#609c64" transform="rotate(12 60 71)" />
        <rect x="83" y="59" width="34" height="24" rx="11" fill="#609c64" transform="rotate(-12 100 71)" />
        <path d="M74 94q6 4 12 0" fill="none" stroke="#244c3c" stroke-width="3" stroke-linecap="round" />
      </g>
    </g>
    <g v-if="privateMode" class="companion-lock">
      <circle cx="136" cy="29" r="18" fill="#126b58" />
      <rect x="129" y="27" width="14" height="11" rx="3" fill="#f4f5d8" />
      <path d="M132 27v-4a4 4 0 0 1 8 0v4" fill="none" stroke="#f4f5d8" stroke-width="2.5" />
    </g>
    <path v-else d="M137 16v12m-6-6h12" stroke="#dda45a" stroke-width="3" stroke-linecap="round" />
  </svg>
</template>

<script setup>
defineProps({
  privateMode: { type: Boolean, default: false },
  side: { type: String, default: "left" },
});
</script>

<style scoped>
.login-companion { display: inline-block; overflow: visible; vertical-align: middle; }
.companion-hands, .companion-lock { transform-origin: center; animation: cover-eyes .2s ease-out; }
@keyframes cover-eyes { from { opacity: 0; transform: translateY(8px); } to { opacity: 1; transform: translateY(0); } }
@media (prefers-reduced-motion: reduce) {
  .companion-hands, .companion-lock { animation: none; }
}
</style>
