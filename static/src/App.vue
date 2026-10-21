<template>
  <router-view class="router-view"/>
  <component v-if="$offlinePreview" :is="'OfflinePreviewToolbar'"/>
</template>

<script>

export default {
  name: 'App',

}

</script>
