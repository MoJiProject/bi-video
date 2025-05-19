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
import banner1 from "../img/banner1.jpg";
import banner2 from "../img/banner2.jpg";
import banner3 from "../img/banner3.png";
import banner4 from "../img/banner4.jpg";

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
  width: 100%; /* 填满轮播项宽度 */
  height: 100%; /* 填满轮播项高度 */
  /* 保持比例填充 */
}
</style>
