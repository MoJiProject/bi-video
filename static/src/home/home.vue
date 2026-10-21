<template>
    <div class="home" :class="{ 'without-profile': !showProfile }">
        <div v-if="!loadMore && showProfile" class="home-user-info">
            <div>个人资料
                <span v-if="store.homeUserInformation.id === store.userId" @click="openAccount">
                    编辑 
                    <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1024 1024">
                        <path fill="currentColor" d="M340.864 149.312a30.592 30.592 0 0 0 0 42.752L652.736 512 340.864 831.872a30.592 30.592 0 0 0 0 42.752 29.12 29.12 0 0 0 41.728 0L714.24 534.336a32 32 0 0 0 0-44.672L382.592 149.376a29.12 29.12 0 0 0-41.728 0z"></path>
                    </svg>
                </span>
            </div>
            <div><img src="/img/uid.png"> {{ store.homeUserInformation.id }}</div>
            <div v-if="store.homeUserInformation.birthday"><img src="/img/birthday.png"> {{ store.homeUserInformation?.birthday?.split(' ')[0] }}</div>
        </div>
        <div class="home-content">
        <!-- 单独渲染加载中部分 -->
        <loadingIndicator v-show="loadMore" min-height="180px"/>
        <!-- 使用 v-for 遍历排序后的数组来渲染其他 div -->
        <div class="home-div" v-for="(item, index) in divs" :key="index">
            <div v-if="item.class === 'magnum-video' && !loadMore && (featuredVideos.length || store.userId === userId)"
                class="magnum-video">
                <div class="title">
                    <span>代表作</span>
                    <el-button v-if="store.userId === userId" text @click="openFeatured">设置代表作</el-button>
                </div>
                <videoList :data="featuredVideos"/>
                <p v-if="!featuredVideos.length" class="featured-empty">选择最多 3 个已发布的视频展示在首页</p>
            </div>
            <!-- 视频部分 -->
            <div :class="item.class" v-if="item.class === 'tribute-video'&&!loadMore">
                <div class="title">
                    <div class="title-name" @click="store.setHomeMenu(3,userId,true)">视频</div> · &nbsp;
                    <span class="contribute-total">{{ contributeTotal }}</span>
                    <div class="video-sort">
                        <span :class="{sortClass: sort === 1}" @click="sort = 1">最新发布</span>
                        <span :class="{sortClass: sort === 2}" @click="sort = 2">最多播放</span>
                        <span :class="{sortClass: sort === 3}" @click="sort = 3">最多收藏</span>   
                    </div>
                    <div class="more" @click="store.setHomeMenu(3,userId,true)">
                        查看更多
                        <svg  xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1024 1024">
                            <path fill="currentColor" d="M340.864 149.312a30.592 30.592 0 0 0 0 42.752L652.736 512 340.864 831.872a30.592 30.592 0 0 0 0 42.752 29.12 29.12 0 0 0 41.728 0L714.24 534.336a32 32 0 0 0 0-44.672L382.592 149.376a29.12 29.12 0 0 0-41.728 0z"></path>
                        </svg>
                    </div>
                </div>
                <videoList :data="contributeVideos"/>
            </div>
            <!-- 最近投币的视频部分 -->
            <div :class="item.class" v-if="item.class === 'throw-coin-video'&&!loadMore && ((store.userId!== null && store.userId === userId) || store.homeUserInformation.publicCoin === 1)">
                <div class="title">
                    <span>最近投币的视频</span>
                    <span v-if="store.userId===store.homeUserInformation.id&&store.homeUserInformation.publicCoin===0" class="home-user-info-public"><img src="/img/隐藏.png">仅自己可见</span> 
                </div>
                <videoList2 :data="throwCoinVideos"/>
            </div>
            <!-- 收藏夹部分 -->
            <div :class="item.class" v-if="item.class === 'collect'&&!loadMore && ((store.userId!== null && store.userId === userId) || store.homeUserInformation.publicCollect === 1)">
                <div class="title">
                    <div class="title-name" @click="store.setHomeMenu(5,userId,true)">收藏夹</div> · &nbsp;
                    <span class="contribute-total">{{ collectTotal }}</span>
                    <span v-if="store.userId===store.homeUserInformation.id&&store.homeUserInformation.publicCollect===0" class="home-user-info-public"><img src="/img/隐藏.png">仅自己可见</span> 
                    <div class="more" @click="store.setHomeMenu(5,userId,true)">
                        查看更多
                        <svg  xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1024 1024">
                            <path fill="currentColor" d="M340.864 149.312a30.592 30.592 0 0 0 0 42.752L652.736 512 340.864 831.872a30.592 30.592 0 0 0 0 42.752 29.12 29.12 0 0 0 41.728 0L714.24 534.336a32 32 0 0 0 0-44.672L382.592 149.376a29.12 29.12 0 0 0-41.728 0z"></path>
                        </svg>
                    </div>
                </div>
                <div class="collect-list">
                    <div class="video-item" v-for="collect in collects" :key="collect.id">
                        <div class="collect-back"></div>
                        <div class="collect-back2"></div>
                        <div class="content" @click="store.setHomeMenu(5,userId,true,collect.id)">
                            <img :src="collect.coverAddress?collect.coverAddress:'/img/collect-cover.png'">
                            <svg v-if="collect.status === 0" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1024 1024">
                                <path fill="currentColor" d="M224 448a32 32 0 0 0-32 32v384a32 32 0 0 0 32 32h576a32 32 0 0 0 32-32V480a32 32 0 0 0-32-32zm0-64h576a96 96 0 0 1 96 96v384a96 96 0 0 1-96 96H224a96 96 0 0 1-96-96V480a96 96 0 0 1 96-96"></path>
                                <path fill="currentColor" d="M512 544a32 32 0 0 1 32 32v192a32 32 0 1 1-64 0V576a32 32 0 0 1 32-32m192-160v-64a192 192 0 1 0-384 0v64zM512 64a256 256 0 0 1 256 256v128H256V320A256 256 0 0 1 512 64"></path>
                            </svg>
                            <div class="video-number">{{ collect.videoNumber }}个视频</div> 
                            <div class="collect-info">
                                <div class="name">{{ collect.collectName }}</div>
                                <div class="status">{{ collect.status === 1?'公开':'私密' }}</div>
                            </div>
                        </div>
                    </div>
                </div>        
            </div>
            <!-- 最近点赞的视频部分 -->
            <div :class="item.class" v-if="item.class === 'love-video'&&!loadMore && ((store.userId!== null && store.userId === userId) || store.homeUserInformation.publicLove === 1)">
                <div class="title">
                    <span>最近点赞的视频</span>
                    <span v-if="store.userId===store.homeUserInformation.id&&store.homeUserInformation.publicLove===0" class="home-user-info-public"><img src="/img/隐藏.png">仅自己可见</span> 
                </div>
                <videoList2 :data="loveVideos"/>
            </div>
        </div>
            <el-dialog v-model="featuredVisible" title="设置代表作（最多 3 个）" width="min(90vw, 720px)">
                <div class="featured-tools">
                    <el-input v-model="featuredKeyword" placeholder="搜索自己的视频" maxlength="50" clearable
                        @keyup.enter="searchFeatured" @clear="searchFeatured"/>
                    <el-button @click="searchFeatured">搜索</el-button>
                </div>
                <p>已选择 {{ featuredSelection.length }} / 3，可取消全部代表作。</p>
                <loadingIndicator v-if="featuredLoading" min-height="120px"/>
                <div v-else class="featured-options">
                    <el-checkbox v-for="video in featuredOptions" :key="video.id"
                        :model-value="featuredSelection.includes(video.id)"
                        :disabled="featuredSaving || (!featuredSelection.includes(video.id) && featuredSelection.length >= 3)"
                        @change="selectFeatured(video.id, $event)">{{ video.title }}</el-checkbox>
                    <p v-if="!featuredOptions.length">暂无匹配视频</p>
                </div>
                <el-pagination v-if="featuredTotal" v-model:current-page="featuredPage" :page-size="20"
                    layout="prev, pager, next" :total="featuredTotal" :disabled="featuredSaving"/>
                <template #footer>
                    <el-button :disabled="featuredSaving" @click="featuredVisible = false">取消</el-button>
                    <el-button type="primary" :loading="featuredSaving" @click="saveFeatured">保存</el-button>
                </template>
            </el-dialog>
        </div>
    </div>
</template>

<script setup>
import { computed, reactive, ref, watch } from 'vue';
import {useGlobalStore} from "@/store/store";
import videoList from './homeVideos.vue';
import videoList2 from './homeVideos.vue';
import loadingIndicator from '@/components/loadingIndicator.vue';
import {homeContributeVideos,homeThrowCoinVideos,getCollectsClassify,homeLoveVideos,homeMagnumVideos,updateFeaturedVideos} from '@/api/home/index';
import { ElMessage } from 'element-plus';

const store = useGlobalStore();
const userId = parseInt(new URL(window.location).searchParams.get("userId")) || store.userId;
const showProfile = computed(() => store.userId === userId || store.homeUserInformation.publicBirthday === 1);
const contributeVideos=reactive([]);
const sort=ref(1);
const contributeTotal=ref(0);
const throwCoinVideos=reactive([]);
const collects=reactive([]);
const collectTotal=ref(0);
const loveVideos=reactive([]);
const loadMore=ref(true);
const featuredVideos = ref([]);
const featuredVisible = ref(false);
const featuredSelection = ref([]);
const featuredOptions = ref([]);
const featuredKeyword = ref('');
const featuredAppliedKeyword = ref('');
const featuredPage = ref(1);
const featuredTotal = ref(0);
const featuredLoading = ref(false);
const featuredSaving = ref(false);
let featuredRequestId = 0;

async function openFeatured() {
    featuredSelection.value = featuredVideos.value.map(video => video.id);
    featuredVisible.value = true;
    featuredKeyword.value = '';
    featuredAppliedKeyword.value = '';
    featuredPage.value = 1;
    await loadFeaturedOptions();
}
function selectFeatured(id, selected) {
    featuredSelection.value = selected ? [...new Set([...featuredSelection.value, id])]
        : featuredSelection.value.filter(videoId => videoId !== id);
}
function searchFeatured() {
    featuredPage.value = 1;
    featuredAppliedKeyword.value = featuredKeyword.value.trim();
}
async function loadFeaturedOptions() {
    const current = ++featuredRequestId;
    featuredLoading.value = true;
    try {
        const response = await homeContributeVideos(userId, store.userId, 1, featuredPage.value, featuredAppliedKeyword.value);
        if (current !== featuredRequestId) return;
        featuredOptions.value = response.data.data.records;
        featuredTotal.value = response.data.data.total;
    } catch (error) { if (current === featuredRequestId) ElMessage.error(error.message); }
    finally { if (current === featuredRequestId) featuredLoading.value = false; }
}
watch([featuredPage, featuredAppliedKeyword], () => { if (featuredVisible.value) loadFeaturedOptions(); });
async function saveFeatured() {
    featuredSaving.value = true;
    try {
        await updateFeaturedVideos(featuredSelection.value);
        const response = await homeMagnumVideos(userId);
        featuredVideos.value = response.data.data.records;
        featuredVisible.value = false;
        ElMessage.success('代表作已保存');
    } catch (error) { ElMessage.error(error.message); }
    finally { featuredSaving.value = false; }
}

const divs = reactive([
    { class: 'magnum-video'},
    { class: 'tribute-video'},
    { class: 'throw-coin-video'},
    { class: 'collect'},
    { class: 'follow-anime'},
    { class: 'love-video'}
]);

async function loadVideos() {
    const currentSort = sort.value;
    const res = await homeContributeVideos(userId, store.userId, sort.value, 1);
    if (currentSort !== sort.value) return;
    contributeVideos.splice(0, contributeVideos.length, ...res.data.data.records.slice(0, 10));
    contributeTotal.value = res.data.data.total;
}

watch(() => store.homeUserInformation.id, async (id) => {
    if (!id) return;
    getSort();
    loadMore.value = true;
    const profile = store.homeUserInformation;
    const owner = store.userId === userId;
    const requests = [loadVideos(), homeMagnumVideos(userId).then(res => {
        featuredVideos.value = res.data.data.records;
    })];
    if (owner || profile.publicCoin === 1)
        requests.push(homeThrowCoinVideos(store.userId, userId).then(res => throwCoinVideos.splice(0, throwCoinVideos.length, ...res.data.data)));
    if (owner || profile.publicLove === 1)
        requests.push(homeLoveVideos(store.userId, userId).then(res => loveVideos.splice(0, loveVideos.length, ...res.data.data)));
    if (owner || profile.publicCollect === 1)
        requests.push(getCollectsClassify(userId).then(res => {
            collects.splice(0, collects.length, ...res.data.data.slice(0, 10));
            collectTotal.value = res.data.data.length;
        }));
    const results = await Promise.allSettled(requests);
    const failed = results.find(result => result.status === 'rejected');
    if (failed) ElMessage.error(failed.reason.message);
    loadMore.value = false;
    store.setHomeLoad(true, 'homeBody');
    store.setHomeLoadMenuList(1);
}, { immediate: true });

watch(sort, async () => {
    try {
        await loadVideos();
    } catch (error) {
        ElMessage.error(error.message);
    }
});

watch(()=>store.homeMenu,()=>{
    if(store.homeMenu === 1){
        getSort();
    }
})


//获取排序
function getSort(){
    const homeSort = store.homeUserInformation.homeSectionOrder;
    const allowed = ['magnum-video', 'tribute-video', 'throw-coin-video', 'collect', 'follow-anime', 'love-video'];
    if(Array.isArray(homeSort) && homeSort.length === allowed.length && new Set(homeSort).size === allowed.length &&
        homeSort.every(key => allowed.includes(key))){
        divs.splice(0, divs.length, ...homeSort.map(key => ({ class: key })));
    }
}

//打开个人中心
function openAccount(){
   
    window.open('./account','_blank');

}

</script>

<style lang="scss" scoped>
*{
    margin: 0;
    padding: 0;
    box-sizing: border-box;
}

.home-content{
    min-width: 0;
}
.featured-tools { display: flex; gap: 12px; }
.featured-options { display: flex; flex-direction: column; align-items: flex-start; max-height: 360px; overflow: auto; }
.featured-options :deep(.el-checkbox__label) { white-space: normal; overflow-wrap: anywhere; }
.featured-options .el-checkbox { height: auto; min-height: 36px; }
.featured-empty { color: #8D9794; padding: 20px 0; }
.home.without-profile { grid-template-columns: minmax(0, 1fr); }

.home{
    position: relative;
    display: grid;
    grid-template-columns: 240px minmax(0, 1fr);
    align-items: start;
    gap: 0 var(--gap-5);
    width: 100%;
    max-width: var(--page-max);
    margin: 0 auto;
    padding: var(--gap-5) var(--page-pad) 25px;

    .home-user-info{
    position: sticky;
    top: 80px;
    width: 240px;
    height: 125px;
    z-index: 10;
    padding: 20px 16px 24px;
    background-color: #F4F7F6;
    border-radius: 6px;

    div:nth-child(1){
    margin-bottom: 16px;
    display: flex;
    align-items: center;
    justify-content: space-between;
    color: #1C2321;
    font-size: 16px;
    font-weight: 500;
        span{
        color: #5C6664;
        font-size: 12px;
        transition: color 0.3s ease;
        cursor: pointer;
        svg{
            position: relative;
            top: 2px;
            width: 12px;
            height: 12px;
            
        }
        }
        span:hover{
            color: #0FA68E;
        }
    }
    div:nth-child(2){
        display: flex;
        align-items: center;
        margin-bottom: 10px;
        font-size: 13px;
    }
    div:nth-child(3){
        display: flex;
        align-items: center;
        font-size: 13px;
    }
    div{
        img{
            width: 19px;
            height: 16px;
            margin-right: 8px;
        }
    }
    }

    .home-div{
        .magnum-video{
        position: relative;
        top: 35px;
        width: 100%;
        margin-bottom: 34.5px;
        padding-bottom: 24px;
        border-bottom: 1px solid #E0E5E3;
            .title{
            span{
                cursor: pointer;
                color: #1C2321;
                font-size: 24px;
                font-weight: 600;
                transition: color 0.3s ease;
                margin-right: 5px;
            }
            span:hover{
                color: #0FA68E;
            }
            }
        }
        .tribute-video{
        position: relative;
        top: 24px;
        width: 100%;
        padding-bottom: 24px;
        margin-bottom: 24px;
        border-bottom: 1px solid #E0E5E3;
            .title{
            .title-name{
                cursor: pointer;
                color: #1C2321;
                font-size: 24px;
                font-weight: 600;
                transition: color 0.3s ease;
                margin-right: 5px;
            }
            .title-name:hover{
                color: #0FA68E;
            }
            .contribute-total{
                margin-top: 2px;
            }
            }
            .video-sort{
            position: relative;
            left: 40px;
            top: 0px;   
            display: flex;
            font-size: 14px;
            column-gap: 12px;
            row-gap: 12px;
            flex-wrap: wrap;
            user-select: none;
                span{
                display: flex;
                align-items: center;
                height: 34px;
                padding: 0 15px;
                flex-shrink: 0;
                color: #5C6664;
                cursor: pointer;
                transition: all .3s;
                border-radius: 6px;
                background-color: #F4F7F6;
                }
                span:hover{
                    color: #0FA68E;
                }
                .sortClass{
                    color: white !important;
                    background-color: #0FA68E !important;
                }
            }
            .more{
                    font-weight: normal;
                    position: relative;
                    width: 100px;
                    height: 34px;
                    cursor: pointer;
                    display: flex;
                    justify-content: center;
                    align-items: center;
                    padding: 0 12px;
                    left: 563px;
                    top: 0;
                    font-size: 14px;
                    color: #1C2321;
                    background: white;
                    border-radius: 8px;
                    transition: all .3s;
                    user-select: none;
                    border: 1px solid #E0E5E3;
                    svg{
                        width: 14px;
                        height: 14px;
                    }
            }
            .more:hover{
                background-color: #E0E5E3;
                border: #E0E5E3;
            }
        }

        .throw-coin-video{
        position: relative;
        top: 24px;
        width: 100%;
        margin-bottom: 24px;
        padding-bottom: 24px;
        border-bottom: 1px solid #E0E5E3;
            .title{
            span{
                cursor: pointer;
                color: #1C2321;
                font-size: 24px;
                font-weight: 600;
                transition: color 0.3s ease;
                margin-right: 5px;
            }
            span:hover{
                color: #0FA68E;
            }
            }
            .video-sort{
            position: relative;
            left: 40px;
            top: 0px;   
            display: flex;
            font-size: 14px;
            column-gap: 12px;
            row-gap: 12px;
            flex-wrap: wrap;
            user-select: none;
                span{
                display: flex;
                align-items: center;
                height: 34px;
                padding: 0 15px;
                flex-shrink: 0;
                color: #5C6664;
                cursor: pointer;
                transition: all .3s;
                border-radius: 6px;
                background-color: #F4F7F6;
                }
                span:hover{
                    color: #0FA68E;
                }
                .sortClass{
                    color: white !important;
                    background-color: #0FA68E !important;
                }
            }
            .more{
                    font-weight: normal;
                    position: absolute;
                    width: 100px;
                    height: 34px;
                    cursor: pointer;
                    display: flex;
                    justify-content: center;
                    align-items: center;
                    padding: 0 12px;
                    right: 0px;
                    top: 0;
                    font-size: 14px;
                    color: #1C2321;
                    background: white;
                    border-radius: 8px;
                    transition: all .3s;
                    user-select: none;
                    border: 1px solid #E0E5E3;
                    svg{
                        width: 14px;
                        height: 14px;
                    }
            }
            .more:hover{
                background-color: #E0E5E3;
                border: #E0E5E3;
            }
        }

        .collect{
        position: relative;
        top: 24px;
        width: 100%;
        padding-bottom: 24px;
        margin-bottom: 24px;
        border-bottom: 1px solid #E0E5E3;
            .title{   
            .title-name{
                cursor: pointer;
                color: #1C2321;
                font-size: 24px;
                font-weight: 600;
                transition: color 0.3s ease;
                margin-right: 5px;
            }
            .title-name:hover{
                color: #0FA68E;
            }
            .contribute-total{
                margin-top: 2px;
            }
            }
            .video-sort{
            position: relative;
            left: 40px;
            top: 0px;   
            display: flex;
            font-size: 14px;
            column-gap: 12px;
            row-gap: 12px;
            flex-wrap: wrap;
            user-select: none;
                span{
                display: flex;
                align-items: center;
                height: 34px;
                padding: 0 15px;
                flex-shrink: 0;
                color: #5C6664;
                cursor: pointer;
                transition: all .3s;
                border-radius: 6px;
                background-color: #F4F7F6;
                }
                span:hover{
                    color: #0FA68E;
                }
                .sortClass{
                    color: white !important;
                    background-color: #0FA68E !important;
                }
            }
            .more{
                    font-weight: normal;
                    position: relative;
                    width: 100px;
                    height: 34px;
                    cursor: pointer;
                    display: flex;
                    justify-content: center;
                    align-items: center;
                    padding: 0 12px;
                    left: 820px;
                    top: 0;
                    font-size: 14px;
                    color: #1C2321;
                    background: white;
                    border-radius: 8px;
                    transition: all .3s;
                    user-select: none;
                    border: 1px solid #E0E5E3;
                    svg{
                        width: 14px;
                        height: 14px;
                    }
            }
            .more:hover{
                background-color: #E0E5E3;
                border: #E0E5E3;
            }
            .collect-list{
            column-gap: 16px;
            row-gap: 23px;
            display: grid;
            grid-template-columns: repeat(auto-fill, minmax(min(100%, 193px), 1fr));
                .video-item{
                    cursor: pointer;
                    position: relative;
                    top: 21px;
                    min-width: 0;
                    width: 100%;
                    height: 180px;

                    .collect-back{
                        position: absolute;
                        background: #F4F7F6;
                        width: 80%;
                        height: 16px;
                        border-radius: 6px;
                        top: -16px;
                        left: 0;
                        right: 0;
                        margin: auto;
                        z-index: -10;
                    }
                    .collect-back2{
                        position: absolute;
                        background: #EFF3F2;
                        width: 90%;
                        height: 16px;
                        border-radius: 6px;
                        top: -8px;
                        left: 0;
                        right: 0;
                        margin: auto;
                        z-index: -5;
                    }
                    .content{
                        img{
                        width: 100%;
                        height: 110px;
                        background-color: #E0E5E3;
                        border-radius: 6px;
                        object-fit: cover;
                        }
                        svg{
                            position: absolute;
                            width: 18px;
                            height: 16px;
                            bottom: 74px;
                            color: white;
                            z-index: 100;
                            left: 6px;
                        }
                        .video-number{
                            position: absolute;
                            bottom: 70px;
                            left: 0;
                            box-sizing: border-box;
                            padding: 14px 8px 6px;
                            width: 100%;
                            height: 34px;
                            z-index: 10;
                            border-bottom-right-radius: 6px;
                            border-bottom-left-radius: 6px;
                            background-image: linear-gradient(180deg, #0000, #000c);
                            color: #fff;
                            font-size: 11.5px;
                            opacity: 1;
                            display: -webkit-flex;
                            display: flex;
                            align-items: center;
                            justify-content: right;
                            pointer-events: none;
                        }
                        .collect-info{
                        cursor: pointer;
                        max-width: 100%;
                        margin-top: 10px;
                        font-weight: 500;
                        font-size: 16px;
                        line-height: 1.5;
                        color: #1C2321;

                        .name{
                            font-size: 15.5px;
                            display: -webkit-box;
                            -webkit-line-clamp: 1;
                            -webkit-box-orient: vertical;
                            overflow: hidden;
                            transition: color 0.3s ease;
                        }
                        .name:hover{
                            color: #0FA68E;
                        }
                        .status{
                            font-size: 11.5px;
                            color: #8D9794;
                            margin-top: 4px;
                        }
                        }
                    }
                    
                }
            }
        }

        .follow-anime{
        position: relative;
        top: 24px;
        width: 100%;
        padding-bottom: 24px;
        margin-bottom: 24px;
        border-bottom: 1px solid #E0E5E3;
            .title{
            .title-name{
                cursor: pointer;
                color: #1C2321;
                font-size: 24px;
                font-weight: 600;
                transition: color 0.3s ease;
                margin-right: 5px;
            }
            .title-name:hover{
                color: #0FA68E;
            }
            .contribute-total{
                margin-top: 2px;
            }
            }
            .video-sort{
            position: relative;
            left: 40px;
            top: 0px;   
            display: flex;
            font-size: 14px;
            column-gap: 12px;
            row-gap: 12px;
            flex-wrap: wrap;
            user-select: none;
                span{
                display: flex;
                align-items: center;
                height: 34px;
                padding: 0 15px;
                flex-shrink: 0;
                color: #5C6664;
                cursor: pointer;
                transition: all .3s;
                border-radius: 6px;
                background-color: #F4F7F6;
                }
                span:hover{
                    color: #0FA68E;
                }
                .sortClass{
                    color: white !important;
                    background-color: #0FA68E !important;
                }
            }
            .more{
                    font-weight: normal;
                    position: absolute;
                    width: 100px;
                    height: 34px;
                    cursor: pointer;
                    display: flex;
                    justify-content: center;
                    align-items: center;
                    padding: 0 12px;
                    right: 0px;
                    top: 0;
                    font-size: 14px;
                    color: #1C2321;
                    background: white;
                    border-radius: 8px;
                    transition: all .3s;
                    user-select: none;
                    border: 1px solid #E0E5E3;
                    svg{
                        width: 14px;
                        height: 14px;
                    }
            }
            .more:hover{
                background-color: #E0E5E3;
                border: #E0E5E3;
            }
            .video-list{
            column-gap: 16px;
            row-gap: 23px;
            display: grid;
            grid-template-columns: repeat(5,1fr);
                .video-item{
                    height: 173.5px;
                }
                .no-data{
                    user-select: none;
                    width: 100%;
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
        }

        .love-video{
        position: relative;
        top: 24px;
        width: 100%;
        margin-bottom: 24px;
        padding-bottom: 24px;
        border-bottom: 1px solid #E0E5E3;
            .title{
            span{
                cursor: pointer;
                color: #1C2321;
                font-size: 24px;
                font-weight: 600;
                transition: color 0.3s ease;
                margin-right: 5px;
            }
            span:hover{
                color: #0FA68E;
            }
            }
            .video-sort{
            position: relative;
            left: 40px;
            top: 0px;   
            display: flex;
            font-size: 14px;
            column-gap: 12px;
            row-gap: 12px;
            flex-wrap: wrap;
            user-select: none;
                span{
                display: flex;
                align-items: center;
                height: 34px;
                padding: 0 15px;
                flex-shrink: 0;
                color: #5C6664;
                cursor: pointer;
                transition: all .3s;
                border-radius: 6px;
                background-color: #F4F7F6;
                }
                span:hover{
                    color: #0FA68E;
                }
                .sortClass{
                    color: white !important;
                    background-color: #0FA68E !important;
                }
            }
            .more{
                    font-weight: normal;
                    position: absolute;
                    width: 100px;
                    height: 34px;
                    cursor: pointer;
                    display: flex;
                    justify-content: center;
                    align-items: center;
                    padding: 0 12px;
                    right: 0px;
                    top: 0;
                    font-size: 14px;
                    color: #1C2321;
                    background: white;
                    border-radius: 8px;
                    transition: all .3s;
                    user-select: none;
                    border: 1px solid #E0E5E3;
                    svg{
                        width: 14px;
                        height: 14px;
                    }
            }
            .more:hover{
                background-color: #E0E5E3;
                border: #E0E5E3;
            }
                .video-list{
                column-gap: 16px;
                row-gap: 23px;
                display: grid;
                grid-template-columns: repeat(5,1fr);
                .video-item{
                    height: 173.5px;
                }
                .no-data{
                        user-select: none;
                        width: 100%;
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
        }
    }
    
}

.home-user-info-public{
    font-size: 12px !important;
    margin-left: 16px !important;
    color: #8D9794 !important;
    display: flex;
    font-weight: normal !important;
    align-items: center;

    img{
        margin-right: 4px;
        width: 16px;
        height: 13px;
    }
}
.title{
    user-select: none;
    display: flex;
    align-items: center;
    margin-bottom: 15px;
}

@media (max-width: 1150px) {
  .home{
    grid-template-columns: minmax(0, 1fr);

    .home-user-info{
      position: static;
      width: 100%;
      height: auto;
    }
  }

}

@media (max-width: 720px) {
  .home .home-div :is(.tribute-video, .collect, .throw-coin-video, .love-video) {
    .title { flex-wrap: wrap; gap: 8px; }
    .title .title-name { font-size: 20px; }
    .title .more { position: static; margin-left: auto; flex: none; }
    .video-sort { order: 1; flex-basis: 100%; left: auto; gap: 8px; }
    .video-sort span { padding: 0 10px; font-size: 12px; }
  }
  .home-user-info-public { margin-left: 0 !important; }
}

</style>