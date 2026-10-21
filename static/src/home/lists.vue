<template>
    <div class="home-lists">
        <div class="title">{{ store.userId!==null&&store.userId==userId? "我的合集列表" : "TA的合集列表" }}</div>
        <AppEmptyState title="合集功能暂未开放" description="更多内容整理方式，敬请期待"/>
    </div>
</template>

<script setup>
import { useGlobalStore } from "@/store/store";
import { onMounted } from "vue";

const store = useGlobalStore();
const userId = parseInt(new URL(window.location).searchParams.get("userId")) || store.userId;

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
    padding: var(--gap-5) var(--page-pad);
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