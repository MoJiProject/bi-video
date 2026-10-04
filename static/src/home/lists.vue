<template>
    <div class="home-lists">
        <div class="title">{{ store.userId!==null&&store.userId==userId? "我的合集列表" : "TA的合集列表" }}</div>
        <div class="no-data">
            <img src="/img/home_nodata.svg">
            <div>暂无数据</div>
        </div>
    </div>
</template>

<script setup>
import { useGlobalStore } from "@/store/store";
import { onMounted } from "vue";

const store = useGlobalStore();
const userId = parseInt(new URL(window.location).searchParams.get("userId")) || null;

onMounted(()=>{
    store.setHomeLoad(true,'homeBody');
    store.setHomeLoadMenuList(4);
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
    color: #1C2321;
    line-height: 34px;
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
                color: #8D9794;
                
            }
    }
}

</style>