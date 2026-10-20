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
    <footer>{{ dynamic.likeCount || 0 }} 赞 · {{ dynamic.commentCount || 0 }} 评论</footer>
  </article>
</template>

<script setup>
import { ref } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { socialApi } from '@/api/product';
import { useGlobalStore } from '@/store/store';

const props = defineProps({ dynamic: { type: Object, required: true } });
const store = useGlobalStore();
const deleting = ref(false);

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
header { display: flex; align-items: center; gap: 12px; }
header img { width: 40px; height: 40px; border-radius: 50%; }
time, footer { color: #8D9794; font-size: 13px; }
p { white-space: pre-wrap; overflow-wrap: anywhere; line-height: 1.7; }
.images { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 8px; }
.images .el-image { height: 160px; border-radius: 6px; }
a { display: flex; align-items: center; gap: 16px; color: inherit; text-decoration: none; margin: 16px 0; }
a img { width: 180px; border-radius: 6px; }
footer { margin-top: 16px; }
</style>
