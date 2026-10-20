<template>
  <main class="feed">
    <h1>动态</h1>
    <el-card v-if="store.token" class="composer">
      <el-input v-model="content" type="textarea" :rows="4" maxlength="1000" show-word-limit placeholder="分享你的想法"/>
      <div class="attachments">
        <el-tag v-for="(image, index) in images" :key="image" closable @close="images.splice(index, 1)">图片 {{ index + 1 }}</el-tag>
        <input type="file" accept="image/png,image/jpeg,image/gif" :disabled="uploading || images.length >= 9" @change="upload">
      </div>
      <div class="publish">
        <el-select v-model="visibility" style="width: 140px"><el-option label="公开" :value="0"/>
          <el-option label="仅自己" :value="1"/></el-select>
        <el-button type="primary" :loading="publishing || uploading" @click="publish">发布</el-button>
      </div>
    </el-card>
    <el-radio-group v-model="mine" class="tabs">
      <el-radio-button :value="false">公开动态</el-radio-button><el-radio-button :value="true" :disabled="!store.userId">我的动态</el-radio-button>
    </el-radio-group>
    <homePost v-for="post in rows" :key="post.id" :dynamic="post"/>
    <loadingIndicator v-if="loading"/>
    <el-empty v-if="!loading && !rows.length" description="暂无动态"/>
    <el-button v-if="hasMore" :loading="loading" @click="load">加载更多</el-button>
  </main>
</template>
<script setup>
import { ref, watch } from 'vue';
import { ElMessage } from 'element-plus';
import { socialApi } from '@/api/product';
import { post } from '@/services/http';
import { useGlobalStore } from '@/store/store';
import homePost from '@/home/homePost.vue';
import loadingIndicator from '@/components/loadingIndicator.vue';
const store = useGlobalStore();
const rows = ref([]);
const mine = ref(false);
const cursorId = ref(null);
const hasMore = ref(false);
const loading = ref(false);
const content = ref('');
const images = ref([]);
const visibility = ref(0);
const publishing = ref(false);
const uploading = ref(false);
let generation = 0;
async function load() {
  const current = generation;
  loading.value = true;
  try {
    const page = await socialApi.posts({ targetUserId: mine.value ? store.userId : undefined, cursorId: cursorId.value, size: 20 });
    if (current !== generation) return;
    const ids = new Set(rows.value.map(item => item.id));
    rows.value.push(...page.records.filter(item => !ids.has(item.id)));
    cursorId.value = page.cursorId;
    hasMore.value = page.hasMore;
  } catch (error) { if (current === generation) ElMessage.error(error.message); }
  finally { if (current === generation) loading.value = false; }
}
function reload() { generation++; rows.value = []; cursorId.value = null; hasMore.value = false; load(); }
async function upload(event) {
  const file = event.target.files[0];
  if (!file) return;
  uploading.value = true;
  try {
    const body = new FormData();
    body.append('file', file);
    const result = await post('/media/image', body);
    images.value.push(result.url);
  } catch (error) { ElMessage.error(error.message); }
  finally { uploading.value = false; event.target.value = ''; }
}
async function publish() {
  if (!content.value.trim() && !images.value.length) { ElMessage.warning('请输入内容或添加图片'); return; }
  publishing.value = true;
  try {
    await socialApi.createPost({ content: content.value, imageUrls: images.value, visibility: visibility.value });
    content.value = ''; images.value = []; reload();
    ElMessage.success('发布成功');
  } catch (error) { ElMessage.error(error.message); }
  finally { publishing.value = false; }
}
watch([mine, () => store.userId], reload, { immediate: true });
watch(() => store.deleteFlag, id => { rows.value = rows.value.filter(row => row.id !== id); });
</script>
<style scoped>
.feed { box-sizing: border-box; width: 100%; max-width: var(--page-max); margin: auto; padding: var(--gap-5) var(--page-pad); }
.feed :deep(.home-post > p) { max-width: var(--col-narrow); }
.composer :deep(.el-textarea) { max-width: var(--col-narrow); }
.composer { margin-bottom: 24px; }
.attachments { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; margin: 16px 0; }
.attachments input { min-width: 0; max-width: 100%; }
.attachments :deep(.el-tag) { max-width: 100%; }
.publish { display: flex; justify-content: space-between; gap: 16px; }
.tabs { margin-bottom: 24px; }
@media (max-width: 640px) {
  .feed { padding-top: var(--gap-4); }
  h1 { margin-top: 0; font-size: 24px; }
  .composer :deep(.el-card__body) { padding: var(--gap-4) var(--gap-3); }
  .publish { flex-wrap: wrap; gap: var(--gap-3); }
}
</style>
