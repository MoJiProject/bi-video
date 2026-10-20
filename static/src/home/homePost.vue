<template>
  <article class="home-post">
    <header>
      <img :src="dynamic.authorAvatar" alt="">
      <span>{{ dynamic.authorNickname }}</span>
      <span v-if="dynamic.sourceType === 2">图文评论</span>
      <time>{{ dynamic.createdAt?.replace('T', ' ') }}</time>
      <el-button v-if="dynamic.isSelf && dynamic.sourceType !== 2" text type="danger" :loading="deleting" @click="remove">删除</el-button>
    </header>
    <p>{{ dynamic.content }}</p>
    <div v-if="dynamic.imageList?.length" class="images">
      <el-image v-for="image in dynamic.imageList" :key="image" :src="image"
        :preview-src-list="dynamic.imageList" preview-teleported fit="cover"/>
    </div>
    <a v-if="dynamic.videoId" :href="'./video?videoId=BV' + dynamic.videoId" target="_blank">
      <img :src="dynamic.videoCover" alt="">{{ dynamic.videoTitle }}
    </a>
    <footer v-if="dynamic.sourceType !== 2">
      <el-button text :type="liked ? 'primary' : ''" :loading="liking" @click="toggleLike">{{ likeCount }} 赞</el-button>
      <a :href="'./dynamicDetail?dynamicId=' + dynamic.id">{{ dynamic.commentCount || 0 }} 评论</a>
    </footer>
    <footer v-else>{{ dynamic.likeCount || 0 }} 赞</footer>
  </article>
</template>

<script setup>
import { ref, watch } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { socialApi } from '@/api/product';
import { useGlobalStore } from '@/store/store';
import { post } from '@/services/http';

const props = defineProps({ dynamic: { type: Object, required: true } });
const store = useGlobalStore();
const deleting = ref(false);
const liked = ref(!!props.dynamic.liked);
const likeCount = ref(props.dynamic.likeCount || 0);
const liking = ref(false);
watch(() => [props.dynamic.id, props.dynamic.liked, props.dynamic.likeCount], () => {
  liked.value = !!props.dynamic.liked;
  likeCount.value = props.dynamic.likeCount || 0;
});
async function toggleLike() {
  if (!store.token) { store.loginDialogVisible = true; return; }
  liking.value = true;
  try {
    const result = await post(`/post/${props.dynamic.id}/like`);
    likeCount.value = result.likeCount;
    liked.value = result.liked;
  } catch (error) { ElMessage.error(error.message); }
  finally { liking.value = false; }
}

async function remove() {
  const confirmed = await ElMessageBox.confirm('确定删除这条动态？', '删除动态', {
    type: 'warning',
  }).then(() => true, () => false);
  if (!confirmed) return;
  deleting.value = true;
  try {
    await socialApi.deletePost(props.dynamic.id);
    store.deleteFlag = props.dynamic.id;
  } catch (error) {
    ElMessage.error(error.message);
  } finally {
    deleting.value = false;
  }
}
</script>

<style scoped>
.home-post { padding: 24px; border-bottom: 1px solid #EFF3F2; background: white; }
header { display: flex; align-items: center; gap: 12px; flex-wrap: wrap; }
header img { flex: none; width: 40px; height: 40px; border-radius: 50%; }
header span { min-width: 0; overflow-wrap: anywhere; }
time, footer { color: #8D9794; font-size: 13px; }
p { white-space: pre-wrap; overflow-wrap: anywhere; line-height: 1.7; }
.images { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 8px; max-width: 640px; }
.images .el-image { min-width: 0; width: 100%; aspect-ratio: 1; border-radius: 6px; }
a { display: flex; align-items: center; gap: 16px; color: inherit; text-decoration: none; margin: 16px 0; overflow-wrap: anywhere; }
a img { flex: none; width: 180px; max-width: 40%; border-radius: 6px; }
footer { margin-top: 16px; display: flex; align-items: center; gap: 16px; }
footer a { margin: 0; }
@media (max-width: 640px) {
  .home-post { padding: 16px 12px; }
  header { gap: 8px; }
  header time { flex-basis: 100%; }
  a { gap: 10px; font-size: 13px; }
}
</style>
