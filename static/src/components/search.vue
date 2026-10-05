<template>
  <div
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
            :class="{searchboxMax:showBox}"
            :placeholder="placeholderWord"
            @focus="showBox = true"
            maxlength="30"
            v-model="Content"
            @keyup.enter="jumpSearch"
          />
          </el-tooltip>

      <img
      v-show="Content.length>0"
      @mouseover="deleteAllSearchFlag = false"
      @mouseleave="deleteAllSearchFlag = true"
      @click="Content = ''"
      :src="deleteAllSearchFlag ? deleteAllSearch : deleteAllSearchBalack"
      class="deleteAllSearchImg"
    />

    <div class="search2" @click="jumpSearch">
      <img src="/img/搜索.png" alt="搜索" />
    </div>
    <!-- 显示的盒子-->
    <div v-if="showBox && keyWord.length === 0" class="box" @mousedown.stop>
      <!-- 这里可以放置搜索历史记录或其他内容 -->

      <div
        v-show="flag"
        class="box-title"
      >
        搜索历史
      </div>
      <div
        v-show="flag && !showMore"
        class="box-body is-clamped"
      >
        <div @click="ckaeanAllContent" class="cleanAllSearch">清空</div>
        <div class="history-chips">
          <div
            class="searchContentCss"
            v-for="(item, index) in reversedSearchDatas"
            :key="index"
          >
            <img
              src="/img/删除搜索记录.png"
              class="deleteSearchCss"
              @click="deleteSearchContent(index)"
            />
            <div
              class="searchContentFontCss"
              @click="sendSearchAxios(item.value)"
            >
              {{ item.value }}
            </div>
          </div>
        </div>
      </div>

      <div
        v-show="flag && showMore"
        class="box-body"
      >
        <div @click="ckaeanAllContent" class="cleanAllSearch">清空</div>
        <div class="history-chips">
          <div
            class="searchContentCss"
            v-for="(item, index) in reversedSearchDatas"
            :key="index"
          >
            <img
              src="/img/删除搜索记录.png"
              class="deleteSearchCss"
              @click="deleteSearchContent(index)"
            />
            <div class="searchContentFontCss">{{ item.value }}</div>
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
import apiClient from "../services/apiClient";
import { searchApi } from "../api/product";
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
const deleteAllSearch = "/img/删除搜索记录.png"
const deleteAllSearchBalack = "/img/删除全部搜索hover.png"
import {useGlobalStore} from "../store/store";
export default {
  name: "search",
  props:{
    head2Flag:{
      default:false,
      type:Boolean
    },
  },
  setup(props) {
    const head2Flag=ref(props.head2Flag);
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
    const deleteAllSearchFlag = ref(true);
    const store = useGlobalStore();
    const handleClickInside = (event) => {
      // 判断点击是否在搜索框及其子元素内
      const searchElement = document.querySelector(".search");
      if (searchElement && !searchElement.contains(event.target)) {
        showBox.value = false;
      }
    };

    const uniqueContentHolder = ref(null); // 引用盒子
    onMounted(() => {
      document.addEventListener("mousedown", handleClickInside);
      selectFireWord();
      selectSearchContent();
      getBoxHeight();
      document.addEventListener("scroll",handleScroll);

        setTimeout(() => {
          placeholderWord.value=store.placeholderWord;
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
      } catch (error) {}
    }
    //获取关键字
    let keyWordTIme;
async function selectKeyWord(Value) {
      if (!Value) {
        keyWord.length = 0;
        return;
      }
      try {
        const list = await searchApi.hot(20);
        keyWord.length = 0;
        Object.assign(keyWord, list.map((k) => k.keyword).filter((w) => w.includes(Value)));
      } catch (error) {}
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
      if (Content.value.length === 0) Content.value = placeholderWord.value;
      const url = `./search?keyword=${encodeURIComponent(
        Content.value,
      )}&classifyIndex=`;
      window.open(url, "_blank"); // '_blank' 表示在新窗口或标签页打开
    }

    //监视不显示时关闭搜索框
    function handleScroll(){

      const scrollPosition = window.scrollY; // 当前滚动距离

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
      deleteAllSearch,
      deleteAllSearchBalack,
      deleteAllSearchFlag,
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
  position: relative;
  display: flex;
  align-items: center;
  width: 100%;
  height: 40px;
  border-radius: var(--radius-md);
  background-color: var(--fill);
  border: 1px solid var(--line);
  z-index: 10000;
  transition: background-color .2s, border-color .2s, box-shadow .2s;
}

.search:hover,
.search:focus-within {
  background-color: #fff;
  border-color: var(--brand-light-5);
  box-shadow: 0 0 0 3px var(--brand-soft);
}

.search.showBox {
  background-color: #fff;
  border-radius: var(--radius-md) var(--radius-md) 0 0;
  border-bottom-color: transparent;
  box-shadow: none;
}

/* 输入框：唯一伸缩项 */
.search-box {
  flex: 1 1 auto;
  min-width: 0;
  height: 100%;
  margin: 0;
  padding: 0 8px 0 12px;
  background: transparent;
  border: none;
  outline: none;
  color: var(--ink);
  font-size: 13px;
}

/* 清空按钮：贴输入框右缘，跟随输入宽度，不写死 translate */
.deleteAllSearchImg {
  flex: none;
  width: 14px;
  height: 14px;
  margin-right: 4px;
  cursor: pointer;
  transform: none;
}

/* 搜索按钮 */
.search2 {
  flex: none;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 44px;
  height: 100%;
  margin: 0;
  padding: 0;
  border-radius: 0 var(--radius-md) var(--radius-md) 0;
  cursor: pointer;
  transform: none;
  transition: background-color .2s;
}

.search.showBox .search2 {
  border-radius: 0;
}

.search2:hover {
  background-color: var(--brand-soft);
}

.search2 img {
  width: 16px;
  height: 16px;
}

/* 下拉面板：宽度跟随输入框，不再写死 337 / 413px */
.box {
  position: absolute;
  top: 100%;
  left: -1px;
  width: calc(100% + 2px);
  max-height: 420px;
  padding: 16px;
  background-color: #fff;
  border: 1px solid var(--line);
  border-top: none;
  border-radius: 0 0 var(--radius-md) var(--radius-md);
  box-shadow: var(--shadow-3);
  z-index: 10000;
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
  max-width: 190px;
  height: 28px;
  padding: 0 10px;
  border-radius: var(--radius-sm);
  background-color: #f4f7f6;
  cursor: pointer;
  transform: none;
  margin: 0;
  transition: background-color .2s;
}

.searchContentCss:hover {
  background-color: var(--brand-soft);
}

.searchContentCss .searchContentFontCss {
  overflow: hidden;
  color: var(--ink);
  font-size: 12px;
  line-height: 28px;
  white-space: nowrap;
  text-overflow: ellipsis;
}

.searchContentCss:hover .searchContentFontCss {
  color: var(--brand);
}

.deleteSearchCss {
  position: absolute;
  top: -6px;
  right: -6px;
  width: 12px;
  cursor: pointer;
  visibility: hidden;
}

.searchContentCss:hover .deleteSearchCss {
  visibility: visible;
}

/* 清空 / 展开 / 收起：行内流式排布 */
.cleanAllSearch {
  display: inline-block;
  margin: 0 0 8px;
  color: var(--ink-3);
  font-size: 12px;
  cursor: pointer;
  transition: color .2s;
}

.cleanAllSearch:hover,
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
