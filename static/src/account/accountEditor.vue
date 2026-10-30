<template>
  <div class="editor">
    <loadingIndicator v-if="loading"/>
    <template v-else-if="profile">
      <template v-if="store.accountMenu === 1">
        <h2 class="panel-heading">我的信息</h2>
        <el-form class="profile-form" label-width="115px">
          <el-form-item label="昵称"><el-input v-model="form.nickname" maxlength="20"/></el-form-item>
          <el-form-item label="我的签名"><el-input v-model="form.signature" type="textarea" :rows="4" maxlength="250" placeholder="请输入个人签名"/></el-form-item>
          <el-form-item label="性别"><el-radio-group v-model="form.gender">
            <el-radio-button :value="0">保密</el-radio-button><el-radio-button :value="1">男</el-radio-button><el-radio-button :value="2">女</el-radio-button>
          </el-radio-group></el-form-item>
          <el-form-item label="出生日期"><el-date-picker v-model="form.birthday" type="date" value-format="YYYY-MM-DD" popper-class="account-date-picker"/></el-form-item>
        </el-form>
        <div class="save-actions"><el-button type="primary" :loading="saving" @click="save">保存</el-button></div>
      </template>
      <section v-else class="media-panel">
        <h2 class="panel-heading">{{ store.accountMenu === 2 ? '我的头像' : '我的背景' }}</h2>
        <div class="media-content">
          <p v-if="store.accountMenu !== 2" class="preview-label">当前背景</p>
          <div class="media-preview" :class="{ 'avatar-frame': store.accountMenu === 2 }">
            <img v-if="store.accountMenu === 2 ? profile.avatarUrl : profile.backgroundUrl"
                 class="preview" :class="{ avatar: store.accountMenu === 2 }"
                 :src="store.accountMenu === 2 ? profile.avatarUrl : profile.backgroundUrl"
                 :alt="store.accountMenu === 2 ? '当前头像' : '当前背景'">
            <span v-else class="media-empty">{{ store.accountMenu === 2 ? '暂无头像' : '尚未设置背景' }}</span>
            <label class="upload-control" :class="{ disabled: saving }">
              <span>{{ saving ? '上传中' : store.accountMenu === 2 ? '更换头像' : '更换背景' }}</span>
              <input class="upload-file" type="file" accept="image/png,image/jpeg,image/gif"
                     :aria-label="store.accountMenu === 2 ? '选择头像图片' : '选择背景图片'"
                     :disabled="saving" @change="upload">
            </label>
          </div>
          <p class="upload-hint">支持 PNG、JPEG、GIF，最大 10MB。</p>
        </div>
      </section>
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
.editor { --el-color-primary: var(--brand); --el-color-primary-light-3: var(--brand-light-3); --el-color-primary-dark-2: var(--brand-hover); display: flex; flex-direction: column; width: 100%; min-width: 0; min-height: 500px; box-sizing: border-box; color: var(--ink); }
.editor *, .editor *::before, .editor *::after { box-sizing: border-box; }
.panel-heading { display: flex; flex: none; align-items: center; gap: 5px; min-height: 50px; margin: 0; padding: 12px 30px; border-bottom: 1px solid #ddd; color: var(--brand); font-size: 14px; line-height: 25px; font-weight: 400; }
.panel-heading::before { content: ""; flex: none; width: 4px; height: 16px; border-radius: 4px; background: var(--brand); }
.profile-form { padding: 20px 20px 0; }
.profile-form :deep(.el-form-item) { margin-bottom: 22px; }
.profile-form :deep(.el-form-item__label) { height: 30px; padding-right: 20px; line-height: 30px; color: #606266; }
.profile-form :deep(.el-form-item__content) { min-width: 0; min-height: 30px; line-height: 30px; }
.profile-form :deep(.el-input) { width: 100%; max-width: 225px; }
.profile-form :deep(.el-input__wrapper) { min-height: 30px; padding: 0 15px; border-radius: 4px; background: #fff; }
.profile-form :deep(.el-input__inner) { height: 30px; line-height: 30px; color: #606266; font-size: 14px; }
.profile-form :deep(.el-textarea) { width: 100%; max-width: 618px; }
.profile-form :deep(.el-textarea__inner) { min-height: 88px; padding: 5px 15px; border-radius: 4px; background: #fff; line-height: 1.5; }
.profile-form :deep(.el-radio-group) { display: flex; flex-wrap: wrap; gap: 8px 20px; }
.profile-form :deep(.el-radio-button) { --el-radio-button-checked-bg-color: var(--brand); --el-radio-button-checked-border-color: var(--brand); }
.profile-form :deep(.el-radio-button .el-radio-button__inner) { padding: 5px 8px; border: 1px solid #bfcbd9; border-radius: 5px; background: #f4f4f4; color: #717171; font-size: 14px; line-height: 18px; }
.profile-form :deep(.el-radio-button.is-active .el-radio-button__inner) { border-color: var(--brand); background: var(--brand); color: #fff; box-shadow: none; }
.save-actions { margin-top: auto; padding: 20px 20px 30px; text-align: center; }
.save-actions .el-button { width: 110px; min-height: 40px; border-radius: 4px; font-size: 14px; }
.media-panel { min-width: 0; }
.media-content { padding: 20px 40px; }
.preview-label { margin: 0 0 14px; color: #8c959c; font-size: 14px; }
.media-preview { position: relative; min-width: 0; }
.preview { display: block; width: 100%; max-width: 100%; aspect-ratio: 6.4 / 1; object-fit: cover; }
.media-empty { display: grid; place-items: center; width: 100%; aspect-ratio: 6.4 / 1; border: 1px solid #dcdfe6; background: #f4f4f4; color: #8c959c; font-size: 12px; }
.avatar-frame { width: 200px; height: 200px; margin: 0 auto; border: 1px solid #abaeaf; border-radius: 50%; }
.avatar-frame .avatar, .avatar-frame .media-empty { position: absolute; top: 50%; left: 50%; width: 96px; height: 96px; border-radius: 50%; transform: translate(-50%, -50%); }
.upload-control { position: relative; display: flex; justify-content: center; align-items: center; width: 140px; min-height: 40px; max-width: 100%; margin: 20px auto 0; padding: 8px; border: 1px solid var(--brand); border-radius: 4px; background: var(--brand); color: #fff; font-size: 14px; line-height: 20px; text-align: center; cursor: pointer; }
.upload-control:hover { border-color: var(--brand-hover); background: var(--brand-hover); }
.upload-control.disabled { opacity: .6; cursor: wait; }
.upload-file { position: absolute; width: 1px; height: 1px; overflow: hidden; clip-path: inset(50%); white-space: nowrap; }
.avatar-frame .upload-control { position: absolute; top: 50%; left: -27px; width: 54px; min-height: 54px; margin: 0; padding: 6px; border-radius: 50%; transform: translateY(-50%); }
.avatar-frame .upload-control > span { width: 2em; line-height: 18px; }
.upload-hint { margin: 16px 0 0; color: #8c959c; font-size: 12px; line-height: 1.6; text-align: center; }
.editor > .el-button { align-self: center; margin: auto; }
:global(.account-date-picker) { --el-color-primary: var(--brand); --el-color-primary-light-9: var(--brand-light-9); box-sizing: border-box; width: min(322px, calc(100vw - 24px)); }
:global(.account-date-picker .el-picker-panel) { width: 100%; }
@media (max-width: 900px) {
  .panel-heading { padding-right: 20px; padding-left: 20px; }
}
@media (max-width: 520px) {
  .panel-heading { padding-right: 16px; padding-left: 16px; }
  .profile-form { padding: 20px 16px 0; }
  .profile-form :deep(.el-form-item) { display: block; }
  .profile-form :deep(.el-form-item__label) { justify-content: flex-start; width: 100% !important; height: auto; padding: 0 0 8px; line-height: 1.5; }
  .profile-form :deep(.el-form-item__content) { margin-left: 0 !important; }
  .profile-form :deep(.el-input) { max-width: none; }
  .profile-form :deep(.el-input__wrapper), .profile-form :deep(.el-input__inner) { min-height: 38px; }
  .media-content { padding: 20px 16px; }
  .avatar-frame { width: min(180px, calc(100% - 54px)); height: auto; aspect-ratio: 1; }
}
</style>
