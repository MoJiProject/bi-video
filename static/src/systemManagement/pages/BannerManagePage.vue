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
              <el-button size="small" @click="$exportCsv('banners', banners)">导出 CSV</el-button>
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
            <div class="image-actions">
              <el-upload action="" accept="image/jpeg,image/png,image/gif" :show-file-list="false"
                :disabled="uploading || submitting || cropDialogVisible || !!pendingUploadOptions" :http-request="uploadImage">
                <el-button :loading="uploading" :disabled="submitting">{{ form.imageUrl ? "更换图片" : "上传图片" }}</el-button>
              </el-upload>
              <el-button v-if="form.imageUrl" :disabled="uploading || submitting || cropDialogVisible || !!pendingUploadOptions"
                @click="openExistingImageCrop">裁剪</el-button>
            </div>
            <span class="field-tip">支持JPG、PNG、GIF，最大10MB；上传后可拖动图片选取首页显示范围（24:5），建议使用清晰的大图。GIF裁剪后会转为静态图片。</span>
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
    <el-dialog v-model="cropDialogVisible" class="sys-dialog banner-crop-dialog" title="选择Banner显示范围"
      width="min(640px, calc(100vw - 24px))" align-center :close-on-click-modal="false"
      :show-close="!uploading" :close-on-press-escape="!uploading" @closed="handleCropClosed">
      <div class="crop-instructions">拖动图片调整显示位置，使用滑块缩放。框内区域会按首页横幅比例裁剪；GIF将转为静态图片。</div>
      <div ref="cropViewportRef" class="crop-viewport" @pointerdown="startCropDrag"
        @pointermove="moveCropDrag" @pointerup="endCropDrag" @pointercancel="endCropDrag">
        <img ref="cropImageRef" :src="cropImageSource" :style="cropImageStyle" alt="Banner裁剪预览" draggable="false" />
      </div>
      <div class="crop-controls">
        <span>缩放</span>
        <input class="crop-zoom-slider" type="range" min="1" :max="cropMaxZoom" step="0.01"
          :value="cropZoom" :disabled="cropMaxZoom <= 1" @input="setCropZoom(Number($event.target.value))" />
      </div>
      <p v-if="!cropHasEnoughResolution" class="crop-warning">
        图片分辨率不足以满足横幅清晰度要求，请选择更大分辨率的图片。
      </p>
      <template #footer>
        <el-button :disabled="uploading" @click="cropDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="uploading" :disabled="!cropHasEnoughResolution"
          @click="confirmCrop">裁剪并上传</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, nextTick, onMounted, onUnmounted, reactive, ref } from "vue";
import { ElMessage } from "element-plus";
import { deleteBanner, saveBanner, searchBanners, uploadBannerImage } from "../../api/banner";
import SystemPagination from "../components/SystemPagination.vue";
import { confirmDanger, shortTime } from "../systemCommon";

const banners = ref([]);
const total = ref(0);
const loading = ref(false);
const submitting = ref(false);
const uploading = ref(false);
const deletingId = ref(null);
const query = reactive({ keyword: "", pageNum: 1 });
const dialogVisible = ref(false);
const cropDialogVisible = ref(false);
const editId = ref(null);
const formRef = ref(null);
const cropViewportRef = ref(null);
const cropImageRef = ref(null);
const cropImageSource = ref("");
const cropImageSize = reactive({ width: 0, height: 0 });
const cropViewportWidth = ref(0);
const cropViewportHeight = ref(0);
const cropZoom = ref(1);
const cropOffset = reactive({ x: 0, y: 0 });
let pendingUploadOptions = null;
let pendingOriginalImageUrl = "";
let cropOutputType = "image/jpeg";
let dragStart = null;
const form = reactive({
  imageUrl: "", originalImageUrl: "", content: "", linkUrl: "", sortOrder: 0, enabled: true,
});
const rules = {
  imageUrl: [{ required: true, message: "请上传Banner图片", trigger: "change" }],
  linkUrl: [{ validator: validateLink, trigger: "blur" }],
  sortOrder: [{ required: true, type: "number", min: 0, max: 999999, message: "请填写有效排序", trigger: "change" }],
};
let generation = 0;
const cropScale = computed(() => {
  if (!cropImageSize.width || !cropImageSize.height || !cropViewportWidth.value || !cropViewportHeight.value) return 1;
  return Math.max(
    cropViewportWidth.value / cropImageSize.width,
    cropViewportHeight.value / cropImageSize.height,
  );
});
const cropImageWidth = computed(() => cropImageSize.width * cropScale.value * cropZoom.value);
const cropImageHeight = computed(() => cropImageSize.height * cropScale.value * cropZoom.value);
const cropSourceRect = computed(() => {
  if (!cropImageWidth.value || !cropImageHeight.value || !cropViewportWidth.value) {
    return { width: 0, height: 0 };
  }
  const scale = cropScale.value * cropZoom.value;
  return {
    x: Math.max(0, -cropOffset.x / scale),
    y: Math.max(0, -cropOffset.y / scale),
    width: cropViewportWidth.value / scale,
    height: cropViewportHeight.value / scale,
  };
});
const cropHasEnoughResolution = computed(() =>
  cropSourceRect.value.width >= 1280 && cropSourceRect.value.height >= 267,
);
const cropMaxZoom = computed(() => {
  if (!cropImageSize.width || !cropImageSize.height || !cropViewportWidth.value) return 1;
  const scale = cropScale.value;
  const widthAtMinimumZoom = cropViewportWidth.value / scale;
  const heightAtMinimumZoom = cropViewportHeight.value / scale;
  return Math.max(1, Math.min(3, widthAtMinimumZoom / 1280, heightAtMinimumZoom / 267));
});
const cropImageStyle = computed(() => ({
  left: `${cropOffset.x}px`,
  top: `${cropOffset.y}px`,
  width: `${cropImageWidth.value}px`,
  height: `${cropImageHeight.value}px`,
}));

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
    if (current === generation) ElMessage.error(error.message || "加载Banner失败");
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
    imageUrl: row.imageUrl, originalImageUrl: row.originalImageUrl || row.imageUrl,
    content: row.content || "", linkUrl: row.linkUrl || "",
    sortOrder: row.sortOrder, enabled: row.enabled,
  } : { imageUrl: "", originalImageUrl: "", content: "", linkUrl: "", sortOrder: 0, enabled: true });
  dialogVisible.value = true;
  await nextTick();
  formRef.value.clearValidate();
}

async function uploadImage(options) {
  const file = options.file;
  if (!["image/jpeg", "image/png", "image/gif"].includes(file.type) || file.size > 10 * 1024 * 1024 || !file.size) {
    const error = new Error("请选择不超过10MB的JPG、PNG或GIF图片");
    ElMessage.error(error.message);
    options.onError(error);
    return;
  }
  pendingUploadOptions = options;
  pendingOriginalImageUrl = "";
  uploading.value = true;
  cropOutputType = file.type === "image/jpeg" ? "image/jpeg" : "image/png";
  try {
    const result = await uploadBannerImage(file);
    pendingOriginalImageUrl = result.url;
    cropOutputType = file.type === "image/jpeg" ? "image/jpeg" : "image/png";
    await loadCropImage(result.url);
  } catch (error) {
    ElMessage.error(error.message || "原图上传失败");
    pendingUploadOptions = null;
    pendingOriginalImageUrl = "";
    options.onError(error);
  } finally {
    uploading.value = false;
  }
}

async function openExistingImageCrop() {
  if (!form.imageUrl || uploading.value || submitting.value) return;
  pendingUploadOptions = null;
  pendingOriginalImageUrl = form.originalImageUrl || form.imageUrl;
  cropOutputType = /\.jpe?g(?:$|[?#])/i.test(pendingOriginalImageUrl) ? "image/jpeg" : "image/png";
  await loadCropImage(pendingOriginalImageUrl);
}

async function loadCropImage(source) {
  try {
    const image = new Image();
    await new Promise((resolve, reject) => {
      image.onload = resolve;
      image.onerror = () => reject(new Error("无法读取图片，请重新选择"));
      image.src = source;
    });
    cropImageSize.width = image.naturalWidth;
    cropImageSize.height = image.naturalHeight;
    cropImageSource.value = source;
    cropZoom.value = 1;
    cropOffset.x = 0;
    cropOffset.y = 0;
    cropDialogVisible.value = true;
    await nextTick();
    updateCropViewport();
    centerCropImage();
  } catch (error) {
    ElMessage.error(error.message || "无法读取图片");
    cleanupCrop();
    if (pendingUploadOptions) pendingUploadOptions.onError(error);
    pendingUploadOptions = null;
  }
}

function updateCropViewport() {
  if (!cropViewportRef.value) return;
  const { width, height } = cropViewportRef.value.getBoundingClientRect();
  cropViewportWidth.value = width;
  cropViewportHeight.value = height;
  clampCropOffset();
}

function clampCropOffset() {
  cropOffset.x = Math.min(0, Math.max(cropViewportWidth.value - cropImageWidth.value, cropOffset.x));
  cropOffset.y = Math.min(0, Math.max(cropViewportHeight.value - cropImageHeight.value, cropOffset.y));
}

function centerCropImage() {
  cropOffset.x = (cropViewportWidth.value - cropImageWidth.value) / 2;
  cropOffset.y = (cropViewportHeight.value - cropImageHeight.value) / 2;
}

function startCropDrag(event) {
  if (uploading.value) return;
  dragStart = { x: event.clientX, y: event.clientY, offsetX: cropOffset.x, offsetY: cropOffset.y };
  event.currentTarget.setPointerCapture(event.pointerId);
}

function moveCropDrag(event) {
  if (!dragStart) return;
  cropOffset.x = dragStart.offsetX + event.clientX - dragStart.x;
  cropOffset.y = dragStart.offsetY + event.clientY - dragStart.y;
  clampCropOffset();
}

function endCropDrag() {
  dragStart = null;
}

function setCropZoom(value) {
  const focusX = (cropViewportWidth.value / 2 - cropOffset.x) / cropImageWidth.value;
  const focusY = (cropViewportHeight.value / 2 - cropOffset.y) / cropImageHeight.value;
  cropZoom.value = value;
  cropOffset.x = cropViewportWidth.value / 2 - focusX * cropImageWidth.value;
  cropOffset.y = cropViewportHeight.value / 2 - focusY * cropImageHeight.value;
  clampCropOffset();
}

async function createCroppedImage() {
  const { x, y, width, height } = cropSourceRect.value;
  const canvas = document.createElement("canvas");
  canvas.width = 1920;
  canvas.height = 400;
  const context = canvas.getContext("2d");
  const image = cropImageRef.value;
  if (!context || !image) throw new Error("无法创建裁剪图片");
  if (!image.complete) {
    await new Promise((resolve, reject) => {
      image.onload = resolve;
      image.onerror = () => reject(new Error("无法读取裁剪图片"));
    });
  }
  if (!image.naturalWidth) throw new Error("无法读取裁剪图片");
  context.drawImage(image, x, y, width, height, 0, 0, canvas.width, canvas.height);
  const type = cropOutputType;
  return new Promise((resolve, reject) => {
    canvas.toBlob(blob => {
      if (!blob) {
        reject(new Error("图片裁剪失败，请重试"));
        return;
      }
      const extension = type === "image/jpeg" ? "jpg" : "png";
      resolve(new File([blob], `banner-cropped.${extension}`, { type }));
    }, type, 0.92);
  });
}

function cleanupCrop() {
  cropImageSource.value = "";
  cropImageSize.width = 0;
  cropImageSize.height = 0;
  cropViewportWidth.value = 0;
  cropViewportHeight.value = 0;
  cropZoom.value = 1;
  cropOffset.x = 0;
  cropOffset.y = 0;
}

function handleCropClosed() {
  if (pendingUploadOptions) {
    pendingUploadOptions.onError(new Error("已取消图片裁剪"));
    pendingUploadOptions = null;
  }
  pendingOriginalImageUrl = "";
  cleanupCrop();
}

async function confirmCrop() {
  if (uploading.value || !cropImageSource.value || !cropHasEnoughResolution.value) return;
  const options = pendingUploadOptions;
  uploading.value = true;
  try {
    const file = await createCroppedImage();
    if (file.size > 10 * 1024 * 1024) throw new Error("裁剪后的图片超过10MB，请缩小裁剪区域后重试");
    pendingUploadOptions = null;
    const result = await uploadBannerImage(file);
    form.imageUrl = result.url;
    form.originalImageUrl = pendingOriginalImageUrl || result.url;
    formRef.value.clearValidate("imageUrl");
    if (options) options.onSuccess(result);
    else ElMessage.success("图片裁剪成功");
    pendingOriginalImageUrl = "";
    cropDialogVisible.value = false;
    cleanupCrop();
  } catch (error) {
    ElMessage.error(error.message || "图片裁剪或上传失败");
    if (options) options.onError(error);
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
    ElMessage.success("保存成功");
    dialogVisible.value = false;
    await loadBanners();
  } catch (error) {
    ElMessage.error(error.message || "保存Banner失败");
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
    ElMessage.success("删除成功");
    if (banners.value.length === 1 && query.pageNum > 1) query.pageNum--;
    await loadBanners();
  } catch (error) {
    ElMessage.error(error.message || "删除Banner失败");
  } finally {
    deletingId.value = null;
  }
}

onMounted(() => {
  loadBanners();
  window.addEventListener("resize", updateCropViewport);
});

onUnmounted(() => {
  window.removeEventListener("resize", updateCropViewport);
  cleanupCrop();
});
</script>

<style scoped lang="scss">
@use "../systemPanel.scss" as *;
.keyword-input { width: 260px; max-width: 100%; }
.banner-preview { width: 144px; height: 90px; border-radius: 6px; }
.image-field { display: flex; flex-direction: column; align-items: flex-start; gap: 12px; width: 100%; }
.image-actions { display: flex; align-items: center; gap: 10px; }
.edit-preview { width: 100%; height: 200px; background: $sys-bg; border-radius: 6px; }
.field-tip { display: block; color: $sys-sub; font-size: 12px; line-height: 1.6; }
.crop-instructions { margin-bottom: 12px; color: $sys-sub; font-size: 13px; line-height: 1.6; }
.crop-viewport { position: relative; width: 100%; max-width: 560px; aspect-ratio: 24 / 5; margin: 0 auto; overflow: hidden; background: #171a18; cursor: grab; touch-action: none; user-select: none; }
.crop-viewport:active { cursor: grabbing; }
.crop-viewport img { position: absolute; max-width: none; pointer-events: none; }
.crop-controls { display: flex; align-items: center; gap: 16px; margin-top: 20px; color: $sys-sub; font-size: 13px; }
.crop-zoom-slider { flex: 1; min-width: 0; accent-color: $sys-primary; }
.crop-warning { margin: 8px 0 0; color: #c45656; font-size: 12px; line-height: 1.5; }
.banner-page a { color: $sys-primary; text-decoration: none; overflow-wrap: anywhere; }
@media (max-width: 640px) {
  .sys-panel { padding: 16px 12px; }
  .title-tip { display: block; margin: 8px 0 0; line-height: 1.6; }
  .crop-controls { gap: 10px; }
}
</style>
