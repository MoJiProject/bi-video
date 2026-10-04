<template>
  <div class="follow-anime-list">
    <div
      class="item"
      v-for="anime in data"
      :key="anime.name"
      @click="jumpToAnime(anime.url)"
    >
      <img :src="anime.coverAddress" />
      <div class="info">
        <div class="name">{{ anime.name }}</div>
        <div class="introduce">{{ anime.introduce }}</div>
        <div class="source">番剧 · 日本 · {{ anime.episodeCount }}</div>
        <span class="tags" v-for="anime in anime.tag" :key="anime">
          <el-tag :type="tagType(anime)">{{ anime }}</el-tag>
        </span>
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  data: {
    type: Array,
    required: true,
  },
});

// 标签类型
function tagType(tag) {
  if (tag === "血腥" || tag === "爱情" || tag === "反战") return "danger";
  else if (tag === "治愈" || tag === "校园" || tag === "友情" || tag === "亲情")
    return "success";
  else if (tag === "悬疑" || tag === "推理" || tag === "冒险") return "info";
  else if (tag === "后宫" || tag === "成长" || tag === "奇幻") return "warning";
  else return "primary";
}

// 跳转
function jumpToAnime(url) {
  window.open(url, "anime");
}
</script>

<style lang="scss" scoped>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

.follow-anime-list {
  display: grid;
  grid-template-columns: repeat(3, 377.33px);
  column-gap: 16px;
  row-gap: 20px;
  .item {
    display: flex;
    cursor: pointer;
    img {
      width: 154px;
      height: 205px;
      flex-shrink: 0;
      object-fit: cover;
      position: relative;
      border-radius: 6px;
    }
    .info {
      margin-left: 12px;

      .name {
        color: #1C2321;
        font-weight: 500;
        font-size: 16px;
        line-height: 22px;
        transition: color 0.3s ease;
      }
      .name:hover {
        color: #3CC0A6;
      }
      .introduce {
        margin-top: 12px;
        margin-bottom: 12px;
        font-size: 14px;
        line-height: 20px;
        color: #5C6664;
        display: -webkit-box;
        overflow: hidden;
        word-break: break-all;
        text-overflow: ellipsis;
        -webkit-line-clamp: 3;
        -webkit-box-orient: vertical;
      }
      .source {
        margin-bottom: 4px;
        font-size: 14px;
        line-height: 20px;
        color: #5C6664;
        display: flex;
        align-items: center;
      }
      .tags {
        display: inline-flex;
        margin-top: 10px;
        padding-right: 5px;
      }
    }
  }
}
@media (max-width: 1400px) {
  .follow-anime-list {
    grid-template-columns: repeat(2, 377.33px);
  }
}
@media (max-width: 1050px) {
    .follow-anime-list {
    grid-template-columns: repeat(1, 377.33px);
  }
}
</style>
