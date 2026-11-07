<template>
  <div ref="homePageRef" v-if="ready && userId !== null" class="home-page" :style="homePageStyle">
    <loadingBar :active="barActive" :percent="barPercent" :min-duration="initialLoadFinished?260:400"/>
    <home-head/>
    <homeAside/>
    <homeBody/>
    <el-backtop class="home-page-backtop" :right="24"/>
  </div>
    
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue';
import {useGlobalStore} from "../store/store";
import { authApi } from "../api/product";
import { ElMessage } from 'element-plus';
import homeHead from './homeHead.vue';
import homeAside from './homeAside.vue';
import homeBody from './homeBody.vue';
import loadingBar from '@/components/loadingBar.vue';
import { useBodyLayout } from '@/composables/useBodyLayout';

const store = useGlobalStore();
const initialLoadFinished = ref(false);
const menuLoading = ref(false);
const homePageRef = ref(null);
const preservedPageHeight = ref(0);
const urlUserId = parseInt(new URL(window.location).searchParams.get("userId")) || null;
const userId = computed(() => urlUserId || store.userId);
const ready = ref(false);
const homePageStyle = computed(() => preservedPageHeight.value
  ? { minHeight: `${preservedPageHeight.value}px` }
  : undefined);

let previousScrollY = 0;
let previousDocumentHeight = 0;
let pageResizeObserver = null;
let scrollRestoreFrame = 0;
let resizeCheckFrame = 0;

function getDocumentHeight() {
  return Math.max(document.documentElement.scrollHeight, document.body.scrollHeight);
}

function preserveHomeScroll() {
  const page = homePageRef.value;
  if (!page) return;

  const documentHeight = getDocumentHeight();
  const scrollY = window.scrollY;
  const pageTop = page.getBoundingClientRect().top + scrollY;

  if (documentHeight < previousDocumentHeight && previousScrollY > 0) {
    const scrollYToRestore = previousScrollY;
    preservedPageHeight.value = Math.max(
      preservedPageHeight.value,
      previousDocumentHeight - pageTop,
      previousScrollY + window.innerHeight - pageTop,
      0,
    );
    if (scrollY < scrollYToRestore) {
      nextTick(() => {
        cancelAnimationFrame(scrollRestoreFrame);
        scrollRestoreFrame = requestAnimationFrame(() => {
          if (preservedPageHeight.value && window.scrollY < scrollYToRestore) {
            window.scrollTo(0, scrollYToRestore);
          }
        });
      });
    }
  } else if (
    preservedPageHeight.value &&
    documentHeight > pageTop + preservedPageHeight.value + 1
  ) {
    preservedPageHeight.value = 0;
  }

  previousScrollY = window.scrollY;
  previousDocumentHeight = Math.max(
    getDocumentHeight(),
    pageTop + preservedPageHeight.value,
  );
}

function handleHomeScroll() {
  const documentHeight = getDocumentHeight();
  if (
    preservedPageHeight.value &&
    window.scrollY === 0 &&
    documentHeight >= previousDocumentHeight
  ) {
    preservedPageHeight.value = 0;
  }
  if (documentHeight >= previousDocumentHeight) {
    previousScrollY = window.scrollY;
    previousDocumentHeight = documentHeight;
  }
}

function scheduleHomeResizeCheck() {
  if (resizeCheckFrame) return;
  resizeCheckFrame = requestAnimationFrame(() => {
    resizeCheckFrame = 0;
    preserveHomeScroll();
  });
}

useBodyLayout({});

// 头部与侧栏先完成，正文占剩余权重，整体不会过早显示为 100%
const loadPercent = computed(() => {
  const load = store.homeLoad;
  if (load.homeHead && load.homeAside && load.homeBody)
    return 100;
  let percent = 0;
  percent += load.homeHead ? 25 : 0;
  percent += load.homeAside ? 20 : 0;
  percent += load.homeBody ? 55 : 0;
  return percent;
});

// 首屏显示整体进度，切换子页面只跟踪正文加载
const barActive = computed(() => !initialLoadFinished.value || menuLoading.value);
const barPercent = computed(() => {
  if (!initialLoadFinished.value)
    return loadPercent.value;
  return store.homeLoad.homeBody ? 100 : 70;
});

const handlePopState = () => {
  const url = new URL(window.location.href);
  let homeMenu = parseInt(url.searchParams.get("homeMenu")) || 1;
  if (homeMenu === 4) {
    homeMenu = 1;
    url.searchParams.set("homeMenu", "1");
    window.history.replaceState(window.history.state, "", url);
  }
  store.setHomeMenu(homeMenu, userId.value, false);
};

onMounted(async () => {
  if (store.token && !store.userId) {
      try {
        const me = await authApi.me();
        store.setUserId(me.userId);
      } catch (error) {
        ElMessage.error(error.message);
        if (!urlUserId) {
          window.location.href = './';
          return;
        }
      }
  } else if (!store.token && store.userId) {
    store.setUserId(null);
  }
  if (!userId.value) {
      window.location.href = './';
      return;
  }
  handlePopState();
  ready.value = true;
  await nextTick();
  previousScrollY = window.scrollY;
  previousDocumentHeight = getDocumentHeight();
  if (typeof ResizeObserver !== 'undefined' && homePageRef.value) {
    pageResizeObserver = new ResizeObserver(scheduleHomeResizeCheck);
    pageResizeObserver.observe(homePageRef.value);
  }
  window.addEventListener('scroll', handleHomeScroll, { passive: true });
  window.addEventListener('popstate', handlePopState);
  
});

onBeforeUnmount(()=>{
  cancelAnimationFrame(scrollRestoreFrame);
  cancelAnimationFrame(resizeCheckFrame);
  pageResizeObserver?.disconnect();
  window.removeEventListener('scroll', handleHomeScroll);
  window.removeEventListener('popstate', handlePopState);
});

watch(()=>[store.homeLoad.homeHead,store.homeLoad.homeAside,store.homeLoad.homeBody],()=>{
    if(!initialLoadFinished.value&&store.homeLoad.homeHead&&store.homeLoad.homeAside&&store.homeLoad.homeBody)
        initialLoadFinished.value=true;
},{immediate:true});

// 切换子页面时重新显示进度条，正文加载完成后再结束
watch(()=>store.homeMenu,()=>{
    if(!initialLoadFinished.value)
        return;
    menuLoading.value=!store.homeLoad.homeBody;
});

watch(()=>store.homeLoad.homeBody,(value)=>{
    if(value)
        menuLoading.value=false;
});


</script>

<style lang="scss" scoped>
*{	
    margin: 0;
    padding: 0;
    box-sizing: border-box;
}

.home-page{
    position: relative;
    width: 100%;
}

.home-page-backtop{
    position: fixed;
    right: 24px;
    bottom: 120px;
    z-index: 700;
}

</style>
<style lang="scss" scoped src="./homeLayout.scss"></style>
