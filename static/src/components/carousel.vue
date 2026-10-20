<template>
  <div class="carousel">
      <el-carousel
      style="border-radius: 5px;width: 510px;"
        height="370px"
        :motion-blur="false"
        trigger="click"
        :interval="5000"
      >
        <el-carousel-item v-for="(item, index) in slides" :key="item.id">
          <component :is="item.linkUrl ? 'a' : 'div'" class="banner-slide"
            :href="item.linkUrl || undefined" :target="item.linkUrl ? '_blank' : undefined"
            :rel="item.linkUrl ? 'noopener noreferrer' : undefined"
            :aria-label="item.content || '首页Banner ' + (index + 1)">
            <img :src="item.imageUrl" :alt="item.content || '首页Banner ' + (index + 1)" class="carousel-image" />
            <span v-if="item.content" class="banner-content">{{ item.content }}</span>
          </component>
        </el-carousel-item>
      </el-carousel>
  </div>
</template>

<script>
import { computed, onMounted, ref } from "vue";
import { ElMessage } from "element-plus";
import { getHomeBanners } from "../api/banner";

export default {
  name: "HomeBannerCarousel",
  setup() {
    const banners = ref([]);
    const slides = computed(() => banners.value.length ? banners.value : [
      { id: "default", imageUrl: "/img/banner1.webp", content: "", linkUrl: "" },
    ]);

    onMounted(async () => {
      try {
        const res = await getHomeBanners();
        if (res.data.code !== 1) throw new Error(res.data.msg || "Banner加载失败");
        banners.value = res.data.data.slice(0, 6);
      } catch (error) {
        ElMessage.error(error.message || "Banner加载失败");
      }
    });
    return {
      slides,
    };
  },
};
</script>

<style scoped>
.carousel {
  width: 510px;
  transform: translate(75px, 64px);
  z-index: 100000;
}

.banner-slide { position: relative; display: block; width: 100%; height: 100%; overflow: hidden; color: white; }
.banner-slide:focus-visible { outline: 2px solid #00a1d6; outline-offset: -2px; }
.banner-content {
  position: absolute;
  right: 0;
  bottom: 0;
  left: 0;
  padding: 28px 16px 36px;
  background: linear-gradient(transparent, rgba(0, 0, 0, .75));
  font-size: 16px;
  line-height: 1.6;
  overflow-wrap: anywhere;
}
.carousel-image {
  user-select: none;
  width: 100%; /* 填满轮播项宽度 */
  height: 100%; /* 填满轮播项高度 */
  display: block;
  object-fit: cover;
  /* 保持比例填充 */
}
</style>
