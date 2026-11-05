<template>
  <router-view class="router-view"/>
  <component v-if="$offlinePreview" :is="'OfflinePreviewToolbar'"/>
  <GlobalMentionHoverCard />
</template>

<script>
import GlobalMentionHoverCard from "./components/GlobalMentionHoverCard.vue";

export default {
  name: 'App',
  components: { GlobalMentionHoverCard },
}
</script>
