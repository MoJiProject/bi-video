<template>

    <div class="classify2" @mouseover="classify2PartMouseover" @mouseleave="classify2PartMouseleave">

      <div class="classify2-part1">
        <span @mouseover="dynamicHoverFlag=true" @mouseleave="dynamicHoverFlag=false"><img :src="dynamicHoverFlag?dynamicBlue:dynamicGray" class="img1">动 态</span>
        <span @mousemove="fireHoverFlag=true" @mouseleave="fireHoverFlag=false"><img :src="fireHoverFlag?fireBlue:fireGray" class="img2">热 门</span> 
      </div>

      <div class="classify2-part2"></div>

      <div class="classify2-part3">
      <div style="margin-bottom: 5px">
        <a href="./search?keyword=&classifyIndex=番剧" target="_blank">
          <el-dropdown
            :teleported ="false"
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
          :teleported ="false"
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
            :teleported ="false"
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
          :teleported ="false"
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
          :teleported ="false"
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
          :teleported ="false"
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
          :teleported ="false"
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
          :teleported ="false"
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
          :teleported ="false"
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
          :teleported ="false"
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
        <a href="https://www.bilibili.com/read/home?spm_id_from=333.1007.0.0" target="_blank">
            <el-button> 专 栏 </el-button>
        </a>
        <a href="https://www.bilibili.com/blackboard/activity-list.html?spm_id_from=333.1007.0.0" target="_blank">
            <el-button> 活 动 </el-button>
        </a>
        <a href="https://www.bilibili.com/blackboard/activity-5zJxM3spoS.html?spm_id_from=333.1007.0.0" target="_blank">
            <el-button> 社区中心 </el-button>
        </a>
      </div>
      </div>

      <div class="classify2-part3-2" v-show="classify2Part">
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
            :teleported ="false"
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
           :teleported ="false"
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
            :teleported ="false"
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
            :teleported ="false"
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
            :teleported ="false"
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
            :teleported ="false"
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
            :teleported ="false"
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
        <a href="https://www.bilibili.com/v/animal?spm_id_from=333.1007.0.0" target="_blank">
            <el-button> 动物圈 </el-button>
        </a>
        <a href="https://live.bilibili.com/?spm_id_from=333.1007.0.0" target="_blank">
            <el-button> 直 播 </el-button>
        </a>
        <a href="https://www.bilibili.com/cheese/?spm_id_from=333.1007.0.0" target="_blank">
            <el-button> 课 堂 </el-button>
        </a>
        <a href="https://music.bilibili.com/pc/music-center/?spm_id_from=333.1007.0.0" target="_blank">
            <el-button> 新歌热榜 </el-button>
        </a>
      </div>
      </div>

      <div class="classify2-part3-3" v-show="classify2Part">
        <div style="margin-top: 5px">
        <a href="https://www.bilibili.com/v/life/daily/?tag=530003&spm_id_from=333.1007.0.0" target="_blank">
            <el-button> VLOG </el-button>
        </a>
        <a href="https://www.bilibili.com/v/life/funny?spm_id_from=333.1007.0.0" target="_blank">
            <el-button> 搞 笑 </el-button>
        </a>
        <a href="https://www.bilibili.com/v/game/stand_alone?spm_id_from=333.1007.0.0" target="_blank">
            <el-button> 单机游戏 </el-button>
        </a>
        <a href="https://www.bilibili.com/v/virtual?spm_id_from=333.1007.0.0" target="_blank">
            <el-button> 虚拟UP主 </el-button>
        </a>
        <a href="https://love.bilibili.com/?spm_id_from=333.1007.0.0" target="_blank">
            <el-button> 公 益 </el-button>
        </a>
        <a href="https://www.bilibili.com/mooc?spm_id_from=333.1007.0.0" target="_blank">
            <el-button> 公开课 </el-button>
        </a>
      </div>
      </div>

      <div class="classify2-part4">
         <img src="../img/头首页.png">
      </div>
  

    </div>
  
</template>

<script>
import dynamicGray from '../img/动态灰.png';
import dynamicBlue from '../img/动态蓝.png';
import fireGray from '../img/热门灰.png';
import fireBlue from '../img/热门蓝.png';
import { ref } from 'vue';
export default {
  name: 'classify2',
  setup() {

    
   const dynamicHoverFlag = ref(false);
   const fireHoverFlag = ref(false);
   const classify2Part=ref(false);
   let classify2PartTime1=null;
   let classify2PartTime2=null;

   function classify2PartMouseover(){
    clearTimeout(classify2PartTime1);
    clearTimeout(classify2PartTime2);

    classify2PartTime1=setTimeout(() => {
      classify2Part.value=true;
    }, 150);
   }

   function classify2PartMouseleave(){

      clearTimeout(classify2PartTime1);
      clearTimeout(classify2PartTime2);

      classify2PartTime2=setTimeout(() => {
        classify2Part.value=false;
      }, 100);

   }







    return {
      dynamicGray,
      dynamicBlue,
      fireGray,
      fireBlue,
      dynamicHoverFlag,
      fireHoverFlag,
      classify2Part,
      classify2PartMouseover,
      classify2PartMouseleave,


    }




  }
}
</script>

<style lang="scss" scoped>

.classify2{
  top: -55px;
  width: 1425px;
  z-index: 1000000;
  position: fixed;
  height: 55px;
  box-shadow: 2px 0px 4px #d3d3d3;
  background-color: white;
  transition: 0.3s ease-in-out;
}

.classify2:hover{
   height: 140px;

}
 
.classify2-part1{

   position: absolute;
   line-height: 56px;
   left: 60px;
   span{
    cursor: pointer;
    color: #72767b;
    font-size: 13px;
    margin-right: 16px;
    .img1{
      width: 21px;
      height: 20px;
      margin-right: 6px;
      transform: translateY(4px);
    }
    .img2{
      width: 16px;
      height: 19.5px;
      margin-right: 8px;
      transform: translateY(3.5px);
    }
   }
   span:hover{
    color: #00aeec;
   }
}

.classify2-part2{
    position: absolute;
    width: 1px;
    height: 13.5px;
    background-color: #72767b;
    opacity: 0.2;
    left: 200px;
    top: 50%;
    transform: translateY(-50%);
}

.classify2-part3{

top: 15px;
display: flex;
justify-content: space-around;
align-items: center;
flex-direction: column;
width: 700px;
position: absolute;
left: 439px; 
 .el-button{
    margin-right: 6px;
 }
  

}

.classify2-part4{
    position: absolute;
    top: 15px;
    width: 24px;
    height: 24px;
    display: flex;
    justify-content: center;
    align-items: center;
    border-radius: 5px;
    left: 96.7%;
    cursor: pointer;
    img{
        width: 8px;
        height: 5px;
        transition: 0.3s ease-in-out;

    }
}

.classify2-part4:hover{
    background-color: rgba(231, 231, 231);
}

.classify2:hover{
    .classify2-part4{
        img{
            transform: rotate(180deg);
        }
    }
}

.classify2-part3-2{

top: 53px;
display: flex;
justify-content: space-around;
align-items: center;
flex-direction: column;
width: 700px;
position: absolute;
left: 439px; 
 .el-button{
    margin-right: 6px;
 }
  
}

.classify2-part3-3{

top: 95px;
display: flex;
justify-content: space-around;
align-items: center;
flex-direction: column;
width: 700px;
position: absolute;
left: 107px; 

 .el-button{
    margin-right: 6px;
 }
  
}

div{
  --el-border: none;
}
</style>