<template>
  <div class="collections">
    <aside>
      <h3>{{ isOwner ? '我的收藏夹' : 'TA的收藏夹' }}</h3>
      <el-button v-if="isOwner" @click="openEditor()">新建收藏夹</el-button>
      <button v-for="folder in folders" :key="folder.id" :class="{ active: folder.id === selectedId }"
        @click="selectedId = folder.id">{{ folder.name }} · {{ folder.itemCount }}</button>
    </aside>
    <main>
      <loadingIndicator v-if="loadingFolders"/>
      <template v-else-if="selected">
        <header>
          <img :src="selected.coverUrl || '/img/collect-cover.png'" alt="">
          <div><h2>{{ selected.name }}</h2><p>{{ selected.description }}</p>
            <span>{{ selected.visibility === 1 ? '公开' : '私密' }}</span>
          </div>
          <el-button v-if="isOwner" @click="openEditor(selected)">编辑信息</el-button>
          <el-button v-if="isOwner && !selected.isDefault" type="danger" @click="removeFolder">删除收藏夹</el-button>
        </header>
        <div class="tools">
          <el-select v-model="sort" style="width: 150px">
            <el-option label="最近收藏" :value="1"/><el-option label="最多播放" :value="2"/>
            <el-option label="最近投稿" :value="3"/>
          </el-select>
          <el-select v-model="scope" style="width: 150px">
            <el-option label="当前收藏夹" :value="1"/><el-option label="全部收藏夹" :value="2"/>
          </el-select>
          <el-input v-model="keyword" placeholder="搜索视频" maxlength="50" clearable @keyup.enter="search" @clear="search"/>
          <el-button @click="search">搜索</el-button>
          <template v-if="isOwner && scope === 1">
            <el-button :disabled="busy" @click="batchMode = !batchMode">{{ batchMode ? '退出批量' : '批量操作' }}</el-button>
            <el-button :loading="busy" @click="clearInvalid">清除失效内容</el-button>
          </template>
        </div>
        <div v-if="batchMode && isOwner && scope === 1" class="batch-tools">
          <el-checkbox :model-value="allSelected" :indeterminate="selectedVideoIds.length > 0 && !allSelected"
            :disabled="busy || loadingVideos" @change="selectAll">本页全选</el-checkbox>
          <span>已选择 {{ selectedVideoIds.length }} 个视频</span>
          <el-button :disabled="!selectedVideoIds.length || busy" @click="openTransfer('copy')">复制至</el-button>
          <el-button :disabled="!selectedVideoIds.length || busy" @click="openTransfer('move')">移动至</el-button>
          <el-button type="danger" :disabled="!selectedVideoIds.length || busy" @click="deleteSelected">删除所选</el-button>
        </div>
        <loadingIndicator v-if="loadingVideos"/>
        <template v-else>
          <div class="videos">
            <div v-for="video in videos" :key="video.id">
              <el-checkbox v-if="batchMode && isOwner && scope === 1" :model-value="selectedVideoIds.includes(video.id)"
                :disabled="busy" @change="toggleVideo(video.id, $event)">选择视频</el-checkbox>
              <homeVideoCard :video="video"/>
              <div v-if="isOwner && scope === 1 && !batchMode" class="video-actions">
                <el-button text :disabled="busy" @click="openTransfer('copy', video.id)">复制</el-button>
                <el-button text :disabled="busy" @click="openTransfer('move', video.id)">移动</el-button>
                <el-button text type="danger" :disabled="busy" @click="removeVideo(video.id)">移出收藏夹</el-button>
              </div>
            </div>
          </div>
          <AppEmptyState v-if="!videos.length" :kind="appliedKeyword ? 'search' : 'empty'"
            :title="appliedKeyword ? '没有找到相关视频' : '收藏夹还是空的'"
            :description="appliedKeyword ? '试试其他关键词，或切换收藏夹继续查找' : '收藏喜欢的视频，下次就能在这里轻松找到'"/>
          <el-pagination v-if="total" v-model:current-page="pageNum" :page-size="20"
            layout="total, prev, pager, next, jumper" :total="total"/>
        </template>
      </template>
      <AppEmptyState v-else-if="!loadingFolders" title="暂无收藏夹" description="这里还没有可以查看的收藏夹"/>
    </main>
    <el-dialog v-model="editorVisible" :title="editingId ? '编辑收藏夹' : '新建收藏夹'" width="min(90vw, 500px)">
      <el-form label-width="80px">
        <el-form-item label="名称"><el-input v-model="form.name" maxlength="64"/></el-form-item>
        <el-form-item label="简介"><el-input v-model="form.description" maxlength="255" type="textarea"/></el-form-item>
        <el-form-item label="封面地址"><el-input v-model="form.coverUrl" maxlength="512"/></el-form-item>
        <el-form-item label="公开"><el-switch v-model="form.visibility" :active-value="1" :inactive-value="0"/></el-form-item>
      </el-form>
      <template #footer><el-button @click="editorVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveFolder">保存</el-button></template>
    </el-dialog>
    <el-dialog v-model="transferVisible" :title="transferOperation === 'copy' ? '复制视频至' : '移动视频至'"
      width="min(90vw, 500px)" :close-on-click-modal="!busy" :show-close="!busy">
      <p>已选择 {{ transferVideoIds.length }} 个视频，目标中已有的视频不会重复添加。</p>
      <el-select v-model="targetFolderId" placeholder="选择目标收藏夹" style="width: 100%" :disabled="busy">
        <el-option v-for="folder in folders.filter(item => item.id !== selectedId)" :key="folder.id"
          :label="folder.name" :value="folder.id"/>
      </el-select>
      <template #footer>
        <el-button :disabled="busy" @click="transferVisible = false">取消</el-button>
        <el-button type="primary" :loading="busy" :disabled="!targetFolderId" @click="transfer">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, reactive, ref, watch } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { useGlobalStore } from '@/store/store';
import { getCollectsClassify, getCollectByName, batchFolderVideos, clearInvalidFolderVideos } from '@/api/home';
import { userContentApi } from '@/api/product';
import homeVideoCard from '@/components/homeVideoCard.vue';
import loadingIndicator from '@/components/loadingIndicator.vue';

const store = useGlobalStore();
const homeUserId = parseInt(new URL(window.location).searchParams.get('userId')) || store.userId;
const isOwner = computed(() => homeUserId === store.userId);
const folders = ref([]);
const selectedId = ref(parseInt(new URL(window.location).searchParams.get('collectId')) || null);
const selected = computed(() => folders.value.find(folder => folder.id === selectedId.value));
const videos = ref([]);
const total = ref(0);
const sort = ref(1);
const scope = ref(1);
const keyword = ref('');
const appliedKeyword = ref('');
const pageNum = ref(1);
const loadingFolders = ref(true);
const loadingVideos = ref(false);
const editorVisible = ref(false);
const editingId = ref(null);
const saving = ref(false);
const busy = ref(false);
const batchMode = ref(false);
const selectedVideoIds = ref([]);
const allSelected = computed(() => videos.value.length > 0 && videos.value.every(video => selectedVideoIds.value.includes(video.id)));
const transferVisible = ref(false);
const transferOperation = ref('copy');
const transferVideoIds = ref([]);
const targetFolderId = ref(null);
const form = reactive({ name: '', description: '', coverUrl: '', visibility: 1 });
let requestId = 0;

async function loadFolders() {
  loadingFolders.value = true;
  try {
    const response = await getCollectsClassify(homeUserId);
    folders.value = response.data.data;
    store.setCollectClassifyNumber(folders.value.length);
    if (!selected.value) selectedId.value = folders.value[0]?.id || null;
  } catch (error) {
    ElMessage.error(error.message);
  } finally {
    loadingFolders.value = false;
    if (!selected.value) store.setHomeLoad(true, 'homeBody');
    store.setHomeLoadMenuList(5);
  }
}
watch(() => store.homeUserInformation.id, id => { if (id) loadFolders(); }, { immediate: true });
watch(() => store.homeMenu, menu => {
  if (menu !== 5) return;
  const id = parseInt(new URL(window.location).searchParams.get('collectId'));
  if (folders.value.some(folder => folder.id === id)) selectedId.value = id;
});
watch(selectedId, id => {
  if (!id || store.homeMenu !== 5) return;
  const url = new URL(window.location);
  url.searchParams.set('collectId', id);
  window.history.replaceState({}, '', url);
});

async function loadVideos() {
  if (!selected.value) return;
  const current = ++requestId;
  loadingVideos.value = true;
  try {
    const response = await getCollectByName(selected.value.name, homeUserId, store.userId,
      pageNum.value, scope.value, sort.value, appliedKeyword.value, selectedId.value);
    if (current !== requestId) return;
    videos.value = response.data.data.records;
    total.value = response.data.data.total;
  } catch (error) {
    if (current === requestId) ElMessage.error(error.message);
  } finally {
    if (current === requestId) {
      loadingVideos.value = false;
      store.setHomeLoad(true, 'homeBody');
    }
  }
}
watch([selectedId, scope, sort, appliedKeyword], () => { pageNum.value = 1; }, { flush: 'sync' });
watch([selectedId, scope, sort, pageNum, appliedKeyword], () => {
  selectedVideoIds.value = [];
  transferVisible.value = false;
}, { flush: 'sync' });
watch([selected, scope, sort, pageNum, appliedKeyword], loadVideos);

function search() { pageNum.value = 1; appliedKeyword.value = keyword.value.trim(); }
function selectAll(value) { selectedVideoIds.value = value ? videos.value.map(video => video.id) : []; }
function toggleVideo(id, value) {
  selectedVideoIds.value = value ? [...new Set([...selectedVideoIds.value, id])] : selectedVideoIds.value.filter(item => item !== id);
}
function openTransfer(operation, videoId) {
  if (folders.value.length < 2) { ElMessage.info('请先新建另一个收藏夹'); return; }
  transferOperation.value = operation;
  transferVideoIds.value = videoId ? [videoId] : [...selectedVideoIds.value];
  targetFolderId.value = null;
  transferVisible.value = true;
}
async function refreshAfterMutation() {
  selectedVideoIds.value = [];
  await loadFolders();
  await loadVideos();
  if (!videos.value.length && pageNum.value > 1) pageNum.value--;
}
async function transfer() {
  if (busy.value || !targetFolderId.value) return;
  busy.value = true;
  try {
    await batchFolderVideos(selectedId.value, transferOperation.value, transferVideoIds.value, targetFolderId.value);
    transferVisible.value = false;
    ElMessage.success(transferOperation.value === 'copy' ? '复制完成' : '移动完成');
    await refreshAfterMutation();
  } catch (error) { ElMessage.error(error.message); }
  finally { busy.value = false; }
}
async function deleteSelected() {
  const confirmed = await ElMessageBox.confirm(`确定从当前收藏夹移除所选 ${selectedVideoIds.value.length} 个视频？`, '批量删除',
    { type: 'warning' }).then(() => true, () => false);
  if (!confirmed || busy.value) return;
  busy.value = true;
  try {
    await batchFolderVideos(selectedId.value, 'delete', selectedVideoIds.value);
    await refreshAfterMutation();
    ElMessage.success('已移出收藏夹');
  } catch (error) { ElMessage.error(error.message); }
  finally { busy.value = false; }
}
async function clearInvalid() {
  const confirmed = await ElMessageBox.confirm('确定清除当前收藏夹中已删除、下架或作者已注销的视频？', '清除失效内容',
    { type: 'warning' }).then(() => true, () => false);
  if (!confirmed || busy.value) return;
  busy.value = true;
  try {
    const count = await clearInvalidFolderVideos(selectedId.value);
    await refreshAfterMutation();
    ElMessage.success(`已清除 ${count} 条失效内容`);
  } catch (error) { ElMessage.error(error.message); }
  finally { busy.value = false; }
}
function openEditor(folder) {
  editingId.value = folder?.id || null;
  Object.assign(form, { name: folder?.name || '', description: folder?.description || '',
    coverUrl: folder?.coverUrl || '', visibility: folder?.visibility ?? 1 });
  editorVisible.value = true;
}
async function saveFolder() {
  if (!form.name.trim()) { ElMessage.warning('请输入收藏夹名称'); return; }
  saving.value = true;
  try {
    const body = { ...form, name: form.name.trim() };
    if (editingId.value) await userContentApi.renameFolder(editingId.value, body);
    else {
      const folder = await userContentApi.createFolder(body);
      selectedId.value = folder.id;
    }
    editorVisible.value = false;
    await loadFolders();
    await loadVideos();
  } catch (error) {
    ElMessage.error(error.message);
  } finally {
    saving.value = false;
  }
}
async function removeFolder() {
  const confirmed = await ElMessageBox.confirm('删除后，视频将移入默认收藏夹。是否继续？', '删除收藏夹',
    { type: 'warning' }).then(() => true, () => false);
  if (!confirmed) return;
  try {
    await userContentApi.deleteFolder(selectedId.value);
    await loadFolders();
  } catch (error) { ElMessage.error(error.message); }
}
async function removeVideo(videoId) {
  busy.value = true;
  try {
    await batchFolderVideos(selectedId.value, 'delete', [videoId]);
    await refreshAfterMutation();
  } catch (error) { ElMessage.error(error.message); }
  finally { busy.value = false; }
}
</script>

<style scoped>
.collections { box-sizing: border-box; display: flex; gap: var(--gap-6); padding: var(--gap-5) var(--page-pad); }
aside { width: 200px; display: flex; flex-direction: column; gap: 12px; flex-shrink: 0; }
aside button { text-align: left; border: 0; border-radius: 6px; padding: 12px; cursor: pointer; }
aside .active { background: #0FA68E; color: white; }
main { flex: 1; min-width: 0; }
.batch-tools, .video-actions { display: flex; align-items: center; flex-wrap: wrap; gap: 8px; margin-bottom: 12px; }
.batch-tools { padding: 12px; background: #EFF3F2; border-radius: 8px; }
.video-actions .el-button { margin-left: 0; }
header { display: flex; align-items: center; gap: 16px; flex-wrap: wrap; margin-bottom: 24px; }
header > img { width: 160px; border-radius: 8px; }
header > div { flex: 1; min-width: 0; overflow-wrap: anywhere; }
header h2 { margin: 0; }
header p, header span { color: #8D9794; font-size: 13px; }
.tools { display: flex; flex-wrap: wrap; gap: 12px; margin-bottom: 24px; }
.tools .el-input { width: 240px; }
.videos { display: grid; grid-template-columns: repeat(auto-fill, minmax(min(100%, 193px), 1fr)); gap: 16px; }
.no-data { padding: 60px; text-align: center; color: #8D9794; }
.el-pagination { margin-top: 24px; }
@media (max-width: 720px) {
  .collections { flex-direction: column; padding: var(--gap-4) var(--page-pad); }
  aside { width: 100%; }
  aside button { overflow-wrap: anywhere; }
  .tools > .el-input { width: 100%; }
  .no-data { padding: 40px 12px; }
}
</style>
