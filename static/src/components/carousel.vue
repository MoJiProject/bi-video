<template>
  <div class="carousel">
      <el-carousel
        style="border-radius: 5px;width: 510px;"
        height="370px"
        :motion-blur="false"
        trigger="click"
        :interval="5000"
      >
        <el-carousel-item v-for="(item, index) in images" :key="index">
          <img :src="item" alt="Image" class="carousel-image" />
        </el-carousel-item>
      </el-carousel>
  </div>
</template>

<script>
import { reactive } from "vue";
const banner1 = "/img/banner1.webp"
const banner2 = "/img/banner2.webp"
const banner3 = "/img/banner3.webp"
const banner4 = "/img/banner4.webp"

export default {
  name: "Carousel",
  setup() {
    const images = reactive([banner1, banner2, banner3, banner4]);

    return {
      images,
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

.carousel-image {
  user-select: none;
  width: 100%; /* 填满轮播项宽度 */
  height: 100%; /* 填满轮播项高度 */
  /* 保持比例填充 */
}
</style>
