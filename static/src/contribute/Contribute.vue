<template>
  <div
  v-if="store.userId!== null&&pageLoad"
    class="common-layout"
    style="user-select: none;"
  >
    <el-container>
      <el-header class="cu-header">
        <div class="cu-header-inner">
          <div class="cu-brand">
            <img class="cu-logo" src="/img/logo.png" alt="青芒视频" />
            <span class="cu-brand-title">创作中心</span>
            <a class="cu-back" href="../" target="_blank">
              <img src="/img/主站.png" alt="" />
              <span>主站</span>
            </a>
          </div>

          <div class="cu-actions">
            <div class="cu-up-days">成为创作者的第{{ daysAsUP }}天 &gt;</div>
            <span class="cu-divider"></span>

            <div class="message">
              <a v-if="user.userName !== null" href="/message" target="_blank" class="message-trigger">
                <div v-if="allMessageNumber > 0" class="number-style">
                  {{ allMessageNumber > 99 ? '99+' : allMessageNumber }}
                </div>
                <img src="/img/消息灰色.png" alt="消息" />
              </a>
              <div class="message-info" v-if="user.userName !== null">
                <span @click="openMessage(1)">
                  <div class="text">我的消息</div>
                  <div v-if="messageNumber > 0" class="message-number-style">
                    {{ messageNumber > 99 ? '99+' : messageNumber }}
                  </div>
                </span>
                <span @click="openMessage(2)">
                  <div class="text">回复我的</div>
                  <div v-if="replyCommentNumber > 0" class="message-number-style">
                    {{ replyCommentNumber > 99 ? '99+' : replyCommentNumber }}
                  </div>
                </span>
                <span @click="openMessage(3)">
                  <div class="text">@我的</div>
                  <div v-if="atNumber > 0" class="message-number-style">
                    {{ atNumber > 99 ? '99+' : atNumber }}
                  </div>
                </span>
                <span @click="openMessage(4)">
                  <div class="text">收到的赞</div>
                  <div v-if="likeAllNumber > 0" class="message-number-style">
                    {{ likeAllNumber > 99 ? '99+' : likeAllNumber }}
                  </div>
                </span>
                <span @click="openMessage(5)">
                  <div class="text">消息设置</div>
                </span>
              </div>
            </div>

            <div class="avatar">
              <a :href="'/home?homeMenu=1&userId='+store.userId" target="_blank">
                <img :src="user.avatarAddress" class="avatar1" alt="" />
              </a>
              <div class="feature">
                <a href="/account" target="_blank">
                  <div>
                    <img src="/img/个人中心.png" alt="" /> <span>个人中心</span>
                  </div>
                </a>
                <router-link to="/contribute/subpage2">
                  <div>
                    <img src="/img/投稿管理.png" alt="" /> <span>投稿管理</span>
                  </div>
                </router-link>
                <div @click="logout">
                  <img src="/img/退出登录.png" alt="" /> <span>退出登录</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </el-header>
      <el-container >
        <el-aside width="200px" class="contribute-aside">
          <AsideComponent />
        </el-aside>
        <el-main class="contribute-main">
          <component :is="currentContentComponent" class="contribute-content" />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script>
import { reactive, onMounted, computed, ref } from "vue";
import { useRoute } from "vue-router";
import apiClient from "../services/apiClient";
import {useGlobalStore} from "../store/store";
import "element-plus/theme-chalk/el-message.css";
import { ElMessage } from "element-plus";
import AsideComponent from './AsideComponent.vue';
import MainComponent from "./MainComponent.vue";
import EditContribute from "./EditContribute.vue";
import SubPage1 from "./SubPage1.vue";
import SubPage2 from "./SubPage2.vue";
export default {
  name: "ContributePage",
  components: {
    AsideComponent,
    MainComponent,
    EditContribute,
    SubPage1,
    SubPage2,
  },
  setup() {
    
    const pageLoad=ref(false);
    const store = useGlobalStore();
    const route = useRoute();
    const contentComponentMap = {
      "/contribute/edit": EditContribute,
      "/contribute/subpage1": SubPage1,
      "/contribute/subpage2": SubPage2,
    };
    const currentContentComponent = computed(() => contentComponentMap[route.path] || MainComponent);
    const user = reactive({
      avatarAddress: "",
      coinNumber: 0,
      collectNumber: 0,
      createTime: "",
      dynamicNumber: 0,
      grade: "",
      id: 0,
      messageNumber: 0,
      phone: "",
      gender: "",
      userName: null,
      users: "",
      videoNumber: 0,
      token: "",
      exp: 0,
      followNumber: 0,
      ownDynamicNumber: 0,
      fansNumber: 0,
      introduce: "",
    });
    // 计算成为创作者的天数
    const daysAsUP = computed(() => {
      const nowDate = new Date();
      const upDate = new Date(user.createTime);
      if (!isNaN(upDate)) {
        const diffTime = nowDate - upDate; // 毫秒差
        return Math.floor(diffTime / (1000 * 60 * 60 * 24)); // 转换为天数
      }
      return 0; // 如果upDate无效，返回0
    });

    // store.userInformation 初始可能是 null，模板里直接读 .allMessageNumber 会在
    // 异步填充前抛错并让整页白屏，这里统一兜底成 0。
    const unreadCount = (key) => Number(store.userInformation?.[key]) || 0;
    const allMessageNumber = computed(() => unreadCount("allMessageNumber"));
    const messageNumber = computed(() => unreadCount("messageNumber"));
    const replyCommentNumber = computed(() => unreadCount("replyCommentNumber"));
    const atNumber = computed(() => unreadCount("atNumber"));
    const likeAllNumber = computed(() => unreadCount("likeAllNumber"));

    onMounted(async() => {
      document.title = "创作中心 - 青芒视频";
      window.scrollTo({top: 0, behavior: "smooth"});
      await getUserIp();
      pageLoad.value=true;
      
    });
    
    //获取用户ip和token
    // token 由 /auth/login 返回后存在本地，这里不再向后端要 IP + token。
    async function getUserIp(){
      store.setUserIp("0.0.0.0");
    }

    //登出
    async function logout() {

    let limiterLoginDto={
        user: user,
        userIp: store.userIp,
      };
    try {
      const response = await apiClient.post("/user/signOut",limiterLoginDto,{
        headers: {
          "Content-Type": "application/json",
          "Authorization": store.token,
        },          
      });
      if(response.data.code === 1){
        location.reload(); 
      }
      else{
        ElMessage({
          message: response.data.msg,
          type: "info",
          plain: true,
          duration: 1700,
        });
        if(response.data.msg==="您还没有登录")
        location.reload(); 
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

    //获取用户ip和token
    // token 由 /auth/login 返回后存在本地，这里不再向后端要 IP + token。
    async function getUserIp(){
      store.setUserIp("0.0.0.0");
    }
    //打开消息页面
    function openMessage(menu){

      if(store.userInformation?.allMessageNumber>0)
      {
        let userInformation=store.userInformation;
        userInformation.allMessageNumber=0;
        if(menu===1)
        userInformation.messageNumber=0;
        if(menu===2)
        userInformation.replyCommentNumber=0;
        if(menu===3)
        userInformation.atNumber=0;
        if(menu===4)
        userInformation.likeAllNumber=0;
        store.setUserInformation(userInformation);
      }

      window.open(
        `/message?messageMenu=${menu}`,
        "_blank",
      );
    }

    return {
      user,
      store,
      logout,
      pageLoad,
      daysAsUP,
      openMessage,
      currentContentComponent,
      allMessageNumber,
      messageNumber,
      replyCommentNumber,
      atNumber,
      likeAllNumber,
    };
  },
};
</script>

<style scoped lang="scss">
/* ---------- 页头 ----------
   原来是 logo translate(30px,8px)、标题 translate(90px,-25px)、主站 translate(220px,-52px)、
   消息图标 translate(1200px,-63px) 这类硬像素偏移，视口一变就整排散开。
   现在改成一条 flex 横轴：左侧品牌区、右侧操作区，中间自动留空。
   两个悬浮面板挂在自己触发器的下方绝对定位，不再依赖 transparent-div 算位置。
*/
.cu-header {
  position: sticky;
  top: 0;
  z-index: 200;
  height: 56px;
  padding: 0;
  background-color: #fff;
  box-shadow: 0 1px 6px rgba(0, 0, 0, .04);
}

.cu-header-inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--gap-4);
  width: 100%;
  max-width: var(--page-max);
  height: 100%;
  margin: 0 auto;
  padding: 0 var(--page-pad);
}

.cu-brand {
  display: flex;
  align-items: center;
  gap: var(--gap-3);
  min-width: 0;
}

.cu-logo {
  width: 48px;
  height: auto;
  flex: none;
}

.cu-brand-title {
  color: var(--brand);
  font-size: 19px;
  font-weight: 700;
  letter-spacing: 1px;
  white-space: nowrap;
}

.cu-back {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  margin-left: var(--gap-4);
  padding: 4px 10px;
  border-radius: 999px;
  color: var(--ink-2);
  font-size: 13px;
  white-space: nowrap;
  transition: background .18s, color .18s;

  img {
    width: 16px;
    height: 16px;
    object-fit: contain;
  }
}

.cu-back:hover {
  background: var(--brand-soft);
  color: var(--brand);
}

.cu-actions {
  display: flex;
  align-items: center;
  gap: var(--gap-4);
}

.cu-up-days {
  height: 28px;
  padding: 0 16px;
  display: flex;
  align-items: center;
  border-radius: 999px;
  border: 1px solid #f6cbb2;
  background: #fdf4ef;
  color: #eb9362;
  font-size: 13px;
  white-space: nowrap;
}

.cu-divider {
  width: 1px;
  height: 20px;
  background: var(--line);
}

/* ---------- 消息 ---------- */
.message {
  position: relative;
}

.message-trigger {
  display: block;
  position: relative;
  line-height: 0;

  img {
    width: 20px;
    height: 20px;
    object-fit: contain;
  }
}

.message-info {
  position: absolute;
  top: 100%;
  right: 0;
  z-index: 300;
  display: flex;
  flex-direction: column;
  width: 156px;
  padding: 6px;
  border: 1px solid var(--line);
  border-radius: var(--radius-md);
  background: #fff;
  box-shadow: 0 6px 20px rgba(0, 0, 0, .1);
  opacity: 0;
  visibility: hidden;
  transition: opacity .2s ease, visibility .2s ease;

  span {
    position: relative;
    display: flex;
    align-items: center;
    height: 36px;
    padding: 0 var(--gap-3);
    border-radius: var(--radius-sm);
    color: var(--ink-2);
    font-size: 14px;
    cursor: pointer;
    transition: background .18s, color .18s;
  }

  span:hover {
    background: var(--fill);
    color: var(--brand);
  }

  .text {
    flex: 1;
  }
}

.message:hover .message-info {
  opacity: 1;
  visibility: visible;
}

.number-style {
  position: absolute;
  top: -7px;
  left: 10px;
  z-index: 1;
  min-width: 16px;
  padding: 0 4px;
  border-radius: 999px;
  background: #fa5a57;
  color: #fff;
  font-size: 11px;
  line-height: 16px;
  text-align: center;
}

.message-number-style {
  position: absolute;
  right: 10px;
  z-index: 1;
  min-width: 16px;
  padding: 0 4px;
  border-radius: 999px;
  background: #fa5a57;
  color: #fff;
  font-size: 11px;
  line-height: 16px;
  text-align: center;
}

/* ---------- 头像与悬浮菜单 ---------- */
.avatar {
  position: relative;
  width: 36px;
  height: 36px;
  border-radius: 50%;
  cursor: pointer;
}

.avatar1 {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  object-fit: cover;
  display: block;
}

.feature {
  position: absolute;
  top: 100%;
  right: 0;
  z-index: 300;
  display: flex;
  flex-direction: column;
  width: 156px;
  margin-top: var(--gap-2);
  padding: 6px;
  border-radius: var(--radius-md);
  background: #fff;
  box-shadow: 0 6px 20px rgba(0, 0, 0, .12);
  visibility: hidden;
  opacity: 0;
  transition: opacity .2s ease, visibility .2s ease;

  div {
    display: flex;
    align-items: center;
    gap: var(--gap-2);
    height: 36px;
    padding: 0 var(--gap-3);
    border-radius: var(--radius-sm);
    font-size: 14px;
    color: var(--ink-2);
    cursor: pointer;
    transition: background .18s, color .18s;
  }

  div:hover {
    background: var(--fill);
    color: var(--brand);
  }

  img {
    width: 18px;
    height: 18px;
    object-fit: contain;
    flex: none;
  }
}

.avatar:hover .feature,
.avatar:focus-within .feature {
  visibility: visible;
  opacity: 1;
}

/* ---------- 主体 ---------- */
.common-layout {
  width: 100%;
  max-width: var(--page-max);
  margin: 0 auto;
  user-select: none;
}

.contribute-aside,
.contribute-main,
.contribute-content {
  position: relative;
  z-index: 10;
}

.contribute-main {
  overflow: visible;
}

@media (max-width: 900px) {
  .cu-up-days {
    display: none;
  }
}

@media (max-width: 720px) {
  .cu-back {
    display: none;
  }
}
</style>
