<template>
  <div class="classify-style" v-external-link>
    <div
      class="classify1"
      style="
        display: flex;
        justify-content: center;
        width: 100px;
        transform: translate(-148px, 5.5px);
      "
    >
      <span
        class="dynamic"
        @click="openDynamic"
        style="display: flex; flex-direction: column; align-items: center;cursor: pointer;"
      >
        <div class="dynamic1"><img src="../img/动态.png" /></div>
        <span>动态</span>
      </span>
      <span
        class="fire"
        style="
          display: flex;
          flex-direction: column;
          align-items: center;
          transform: translate(25px, -1px);
          cursor: pointer;
        "
      >
        <div class="fire1"><img src="../img/热门.png" /></div>
        <span>热门</span>
      </span>
    </div>
    <div
      class="classify2"
      style="
        display: flex;
        justify-content: center;
        align-items: center;
        flex-direction: column;
        width: 700px;
        position: absolute;
        transform: translate(-74px, 22px);
      "
    >
      <div style="margin-bottom: 5px">
        <a href="./search?keyword=&classifyIndex=番剧" target="_blank">
          <el-dropdown
            placement="bottom-end"
            max-height="100px"
            :show-timeout="300"
          >
            <el-button> 番 剧 </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <a
                  target="_blank"
                  href="./search?keyword=&classifyIndex=番剧"
                >
                  <el-dropdown-item>连载动画</el-dropdown-item></a
                >
                <a
                  target="_blank"
                  href="./search?keyword=&classifyIndex=番剧"
                  ><el-dropdown-item>完结动画</el-dropdown-item></a
                >
                <a
                  target="_blank"
                  href="./search?keyword=&classifyIndex=番剧"
                  ><el-dropdown-item>资讯</el-dropdown-item></a
                >
                <a
                  target="_blank"
                  href="./search?keyword=&classifyIndex=番剧"
                  ><el-dropdown-item>官方延伸</el-dropdown-item></a
                >
                <a
                  target="_blank"
                  href="./search?keyword=&classifyIndex=番剧"
                  ><el-dropdown-item>新番时间表</el-dropdown-item></a
                >
                <a
                  target="_blank"
                  href="./search?keyword=&classifyIndex=番剧"
                  ><el-dropdown-item>番剧索引</el-dropdown-item></a
                >
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </a>
        <a href="./search?keyword=&classifyIndex=国创" target="_blank">
          <el-dropdown
            placement="bottom-end"
            max-height="100px"
            :show-timeout="300"
          >
            <el-button> 国 创 </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <a
                  target="_blank"
                  href="./search?keyword=&classifyIndex=国创"
                >
                  <el-dropdown-item>国产动画 </el-dropdown-item></a
                >
                <a
                  target="_blank"
                  href="./search?keyword=&classifyIndex=国创"
                  ><el-dropdown-item>国产原创相关 </el-dropdown-item></a
                >
                <a
                  target="_blank"
                  href="./search?keyword=&classifyIndex=国创"
                  ><el-dropdown-item>布袋戏 </el-dropdown-item></a
                >
                <a
                  target="_blank"
                  href="./search?keyword=&classifyIndex=国创"
                  ><el-dropdown-item>动态漫·广播剧 </el-dropdown-item></a
                >
                <a
                  target="_blank"
                  href="./search?keyword=&classifyIndex=国创"
                  ><el-dropdown-item>资讯 </el-dropdown-item></a
                >
                <a
                  target="_blank"
                  href="./search?keyword=&classifyIndex=国创"
                  ><el-dropdown-item>新番时间表 </el-dropdown-item></a
                >
                <a
                  target="_blank"
                  href="./search?keyword=&classifyIndex=国创"
                  ><el-dropdown-item>国产动画索引</el-dropdown-item></a
                >
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </a>
        <a href="./search?keyword=&classifyIndex=综艺" target="_blank">
          <el-button> 综 艺 </el-button>
        </a>
        <a href="./search?keyword=&classifyIndex=动画" target="_blank">
          <el-dropdown
            placement="bottom-end"
            max-height="100px"
            :show-timeout="300"
          >
            <el-button> 动 画 </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <a
                  target="_blank"
                  href="./search?keyword=&classifyIndex=动画"
                >
                  <el-dropdown-item>MAD·AMV </el-dropdown-item></a
                >
                <a
                  target="_blank"
                  href="./search?keyword=&classifyIndex=动画"
                  ><el-dropdown-item>MMD·3D </el-dropdown-item></a
                >
                <a
                  target="_blank"
                  href="./search?keyword=&classifyIndex=动画"
                  ><el-dropdown-item>同人·手书 </el-dropdown-item></a
                >
                <a
                  target="_blank"
                  href="./search?keyword=&classifyIndex=动画"
                  ><el-dropdown-item>配音 </el-dropdown-item></a
                >
                <a
                  target="_blank"
                  href="./search?keyword=&classifyIndex=动画"
                  ><el-dropdown-item>模玩·周边 </el-dropdown-item></a
                >
                <a
                  target="_blank"
                  href="./search?keyword=&classifyIndex=动画"
                  ><el-dropdown-item>特摄 </el-dropdown-item></a
                >
                <a
                  target="_blank"
                  href="./search?keyword=&classifyIndex=动画"
                  ><el-dropdown-item>动画杂谈</el-dropdown-item></a
                >
                <a
                  target="_blank"
                  href="./search?keyword=&classifyIndex=动画"
                  ><el-dropdown-item>综合</el-dropdown-item></a
                >
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </a>
        <a href="./search?keyword=&classifyIndex=鬼畜" target="_blank">
          <el-dropdown
            placement="bottom-end"
            max-height="100px"
            :show-timeout="300"
          >
            <el-button> 鬼 畜 </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <a
                  target="_blank"
                  href="./search?keyword=&classifyIndex=鬼畜"
                >
                  <el-dropdown-item>鬼畜调教 </el-dropdown-item></a
                >
                <a
                  target="_blank"
                  href="./search?keyword=&classifyIndex=鬼畜"
                  ><el-dropdown-item>音MAD </el-dropdown-item></a
                >
                <a
                  target="_blank"
                  href="./search?keyword=&classifyIndex=鬼畜"
                  ><el-dropdown-item>人力VOCALOID </el-dropdown-item></a
                >
                <a
                  target="_blank"
                  href="./search?keyword=&classifyIndex=鬼畜"
                  ><el-dropdown-item>鬼畜剧场 </el-dropdown-item></a
                >
                <a
                  target="_blank"
                  href="./search?keyword=&classifyIndex=鬼畜"
                  ><el-dropdown-item>教程演示 </el-dropdown-item></a
                >
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </a>
        <a href="./search?keyword=&classifyIndex=舞蹈" target="_blank">
          <el-dropdown
            placement="bottom-end"
            max-height="100px"
            :show-timeout="300"
          >
            <el-button> 舞 蹈 </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <a
                  target="_blank"
                  href="./search?keyword=&classifyIndex=舞蹈"
                >
                  <el-dropdown-item>宅舞 </el-dropdown-item></a
                >
                <a
                  target="_blank"
                  href="./search?keyword=&classifyIndex=舞蹈"
                  ><el-dropdown-item>街舞 </el-dropdown-item></a
                >
                <a
                  target="_blank"
                  href="./search?keyword=&classifyIndex=舞蹈"
                  ><el-dropdown-item>明星舞蹈 </el-dropdown-item></a
                >
                <a
                  target="_blank"
                  href="./search?keyword=&classifyIndex=舞蹈"
                  ><el-dropdown-item>国风舞蹈 </el-dropdown-item></a
                >
                <a
                  target="_blank"
                  href="./search?keyword=&classifyIndex=舞蹈"
                  ><el-dropdown-item>颜值·网红舞 </el-dropdown-item></a
                >
                <a
                  target="_blank"
                  href="./search?keyword=&classifyIndex=舞蹈"
                  ><el-dropdown-item>舞蹈综合 </el-dropdown-item></a
                >
                <a
                  target="_blank"
                  href="./search?keyword=&classifyIndex=舞蹈"
                  ><el-dropdown-item>舞蹈教程 </el-dropdown-item></a
                >
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </a>
        <a href="./search?keyword=&classifyIndex=娱乐" target="_blank">
          <el-dropdown
            placement="bottom-end"
            max-height="100px"
            :show-timeout="300"
          >
            <el-button> 娱 乐 </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <a
                  target="_blank"
                  href="./search?keyword=&classifyIndex=娱乐"
                >
                  <el-dropdown-item>娱乐杂谈 </el-dropdown-item></a
                >
                <a
                  target="_blank"
                  href="./search?keyword=&classifyIndex=娱乐"
                  ><el-dropdown-item>CP安利 </el-dropdown-item></a
                >
                <a
                  target="_blank"
                  href="./search?keyword=&classifyIndex=娱乐"
                  ><el-dropdown-item>颜值安利 </el-dropdown-item></a
                >
                <a
                  target="_blank"
                  href="./search?keyword=&classifyIndex=娱乐"
                  ><el-dropdown-item>娱乐粉丝创作 </el-dropdown-item></a
                >
                <a
                  target="_blank"
                  href="./search?keyword=&classifyIndex=娱乐"
                  ><el-dropdown-item>娱乐资讯 </el-dropdown-item></a
                >
                <a
                  target="_blank"
                  href="./search?keyword=&classifyIndex=娱乐"
                  ><el-dropdown-item>明星综合 </el-dropdown-item></a
                >
                <a
                  target="_blank"
                  href="./search?keyword=&classifyIndex=娱乐"
                  ><el-dropdown-item>综艺 </el-dropdown-item></a
                >
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </a>
        <a href="./search?keyword=&classifyIndex=科技" target="_blank">
          <el-dropdown
            placement="bottom-end"
            max-height="100px"
            :show-timeout="300"
          >
            <el-button> 科 技 </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <a
                  target="_blank"
                  href="./search?keyword=&classifyIndex=科技"
                >
                  <el-dropdown-item>数码 </el-dropdown-item></a
                >
                <a
                  target="_blank"
                  href="./search?keyword=&classifyIndex=科技"
                  ><el-dropdown-item>软件应用 </el-dropdown-item></a
                >
                <a
                  target="_blank"
                  href="./search?keyword=&classifyIndex=科技"
                  ><el-dropdown-item>计算机技术 </el-dropdown-item></a
                >
                <a
                  target="_blank"
                  href="./search?keyword=&classifyIndex=科技"
                  ><el-dropdown-item>科工机械 </el-dropdown-item></a
                >
                <a
                  target="_blank"
                  href="./search?keyword=&classifyIndex=科技"
                  ><el-dropdown-item>极客DIY </el-dropdown-item></a
                >
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </a>
        <a href="./search?keyword=&classifyIndex=美食" target="_blank">
          <el-dropdown
            placement="bottom-end"
            max-height="100px"
            :show-timeout="300"
          >
            <el-button> 美 食 </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <a
                  target="_blank"
                  href="./search?keyword=&classifyIndex=美食"
                >
                  <el-dropdown-item>美食制作 </el-dropdown-item></a
                >
                <a
                  target="_blank"
                  href="./search?keyword=&classifyIndex=美食"
                  ><el-dropdown-item>美食侦探 </el-dropdown-item></a
                >
                <a
                  target="_blank"
                  href="./search?keyword=&classifyIndex=美食"
                  ><el-dropdown-item>美食测评 </el-dropdown-item></a
                >
                <a
                  target="_blank"
                  href="./search?keyword=&classifyIndex=美食"
                  ><el-dropdown-item>田园美食 </el-dropdown-item></a
                >
                <a
                  target="_blank"
                  href="./search?keyword=&classifyIndex=美食"
                  ><el-dropdown-item>美食记录 </el-dropdown-item></a
                >
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </a>
        <a href="./search?keyword=&classifyIndex=汽车" target="_blank">
          <el-dropdown
            placement="bottom-end"
            max-height="100px"
            :show-timeout="300"
          >
            <el-button> 汽 车 </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <a
                  target="_blank"
                  href="./search?keyword=&classifyIndex=汽车"
                >
                  <el-dropdown-item>汽车知识科普 </el-dropdown-item></a
                >
                <a
                  target="_blank"
                  href="./search?keyword=&classifyIndex=汽车"
                  ><el-dropdown-item>购车攻略 </el-dropdown-item></a
                >
                <a
                  target="_blank"
                  href="./search?keyword=&classifyIndex=汽车"
                  ><el-dropdown-item>新能源汽车 </el-dropdown-item></a
                >
                <a
                  target="_blank"
                  href="./search?keyword=&classifyIndex=汽车"
                  ><el-dropdown-item>赛车 </el-dropdown-item></a
                >
                <a
                  target="_blank"
                  href="./search?keyword=&classifyIndex=汽车"
                  ><el-dropdown-item>改装玩车 </el-dropdown-item></a
                >
                <a
                  target="_blank"
                  href="./search?keyword=&classifyIndex=汽车"
                  ><el-dropdown-item>摩托车 </el-dropdown-item></a
                >
                <a
                  target="_blank"
                  href="./search?keyword=&classifyIndex=汽车"
                  ><el-dropdown-item>房车 </el-dropdown-item></a
                >
                <a
                  target="_blank"
                  href="./search?keyword=&classifyIndex=汽车"
                  ><el-dropdown-item>汽车生活 </el-dropdown-item></a
                >
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </a>
        <a href="./search?keyword=&classifyIndex=运动" target="_blank">
          <el-dropdown
            placement="bottom-end"
            max-height="100px"
            :show-timeout="300"
          >
            <el-button> 运 动 </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <a
                  target="_blank"
                  href="./search?keyword=&classifyIndex=运动"
                >
                  <el-dropdown-item>篮球 </el-dropdown-item></a
                >
                <a
                  target="_blank"
                  href="./search?keyword=&classifyIndex=运动"
                  ><el-dropdown-item>足球 </el-dropdown-item></a
                >
                <a
                  target="_blank"
                  href="./search?keyword=&classifyIndex=运动"
                  ><el-dropdown-item>健身 </el-dropdown-item></a
                >
                <a
                  target="_blank"
                  href="./search?keyword=&classifyIndex=运动"
                  ><el-dropdown-item>竞技体育 </el-dropdown-item></a
                >
                <a
                  target="_blank"
                  href="./search?keyword=&classifyIndex=运动"
                  ><el-dropdown-item>运动文化 </el-dropdown-item></a
                >
                <a
                  target="_blank"
                  href="./search?keyword=&classifyIndex=运动"
                  ><el-dropdown-item>运动综合 </el-dropdown-item></a
                >
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </a>
      </div>
      <div style="margin-top: 5px">
        <a href="./search?keyword=&classifyIndex=电影" target="_blank">
          <el-button> 电 影 </el-button>
        </a>
        <a
          href="./search?keyword=&classifyIndex=电视剧" target="_blank"
        >
          <el-button> 电 视 剧 </el-button>
        </a>
        <a
          href="./search?keyword=&classifyIndex=纪录片" target="_blank"
        >
          <el-button> 纪 录 片 </el-button>
        </a>
        <a href="./search?keyword=&classifyIndex=游戏" target="_blank">
          <el-dropdown
            placement="bottom-end"
            max-height="100px"
            :show-timeout="300"
          >
            <el-button> 游 戏 </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <a
                  target="_blank"
                  href="./search?keyword=&classifyIndex=游戏"
                >
                  <el-dropdown-item>单击游戏 </el-dropdown-item></a
                >
                <a
                  target="_blank"
                  href="./search?keyword=&classifyIndex=游戏"
                  ><el-dropdown-item>电子竞技 </el-dropdown-item></a
                >
                <a
                  target="_blank"
                  href="./search?keyword=&classifyIndex=游戏"
                  ><el-dropdown-item>手机游戏 </el-dropdown-item></a
                >
                <a
                  target="_blank"
                  href="./search?keyword=&classifyIndex=游戏"
                  ><el-dropdown-item>网络游戏 </el-dropdown-item></a
                >
                <a
                  target="_blank"
                  href="./search?keyword=&classifyIndex=游戏"
                  ><el-dropdown-item>桌游棋牌 </el-dropdown-item></a
                >
                <a
                  target="_blank"
                  href="./search?keyword=&classifyIndex=游戏"
                  ><el-dropdown-item>GMV </el-dropdown-item></a
                >
                <a
                  target="_blank"
                  href="./search?keyword=&classifyIndex=游戏"
                  ><el-dropdown-item>音游 </el-dropdown-item></a
                >
                <a
                  target="_blank"
                  href="./search?keyword=&classifyIndex=游戏"
                  ><el-dropdown-item>Mugen </el-dropdown-item></a
                >
                <a
                  target="_blank"
                  href="./search?keyword=&classifyIndex=游戏"
                  ><el-dropdown-item>游戏赛事 </el-dropdown-item></a
                >
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </a>
        <a href="./search?keyword=&classifyIndex=音乐" target="_blank">
          <el-dropdown
            placement="bottom-end"
            max-height="100px"
            :show-timeout="300"
          >
            <el-button> 音 乐 </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <a
                  target="_blank"
                  href="./search?keyword=&classifyIndex=音乐"
                >
                  <el-dropdown-item>原创音乐 </el-dropdown-item></a
                >
                <a
                  target="_blank"
                  href="./search?keyword=&classifyIndex=音乐"
                  ><el-dropdown-item>音乐现场 </el-dropdown-item></a
                >
                <a
                  target="_blank"
                  href="./search?keyword=&classifyIndex=音乐"
                  ><el-dropdown-item>翻唱 </el-dropdown-item></a
                >
                <a
                  target="_blank"
                  href="./search?keyword=&classifyIndex=音乐"
                  ><el-dropdown-item>演奏 </el-dropdown-item></a
                >
                <a
                  target="_blank"
                  href="./search?keyword=&classifyIndex=音乐"
                  ><el-dropdown-item>乐评盘点 </el-dropdown-item></a
                >
                <a
                  target="_blank"
                  href="./search?keyword=&classifyIndex=音乐"
                  ><el-dropdown-item>VOCALOID·UTAU </el-dropdown-item></a
                >
                <a
                  target="_blank"
                  href="./search?keyword=&classifyIndex=音乐"
                  ><el-dropdown-item>MV </el-dropdown-item></a
                >
                <a
                  target="_blank"
                  href="./search?keyword=&classifyIndex=音乐"
                  ><el-dropdown-item>音乐粉丝饭拍</el-dropdown-item></a
                >
                <a
                  target="_blank"
                  href="./search?keyword=&classifyIndex=音乐"
                  ><el-dropdown-item>AI音乐 </el-dropdown-item></a
                >
                <a
                  target="_blank"
                  href="./search?keyword=&classifyIndex=音乐"
                  ><el-dropdown-item>电台</el-dropdown-item></a
                >
                <a
                  target="_blank"
                  href="./search?keyword=&classifyIndex=音乐"
                  ><el-dropdown-item>音乐教学 </el-dropdown-item></a
                >
                <a
                  target="_blank"
                  href="./search?keyword=&classifyIndex=音乐"
                  ><el-dropdown-item>音乐综合 </el-dropdown-item></a
                >
                <a
                  target="_blank"
                  href="./search?keyword=&classifyIndex=音乐"
                  ><el-dropdown-item>说唱 </el-dropdown-item></a
                >
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </a>
        <a href="./search?keyword=&classifyIndex=影视" target="_blank">
          <el-dropdown
            placement="bottom-end"
            max-height="100px"
            :show-timeout="300"
          >
            <el-button> 影 视 </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <a
                  target="_blank"
                  href="./search?keyword=&classifyIndex=影视"
                >
                  <el-dropdown-item>影视杂谈 </el-dropdown-item></a
                >
                <a
                  target="_blank"
                  href="./search?keyword=&classifyIndex=影视"
                  ><el-dropdown-item>影视剪辑 </el-dropdown-item></a
                >
                <a
                  target="_blank"
                  href="./search?keyword=&classifyIndex=影视"
                  ><el-dropdown-item>影视整活 </el-dropdown-item></a
                >
                <a
                  target="_blank"
                  href="./search?keyword=&classifyIndex=影视"
                  ><el-dropdown-item>AI影像 </el-dropdown-item></a
                >
                <a
                  target="_blank"
                  href="./search?keyword=&classifyIndex=影视"
                  ><el-dropdown-item>预告·资讯 </el-dropdown-item></a
                >
                <a
                  target="_blank"
                  href="./search?keyword=&classifyIndex=影视"
                  ><el-dropdown-item>小剧场 </el-dropdown-item></a
                >
                <a
                  target="_blank"
                  href="./search?keyword=&classifyIndex=影视"
                  ><el-dropdown-item>短片 </el-dropdown-item></a
                >
                <a
                  target="_blank"
                  href="./search?keyword=&classifyIndex=影视"
                  ><el-dropdown-item>影视综合 </el-dropdown-item></a
                >
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </a>
        <a href="./search?keyword=&classifyIndex=知识" target="_blank">
          <el-dropdown
            placement="bottom-end"
            max-height="100px"
            :show-timeout="300"
          >
            <el-button> 知 识 </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <a
                  target="_blank"
                  href="./search?keyword=&classifyIndex=知识"
                >
                  <el-dropdown-item>科学科普 </el-dropdown-item></a
                >
                <a
                  target="_blank"
                  href="./search?keyword=&classifyIndex=知识"
                  ><el-dropdown-item>社科·法律·心理 </el-dropdown-item></a
                >
                <a
                  target="_blank"
                  href="./search?keyword=&classifyIndex=知识"
                  ><el-dropdown-item>人文历史 </el-dropdown-item></a
                >
                <a
                  target="_blank"
                  href="./search?keyword=&classifyIndex=知识"
                  ><el-dropdown-item>财经商业 </el-dropdown-item></a
                >
                <a
                  target="_blank"
                  href="./search?keyword=&classifyIndex=知识"
                  ><el-dropdown-item>校园学习 </el-dropdown-item></a
                >
                <a
                  target="_blank"
                  href="./search?keyword=&classifyIndex=知识"
                  ><el-dropdown-item>职业职场 </el-dropdown-item></a
                >
                <a
                  target="_blank"
                  href="./search?keyword=&classifyIndex=知识"
                  ><el-dropdown-item>设计·创意 </el-dropdown-item></a
                >
                <a
                  target="_blank"
                  href="./search?keyword=&classifyIndex=知识"
                  ><el-dropdown-item>野生技能协会 </el-dropdown-item></a
                >
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </a>
        <a href="./search?keyword=&classifyIndex=资讯" target="_blank">
          <el-dropdown
            placement="bottom-end"
            max-height="100px"
            :show-timeout="300"
          >
            <el-button> 资 讯 </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <a
                  target="_blank"
                  href="./search?keyword=&classifyIndex=资讯"
                >
                  <el-dropdown-item>热点 </el-dropdown-item></a
                >
                <a
                  target="_blank"
                  href="./search?keyword=&classifyIndex=资讯"
                  ><el-dropdown-item>环球 </el-dropdown-item></a
                >
                <a
                  target="_blank"
                  href="./search?keyword=&classifyIndex=资讯"
                  ><el-dropdown-item>社会 </el-dropdown-item></a
                >
                <a
                  target="_blank"
                  href="./search?keyword=&classifyIndex=资讯"
                  ><el-dropdown-item>综合 </el-dropdown-item></a
                >
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </a>
        <a href="./search?keyword=&classifyIndex=生活" target="_blank">
          <el-dropdown
            placement="bottom-end"
            max-height="100px"
            :show-timeout="300"
          >
            <el-button> 生 活 </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <a
                  target="_blank"
                  href="./search?keyword=&classifyIndex=生活"
                >
                  <el-dropdown-item>搞笑 </el-dropdown-item></a
                >
                <a
                  target="_blank"
                  href="./search?keyword=&classifyIndex=生活"
                  ><el-dropdown-item>亲子 </el-dropdown-item></a
                >
                <a
                  target="_blank"
                  href="./search?keyword=&classifyIndex=生活"
                  ><el-dropdown-item>出行 </el-dropdown-item></a
                >
                <a
                  target="_blank"
                  href="./search?keyword=&classifyIndex=生活"
                  ><el-dropdown-item>三农 </el-dropdown-item></a
                >
                <a
                  target="_blank"
                  href="./search?keyword=&classifyIndex=生活"
                  ><el-dropdown-item>家具房产 </el-dropdown-item></a
                >
                <a
                  target="_blank"
                  href="./search?keyword=&classifyIndex=生活"
                  ><el-dropdown-item>手工 </el-dropdown-item></a
                >
                <a
                  target="_blank"
                  href="./search?keyword=&classifyIndex=生活"
                  ><el-dropdown-item>绘画 </el-dropdown-item></a
                >
                <a
                  target="_blank"
                  href="./search?keyword=&classifyIndex=生活"
                  ><el-dropdown-item>日常</el-dropdown-item></a
                >
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </a>
        <a href="./search?keyword=&classifyIndex=时尚" target="_blank">
          <el-dropdown
            placement="bottom-end"
            max-height="100px"
            :show-timeout="300"
          >
            <el-button> 时 尚 </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <a
                  target="_blank"
                  href="./search?keyword=&classifyIndex=时尚"
                >
                  <el-dropdown-item>美妆护肤 </el-dropdown-item></a
                >
                <a
                  target="_blank"
                  href="./search?keyword=&classifyIndex=时尚"
                  ><el-dropdown-item>仿妆cos </el-dropdown-item></a
                >
                <a
                  target="_blank"
                  href="./search?keyword=&classifyIndex=时尚"
                  ><el-dropdown-item>穿搭 </el-dropdown-item></a
                >
                <a
                  target="_blank"
                  href="./search?keyword=&classifyIndex=时尚"
                  ><el-dropdown-item>时尚潮流 </el-dropdown-item></a
                >
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </a>
        <el-dropdown
          placement="bottom-end"
          max-height="100px"
          :show-timeout="300"
        >
          <el-button
            class="gengduo"
            style="
              width: 73.5px;
              height: 28px;
              transition: all 0.3s ease;
              font-size: 13.5px;
              background-color: #f6f7f8;
              margin: 0 4px;
              font-weight: 400;
              outline: none;
            "
          >
            更多 <img src="../img/更多.png" class="more" />
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <a
                target="_blank"
                href="./search?keyword=&classifyIndex=动物圈"
                ><el-dropdown-item>动物圈</el-dropdown-item></a
              >
              <a
                target="_blank"
                href="./search?keyword=&classifyIndex=VLOG"
                ><el-dropdown-item>VLOG</el-dropdown-item></a
              >
              <a
                target="_blank"
                href="./search?keyword=&classifyIndex=搞笑"
                ><el-dropdown-item>搞笑</el-dropdown-item></a
              >
              <a
                target="_blank"
                href="./search?keyword=&classifyIndex=单机游戏"
                ><el-dropdown-item>单机游戏</el-dropdown-item></a
              >
              <a
                target="_blank"
                href="./search?keyword=&classifyIndex=虚拟up主"
                ><el-dropdown-item>虚拟up主</el-dropdown-item></a
              >
              <a
                target="_blank"
                href="./search?keyword=&classifyIndex=公益"
                ><el-dropdown-item>公益</el-dropdown-item></a
              >
              <a
                target="_blank"
                href="./search?keyword=&classifyIndex=公开课"
                ><el-dropdown-item>公开课</el-dropdown-item></a
              >
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>
    <div class="classify3" style="display: flex; align-items: center">
      <div class="shuxian"></div>
    </div>
    <div
      class="classify4"
      style="
        display: flex;
        align-items: center;
        flex-direction: column;
        position: absolute;
        transform: translate(-5px, 8px);
      "
    >
      <div style="transform: translate(501px, 19px)">
        <a href="https://www.bilibili.com/read/home?spm_id_from=333.1007.0.0" target="_blank">
          <img src="../img/专栏.png" />
          <span
            style="
              color: #62666c;
              transform: translateX(6px);
              margin-bottom: 1px;
            "
            >专 栏</span
          >
        </a>
        <a
          href="https://www.bilibili.com/blackboard/activity-list.html?spm_id_from=333.1007.0.0" target="_blank"
          style="display: flex; transform: translateX(28px)"
        >
          <img src="../img/活动.png" />
          <span
            style="
              color: #62666c;
              transform: translateX(8px);
              margin-bottom: 1px;
            "
            >活 动</span
          >
        </a>
        <a
          href="https://www.bilibili.com/blackboard/activity-5zJxM3spoS.html?spm_id_from=333.1007.0.0" target="_blank"
          style="display: flex; transform: translateX(56px)"
        >
          <img src="../img/社区中心.png" />
          <span
            style="
              color: #62666c;
              transform: translateX(8px);
              margin-bottom: 1px;
            "
            >社区中心</span
          >
        </a>
      </div>
      <div style="transform: translate(518px, 58px);width: 200px;">
        <a
          href="https://live.bilibili.com/?spm_id_from=333.1007.0.0" target="_blank"
          style="transform: translateX(-43px)"
        >
              <img src="../img/直播.png" style="width: 15px" /><span
                style="
                  font-size: 13px;
                  color: #62666c;
                  transform: translate(6px, -1px);
                "
                >直 播</span>
        </a>
        <a
          href="https://www.bilibili.com/cheese/?csource=common_hp_channelclass_icon&spm_id_from=333.1007.0.0" target="_blank"
          style="transform: translateX(-16px)"
        >
              <img src="../img/课堂.png" /><span
                style="
                  font-size: 13px;
                  color: #62666c;
                  transform: translate(8px, -1px);
                "
                >课 堂</span>
        </a>
        <a
          href="https://music.bilibili.com/pc/music-center/?spm_id_from=333.1007.0.0" target="_blank"
          style="transform: translateX(12px);"
        >
          <img src="../img/新歌热榜.png" />
          <span style="position: absolute; transform: translate(43px, -1px);width: 56px;"
            >新歌热榜</span>
        </a>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: "classify",
  
  setup(){

    function openDynamic(){
      window.open("./dynamic", "_blank");
    }

    return{
      openDynamic,
    }
  }
};
</script>

<style>
.classify-style {
  display: flex;
  justify-content: space-around;
  background-color: white;
  width: 1530px;
  height: 110px;
  transform: translate(-24px, 85px);
}

.dynamic1 {
  border-radius: 50%;
  width: 46px;
  height: 46px;
  background-color: #f0983a;
  display: flex;
  justify-content: center;
  align-items: center;
  transform: translate(-112px, 15px);
  transition: opacity 0.3s ease, visibility 0.3s ease;
}
.dynamic1 img {
  border-radius: 50%;
  width: 20px;
  height: 20px;
  overflow: hidden;
}
.dynamic span {
  font-size: 14px;
  display: flex;
  transform: translate(-112px, 20px);
}

.fire1 {
  border-radius: 50%;
  width: 46px;
  height: 46px;
  background-color: #e28480;
  display: flex;
  justify-content: center;
  align-items: center;
  transform: translate(-112px, 15px);
  transition: opacity 0.3s ease, visibility 0.3s ease;
}
.fire1 img {
  border-radius: 50%;
  width: 20px;
  height: 20px;
  overflow: hidden;
}
.fire span {
  font-size: 14px;
  display: flex;
  transform: translate(-112px, 20px);
}

.dynamic1:hover,
.fire1:hover {
  opacity: 0.8;
}

.classify2 div {
  display: flex;
  justify-content: space-between;
  color: black;
}

.classify2 div a .el-button {
  color: #62666c;
  width: 73.5px;
  height: 28px;
  background-color: #f6f7f8;
  margin: 0 4px;
  outline: none;
  font-size: 13px;
}

.classify2 div a .el-button:hover {
  color: black;
  background-color: #e3e5e7;
  border: none;
  outline: none;
}

.classify2 div .el-button:hover {
  color: black;
  background-color: #e3e5e7;
  border: none;
  outline: none;
}

.more {
  width: 9px;
  display: flex;
  transition: transform 0.3s linear;
  margin-left: 5px;
  margin-top: 2.5px;
  position: relative;
}

.gengduo:hover {
  background-color: #e3e5e7 !important;
}

.gengduo:hover .more {
  transform: rotate(180deg); /* 鼠标悬停时旋转180度 */
}
.shuxian {
  margin-right: 12px;
  width: 1px; /* 竖线的宽度 */
  height: 65px; /* 竖线的高度 */
  background-color: #e3e5e7; /* 竖线的颜色 */
}

.classify4 div {
  width: 200px;
  position: absolute;
  display: flex;
  justify-content: center;
  align-items: center;
}
.classify4 div a {
  display: flex;
  justify-content: center;
  align-items: center;
}
.classify4 div img {
  width: 14px;
}
.classify4 div span {
  font-size: 13px;
  color: #62666c;
}

div{
  --el-border: none;
}

.el-button{
  transition: all 0.3s ease;
}

</style>
