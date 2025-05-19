<template>
    <div class="home-lists">
        <div class="title">{{ store.userId!==null&&store.userId==userId? "我的合集列表" : "TA的合集列表" }}</div>
        <div v-if="loadMore" class="class-loading">
               <img src="../img/loading-blue.gif">
               <span>拼命加载中...</span>
        </div>
        <div v-else class="no-data">
            <img src="../img/home_nodata.svg">
            <div>暂无数据</div>
        </div>
    </div>
</template>

<script setup>
import { useGlobalStore } from "@/store/store";
import { onMounted, onUnmounted, ref } from "vue";

const loadMore=ref(true);
const store = useGlobalStore();
const userId = parseInt(new URL(window.location).searchParams.get("userId")) || null;

onMounted(()=>{
    setTimeout(() => {
    store.setHomeLoad(true,'homeBody');
    loadMore.value=false;
    }, 300);
})

onUnmounted(()=>{
    store.setHomeLoad(false,"homeBody");
})

</script>

<style lang="scss" scoped>
*{
    margin: 0;
    padding: 0;
    box-sizing: border-box;
}

.home-lists{
    padding-left: 60px;
    padding-top: 30px;
    .title{
    font-size: 24px;
    font-weight: 600;
    color: #18191C;
    line-height: 34px;
    }
    .class-loading{
            position: relative;
            margin-top: 60px;
            display: flex;
            justify-content: center;

            img{
                width: 30px;
                height: 30px;
            }
            span{
                position: relative;
                top: 5px;
                left: 5px;
                font-size: 14px;
                color: #61666D;
            }
    }
    .no-data{
        width: 100%;
        height: 50vh;
        user-select: none;
        display: flex;
        justify-content: center;
        align-items: center;
        flex-direction: column;  
            img{
                width: 140px;
                height: 140px;
            }
            div{
                margin-top: 6px;
                font-size: 14px;
                line-height: 20px;
                font-weight: 400;
                color: #9499A0;
                
            }
    }
}

</style>