<template>
  <div>
    <div id="fixedBox" class="hiddenBox" :class="{ showHiddenBox: isVisible }">
      <img
        src="/img/logo.png"
        style="width: 60px; transform: translate(24px, 18px); cursor: pointer"
      />
      <Searcha
        ref="searcha"
        style="position: relative;
            left: 50%;
            transform: translate(-50%,-25px);
            z-index: 1000;"
        :mainSearchFlag="true"
        :handleUrlChange="handleUrlChange"
      />
    </div>
    <div>
      <div
        class="head"
        :class="{
          headLoginIndex: store.loginDialogVisible || store.loginLoadFlag,
        }"
      >
        <head1
          :head2Flag="true"
          :searchFlag="true"
          :loginDialogVisibleFlag="loginDialogVisibleFlag"
        />
      </div>
      <div class="searchBox2">
        <Searcha
        ref="searcha2"
        :handleUrlChange="handleUrlChange"
          style="
            position: relative;
            left: 50%;
            transform: translate(-50%,28px);
            z-index: 1000;
          "
        />
      </div>
      <div class="middle">
        <div class="sort" ref="sortBarEl">
    <!-- 原来这里是 14 个 span：每个 tab 按选中与否各写一份 .aw / .aww，
         靠 17px / 26px / 8px / 14px 这些 translate 微调对齐；
         下面再挂 7 个写死 translate(63px, 137px) 的下划线。
         现在只留一份 tab，选中态走 is-on，下划线按 DOM 实测位置。 -->
    <span
      v-for="tab in resultTabs"
      :key="tab.flag"
      :ref="el => setTabEl(el, tab.flag)"
      class="aw"
      :class="{ 'is-on': clickFlag === tab.flag }"
      @click="ClickFlag(tab.flag)"
      >{{ tab.label }}
      <span class="ac" v-if="tab.total !== null">{{ tab.total > 99 ? '99+' : tab.total }}</span>
    </span>
    <span
      class="sort-underline"
      :style="{ transform: 'translateX('+sortLine.x+'px)', width: sortLine.w+'px' }"
      v-show="sortLine.visible"
    ></span>
        </div>
      </div>
      <div class="content">
        <div v-show="clickFlag1 || clickFlag2" class="videoSort">
          <span
            v-show="clickSortFlag1 === false"
            @click="ClickSortFlag1"
            class="condition"
            style="transform: translate(86px, 36.5px)"
            >综合排序</span
          >
          <span
            v-show="clickSortFlag1"
            class="conditionClick"
            style="transform: translate(64px, 31px)"
            >综合排序</span
          >
          <span
            v-show="clickSortFlag2 === false"
            @click="ClickSortFlag2"
            class="condition"
            style="transform: translate(196px, 36.5px)"
            >最多播放</span
          >
          <span
            v-show="clickSortFlag2"
            class="conditionClick"
            style="transform: translate(174px, 31px)"
            >最多播放</span
          >
          <span
            v-show="clickSortFlag3 === false"
            @click="ClickSortFlag3"
            class="condition"
            style="transform: translate(306px, 36.5px)"
            >最新发布</span
          >
          <span
            v-show="clickSortFlag3"
            class="conditionClick"
            style="transform: translate(284px, 31px)"
            >最新发布</span
          >
          <span
            v-show="clickSortFlag4 === false"
            @click="ClickSortFlag4"
            class="condition"
            style="transform: translate(416px, 36.5px)"
            >最多弹幕</span
          >
          <span
            v-show="clickSortFlag4"
            class="conditionClick"
            style="transform: translate(394px, 31px)"
            >最多弹幕</span
          >
          <span
            v-show="clickSortFlag5 === false"
            @click="ClickSortFlag5"
            class="condition"
            style="transform: translate(526px, 36.5px)"
            >最多收藏</span
          >
          <span
            v-show="clickSortFlag5"
            class="conditionClick"
            style="transform: translate(504px, 31px)"
            >最多收藏</span
          >
          <span class="screen" @click="expanded = !expanded"
            ><span>更多筛选</span><img src="/img/更多.png"
          /></span>

          <div class="showBox" :class="{ expanded }">
            <span
              v-show="clickDateFlag1 === false"
              @click="ClickDateFlag1"
              class="condition"
              style="transform: translate(86px, 80.5px)"
              >全部日期</span
            >
            <span
              v-show="clickDateFlag1"
              class="conditionClick"
              style="transform: translate(64px, 75px)"
              >全部日期</span
            >
            <span
              v-show="clickDateFlag2 === false"
              @click="ClickDateFlag2"
              class="condition"
              style="transform: translate(196px, 80.5px)"
              >最近一天</span
            >
            <span
              v-show="clickDateFlag2"
              class="conditionClick"
              style="transform: translate(174px, 75px)"
              >最近一天</span
            >
            <span
              v-show="clickDateFlag3 === false"
              @click="ClickDateFlag3"
              class="condition"
              style="transform: translate(306px, 80.5px)"
              >最近一周</span
            >
            <span
              v-show="clickDateFlag3"
              class="conditionClick"
              style="transform: translate(284px, 75px)"
              >最近一周</span
            >
            <span
              v-show="clickDateFlag4 === false"
              @click="ClickDateFlag4"
              class="condition"
              style="transform: translate(416px, 80.5px)"
              >最近半年</span
            >
            <span
              v-show="clickDateFlag4"
              class="conditionClick"
              style="transform: translate(394px, 75px)"
              >最近半年</span
            >
            <span
              @click="ClickDateFlag5"
              class="condition"
              style="transform: translate(504px, 74px)"
            >
              <el-date-picker
                v-model="datea"
                style="border-radius: 8px; width: 310px; height: 34px"
                type="daterange"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                :default-value="[new Date(), new Date()]"
              >
              </el-date-picker>
            </span>

            <span
              v-show="clickTimeFlag1 === false"
              @click="ClickTimeFlag1"
              class="condition"
              style="transform: translate(86px, 123.5px)"
              >全部时长</span
            >
            <span
              v-show="clickTimeFlag1"
              class="conditionClick"
              style="transform: translate(64px, 118px)"
              >全部时长</span
            >
            <span
              v-show="clickTimeFlag2 === false"
              @click="ClickTimeFlag2"
              class="condition"
              style="transform: translate(189px, 123.5px)"
              >10分钟以下</span
            >
            <span
              v-show="clickTimeFlag2"
              class="conditionClick"
              style="transform: translate(175px, 118px)"
              >10分钟以下</span
            >
            <span
              v-show="clickTimeFlag3 === false"
              @click="ClickTimeFlag3"
              class="condition"
              style="transform: translate(302px, 123.5px)"
              >10-30分钟</span
            >
            <span
              v-show="clickTimeFlag3"
              class="conditionClick"
              style="transform: translate(285.5px, 118px)"
              >10-30分钟</span
            >
            <span
              v-show="clickTimeFlag4 === false"
              @click="ClickTimeFlag4"
              class="condition"
              style="transform: translate(412px, 123.5px)"
              >30-60分钟</span
            >
            <span
              v-show="clickTimeFlag4"
              class="conditionClick"
              style="transform: translate(395.5px, 118px)"
              >30-60分钟</span
            >
            <span
              v-show="clickTimeFlag5 === false"
              @click="ClickTimeFlag5"
              class="condition"
              style="transform: translate(521px, 123.5px)"
              >60分钟以上</span
            >
            <span
              v-show="clickTimeFlag5"
              class="conditionClick"
              style="transform: translate(507px, 118px)"
              >60分钟以上</span
            >

            <span
              v-if="clickClassifyFlag1 === false"
              @click="ClickClassifyFlag1"
              class="condition"
              style="transform: translate(86px, 165.5px)"
              >全部分类</span
            >
            <span
              v-if="clickClassifyFlag1"
              class="conditionClick"
              style="transform: translate(64px, 160px)"
              >全部分类</span
            >
            <el-tooltip
              class="box-item"
              :show-arrow="false"
              effect="light"
              placement="bottom"
              @click="ClickClassifyFlag2"
            >
              <template #content>
                <div class="custom-tooltip2" @click="ClickClassifyFlag2">
                  <span>MAD·AMV</span><span>MMD·3D</span> <span>同人·手书</span
                  ><span>配音</span><span>模玩·周边</span><span>特摄</span
                  ><span>动漫杂谈</span><span>综合</span>
                </div>
              </template>
              <span
                v-if="!clickClassifyFlag2"
                @click="ClickClassifyFlag2"
                class="condition"
                style="transform: translate(210px, 165.5px)"
                >动画</span
              >
              <span
                v-if="clickClassifyFlag2"
                class="conditionClick"
                style="transform: translate(174px, 160px)"
                >动画</span
              >
            </el-tooltip>
            <el-tooltip
              class="box-item"
              :show-arrow="false"
              effect="light"
              placement="bottom"
              @click="ClickClassifyFlag3"
              :disabled="true"
            >
              <span
                v-if="clickClassifyFlag3 === false"
                @click="ClickClassifyFlag3"
                class="condition"
                style="transform: translate(320px, 165.5px)"
                >番剧</span
              >
              <span
                v-if="clickClassifyFlag3"
                class="conditionClick"
                style="transform: translate(284px, 160px)"
                >番剧</span
              >
            </el-tooltip>
            <el-tooltip
              class="box-item"
              :show-arrow="false"
              effect="light"
              placement="bottom"
              @click="ClickClassifyFlag4"
            >
              <template #content>
                <div class="custom-tooltip2" @click="ClickClassifyFlag4">
                  <span>国产动画</span><span>国产原创相关</span>
                  <span>布袋戏</span><span>动态漫·广播剧</span><span>资讯</span
                  ><span>新番时间表</span><span>国产动画索引</span>
                </div>
              </template>
              <span
                v-if="clickClassifyFlag4 === false"
                @click="ClickClassifyFlag4"
                class="condition"
                style="transform: translate(430px, 165.5px)"
                >国创</span
              >
              <span
                v-if="clickClassifyFlag4"
                class="conditionClick"
                style="transform: translate(394px, 160px)"
                >国创</span
              >
            </el-tooltip>
            <el-tooltip
              class="box-item"
              :show-arrow="false"
              effect="light"
              placement="bottom"
              @click="ClickClassifyFlag5"
            >
              <template #content>
                <div class="custom-tooltip2" @click="ClickClassifyFlag5">
                  <span>音乐现场</span><span>翻唱</span> <span>演奏</span
                  ><span>乐评盘点</span><span>VOCLOID·UTAU</span><span>MV</span
                  ><span>音乐粉丝饭拍</span><span>AI音乐</span><span>电台</span
                  ><span>音乐教学</span><span>音乐综合</span><span>说唱</span>
                </div>
              </template>
              <span
                v-if="clickClassifyFlag5 === false"
                @click="ClickClassifyFlag5"
                class="condition"
                style="transform: translate(540px, 165.5px)"
                >音乐</span
              >
              <span
                v-if="clickClassifyFlag5"
                class="conditionClick"
                style="transform: translate(504px, 160px)"
                >音乐</span
              >
            </el-tooltip>
            <el-tooltip
              class="box-item"
              :show-arrow="false"
              effect="light"
              placement="bottom"
              @click="ClickClassifyFlag6"
            >
              <template #content>
                <div class="custom-tooltip2" @click="ClickClassifyFlag6">
                  <span>宅舞</span><span>街舞</span> <span>明星舞蹈</span
                  ><span>国风舞蹈</span><span>颜值·网红舞</span
                  ><span>舞蹈综合</span><span>舞蹈教程</span>
                </div>
              </template>
              <span
                v-if="clickClassifyFlag6 === false"
                @click="ClickClassifyFlag6"
                class="condition"
                style="transform: translate(650px, 165.5px)"
                >舞蹈</span
              >
              <span
                v-if="clickClassifyFlag6"
                class="conditionClick"
                style="transform: translate(614px, 160px)"
                >舞蹈</span
              >
            </el-tooltip>
            <el-tooltip
              class="box-item"
              :show-arrow="false"
              effect="light"
              placement="bottom"
              @click="ClickClassifyFlag7"
            >
              <template #content>
                <div class="custom-tooltip2" @click="ClickClassifyFlag7">
                  <span>单机游戏</span><span>电子竞技</span>
                  <span>手机游戏</span><span>网络游戏</span><span>桌游棋牌</span
                  ><span>GMV</span><span>音游</span><span>Mugen</span
                  ><span>游戏赛事</span>
                </div>
              </template>
              <span
                v-if="clickClassifyFlag7 === false"
                @click="ClickClassifyFlag7"
                class="condition"
                style="transform: translate(760px, 165.5px)"
                >游戏</span
              >
              <span
                v-if="clickClassifyFlag7"
                class="conditionClick"
                style="transform: translate(724px, 160px)"
                >游戏</span
              >
            </el-tooltip>
            <el-tooltip
              class="box-item"
              :show-arrow="false"
              effect="light"
              placement="bottom"
              @click="ClickClassifyFlag8"
            >
              <template #content>
                <div class="custom-tooltip2" @click="ClickClassifyFlag8">
                  <span>科学科普</span><span>社科·法律·心理</span>
                  <span>人文历史</span><span>财经商业</span><span>校园学校</span
                  ><span>职业职场</span><span>设计·创意</span
                  ><span>野生技能协会</span><span>游戏赛事</span>
                </div>
              </template>
              <span
                v-if="clickClassifyFlag8 === false"
                @click="ClickClassifyFlag8"
                class="condition"
                style="transform: translate(870px, 165.5px)"
                >知识</span
              >
              <span
                v-if="clickClassifyFlag8"
                class="conditionClick"
                style="transform: translate(834px, 160px)"
                >知识</span
              >
            </el-tooltip>
            <el-tooltip
              class="box-item"
              :show-arrow="false"
              effect="light"
              placement="bottom"
              @click="ClickClassifyFlag9"
            >
              <template #content>
                <div class="custom-tooltip2" @click="ClickClassifyFlag9">
                  <span>数码</span><span>软件应用</span> <span>计算机技术</span
                  ><span>科工机械</span><span>极客DIY</span>
                </div>
              </template>
              <span
                v-if="clickClassifyFlag9 === false"
                @click="ClickClassifyFlag9"
                class="condition"
                style="transform: translate(980px, 165.5px)"
                >科技</span
              >
              <span
                v-if="clickClassifyFlag9"
                class="conditionClick"
                style="transform: translate(944px, 160px)"
                >科技</span
              >
            </el-tooltip>
            <el-tooltip
              class="box-item"
              :show-arrow="false"
              effect="light"
              placement="bottom"
              @click="ClickClassifyFlag10"
            >
              <template #content>
                <div class="custom-tooltip2" @click="ClickClassifyFlag10">
                  <span>篮球</span><span>足球</span> <span>健身</span
                  ><span>竞技体育</span><span>运动文化</span
                  ><span>运动综合</span>
                </div>
              </template>
              <span
                v-if="clickClassifyFlag10 === false"
                @click="ClickClassifyFlag10"
                class="condition"
                style="transform: translate(1090px, 165.5px)"
                >运动</span
              >
              <span
                v-if="clickClassifyFlag10"
                class="conditionClick"
                style="transform: translate(1054px, 160px)"
                >运动</span
              >
            </el-tooltip>
            <el-tooltip
              class="box-item"
              :show-arrow="false"
              effect="light"
              placement="bottom"
              @click="ClickClassifyFlag11"
              :disabled="true"
            >
              <span
                v-if="clickClassifyFlag11 === false"
                @click="ClickClassifyFlag11"
                class="condition"
                style="transform: translate(1200px, 165.5px)"
                >汽车</span
              >
              <span
                v-if="clickClassifyFlag11"
                class="conditionClick"
                style="transform: translate(1164px, 160px)"
                >汽车</span
              >
            </el-tooltip>
            <el-tooltip
              class="box-item"
              :show-arrow="false"
              effect="light"
              placement="bottom"
              @click="ClickClassifyFlag12"
            >
              <template #content>
                <div class="custom-tooltip2" @click="ClickClassifyFlag12">
                  <span>搞笑</span><span>亲子</span> <span>出行</span
                  ><span>三农</span><span>家居房产</span><span>手工</span
                  ><span>绘画</span><span>日常</span>
                </div>
              </template>
              <span
                v-if="clickClassifyFlag12 === false"
                @click="ClickClassifyFlag12"
                class="condition"
                style="transform: translate(100px, 207.5px)"
                >生活</span
              >
              <span
                v-if="clickClassifyFlag12"
                class="conditionClick"
                style="transform: translate(64px, 202px)"
                >生活</span
              >
            </el-tooltip>
            <el-tooltip
              class="box-item"
              :show-arrow="false"
              effect="light"
              placement="bottom"
              @click="ClickClassifyFlag13"
            >
              <template #content>
                <div class="custom-tooltip2" @click="ClickClassifyFlag13">
                  <span>美食制作</span><span>美食侦探</span>
                  <span>美食测评</span><span>田园美食</span
                  ><span>美食记录</span>
                </div>
              </template>
              <span
                v-if="clickClassifyFlag13 === false"
                @click="ClickClassifyFlag13"
                class="condition"
                style="transform: translate(210px, 207.5px)"
                >美食</span
              >
              <span
                v-if="clickClassifyFlag13"
                class="conditionClick"
                style="transform: translate(174px, 202px)"
                >美食</span
              >
            </el-tooltip>
            <el-tooltip
              class="box-item"
              :show-arrow="false"
              effect="light"
              placement="bottom"
              @click="ClickClassifyFlag14"
            >
              <template #content>
                <div class="custom-tooltip2" @click="ClickClassifyFlag14">
                  <span>喵星人</span><span>汪星人</span> <span>小宠异能</span
                  ><span>野生动物</span><span>动物二创</span
                  ><span>动物综合</span>
                </div>
              </template>
              <span
                v-if="clickClassifyFlag14 === false"
                @click="ClickClassifyFlag14"
                class="condition"
                style="transform: translate(313px, 207.5px)"
                >动物圈</span
              >
              <span
                v-if="clickClassifyFlag14"
                class="conditionClick"
                style="transform: translate(284px, 202px)"
                >动物圈</span
              >
            </el-tooltip>
            <el-tooltip
              class="box-item"
              :show-arrow="false"
              effect="light"
              placement="bottom"
              @click="ClickClassifyFlag15"
            >
              <template #content>
                <div class="custom-tooltip2" @click="ClickClassifyFlag15">
                  <span>鬼畜教程</span><span>音MAD</span>
                  <span>人力VOCALOID</span><span>鬼畜剧场</span
                  ><span>教程演示</span>
                </div>
              </template>
              <span
                v-if="clickClassifyFlag15 === false"
                @click="ClickClassifyFlag15"
                class="condition"
                style="transform: translate(430px, 207.5px)"
                >鬼畜</span
              >
              <span
                v-if="clickClassifyFlag15"
                class="conditionClick"
                style="transform: translate(394px, 202px)"
                >鬼畜</span
              >
            </el-tooltip>
            <el-tooltip
              class="box-item"
              :show-arrow="false"
              effect="light"
              placement="bottom"
              @click="ClickClassifyFlag16"
            >
              <template #content>
                <div class="custom-tooltip2" @click="ClickClassifyFlag16">
                  <span>美妆护肤</span><span>仿妆cos</span> <span>穿搭</span
                  ><span>时尚潮流</span>
                </div>
              </template>
              <span
                v-if="clickClassifyFlag16 === false"
                @click="ClickClassifyFlag16"
                class="condition"
                style="transform: translate(540px, 207.5px)"
                >时尚</span
              >
              <span
                v-if="clickClassifyFlag16"
                class="conditionClick"
                style="transform: translate(504px, 202px)"
                >时尚</span
              >
            </el-tooltip>
            <el-tooltip
              class="box-item"
              :show-arrow="false"
              effect="light"
              placement="bottom"
              @click="ClickClassifyFlag17"
            >
              <template #content>
                <div class="custom-tooltip2" @click="ClickClassifyFlag17">
                  <span>热点</span><span>环球</span> <span>社会</span
                  ><span>综合</span>
                </div>
              </template>
              <span
                v-if="clickClassifyFlag17 === false"
                @click="ClickClassifyFlag17"
                class="condition"
                style="transform: translate(650px, 207.5px)"
                >资讯</span
              >
              <span
                v-if="clickClassifyFlag17"
                class="conditionClick"
                style="transform: translate(614px, 202px)"
                >资讯</span
              >
            </el-tooltip>
            <el-tooltip
              class="box-item"
              :show-arrow="false"
              effect="light"
              placement="bottom"
              @click="ClickClassifyFlag18"
            >
              <template #content>
                <div class="custom-tooltip2" @click="ClickClassifyFlag18">
                  <span>资讯杂谈</span><span>CP安利</span> <span>颜值安利</span
                  ><span>娱乐粉丝创作</span><span>明星综合</span
                  ><span>综艺</span>
                </div>
              </template>
              <span
                v-if="clickClassifyFlag18 === false"
                @click="ClickClassifyFlag18"
                class="condition"
                style="transform: translate(760px, 207.5px)"
                >娱乐</span
              >
              <span
                v-if="clickClassifyFlag18"
                class="conditionClick"
                style="transform: translate(724px, 202px)"
                >娱乐</span
              >
            </el-tooltip>
            <el-tooltip
              class="box-item"
              :show-arrow="false"
              effect="light"
              placement="bottom"
              @click="ClickClassifyFlag19"
            >
              <template #content>
                <div class="custom-tooltip2" @click="ClickClassifyFlag19">
                  <span>影视杂谈</span><span>影视剪辑</span>
                  <span>影视整活</span><span>AI影像</span><span>预告·资讯</span
                  ><span>小剧场</span><span>短片</span><span>影视综合</span>
                </div>
              </template>
              <span
                v-if="clickClassifyFlag19 === false"
                @click="ClickClassifyFlag19"
                class="condition"
                style="transform: translate(870px, 207.5px)"
                >影视</span
              >
              <span
                v-if="clickClassifyFlag19"
                class="conditionClick"
                style="transform: translate(834px, 202px)"
                >影视</span
              >
            </el-tooltip>
            <el-tooltip
              class="box-item"
              :show-arrow="false"
              effect="light"
              placement="bottom"
              @click="ClickClassifyFlag20"
              :disabled="true"
            >
              <span
                v-if="clickClassifyFlag20 === false"
                @click="ClickClassifyFlag20"
                class="condition"
                style="transform: translate(973px, 207.5px)"
                >纪录片</span
              >
              <span
                v-if="clickClassifyFlag20"
                class="conditionClick"
                style="transform: translate(944px, 202px)"
                >纪录片</span
              >
            </el-tooltip>
            <el-tooltip
              class="box-item"
              :show-arrow="false"
              effect="light"
              placement="bottom"
              @click="ClickClassifyFlag21"
              :disabled="true"
            >
              <span
                v-if="clickClassifyFlag21 === false"
                @click="ClickClassifyFlag21"
                class="condition"
                style="transform: translate(1090px, 207.5px)"
                >电影</span
              >
              <span
                v-if="clickClassifyFlag21"
                class="conditionClick"
                style="transform: translate(1054px, 202px)"
                >电影</span
              >
            </el-tooltip>
            <el-tooltip
              class="box-item"
              :show-arrow="false"
              effect="light"
              placement="bottom"
              @click="ClickClassifyFlag22"
              :disabled="true"
            >
              <span
                v-if="clickClassifyFlag22 === false"
                @click="ClickClassifyFlag22"
                class="condition"
                style="transform: translate(1193px, 207.5px)"
                >电视剧</span
              >
              <span
                v-if="clickClassifyFlag22"
                class="conditionClick"
                style="transform: translate(1164px, 202px)"
                >电视剧</span
              >
            </el-tooltip>
            <div
              v-if="expanded"
              style="width: 100%; height: 170px; z-index: 0"
            ></div>
          </div>
          <div v-if="Videos.length === 0">
            <img
              src="/img/搜索空.png"
              style="
                width: 150px;
                height: 160px;
                transform: translate(637px, 133px);
              "
            />
            <span
              style="
                position: absolute;
                transform: translate(496.5px, 321px);
                font-size: 14px;
                color: #8F9794;
              "
              >今天真是寂寞如雪啊~</span
            >
          </div>
          <div class="bottomVideo">
            <div
              class="video-video"
              v-for="(video, index) in Videos"
              :key="video.id"
            >
              <div
                class="videoBox1"
                style="width: 247px"
                @mouseover="videoMouseover(video.videoId)"
                @mouseleave="videoMouseleave(video.videoId)"
              >
                <img
                  class="coverAddress"
                  @click="locationHerfVideo(video.videoId)"
                  :src="video.coverAddress"
                />
                <video
                  :id="video.videoId"
                  preload="none"
                  disablePictureInPicture
                  muted
                  loop
                  @click="locationHerfVideo(video.videoId)"
                  :src="video.videoAddress"
                ></video>
                <div
                  v-show="
                    store.userId !== null &&
                    (video.waitWatch === 0 || video.waitWatch === 1)
                  "
                  class="waitWatch"
                  @click="waitWatch(video.videoId)"
                  @mouseover="waitFont = 1"
                  @mouseleave="waitFont = 0"
                >
                  <img
                    v-show="video.waitWatch === 0"
                    src="/img/待看清单.png"
                    style="
                      width: 21px;
                      height: 18px;
                      opacity: 1 !important;
                      margin-left: 4px;
                    "
                  />
                  <img
                    v-show="video.waitWatch === 1"
                    src="/img/添加成功.png"
                    style="
                      width: 18px;
                      height: 15px;
                      opacity: 1 !important;
                      margin-left: 5px;
                    "
                  />
                  <span
                    v-show="waitFont === 1 && video.waitWatch === 0"
                    style="
                      width: 110px;
                      color: white;
                      z-index: 10;
                      font-size: 12px;
                      transform: translate(28.5px, -0.5px);
                      position: absolute;
                    "
                    >添加至稍后观看</span
                  >
                  <span
                    v-show="waitFont === 1 && video.waitWatch === 1"
                    style="
                      width: 110px;
                      color: white;
                      z-index: 10;
                      font-size: 12px;
                      transform: translate(28.5px, -0.5px);
                      position: absolute;
                    "
                    >已添加稍后观看</span
                  >
                </div>
                <div class="videoContent1">
                  <img
                    src="/img/播放量白.png"
                    style="
                      width: 15px;
                      height: 12px;
                      transform: translate(9.5px, 7.5px);
                      border-radius: 1px;
                    "
                  />
                  <span
                    style="
                      font-size: 12.6px;
                      transform: translate(13px, 9.5px);
                      position: absolute;
                    "
                    >{{ video.videoPlayNumber }}</span
                  >
                  <img
                    src="/img/弹幕白.png"
                    style="
                      width: 15px;
                      height: 12px;
                      transform: translate(57.5px, 7px);
                      border-radius: 1px;
                    "
                  />
                  <span
                    style="
                      font-size: 12.6px;
                      transform: translate(61px, 9.5px);
                      position: absolute;
                    "
                    >{{ video.videoScrollingNumber }}</span
                  >
                  <span
                    v-if="video.hour !== null"
                    style="
                      font-size: 12.6px;
                      transform: translate(159.5px, 9.5px);
                      position: absolute;
                    "
                  >
                    <span>{{ video.hour }}<span class="colon">:</span></span
                    >{{ video.minutes }}<span class="colon">:</span
                    >{{ video.second }}</span
                  >

                  <span
                    v-if="video.hour === null"
                    style="
                      font-size: 12.6px;
                      transform: translate(175.5px, 9.5px);
                      position: absolute;
                    "
                  >
                    {{ video.minutes }}<span class="colon">:</span
                    >{{ video.second }}</span
                  >
                </div>
                <div
                  style="
                    width: 245px;
                    height: 15px;
                    transform: translate(0px, -35px);
                    position: absolute;
                    opacity: 0;
                    z-index: -10;
                  "
                ></div>
                <div class="videoContent"></div>
              </div>
              <el-tooltip
                popper-class="custom-tooltip1"
                class="box-item"
                :show-after="300"
                effect="light"
                :content="video.videoTitle"
                placement="left"
                :show-arrow="false"
              >
                <span
                  id="result"
                  class="title"
                  @click="locationHerfVideo(video.videoId)"
                  v-html="highlightText(video.videoTitle)"
                ></span>
              </el-tooltip>
              <div
                style="
                  height: 32px;
                  position: absolute;
                  width: 245px;
                  transform: translate(0px, -8px);
                  opacity: 0;
                  z-index: -10;
                "
              ></div>
              <el-tooltip
                popper-class="custom-tooltip1"
                class="box-item"
                effect="light"
                :show-after="300"
                :content="video.userName"
                placement="left"
                :show-arrow="false"
              >
                <a
                  :href="'./home?homeMenu=1&userId=' + video.userId"
                  target="_blank"
                >
                  <span
                    class="videoBottomInfo"
                    @mouseover="upImgFlag = index - 10"
                    @mouseleave="upImgFlag = -index - 50000"
                    ><img
                      :src="upImgFlag === index - 10 ? upBlue : up"
                      style="
                        width: 15px;
                        height: 12px;
                        position: absolute;
                        transform: translate(1px, 26px);
                        border-radius: 0px;
                      "
                    />
                    <span class="upInfo"
                      >{{ video.userName }} &nbsp;·&nbsp;&nbsp;{{
                        video.createTime
                      }}</span
                    >
                  </span>
                </a>
              </el-tooltip>
            </div>
            <div
              style="
                width: 100%;
                display: flex;
                justify-content: center;
                transform: translate(70px, -200px);
                padding-bottom: 50px;
              "
            >
              <div v-show="acceptSearchData.videoTotal" class="page-container">
                <el-pagination
                  :current-page="videoPageNum"
                  :page-size="20"
                  layout="prev, pager, next"
                  :total="acceptSearchData.videoTotal"
                  :background="true"
                  @current-change="handleCurrentChangeVideo"
                />
                <span
                  >共 {{ Math.ceil(acceptSearchData.videoTotal / 20) }} 页 /
                  {{ acceptSearchData.videoTotal }} 个，跳至<input
                    type="number"
                    @keydown.enter="handleCurrentChangeVideo2"
                  />页</span
                >
              </div>
            </div>
          </div>
        </div>

        <div
          class="falseData"
          v-if="clickFlag3 || clickFlag4 || clickFlag5 || clickFlag6"
        >
          <img
            src="/img/搜索空.png"
            style="
              width: 150px;
              height: 160px;
              transform: translate(637px, 99px);
            "
          />
          <span
            style="
              position: absolute;
              transform: translate(496.5px, 287px);
              font-size: 14px;
              color: #8F9794;
            "
            >今天真是寂寞如雪啊~</span
          >
        </div>

        <div v-if="clickFlag7" class="userSort">
          <span
            v-if="clickUserFlag1 === false"
            @click="ClickUserFlag1"
            class="condition"
            style="transform: translate(86px, 36.5px)"
            >默认排序</span
          >
          <span
            v-if="clickUserFlag1"
            class="conditionClick"
            style="transform: translate(64px, 31px); width: 100px"
            >默认排序</span
          >
          <span
            v-if="clickUserFlag2 === false"
            @click="ClickUserFlag2"
            class="condition"
            style="transform: translate(189px, 36.5px)"
            >粉丝数由高到低</span
          >
          <span
            v-if="clickUserFlag2"
            class="conditionClick"
            style="transform: translate(174px, 31px)"
            >粉丝数由高到低</span
          >
          <span
            v-if="clickUserFlag3 === false"
            @click="ClickUserFlag3"
            class="condition"
            style="transform: translate(327px, 36.5px)"
            >粉丝数由低到高</span
          >
          <span
            v-if="clickUserFlag3"
            class="conditionClick"
            style="transform: translate(312px, 31px)"
            >粉丝数由低到高</span
          >
          <span
            v-if="clickUserFlag4 === false"
            @click="ClickUserFlag4"
            class="condition"
            style="transform: translate(465px, 36.5px)"
            >Lv等级由高到低</span
          >
          <span
            v-if="clickUserFlag4"
            class="conditionClick"
            style="transform: translate(450px, 31px)"
            >Lv等级由高到低</span
          >
          <span
            v-if="clickUserFlag5 === false"
            @click="ClickUserFlag5"
            class="condition"
            style="transform: translate(603px, 36.5px)"
            >Lv等级由低到高</span
          >
          <span
            v-if="clickUserFlag5"
            class="conditionClick"
            style="transform: translate(588px, 31px)"
            >Lv等级由低到高</span
          >
          <div v-if="searchUserList.length === 0">
            <img
              src="/img/搜索空.png"
              style="
                width: 150px;
                height: 160px;
                transform: translate(637px, 133px);
              "
            />
            <span
              style="
                position: absolute;
                transform: translate(496.5px, 321px);
                font-size: 14px;
                color: #8F9794;
              "
              >今天真是寂寞如雪啊~</span
            >
          </div>
          <div class="searchUsers">
            <div style="display: flex; flex-wrap: wrap">
              <div
                class="usersContent"
                v-for="(user, index) in searchUserList"
                :key="index"
              >
                <img
                  :src="user.avatarAddress"
                  @click="openHome(user.userId)"
                  style="
                    width: 84.8px;
                    height: 84.8px;
                    border-radius: 50%;
                    cursor: pointer;
                  "
                />

                <el-tooltip
                  popper-class="custom-tooltip1"
                  class="box-item"
                  effect="light"
                  :content="user.userName"
                  placement="right"
                  :show-arrow="false"
                  :offset="20"
                >
                  <span
                    class="user-Name"
                    @click="openHome(user.userId)"
                    style="
                      width: auto;
                      display: inline-block;
                      transform: translate(17px, -65px);
                      cursor: pointer;
                    "
                    >{{ user.userName }}
                    <img
                      v-if="user.grade === 0"
                      src="/img/0级.png"
                      style="
                        width: 23.5px;
                        height: 12px;
                        margin-left: 10.5px;
                        margin-top: 6.5px;
                      "
                    />
                    <img
                      v-if="user.grade === 1"
                      src="/img/1级.png"
                      style="
                        width: 23.5px;
                        height: 12px;
                        margin-left: 10.5px;
                        margin-top: 6.5px;
                      "
                    />
                    <img
                      v-if="user.grade === 2"
                      src="/img/2级.png"
                      style="
                        width: 23.5px;
                        height: 12px;
                        margin-left: 10.5px;
                        margin-top: 6.5px;
                      "
                    />
                    <img
                      v-if="user.grade === 3"
                      src="/img/3级.png"
                      style="
                        width: 23.5px;
                        height: 12px;
                        margin-left: 10.5px;
                        margin-top: 6.5px;
                      "
                    />
                    <img
                      v-if="user.grade === 4"
                      src="/img/4级.png"
                      style="
                        width: 23.5px;
                        height: 12px;
                        margin-left: 10.5px;
                        margin-top: 6.5px;
                      "
                    />
                    <img
                      v-if="user.grade === 5"
                      src="/img/5级.png"
                      style="
                        width: 23.5px;
                        height: 12px;
                        margin-left: 10.5px;
                        margin-top: 6.5px;
                      "
                    />
                    <img
                      v-if="user.grade === 6"
                      src="/img/6级.png"
                      style="
                        width: 23.5px;
                        height: 12px;
                        margin-left: 10.5px;
                        margin-top: 6.5px;
                      "
                    />
                  </span>
                </el-tooltip>
                <el-tooltip
                  popper-class="custom-tooltip1"
                  class="box-item"
                  effect="light"
                  :content="
                    userContent(
                      user.fansNumber,
                      user.videoNumber,
                      user.introduce
                    )
                  "
                  placement="right"
                  :offset="-350"
                  :show-arrow="false"
                >
                  <div class="userInfo-Content">
                    <span>{{ user.fansNumber }}粉丝 </span>
                    <span style="margin-left: 5.5px; margin-right: 5.5px"
                      >·</span
                    >
                    <span>{{ user.videoNumber }}个视频 </span>
                    <span class="introduce">{{ user.introduce }}</span>
                  </div>
                </el-tooltip>
                <div
                  v-if="user.follow === null"
                  class="follow"
                  v-debounce
                  @click="addFollowAxios(user.userId)"
                >
                  + 关注
                </div>
                <div
                  v-else
                  class="deleteFollow"
                  v-debounce
                  @click="deleteFollowAxios(user.userId)"
                >
                  已关注
                </div>
              </div>
              <div
                :style="{
                  width: windowWidth + 'px',
                }"
                style="
                  width: 100vw;
                  display: flex;
                  justify-content: center;
                  margin-top: 70px;
                  margin-bottom: 50px;
                "
              >
                <div v-show="acceptSearchData.userTotal" class="page-container">
                  <el-pagination
                    :current-page="userPageNum"
                    :page-size="20"
                    layout="prev, pager, next"
                    :total="acceptSearchData.userTotal"
                    :background="true"
                    @current-change="handleCurrentChangeUser"
                  />
                  <span
                    >共 {{ Math.ceil(acceptSearchData.userTotal / 20) }} 页 /
                    {{ acceptSearchData.userTotal }} 个，跳至<input
                      type="number"
                      @keydown.enter="handleCurrentChangeUser2"
                    />页</span
                  >
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    <el-backtop :right="5"/>
  </div>
</template>

<script>
import head1 from "../components/mainHead.vue";
import Searcha from "./searcha";
const up = "/img/author-badge-default.png"
const upBlue = "/img/author-badge-blue.png"
import { computed, reactive, nextTick, onMounted, ref, watch, onUnmounted } from "vue";
import apiClient from "../services/apiClient";
import { ElMessage } from "element-plus";
import { useGlobalStore } from "../store/store";
export default {
  name: "MainSearch",
  components: {
    head1,
    Searcha,
  },

  setup() {
    const loginDialogVisibleFlag = ref(0);
    const store = useGlobalStore();
    const onloadPage = ref(false);
    const videoPageNum = ref(1);
    const userPageNum = ref(1);
    const acceptSearchData = reactive({
      userId: 0,
      keyWord: "",
      sort: 0,
      date: 0,
      time: 0,
      classify: "全部",
      startTIme: null,
      startTime: "",
      endTime: "",
      userSort: 0,
      videoPageNum: 1,
      userPageNum: 1,
      classifyIndex: "",
      videoTotal: 0,
      userTotal: 0,
    });
    const searchUserList = reactive([]);
    const expanded = ref(false);
    const waitFont = ref(0);
    const upImgFlag = ref(false);
    const windowWidth = ref(0);
    const Videos = reactive([]);
    const searcha = ref(null);
    const searcha2 = ref(null);
    function userContent(fansNumber, videoNumber, introduce) {
      if (introduce !== null)
        return `${fansNumber}粉丝 ·  ${videoNumber}个视频 · ${introduce}`;
      else return `${fansNumber}粉丝 ·  ${videoNumber}个视频`;
    }
    const isVisible = ref(false); // 用于控制盒子的可见性
    const handleScroll = () => {
      const scrollPosition = window.scrollY; // 当前滚动距离
      isVisible.value = scrollPosition > 155; // 当滚动超过155px时显示盒子
    };
    const datea = ref("");
const clickFlag = ref(1);
const clickFlag1 = computed(() => clickFlag.value === 1);
const clickFlag2 = computed(() => clickFlag.value === 2);
const clickFlag3 = computed(() => clickFlag.value === 3);
const clickFlag4 = computed(() => clickFlag.value === 4);
const clickFlag5 = computed(() => clickFlag.value === 5);
const clickFlag6 = computed(() => clickFlag.value === 6);
const clickFlag7 = computed(() => clickFlag.value === 7);

// 结果分类页签。原来是 7 个 ref + 7 个各自把另外 6 个置 false 的函数，
// 写错一个就会让两个 tab 同时高亮。改成单一 clickFlag，页签表由它派生。
const resultTabs = computed(() => [
  { flag: 1, label: "综合", total: null },
  { flag: 2, label: "视频", total: acceptSearchData.videoTotal },
  { flag: 3, label: "番剧", total: 0 },
  { flag: 4, label: "影视", total: 0 },
  { flag: 5, label: "直播", total: 0 },
  { flag: 6, label: "专栏", total: 0 },
  { flag: 7, label: "用户", total: acceptSearchData.userTotal },
]);

const tabEls = new Map();
const sortLine = reactive({ visible: false, x: 0, w: 0 });

function setTabEl(el, flag) {
  if (el) tabEls.set(flag, el);
  else tabEls.delete(flag);
}

function measureSortLine() {
  const el = tabEls.get(clickFlag.value);
  if (!el) {
    sortLine.visible = false;
    return;
  }
  sortLine.x = el.offsetLeft;
  sortLine.w = el.offsetWidth;
  sortLine.visible = true;
}

watch(
  () => [clickFlag.value, acceptSearchData.videoTotal, acceptSearchData.userTotal],
  () => {
    nextTick(measureSortLine);
  }
);

    onMounted(() => {
      nextTick(measureSortLine);
    });
    const clickSortFlag1 = ref(true);
    const clickSortFlag2 = ref(false);
    const clickSortFlag3 = ref(false);
    const clickSortFlag4 = ref(false);
    const clickSortFlag5 = ref(false);
    const clickUserFlag1 = ref(true);
    const clickUserFlag2 = ref(false);
    const clickUserFlag3 = ref(false);
    const clickUserFlag4 = ref(false);
    const clickUserFlag5 = ref(false);
    const clickDateFlag1 = ref(true);
    const clickDateFlag2 = ref(false);
    const clickDateFlag3 = ref(false);
    const clickDateFlag4 = ref(false);
    const clickDateFlag5 = ref(false);
    const clickTimeFlag1 = ref(true);
    const clickTimeFlag2 = ref(false);
    const clickTimeFlag3 = ref(false);
    const clickTimeFlag4 = ref(false);
    const clickTimeFlag5 = ref(false);
    const clickClassifyFlag1 = ref(true);
    const clickClassifyFlag2 = ref(false);
    const clickClassifyFlag3 = ref(false);
    const clickClassifyFlag4 = ref(false);
    const clickClassifyFlag5 = ref(false);
    const clickClassifyFlag6 = ref(false);
    const clickClassifyFlag7 = ref(false);
    const clickClassifyFlag8 = ref(false);
    const clickClassifyFlag9 = ref(false);
    const clickClassifyFlag10 = ref(false);
    const clickClassifyFlag11 = ref(false);
    const clickClassifyFlag12 = ref(false);
    const clickClassifyFlag13 = ref(false);
    const clickClassifyFlag14 = ref(false);
    const clickClassifyFlag15 = ref(false);
    const clickClassifyFlag16 = ref(false);
    const clickClassifyFlag17 = ref(false);
    const clickClassifyFlag18 = ref(false);
    const clickClassifyFlag19 = ref(false);
    const clickClassifyFlag20 = ref(false);
    const clickClassifyFlag21 = ref(false);
    const clickClassifyFlag22 = ref(false);
    const videoAutoPlayTIme = {};
    watch(datea, (newValue) => {
      if (newValue !== null) {
        if (newValue.length !== 0) {
          const date1 = new Date(datea.value[0]);
          const date2 = new Date(datea.value[1]);
          if (!isNaN(date1.getTime())) {
            acceptSearchData.startTime = date1.toISOString();
          }
          if (!isNaN(date2.getTime())) {
            acceptSearchData.endTime = date2.toISOString();
          }
          videoPageNum.value = 1;
          searchByKeyWordVideo();
        }
        if (newValue.length === 0) {
          acceptSearchData.startTIme = "";
          acceptSearchData.endTime = "";
        }

        if (newValue.length !== 0) {
          clickDateFlag1.value = false;
          clickDateFlag2.value = false;
          clickDateFlag3.value = false;
          clickDateFlag4.value = false;
          clickDateFlag5.value = true;
        } else if (
          clickDateFlag2.value &&
          clickDateFlag3.value &&
          clickDateFlag4.value
        ) {
          clickDateFlag1.value = true;
          clickDateFlag5.value = false;
        }
      }
    });

   function handleUrlChange(keyword){
      searcha.value.Content = keyword || '';
      searcha2.value.Content = keyword || '';
      if (clickFlag1.value || clickFlag2.value) {
        videoPageNum.value = 1;
        searchByKeyWordVideo();
      }else if(clickFlag7.value){
        userPageNum.value = 1;
        selectUsersAxios();
      }
   }

    function ClickFlag(n) {
      clickFlag.value = n;
      nextTick(measureSortLine);
      if (n === 7) {
        selectUsersAxios();
      }
    }
    function ClickSortFlag1() {
      clickSortFlag1.value = true;
      clickSortFlag2.value = false;
      clickSortFlag3.value = false;
      clickSortFlag4.value = false;
      clickSortFlag5.value = false;

      acceptSearchData.sort = 0;
      searchByKeyWordVideo();
    }
    function ClickSortFlag2() {
      clickSortFlag1.value = false;
      clickSortFlag2.value = true;
      clickSortFlag3.value = false;
      clickSortFlag4.value = false;
      clickSortFlag5.value = false;

      acceptSearchData.sort = 1;
      searchByKeyWordVideo();
    }
    function ClickSortFlag3() {
      clickSortFlag1.value = false;
      clickSortFlag2.value = false;
      clickSortFlag3.value = true;
      clickSortFlag4.value = false;
      clickSortFlag5.value = false;

      acceptSearchData.sort = 2;
      searchByKeyWordVideo();
    }
    function ClickSortFlag4() {
      clickSortFlag1.value = false;
      clickSortFlag2.value = false;
      clickSortFlag3.value = false;
      clickSortFlag4.value = true;
      clickSortFlag5.value = false;

      acceptSearchData.sort = 3;
      searchByKeyWordVideo();
    }

    function ClickSortFlag5() {
      clickSortFlag1.value = false;
      clickSortFlag2.value = false;
      clickSortFlag3.value = false;
      clickSortFlag4.value = false;
      clickSortFlag5.value = true;

      acceptSearchData.sort = 4;
      searchByKeyWordVideo();
    }
    function ClickUserFlag1() {
      clickUserFlag1.value = true;
      clickUserFlag2.value = false;
      clickUserFlag3.value = false;
      clickUserFlag4.value = false;
      clickUserFlag5.value = false;

      acceptSearchData.userSort = 0;
      selectUsersAxios();
    }
    function ClickUserFlag2() {
      clickUserFlag1.value = false;
      clickUserFlag2.value = true;
      clickUserFlag3.value = false;
      clickUserFlag4.value = false;
      clickUserFlag5.value = false;
      acceptSearchData.userSort = 1;
      selectUsersAxios();
    }
    function ClickUserFlag3() {
      clickUserFlag1.value = false;
      clickUserFlag2.value = false;
      clickUserFlag3.value = true;
      clickUserFlag4.value = false;
      clickUserFlag5.value = false;
      acceptSearchData.userSort = 2;
      selectUsersAxios();
    }
    function ClickUserFlag4() {
      clickUserFlag1.value = false;
      clickUserFlag2.value = false;
      clickUserFlag3.value = false;
      clickUserFlag4.value = true;
      clickUserFlag5.value = false;
      acceptSearchData.userSort = 3;
      selectUsersAxios();
    }
    function ClickUserFlag5() {
      clickUserFlag1.value = false;
      clickUserFlag2.value = false;
      clickUserFlag3.value = false;
      clickUserFlag4.value = false;
      clickUserFlag5.value = true;
      acceptSearchData.userSort = 4;
      selectUsersAxios();
    }
    function ClickDateFlag1() {
      clickDateFlag1.value = true;
      clickDateFlag2.value = false;
      clickDateFlag3.value = false;
      clickDateFlag4.value = false;
      clickDateFlag5.value = false;
      datea.value = "";
      videoPageNum.value = 1;
      acceptSearchData.date = 0;
      searchByKeyWordVideo();
    }
    function ClickDateFlag2() {
      clickDateFlag1.value = false;
      clickDateFlag2.value = true;
      clickDateFlag3.value = false;
      clickDateFlag4.value = false;
      clickDateFlag5.value = false;
      datea.value = "";
      videoPageNum.value = 1;
      acceptSearchData.date = 1;
      searchByKeyWordVideo();
    }
    function ClickDateFlag3() {
      clickDateFlag1.value = false;
      clickDateFlag2.value = false;
      clickDateFlag3.value = true;
      clickDateFlag4.value = false;
      clickDateFlag5.value = false;
      datea.value = "";
      videoPageNum.value = 1;
      acceptSearchData.date = 2;
      searchByKeyWordVideo();
    }
    function ClickDateFlag4() {
      clickDateFlag1.value = false;
      clickDateFlag2.value = false;
      clickDateFlag3.value = false;
      clickDateFlag4.value = true;
      clickDateFlag5.value = false;
      datea.value = "";
      videoPageNum.value = 1;
      acceptSearchData.date = 3;
      searchByKeyWordVideo();
    }
    function ClickDateFlag5() {
      if (datea.value !== null) {
        clickDateFlag1.value = false;
        clickDateFlag2.value = false;
        clickDateFlag3.value = false;
        clickDateFlag4.value = false;
        clickDateFlag5.value = true;
        acceptSearchData.date = 4;
      }
    }
    function ClickTimeFlag1() {
      clickTimeFlag1.value = true;
      clickTimeFlag2.value = false;
      clickTimeFlag3.value = false;
      clickTimeFlag4.value = false;
      clickTimeFlag5.value = false;
      videoPageNum.value = 1;
      acceptSearchData.time = 0;
      searchByKeyWordVideo();
    }
    function ClickTimeFlag2() {
      clickTimeFlag1.value = false;
      clickTimeFlag2.value = true;
      clickTimeFlag3.value = false;
      clickTimeFlag4.value = false;
      clickTimeFlag5.value = false;
      videoPageNum.value = 1;
      acceptSearchData.time = 1;
      searchByKeyWordVideo();
    }
    function ClickTimeFlag3() {
      clickTimeFlag1.value = false;
      clickTimeFlag2.value = false;
      clickTimeFlag3.value = true;
      clickTimeFlag4.value = false;
      clickTimeFlag5.value = false;
      videoPageNum.value = 1;
      acceptSearchData.time = 2;
      searchByKeyWordVideo();
    }
    function ClickTimeFlag4() {
      clickTimeFlag1.value = false;
      clickTimeFlag2.value = false;
      clickTimeFlag3.value = false;
      clickTimeFlag4.value = true;
      clickTimeFlag5.value = false;
      videoPageNum.value = 1;
      acceptSearchData.time = 3;
      searchByKeyWordVideo();
    }
    function ClickTimeFlag5() {
      clickTimeFlag1.value = false;
      clickTimeFlag2.value = false;
      clickTimeFlag3.value = false;
      clickTimeFlag4.value = false;
      clickTimeFlag5.value = true;
      videoPageNum.value = 1;
      acceptSearchData.time = 4;
      searchByKeyWordVideo();
    }
    function ClickClassifyFlag1() {
      clickClassifyFlag1.value = true;
      clickClassifyFlag2.value = false;
      clickClassifyFlag3.value = false;
      clickClassifyFlag4.value = false;
      clickClassifyFlag5.value = false;
      clickClassifyFlag6.value = false;
      clickClassifyFlag7.value = false;
      clickClassifyFlag8.value = false;
      clickClassifyFlag9.value = false;
      clickClassifyFlag10.value = false;
      clickClassifyFlag11.value = false;
      clickClassifyFlag12.value = false;
      clickClassifyFlag13.value = false;
      clickClassifyFlag14.value = false;
      clickClassifyFlag15.value = false;
      clickClassifyFlag16.value = false;
      clickClassifyFlag17.value = false;
      clickClassifyFlag18.value = false;
      clickClassifyFlag19.value = false;
      clickClassifyFlag20.value = false;
      clickClassifyFlag21.value = false;
      clickClassifyFlag22.value = false;
      videoPageNum.value = 1;
      acceptSearchData.classify = "全部";
      searchByKeyWordVideo();
    }
    function ClickClassifyFlag2() {
      clickClassifyFlag1.value = false;
      clickClassifyFlag2.value = true;
      clickClassifyFlag3.value = false;
      clickClassifyFlag4.value = false;
      clickClassifyFlag5.value = false;
      clickClassifyFlag6.value = false;
      clickClassifyFlag7.value = false;
      clickClassifyFlag8.value = false;
      clickClassifyFlag9.value = false;
      clickClassifyFlag10.value = false;
      clickClassifyFlag11.value = false;
      clickClassifyFlag12.value = false;
      clickClassifyFlag13.value = false;
      clickClassifyFlag14.value = false;
      clickClassifyFlag15.value = false;
      clickClassifyFlag16.value = false;
      clickClassifyFlag17.value = false;
      clickClassifyFlag18.value = false;
      clickClassifyFlag19.value = false;
      clickClassifyFlag20.value = false;
      clickClassifyFlag21.value = false;
      clickClassifyFlag22.value = false;
      acceptSearchData.classify = "动画";
      videoPageNum.value = 1;
      searchByKeyWordVideo();
    }
    function ClickClassifyFlag3() {
      clickClassifyFlag1.value = false;
      clickClassifyFlag2.value = false;
      clickClassifyFlag3.value = true;
      clickClassifyFlag4.value = false;
      clickClassifyFlag5.value = false;
      clickClassifyFlag6.value = false;
      clickClassifyFlag7.value = false;
      clickClassifyFlag8.value = false;
      clickClassifyFlag9.value = false;
      clickClassifyFlag10.value = false;
      clickClassifyFlag11.value = false;
      clickClassifyFlag12.value = false;
      clickClassifyFlag13.value = false;
      clickClassifyFlag14.value = false;
      clickClassifyFlag15.value = false;
      clickClassifyFlag16.value = false;
      clickClassifyFlag17.value = false;
      clickClassifyFlag18.value = false;
      clickClassifyFlag19.value = false;
      clickClassifyFlag20.value = false;
      clickClassifyFlag21.value = false;
      clickClassifyFlag22.value = false;
      acceptSearchData.classify = "番剧";
      videoPageNum.value = 1;
      searchByKeyWordVideo();
    }
    function ClickClassifyFlag4() {
      clickClassifyFlag1.value = false;
      clickClassifyFlag2.value = false;
      clickClassifyFlag3.value = false;
      clickClassifyFlag4.value = true;
      clickClassifyFlag5.value = false;
      clickClassifyFlag6.value = false;
      clickClassifyFlag7.value = false;
      clickClassifyFlag8.value = false;
      clickClassifyFlag9.value = false;
      clickClassifyFlag10.value = false;
      clickClassifyFlag11.value = false;
      clickClassifyFlag12.value = false;
      clickClassifyFlag13.value = false;
      clickClassifyFlag14.value = false;
      clickClassifyFlag15.value = false;
      clickClassifyFlag16.value = false;
      clickClassifyFlag17.value = false;
      clickClassifyFlag18.value = false;
      clickClassifyFlag19.value = false;
      clickClassifyFlag20.value = false;
      clickClassifyFlag21.value = false;
      clickClassifyFlag22.value = false;
      acceptSearchData.classify = "国创";
      videoPageNum.value = 1;
      searchByKeyWordVideo();
    }
    function ClickClassifyFlag5() {
      clickClassifyFlag1.value = false;
      clickClassifyFlag2.value = false;
      clickClassifyFlag3.value = false;
      clickClassifyFlag4.value = false;
      clickClassifyFlag5.value = true;
      clickClassifyFlag6.value = false;
      clickClassifyFlag7.value = false;
      clickClassifyFlag8.value = false;
      clickClassifyFlag9.value = false;
      clickClassifyFlag10.value = false;
      clickClassifyFlag11.value = false;
      clickClassifyFlag12.value = false;
      clickClassifyFlag13.value = false;
      clickClassifyFlag14.value = false;
      clickClassifyFlag15.value = false;
      clickClassifyFlag16.value = false;
      clickClassifyFlag17.value = false;
      clickClassifyFlag18.value = false;
      clickClassifyFlag19.value = false;
      clickClassifyFlag20.value = false;
      clickClassifyFlag21.value = false;
      clickClassifyFlag22.value = false;
      acceptSearchData.classify = "音乐";
      videoPageNum.value = 1;
      searchByKeyWordVideo();
    }
    function ClickClassifyFlag6() {
      clickClassifyFlag1.value = false;
      clickClassifyFlag2.value = false;
      clickClassifyFlag3.value = false;
      clickClassifyFlag4.value = false;
      clickClassifyFlag5.value = false;
      clickClassifyFlag6.value = true;
      clickClassifyFlag7.value = false;
      clickClassifyFlag8.value = false;
      clickClassifyFlag9.value = false;
      clickClassifyFlag10.value = false;
      clickClassifyFlag11.value = false;
      clickClassifyFlag12.value = false;
      clickClassifyFlag13.value = false;
      clickClassifyFlag14.value = false;
      clickClassifyFlag15.value = false;
      clickClassifyFlag16.value = false;
      clickClassifyFlag17.value = false;
      clickClassifyFlag18.value = false;
      clickClassifyFlag19.value = false;
      clickClassifyFlag20.value = false;
      clickClassifyFlag21.value = false;
      clickClassifyFlag22.value = false;
      acceptSearchData.classify = "舞蹈";
      videoPageNum.value = 1;
      searchByKeyWordVideo();
    }
    function ClickClassifyFlag7() {
      clickClassifyFlag1.value = false;
      clickClassifyFlag2.value = false;
      clickClassifyFlag3.value = false;
      clickClassifyFlag4.value = false;
      clickClassifyFlag5.value = false;
      clickClassifyFlag6.value = false;
      clickClassifyFlag7.value = true;
      clickClassifyFlag8.value = false;
      clickClassifyFlag9.value = false;
      clickClassifyFlag10.value = false;
      clickClassifyFlag11.value = false;
      clickClassifyFlag12.value = false;
      clickClassifyFlag13.value = false;
      clickClassifyFlag14.value = false;
      clickClassifyFlag15.value = false;
      clickClassifyFlag16.value = false;
      clickClassifyFlag17.value = false;
      clickClassifyFlag18.value = false;
      clickClassifyFlag19.value = false;
      clickClassifyFlag20.value = false;
      clickClassifyFlag21.value = false;
      clickClassifyFlag22.value = false;
      acceptSearchData.classify = "游戏";
      videoPageNum.value = 1;
      searchByKeyWordVideo();
    }
    function ClickClassifyFlag8() {
      clickClassifyFlag1.value = false;
      clickClassifyFlag2.value = false;
      clickClassifyFlag3.value = false;
      clickClassifyFlag4.value = false;
      clickClassifyFlag5.value = false;
      clickClassifyFlag6.value = false;
      clickClassifyFlag7.value = false;
      clickClassifyFlag8.value = true;
      clickClassifyFlag9.value = false;
      clickClassifyFlag10.value = false;
      clickClassifyFlag11.value = false;
      clickClassifyFlag12.value = false;
      clickClassifyFlag13.value = false;
      clickClassifyFlag14.value = false;
      clickClassifyFlag15.value = false;
      clickClassifyFlag16.value = false;
      clickClassifyFlag17.value = false;
      clickClassifyFlag18.value = false;
      clickClassifyFlag19.value = false;
      clickClassifyFlag20.value = false;
      clickClassifyFlag21.value = false;
      clickClassifyFlag22.value = false;
      acceptSearchData.classify = "知识";
      videoPageNum.value = 1;
      searchByKeyWordVideo();
    }
    function ClickClassifyFlag9() {
      clickClassifyFlag1.value = false;
      clickClassifyFlag2.value = false;
      clickClassifyFlag3.value = false;
      clickClassifyFlag4.value = false;
      clickClassifyFlag5.value = false;
      clickClassifyFlag6.value = false;
      clickClassifyFlag7.value = false;
      clickClassifyFlag8.value = false;
      clickClassifyFlag9.value = true;
      clickClassifyFlag10.value = false;
      clickClassifyFlag11.value = false;
      clickClassifyFlag12.value = false;
      clickClassifyFlag13.value = false;
      clickClassifyFlag14.value = false;
      clickClassifyFlag15.value = false;
      clickClassifyFlag16.value = false;
      clickClassifyFlag17.value = false;
      clickClassifyFlag18.value = false;
      clickClassifyFlag19.value = false;
      clickClassifyFlag20.value = false;
      clickClassifyFlag21.value = false;
      clickClassifyFlag22.value = false;
      acceptSearchData.classify = "科技";
      videoPageNum.value = 1;
      searchByKeyWordVideo();
    }
    function ClickClassifyFlag10() {
      clickClassifyFlag1.value = false;
      clickClassifyFlag2.value = false;
      clickClassifyFlag3.value = false;
      clickClassifyFlag4.value = false;
      clickClassifyFlag5.value = false;
      clickClassifyFlag6.value = false;
      clickClassifyFlag7.value = false;
      clickClassifyFlag8.value = false;
      clickClassifyFlag9.value = false;
      clickClassifyFlag10.value = true;
      clickClassifyFlag11.value = false;
      clickClassifyFlag12.value = false;
      clickClassifyFlag13.value = false;
      clickClassifyFlag14.value = false;
      clickClassifyFlag15.value = false;
      clickClassifyFlag16.value = false;
      clickClassifyFlag17.value = false;
      clickClassifyFlag18.value = false;
      clickClassifyFlag19.value = false;
      clickClassifyFlag20.value = false;
      clickClassifyFlag21.value = false;
      clickClassifyFlag22.value = false;
      acceptSearchData.classify = "运动";
      videoPageNum.value = 1;
      searchByKeyWordVideo();
    }
    function ClickClassifyFlag11() {
      clickClassifyFlag1.value = false;
      clickClassifyFlag2.value = false;
      clickClassifyFlag3.value = false;
      clickClassifyFlag4.value = false;
      clickClassifyFlag5.value = false;
      clickClassifyFlag6.value = false;
      clickClassifyFlag7.value = false;
      clickClassifyFlag8.value = false;
      clickClassifyFlag9.value = false;
      clickClassifyFlag10.value = false;
      clickClassifyFlag11.value = true;
      clickClassifyFlag12.value = false;
      clickClassifyFlag13.value = false;
      clickClassifyFlag14.value = false;
      clickClassifyFlag15.value = false;
      clickClassifyFlag16.value = false;
      clickClassifyFlag17.value = false;
      clickClassifyFlag18.value = false;
      clickClassifyFlag19.value = false;
      clickClassifyFlag20.value = false;
      clickClassifyFlag21.value = false;
      clickClassifyFlag22.value = false;
      acceptSearchData.classify = "汽车";
      videoPageNum.value = 1;
      searchByKeyWordVideo();
    }
    function ClickClassifyFlag12() {
      clickClassifyFlag1.value = false;
      clickClassifyFlag2.value = false;
      clickClassifyFlag3.value = false;
      clickClassifyFlag4.value = false;
      clickClassifyFlag5.value = false;
      clickClassifyFlag6.value = false;
      clickClassifyFlag7.value = false;
      clickClassifyFlag8.value = false;
      clickClassifyFlag9.value = false;
      clickClassifyFlag10.value = false;
      clickClassifyFlag11.value = false;
      clickClassifyFlag12.value = true;
      clickClassifyFlag13.value = false;
      clickClassifyFlag14.value = false;
      clickClassifyFlag15.value = false;
      clickClassifyFlag16.value = false;
      clickClassifyFlag17.value = false;
      clickClassifyFlag18.value = false;
      clickClassifyFlag19.value = false;
      clickClassifyFlag20.value = false;
      clickClassifyFlag21.value = false;
      clickClassifyFlag22.value = false;
      acceptSearchData.classify = "生活";
      videoPageNum.value = 1;
      searchByKeyWordVideo();
    }
    function ClickClassifyFlag13() {
      clickClassifyFlag1.value = false;
      clickClassifyFlag2.value = false;
      clickClassifyFlag3.value = false;
      clickClassifyFlag4.value = false;
      clickClassifyFlag5.value = false;
      clickClassifyFlag6.value = false;
      clickClassifyFlag7.value = false;
      clickClassifyFlag8.value = false;
      clickClassifyFlag9.value = false;
      clickClassifyFlag10.value = false;
      clickClassifyFlag11.value = false;
      clickClassifyFlag12.value = false;
      clickClassifyFlag13.value = true;
      clickClassifyFlag14.value = false;
      clickClassifyFlag15.value = false;
      clickClassifyFlag16.value = false;
      clickClassifyFlag17.value = false;
      clickClassifyFlag18.value = false;
      clickClassifyFlag19.value = false;
      clickClassifyFlag20.value = false;
      clickClassifyFlag21.value = false;
      clickClassifyFlag22.value = false;
      acceptSearchData.classify = "美食";
      videoPageNum.value = 1;
      searchByKeyWordVideo();
    }
    function ClickClassifyFlag14() {
      clickClassifyFlag1.value = false;
      clickClassifyFlag2.value = false;
      clickClassifyFlag3.value = false;
      clickClassifyFlag4.value = false;
      clickClassifyFlag5.value = false;
      clickClassifyFlag6.value = false;
      clickClassifyFlag7.value = false;
      clickClassifyFlag8.value = false;
      clickClassifyFlag9.value = false;
      clickClassifyFlag10.value = false;
      clickClassifyFlag11.value = false;
      clickClassifyFlag12.value = false;
      clickClassifyFlag13.value = false;
      clickClassifyFlag14.value = true;
      clickClassifyFlag15.value = false;
      clickClassifyFlag16.value = false;
      clickClassifyFlag17.value = false;
      clickClassifyFlag18.value = false;
      clickClassifyFlag19.value = false;
      clickClassifyFlag20.value = false;
      clickClassifyFlag21.value = false;
      clickClassifyFlag22.value = false;
      acceptSearchData.classify = "动物圈";
      videoPageNum.value = 1;
      searchByKeyWordVideo();
    }
    function ClickClassifyFlag15() {
      clickClassifyFlag1.value = false;
      clickClassifyFlag2.value = false;
      clickClassifyFlag3.value = false;
      clickClassifyFlag4.value = false;
      clickClassifyFlag5.value = false;
      clickClassifyFlag6.value = false;
      clickClassifyFlag7.value = false;
      clickClassifyFlag8.value = false;
      clickClassifyFlag9.value = false;
      clickClassifyFlag10.value = false;
      clickClassifyFlag11.value = false;
      clickClassifyFlag12.value = false;
      clickClassifyFlag13.value = false;
      clickClassifyFlag14.value = false;
      clickClassifyFlag15.value = true;
      clickClassifyFlag16.value = false;
      clickClassifyFlag17.value = false;
      clickClassifyFlag18.value = false;
      clickClassifyFlag19.value = false;
      clickClassifyFlag20.value = false;
      clickClassifyFlag21.value = false;
      clickClassifyFlag22.value = false;
      acceptSearchData.classify = "鬼畜";
      videoPageNum.value = 1;
      searchByKeyWordVideo();
    }
    function ClickClassifyFlag16() {
      clickClassifyFlag1.value = false;
      clickClassifyFlag2.value = false;
      clickClassifyFlag3.value = false;
      clickClassifyFlag4.value = false;
      clickClassifyFlag5.value = false;
      clickClassifyFlag6.value = false;
      clickClassifyFlag7.value = false;
      clickClassifyFlag8.value = false;
      clickClassifyFlag9.value = false;
      clickClassifyFlag10.value = false;
      clickClassifyFlag11.value = false;
      clickClassifyFlag12.value = false;
      clickClassifyFlag13.value = false;
      clickClassifyFlag14.value = false;
      clickClassifyFlag15.value = false;
      clickClassifyFlag16.value = true;
      clickClassifyFlag17.value = false;
      clickClassifyFlag18.value = false;
      clickClassifyFlag19.value = false;
      clickClassifyFlag20.value = false;
      clickClassifyFlag21.value = false;
      clickClassifyFlag22.value = false;
      acceptSearchData.classify = "时尚";
      videoPageNum.value = 1;
      searchByKeyWordVideo();
    }
    function ClickClassifyFlag17() {
      clickClassifyFlag1.value = false;
      clickClassifyFlag2.value = false;
      clickClassifyFlag3.value = false;
      clickClassifyFlag4.value = false;
      clickClassifyFlag5.value = false;
      clickClassifyFlag6.value = false;
      clickClassifyFlag7.value = false;
      clickClassifyFlag8.value = false;
      clickClassifyFlag9.value = false;
      clickClassifyFlag10.value = false;
      clickClassifyFlag11.value = false;
      clickClassifyFlag12.value = false;
      clickClassifyFlag13.value = false;
      clickClassifyFlag14.value = false;
      clickClassifyFlag15.value = false;
      clickClassifyFlag16.value = false;
      clickClassifyFlag17.value = true;
      clickClassifyFlag18.value = false;
      clickClassifyFlag19.value = false;
      clickClassifyFlag20.value = false;
      clickClassifyFlag21.value = false;
      clickClassifyFlag22.value = false;
      acceptSearchData.classify = "资讯";
      videoPageNum.value = 1;
      searchByKeyWordVideo();
    }
    function ClickClassifyFlag18() {
      clickClassifyFlag1.value = false;
      clickClassifyFlag2.value = false;
      clickClassifyFlag3.value = false;
      clickClassifyFlag4.value = false;
      clickClassifyFlag5.value = false;
      clickClassifyFlag6.value = false;
      clickClassifyFlag7.value = false;
      clickClassifyFlag8.value = false;
      clickClassifyFlag9.value = false;
      clickClassifyFlag10.value = false;
      clickClassifyFlag11.value = false;
      clickClassifyFlag12.value = false;
      clickClassifyFlag13.value = false;
      clickClassifyFlag14.value = false;
      clickClassifyFlag15.value = false;
      clickClassifyFlag16.value = false;
      clickClassifyFlag17.value = false;
      clickClassifyFlag18.value = true;
      clickClassifyFlag19.value = false;
      clickClassifyFlag20.value = false;
      clickClassifyFlag21.value = false;
      clickClassifyFlag22.value = false;
      acceptSearchData.classify = "娱乐";
      videoPageNum.value = 1;
      searchByKeyWordVideo();
    }
    function ClickClassifyFlag19() {
      clickClassifyFlag1.value = false;
      clickClassifyFlag2.value = false;
      clickClassifyFlag3.value = false;
      clickClassifyFlag4.value = false;
      clickClassifyFlag5.value = false;
      clickClassifyFlag6.value = false;
      clickClassifyFlag7.value = false;
      clickClassifyFlag8.value = false;
      clickClassifyFlag9.value = false;
      clickClassifyFlag10.value = false;
      clickClassifyFlag11.value = false;
      clickClassifyFlag12.value = false;
      clickClassifyFlag13.value = false;
      clickClassifyFlag14.value = false;
      clickClassifyFlag15.value = false;
      clickClassifyFlag16.value = false;
      clickClassifyFlag17.value = false;
      clickClassifyFlag18.value = false;
      clickClassifyFlag19.value = true;
      clickClassifyFlag20.value = false;
      clickClassifyFlag21.value = false;
      clickClassifyFlag22.value = false;
      acceptSearchData.classify = "影视";
      videoPageNum.value = 1;
      searchByKeyWordVideo();
    }
    function ClickClassifyFlag20() {
      clickClassifyFlag1.value = false;
      clickClassifyFlag2.value = false;
      clickClassifyFlag3.value = false;
      clickClassifyFlag4.value = false;
      clickClassifyFlag5.value = false;
      clickClassifyFlag6.value = false;
      clickClassifyFlag7.value = false;
      clickClassifyFlag8.value = false;
      clickClassifyFlag9.value = false;
      clickClassifyFlag10.value = false;
      clickClassifyFlag11.value = false;
      clickClassifyFlag12.value = false;
      clickClassifyFlag13.value = false;
      clickClassifyFlag14.value = false;
      clickClassifyFlag15.value = false;
      clickClassifyFlag16.value = false;
      clickClassifyFlag17.value = false;
      clickClassifyFlag18.value = false;
      clickClassifyFlag19.value = false;
      clickClassifyFlag20.value = true;
      clickClassifyFlag21.value = false;
      clickClassifyFlag22.value = false;
      acceptSearchData.classify = "纪录片";
      videoPageNum.value = 1;
      searchByKeyWordVideo();
    }
    function ClickClassifyFlag21() {
      clickClassifyFlag1.value = false;
      clickClassifyFlag2.value = false;
      clickClassifyFlag3.value = false;
      clickClassifyFlag4.value = false;
      clickClassifyFlag5.value = false;
      clickClassifyFlag6.value = false;
      clickClassifyFlag7.value = false;
      clickClassifyFlag8.value = false;
      clickClassifyFlag9.value = false;
      clickClassifyFlag10.value = false;
      clickClassifyFlag11.value = false;
      clickClassifyFlag12.value = false;
      clickClassifyFlag13.value = false;
      clickClassifyFlag14.value = false;
      clickClassifyFlag15.value = false;
      clickClassifyFlag16.value = false;
      clickClassifyFlag17.value = false;
      clickClassifyFlag18.value = false;
      clickClassifyFlag19.value = false;
      clickClassifyFlag20.value = false;
      clickClassifyFlag21.value = true;
      clickClassifyFlag22.value = false;
      acceptSearchData.classify = "电影";
      videoPageNum.value = 1;
      searchByKeyWordVideo();
    }
    function ClickClassifyFlag22() {
      clickClassifyFlag1.value = false;
      clickClassifyFlag2.value = false;
      clickClassifyFlag3.value = false;
      clickClassifyFlag4.value = false;
      clickClassifyFlag5.value = false;
      clickClassifyFlag6.value = false;
      clickClassifyFlag7.value = false;
      clickClassifyFlag8.value = false;
      clickClassifyFlag9.value = false;
      clickClassifyFlag10.value = false;
      clickClassifyFlag11.value = false;
      clickClassifyFlag12.value = false;
      clickClassifyFlag13.value = false;
      clickClassifyFlag14.value = false;
      clickClassifyFlag15.value = false;
      clickClassifyFlag16.value = false;
      clickClassifyFlag17.value = false;
      clickClassifyFlag18.value = false;
      clickClassifyFlag19.value = false;
      clickClassifyFlag20.value = false;
      clickClassifyFlag21.value = false;
      clickClassifyFlag22.value = true;
      acceptSearchData.classify = "电视剧";
      videoPageNum.value = 1;
      searchByKeyWordVideo();
    }
    function highlightText(title) {
      const regex = new RegExp(`(${acceptSearchData.keyWord})`, "g");
      return title.replace(regex, '<span class="highlight">$1</span>');
    }

    //获取用户ip和token
    // token 由 /auth/login 返回后存在本地，这里不再向后端要 IP + token。
    async function getUserIp(){
      store.setUserIp("");
    }
    onMounted(async () => {
      window.scrollTo({ top: 0, behavior: "smooth" });
      windowWidth.value = window.screen.width;
      onloadPage.value = true;
      await getUserIp();
      await ChecklLogin();
      searchByKeyWordVideoOnce();
      selectUsersAxios();
      document.title =
        acceptSearchData.keyWord ||
        acceptSearchData.classify + "-青芒视频";
      window.addEventListener("scroll", handleScroll); // 监听滚动事件
    });
    onUnmounted(() => {
      document.removeEventListener("scroll", handleScroll); // 移除监听滚动事件
    });

    async function searchByKeyWordVideoOnce() {
      try {
        acceptSearchData.videoPageNum = 1;
        const urlParams = new URLSearchParams(window.location.search);
        const keyWord = urlParams.get("keyword");
        const classifyIndex = urlParams.get("classifyIndex");
        acceptSearchData.keyWord = keyWord;
        if (classifyIndex && classifyIndex.length > 0) {
          acceptSearchData.classify = classifyIndex;
          clickClassifyFlag1.value = false;
        }
        acceptSearchData.classifyIndex = classifyIndex;
        const response = await apiClient.post(
          "/search/searchVideoByKeyWord",
          acceptSearchData
        );
        if (response.data.code === 1) {
          Videos.length = 0;
          acceptSearchData.videoTotal = response.data.data.videoTotal;
          Object.assign(Videos, response.data.data.selectVideoDtoList);
        }
      } catch (error) {
        ElMessage({
          message: "未知错误",
          type: "info",
          plain: true,
          duration: 1700,
        });
      }
    }

    //根据关键字搜索视频
    async function searchByKeyWordVideo() {
      try {
        const urlParams = new URLSearchParams(window.location.search);
        const keyWord = urlParams.get("keyword");
        if(!keyWord&&clickClassifyFlag1.value)
        {
          return;
        }
        acceptSearchData.videoPageNum = videoPageNum.value;
        userPageNum.value = 1;
        acceptSearchData.keyWord = keyWord;
        const classifyIndex = urlParams.get("classifyIndex");
        acceptSearchData.classifyIndex = classifyIndex;
        const response = await apiClient.post(
          "/search/searchVideoByKeyWord",
          acceptSearchData
        );
        if (response.data.code === 1) {
          Videos.length = 0;
          acceptSearchData.videoTotal = response.data.data.videoTotal;
          Object.assign(Videos, response.data.data.selectVideoDtoList);
        }
      } catch (error) {
        ElMessage({
          message: "未知错误",
          type: "info",
          plain: true,
          duration: 1700,
        });
      }
    }

    //根据关键词搜索用户
    async function selectUsersAxios() {
      try {
        const urlParams = new URLSearchParams(window.location.search);
        const keyWord = urlParams.get("keyword");
        if(!keyWord)
        {
          return;
        }
        acceptSearchData.keyWord = keyWord;
        acceptSearchData.userPageNum = userPageNum.value;
        videoPageNum.value = 1;
        const response = await apiClient.post(
          "/search/selectUsers",
          acceptSearchData
        );
        if (response.data.code === 1) {
            searchUserList.length = 0;
            acceptSearchData.userTotal = response.data.data.userTotal;
            Object.assign(searchUserList, response.data.data.selectUserDtoList);
          }
      } catch (error) {
        ElMessage({
          message: "未知错误",
          type: "info",
          plain: true,
          duration: 1700,
        });
      }
    }

    // 更改视频当前页
    function handleCurrentChangeVideo(val) {
      videoPageNum.value = val;
    }

    // 更改视频当前页
    function handleCurrentChangeVideo2(event) {
      if (
        event.target.value !== "" &&
        event.target.value <= Math.ceil(acceptSearchData.videoTotal / 20) &&
        event.target.value >= 1
      )
        videoPageNum.value = parseInt(event.target.value);
    }

    // 更改用户当前页
    function handleCurrentChangeUser(val) {
      userPageNum.value = val;
    }

    // 更改用户当前页
    function handleCurrentChangeUser2(event) {
      if (
        event.target.value !== "" &&
        event.target.value <= Math.ceil(acceptSearchData.userTotal / 20) &&
        event.target.value >= 1
      )
        userPageNum.value = parseInt(event.target.value);
    }

    //监视视频页数变化
    watch(videoPageNum, () => {
      searchByKeyWordVideo();
    });

    //监视用户页数变化
    watch(userPageNum, () => {
      selectUsersAxios();
    });

    //关注
    async function addFollowAxios(userId) {
      try {
        if (store.userId === null) {
          loginDialogVisibleFlag.value =
            loginDialogVisibleFlag.value === 0 ? 1 : 0;
          return;
        }

        if (store.userId === userId) {
          ElMessage({
            message: "不能关注自己哦",
            type: "info",
            plain: true,
            duration: 1700,
          });
          return;
        }

        const response = await apiClient.post(
          "/video/addFollow",
          {
            followId: userId,
            fansId: store.userId,
          },
          {
            headers: {
              "Content-Type": "application/json",
              Authorization: store.token,
            },
          }
        );
        if (response.data.code === 1) selectUsersAxios();
        else {
          ElMessage({
            message: response.data.msg,
            type: "info",
            plain: true,
            duration: 1700,
          });
        }
      } catch (error) {}
    }

    //取消关注
    async function deleteFollowAxios(userId) {
      try {
        if (store.userId === null) {
          loginDialogVisibleFlag.value =
            loginDialogVisibleFlag.value === 0 ? 1 : 0;
          return;
        }

        const response = await apiClient.post(
          "/video/deleteFollow",
          {
            followId: userId,
            fansId: acceptSearchData.userId,
          },
          {
            headers: {
              "Content-Type": "application/json",
              Authorization: store.token,
            },
          }
        );
        if (response.data.code === 1) selectUsersAxios();
        else {
          ElMessage({
            message: response.data.msg,
            type: "info",
            plain: true,
            duration: 1700,
          });
        }
      } catch (error) {}
    }

    //检查登录
    async function ChecklLogin() {
      try {
        const response = await apiClient.get(
          `/user/checkLoginFlag/${store.userIp}`
        );
        if (response.data.code === 1) {
          acceptSearchData.userId = response.data.data.id;
          store.setUserId(response.data.data.id);
        } else {
          acceptSearchData.userId = 0;
          store.setUserId(null);
        }
      } catch (error) {
        acceptSearchData.userId = 0;
        store.setUserId(null);
      }
    }

    //添加到稍后观看
    async function waitWatch(videoId) {
      try {
        if (store.userId === null) {
          loginDialogVisibleFlag.value =
            loginDialogVisibleFlag.value === 0 ? 1 : 0;
          return;
        }

        let dynamicDto = {
          videoId: videoId,
          userId: store.userId,
        };
        const response = await apiClient.put(
          "/dynamic/updateWaitWatch",
          dynamicDto,
          {
            headers: {
              "Content-Type": "application/json",
              Authorization: store.token,
            },
          }
        );
        if (response.data.code === 1) {
          const updatedVideos = Videos.map((item) => {
            if (item.videoId === videoId && item.waitWatch === 0) {
              return { ...item, waitWatch: 1 }; // 创建一个新的对象并修改 waitWatch
            } else if (item.videoId === videoId && item.waitWatch === 1) {
              return { ...item, waitWatch: 0 };
            }
            return item; // 保留原始对象
          });
          Videos.length = 0; // 清空原数组
          Object.assign(Videos, updatedVideos); // 重新赋值
        } else {
          ElMessage({
            message: response.data.msg,
            type: "info",
            plain: true,
            duration: 1700,
          });
        }
      } catch (error) {}
    }

    //监视搜索条件变化
    watch(
      acceptSearchData,
      (newValue) => {
        if (newValue.classify === "动画") clickClassifyFlag2.value = true;
        else if (newValue.classify === "番剧") clickClassifyFlag3.value = true;
        else if (newValue.classify === "国创") clickClassifyFlag4.value = true;
        else if (newValue.classify === "音乐") clickClassifyFlag5.value = true;
        else if (newValue.classify === "舞蹈") clickClassifyFlag6.value = true;
        else if (newValue.classify === "游戏") clickClassifyFlag7.value = true;
        else if (newValue.classify === "知识") clickClassifyFlag8.value = true;
        else if (newValue.classify === "科技") clickClassifyFlag9.value = true;
        else if (newValue.classify === "运动") clickClassifyFlag10.value = true;
        else if (newValue.classify === "汽车") clickClassifyFlag11.value = true;
        else if (newValue.classify === "生活") clickClassifyFlag12.value = true;
        else if (newValue.classify === "美食") clickClassifyFlag13.value = true;
        else if (newValue.classify === "动物圈")
          clickClassifyFlag14.value = true;
        else if (newValue.classify === "鬼畜") clickClassifyFlag15.value = true;
        else if (newValue.classify === "时尚") clickClassifyFlag16.value = true;
        else if (newValue.classify === "资讯") clickClassifyFlag17.value = true;
        else if (newValue.classify === "娱乐") clickClassifyFlag18.value = true;
        else if (newValue.classify === "影视") clickClassifyFlag19.value = true;
        else if (newValue.classify === "纪录片")
          clickClassifyFlag20.value = true;
        else if (newValue.classify === "电影") clickClassifyFlag21.value = true;
        else if (newValue.classify === "电视剧")
          clickClassifyFlag22.value = true;
      },
      { deep: true }
    );

    //跳转到视频详情页
    function locationHerfVideo(videoId) {
      window.open(`./video?videoId=BV${videoId}`, "videoWindow");
    }

    function videoMouseover(id) {
      // 清除之前的定时器，防止重复触发
      if (videoAutoPlayTIme[id]) {
        clearTimeout(videoAutoPlayTIme[id]);
      }

      // 延迟播放视频
      videoAutoPlayTIme[id] = setTimeout(() => {
        const video = document.getElementById(id);
        if (video)
          if (video.paused) {
            // 只有在视频处于暂停状态时才播放
            video.play().catch(function (error) {});
          }
      }, 700); // 1秒后播放视频
    }

    function videoMouseleave(id) {
      // 清除之前的视频播放定时器
      clearTimeout(videoAutoPlayTIme[id]);

      const video = document.getElementById(id);
      if (video)
        if (!video.paused) {
          // 只有在视频播放时才暂停
          video.pause();
        }
    }

    function openHome(userId) {
      window.open(`./home?userId=${userId}&homeMenu=1`, "_blank");
    }

    return {
      onloadPage,
      isVisible,
      clickFlag,
      clickFlag1,
      clickFlag2,
      clickFlag3,
      clickFlag4,
      clickFlag5,
      clickFlag6,
      clickFlag7,
      ClickFlag,
      resultTabs,
      sortLine,
      setTabEl,
      clickSortFlag1,
      clickSortFlag2,
      clickSortFlag3,
      clickSortFlag4,
      clickSortFlag5,
      ClickSortFlag1,
      ClickSortFlag2,
      ClickSortFlag3,
      ClickSortFlag4,
      ClickSortFlag5,
      clickUserFlag1,
      clickUserFlag2,
      clickUserFlag3,
      clickUserFlag4,
      clickUserFlag5,
      ClickUserFlag1,
      ClickUserFlag2,
      ClickUserFlag3,
      ClickUserFlag4,
      ClickUserFlag5,
      clickDateFlag1,
      clickDateFlag2,
      clickDateFlag3,
      clickDateFlag4,
      clickDateFlag5,
      ClickDateFlag1,
      ClickDateFlag2,
      ClickDateFlag3,
      ClickDateFlag4,
      ClickDateFlag5,
      datea,
      clickTimeFlag1,
      clickTimeFlag2,
      clickTimeFlag3,
      clickTimeFlag4,
      clickTimeFlag5,
      ClickTimeFlag1,
      ClickTimeFlag2,
      ClickTimeFlag3,
      ClickTimeFlag4,
      ClickTimeFlag5,
      clickClassifyFlag1,
      clickClassifyFlag2,
      clickClassifyFlag3,
      clickClassifyFlag4,
      clickClassifyFlag5,
      clickClassifyFlag6,
      clickClassifyFlag7,
      clickClassifyFlag8,
      clickClassifyFlag9,
      clickClassifyFlag10,
      clickClassifyFlag11,
      clickClassifyFlag12,
      clickClassifyFlag13,
      clickClassifyFlag14,
      clickClassifyFlag15,
      clickClassifyFlag16,
      clickClassifyFlag17,
      clickClassifyFlag18,
      clickClassifyFlag19,
      clickClassifyFlag20,
      clickClassifyFlag21,
      clickClassifyFlag22,
      ClickClassifyFlag1,
      ClickClassifyFlag2,
      ClickClassifyFlag3,
      ClickClassifyFlag4,
      ClickClassifyFlag5,
      ClickClassifyFlag6,
      ClickClassifyFlag7,
      ClickClassifyFlag8,
      ClickClassifyFlag9,
      ClickClassifyFlag10,
      ClickClassifyFlag11,
      ClickClassifyFlag12,
      ClickClassifyFlag13,
      ClickClassifyFlag14,
      ClickClassifyFlag15,
      ClickClassifyFlag16,
      ClickClassifyFlag17,
      ClickClassifyFlag18,
      ClickClassifyFlag19,
      ClickClassifyFlag20,
      ClickClassifyFlag21,
      ClickClassifyFlag22,
      expanded,
      waitFont,
      upImgFlag,
      up,
      upBlue,
      Videos,
      acceptSearchData,
      highlightText,
      selectUsersAxios,
      searchUserList,
      userContent,
      addFollowAxios,
      deleteFollowAxios,
      waitWatch,
      locationHerfVideo,
      videoMouseover,
      videoMouseleave,
      store,
      loginDialogVisibleFlag,
      openHome,
      handleCurrentChangeUser,
      handleCurrentChangeUser2,
      handleCurrentChangeVideo,
      handleCurrentChangeVideo2,
      windowWidth,
      videoPageNum,
      userPageNum,
      handleUrlChange,
      searcha,
      searcha2
    };
  },
};
</script>

<style lang="scss">
* {
  padding: 0; /* 移除内边距 */
  margin: 0; /* 移除外边距 */
  box-sizing: border-box; /* 包括内边距和边框在元素的总宽度和高度中 */
}

/* 原来写死 width:1425px 靠 left:50% + translateX(-50%) 居中，
   视口窄于 1425px 就必然出现横向滚动条。改成 max-width 由页面容器控宽。 */
.SearchBox {
  user-select: none;
  height: auto;
  position: relative;
  width: 100%;
  max-width: var(--page-max);
  margin: 0 auto;
}


.hiddenBox {
  width: 100%;
  height: 65px;
  box-shadow: 0 0px 4px rgba(0, 0, 0, 0.3); /* 添加底部阴影 */
  position: fixed;
  transform: translate(0px, -60px);
  visibility: hidden;
  opacity: 0;
  background-color: white;
  z-index: -1000;
}

.showHiddenBox {
  opacity: 1;
  transition: all 0.3s ease;
  visibility: visible;
  transform: translateY(0px);
  z-index: 100000;
}

.head {
  position: relative;
  width: 100%;
  box-sizing: border-box;
  height: 65px;
  z-index: 100000;
  box-shadow: 0 0px 3px rgba(0, 0, 0, 0.3); /* 添加底部阴影 */
}

.headLoginIndex {
  z-index: 100;
}

.middle {
  width: 100%;
  height: 160px;
  transform: translateY(-65px);
  border-bottom: 1px solid #e5e5e5;
  z-index: 0;
}

/* 结果分类页签。
   原来 .sort 是 width:50% + translate(15px,122.5px)，下划线另有 7 个写死坐标；
   现在页签按内容宽度排布，选中态走 .is-on，下划线用 .sort-underline 由脚本定位。 */
.sort {
  position: relative;
  display: flex;
  align-items: center;
  gap: 34px;
  padding-bottom: 10px;
  font-size: 15.5px;
}

.content {
  transform: translateY(-65px);
}

.sort .ac {
  display: inline-block;
  min-width: 20px;
  height: 16px;
  margin-left: 5px;
  padding: 0 5px;
  font-size: 12px;
  line-height: 16px;
  text-align: center;
  background-color: #eaeaea;
  border-radius: 5px;
  color: #919090;
  vertical-align: middle;
}
.sort .aw {
  position: relative;
  cursor: pointer;
  color: var(--ink-2);
  white-space: nowrap;
  transition: color .2s ease;
}
.sort .aw.is-on {
  color: var(--brand);
  font-weight: 600;
}
.sort .aw:hover {
  color: var(--brand);
}

.sort-underline {
  position: absolute;
  bottom: 0;
  left: 0;
  height: 3px;
  border-radius: 3px;
  background-color: var(--brand);
  transition: transform .25s ease, width .25s ease;
}

.videoSort .condition {
  font-size: 14px;
  position: absolute;
  cursor: pointer;
  color: #5b5b5b;
  transition: all 0.3s ease;
}

.videoSort .condition:hover {
  color: #0FA68E;
}

.videoSort .conditionClick {
  text-align: center;
  line-height: 30px;
  font-size: 14px;
  position: absolute;
  cursor: pointer;
  color: #0FA68E;
  border-radius: 7px;
  width: 100px;
  height: 32px;
  background-color: #E2F4EF;
}

.userSort .condition {
  z-index: 10;
  font-size: 14px;
  position: absolute;
  cursor: pointer;
  color: #5b5b5b;
  transition: all 0.3s ease;
}

.userSort .condition:hover {
  color: #0FA68E;
}

.userSort .conditionClick {
  text-align: center;
  line-height: 30px;
  font-size: 14px;
  position: absolute;
  cursor: pointer;
  color: #0FA68E;
  border-radius: 7px;
  width: auto;
  height: 32px;
  padding-right: 15px;
  padding-left: 15px;
  background-color: #E2F4EF;
}

.screen {
  display: flex;
  justify-content: center;
  align-items: center;
  position: absolute;
  transform: translate(1260.5px, 31.5px);
  font-size: 14px;
  border-radius: 5px;
  width: 100px;
  height: 33px;
  transition: all 0.3s ease;
  border: 1px solid #d8d8d8;
  cursor: pointer;
}
.screen:hover {
  background-color: #d8d8d8;
}
.screen span {
  margin-right: 3px;
}
.screen img {
  width: 11px;
  margin-left: 3px;
  margin-top: 2px;
  transform: rotate(180deg);
}
.custom-tooltip2 {
  display: flex;
  justify-content: space-around;
  cursor: pointer; /* 鼠标样式 */
}
.custom-tooltip2 span {
  margin-left: 5px;
  margin-right: 5px;
}
.custom-tooltip2 span:hover {
  color: #0FA68E;
}
.showBox {
  width: auto;
  visibility: hidden;
  opacity: 0;
}

.showBox.expanded {
  visibility: visible;
  opacity: 1;
  height: auto;
}

.bottomVideo {
  top: 306px;
  left: 0;
  right: 0;
  width: auto;
  display: flex;
  flex-wrap: wrap; /* 允许换行 */
  z-index: 0;
  position: absolute;
}
.video-video {
  flex: 0 0 20%; /* 每个盒子占据 30% 的宽度 */
  width: 247px;
  height: 138px;
  margin-top: 295px;
  margin-bottom: -180px; /* 设置每行之间的间距 */
  /* 父级原来 translate(-11px,306px)、这里 translate(75px,-507px)，合起来是 (64,-201)。
     父级改成 left/right 定位后不再带 -11px，这里补成 64px 保持渲染位置不变。 */
  transform: translate(64px, -507px);
}

video {
  width: 247px; /* 视频宽度为100% */
  height: 100%; /* 视频高度为100% */
  object-fit: cover; /* 填充并保持比例 */
  position: absolute; /* 绝对定位 */
  top: 0; /* 上部对齐 */
  left: 0; /* 左部对齐 */
  z-index: 2000;
  border-radius: 5px;
  cursor: pointer;
  visibility: hidden; /* 隐藏 */
  user-select: none;
}

.video-video:hover .waitWatch {
  visibility: visible;
}
.video-video img {
  width: 247px;
  height: 138px;
  object-fit: cover;
  border-radius: 6px;
}

.video-video:hover .waitWatch {
  visibility: visible;
}
.videoContent {
  position: absolute; /* 确保位置是绝对的 */
  width: 246.5px;
  height: 30px;
  background: linear-gradient(to top, rgba(0, 0, 0, 0.9), rgba(0, 0, 0, 0.2));
  top: 78%; /* 改为100%以使其在图片下方显示 */
  border-radius: 0px 0px 6px 6px;
  opacity: 1;
  transition: all 0.3s ease;
  color: white;
  visibility: visible;
  z-index: 2990;
}
.videoContent1 {
  width: 245px;
  height: 30px;
  position: relative;
  transform: translate(0px, -35px);
  color: white;
  z-index: 3000;
}

.videoBox1:hover {
  video {
    opacity: 1;
    transition: all 0.3s ease;
    visibility: visible;
    transition-delay: 0.6s;
  }

  .videoContent1 {
    opacity: 0;
    transition: all 0.3s ease;
    visibility: hidden;
  }
  .videoContent {
    opacity: 0;
    transition: all 0.3s ease;
    visibility: hidden;
  }

  .waitWatch {
    transition-delay: 0.3s;
    opacity: 1;
    transition: all 0.3s ease;
    visibility: visible;
  }
}

.colon {
  display: inline-block;
  position: relative;
  transform: translateY(-1px);
}

.waitWatch {
  display: flex;
  justify-content: left;
  align-items: center;
  border-radius: 5px;
  width: 28px;
  height: 28px;
  right: 28px;
  top: 8px;
  background-color: rgba(33, 33, 33, 0.8);
  opacity: 0;
  visibility: hidden;
  transition: transform 0.3s ease, width 0.3s ease; /* 添加宽度变化的动画效果 */
  cursor: pointer;
  position: absolute;
  z-index: 3000;
  overflow: hidden;
}

.waitWatch:hover {
  transition-delay: 0.3s;
  width: 120px; /* 扩大宽度 */
  visibility: visible; /* 悬停时可见 */
}

.upInfo {
  width: 224.5px;
  color: #8F9794;
  font-size: 12.5px;
  transform: translate(19.5px, 23px);
  position: absolute;
  display: -webkit-box; /* 使用 flexbox 布局 */
  -webkit-box-orient: vertical; /* 垂直方向排列 */
  -webkit-line-clamp: 1; /* 限制为 1行 */
  overflow: hidden; /* 隐藏超出部分 */
  transition: all 0.2s ease;
  text-overflow: ellipsis; /* 显示省略号 */
}

.videoBottomInfo:hover .upInfo {
  color: #0FA68E;
}
.videoBottomInfo {
  cursor: pointer;
  position: absolute;
  transform: translate(0px, 0.5px);
}

.changer {
  width: 40px;
  height: 83.5px;
  border-radius: 10px;
  position: absolute;
  transform: translate(1390px, -305px);
  border: 1px solid #E0E5E3;
  cursor: pointer;
}
.changer img {
  width: 13px;
  height: 13px;
  transform: translate(12px, 10px);
  transition: all 0.3s ease;
  position: absolute;
}
.changer:hover {
  background-color: #E0E5E3;
}

.changer span {
  font-size: 11px;
  writing-mode: vertical-rl; /* 垂直从右到左 */
  /* 或者使用: writing-mode: vertical-lr; 从左到右 */
  transform: translate(12px, 32px);
}

.title {
  width: 247px; /* 设置宽度 */
  word-wrap: break-word; /* 超出部分换行 */
  display: -webkit-box; /* 使用 flexbox 布局 */
  -webkit-box-orient: vertical; /* 垂直方向排列 */
  -webkit-line-clamp: 2; /* 限制为 2 行 */
  overflow: hidden; /* 隐藏超出部分 */
  text-overflow: ellipsis; /* 显示省略号 */
  line-height: 1.5; /* 设置行高 */
  color: black;
  font-weight: 500;
  font-size: 15px;
  transform: translate(0px, -25px);
  position: absolute; /* 或者 absolute，视布局而定 */
  cursor: pointer;
  transition: all 0.3s ease;
  font-weight: normal; /* 正常 */
  padding-bottom: 3px;
}

.title:hover {
  color: #0FA68E;
}
.highlight {
  color: #E06B33 !important;
}

.searchUsers {
  display: flex;
  overflow-y: hidden;
  overflow-x: hidden;
  flex-wrap: wrap; /* 允许换行 */
  width: 91.15%;
  position: absolute;
  transform: translate(-10px, -10px);
}

.usersContent {
  transform: translate(64.5px, 104.5px);
  flex: 0 0 calc(40% - 20px); /* 每个盒子占据 50% 的宽度，减去边距 */
  margin: 10px; /* 添加间距 */
  box-sizing: border-box; /* 包含内边距和边框 */
  width: 200px;
  margin-bottom: 30px;
}
.follow {
  width: 100.5px;
  height: 32px;
  background-color: #0FA68E;
  color: white;
  display: flex;
  justify-content: center;
  align-items: center;
  border-radius: 8px;
  font-size: 13.5px;
  transform: translate(100px, -55px);
  cursor: pointer;
  transition: all 0.3s ease;
  position: absolute;
}

.follow:hover {
  opacity: 0.8;
}

.deleteFollow {
  width: 100.5px;
  height: 32px;
  background-color: #EFF3F2;
  color: #5D6764;
  display: flex;
  justify-content: center;
  align-items: center;
  border-radius: 8px;
  font-size: 13.5px;
  transform: translate(100px, -55px);
  cursor: pointer;
  position: absolute;
}

.deleteFollow:hover {
  background-color: #E0E5E3;
}

.user-Name {
  font-weight: 800;
  font-size: 17.5px;
  transform: translate(101px, -88px);
  display: flex;
  transition: all 0.3s ease;
}

.user-Name:hover {
  color: #0FA68E;
}

.custom-tooltip1 {
  border: 1px solid black !important; /* 修改边框颜色和宽度 */
  height: 24px;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 5px;
  border-radius: 0px !important;
}

.userInfo-Content {
  display: flex; /* 使用 Flexbox 布局 */
  align-items: center; /* 垂直居中对齐 */
  width: 510px;
  font-size: 13px;
  color: #626b92;
  overflow: hidden; /* 隐藏超出部分 */
  white-space: nowrap; /* 不换行 */
  text-overflow: ellipsis; /* 显示省略号 */
  transform: translate(100px, -60px);
}

.introduce {
  margin-left: 8px;
  overflow: hidden; /* 确保超出部分隐藏 */
  white-space: nowrap; /* 不换行 */
  text-overflow: ellipsis; /* 显示省略号 */
}

.coverAddress {
  user-select: none;
  cursor: pointer;
}

.page-container {
  position: relative;
  margin-top: 60px;
  display: flex;
  justify-content: center;
  align-items: center;

  span {
    color: #1C2321;
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
      border: 1px solid #E0E5E3;
      font-size: 14px;
      border-radius: 6px;
      transition: all 0.3s ease;
      padding: 0 10px;
    }
    input:hover {
      border-color: #0FA68E;
    }
    input:focus {
      border-color: #0FA68E;
    }
    input::-webkit-inner-spin-button,
    input::-webkit-outer-spin-button {
      -webkit-appearance: none;
    }
  }
}

@media (max-width: 1400px) {
  .bottomVideo{
    width: 1050px;
  }
  .video-video {
    flex: 0 0 25% ;
  }
}

@media (max-width: 1130px) {
  .bottomVideo{
    width: 787.5px;
  }
  .video-video {
    flex: 0 0 33.3333333333% ;
  }
}

@media (max-width: 880px) {
  .bottomVideo{
    width: 525px;
  }
  .video-video {
    flex: 0 0 50% ;
  }
}

@media (max-width: 600px) {
  .bottomVideo{
    width: 263px;
  }
  .video-video {
    flex: 0 0 100% ;
  }
}

@media (max-width: 950px) {
  .usersContent {
    flex: 0 0 calc(80% - 20px);
  }
}

</style>
