<template>
  <div  class="aside-container">
    <div style="transform: translateY(50px)">
      <router-link to="/contribute" style="text-decoration: none">
        <div class="upload">
          <img
            src="../img/投稿.png"
            alt="投稿"
            style="width: 16px; transform: translate(1px, 1px)"
          />投稿
        </div>
      </router-link>
      <router-link class="aside-link" to="/contribute/subpage1">
        <div
          v-if="!isActive('/contribute/subpage1')"
          style="margin-bottom: 10px"
        >
          <img src="../img/投稿首页灰.png" alt="首页" /><span
            style="margin-left: 4px"
            >首页</span
          >
        </div>
        <div
          v-if="isActive('/contribute/subpage1')"
          style="margin-bottom: 10px"
        >
          <img src="../img/投稿首页.png" alt="首页" /><span
            style="margin-left: 4px; color: #479fd1"
            >首页</span
          >
        </div>
      </router-link>
      <router-link class="aside-link" to="/contribute/subpage2">
        <div v-if="!isActive('/contribute/subpage2')" style="margin-top: 10px">
          <img src="../img/稿件管理灰.png" alt="稿件管理" /> 稿件管理
        </div>
        <div
          v-if="isActive('/contribute/subpage2')"
          style="color: #479fd1; margin-top: 10px"
        >
          <img src="../img/稿件管理.png" alt="稿件管理" /> 稿件管理
        </div>
      </router-link>
      <router-link
        v-if="store.userInformation.adminFlag||store.userInformation.userName==='MoJi'"
        class="aside-link"
        to="/contribute/subpage3"
      >
        <div v-if="!isActive('/contribute/subpage3')" style="margin-top: 20px">
          <img src="../img/数据中心灰.png" alt="数据中心" /> 审核视频
        </div>
        <div
          v-if="isActive('/contribute/subpage3')"
          style="margin-top: 20px; color: #479fd1"
        >
          <img src="../img/数据中心.png" alt="数据中心" /> 审核视频
        </div>
      </router-link>
      <router-link
        v-if="store.userInformation.userName==='MoJi'"
        class="aside-link"
        to="/contribute/subpage4"
      >
        <div v-if="!isActive('/contribute/subpage4')" style="margin-top: 20px">
          <img src="../img/数据中心灰.png" alt="数据中心" /> 管理用户
        </div>
        <div
          v-if="isActive('/contribute/subpage4')"
          style="margin-top: 20px; color: #479fd1"
        >
          <img src="../img/数据中心.png" alt="数据中心" /> 管理用户
        </div>
      </router-link>
    </div>
  </div>
</template>

<script>
import { useRoute } from "vue-router";
import {useGlobalStore} from "../store/store";
export default {
  name: "AsideComponent",
  setup() {
    const store = useGlobalStore(); // 获取全局状态管理对象
    const route = useRoute(); // 获取当前路由对象
    const isActive = (path) => {
      return route.path === path; // 判断当前路径是否匹配
    };

    return { isActive,store };
  },
};
</script>

<style>
.aside-container {
  display: flex;
  flex-direction: column;
  transform: translate(40px, 0px);
  position: absolute;
  box-shadow: 5px 0 8px rgba(0, 0, 0, 0.03); /* 阴影向右偏移 */
  width: 157px;
  height: 1000px;
}

.upload {
  width: 136px;
  height: 40px;
  background-color: #479fd1;
  transform: translate(-11px, -25px);
  display: flex;
  justify-content: center;
  align-items: center;
  color: white;
  transition: background-color 0.3s ease; /* 添加动画效果 */
}

.upload:hover {
  background-color: #65b6df; /* 改变背景颜色 */
}

.aside-link {
  text-decoration: none;
  color: black;
  display: flex;
  align-items: center; /* 垂直对齐 */
  margin: 5px 0; /* 链接之间的间距 */
}

div img {
  width: 18px;
  margin-right: 8px; /* 图标与文本之间的间距 */
  transform: translate(-13px, 4px);
}

</style>
