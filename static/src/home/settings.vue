<template>
    <div class="settings">
        <div v-show="!loadMore" class="title">隐私设置</div>
        <div v-if="loadMore" class="class-loading">
               <img src="../img/loading-blue.gif">
               <span>拼命加载中...</span>
        </div>
        <div class="content" v-show="checkValue&&!loadMore">
            <div class="item"><span>公开我的收藏</span><el-switch class="custom-switch1" v-model="publicCollect"/> </div>
            <div class="item"><span>公开我的追番追剧</span> <el-switch class="custom-switch1" v-model="publicAnime" /> </div>
            <div class="item"><span>公开最近投币的视频</span> <el-switch class="custom-switch1" v-model="publicCoin"/> </div>
            <div class="item"><span>公开最近点赞的视频</span> <el-switch class="custom-switch1" v-model="publicLove"/> </div>
            <div class="item"><span>公开我的关注列表</span> <el-switch class="custom-switch1" v-model="publicFollowList"/> </div>
            <div class="item"><span>公开我的粉丝列表</span> <el-switch class="custom-switch1" v-model="publicFansList"/> </div>
            <div class="item"><span>公开我的生日、个人标签</span> <el-switch class="custom-switch1" v-model="publicBirthday"/> </div>
        </div>
        <div v-show="!loadMore" class="title">排序设置</div>
        <div v-show="!loadMore" class="sort">
            <div class="item" ref="sort">
                <div><span :sortName="divs[0].class">{{ thumbName(divs[0].class) }}</span><img class="img1" src="../img/排序.png"><img class="img2" src="../img/取消关注白.png"></div>
                <div><span :sortName="divs[1].class">{{ thumbName(divs[1].class) }}</span><img class="img1" src="../img/排序.png"><img class="img2" src="../img/取消关注白.png"></div>
                <div><span :sortName="divs[2].class">{{ thumbName(divs[2].class) }}</span><img class="img1" src="../img/排序.png"><img class="img2" src="../img/取消关注白.png"></div>
                <div><span :sortName="divs[3].class">{{ thumbName(divs[3].class) }}</span><img class="img1" src="../img/排序.png"><img class="img2" src="../img/取消关注白.png"></div>
                <div><span :sortName="divs[4].class">{{ thumbName(divs[4].class) }}</span><img class="img1" src="../img/排序.png"><img class="img2" src="../img/取消关注白.png"></div>
                <div><span :sortName="divs[5].class">{{ thumbName(divs[5].class) }}</span><img class="img1" src="../img/排序.png"><img class="img2" src="../img/取消关注白.png"></div>
            </div>
        </div>
    </div>
</template>

<script setup>
import { onMounted, onUnmounted, ref,reactive, watch } from "vue";
import { useGlobalStore } from "../store/store";
import { putHomeSettings } from "../api/user/index";
import { ElMessage } from "element-plus";
import Sortable from "sortablejs";

const store = useGlobalStore();
const publicCollect = ref(false);
const publicAnime = ref(false);
const publicCoin = ref(false);
const publicLove = ref(false);
const publicFollowList = ref(false);
const publicFansList = ref(false);
const publicBirthday = ref(false);
const sort=ref(null);
const loadMore=ref(true);

const divs = reactive([
    { class: 'magnum-video'},
    { class: 'tribute-video'},
    { class: 'throw-coin-video'},
    { class: 'collect'},
    { class: 'follow-anime'},
    { class: 'love-video'}
]);

onMounted(()=>{
    
    getSort();
    if(store.userId!==null&&store.homeUserInformation.id===store.userId)
    {
        publicCollect.value=store.userInformation.publicCollect?true:false;
        publicAnime.value=store.userInformation.publicAnime?true:false;
        publicCoin.value=store.userInformation.publicCoin?true:false;
        publicLove.value=store.userInformation.publicLove?true:false;  
        publicFollowList.value=store.userInformation.publicFollowList?true:false;
        publicFansList.value=store.userInformation.publicFansList?true:false;
        publicBirthday.value=store.userInformation.publicBirthday?true:false;
        onceFlag=false;
        setTimeout(() => {
        store.setHomeLoad(true,"homeBody");
        loadMore.value=false;
        }, 300);  
    }
    sort.value=Sortable.create(sort.value,{
        animation: 150,
        chosenClass: "sortabel-chosen",
        onEnd: function() {
            const sortArr = [];
            const sortList = sort.value.querySelectorAll(".item div");
            for (let i = 0; i < sortList.length; i++) {
                let sort={};
                sort.class=sortList[i].querySelector("span").getAttribute("sortName");
                sortArr.push(sort);
            }
            localStorage.setItem("homeSort", JSON.stringify(sortArr));
        }
    });
    

})

onUnmounted(()=>{
    store.setHomeLoad(false,"homeBody");
})

//更新设置
function putSettingF() {

    let userInfo2=store.userInformation;
    userInfo2.publicCollect=publicCollect.value?1:0;
    userInfo2.publicAnime=publicAnime.value?1:0;
    userInfo2.publicCoin=publicCoin.value?1:0;
    userInfo2.publicLove=publicLove.value?1:0;
    userInfo2.publicFollowList=publicFollowList.value?1:0;
    userInfo2.publicFansList=publicFansList.value?1:0;
    userInfo2.publicBirthday=publicBirthday.value?1:0;
    
    putHomeSettings(store.token,userInfo2).then(res => {
        if (res.data.code === 0) {
            ElMessage({
            message: "修改失败",
            type: "info",
            plain: true,
            duration: 1700,
            });
        }else if(res.data.code === 1){
            store.setUserInformation(userInfo2);
        }
    });
}

function checkValue(){

    return store.userInformation.publicCollect!==undefined&&
    store.userInformation.publicAnime!==undefined&&
    store.userInformation.publicCoin!==undefined&&
    store.userInformation.publicLove!==undefined&&
    store.userInformation.publicFollowList!==undefined&&
    store.userInformation.publicFansList!==undefined&&
    store.userInformation.publicBirthday!==undefined;
}

let onceFlag=true;
watch(()=>store.userInformation,()=>{

    if(onceFlag&&checkValue())
    {
        publicCollect.value=store.userInformation.publicCollect?true:false;
        publicAnime.value=store.userInformation.publicAnime?true:false;
        publicCoin.value=store.userInformation.publicCoin?true:false;
        publicLove.value=store.userInformation.publicLove?true:false;  
        publicFollowList.value=store.userInformation.publicFollowList?true:false;
        publicFansList.value=store.userInformation.publicFansList?true:false;
        publicBirthday.value=store.userInformation.publicBirthday?true:false;
        onceFlag=false;
        setTimeout(() => {
        store.setHomeLoad(true,"homeBody");
        loadMore.value=false;
        }, 300);  
    }
})

//监听设置
watch(publicCollect,()=>{
    if(!onceFlag)
      putSettingF();
})
watch(publicAnime,()=>{
    if(!onceFlag)
      putSettingF();
})
watch(publicCoin,()=>{
    if(!onceFlag)
      putSettingF();
})
watch(publicLove,()=>{
    if(!onceFlag)
      putSettingF();
})
watch(publicFollowList,()=>{
    if(!onceFlag)
      putSettingF();
})
watch(publicFansList,()=>{
    if(!onceFlag)
      putSettingF();
})
watch(publicBirthday,()=>{
    if(!onceFlag)
      putSettingF();
})

//获取排序
function getSort(){
    const homeSort=JSON.parse(localStorage.getItem('homeSort'));
    if(Array.isArray(homeSort)&&homeSort.length===6){
        divs.length=0;
        Object.assign(divs,homeSort);
    }
}

//转换名字
function thumbName(name){
   if(name==='magnum-video')
    {
       return '代表作';
    }else if(name==='tribute-video')
    {
       return '视频';
    }else if(name==='throw-coin-video')
    {
       return '最近投币的视频';
    }else if(name==='collect')
    {
       return '收藏夹';
    }else if(name==='follow-anime')
    {
       return '订阅追番';
    }else if(name==='love-video')
    {
       return '最近点赞的视频';
    }
}
</script>

<style lang="scss" scoped>
*{
    margin: 0;
    padding: 0;
    box-sizing: border-box;
}

.settings{
    padding: 30px 60px;

    .title{
        font-size: 24px;
        font-weight: 600;
        color: #18191C;
        line-height: 34px;
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
    margin-top: 30px;    
    display: grid;
    column-gap: 60px;
    row-gap: 18px;
    grid-template-columns: repeat(3, 320px);
    margin-bottom: 30px;

    .item{

        display: flex;
        align-items: center;
        justify-content: space-between;
        span{
        display: inline-block;
        font-size: 15.5px;
        line-height: 22px;
        color: #18191C;
        }
        .custom-switch1 {
        width: 30px !important;
        --el-switch-height: 20px; /* 修改高度 */
        --el-switch-width: 30px !important; /* 修改宽度 */
        --el-switch-on-color: #00aeec !important;
        }   
    }
    }   
    .sort{
    border: 1px solid #E3E5E7;
    border-radius: 6px;
    width: 322px;
    margin-top: 30px;
    cursor: grab;

        .item{
            div{
            width: 320px;
            height: 52px;
            display: flex;
            align-items: center;
            user-select: none;

                span{
                margin: 15px 0 15px 16px;
                width: 256px;
                line-height: 22.4px;
                font-weight: 400;
                font-size: 16px;
                color: #18191C;  
                }
                img{
                    width: 18px;
                    height: 13.5px;
                    margin: 14px 14px 14px 11px;
                }

                .img2{
                    display: none;
                }
            }
            div:hover{
                background-color: #00AEEC;
                    span{
                        color: white;
                    }
                    .img1{
                        display: none;
                    }
                    .img2{
                        display: block;
                    }
                
            }
        }
    }
}

.sortabel-chosen{
    opacity: 0;
}

</style>