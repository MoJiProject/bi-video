<template>
  <main class="detail">
    <loadingIndicator v-if="loading"/>
    <template v-else-if="dynamic">
      <homePost :dynamic="dynamic"/>
      <productComments :post-id="dynamic.id"/>
    </template>
    <el-empty v-else description="动态不存在或无权查看"/>
  </main>
</template>
<script setup>
import { onMounted, ref, watch } from 'vue';
import { ElMessage } from 'element-plus';
import { get } from '@/services/http';
import { useGlobalStore } from '@/store/store';
import homePost from '@/home/homePost.vue';
import productComments from '@/comment/productComments.vue';
import loadingIndicator from '@/components/loadingIndicator.vue';
const store = useGlobalStore();
const dynamic = ref(null);
const loading = ref(true);
const id = Number(new URLSearchParams(window.location.search).get('dynamicId'));
onMounted(async () => {
  try {
    if (!Number.isSafeInteger(id) || id <= 0) throw new Error('动态 ID 不正确');
    dynamic.value = await get(`/post/${id}`);
  } catch (error) { ElMessage.error(error.message); }
  finally { loading.value = false; }
});
watch(() => store.deleteFlag, value => { if (value === id) dynamic.value = null; });
</script>
<style scoped>.detail { max-width: 900px; margin: auto; padding: 24px 16px; }</style>
