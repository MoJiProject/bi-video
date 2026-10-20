<template>
  <div
    ref="searchRoot"
    class="search"
    :class="{searchShowBox:showBox,headBorderFlag:head2Flag}"
    @mousedown="handleClickInside"
    @click="selectSearchContent"
  >
    <!-- 搜索框 -->
          <el-tooltip
           :teleported="false"
            :disabled="showBox"
            popper-class="custom-tooltip"
            effect="light"
            :content="Content.length>0?Content:placeholderWord"
            placement="bottom"
            :show-after="300"
            :offset="10"
            :show-arrow="false"
            :hide-after="0"
            >
            <input
            type="text"
            class="search-box"
            aria-label="搜索视频、创作者或感兴趣的内容"
            autocomplete="off"
            :class="{searchboxMax:showBox}"
            :placeholder="placeholderWord"
            @focus="showBox = true"
            maxlength="30"
            v-model="Content"
            @keyup.enter="jumpSearch"
          />
          </el-tooltip>

      <button
      type="button"
      v-show="Content.length>0"
      aria-label="清空搜索内容"
      title="清空搜索内容"
      @mousedown.prevent
      @click.stop="Content = ''"
      class="deleteAllSearchImg"
    >
        <svg viewBox="0 0 24 24" aria-hidden="true"><path d="m7 7 10 10M17 7 7 17" /></svg>
      </button>

    <button type="button" class="search2" aria-label="搜索" @click.stop="jumpSearch">
      <svg viewBox="0 0 24 24" aria-hidden="true">
        <circle cx="10.5" cy="10.5" r="6.5" />
        <path d="m15.5 15.5 4.5 4.5" />
      </svg>
    </button>
    <!-- 显示的盒子-->
    <div v-if="showBox && keyWord.length === 0" class="box" @mousedown.stop>
      <!-- 这里可以放置搜索历史记录或其他内容 -->

      <div v-show="flag" class="history-heading">
        <span class="box-title">搜索历史</span>
        <button type="button" @click.stop="ckaeanAllContent" class="cleanAllSearch">
          <svg viewBox="0 0 24 24" aria-hidden="true">
            <path d="M4 6h16M9 6V4h6v2M6 6l1 14h10l1-14M10 10v6m4-6v6" />
          </svg>
          清空历史
        </button>
      </div>
      <div
        v-show="flag"
        class="box-body"
        :class="{ 'is-clamped': !showMore }"
      >
        <div class="history-chips">
          <div
            class="searchContentCss"
            v-for="(item, index) in reversedSearchDatas"
            :key="index"
          >
            <button
              type="button"
              class="searchContentFontCss"
              @click="sendSearchAxios(item.value)"
            >
              {{ item.value }}
            </button>
            <button
              type="button"
              class="deleteSearchCss"
              :aria-label="'删除搜索记录：' + item.value"
              title="删除这条记录"
              @click.stop="deleteSearchContent(index)"
            >
              <svg viewBox="0 0 24 24" aria-hidden="true"><path d="m7 7 10 10M17 7 7 17" /></svg>
            </button>
          </div>
        </div>
      </div>
      <div
        v-show="searchHeight > 100 && reversedSearchDatas.length>6 && !showFewerFlag"
        class="searchMore"
        @mouseover="imgFlag1 = true"
        @mouseleave="imgFlag1 = false"
        @click="searchMore"
      >
        展开更多
        <img :src="imgFlag1 ? fewerImg : showImg" style="width: 10px" />
      </div>
      <div
        v-show="showFewerFlag"
        class="searchFewer"
        @mouseover="imgFlag2 = true"
        @mouseleave="imgFlag2 = false"
        @click="searchFewer"
      >
        收起
        <img
          :src="imgFlag2 ? fewerImg : showImg"
          style="width: 10px; transform: rotate(180deg)"
        />
      </div>
      <div v-show="fireSearch.length === 10" class="searchContent">
        <div class="box-title">
          qingmang热搜
        </div>
        <ul class="fireSearch">
          <li v-for="(item, i) in fireSearch" :key="i" @click="sendSearchAxios(item?.word)">
            <span class="aa" :class="{ 'is-top': i < 3 }">{{ i + 1 }}</span>
            <span class="bb">
              {{ item?.word }}
              <img v-if="i < 3" src="/img/热门搜索.png" />
            </span>
          </li>
        </ul>
      </div>
    </div>

    <!-- 显示的盒子 -->
    <div v-show="showBox && keyWord.length !== 0" class="box" @mousedown.stop>
      <ul class="fireSearch">
        <li
          v-for="(item, i) in keyWord.slice(0, 10)"
          :key="i"
          @click="sendSearchAxios(Content + item?.word)"
        >
          <span class="cc">{{ Content }}</span>
          <span class="dd">{{ item?.word }}</span>
        </li>
      </ul>
    </div>
  </div>
</template>

<script>
import { searchApi } from "../api/product";
import { ElMessage } from "element-plus";
import {
  ref,
  reactive,
  onMounted,
  onBeforeUnmount,
  computed,
  watch,
} from "vue";
const showImg = "/img/展开更多.png"
const fewerImg = "/img/展开更多蓝.png"
import {useGlobalStore} from "../store/store";
export default {
  name: "search",
  props:{
    head2Flag:{
      default:false,
      type:Boolean
    },
    mainSearchFlag: {
      type: Boolean,
      default: false,
    },
    handleUrlChange: {
      type: Function,
      default: null,
    },
  },
  setup(props) {
    const head2Flag=ref(props.head2Flag);
    const searchRoot = ref(null);
    const showBox = ref(false); // 控制盒子显示的状态
    const Content = ref("");
    const searchDatas = reactive([]);
    const flag = ref(false);
    const searchHeight = ref(0);
    const imgFlag1 = ref(false);
    const imgFlag2 = ref(false);
    const showMore = ref(false);
    const placeholderWord=ref(null);
    const showFewerFlag = ref(false);
    const fireSearch = reactive([]);
    const keyWord = reactive([]);
    const store = useGlobalStore();
    const handleClickInside = (event) => {
      // 判断点击是否在搜索框及其子元素内
      const searchElement = searchRoot.value;
      if (searchElement && !searchElement.contains(event.target)) {
        showBox.value = false;
      }
    };

    const uniqueContentHolder = ref(null); // 引用盒子
    onMounted(() => {
      if (props.handleUrlChange) {
        Content.value = new URLSearchParams(window.location.search).get("keyword") || "";
      }
      document.addEventListener("mousedown", handleClickInside);
      selectFireWord();
      selectSearchContent();
      getBoxHeight();
      document.addEventListener("scroll",handleScroll);

        setTimeout(() => {
          placeholderWord.value = props.handleUrlChange ? "输入关键字搜索" : store.placeholderWord;
          if(!placeholderWord.value)
            getPlaceholder();
        }, 200);

    });

    onBeforeUnmount(() => {
      document.removeEventListener("mousedown", handleClickInside);
      document.removeEventListener("scroll",handleScroll);

    });

    function saveSearchContent() {
      if (Content.value.length > 0) {
        const searchData = {
          value: Content.value,
          timestamp: Date.now(),
        };

        // 获取现有的搜索数据
        const existingData = localStorage.getItem("searchData");
        let searchArray = [];
        if (existingData) {
          // 如果已有数据，解析为数组
          searchArray = JSON.parse(existingData);
        }

        const valueExists = (value, array) => {
          return array.some((item) => item.value === value);
        };

        // 检查当前输入的值是否已存在
        if (valueExists(Content.value, searchArray)) {
          return; // 如果已存在，则返回
        } else {
          if (searchArray.length === 10) searchArray.shift();
          // 添加新的搜索数据到数组中
          searchArray.push(searchData);

          // 存储更新后的数组
          localStorage.setItem("searchData", JSON.stringify(searchArray));
        }
        getBoxHeight();
      }
    }
    function selectSearchContent() {
      const storedData = localStorage.getItem("searchData"); // 获取存储的数据
      if (storedData) {
        const parsedData = JSON.parse(storedData);
        Object.assign(searchDatas, parsedData); // 合并到 searchDatas 中
        if (searchDatas.length > 0) {
          flag.value = true;
          getBoxHeight();
        }
      } else flag.value = false;
    }

    function deleteSearchContent(index) {
      // 找到在 reversedSearchDatas 中的值
      const itemToDelete = searchDatas[searchDatas.length - index - 1];

      // 在 searchDatas 中找到并删除该值
      const itemIndex = searchDatas.findIndex(
        (item) => item.value === itemToDelete.value,
      );
      if (itemIndex !== -1) {
        searchDatas.splice(itemIndex, 1); // 删除 searchDatas 中的元素
      }
      localStorage.setItem("searchData", JSON.stringify(searchDatas));
      if (searchDatas.length === 0) flag.value = false;

      getBoxHeight();
    }
    const reversedSearchDatas = computed(() => {
      return [...searchDatas].reverse(); // 反转数组并返回新的数组
    });

    function jumpSearch() {
      saveSearchContent();
      selectSearchContent();
      addKeyWord();
      searchVideoAxios();
    }

    function ckaeanAllContent() {
      localStorage.removeItem("searchData");
      searchDatas.length = 0; // 清空数组
      searchHeight.value = 0;
      showFewerFlag.value = false;
      showMore.value = false;
    }
    function getBoxHeight() {
      const box = document.getElementById('uniqueContentHolder');
      if(box)
        searchHeight.value = box.offsetHeight; // 获取计算后的高度
    }

    function searchMore() {
      showMore.value = true;
      showFewerFlag.value = true;
      imgFlag2.value = false;
    }
    function searchFewer() {
      showMore.value = false;
      showFewerFlag.value = false;
      imgFlag1.value = false;
    }

    //获取热词
    async function selectFireWord() {
      try {
        const list = await searchApi.hot(10);
        fireSearch.length = 0;
        list.forEach((k) => fireSearch.push(k.keyword));
      } catch (error) { ElMessage.error(error.message); }
    }
    //获取关键字
async function selectKeyWord(Value) {
      if (!Value) {
        keyWord.length = 0;
        return;
      }
      try {
        const list = await searchApi.hot(20);
        keyWord.length = 0;
        Object.assign(keyWord, list.map((k) => k.keyword).filter((w) => w.startsWith(Value))
          .map(word => ({ word: word.slice(Value.length) })));
      } catch (error) { ElMessage.error(error.message); }
    }

    //记录一次搜索。新后端在 searchApi.videos 内部记账，这里不用单独上报。
    async function addKeyWord() {
    }

    watch(Content, (newValue) => {
      if (newValue.length === 0) keyWord.length = 0;
      else selectKeyWord(newValue);
    });

    function sendSearchAxios(value) {
      Content.value = value;
      searchVideoAxios();
    }

    //搜索placeholder
    function searchVideoAxios() {
      if (props.handleUrlChange) {
        showBox.value = false;
        const params = new URLSearchParams(window.location.search);
        params.set("keyword", Content.value);
        window.history.replaceState({}, document.title, `${window.location.pathname}?${params.toString()}`);
        props.handleUrlChange(Content.value);
        return;
      }
      if (Content.value.length === 0) Content.value = placeholderWord.value;
      const url = `./search?keyword=${encodeURIComponent(
        Content.value,
      )}&classifyIndex=`;
      window.open(url, "_blank"); // '_blank' 表示在新窗口或标签页打开
    }

    //监视不显示时关闭搜索框
    function handleScroll(){

      const scrollPosition = window.scrollY; // 当前滚动距离
      if (props.handleUrlChange) {
        if (props.mainSearchFlag === (scrollPosition > 155)) {
          store.setSearchDisplayFlag2(showBox.value);
        } else {
          showBox.value = false;
        }
        return;
      }

      if(scrollPosition < 70 && !head2Flag.value && !showBox.value)
         store.setSearchDisplayFlag(false);
      if(scrollPosition < 70 && !head2Flag.value && showBox.value)
         store.setSearchDisplayFlag(true);
      if(scrollPosition > 70 && head2Flag.value && !showBox.value)
         store.setSearchDisplayFlag(false);
      if(scrollPosition > 70 && head2Flag.value && showBox.value)
         store.setSearchDisplayFlag(true);

       if(store.searchDisplayFlag)
       showBox.value = true;
       if(!store.searchDisplayFlag)
       showBox.value = false;

    }

//获取placeholder
      async function getPlaceholder(){
        // 新后端没有随机占位词接口，固定用品牌标语
        placeholderWord.value = "分享每一次观看";
      }


      watch(searchHeight,()=>{
       if(searchHeight.value<100)
       {
         showMore.value=false;
         showFewerFlag.value=false;
       }
     })



    return {
      searchRoot,
      showBox,
      saveSearchContent,
      Content,
      selectSearchContent,
      searchDatas,
      deleteSearchContent,
      reversedSearchDatas,
      jumpSearch,
      ckaeanAllContent,
      flag,
      uniqueContentHolder,
      searchHeight,
      imgFlag1,
      imgFlag2,
      showImg,
      fewerImg,
      showMore,
      showFewerFlag,
      searchMore,
      searchFewer,
      fireSearch,
      keyWord,
      sendSearchAxios,
      placeholderWord,
    };
  },
};
</script>

<style lang="scss" scoped>
/*
  搜索框。
  原宽度散在三处：.search 写死 300px、.headBorderFlag .box 写死 337px、
  .box 下拉写死 413px，导致浅色页头上输入框比容器窄一截、下拉比输入框宽 76px。
  现在容器宽度交给父级（页头的 .hd-search > *），自身只负责内部排布。
*/

.search {
  box-sizing: border-box;
  position: relative;
  display: flex;
  align-items: center;
  width: 100%;
  height: 44px;
  border-radius: 15px;
  background: linear-gradient(110deg, #f6f9f2, #fafaf5);
  border: 1px solid #dfe8d9;
  z-index: var(--z-panel);
  transition: background-color .2s, border-color .2s, box-shadow .2s;
}

.search:hover {
  background-color: #fff;
  border-color: #b9cdb5;
}

.search:focus-within,
.search.searchShowBox {
  background-color: #fff;
  border-color: #79a88a;
  box-shadow: 0 0 0 3px rgba(76, 129, 84, .09);
}

/* 输入框：唯一伸缩项 */
.search-box {
  box-sizing: border-box;
  flex: 1 1 auto;
  min-width: 0;
  height: 100%;
  margin: 0;
  width: 0;
  padding: 0 12px 0 16px;
  background: transparent;
  border: none;
  outline: none;
  color: var(--ink);
  font-size: 14px;

  &::placeholder { color: #97a18f; font-size: 13px; }
}

/* 清空按钮：贴输入框右缘，跟随输入宽度，不写死 translate */
.deleteAllSearchImg {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex: none;
  width: 30px;
  height: 30px;
  margin-right: 2px;
  padding: 0;
  border: 0;
  border-radius: 50%;
  background: #edf2e8;
  color: #7c8f72;
  cursor: pointer;
  transform: none;

  &:hover { color: #355e3d; background: #dfe9d7; }
}

.deleteAllSearchImg svg,
.deleteSearchCss svg,
.cleanAllSearch svg {
  flex: none;
  width: 20px;
  height: 20px;
  fill: none;
  stroke: currentColor;
  stroke-width: 1.8;
  stroke-linecap: round;
  stroke-linejoin: round;
}

/* 搜索按钮 */
.search2 {
  box-sizing: border-box;
  flex: none;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: calc(100% - 8px);
  margin: 4px;
  padding: 0;
  border: 1px solid #dfe8d7;
  border-radius: 11px;
  background: #edf3e7;
  color: #456c48;
  cursor: pointer;
  transform: none;
  transition: background-color .2s, border-color .2s, color .2s;
}

.search2:hover {
  background-color: #e0ecd6;
  border-color: #c7d9bc;
  color: #285438;
}

.search2:focus-visible { outline: 2px solid #126b58; outline-offset: 1px; }

.search2 svg {
  flex: none;
  width: 24px;
  height: 24px;
  fill: none;
  stroke: currentColor;
  stroke-width: 1.8;
  stroke-linecap: round;
  stroke-linejoin: round;
}

@media (max-width: 480px) {
  .search2 { width: 34px; height: calc(100% - 4px); margin: 2px; border-radius: 8px; }
  .search2 svg { width: 20px; height: 20px; }
}

/* 下拉面板：宽度跟随输入框，不再写死 337 / 413px */
.box {
  box-sizing: border-box;
  position: absolute;
  top: calc(100% + 10px);
  left: -1px;
  width: calc(100% + 2px);
  max-height: 420px;
  padding: 18px;
  background-color: #fff;
  border: 1px solid #e0e7dc;
  border-radius: 14px;
  box-shadow: 0 14px 36px rgba(38, 66, 37, .12);
  z-index: var(--z-panel);
  overflow-x: hidden;
  overflow-y: auto;
  transform: none;
}

.box-title {
  margin-bottom: var(--gap-3);
  color: var(--ink);
  font-size: 15px;
  font-weight: 700;
}

.box-body {
  margin-top: 0;
}

/* 折叠态：超出两行截断，靠 max-height 而不是 translate 上移 */
.box-body.is-clamped {
  max-height: 104px;
  overflow: hidden;
}

/* 历史词条：flex 换行，宽度由内容决定 */
.history-chips {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.searchContentCss {
  position: relative;
  display: flex;
  align-items: center;
  box-sizing: border-box;
  gap: 6px;
  max-width: min(220px, 100%);
  height: 36px;
  padding: 0 4px 0 12px;
  border: 1px solid #e7ecdf;
  border-radius: 10px;
  background-color: #f6f8f1;
  transform: none;
  margin: 0;
  transition: background-color .2s;
}

.searchContentCss:hover {
  background-color: var(--brand-soft);
}

.searchContentCss .searchContentFontCss {
  flex: 1 1 auto;
  min-width: 0;
  padding: 0;
  border: 0;
  background: transparent;
  overflow: hidden;
  color: var(--ink);
  font-family: inherit;
  font-size: 13px;
  line-height: 34px;
  text-align: left;
  white-space: nowrap;
  text-overflow: ellipsis;
  cursor: pointer;
}

.searchContentCss:hover .searchContentFontCss {
  color: var(--brand);
}

.deleteSearchCss {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex: none;
  width: 28px;
  height: 28px;
  padding: 0;
  border: 0;
  border-radius: 8px;
  background: transparent;
  color: #87977d;
  cursor: pointer;

  &:hover { background: #f7e9e3; color: #ae6249; }
}

/* 清空 / 展开 / 收起：行内流式排布 */
.history-heading {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 14px;

  .box-title { margin: 0; }
}

.cleanAllSearch {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex: none;
  gap: 5px;
  min-height: 32px;
  margin: 0;
  padding: 0 9px;
  border: 1px solid #e5eade;
  border-radius: 9px;
  background: #f7f9f3;
  color: #73866a;
  font-family: inherit;
  font-size: 12px;
  cursor: pointer;
  transition: color .2s, background-color .2s;

  &:hover { border-color: #e8d4c8; background: #fbf0e9; color: #a86a4c; }
}

.deleteAllSearchImg:focus-visible,
.deleteSearchCss:focus-visible,
.cleanAllSearch:focus-visible,
.searchContentFontCss:focus-visible { outline: 2px solid #608657; outline-offset: 2px; }

.searchMore:hover,
.searchFewer:hover {
  color: var(--brand);
}

.searchMore,
.searchFewer {
  display: flex;
  align-items: center;
  gap: 4px;
  margin: 8px 0 0;
  color: var(--ink-3);
  font-size: 12px;
  cursor: pointer;
  transform: none;
}

/* 热搜榜 */
.fireSearch {
  margin: 0;
  padding: 0;
  list-style: none;
}

.fireSearch li {
  display: flex;
  align-items: center;
  gap: 10px;
  width: 100%;
  height: 34px;
  padding: 0 8px;
  border-radius: var(--radius-sm);
  cursor: pointer;
  transform: none;
  transition: background-color .2s;
}

.fireSearch li:hover {
  background-color: var(--fill);
}

.fireSearch .aa {
  flex: none;
  width: 18px;
  color: var(--ink-3);
  font-size: 13px;
  text-align: center;
}

.fireSearch .aa.is-top {
  color: var(--accent);
  font-weight: 700;
}

.fireSearch .bb {
  display: flex;
  align-items: center;
  gap: 4px;
  min-width: 0;
  color: var(--ink-2);
  font-size: 13px;
  transform: none;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.fireSearch .bb img {
  flex: none;
  width: 14px;
  height: 14px;
}

.fireSearch .cc {
  flex: none;
  color: var(--brand);
  font-size: 13px;
  transform: none;
}

.fireSearch .dd {
  min-width: 0;
  color: var(--ink-2);
  font-size: 13px;
  transform: none;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
</style>
