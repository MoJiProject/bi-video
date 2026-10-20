<template>
  <div class="editor">
    <loadingIndicator v-if="loading"/>
    <template v-else-if="profile">
      <el-form v-if="store.accountMenu === 1" label-width="90px">
        <el-form-item label="昵称"><el-input v-model="form.nickname" maxlength="20"/></el-form-item>
        <el-form-item label="签名"><el-input v-model="form.signature" type="textarea" maxlength="250"/></el-form-item>
        <el-form-item label="性别"><el-radio-group v-model="form.gender">
          <el-radio :value="0">保密</el-radio><el-radio :value="1">男</el-radio><el-radio :value="2">女</el-radio>
        </el-radio-group></el-form-item>
        <el-form-item label="生日"><el-date-picker v-model="form.birthday" type="date" value-format="YYYY-MM-DD"/></el-form-item>
        <el-form-item><el-button type="primary" :loading="saving" @click="save">保存</el-button></el-form-item>
      </el-form>
      <div v-else>
        <h2>{{ store.accountMenu === 2 ? '头像' : '空间背景' }}</h2>
        <img class="preview" :class="{ avatar: store.accountMenu === 2 }"
          :src="store.accountMenu === 2 ? profile.avatarUrl : profile.backgroundUrl" alt="">
        <input class="upload-file" type="file" accept="image/png,image/jpeg,image/gif" :disabled="saving" @change="upload">
        <p>支持 PNG、JPEG、GIF，最大 10MB。</p>
      </div>
    </template>
    <el-button v-else @click="load">重新加载</el-button>
  </div>
</template>
<script setup>
import { onMounted, reactive, ref } from 'vue';
import { ElMessage } from 'element-plus';
import { authApi } from '@/api/product';
import { post, put } from '@/services/http';
import { useGlobalStore } from '@/store/store';
import { selectUserInfo } from '@/api/home/user';
import loadingIndicator from '@/components/loadingIndicator.vue';
const store = useGlobalStore();
const profile = ref(null);
const loading = ref(true);
const saving = ref(false);
const form = reactive({ nickname: '', signature: '', gender: 0, birthday: null });
async function load() {
  loading.value = true;
  try {
    profile.value = await authApi.me();
    store.setUserId(profile.value.userId);
    Object.assign(form, { nickname: profile.value.nickname, signature: profile.value.signature || '',
      gender: profile.value.gender || 0, birthday: profile.value.birthday || null });
  } catch (error) { ElMessage.error(error.message); }
  finally { loading.value = false; }
}
async function refreshStore() {
  const response = await selectUserInfo(profile.value.userId);
  store.setUserInformation(response.data.data);
  if (store.homeUserInformation.id === profile.value.userId) store.setHomeUserInformation(response.data.data);
}
async function save() {
  if (!form.nickname.trim()) { ElMessage.warning('请输入昵称'); return; }
  saving.value = true;
  try {
    await put('/auth/account', form);
    await load();
    await refreshStore();
    ElMessage.success('保存成功');
  } catch (error) { ElMessage.error(error.message); }
  finally { saving.value = false; }
}
async function upload(event) {
  const file = event.target.files[0];
  if (!file) return;
  saving.value = true;
  try {
    const body = new FormData();
    body.append('file', file);
    await post(`/media/${store.accountMenu === 2 ? 'avatar' : 'background'}`, body);
    await load();
    await refreshStore();
    ElMessage.success('上传成功');
  } catch (error) { ElMessage.error(error.message); }
  finally { saving.value = false; event.target.value = ''; }
}
onMounted(load);
</script>
<style scoped>
.editor { padding: 32px; width: 100%; box-sizing: border-box; }
.editor > .el-form { max-width: 870px; }
.upload-file { display: block; max-width: 100%; }
.editor :deep(.el-form-item__content) { min-width: 0; }
.editor :deep(.el-date-editor.el-input) { width: 100%; max-width: 260px; }
.editor :deep(.el-radio-group) { display: flex; flex-wrap: wrap; gap: 8px 16px; }
.editor :deep(.el-radio) { margin-right: 0; }
.preview { display: block; max-width: 100%; width: 600px; max-height: 300px; object-fit: cover; margin: 20px 0; }
.avatar { width: 160px; height: 160px; border-radius: 50%; }
p { color: #8D9794; font-size: 13px; }
@media (max-width: 520px) {
  .editor { padding: 24px 16px; }
  .editor :deep(.el-form-item) { display: block; }
  .editor :deep(.el-form-item__label) { justify-content: flex-start; width: 100% !important; height: auto; padding: 0 0 8px; line-height: 1.5; }
  .editor :deep(.el-form-item__content) { margin-left: 0 !important; }
  .editor :deep(.el-input__wrapper) { min-height: 38px; }
  .preview { height: auto; }
  .avatar { width: 128px; height: 128px; }
}
</style>
