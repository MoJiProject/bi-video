<template>
  <div  v-if="onloadPage" class="main">
    <div class="head2" v-show="isHead2Visible&&!store.loginDialogVisible" :class="{ head2Animate: isHead2Visible }">
      <head1 :head2Flag="true" />
    </div>
      <classify2 v-show="isClissify2Visible&&!store.loginDialogVisible" :class="{ isClissify2Visible: isClissify2Visible }"/>
  <div class="index">
    <div class="head1" @mousemove="handleMouseMove">
      <head1/>
    </div>
    <banner style="z-index: 100;height: 155px;position: absolute;"/>
    <classify/>
    <carousel style="margin-top: 20px;"/>
    <videos/>
  </div>
  <div style="position: fixed;top: 800px;">
      <el-backtop :right="5"/>
  </div>
  </div>

</template>

<script>
import head1 from './mainHead.vue';
import banner from './banner.vue';
import classify from './classify.vue';
import classify2 from './classify2.vue';
import videos from './videos.vue';
import apiClient from "../services/apiClient";
import carousel from './carousel.vue';
import {useGlobalStore} from "../store/store";
import { onMounted, ref,onUnmounted,} from 'vue';
export default {
  name: 'index',
  components: {
    head1,
    banner,
    classify,
    classify2,
    carousel,
    videos,

  },

  setup(){

    const store = useGlobalStore();
    const onloadPage=ref(false);
    const isHead2Visible=ref(false);
    const isClissify2Visible=ref(false);
    onMounted(() => {

        onloadPage.value=true;
        setTimeout(() => {
          window.scrollTo(0, 0);
        }, 0);
        window.addEventListener("scroll",handleScroll);
        getPlaceholder();
    });

    onUnmounted(()=>{
      window.removeEventListener("scroll",handleScroll);
    })

    const handleScroll = () => {
      const scrollPosition = window.scrollY; // 当前滚动距离
      isHead2Visible.value = scrollPosition >= 30;
      isClissify2Visible.value = scrollPosition >= 180;
    };

    //获取placeholder
    async function getPlaceholder(){
      const response=await apiClient.get("/keyWord/placeholder");

      if(response.data.code===1)
        store.setPlaceholderWord(response.data.data.word);
    } 

    //鼠标移动事件
    function handleMouseMove(event) {
      store.setMouseX(event.clientX);
    }


    


    return{
      onloadPage,
      isHead2Visible,
      isClissify2Visible,
      store,
      handleMouseMove,

    }

  }



}
</script>

<style lang="scss" scoped>

 .main{
  position: relative;
  display: flex;
  justify-content: center;
 }
 .index{
  position: relative;
  left: 37.5px;
  user-select: none;
  height: auto;
  min-height: 110vh;
  padding-bottom: 200px;
 }


 .head2{
  position: fixed;
  height: 64px;
  border-bottom: 1px solid rgb(231, 231, 231);
  transform: translate(0px,-63px);
  left: -53px;
  z-index: 1000001;
  background-color: white;
  width: 102%;
 }

 .head2Animate{
  animation: head2Animate 0.5s ease-in-out forwards;
 }

 .head1{
  position: relative;
  left: -40px;
  z-index: 1000;
 }

 @keyframes head2Animate {
  0% {
    transform: translate(40px,-63px);
    opacity: 0;
  }
  100% {
    transform: translate(40px,0px);
    opacity: 1;
  }
 }

.isClissify2Visible{
  animation: classifyAnimate 0.5s ease forwards;
}

@keyframes classifyAnimate {
  0% {
    transform: translate(0px,0px);
    opacity: 0;
  }
  100% {
    transform: translate(0px,119px);
    opacity: 1;
  }
}


</style>
