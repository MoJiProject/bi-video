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
      <img src="../img/搜索.png" alt="搜索" />
    </div>
    <!-- 显示的盒子-->
    <div v-if="showBox && keyWord.length === 0" class="box" @mousedown.stop>
      <!-- 这里可以放置搜索历史记录或其他内容 -->

      <div
        v-show="flag"
        style="
          font-size: 15.5px;
          color: rgb(0, 0, 0);
          margin-bottom:-3px;
          font-weight: 900;
          font-family: Microsoft YaHei UI Light;       
        "
      >
        搜索历史
      </div>
      <div
        v-show="flag && !showMore"
        style="
          transform: translate(0px, -20px);
          max-height: 110px;
          overflow: hidden;
        "
        id="uniqueContentHolder"
      >
        <div @click="ckaeanAllContent" class="cleanAllSearch">清空</div>
        <div style="max-height: 100px">
          <div
            class="searchContentCss"
            v-for="(item, index) in reversedSearchDatas"
            :key="index"
          >
            <img
              src="../img/删除搜索记录.png"
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
        style="transform: translate(0px, -20px);"
        id="uniqueContentHolder"
      >
        <div @click="ckaeanAllContent" class="cleanAllSearch">清空</div>
        <div>
          <div
            class="searchContentCss"
            v-for="(item, index) in reversedSearchDatas"
            :key="index"
          >
            <img
              src="../img/删除搜索记录.png"
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
        <div
          style="
            font-size: 15.5px;
            color: rgb(0, 0, 0);
            margin-top: 3px;
            font-weight: 900;
            font-family: Microsoft YaHei UI Light;       
          "
        >
          bilibili热搜
        </div>
        <ul class="fireSearch">
          <li @click="sendSearchAxios(fireSearch[0]?.word)">
            <span class="aa">1</span>
            <span class="bb"
              >{{ fireSearch[0]?.word }} <img src="../img/热门搜索.png"
            /></span>
          </li>
          <li @click="sendSearchAxios(fireSearch[1]?.word)">
            <span class="aa">2</span>
            <span class="bb"
              >{{ fireSearch[1]?.word }} <img src="../img/热门搜索.png"
            /></span>
          </li>
          <li @click="sendSearchAxios(fireSearch[2]?.word)">
            <span class="aa">3</span>
            <span class="bb"
              >{{ fireSearch[2]?.word }} <img src="../img/热门搜索.png"
            /></span>
          </li>
          <li @click="sendSearchAxios(fireSearch[3]?.word)">
            <span class="aa">4</span>
            <span class="bb">{{ fireSearch[3]?.word }}</span>
          </li>
          <li @click="sendSearchAxios(fireSearch[4]?.word)">
            <span class="aa">5</span>
            <span class="bb">{{ fireSearch[4]?.word }}</span>
          </li>
          <li @click="sendSearchAxios(fireSearch[5]?.word)">
            <span class="aa">6</span>
            <span class="bb">{{ fireSearch[5]?.word }}</span>
          </li>
          <li @click="sendSearchAxios(fireSearch[6]?.word)">
            <span class="aa">7</span>
            <span class="bb">{{ fireSearch[6]?.word }}</span>
          </li>
          <li @click="sendSearchAxios(fireSearch[7]?.word)">
            <span class="aa">8</span>
            <span class="bb">{{ fireSearch[7]?.word }}</span>
          </li>
          <li @click="sendSearchAxios(fireSearch[8]?.word)">
            <span class="aa">9</span>
            <span class="bb">{{ fireSearch[8]?.word }}</span>
          </li>
          <li @click="sendSearchAxios(fireSearch[9]?.word)">
            <span class="aa" style="margin-left:-1.5px; ">10</span>
            <span class="bb">{{ fireSearch[9]?.word }}</span>
          </li>
        </ul>
      </div>
    </div>

    <!-- 显示的盒子 -->
    <div v-show="showBox && keyWord.length !== 0" class="box" @mousedown.stop>
      <div style="transform: translateY(-10px)">
        <ul class="fireSearch">
          <li
            @click="sendSearchAxios(Content + keyWord[0]?.word)"
            v-show="keyWord.length >= 1"
          >
            <span class="cc">{{ Content }}</span>
            <span class="dd">{{ keyWord[0]?.word }}</span>
          </li>
          <li
            @click="sendSearchAxios(Content + keyWord[1]?.word)"
            v-show="keyWord.length >= 2"
          >
            <span class="cc">{{ Content }}</span>
            <span class="dd">{{ keyWord[1]?.word }}</span>
          </li>
          <li
            @click="sendSearchAxios(Content + keyWord[2]?.word)"
            v-show="keyWord.length >= 3"
          >
            <span class="cc">{{ Content }}</span>
            <span class="dd">{{ keyWord[2]?.word }}</span>
          </li>
          <li
            @click="sendSearchAxios(Content + keyWord[3]?.word)"
            v-show="keyWord.length >= 4"
          >
            <span class="cc">{{ Content }}</span>
            <span class="dd">{{ keyWord[3]?.word }}</span>
          </li>
          <li
            @click="sendSearchAxios(Content + keyWord[4]?.word)"
            v-show="keyWord.length >= 5"
          >
            <span class="cc">{{ Content }}</span>
            <span class="dd">{{ keyWord[4]?.word }}</span>
          </li>
          <li
            @click="sendSearchAxios(Content + keyWord[5]?.word)"
            v-show="keyWord.length >= 6"
          >
            <span class="cc">{{ Content }}</span>
            <span class="dd">{{ keyWord[5]?.word }}</span>
          </li>
          <li
            @click="sendSearchAxios(Content + keyWord[6]?.word)"
            v-show="keyWord.length >= 7"
          >
            <span class="cc">{{ Content }}</span>
            <span class="dd">{{ keyWord[6]?.word }}</span>
          </li>
          <li
            @click="sendSearchAxios(Content + keyWord[7]?.word)"
            v-show="keyWord.length >= 8"
          >
            <span class="cc">{{ Content }}</span>
            <span class="dd">{{ keyWord[7]?.word }}</span>
          </li>
          <li
            @click="sendSearchAxios(Content + keyWord[8]?.word)"
            v-show="keyWord.length >= 9"
          >
            <span class="cc">{{ Content }}</span>
            <span class="dd">{{ keyWord[8]?.word }}</span>
          </li>
          <li
            @click="sendSearchAxios(Content + keyWord[9]?.word)"
            v-show="keyWord.length >= 10"
          >
            <span class="cc">{{ Content }}</span>
            <span class="dd">{{ keyWord[9]?.word }}</span>
          </li>
        </ul>
      </div>
    </div>
  </div>
</template>

<script>
import apiClient from "../services/apiClient";
import {
  ref,
  reactive,
  onMounted,
  onBeforeUnmount,
  computed,
  watch,
} from "vue";
import showImg from "../img/展开更多.png";
import fewerImg from "../img/展开更多蓝.png";
import deleteAllSearch from "../img/删除搜索记录.png";
import deleteAllSearchBalack from "../img/删除全部搜索hover.png";
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
    };
    function searchFewer() {
      showMore.value = false;
      showFewerFlag.value = false;
      imgFlag1.value = false;
    };

    //获取热词
    async function selectFireWord() {
      try {
        const response = await apiClient.get("/keyWord/getFireWord");

        if (response.data.code === 1)
          Object.assign(fireSearch, response.data.data);
      } catch (error) {}
    };

    //获取关键字
    let keyWordTIme;
    async function selectKeyWord(Value) {
      try {
        keyWord.length = 0;
        const response = await apiClient.get("/keyWord/getKeyWord", {
          params: {
            keyWord: Value,
          },
        });
          if(keyWordTIme)
          clearTimeout(keyWordTIme);
        if (response.data.code === 1)
          {
            keyWordTIme=setTimeout(() => {
              keyWord.length=0;
            if(Content.value.length>0)
            Object.assign(keyWord, response.data.data);
            }, 800);
          }
      } catch (error) {}
    };

    //添加关键字
    async function addKeyWord() {
      try {
        const response = await apiClient.get("/keyWord/addKeyWord", {
          params: {
            keyWord: Content.value,
          },
        });
      } catch (error) {}
    };

    watch(Content, (newValue) => {
      if (newValue.length === 0) keyWord.length = 0;
      else selectKeyWord(newValue);
    });

    function sendSearchAxios(value) {
      Content.value = value;
      searchVideoAxios();
    };

    //搜索placeholder
    function searchVideoAxios() {
      if (Content.value.length === 0) Content.value = placeholderWord.value;
      const url = `./search?keyword=${encodeURIComponent(
        Content.value,
      )}&classifyIndex=`;
      window.open(url, "_blank"); // '_blank' 表示在新窗口或标签页打开
    };

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
      try {
        const response=await apiClient.get("/keyWord/placeholder");

        if(response.data.code===1)
          placeholderWord.value=response.data.data?.word;

      } catch (error) {

      }
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
      head2Flag,
    };
  },
};
</script>

<style lang="scss" scoped>
* {
  padding: 0;
  margin: 0;
  box-sizing: border-box;
}



.search {
  width: 300px;
  display: flex;
  background-color: rgb(255, 255, 255,0.8);
  align-items: center;
  height: 40px;
  border-radius: 7px; /* 上边角圆角 */
  transition: background-color 0.3s, opacity 0.3s; /* 平滑过渡效果 */
  position: relative; /* 设置为相对定位以便子元素绝对定位 */
  box-shadow: none; /* 去掉阴影效果 */
  z-index: 10000;
  transition: all 0.3s ease;
}

.box{
  border-left: 1px solid #e6e7e9 !important;

}

.headBorderFlag{
  border: 1px solid #e6e7e9;
  background-color: rgba(240, 240, 240, 0.8);

  .box{
    .searchMore{
      transform: translate(115px, -10px);

    }
    .searchFewer{
      transform: translate(123px, -10px);
    }

    .cleanAllSearch{
      margin-left: 275px;
    }


  }
  .search-box{
    width: 84.4%;
  }

  .search-box:focus{
    width: 84.4%;
  }

  .search2{
    transform: translateX(9px);
  }

  .deleteAllSearchImg{
      transform: translate(260px, 0px);
    }

}



.searchShowBox{
  border-radius: 7px 7px 0px 0px;

}


/* 输入框样式 */
.search-box {
  padding: 0 10px;
  width: 87%; /* 使搜索框宽度适应容器 */
  height: 80%;
  margin-top: 0px;
  margin-left: 5px;
  background-color: rgba(255, 255, 255, 0);
  border: none; /* 确保没有边框 */
  border-radius: 7px; /* 左边两个角的圆角 */
  outline: none;
  z-index: 100;
  font-size: 13px;
  text-overflow: ellipsis;
  z-index: 100000;
  padding-right: 40px;
}

/* 输入框聚焦状态 */
.search-box:focus {
  margin-top: 0px;
  text-overflow: ellipsis;
  margin-left: 5px;
  background-color: #d9dcdd; /* 完全不透明的背景 */
  border-radius: 5px;
  opacity: 1; /* 完全不透明 */
  z-index: 10000;
}

/* 当 .search 或其子元素获得焦点时应用样式 */
.search:focus-within {
  background-color: rgba(255, 255, 255, 1); /* 完全不透明的背景 */
  z-index: 10000;
  opacity: 1; /* 完全不透明 */
}

/* 当鼠标悬停在 .search 上时应用样式 */
.search:hover {
  background-color: rgba(255, 255, 255, 1); /* 完全不透明的背景 */
  opacity: 1; /* 完全不透明 */
  z-index: 10000;
}

.searchboxMax{

  background-color: #d9dcdd; /* 完全不透明的背景 */
}

/* 搜索图标样式 */
.search2 {
  margin-right: 5px;
  transform: translate(10.5px,0px);
  display: flex;
  justify-content: center;
  align-items: center;
  cursor: pointer;
  width: 30px;
  height: 31px;
  padding: 5px;
  border-radius: 5px;
  transition: all 0.3s ease;
}

.search2:hover{
  background-color: #d9dcdd;
}

.search2 img {
  width: 16px;
  height: 16px;
}

.headBorderFlag .box{
  width: 337px;
  transform: translate(-1px);
  border: 1px solid #e6e7e9;
  box-shadow: none;
  border-top: none;
}

/* 盒子的样式 */
.box {
  position: absolute; /* 绝对定位使盒子在搜索框下方 */
  top: 100%; /* 紧贴在搜索框的下方 */
  left: 0;
  width: 413px;
  height: auto;
  background-color: white; /* 白色背景 */
  border-radius: 0 0 7px 7px; /* 只保留下边角圆角 */
  box-shadow: 1px 1px rgba(0, 0, 0, 0.2); /* 阴影效果 */
  z-index: 10000; /* 确保盒子在其他元素之上 */
  padding: 18px; /* 内边距 */
  transform: translate(0px, -3px);
  overflow: hidden;
  transition: all 0.3s ease;
}
.box:hover {
  opacity: 1;
}

.searchContentCss {
  display: inline-flex; /* 使用 inline-flex 以便根据内容调整大小 */
  justify-content: center;
  align-items: center; /* 垂直居中 */
  line-height: 20px;
  border-radius: 5px;
  background-color: #f6f7f8;
  max-width: 37%; /* 允许最大宽度 */
  width: auto; /* 自动根据内容宽度调整 */
  cursor: pointer;
  padding: 10px; /* 添加内边距 */
  box-sizing: border-box; /* 包含内边距在总宽度内 */
  transform: translate(-10px, 15px);
  height: 28px;
  margin-left: 10px;
  margin-bottom: 10px;
}

.searchContentCss .searchContentFontCss {
  display: -webkit-box; /* 使用 flexbox 布局 */
  -webkit-box-orient: vertical;
  text-overflow: ellipsis;
  -webkit-line-clamp: 1;
  text-align: center;
  max-width: 90px;
  word-wrap: break-word;
  overflow: hidden;
  font-size: 11.5px;
  color: black;
  height: 24px; /* 可以适当调整高度 */
  transition: all 0.3s ease;
}

.searchContentCss:hover .searchContentFontCss {
  color: #00aeec;
}

.deleteSearchCss {
  position: absolute;
  width: 12px;
  right: -10px;
  top: -5px;
  visibility: hidden;
}

.searchContentCss:hover .deleteSearchCss {
  visibility: visible;
}

.searchMore {
  cursor: pointer;
  font-size: 11.5px;
  color: #9499a0;
  transform: translate(158px, -14px);
}

.searchFewer:hover {
  color: #00aeec;
}

.searchFewer {
  cursor: pointer;
  font-size: 12px;
  color: #9499a0;
  transform: translate(164px, -14px);
  margin-top: 10px;
  transition: all 0.3s ease;
}

.searchMore:hover {
  color: #00aeec;
}

.fireSearch {
  list-style-type: none;
}
.fireSearch li {
  font-size: 14px;
  height: 38px;
  width: 110%;
  transform: translate(-10px);
  padding: 10px 0px 0px 10px;
  cursor: pointer;
  transition: all 0.3s ease;
}
.fireSearch li .aa {
  position: absolute;
  transform: translate(3px);
}
.fireSearch li .bb {
  position: absolute;
  transform: translate(25px);
  color: black;
}

li:nth-child(-n + 3) .bb img {
  width: 14px;
  transform: translateY(1px);
}
/* 控制后七个 li */
li:nth-last-child(-n + 6) .aa {
  color: #95999f;
}

.fireSearch li:hover {
  background-color: #e3e5e7;
}

.fireSearch .cc {
  color: #e1678d;
  transform: translateX(30px);
}

.fireSearch .dd {
  transform: translateX(29px);
}

.cleanAllSearch {
  display: inline;
  font-size: 11.5px;
  color: rgb(171, 171, 171);
  margin-left: 353px;
  cursor: pointer;
  transition: all 0.3s ease;
}


.cleanAllSearch:hover {
  color: #00aeec;
}

.search-icon-box{
  z-index: 1000000;
  transition: all 0.3s ease;
}
.search-icon-box:hover{
  padding: 10px;
  background-color: #e1678d;
}

.deleteAllSearchImg
{
  width: 13.5px;
  transform: translate(340px, 0px);
  position: absolute;
  z-index: 10000000000;
  cursor: pointer;
}
</style>
