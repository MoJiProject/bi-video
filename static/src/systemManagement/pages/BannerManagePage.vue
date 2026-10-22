<template>
  <div class="banner-page">
    <div class="sys-panel">
      <div class="sys-panel-title">
        <span class="title">Banner管理
          <span class="title-tip">首页最多展示6张，排序数字越小越靠前，相同排序按新增顺序倒序</span>
        </span>
      </div>
      <div class="sys-filter">
        <el-input v-model="query.keyword" class="keyword-input" placeholder="搜索Banner内容" maxlength="200"
          clearable @keyup.enter="handleSearch" @clear="handleSearch" />
        <el-button type="primary" @click="handleSearch">查询</el-button>
        <el-button @click="handleReset">重置</el-button>
        <el-button type="success" @click="openEdit()">新增Banner</el-button>
      </div>
      <el-table v-loading="loading" :data="banners" class="sys-table" row-key="id">
        <el-table-column label="图片" width="180" align="center">
          <template #default="{ row }">
            <el-image class="banner-preview" :src="row.imageUrl" fit="cover"
              :preview-src-list="[row.imageUrl]" preview-teleported />
          </template>
        </el-table-column>
        <el-table-column label="内容" min-width="180" show-overflow-tooltip>
          <template #default="{ row }">{{ row.content || "—" }}</template>
        </el-table-column>
        <el-table-column label="链接" min-width="220" show-overflow-tooltip>
          <template #default="{ row }">
            <a v-if="row.linkUrl" :href="row.linkUrl" target="_blank" rel="noopener noreferrer">{{ row.linkUrl }}</a>
            <span v-else>—</span>
          </template>
        </el-table-column>
        <el-table-column prop="sortOrder" label="排序" width="80" align="center" />
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.enabled ? 'success' : 'info'">{{ row.enabled ? "展示" : "隐藏" }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="更新时间" width="160" align="center">
          <template #default="{ row }">{{ shortTime(row.updatedAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right" align="center" class-name="sys-actions">
          <template #default="{ row }">
            <el-button size="small" type="success" @click="openEdit(row)">编辑</el-button>
            <el-button size="small" type="danger" :loading="deletingId === row.id" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
        <template #empty><div class="sys-empty">暂无Banner，点击“新增Banner”添加</div></template>
      </el-table>
      <SystemPagination :total="total" :page-size="10" :current-page="query.pageNum" @current-change="handlePageChange" />
    </div>
    <el-dialog class="sys-dialog" v-model="dialogVisible" :title="editId ? '编辑Banner' : '新增Banner'"
      width="min(620px, calc(100vw - 24px))" align-center :close-on-click-modal="false"
      :show-close="!submitting && !uploading" :close-on-press-escape="!submitting && !uploading">
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item label="Banner图片" prop="imageUrl">
          <div class="image-field">
            <el-image v-if="form.imageUrl" class="edit-preview" :src="form.imageUrl" fit="contain" />
            <el-upload action="" accept="image/jpeg,image/png,image/gif" :show-file-list="false"
              :disabled="uploading || submitting" :http-request="uploadImage">
              <el-button :loading="uploading" :disabled="submitting">{{ form.imageUrl ? "更换图片" : "上传图片" }}</el-button>
            </el-upload>
            <span class="field-tip">支持JPG、PNG、GIF，最大10MB；建议使用横向大图。</span>
          </div>
        </el-form-item>
        <el-form-item label="内容（选填）">
          <el-input v-model="form.content" maxlength="200" show-word-limit placeholder="有内容时显示在首页图片内部底部" />
        </el-form-item>
        <el-form-item label="链接" prop="linkUrl" :required="!!form.content.trim()">
          <el-input v-model="form.linkUrl" maxlength="2000" placeholder="https://example.com 或 /video?videoId=..." />
          <span class="field-tip">填写内容时链接必填；有链接的Banner支持点击跳转。</span>
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="form.sortOrder" :min="0" :max="999999" :precision="0" controls-position="right" />
        </el-form-item>
        <el-form-item label="首页展示">
          <el-switch v-model="form.enabled" active-text="展示" inactive-text="隐藏" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button :disabled="submitting || uploading" @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" :disabled="uploading" @click="submit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { nextTick, onMounted, reactive, ref } from "vue";
import { deleteBanner, saveBanner, searchBanners, uploadBannerImage } from "../../api/banner";
import SystemPagination from "../components/SystemPagination.vue";
import { confirmDanger, shortTime, toast } from "../systemCommon";

const banners = ref([]);
const total = ref(0);
const loading = ref(false);
const submitting = ref(false);
const uploading = ref(false);
const deletingId = ref(null);
const query = reactive({ keyword: "", pageNum: 1 });
const dialogVisible = ref(false);
const editId = ref(null);
const formRef = ref(null);
const form = reactive({ imageUrl: "", content: "", linkUrl: "", sortOrder: 0, enabled: true });
const rules = {
  imageUrl: [{ required: true, message: "请上传Banner图片", trigger: "change" }],
  linkUrl: [{ validator: validateLink, trigger: "blur" }],
  sortOrder: [{ required: true, type: "number", min: 0, max: 999999, message: "请填写有效排序", trigger: "change" }],
};
let generation = 0;

function validateLink(rule, value, callback) {
  const link = value.trim();
  if (!link) {
    callback(form.content.trim() ? new Error("填写内容时必须填写链接") : undefined);
    return;
  }
  const hasControl = Array.from(link).some(char => {
    const code = char.charCodeAt(0);
    return code < 32 || (code >= 127 && code <= 159);
  });
  if (/[\\\s]/.test(link) || hasControl || /%(?![0-9a-f]{2})/i.test(link)) {
    callback(new Error("链接格式无效"));
    return;
  }
  try {
    const url = new URL(link, window.location.origin);
    const relative = link.startsWith("/") && !link.startsWith("//");
    const absolute = /^https?:\/\//i.test(link) && !url.username && !url.password;
    callback(relative || absolute ? undefined : new Error("请输入站内路径或HTTP/HTTPS链接"));
  } catch {
    callback(new Error("链接格式无效"));
  }
}

async function loadBanners() {
  const current = ++generation;
  loading.value = true;
  try {
    const data = await searchBanners({ ...query });
    if (current === generation) {
      banners.value = data.records;
      total.value = data.total;
    }
  } catch (error) {
    if (current === generation) toast(error.message || "加载Banner失败", "error");
  } finally {
    if (current === generation) loading.value = false;
  }
}

function handleSearch() { query.pageNum = 1; loadBanners(); }
function handleReset() { query.keyword = ""; handleSearch(); }
function handlePageChange(page) { query.pageNum = page; loadBanners(); }

async function openEdit(row) {
  editId.value = row ? row.id : null;
  Object.assign(form, row ? {
    imageUrl: row.imageUrl, content: row.content || "", linkUrl: row.linkUrl || "",
    sortOrder: row.sortOrder, enabled: row.enabled,
  } : { imageUrl: "", content: "", linkUrl: "", sortOrder: 0, enabled: true });
  dialogVisible.value = true;
  await nextTick();
  formRef.value.clearValidate();
}

async function uploadImage(options) {
  const file = options.file;
  if (!["image/jpeg", "image/png", "image/gif"].includes(file.type) || file.size > 10 * 1024 * 1024 || !file.size) {
    const error = new Error("请选择不超过10MB的JPG、PNG或GIF图片");
    toast(error.message, "error");
    options.onError(error);
    return;
  }
  uploading.value = true;
  try {
    const result = await uploadBannerImage(file);
    form.imageUrl = result.url;
    formRef.value.clearValidate("imageUrl");
    options.onSuccess(result);
  } catch (error) {
    toast(error.message || "图片上传失败", "error");
    options.onError(error);
  } finally {
    uploading.value = false;
  }
}

async function submit() {
  if (submitting.value || uploading.value) return;
  const valid = await formRef.value.validate().then(() => true, () => false);
  if (!valid) return;
  submitting.value = true;
  try {
    await saveBanner(editId.value, { ...form, content: form.content.trim(), linkUrl: form.linkUrl.trim() });
    toast("保存成功", "success");
    dialogVisible.value = false;
    await loadBanners();
  } catch (error) {
    toast(error.message || "保存Banner失败", "error");
  } finally {
    submitting.value = false;
  }
}

async function handleDelete(row) {
  if (deletingId.value !== null) return;
  const confirmed = await confirmDanger("确定删除这张Banner？删除后首页不再展示。").then(() => true, () => false);
  if (!confirmed) return;
  deletingId.value = row.id;
  try {
    await deleteBanner(row.id);
    toast("删除成功", "success");
    if (banners.value.length === 1 && query.pageNum > 1) query.pageNum--;
    await loadBanners();
  } catch (error) {
    toast(error.message || "删除Banner失败", "error");
  } finally {
    deletingId.value = null;
  }
}

onMounted(loadBanners);
</script>

<style scoped lang="scss">
@use "../systemPanel.scss" as *;
.keyword-input { width: 260px; max-width: 100%; }
.banner-preview { width: 144px; height: 90px; border-radius: 6px; }
.image-field { display: flex; flex-direction: column; align-items: flex-start; gap: 12px; width: 100%; }
.edit-preview { width: 100%; height: 200px; background: $sys-bg; border-radius: 6px; }
.field-tip { display: block; color: $sys-sub; font-size: 12px; line-height: 1.6; }
.banner-page a { color: $sys-primary; text-decoration: none; overflow-wrap: anywhere; }
@media (max-width: 640px) {
  .sys-panel { padding: 16px 12px; }
  .title-tip { display: block; margin: 8px 0 0; line-height: 1.6; }
}
</style>
