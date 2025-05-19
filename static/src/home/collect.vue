<template>
    <div class="collect-container">
        <div class="collect-aside">
              <div class="aside-name" :class="{activeAside: collectAsideOpenFlag}" @click="collectAsideOpenFlag = !collectAsideOpenFlag">
                <span>{{ store.userId&&store.userId===store.homeUserInformation.id?"我创建的收藏夹":"TA创建的收藏夹" }}</span>
                <svg class="vui_icon" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 14 14" width="14" height="14" xmlns:xlink="http://www.w3.org/1999/xlink"><path d="M5.086474166666667 1.8781408333333334C4.9156216666666674 2.048999166666667 4.9156216666666674 2.3260066666666668 5.086474166666667 2.496859166666667L9.383383333333333 6.793762500000001C9.497279166666667 6.9076875 9.497279166666667 7.092312500000001 9.383383333333333 7.2062375L5.086474166666667 11.503129166666668C4.9156216666666674 11.6739875 4.9156216666666674 11.951012500000001 5.086474166666667 12.121870833333332C5.2573325 12.2927 5.534345833333334 12.2927 5.7051983333333345 12.121870833333332L10.002095833333335 7.82495C10.457708333333333 7.369337500000001 10.457708333333333 6.6306625 10.002095833333335 6.175050000000001L5.7051983333333345 1.8781408333333334C5.534345833333334 1.7072883333333333 5.2573325 1.7072883333333333 5.086474166666667 1.8781408333333334z" fill="currentColor"></path></svg>
              </div>
              <div class="aside-list" :style="{height: collectAsideOpenFlag ? 
             (store.userId===store.homeUserInformation.id ? (48*(collectNameList.length+1)+16+'px'):(48*(collectNameList.length)+16+'px')):'0px',
              overflow: collectInfoHover?'visible':'hidden'}">
                <div v-if="store.userId===store.homeUserInformation.id" class="add-collect" @click="openAddCollectDialogF">
                    <svg class="vui_icon" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" width="20" height="20" xmlns:xlink="http://www.w3.org/1999/xlink"><path d="M11.25 3.75C11.25 3.404825 11.529833333333334 3.125 11.875 3.125L16.041666666666668 3.125C16.386833333333335 3.125 16.666666666666668 3.404825 16.666666666666668 3.75C16.666666666666668 4.095175 16.386833333333335 4.375 16.041666666666668 4.375L11.875 4.375C11.529833333333334 4.375 11.25 4.095175 11.25 3.75z" fill="currentColor"></path><path d="M9.050375 3.925716666666667C8.515666666666666 3.284041666666667 7.709475 2.918125 6.8652 2.95975C6.03485 3.0006833333333334 5.41075 3.0499833333333335 4.725341666666667 3.1168250000000004C3.2153416666666668 3.2640583333333333 1.98605 4.4355 1.8496333333333335 5.975008333333334C1.7543333333333333 7.050541666666667 1.6666666666666667 8.457833333333333 1.6666666666666667 9.997833333333334C1.6666666666666667 11.566791666666667 1.75765 12.998000000000001 1.8550250000000001 14.081041666666668C1.9902583333333335 15.585291666666667 3.1767166666666666 16.74395833333333 4.674708333333334 16.865458333333333C5.9936 16.972416666666668 7.860483333333334 17.081166666666668 10 17.081166666666668C12.213208333333332 17.081166666666668 14.134708333333332 16.964791666666667 15.459708333333333 16.854375C16.87316666666667 16.736666666666668 18.040583333333334 15.681416666666665 18.192625000000003 14.232291666666667C18.27025 13.492 18.333333333333336 12.57525 18.333333333333336 11.549333333333333C18.333333333333336 10.440666666666667 18.259666666666668 9.285375 18.174541666666666 8.318808333333335C18.02629166666667 6.6359666666666675 16.597416666666668 5.41655 14.944916666666668 5.41655L10.780166666666666 5.41655C10.471208333333333 5.41655 10.178416666666667 5.2793833333333335 9.980375 5.041741666666667L9.050375 3.925716666666667zM9.444291666666667 8.750041666666666C9.444291666666667 8.443191666666667 9.693041666666668 8.194483333333334 9.999875000000001 8.194483333333334C10.306666666666667 8.194483333333334 10.555416666666666 8.443191666666667 10.555416666666666 8.750041666666666L10.555416666666666 10.486125L12.291541666666667 10.486125C12.598333333333334 10.486125 12.847083333333334 10.734875 12.847083333333334 11.041708333333334C12.847083333333334 11.3485 12.598333333333334 11.59725 12.291541666666667 11.59725L10.555416666666666 11.59725L10.555416666666666 13.333375000000002C10.555416666666666 13.64016666666667 10.306666666666667 13.888916666666667 9.999875000000001 13.888916666666667C9.693041666666668 13.888916666666667 9.444291666666667 13.64016666666667 9.444291666666667 13.333375000000002L9.444291666666667 11.59725L7.708200000000001 11.59725C7.401375000000001 11.59725 7.152641666666668 11.3485 7.152641666666668 11.041708333333334C7.152641666666668 10.734875 7.401375000000001 10.486125 7.708200000000001 10.486125L9.444291666666667 10.486125L9.444291666666667 8.750041666666666z" fill="currentColor"></path></svg>
                    <span>新建收藏夹</span>
                </div>
                <div class="collect-item-container" v-for="collect in collectNameList" :key="collect.id" @mouseover="collectInfoHover=true" @mouseleave="collectInfoHover=false">
                    <el-tooltip
                        popper-class="custom-tooltip"
                        class="box-item"
                        effect="light"
                        :show-after="300"
                        :content="collect.collectName"
                        placement="right"
                        :show-arrow="false"
                        >
                        <div class="collect-aside-item" :class="{activeCollect: collectOpenFlag.collectName===collect.collectName,activeCollect2: store.userId!==store.homeUserInformation.id}" 
                        @click="collectOpenFlag=collect">
                            <span>
                                <svg v-if="collect.status" class="vui_icon fav-sidebar-item__icon" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" width="20" height="20" xmlns:xlink="http://www.w3.org/1999/xlink"><path d="M11.25 3.75C11.25 3.404825 11.529833333333334 3.125 11.875 3.125L16.041666666666668 3.125C16.386833333333335 3.125 16.666666666666668 3.404825 16.666666666666668 3.75C16.666666666666668 4.095175 16.386833333333335 4.375 16.041666666666668 4.375L11.875 4.375C11.529833333333334 4.375 11.25 4.095175 11.25 3.75z" fill="currentColor"></path><path d="M6.8652 2.9599583333333337C7.709475 2.9183333333333334 8.515666666666666 3.28425 9.050375 3.925925L9.980375 5.041950000000001C10.178416666666667 5.279583333333333 10.471208333333333 5.41675 10.780166666666666 5.41675L14.944916666666668 5.41675C16.597416666666668 5.41675 18.02629166666667 6.636175 18.174541666666666 8.318991666666667C18.259666666666668 9.285541666666667 18.333333333333336 10.440875 18.333333333333336 11.549583333333334C18.333333333333336 12.575458333333334 18.27025 13.492166666666668 18.192625000000003 14.232500000000002C18.040583333333334 15.681625 16.87316666666667 16.736833333333333 15.459708333333333 16.854625C14.134708333333332 16.965 12.213208333333332 17.081416666666666 10 17.081416666666666C7.860483333333334 17.081416666666666 5.9936 16.972583333333336 4.674708333333334 16.86566666666667C3.1767166666666666 16.74416666666667 1.9902583333333335 15.585500000000001 1.8550250000000001 14.08125C1.75765 12.998166666666666 1.6666666666666667 11.567 1.6666666666666667 9.998083333333334C1.6666666666666667 8.458 1.7543333333333333 7.050750000000001 1.8496333333333335 5.975208333333334C1.98605 4.435700000000001 3.2153416666666668 3.264266666666667 4.725341666666667 3.117025C5.41075 3.0501916666666666 6.03485 3.000891666666667 6.8652 2.9599583333333337zM8.090091666666666 4.7261500000000005C7.800241666666667 4.378325 7.368958333333334 4.1866416666666675 6.926749999999999 4.208433333333334C6.11845 4.248283333333334 5.5143916666666675 4.296016666666667 4.84665 4.361125C3.9067416666666666 4.452775 3.1756 5.173141666666667 3.0947583333333335 6.0855500000000005C3.0018249999999997 7.1343 2.916666666666667 8.503625 2.916666666666667 9.998083333333334C2.916666666666667 11.520625 3.0050583333333334 12.913250000000001 3.1 13.969333333333335C3.1800916666666668 14.860166666666668 3.876841666666667 15.546875 4.775741666666667 15.619750000000002C6.069166666666667 15.724625000000001 7.901483333333334 15.831416666666666 10 15.831416666666666C12.170875 15.831416666666666 14.056791666666667 15.717125000000001 15.355958333333335 15.608916666666666C16.21004166666667 15.537749999999999 16.864083333333333 14.915958333333332 16.949458333333332 14.102083333333333C17.023333333333333 13.397666666666668 17.083333333333336 12.525 17.083333333333336 11.549583333333334C17.083333333333336 10.491041666666668 17.012708333333336 9.375291666666666 16.929333333333336 8.428708333333333C16.841166666666666 7.427583333333334 15.98675 6.66675 14.944916666666668 6.66675L10.780166666666666 6.66675C10.099833333333333 6.66675 9.455416666666668 6.364508333333333 9.020125 5.842175L8.090091666666666 4.7261500000000005z" fill="currentColor"></path></svg>
                                <svg v-else class="vui_icon fav-sidebar-item__icon" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" width="20" height="20" xmlns:xlink="http://www.w3.org/1999/xlink" style=""><path d="M6.8652 2.9602833333333334C7.709466666666667 2.9186583333333336 8.515666666666666 3.2845750000000002 9.050333333333334 3.92625L9.980416666666667 5.042275C10.178416666666667 5.279908333333333 10.471166666666667 5.417083333333333 10.780166666666666 5.417083333333333L14.944916666666668 5.417083333333333C16.597583333333336 5.417083333333333 18.026416666666666 6.637216666666667 18.174583333333334 8.3199C18.197166666666668 8.576083333333333 18.218833333333336 8.845166666666668 18.238583333333334 9.12275C18.263083333333334 9.467 18.003833333333336 9.766000000000002 17.6595 9.7905C17.315250000000002 9.815000000000001 17.01625 9.555750000000002 16.99175 9.211416666666667C16.972583333333336 8.9415 16.951416666666667 8.679416666666667 16.92941666666667 8.429583333333335C16.841166666666666 7.4283 15.986583333333336 6.667083333333334 14.944916666666668 6.667083333333334L10.780166666666666 6.667083333333334C10.099833333333333 6.667083333333334 9.455416666666668 6.364841666666667 9.020083333333334 5.8425L8.0901 4.726483333333333C7.800241666666667 4.37865 7.368958333333334 4.186966666666667 6.926749999999999 4.2087666666666665C6.11845 4.248616666666667 5.514383333333334 4.296341666666667 4.84665 4.3614500000000005C3.9067416666666666 4.4531 3.1756 5.173475 3.0947583333333335 6.085875C3.0018249999999997 7.134633333333333 2.916666666666667 8.503916666666667 2.916666666666667 9.998416666666667C2.916666666666667 11.520916666666668 3.0050583333333334 12.913583333333333 3.1 13.969666666666667C3.180075 14.860333333333333 3.877275 15.547250000000002 4.776366666666666 15.620083333333334C5.896966666666667 15.711000000000002 7.422266666666667 15.80325 9.174833333333334 15.82625C9.52 15.830833333333334 9.796166666666666 16.114250000000002 9.791583333333334 16.45941666666667C9.787083333333333 16.804583333333337 9.503583333333333 17.08066666666667 9.1585 17.076166666666666C7.3723583333333345 17.05275 5.818316666666667 16.958750000000002 4.675341666666667 16.866C3.1775333333333333 16.744583333333335 1.990275 15.586 1.8550166666666668 14.081583333333334C1.75765 12.9985 1.6666666666666667 11.567333333333334 1.6666666666666667 9.998416666666667C1.6666666666666667 8.458333333333334 1.754325 7.051075 1.8496333333333335 5.975541666666667C1.98605 4.4360333333333335 3.2153333333333336 3.264591666666667 4.725333333333333 3.11735C5.41075 3.0505166666666668 6.03485 3.0012166666666666 6.8652 2.9602833333333334zM11.25 3.7504166666666667C11.25 3.405233333333334 11.529833333333334 3.1254166666666667 11.875 3.1254166666666667L16.041666666666668 3.1254166666666667C16.386833333333335 3.1254166666666667 16.666666666666668 3.405233333333334 16.666666666666668 3.7504166666666667C16.666666666666668 4.095591666666667 16.386833333333335 4.375416666666666 16.041666666666668 4.375416666666666L11.875 4.375416666666666C11.529833333333334 4.375416666666666 11.25 4.095591666666667 11.25 3.7504166666666667zM17.110083333333336 11.693833333333334C17.110083333333336 10.298333333333334 15.978833333333336 9.167083333333334 14.583333333333334 9.167083333333334C13.187833333333334 9.167083333333334 12.056500000000002 10.298333333333334 12.056500000000002 11.693833333333334L12.056500000000002 12.438C11.378583333333333 12.452333333333334 10.833333333333334 13.006333333333334 10.833333333333334 13.687750000000001L10.833333333333334 16.18775C10.833333333333334 16.878083333333333 11.393 17.43775 12.083333333333334 17.43775L17.083333333333336 17.43775C17.773666666666667 17.43775 18.333333333333336 16.878083333333333 18.333333333333336 16.18775L18.333333333333336 13.687750000000001C18.333333333333336 13.006333333333334 17.788083333333336 12.452250000000001 17.110083333333336 12.438L17.110083333333336 11.693833333333334zM15.985083333333332 11.693833333333334L15.985083333333332 12.437750000000001L13.1815 12.437750000000001L13.1815 11.693833333333334C13.1815 10.919666666666668 13.809083333333335 10.292083333333334 14.583333333333334 10.292083333333334C15.3575 10.292083333333334 15.985083333333332 10.919666666666668 15.985083333333332 11.693833333333334zM15.416666666666668 14.521333333333333C15.416666666666668 14.82975 15.249083333333333 15.099083333333335 15 15.243166666666665L15 15.771416666666667C15 16.0015 14.813416666666667 16.188083333333335 14.583333333333334 16.188083333333335C14.353166666666668 16.188083333333335 14.166666666666668 16.0015 14.166666666666668 15.771416666666667L14.166666666666668 15.243166666666665C13.917583333333335 15.099083333333335 13.75 14.82975 13.75 14.521333333333333C13.75 14.061083333333334 14.123083333333335 13.688 14.583333333333334 13.688C15.0435 13.688 15.416666666666668 14.061083333333334 15.416666666666668 14.521333333333333z" fill="currentColor"></path></svg>
                                <span>{{ collect.collectName }}</span>
                            </span>
                            <span class="number">{{ collect.videoNumber }}</span>
                            <span v-if="store.userId&&store.userId===store.homeUserInformation.id" class="controls">
                                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1024 1024"><path fill="currentColor" d="M176 416a112 112 0 1 1 0 224 112 112 0 0 1 0-224m336 0a112 112 0 1 1 0 224 112 112 0 0 1 0-224m336 0a112 112 0 1 1 0 224 112 112 0 0 1 0-224"></path></svg>
                                <div class="controls-info" :class="{'active-controls-info': collect.collectName=== '默认收藏夹'}">
                                    <div @click.stop="openEditCollectDialogF(collect)">编辑信息</div>
                                    <div v-if="collect.collectName!== '默认收藏夹'" @click.stop="deleteCollectDialogFlag=true,collectClassifyId=collect.id">删除</div>
                                </div>
                            </span>
                        </div>
                    </el-tooltip> 
                </div>
              </div>
              <div v-if="store.userId&&store.userId===store.homeUserInformation.id" class="aside-name" @click="openWaitWatch" :class="{'activeAside2': collectAsideOpenFlag}">
                    <span>稍后再看</span>
              </div>
        </div>
        <div class="collect-body">
            <div class="collect-info">
                <div class="left">
                    <div class="collect-back"></div>
                    <div class="collect-back2"></div>
                    <img v-show="!collectOpenFlag.coverAddress" :src="'../img/collect_cover.avif'">
                    <img v-show="collectOpenFlag.coverAddress" :src="collectOpenFlag.coverAddress">
                </div>
                <div class="right">
                    <div class="collect-title">{{ collectOpenFlag.collectName }}</div>
                    <div class="collect-desc">
                        <span>{{ collectOpenFlag.status? '公开':'私密' }}</span>
                        <span>视频数: {{ collectOpenFlag.videoNumber }}</span>
                    </div>
                    <div v-if="store.userId===store.homeUserInformation.id" class="controls" @click="batchControls=!batchControls">{{ !batchControls? '批量操作':'返回' }}</div>
                </div>
            </div>
            <div v-show="!batchControls" class="collect-sort">
                <span class="sort-item" :class="{ sortClass: sort === 1 }" @click="sort = 1"
                >最近收藏</span>
                <span class="sort-item" :class="{ sortClass: sort === 2 }" @click="sort = 2"
                >最多播放</span>
                <span class="sort-item" :class="{ sortClass: sort === 3 }" @click="sort = 3"
                >最近投稿</span>
                <div class="search-container">
                    <span class="search-container-left" @click="openSearchType=!openSearchType">
                        <span>{{ searchType===1? '当前':'全部' }}</span>
                        <svg class="vui_icon" :class="{'activeIcon': openSearchType}" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 14 14" width="14" height="14" xmlns:xlink="http://www.w3.org/1999/xlink"><path d="M5.086474166666667 1.8781408333333334C4.9156216666666674 2.048999166666667 4.9156216666666674 2.3260066666666668 5.086474166666667 2.496859166666667L9.383383333333333 6.793762500000001C9.497279166666667 6.9076875 9.497279166666667 7.092312500000001 9.383383333333333 7.2062375L5.086474166666667 11.503129166666668C4.9156216666666674 11.6739875 4.9156216666666674 11.951012500000001 5.086474166666667 12.121870833333332C5.2573325 12.2927 5.534345833333334 12.2927 5.7051983333333345 12.121870833333332L10.002095833333335 7.82495C10.457708333333333 7.369337500000001 10.457708333333333 6.6306625 10.002095833333335 6.175050000000001L5.7051983333333345 1.8781408333333334C5.534345833333334 1.7072883333333333 5.2573325 1.7072883333333333 5.086474166666667 1.8781408333333334z" fill="currentColor"></path></svg>
                        <div class="search-controls" :class="{'active-search-controls':openSearchType}">
                            <div @click="searchType=1">当前收藏夹</div>
                            <div @click="searchType=2">全部收藏夹</div>
                        </div>
                    </span>
                    <span class="search-container-right">
                        <input type="text" maxlength="30" v-model="searchContent" placeholder="请输入关键词" @keydown.enter="searchCollect">
                        <svg v-show="searchContent.length>0" @click="searchContent=''" class="vui_icon vui_input-clear-base__icon" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 16 16" width="16" height="16" xmlns:xlink="http://www.w3.org/1999/xlink"><path d="M8 1.3333333333333333C4.318099999999999 1.3333333333333333 1.3333333333333333 4.318099999999999 1.3333333333333333 8C1.3333333333333333 11.681899999999999 4.318099999999999 14.666666666666666 8 14.666666666666666C11.681899999999999 14.666666666666666 14.666666666666666 11.681899999999999 14.666666666666666 8C14.666666666666666 4.318099999999999 11.681899999999999 1.3333333333333333 8 1.3333333333333333zM5.64258 6.3496C5.4473199999999995 6.1543399999999995 5.4473199999999995 5.837753333333333 5.64258 5.6424933333333325C5.837846666666666 5.447233333333333 6.154426666666667 5.447233333333333 6.349693333333333 5.6424933333333325L8 7.2928L9.650333333333332 5.6424933333333325C9.845566666666667 5.447233333333333 10.162166666666666 5.447233333333333 10.357433333333333 5.6424933333333325C10.552666666666667 5.837753333333333 10.552666666666667 6.1543399999999995 10.357433333333333 6.3496L8.7071 7.9999L10.357433333333333 9.650233333333333C10.552666666666667 9.845466666666667 10.552666666666667 10.162066666666666 10.357433333333333 10.357333333333333C10.162166666666666 10.5526 9.845566666666667 10.5526 9.650333333333332 10.357333333333333L8 8.706999999999999L6.349693333333333 10.357333333333333C6.154426666666667 10.5526 5.837846666666666 10.5526 5.64258 10.357333333333333C5.4473199999999995 10.162066666666666 5.4473199999999995 9.845466666666667 5.64258 9.650233333333333L7.2928999999999995 7.9999L5.64258 6.3496z" fill="currentColor"></path></svg>
                        <img @click="searchCollect" src="../img/搜索.png">
                    </span>
                </div>
            </div>
            <div v-if="store.userId===store.homeUserInformation.id" v-show="batchControls" class="collect-sort">
                <span class="check-all" @click="checkAllF">
                    <span class="check-all-icon">
                        <span class="check-input-box" :class="{activeCheck:checkAllFlag}"></span>
                    </span>
                    <span class="check-all-text">全选</span>
                </span>
                <span class="select-num">已选择 {{ store.autoVideoList.length }} 个视频</span>
                <span class="controls-item">
                    <div class="controls" @click="deleteFailureCollectDialogFlag=!deleteFailureCollectDialogFlag">清除失效内容</div>
                    <div class="controls" @click="deleteCollectF">取消收藏</div>
                    <div class="controls" @click="copyOrMoveCollectF(1)"><img class="copy" src="../img/复制.png">复制至</div>
                    <div class="controls" @click="copyOrMoveCollectF(2)"><img class="move" src="../img/移动.png">移动至</div>
                </span>
            </div>
            <div v-show="loadMore" class="class-loading">
                <img src="../img/loading-blue.gif" />
                <span>拼命加载中...</span>
            </div>
            <div v-show="!loadMore" class="video-list">
                <videoList7 :data="collectList" :batchControls="batchControls" :searchContent="searchContent"/>
            </div>
            <div v-show="!loadMore&&collectTotal" class="page-container">
            <el-pagination
                :current-page="pageNum"
                :page-size="20"
                layout="prev, pager, next"
                :total="collectTotal"
                :background="true"
                @current-change="handleCurrentChange"
            />
            <span
                >共 {{ Math.ceil(collectTotal / 20) }} 页 /
                {{ collectTotal }} 个，跳至<input
                type="number"
                @keydown.enter="handleCurrentChange2"
                />页</span
            >
            </div>
        </div>
        <!-- 清除失效内容弹窗-->
        <el-dialog
        v-if="store.userId===store.homeUserInformation.id"
        v-model="deleteFailureCollectDialogFlag"
        append-to-body
        width="320"
        :lock-scroll="false"
        align-center
        style="height: 152px; border-radius: 12px"
        >
            <div class="delete-dialog-title">清除失效内容</div>
            <div class="delete-dialog-content">
                是否一键清除当前文件夹所有失效内容？
            </div>
            <div class="delete-dialog-btn-container">
                <button class="delete-dialog-cancel-btn" @click="deleteFailureCollectDialogFlag=false">
                取消
            </button>
            <button
                class="delete-dialog-confirm-btn" @click="deleteFailureCollectF"
            >
                确认
            </button>
            </div>
        </el-dialog>
        <!--删除收藏夹弹窗-->
        <el-dialog
        v-if="store.userId===store.homeUserInformation.id"
        v-model="deleteCollectDialogFlag"
        append-to-body
        width="320"
        :lock-scroll="false"
        align-center
        style="height: 152px; border-radius: 12px"
        >
            <div class="delete-dialog-title">确认提示？</div>
            <div class="delete-dialog-content">
                确定删除这个收藏夹吗？
            </div>
            <div class="delete-dialog-btn-container">
                <button class="delete-dialog-cancel-btn" @click="deleteCollectDialogFlag=false">
                取消
            </button>
            <button
                class="delete-dialog-confirm-btn2" @click="deleteCollectClassifyF"
            >
                确认
            </button>
            </div>
        </el-dialog>
        <!--新增收藏夹弹窗-->
        <el-dialog
        v-if="store.userId===store.homeUserInformation.id"
        v-model="addCollectDialogFlag"
        append-to-body
        width="388"
        :lock-scroll="false"
        align-center
        style="height: 602px; border-radius: 12px;padding: 4px 24px 24px;"
        >
            <div class="add-dialog-title">新建收藏夹</div>
            <div class="add-dialog-body">
                <input type="file" ref="fileInput" style="display: none;" accept="image/jpeg,image/png,image/webp" @change="handleFileChange">
                <div class="add-collect-cover">
                    <div class="collect-back"></div>
                    <div class="collect-back2"></div>
                    <div v-show="!uploadCover" class="default-cover" @click="openFileSelector">
                        <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1024 1024"><path fill="currentColor" d="M480 480V128a32 32 0 0 1 64 0v352h352a32 32 0 1 1 0 64H544v352a32 32 0 1 1-64 0V544H128a32 32 0 0 1 0-64z"></path></svg>
                    </div>
                    <img v-show="uploadCover" :src="uploadCover" @click="openFileSelector">
                </div>
                <p class="collect-name">名称<span style="color: red;"> *</span></p>
                <el-input v-model="collectName" type="text" style="width: 340px;height: 34px;"
                 maxlength="20" placeholder="快来给你的收藏夹命名吧" :show-word-limit="true"/>
                <p class="collect-name">公开</p>
                <el-switch
                    v-model="collectPublic"
                    class="ml-2"
                    style="height: 20px;--el-switch-on-color: #00AEEC;"
                />
                <p class="collect-name">简介</p>
                <el-input v-model="collectIntroduce" type="textarea" resize="none" :autosize="{ minRows: 5, maxRows: 5 }" style="width: 340px;height: 112px !important;" maxlength="200" placeholder="可以简单的描述下你的收藏夹" :show-word-limit="true"/>
            </div>
            <div class="add-dialog-fotter">
                <button class="add-dialog-cancel-btn" @click="addCollectDialogFlag=false">
                    取消
                </button>
                <button class="add-dialog-confirm-btn" :class="{ 'disabled': collectName.trim() === '' }" @click="addCollectClassifyF">
                    创建
                </button>
            </div>
        </el-dialog> 
        <!--编辑收藏夹弹窗-->
        <el-dialog
        v-if="store.userId===store.homeUserInformation.id"
        v-model="editCollectDialogFlag"
        append-to-body
        width="388"
        :lock-scroll="false"
        align-center
        style="height: 602px; border-radius: 12px;padding: 4px 24px 24px;"
        >
            <div class="add-dialog-title">收藏夹信息</div>
            <div class="add-dialog-body">
                <input type="file" ref="fileInput" style="display: none;" accept="image/jpeg,image/png,image/webp" @change="handleFileChange">
                <div class="add-collect-cover">
                    <div class="collect-back"></div>
                    <div class="collect-back2"></div>
                    <div v-show="!uploadCover" class="default-cover" @click="openFileSelector">
                        <svg  xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1024 1024"><path fill="currentColor" d="M480 480V128a32 32 0 0 1 64 0v352h352a32 32 0 1 1 0 64H544v352a32 32 0 1 1-64 0V544H128a32 32 0 0 1 0-64z"></path></svg>
                    </div>
                    <img v-show="uploadCover" :src="uploadCover" @click="openFileSelector">
                </div>
                <p class="collect-name">名称<span style="color: red;"> *</span></p>
                <el-input v-model="collectName" :disabled="collectName==='默认收藏夹'" type="text" style="width: 340px;height: 34px;"
                 maxlength="20" placeholder="快来给你的收藏夹命名吧" :show-word-limit="true"/>
                <p class="collect-name">公开</p>
                <el-switch
                    v-model="collectPublic"
                    class="ml-2"
                    style="height: 20px;--el-switch-on-color: #00AEEC;"
                />
                <p class="collect-name">简介</p>
                <el-input v-model="collectIntroduce" type="textarea" resize="none" :autosize="{ minRows: 5, maxRows: 5 }" style="width: 340px;height: 112px !important;" maxlength="200" placeholder="可以简单的描述下你的收藏夹" :show-word-limit="true"/>
            </div>
            <div class="add-dialog-fotter">
                <button class="add-dialog-cancel-btn" @click="editCollectDialogFlag=false">
                    取消
                </button>
                <button class="add-dialog-confirm-btn" :class="{ 'disabled': collectName.trim() === '' }" @click="updateCollectClassifyF">
                    修改
                </button>
            </div>
        </el-dialog>
        <!--复制移动收藏弹窗-->
        <el-dialog
        v-if="store.userId===store.homeUserInformation.id"
        v-model="store.collectControlsDialogFlag"
        append-to-body
        width="388"
        :lock-scroll="false"
        align-center
        style="height: 474px; border-radius: 12px;padding: 4px 24px 24px;"
        >
            <div class="control-dialog-title">将1个视频{{ store.copyOrMove===1? '复制':'移动' }}至</div>
            <div class="control-dialog-body">
               <div class="add-collect-classify" @click="addCollectDialogFlag=true">
                    <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1024 1024"><path fill="currentColor" d="M480 480V128a32 32 0 0 1 64 0v352h352a32 32 0 1 1 0 64H544v352a32 32 0 1 1-64 0V544H128a32 32 0 0 1 0-64z"></path></svg>
                    <span>新建收藏夹</span>
               </div>
               <div class="checkbox-container">
                    <el-checkbox-group
                        v-model="checkedCities"
                        @change="handleCheckedCitiesChange"
                    >
                       <div class="collect-name-item" v-for="collectName in collectNameList" :key="collectName">
                        <el-checkbox :disabled="collectName.collectName===collectOpenFlag.collectName" :label="collectName" :value="collectName.id">
                        <div class="checkbox-content">
                            <span class="checkbox-title">{{ collectName.collectName }}</span>
                            <span class="video-number">{{ collectName.videoNumber }}</span>
                        </div>
                        </el-checkbox>
                       </div>
                    </el-checkbox-group>
               </div>
            </div>
            <div class="control-dialog-fotter">
                <button class="control-dialog-confirm-btn" @click="controlCollectF">
                    确定
                </button>
            </div>
        </el-dialog>     
    </div>
</template>

<script setup>
import { onMounted, onUnmounted, reactive, ref, watch } from "vue";
import {getCollectsClassify,getCollectByName} from '@/api/home/index';
import { useGlobalStore } from '../store/store';
import { addCollectClassify,deleteCollectClassify,updateCollectClassify } from '@/api/collectClassify/index';
import { deleteFailure,deleteCollect,controlCollect } from '@/api/collect/index';
import { ElMessage } from "element-plus";
import videoList7 from "@/utils/videoList7.vue";

const store = useGlobalStore();
const collectOpenFlag = ref(false);
const collectAsideOpenFlag = ref(false);
const sort= ref(1);//1.最近收藏 2.最多播放 3.最近投稿
const openSearchType = ref(false);
const searchType = ref(1);//1.当前 2.全部
const searchContent = ref('');
const batchControls = ref(false);
const checkAllFlag= ref(false);
const deleteCollectDialogFlag = ref(false);
const deleteFailureCollectDialogFlag = ref(false);
const addCollectDialogFlag = ref(false);
const editCollectDialogFlag = ref(false);
const collectName= ref('');//新建收藏夹名称
const collectPublic= ref(true);//新建收藏夹是否公开
const collectInfoHover = ref(false);
const collectIntroduce= ref('');//新建收藏夹简介
const collectList = reactive([]);
const collectNameList = reactive([]);
const pageNum = ref(1);
const searchFlag = ref(false);
const fileInput = ref(null);
const uploadCover = ref(null);
const collectClassifyId = ref(null);
const checkedCities = ref([]);
const checkAll= ref(false);
const collectTotal = ref(0);
const loadMore = ref(true);

onMounted(async()=>{

if (store.homeUserInformation.id) {
    await getCollectsClassifyF();
    store.setHomeLoad(true,'homeBody');
}
window.addEventListener('click',handleClickOutside);

})

onUnmounted(()=>{

store.setHomeLoad(false,"homeBody");
window.removeEventListener('click',handleClickOutside);
})

function handleClickOutside(event) {
    if(!event.target)
        return;
    if(event.target.closest(".search-container-left"))
        return;
        openSearchType.value = false;
}

// 监听store变化
let onceFlag = true;
watch(
  () => store.homeUserInformation.id,
  async () => {
    if (onceFlag && store.homeUserInformation.id) {
      onceFlag = false;
       getCollectsClassifyF();
      store.setHomeLoad(true, "homeBody");
    }
  },
);

//获取所有收藏夹名称
async function getCollectsClassifyF(){
    getCollectsClassify(store.homeUserInformation.id,store.userId,store.token).then(res=>{
        if(res.data.code === 1){
            Object.assign(collectNameList,res.data.data);
            
            const collectId=parseInt(new URL(window.location).searchParams.get('collectId'))||null;
            if(!collectOpenFlag.value&&collectId){
                collectOpenFlag.value=collectNameList.find(item=>item.id===collectId);
                collectAsideOpenFlag.value=true;
            }
           else{
                if(collectOpenFlag.value)
                    getCollectByNameF();
                else
                    collectOpenFlag.value=collectNameList[0];
           }
        }
    })
}

//根据名称获取收藏的视频
function getCollectByNameF(){
    loadMore.value=true;
    const date=new Date();
    getCollectByName(collectOpenFlag.value.collectName,store.homeUserInformation.id,store.userId,pageNum.value,
    searchType.value,sort.value,searchContent.value).then(res=>{
        if(res.data.code === 1){
            collectList.length = 0;
            Object.assign(collectList,res.data.data.records);
            collectTotal.value = res.data.data.total;
            let date2 = new Date();
            setTimeout(
                () => {
                loadMore.value = false;
                },
                date2 - date < 300 ? 300 : date2 - date,
            );
        }
    })

}

//监视sort
watch(sort,()=>{
    getCollectByNameF();
})

//监视收藏夹名称
watch(collectOpenFlag,()=>{
    if(!collectOpenFlag.value)
        return;
    pageNum.value = 1;
    store.autoVideoList.length=0;
    getCollectByNameF();
})

//搜索
function searchCollect(){
    
    if(searchContent.value.trim() === ''|| searchFlag.value===searchContent.value)
        return;
    pageNum.value = 1;
    getCollectByNameF();
    searchFlag.value=searchContent.value;

}

//监视搜索内容
watch(searchContent,()=>{
    if(searchContent.value.trim() === ''&& searchFlag.value!=='')
    {
    pageNum.value = 1;
    getCollectByNameF();
    searchFlag.value='';
    }
})

//监视搜索类型
watch(searchType,()=>{
    searchFlag.value='1';
});

//新增收藏夹
function addCollectClassifyF(){

    if(collectName.value.trim() === '')
    {
        return;
    }

    if(collectNameList.findIndex(item=>item.collectName===collectName.value)!==-1)
    {
        ElMessage({
            message: '收藏夹名称重复',
            type: "info",
            plain: true,
            duration: 1700,
            });
            return;
    }

    let collectClassify = {
        userId: store.userId,
        collectName: collectName.value,
        coverAddress: uploadCover.value,
        content: collectIntroduce.value,
        status: collectPublic.value? 1 : 0,
        videoNumber: 0,
    };

    addCollectClassify(store.token,collectClassify).then(res=>{
        if(res.data.code === 1){
            ElMessage({
            message: res.data.data,
            type: "info",
            plain: true,
            duration: 1700,
            });
            getCollectsClassifyF();
            addCollectDialogFlag.value=false;
        }
        else{
            ElMessage({
            message: res.data.msg,
            type: "info",
            plain: true,
            duration: 1700,
            });
        }
    })

}

//打开文件选择器
function openFileSelector(){
    fileInput.value.click();
}

//上传的封面
function handleFileChange(e){

    const file = e.target.files[0];

    if(!file)
     return;

    if(file.type.indexOf('image') === -1)
    {
        ElMessage({
            message: '请上传图片',
            type: "info",
            plain: true,
            duration: 1700,
            });
        e.target.value='';    
        return;
    }

    if(file.size>5*1024*1024)
    {
        ElMessage({
            message: '图片大小不能超过5M',
            type: "info",
            plain: true,
            duration: 1700,
            });
        e.target.value='';    
        return;
    }  

    const reader = new FileReader();
    reader.readAsDataURL(file);
    reader.onload = (e) => {
        uploadCover.value = e.target.result;
    };
    e.target.value='';
}

//打开新增收藏夹对话框
function openAddCollectDialogF(){
    collectName.value = '';
    collectPublic.value = true;
    collectIntroduce.value = '';
    uploadCover.value = null;
    addCollectDialogFlag.value = true;
}

//打开编辑收藏夹对话框
function openEditCollectDialogF(collect){

    collectClassifyId.value = collect.id;
    collectName.value = collect.collectName;
    collectPublic.value = collect.status === 1;
    collectIntroduce.value = collect.content;
    uploadCover.value = collect.coverAddress;
    editCollectDialogFlag.value = true;
}

//删除收藏夹
function deleteCollectClassifyF(){

    if(!collectClassifyId.value)
        return;
    deleteCollectClassify(store.token,store.userId,collectClassifyId.value).then(res=>{
        if(res.data.code === 1){
            ElMessage({
            message: res.data.data,
            type: "info",
            plain: true,
            duration: 1700,
            });
             //如果删除的是当前收藏夹，则默认选中第一个收藏夹
             if(collectOpenFlag.value.id === collectClassifyId.value){
                 collectOpenFlag.value=collectNameList[0];
             }
            collectNameList.splice(collectNameList.findIndex(item=>item.id===collectClassifyId.value),1);
            collectClassifyId.value = null;
            deleteCollectDialogFlag.value=false;
           
        }
        else{
            ElMessage({
            message: res.data.msg,
            type: "info",
            plain: true,
            duration: 1700,
            });
        }
    })

}

//修改收藏夹
function updateCollectClassifyF(){

    if(!collectClassifyId.value)
        return;
    if(collectName.value.trim() === '')
    {
        return;
    }
    if(collectNameList.findIndex(item=>item.collectName===collectName.value&&item.id!==collectClassifyId.value)!==-1)
    {
        ElMessage({
            message: '收藏夹名称重复',
            type: "info",
            plain: true,
            duration: 1700,
            });
            return;
    }

    let collectClassify = {
        id: collectClassifyId.value,
        userId: store.userId,
        collectName: collectName.value,
        coverAddress: uploadCover.value,
        content: collectIntroduce.value,
        videoNumber: collectNameList[collectNameList.findIndex(item=>item.id===collectClassifyId.value)].videoNumber,
        status: collectPublic.value ? 1 : 0,
    };

    updateCollectClassify(store.token,collectClassify).then(res=>{

        if(res.data.code === 1){

            ElMessage({
            message: res.data.data,
            type: "info",
            plain: true,
            duration: 1700,
            });
            collectNameList.splice(collectNameList.findIndex(item=>item.id===collectClassifyId.value),1,collectClassify);
            //如果修改的是当前收藏夹，则更新当前收藏夹名称
            if(collectOpenFlag.value.id === collectClassifyId.value){
                collectOpenFlag.value = collectClassify;
            }
            collectClassifyId.value = null;
            editCollectDialogFlag.value=false;
        }
        else{
            ElMessage({
            message: res.data.msg,
            type: "info",
            plain: true,
            duration: 1700,
            });
        }
    })

}

//清除失效收藏
function deleteFailureCollectF(){

    deleteFailure(store.token,store.userId).then(res=>{
        if(res.data.code === 1){
            ElMessage({
            message: res.data.data,
            type: "info",
            plain: true,
            duration: 1700,
            });
            getCollectsClassifyF();
            deleteFailureCollectDialogFlag.value=false;
        }
        else{
            ElMessage({
            message: res.data.msg,
            type: "info",
            plain: true,
            duration: 1700,
            });
        }
    })
}

//选择收藏夹
function handleCheckedCitiesChange(value){
    checkAll.value=value.length===collectNameList.length;
}

//全部选中或全部取消选中
function checkAllF(){
    checkAllFlag.value=!checkAllFlag.value;
    if(checkAllFlag.value){
       let ids=collectList.map(item=>item.collects.id);
       store.setAutoVideoList(ids);
    }
    else{
       store.autoVideoList.length=0; 
    }
}

//重置批量操作
watch(batchControls,()=>{
    if(!batchControls.value){
      store.autoVideoList.length=0;
      checkAllFlag.value=false;
    }
})

//监视选中视频的数量
watch(()=>store.autoVideoList.length,(newValue)=>{
    if(newValue===collectList.length){
        checkAllFlag.value=true;
    }
    else{
        checkAllFlag.value=false;
    }
    if(newValue===1&&!batchControls.value)
    {
        deleteCollectF();
    }
})

//取消收藏
function deleteCollectF(){

   if(store.autoVideoList.length===0){
       
        ElMessage({
            message: '请先选择视频',
            type: "info",
            plain: true,
            duration: 1700,
        });
        return;
   }
   deleteCollect(store.token,store.userId,store.autoVideoList).then(res=>{
        if(res.data.code === 1){
            ElMessage({
            message: res.data.data,
            type: "info",
            plain: true,
            duration: 1700,
            });
            getCollectsClassifyF();
            batchControls.value=false;
        }
        else{
            ElMessage({
            message: res.data.msg,
            type: "info",
            plain: true,
            duration: 1700,
            });
        }
   })

}

//复制或移动收藏对话框
function copyOrMoveCollectF(control){

    if(store.autoVideoList.length===0){
        ElMessage({
            message: '请先选择视频',
            type: "info",
            plain: true,
            duration: 1700,
        });
        return;
    }
    
    store.setCollectControlsDialogFlag(true);
    store.setCopyOrMove(control);
}

//复制或移动收藏
function controlCollectF(){

    if(checkedCities.value.length===0){
        ElMessage({
            message: '请选择收藏夹',
            type: "info",
            plain: true,
            duration: 1700,
        });
        return;
    }

    controlCollect(store.token,store.userId,store.autoVideoList.length?store.autoVideoList:store.collectIds,checkedCities.value,store.copyOrMove).then(res=>{
        if(res.data.code === 1){
            ElMessage({
            message: res.data.data,
            type: "info",
            plain: true,
            duration: 1700,
            });
            getCollectsClassifyF();
            checkedCities.value.length=0;
            store.collectIds.length=0;
            batchControls.value=false;
            store.setCollectControlsDialogFlag(false);
        }
        else{
            ElMessage({
            message: res.data.msg,
            type: "info",
            plain: true,
            duration: 1700,
            });
        }
    })
}

// 更改当前页
function handleCurrentChange(val) {
  pageNum.value = val;
}

// 更改当前页
function handleCurrentChange2(event) {
  if (
    event.target.value !== "" &&
    event.target.value <= Math.ceil(collectTotal.value / 20) &&
    event.target.value >= 1
  )
    pageNum.value = parseInt(event.target.value);
}

//监视页数变化
watch(pageNum, () => {
  if (store.homeUserInformation.id)
    getCollectByNameF();
 
});

//打开稍后再看
function openWaitWatch(){
    window.open('./waitWatch','_blank');
}

</script>

<style lang="scss" scoped>
*{
    margin: 0;
    padding: 0;
    box-sizing: border-box;
}

.collect-container{
  display: flex;
  padding-left: 60px;

    .collect-aside{ 
    margin-top: 18px;   
    position: sticky;
    padding-bottom: 20px;
    top: 64px;
    padding-right: 5px;
    flex-shrink: 0;
    margin-right: 16px;
    width: 195px;
    height: 100%;
    z-index: 1000;
    user-select: none;

        .aside-name{
        font-weight: 500;
        font-size: 16px;
        padding: 0 16px;
        height: 48px;
        display: flex;
        transition: all 0.3s ease;
        cursor: pointer;
        justify-content: space-between;
        align-items: center;
        border-bottom: 1px solid #E3E5E7;
           
            svg{
                transition: all 0.3s ease;
                transform: rotate(90deg);
            }
        }
        .activeAside{
            svg{
                transition: all 0.3s ease;
                transform: rotate(-90deg);
            }
        }
        .aside-name:hover{
            color: #00AEEC;
        }
        .aside-list{
            display: flex;
            height: 0px;
            flex-direction: column;
            transition: all 0.3s ease;
            overflow: hidden;

                .add-collect{
                display: flex;
                align-items: center;
                width: 100%;
                height: 48px;
                border-radius: 6px;
                padding: 14px 16px;
                cursor: pointer;
                transition: all .3s ease;
                font-size: 14px;
                   
                   svg{
                    margin-right: 8px;
                   }
                }
                .add-collect:hover{
                    color: #00AEEC;
                }
                .collect-item-container{
                    .collect-aside-item{
                    display: flex;
                    align-items: center;
                    justify-content: space-between;
                    width: 100%;
                    height: 48px;
                    border-radius: 6px;
                    padding: 14px 16px;
                    cursor: pointer;
                    transition: all .3s ease;
                    font-size: 14px;

                        span{
                            display: flex;
                            align-items: center;
                        }
                        svg{
                            transition: all .3s ease;
                            color: #9499A0;
                            margin-right: 8px;
                        }
                        .number{
                            font-size: 12px;
                            flex-shrink: 0;
                            color: #9499A0;
                        }
                        .controls{
                        position: relative;
                        width: 24px;
                        height: 24px;
                        left: 10px;
                        display: none;

                            svg{
                            width: 16px;
                            height: 22px;
                            transform: rotate(90deg);
                            color: #18191C;
                            margin-right: 0px !important;
                            transition: all .3s ease;
                            }
                            .controls-info{
                            transition-delay: 0.3s;  
                            width: 123px;
                            height: 100px;
                            z-index: 10000;
                            position: absolute;
                            left: 50%;
                            bottom: -105px;
                            transform: translateX(-50%);
                            opacity: 0;
                            transition: opacity .3s ease,visibility .3s ease;
                            visibility: hidden;
                            display: flex;
                            flex-direction: column;
                            align-items: center;
                            justify-content: center;
                            background-color: white;
                            border: 1px solid #E3E5E7;
                            box-shadow: 0 8px 40px rgba(0, 0, 0, .1);
                            padding: 4px;
                            border-radius: 12px;

                                div{
                                width: 96%;
                                text-align: center;    
                                cursor: pointer;
                                font-size: 14px;
                                color: #61666D;
                                transition: background-color .3s ease;
                                border-radius: 8px;
                                height: 40px;
                                line-height: 40px;
                                position: relative;
                                background-color: white;
                                }
                                div:hover{
                                    background-color: #F1F2F3;
                                }
                            }
                            .active-controls-info{
                                height: 70px !important;
                                bottom: -75px !important;
                            }
                        }
                        .controls:hover{
                            .controls-info{
                            transition-delay: 0.3s;
                            opacity: 1;
                            visibility: visible;
                            }
                        }
                    }
                    .collect-aside-item:hover{
                    background-color: #F6F7F8;

                    svg{
                        color: #18191C;
                    }
                    .number{
                        display: none;
                    }
                    .controls{
                        display: flex;
                        justify-content: center;
                        align-items: center;
                    }
                    }
                }
                .activeCollect{
                    background-color: #00AEEC !important;
                    color: white !important;

                    svg{
                        color: white !important;
                    }
                    .number{
                        color: white !important;
                    }
                }
                .activeCollect2:hover{
                    .number{
                        display: inline !important;
                    }
                }
        }
        .activeAside2{
            border-top: 1px solid #E3E5E7;
        }
    }
    .collect-body{
    margin-top: 30px;
    width: 1094px;
    height: 100vh;
    padding-bottom: 100px;

        .collect-info{
        height: 137px;
        padding-bottom: 24px;
        box-sizing: border-box;
        border-bottom: 1px solid #E3E5E7;
        display: flex;
        position: relative;

            .left{
            position: relative;
            margin-top: 12px;
            width: 178px;
                .collect-back{
                position: absolute;
                background: #F6F7F8;
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
                background: #F1F2F3;
                width: 90%;
                height: 16px;
                border-radius: 6px;
                top: -8px;
                left: 0;
                right: 0;
                margin: auto;
                z-index: -5;
                }
                img{
                width: 100%;
                height: 100px;
                background-color: #E3E5E7;
                border-radius: 6px;
                object-fit: cover;
                }
            }
            .right{
            width: calc(100% - 196px);
            margin-left: 18px;
            display: flex;
            flex-direction: column;

               .collect-title{
                margin-top: 2px;
                display: -webkit-box;
                font-size: 20px;
                -webkit-line-clamp: 1;
                -webkit-box-orient: vertical;
                font-weight: 500;
                overflow: hidden;
               }
               .collect-desc{
                margin-top: 8px;
                  span{
                    font-size: 14px;
                    color: #9499A0;
                    line-height: 17px;
                  }
                  span:nth-child(1){
                    margin-right: 12px;
                  }
               }
               .controls{
                position: absolute;
                right: 0px;
                top: 77px;
                height: 34px;
                width: 100px;
                padding: 0 12px;
                display: flex;
                align-items: center;
                justify-content: center;
                border-radius: 8px;
                font-size: 14px;
                line-height: 1;
                color: #18191C;
                background: white;
                border: 1px solid #E3E5E7;
                transition: all .3s ease;
                user-select: none;
                cursor: pointer;
               }
               .controls:hover{
                background-color: #E3E5E7;
                border: 1px solid #E3E5E7;
               }
            }
        }
        .collect-sort{
        position: relative;
        top: 24px;
        display: flex;
        height: 34px;
        font-size: 14px;
        column-gap: 12px;
        row-gap: 12px;
        flex-wrap: wrap;
        user-select: none;
            .sort-item {
            display: flex;
            align-items: center;
            height: 34px;
            padding: 0 15px;
            flex-shrink: 0;
            color: #61666d;
            cursor: pointer;
            transition: all 0.3s;
            border-radius: 6px;
            background-color: #f6f7f8;
            }
            .sort-item:hover {
            color: #00aeec;
            }
            .sortClass {
            color: white !important;
            background-color: #00aeec !important;
            }
            .search-container{
                position: absolute;
                display: inline-flex;
                right: 0px;
                width: 310px;
                height: 34px;
                border-radius: 6px;
                font-size: 14px;
                border: 1px solid #E3E5E7;

                    .search-container-left{
                        display: inline-flex;
                        align-items: center;
                        justify-content: center;
                        height: 34px;
                        width: 100px;
                        padding: 0 12px;
                        font-size: 13.5px;
                        line-height: 1;
                        border-radius: 6px 0 0 6px;
                        transition: all 0.3s ease;
                        border-right: 1px solid #E3E5E7;
                        color: #18191C;
                        user-select: none;
                        cursor: pointer;

                          svg{
                            margin-left: 4px;
                            transition: all 0.3s ease;
                            transform: rotate(90deg);
                          }
                          .activeIcon{
                              transition: all 0.3s ease;
                              transform: rotate(-90deg);
                          }
                          .search-controls{
                            transition-delay: 0.3s;  
                            width: 123px;
                            height: 100px;
                            z-index: 10000;
                            position: absolute;
                            left: -5%;
                            bottom: -110px;
                            opacity: 0;
                            transition: opacity .3s ease,visibility .3s ease;
                            visibility: hidden;
                            display: flex;
                            flex-direction: column;
                            align-items: center;
                            justify-content: center;
                            background-color: white;
                            border: 1px solid #E3E5E7;
                            box-shadow: 0 8px 40px rgba(0, 0, 0, .1);
                            padding: 4px;
                            border-radius: 12px;

                                div{
                                width: 96%;
                                text-align: center;    
                                cursor: pointer;
                                font-size: 14px;
                                color: #61666D;
                                transition: background-color .3s ease;
                                border-radius: 8px;
                                height: 40px;
                                line-height: 40px;
                                position: relative;
                                background-color: white;
                                }
                                div:hover{
                                    background-color: #F1F2F3;
                                }
                          }
                          .active-search-controls{
                              opacity: 1;
                              visibility: visible;
                          }
                    }
                    .search-container-left:hover{
                        background-color: #E3E5E7;
                    }
                    .search-container-right{
                    width: 210px;
                    overflow: hidden;
                    flex-grow: 1;
                    display: inline-flex;
                    position: relative;
                    padding: 0 12px;
                    font-size: 14px;
                    padding: 0 10px;

                        input{
                        width: 135px;    
                        padding: 0;
                        font-size: 14px;
                        display: inline-flex;
                        outline: 0;
                        border: none;
                        outline: none;
                        background-color: transparent;
                        color: #18191C;
                        }
                        svg{
                            position: absolute;
                            right: 40px;
                            top: 50%;
                            transform: translateY(-50%);
                            color: #E3E5E7;
                            transition: color 0.3s ease;
                            cursor: pointer;
                        }
                        svg:hover{
                            color: #C9CCD0;
                        }
                        img{
                            position: absolute;
                            right: 12px;
                            top: 50%;
                            transform: translateY(-50%);
                            width: 16px;
                            height: 16px;
                            cursor: pointer;
                        }
                    }
            }
            .check-all{
            position: relative;
            top: 0px;
            display: inline-flex;
            align-items: center;
            user-select: none;
            font-size: 14px;
            line-height: 1.5;
            color: #18191C;
            cursor: pointer;
                .check-all-icon{
                position: relative;
                display: inline-block;
                line-height: 1;

                    .check-input-box{
                    position: relative;
                    display: block;
                    width: 14px;
                    height: 14px;
                    -webkit-box-sizing: border-box;
                    box-sizing: border-box;
                    background-color: white;
                    border: 1px solid #E3E5E7;
                    border-radius: 4px;
                    -webkit-transition: all .3s ease-in-out;
                    transition: all .3s ease-in-out;
                    }
                    .check-input-box::after{
                    content: " ";
                    position: absolute;
                    top: 44%;
                    left: 21%;
                    display: table;
                    width: 4px;
                    height: 8px;
                    border: 2px solid white;
                    border-top: 0;
                    border-left: 0;
                    transform: rotate(45deg) scale(0.85) translate(-65%, -50%);                    
                    transition: all .3s ease-in-out;
                    }
                    .activeCheck{
                        border: 1px solid #00aeec;
                        background-color: #00aeec;
                    }
                   
                }
                .check-all-text{
                    padding: 0 8px;
                }
            }
            .check-all:hover{
                .check-all-icon{
                    .check-input-box{
                        border: 1px solid #00aeec;
                    }
                }
            }
            .select-num{
                position: relative;
                top: 8px;
                font-size: 13.5px;
                color: #61666D;
                margin-left: 12px;
            }
            .controls-item{
            position: absolute;
            display: inline-flex;
            align-items: center;
            justify-content: center;
            right: 0px;

                .controls{
                height: 34px;
                min-width: 100px;
                padding: 0 12px;
                display: inline-flex;
                align-items: center;
                justify-content: center;
                border-radius: 8px;
                font-size: 14px;
                line-height: 1;
                color: #18191C;
                background: white;
                border: 1px solid #E3E5E7;
                transition: all .3s ease;
                user-select: none;
                margin-left: 12px;
                cursor: pointer;
                    
                    .copy{
                        width: 12px;
                        height: 13px;
                        margin-right: 5px;
                    }
                    .move{
                        width: 13px;
                        height: 11.5px;
                        margin-right: 5px;
                    }
                }
                .controls:hover{
                    background-color: #E3E5E7;
                    border: 1px solid #E3E5E7;
                }
            }
        }
        .class-loading {
        position: relative;
        margin-top: 60px;
        display: flex;
        justify-content: center;

        img {
            width: 30px;
            height: 30px;
        }
        span {
            position: relative;
            top: 5px;
            left: 5px;
            font-size: 14px;
            color: #61666d;
        }
        }
        .video-list{
            margin-top: 48px;
        }
        .page-container {
        position: relative;
        margin-top: 60px;
        display: flex;
        justify-content: center;
        align-items: center;

        span {
          color: #18191c;
          font-size: 13px;
          margin-left: 44px;
          input {
            width: 50px;
            height: 34px;
            overflow: hidden;
            display: inline-flex;
            flex-grow: 1;
            outline: none;
            position: relative;
            padding: 0 12px;
            background-color: white;
            border: 1px solid #e3e5e7;
            font-size: 14px;
            border-radius: 6px;
            transition: all 0.3s ease;
            padding: 0 10px;
          }
          input:hover {
            border-color: #00aeec;
          }
          input:focus {
            border-color: #00aeec;
          }
          input::-webkit-inner-spin-button,
          input::-webkit-outer-spin-button {
            -webkit-appearance: none;
          }
        }
        }
    }
}

.delete-dialog-title{
    position: relative;
    text-align: center;
    font-size: 15.5px;
    font-weight: 550;
    color: #18191C;
}

.delete-dialog-content{
    position: relative;
    text-align: center;
    top: 10px;
    font-size: 13.5px;
    color: #666;
    transition: all 0.2s ease;
}

.delete-dialog-btn-container{
    position: relative;
    top: 35px;
    display: flex;
    justify-content: center;
}

.delete-dialog-confirm-btn{
    border: 1px solid #00AEEC;
    color: white;
    border-radius: 4px;
    min-width: 130px;
    height: 32px;
    cursor: pointer;
    font-size: 15.5px;
    margin: 0 6px;
    background-color: #00AEEC;
    transition: all 0.3s ease;
}

.delete-dialog-confirm-btn:hover{
    background-color: #40C5F1;
    border: 1px solid #40C5F1;
}

.delete-dialog-cancel-btn{
    border: 1px solid #E3E5E7;
    color: #18191C;
    border-radius: 4px;
    min-width: 130px;
    height: 32px;
    cursor: pointer;
    font-size: 15.5px;
    margin: 0 6px;
    background-color: white;
    transition: all 0.3s ease;
}

.delete-dialog-cancel-btn:hover{
    background-color: #E3E5E7;
}

.delete-dialog-confirm-btn2{
    border: 1px solid #F85A54;
    color: white;
    border-radius: 4px;
    min-width: 130px;
    height: 32px;
    cursor: pointer;
    font-size: 15.5px;
    margin: 0 6px;
    background-color: #F85A54;
    transition: all 0.3s ease;
}

.add-dialog-title{
    text-align: center;
    font-size: 15.5px;
    color: #18191C;
}

.add-dialog-body{
    padding-top: 24px;
    padding-bottom: 22px;
    color: #18191C;
    text-align: left;
    font-size: 14px;
    margin-top: 10px;
    margin-bottom: 24px;

    .add-collect-cover{
    position: relative;
    padding-top: 16px;
    width: 158px;
    height: 105px;

        .collect-back{
        position: absolute;
        background: #F6F7F8;
        width: 80%;
        height: 16px;
        border-radius: 6px;
        top: 0px;
        left: 0;
        right: 0;
        margin: auto;
        z-index: 5;
        }
        .collect-back2{
        position: absolute;
        background: #F1F2F3;
        width: 90%;
        height: 16px;
        border-radius: 6px;
        top: 8px;
        left: 0;
        right: 0;
        margin: auto;
        z-index: 10;
        }
        .default-cover{
        position: relative;
        display: flex;
        justify-content: center;
        align-items: center;
        width: 100%;
        height: 100%;
        cursor: pointer;
        background-color: #E3E5E7;
        border-radius: 6px;
        z-index: 10;

            svg{
                width: 35px;
                height: 34px;
                color: #61666D;
            }
        }
        img{
            position: relative;
            width: 100%;
            height: 100%;
            border-radius: 6px;
            object-fit: cover;
            z-index: 10;
            cursor: pointer;
        }
    }
    .collect-name{
    font-size: 14px;
    line-height: 20px;
    margin-top: 24px;
    margin-bottom: 8px;
    }
}

.add-dialog-fotter{
    margin-top: 28px;
    display: flex;
    justify-content: center;
    .add-dialog-cancel-btn{
    border: 1px solid #E3E5E7;
    color: #18191C;
    border-radius: 8px;
    min-width: 164px;
    height: 34px;
    cursor: pointer;
    font-size: 14px;
    margin: 0 6px;
    background-color: white;
    transition: all 0.3s ease;
    }
    .add-dialog-cancel-btn:hover{
        background-color: #E3E5E7;
    }
    .add-dialog-confirm-btn{
    border: 1px solid #00AEEC;
    color: white;
    border-radius: 8px;
    min-width: 164px;
    height: 34px;
    cursor: pointer;
    font-size: 14px;
    margin: 0 6px;
    background-color: #00AEEC;
    transition: all 0.3s ease;
    }
    .add-dialog-confirm-btn:hover{
    background-color: #40C5F1;
    border: 1px solid #40C5F1;
    }
    .disabled{
        background-color: #00AEEC !important;
        opacity: 0.5 !important;
        cursor: not-allowed;
    }

}

.control-dialog-title{
    margin-top: 3px;
    text-align: center;
    font-size: 15.5px;
    color: #18191C;
}

.control-dialog-confirm-btn{
    position: relative;
    border: 1px solid #00AEEC;
    color: white;
    border-radius: 8px;
    min-width: 120px;
    height: 34px;
    cursor: pointer;
    font-size: 14px;
    margin: 0 6px;
    background-color: #00AEEC;
    transition: all 0.3s ease;
}

.control-dialog-body{
    padding-top: 20px;
    width: 340px;
    height: 340px;
    color: #18191C;
    text-align: left;
    font-size: 14px;
    margin-top: 10px;
    margin-bottom: 24px;
    overflow-y: scroll;

    .add-collect-classify{
    width: 325px;
    margin-bottom: 12px;
    cursor: pointer;
    padding: 16px;
    font-size: 13.5px;
    border-radius: 4px;
    border: 1px dashed #C9CCD0;
    height: 52px;
    display: flex;
    user-select: none;
    align-items: center;

        svg{
            width: 18px;
            height: 18px;
            margin-right: 10px;
        }
    }
    .checkbox-container{
        position: relative;
        top: 8px;

        .collect-name-item{
            margin-bottom: 21px;
             
            .checkbox-content {
            display: flex;
            justify-content: space-between;
            align-items: center;
            width: 290px; /* 占满宽度以实现两边对齐 */

              .video-number{
                color: #61666D;
              }
            }
        }
    }

}

.control-dialog-body::-webkit-scrollbar {
  width: 5px; /* 滚动条的宽度 */
  border-radius: 30px; /* 滚动条滑块的圆角 */
}

.control-dialog-body::-webkit-scrollbar-thumb {
  background: #bcbcbc; /* 滚动条的滑块 */
  height: 10px;
  border-radius: 10px;
}

.control-dialog-fotter{
    margin-top: 25px;
    display: flex;
    justify-content: center;
}


</style>