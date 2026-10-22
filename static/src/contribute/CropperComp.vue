<template>
  <div class="cropper">
    <div class="cropper-body">
      <div class="cropper-body-left">
        <div ref="stage" class="img-wrp" @pointermove="moveSelection"
          @pointerup="endInteraction" @pointercancel="endInteraction" @lostpointercapture="endInteraction">
          <img v-if="imgURL" v-show="imageReady" ref="originImage" :src="imgURL"
            alt="待裁剪封面" crossorigin="anonymous" draggable="false"
            @load="onImageLoad" @error="onImageError">
          <div v-if="!imageReady" class="cropper-empty" :role="imageError ? 'alert' : 'status'">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" aria-hidden="true">
              <rect x="3" y="4" width="18" height="16" rx="3"/><circle cx="8" cy="9" r="1.5"/>
              <path d="m3 17 5-5 4 4 4-5 5 6"/>
            </svg>
            <strong>{{ imageError ? '画面加载失败' : imgURL ? '正在加载画面' : '暂无可裁剪画面' }}</strong>
            <span>{{ imageError ? '请重新选择视频画面，或切换到上传封面' : '可选择视频画面，也可以上传图片或使用封面模板' }}</span>
          </div>
          <div v-if="imageReady" class="show-position"
            :style="{ width: `${originShowWidth}px`, height: `${originShowHeight}px`,
              left: `${originWidthOffset}px`, top: `${originHeightOffset}px` }">
            <div class="crop-box" :style="{ width: `${boxWidth}px`, height: `${boxHeight}px`,
              left: `${boxLeft}px`, top: `${boxTop}px` }">
              <div class="crop-box-safe-area" aria-hidden="true"></div>
              <div class="crop-box-move" @pointerdown.prevent="startInteraction($event, 'move')"></div>
              <span v-for="corner in ['lt', 'rt', 'lb', 'rb']" :key="corner"
                class="cropper-point" :class="`point-${corner}`"
                @pointerdown.stop.prevent="startInteraction($event, corner)"></span>
            </div>
          </div>
        </div>
        <div class="tips-wrp">
          <span v-if="imageReady">裁剪分辨率 <strong>{{ cutWidth }} × {{ cutHeight }}</strong></span>
          <span>拖动选框调整位置，拖动四角缩放；虚线内为 4:3 安全区域</span>
        </div>
      </div>
      <div class="cropper-body-right">
        <div v-for="ratio in ['16:9', '4:3']" :key="ratio" class="preview-item">
          <div class="preview-title">{{ ratio }} 效果预览</div>
          <div class="preview-card">
            <div class="preview-image" :class="{ 'is-standard': ratio === '4:3' }">
              <img v-if="imageReady" :src="imgURL" alt="封面裁剪预览" :style="previewStyle(ratio)">
              <span v-else>暂无预览</span>
            </div>
            <p v-if="title.trim()" class="preview-caption">{{ title }}</p>
          </div>
        </div>
      </div>
    </div>
    <slot name="frames"></slot>
    <div class="cropper-controls">
      <slot name="actions"></slot>
      <button type="button" class="crop-confirm" :disabled="!imageReady" @click="cut">完成裁剪</button>
    </div>
    <canvas ref="cropperCanvas" hidden></canvas>
  </div>
</template>

<script>
import { ElMessage } from "element-plus";

const ASPECT_RATIO = 16 / 9;

export default {
  name: "CropperComp",
  emits: ["cut", "update:cutOrder"],
  props: {
    imgURL: { type: String, default: null },
    cutOrder: { type: Boolean, default: false },
    title: { type: String, default: "" },
  },
  data() {
    return {
      width: 0,
      height: 0,
      stageWidth: 480,
      boxWidth: 0,
      boxHeight: 0,
      boxLeft: 0,
      boxTop: 0,
      imageError: false,
      interaction: null,
      stageObserver: null,
    };
  },
  computed: {
    imageReady() { return Boolean(this.imgURL && !this.imageError && this.width > 0 && this.height > 0); },
    stageHeight() { return this.stageWidth / ASPECT_RATIO; },
    originShowWidth() { return this.imageReady ? Math.min(this.stageWidth, this.stageHeight * this.width / this.height) : 0; },
    originShowHeight() { return this.imageReady ? Math.min(this.stageHeight, this.stageWidth * this.height / this.width) : 0; },
    originWidthOffset() { return (this.stageWidth - this.originShowWidth) / 2; },
    originHeightOffset() { return (this.stageHeight - this.originShowHeight) / 2; },
    cutOffsetX() { return this.imageReady ? Math.max(0, Math.min(this.width - 1, Math.floor(this.boxLeft / this.originShowWidth * this.width))) : 0; },
    cutOffsetY() { return this.imageReady ? Math.max(0, Math.min(this.height - 1, Math.floor(this.boxTop / this.originShowHeight * this.height))) : 0; },
    cutWidth() { return this.imageReady ? Math.min(this.width - this.cutOffsetX, Math.max(1, Math.floor(this.boxWidth / this.originShowWidth * this.width))) : 0; },
    cutHeight() { return this.imageReady ? Math.min(this.height - this.cutOffsetY, Math.max(1, Math.floor(this.boxHeight / this.originShowHeight * this.height))) : 0; },
  },
  methods: {
    onImageLoad(event) {
      this.width = event.target.naturalWidth;
      this.height = event.target.naturalHeight;
      this.imageError = false;
      this.initBox();
    },
    onImageError() {
      this.endInteraction();
      this.imageError = true;
      this.width = this.height = 0;
    },
    initBox() {
      this.boxWidth = Math.min(this.originShowWidth, this.originShowHeight * ASPECT_RATIO) * 0.9;
      this.boxHeight = this.boxWidth / ASPECT_RATIO;
      this.boxLeft = (this.originShowWidth - this.boxWidth) / 2;
      this.boxTop = (this.originShowHeight - this.boxHeight) / 2;
    },
    resizeStage(width) {
      if (width <= 0 || width === this.stageWidth) return;
      this.endInteraction();
      const scale = width / this.stageWidth;
      this.stageWidth = width;
      this.boxWidth *= scale;
      this.boxHeight *= scale;
      this.boxLeft *= scale;
      this.boxTop *= scale;
    },
    previewStyle(ratio) {
      const width = this.boxWidth * (ratio === "4:3" ? 0.75 : 1);
      const left = this.boxLeft + (ratio === "4:3" ? this.boxWidth * 0.125 : 0);
      return {
        width: `${this.originShowWidth / width * 100}%`,
        transform: `translate(${-left / this.originShowWidth * 100}%, ${-this.boxTop / this.originShowHeight * 100}%)`,
      };
    },
    startInteraction(event, direction) {
      if (!this.imageReady || this.interaction || event.button !== 0) return;
      event.currentTarget.setPointerCapture(event.pointerId);
      this.interaction = {
        direction, target: event.currentTarget, pointerId: event.pointerId,
        x: event.clientX, y: event.clientY,
        left: this.boxLeft, top: this.boxTop, width: this.boxWidth, height: this.boxHeight,
      };
    },
    moveSelection(event) {
      const start = this.interaction;
      if (!start || start.pointerId !== event.pointerId) return;
      const dx = event.clientX - start.x;
      const dy = event.clientY - start.y;
      if (start.direction === "move") {
        this.boxLeft = Math.max(0, Math.min(this.originShowWidth - this.boxWidth, start.left + dx));
        this.boxTop = Math.max(0, Math.min(this.originShowHeight - this.boxHeight, start.top + dy));
        return;
      }
      const fromLeft = start.direction.includes("l");
      const fromTop = start.direction.includes("t");
      const anchorX = start.left + (fromLeft ? start.width : 0);
      const anchorY = start.top + (fromTop ? start.height : 0);
      const maxWidth = Math.min(fromLeft ? anchorX : this.originShowWidth - anchorX,
        (fromTop ? anchorY : this.originShowHeight - anchorY) * ASPECT_RATIO);
      const delta = Math.abs(dx) >= Math.abs(dy * ASPECT_RATIO)
        ? dx * (fromLeft ? -1 : 1) : dy * ASPECT_RATIO * (fromTop ? -1 : 1);
      this.boxWidth = Math.max(Math.min(64, maxWidth), Math.min(maxWidth, start.width + delta));
      this.boxHeight = this.boxWidth / ASPECT_RATIO;
      this.boxLeft = fromLeft ? anchorX - this.boxWidth : start.left;
      this.boxTop = fromTop ? anchorY - this.boxHeight : start.top;
    },
    endInteraction() {
      const start = this.interaction;
      this.interaction = null;
      if (start?.target.hasPointerCapture(start.pointerId)) start.target.releasePointerCapture(start.pointerId);
    },
    cut() {
      if (!this.imageReady) {
        ElMessage.warning("请先选择可裁剪的画面");
        this.$emit("update:cutOrder", false);
        return;
      }
      try {
        const canvas = this.$refs.cropperCanvas;
        canvas.width = this.cutWidth;
        canvas.height = this.cutHeight;
        const context = canvas.getContext("2d");
        if (!context) throw new Error("当前浏览器无法裁剪图片，请直接上传封面");
        context.drawImage(this.$refs.originImage, this.cutOffsetX, this.cutOffsetY,
          this.cutWidth, this.cutHeight, 0, 0, canvas.width, canvas.height);
        this.$emit("cut", canvas.toDataURL("image/jpeg"));
      } catch (error) {
        ElMessage.error(error.message || "裁剪失败，请重新选择图片或直接上传封面");
      } finally {
        this.$emit("update:cutOrder", false);
      }
    },
  },
  mounted() {
    this.resizeStage(this.$refs.stage.clientWidth);
    this.stageObserver = new ResizeObserver(entries => this.resizeStage(entries[0].contentRect.width));
    this.stageObserver.observe(this.$refs.stage);
  },
  beforeUnmount() {
    this.stageObserver?.disconnect();
    this.endInteraction();
  },
  watch: {
    imgURL() {
      this.endInteraction();
      this.imageError = false;
      this.width = this.height = this.boxWidth = this.boxHeight = 0;
    },
    cutOrder(current) { if (current) this.cut(); },
  },
};
</script>

<style scoped>
.cropper { min-width: 0; user-select: none; }
.cropper-body { display: grid; grid-template-columns: minmax(0, 1fr) 184px; align-items: start; gap: 24px; }
.cropper-body-left { min-width: 0; }
.img-wrp { position: relative; width: 100%; aspect-ratio: 16 / 9; border-radius: 12px; background: #202727; overflow: hidden; touch-action: none; }
.img-wrp > img { position: absolute; inset: 0; display: block; width: 100%; height: 100%; object-fit: contain; }
.cropper-empty { position: absolute; inset: 0; display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 12px; padding: 16px; color: #fff; text-align: center; }
.cropper-empty svg { width: 36px; height: 36px; opacity: .7; }
.cropper-empty strong { font-size: 14px; font-weight: 500; }
.cropper-empty span { max-width: 300px; font-size: 12px; line-height: 1.7; color: #c6d2cf; }
.show-position { position: absolute; }
.crop-box { position: absolute; outline: 1px solid #fff; box-shadow: 0 0 0 100vmax #0008; }
.crop-box-safe-area { position: absolute; inset: 0 12.5%; border-inline: 1px dashed #fff9; pointer-events: none; }
.crop-box-move { position: absolute; inset: 0; cursor: move; }
.cropper-point { position: absolute; box-sizing: border-box; width: 14px; height: 14px; border: 2px solid var(--brand); border-radius: 4px; background: #fff; touch-action: none; }
.point-lt { top: -7px; left: -7px; cursor: nwse-resize; }
.point-rt { top: -7px; right: -7px; cursor: nesw-resize; }
.point-lb { bottom: -7px; left: -7px; cursor: nesw-resize; }
.point-rb { bottom: -7px; right: -7px; cursor: nwse-resize; }
.tips-wrp { display: flex; flex-direction: column; gap: 4px; margin-top: 12px; color: var(--ink-3); font-size: 12px; line-height: 1.7; }
.tips-wrp strong { color: var(--ink-2); font-weight: 500; }
.cropper-body-right { display: grid; gap: 16px; min-width: 0; }
.preview-title { margin-bottom: 8px; color: var(--ink-2); font-size: 12px; }
.preview-card { overflow: hidden; border: 1px solid var(--line); border-radius: 10px; background: #fff; }
.preview-image { position: relative; aspect-ratio: 16 / 9; overflow: hidden; background: var(--fill-soft); }
.preview-image.is-standard { aspect-ratio: 4 / 3; }
.preview-image img { position: absolute; top: 0; left: 0; height: auto; max-width: none; }
.preview-image > span { position: absolute; inset: 0; display: grid; place-items: center; color: var(--ink-3); font-size: 12px; }
.preview-caption { display: -webkit-box; margin: 0; padding: 8px 10px; overflow: hidden; -webkit-box-orient: vertical; -webkit-line-clamp: 2; overflow-wrap: anywhere; color: var(--ink); font-size: 12px; line-height: 1.6; }
.cropper-controls { display: flex; justify-content: flex-end; align-items: center; flex-wrap: wrap; gap: 12px; margin-top: 18px; padding-top: 16px; border-top: 1px solid var(--line); }
.crop-confirm { min-height: 38px; padding: 0 24px; border: 0; border-radius: 9px; background: var(--brand); color: #fff; font: inherit; font-size: 14px; cursor: pointer; }
.crop-confirm:hover:not(:disabled) { background: var(--brand-hover); }
.crop-confirm:disabled { opacity: .45; cursor: not-allowed; }
.crop-confirm:focus-visible { outline: 2px solid var(--brand); outline-offset: 3px; }
@media (max-width: 640px) {
  .cropper-body { grid-template-columns: minmax(0, 1fr); gap: 16px; }
  .cropper-body-right { grid-template-columns: repeat(2, minmax(0, 1fr)); }
  .cropper-empty { gap: 8px; }
  .cropper-empty svg { width: 28px; height: 28px; }
}
</style>
