<template>
    <div class="follow-anime">
        <div class="aside">
            <span>补番</span>
            <span>12</span>
        </div>
        <div v-if="loadMore" class="class-loading">
               <img src="../img/loading-blue.gif">
               <span>拼命加载中...</span>
        </div>
        <div v-else class="content">
            <div class="title">MoJi推荐的番剧</div>
            <div class="condition">
                <span :class="{activeStatus:status===''}" @click="status=''">全部</span>
                <span :class="{activeStatus:status==='更新至第'}" @click="status='更新至第'">未完结</span>
                <span :class="{activeStatus:status==='全'}" @click="status='全'">已完结</span>
            </div>
            <div class="tags">
                <span :class="{activeTags:tags.includes('全部')}" @click="pushF('全部')">全部</span>
                <span :class="{activeTags:tags.includes('校园')}" @click="pushF('校园')">校园</span>
                <span :class="{activeTags:tags.includes('奇幻')}" @click="pushF('奇幻')">奇幻</span>
                <span :class="{activeTags:tags.includes('轮回')}" @click="pushF('轮回')">轮回</span>
                <span :class="{activeTags:tags.includes('悬疑')}" @click="pushF('悬疑')">悬疑</span>
                <span :class="{activeTags:tags.includes('冒险')}" @click="pushF('冒险')">冒险</span>
                <span :class="{activeTags:tags.includes('成长')}" @click="pushF('成长')">成长</span>
                <span :class="{activeTags:tags.includes('亲情')}" @click="pushF('亲情')">亲情</span>
                <span :class="{activeTags:tags.includes('热血')}" @click="pushF('热血')">热血</span>
                <span :class="{activeTags:tags.includes('异世界')}" @click="pushF('异世界')">异世界</span>
                <span :class="{activeTags:tags.includes('友情')}" @click="pushF('友情')">友情</span>
                <span :class="{activeTags:tags.includes('爱情')}" @click="pushF('爱情')">爱情</span>
                <span :class="{activeTags:tags.includes('推理')}" @click="pushF('推理')">推理</span>
                <span :class="{activeTags:tags.includes('科幻')}" @click="pushF('科幻')">科幻</span>
                <span :class="{activeTags:tags.includes('血腥')}" @click="pushF('血腥')">血腥</span>
                <span :class="{activeTags:tags.includes('机战')}" @click="pushF('机战')">机战</span>
                <span :class="{activeTags:tags.includes('催泪')}" @click="pushF('催泪')">催泪</span>
                <span :class="{activeTags:tags.includes('运动')}" @click="pushF('运动')">运动</span>
                <span :class="{activeTags:tags.includes('反战')}" @click="pushF('反战')">反战</span>
                <span :class="{activeTags:typeSource==='原创'}" @click="pushF2('原创')">原创</span>
                <span :class="{activeTags:typeSource==='小说改'}" @click="pushF2('小说改')">小说改</span>
                <span :class="{activeTags:typeSource==='游戏改'}" @click="pushF2('游戏改')">游戏改</span>
                <span :class="{activeTags:typeSource==='漫画改'}" @click="pushF2('漫画改')">漫画改</span>
            </div>
            <div class="anime-list">
                <followAnimeList :data="animeData"/>
                <div v-show="animeData.length===0" class="no-data">
                    <img src="../img/home_nodata.svg">
                    <div>没有该动漫啊━━━∑(ﾟ□ﾟ*川━</div>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref,reactive, watch, onMounted, onUnmounted } from "vue";
import followAnimeData from '@/utils/followAnimeData';
import followAnimeList from '@/utils/followAnimeList';
import { useGlobalStore } from "@/store/store";

const store = useGlobalStore();
const animeData=reactive([...followAnimeData.AnimeData]);
const status = ref("");
const tags=reactive(["全部"]);
const typeSource=ref("");
const loadMore=ref(true);

onMounted(()=>{
    setTimeout(() => {
        store.setHomeLoad(true,"homeBody");
        loadMore.value=false;
    }, 300);
})

onUnmounted(()=>{
    store.setHomeLoad(false,"homeBody");
})

//添加标签
function pushF(tag){

    if(tag==='全部')
    {
        resetTags();
        return;
    }

    if(!tags.includes(tag))
    {
        if(tags.length===1 && tags[0]==='全部')
          tags.length=0;
        tags.push(tag);
    }
    else{
        tags.splice(tags.indexOf(tag),1);
        if(tags.length===0 && typeSource.value==="")
          resetTags();
    }
}

//添加类型
function pushF2(type){
    if(typeSource.value===type)
    {
        typeSource.value="";
        if(tags.length===0)
          resetTags();
    }
    else
        {   
            typeSource.value=type;
            if(tags.length===1 && tags[0]==='全部')
              tags.length=0;
        }
}

//重置tags
function resetTags(){
    tags.length=0;
    typeSource.value="";
    tags.push('全部');
    animeData.length=0;
    animeData.push(...followAnimeData.AnimeData);
}

//更新数据
function updateData(){
    animeData.length=0;
    if(status.value==="" && tags[0]==='全部')
       animeData.push(...followAnimeData.AnimeData);
    else if(status.value!=="")
    {
        if(tags[0]==='全部') 
         animeData.push(...followAnimeData.AnimeData.filter(item=>item.episodeCount.includes(status.value)));
      else{
        if(typeSource.value==="")
           animeData.push(...followAnimeData.AnimeData.filter(item=>item.episodeCount.includes(status.value) && tags.every(tag=>item.tag.includes(tag))));
        else
           animeData.push(...followAnimeData.AnimeData.filter(item=>item.episodeCount.includes(status.value) && item.tag.includes(typeSource.value) && tags.every(tag=>item.tag.includes(tag))));
      }
    }
    else{
        if(tags[0]==='全部') 
         animeData.push(...followAnimeData.AnimeData.filter(item=>tags.every(tag=>item.tag.includes(tag))));
      else{
        if(typeSource.value==="")
           animeData.push(...followAnimeData.AnimeData.filter(item=>tags.every(tag=>item.tag.includes(tag))));
        else
           animeData.push(...followAnimeData.AnimeData.filter(item=>item.tag.includes(typeSource.value) && tags.every(tag=>item.tag.includes(tag))));
      }
    }
   
}

//监听status变化
watch(status,()=>{
    updateData();
})

//监听tags变化
watch(tags,()=>{
    updateData();
});

//监听typeSource变化
watch(typeSource,()=>{
    updateData();
});
</script>

<style lang="scss" scoped>
*{
    margin: 0;
    padding: 0;
    box-sizing: border-box;
}

.follow-anime{
    position: relative;
    display: flex;
    top: 0px;
    padding: 30px 60px;
    .aside{
    position: sticky;
    user-select: none;
    flex-shrink: 0;
    top: 80px;
    width: 150px;
    height: 52px;
    font-size: 14px;
    margin-right: 16px;
    cursor: pointer;
    border-radius: 6px;
    padding: 14px 16px;
    color: white;
    display: flex;
    z-index: 2000;
    align-items: center;
    justify-content: space-between;
    background-color: #00AEEC;
    }
    .class-loading{
            position: relative;
            width: 100%;
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
    .content{
        position: relative;
        padding-bottom: 30px;
        .title{
        font-size: 24px;
        font-weight: 600;
        color: #18191C;
        line-height: 34px;
        }
        .condition{
        margin-top: 30px;    
        display: flex;
        font-size: 14px;
        column-gap: 12px;
        row-gap: 12px;
        user-select: none;
        flex-wrap: wrap;

        span{
        display: flex;
        align-items: center;
        height: 34px;
        padding: 0 15px;
        flex-shrink: 0;
        color: #61666D;
        cursor: pointer;
        transition: all .3s ease;
        border-radius: 6px;
        background-color: #F6F7F8;
        }
        span:hover{
            color: #00AEEC;
        }
        .activeStatus{
            background-color: #00AEEC !important;
            color: white !important;
        }
        }
        .tags{    
        margin-top: 15px;    
        display: flex;
        font-size: 14px;
        column-gap: 12px;
        row-gap: 12px;
        user-select: none;
        flex-wrap: wrap;

        span{
        display: flex;
        align-items: center;
        height: 34px;
        padding: 0 15px;
        flex-shrink: 0;
        color: #61666D;
        cursor: pointer;
        transition: all .3s ease;
        border-radius: 6px;
        background-color: #F6F7F8;
        }
        span:hover{
            color: #00AEEC;
        }
        .activeTags{
            background-color: #00AEEC !important;
            color: white !important;
        }
        }
        .anime-list{
            margin-top: 15px;

            .no-data{
            width: 100%;
            height: 30vh;
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
    }
    
}

</style>