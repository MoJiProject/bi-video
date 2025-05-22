<template>
  <div
  v-if="store.userId!== null&&pageLoad"
    class="common-layout"
    style="user-select: none"
  >
    <el-container>
      <el-header
        style="
          position: relative;
          width: 110%;
          z-index: 200;
          height: 50px;
          background-color: white;
          box-shadow: 0 4px 5px rgba(0, 0, 0, 0.03);
          transform: translate(-15px,10px); /* 仅在底部添加阴影 */
        "
      >
        <img
          src="../img/bilibili蓝.png"
          style="width: 55px; transform: translate(30px, 8px)"
        />
        <span
          style="
            display: flex;
            color: #00a1d6;
            font-weight: bold;
            font-size: 21px;
            transform: translate(90px, -25px);
          "
          >创作中心</span
        >
        <a
          href="../"
          target="_blank"
          style="
            text-decoration: none;
            color: #757575;
            font-size: 14px;
            display: flex;
            transform: translate(220px, -52px);
          "
        >
          <img
            src="../img/主站.png"
            style="transform: translate(); height: 16px"
          />
          <span style="transform: translate(-15px, 3px)">主站</span>
        </a>
       <div class="right">
          <div class="avatar">
            <a :href="'/home?homeMenu=1&userId='+store.userId" target="_blank"><img :src="user.avatarAddress" class="avatar1"/></a>
            <div class="feature">
              <a href="/account" target="_blank">
                <div style="cursor: pointer">
                  <img src="../img/个人中心.png" /> <span>个人中心 </span>
                </div>
              </a>
              <a
                href="/contribute/subpage2"
                style="text-decoration: none; animation: none !important"
                ><div>
                  <img src="../img/投稿管理.png" /> <span>投稿管理 </span>
                </div></a
              >
              <div style="cursor: pointer" @click="logout">
                <img src="../img/退出登录.png" /> <span>退出登录</span>
              </div>
            </div>
          </div>
          <div
            style="
              font-size: 13px;
              transform: translate(970px, -12px);
              width: 160px;
              height: 28px;
              background-color: #fdf4ef;
              display: flex;
              justify-content: center;
              align-items: center;
              border-radius: 25px;
              border: 1px solid #f6cbb2;
              color: #eb9362;
            "
          >
            成为up主的第{{ daysAsUP }}天 >
          </div>
          <div style="transform: translate(1160px, -40px); color: #ededed">
            |
          </div>
          <div class="message">
            <a v-if="user.userName !== null" href="/message" target="_blank">
              <div
              v-if="store.userInformation.allMessageNumber>0"
              class="number-style">
              {{ store.userInformation.allMessageNumber>99? '99+' : store.userInformation.allMessageNumber }}
              </div>
              <img src="../img/消息灰色.png" alt="消息" />
            </a>
            <div class="message-info" v-if="user.userName !== null">
              <span style="margin-top: 15px;" @click="openMessage(1)">
                <div class="text">我的消息</div>
                <div v-if="store.userInformation.messageNumber>0" class="message-number-style">{{ store.userInformation.messageNumber>99? '99+' : store.userInformation.messageNumber }}</div>
              </span>
              <span @click="openMessage(2)">
                <div class="text">回复我的</div>
                <div v-if="store.userInformation.replyCommentNumber>0" class="message-number-style">{{ store.userInformation.replyCommentNumber>99? '99+' : store.userInformation.replyCommentNumber }}</div>
              </span>
              <span @click="openMessage(3)">
                <div class="text">@我的</div>
                <div v-if="store.userInformation.atNumber>0" class="message-number-style">{{ store.userInformation.atNumber>99? '99+' : store.userInformation.atNumber }}</div>
              </span>
              <span @click="openMessage(4)">
                <div class="text">收到的赞</div>
                <div v-if="store.userInformation.likeAllNumber>0" class="message-number-style">{{ store.userInformation.likeAllNumber>99? '99+' : store.userInformation.likeAllNumber }}</div>
              </span>
              <span @click="openMessage(5)">
                <div class="text">消息设置</div>
              </span>
            </div>
          </div>
       </div>
      </el-header>
      <el-container >
        <el-aside width="200px">
          <AsideComponent style="position: relative;z-index: 10;"/>
        </el-aside>
        <el-main>
          <SubPage3 style="position: relative;z-index: 10;"/>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script>
import { reactive, onMounted, computed, ref } from "vue";
import apiClient from "../services/apiClient";
import {useGlobalStore} from "../store/store";
import "element-plus/theme-chalk/el-message.css";
import { ElMessage } from "element-plus";
import AsideComponent from './AsideComponent.vue';
import SubPage3 from "./SubPage3.vue";
export default {
  name: "Contribute",
  components: {
    AsideComponent,
    SubPage3,
  },
  setup() {
    
    const pageLoad=ref(false);
    const store = useGlobalStore();
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
    // 计算成为up主的天数
    const daysAsUP = computed(() => {
      const nowDate = new Date();
      const upDate = new Date(user.createTime);
      if (!isNaN(upDate)) {
        const diffTime = nowDate - upDate; // 毫秒差
        return Math.floor(diffTime / (1000 * 60 * 60 * 24)); // 转换为天数
      }
      return 0; // 如果upDate无效，返回0
    });

    onMounted(async() => {
      document.title = "创作中心-哔哩哔哩弹幕视频网 - (゜- ゜)つロ 干杯~";
      window.scrollTo({top: 0, behavior: "smooth"});
      await getUserIp();
      await ChecklLogin();
      pageLoad.value=true;
      
    });
    
    //检查登录
    async function ChecklLogin() {
      try {
        const response = await apiClient.get(`/user/checkLoginFlag/${store.userIp}`,
        );
        if (response.data.code === 1) {
          store.setUserId(response.data.data.id);
          store.setUserInformation(response.data.data);
          Object.assign(user, response.data.data);
        } else {
          window.location.href="../";
        }
      } catch (error) {
        window.location.href="../";
      }
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
    async function getUserIp(){
    
    const response = await apiClient.get("/userIp/getUserIp");

    if(response.data.code === 1)
      store.setUserIp(response.data.data.userIp);
      store.setToken(response.data.data.token);

    }

    //打开消息页面
    function openMessage(menu){

      if(store.userInformation.allMessageNumber>0)
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
    };
  },
};
</script>

<style scoped lang="scss">

.avatar1 {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  transform: translate(930px, 25px);
}
.avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
}

.feature {
  transform: translate(880px, 20px);
  transition: opacity 0.3s ease, visibility 0.3s ease; /* 过渡效果 */
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  margin-top: 5px;
  visibility: hidden; /* 默认隐藏 */
  opacity: 0;
  width: 150px;
  height: 120px;
  background-color: white;
  /* 添加外阴影 */
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2); /* 水平偏移, 垂直偏移, 模糊半径, 阴影颜色 */

}

.avatar:hover .feature {
  visibility: visible;
  opacity: 1;
}

.feature div {
  margin-right: 82px;
  font-size: 14px;
  width: 150px;
  height: auto;
  display: flex;
  transform: translateX(41px);
  align-items: center;
  border-radius: 5px;
  transition: opacity 0.3s ease, visibility 0.3s ease,background-color 0.3s ease;
}

.feature div:hover {
  background-color: #e3e5e7;
}

.feature div span {
  height: 35px;
  display: flex;
  align-items: center;
  justify-content: center;
  transform: translateX(27px);
  color: #62666c;
}
.feature div img {
  width: 18px;
  height: 16px;
  transform: translateX(14px);
}


.message {
  position: absolute; /* 使子元素绝对定位 */
  transform: translate(1200px, -63px);
}

.message a {
  text-decoration: none;
}

.message-info {
  text-align: center;
  position: absolute;
  display: flex;
  flex-direction: column;
  top: 100%; /* 改为100%以确保它在消息下方 */
  left: 50%;
  transform: translate(-50%, 10px); /* 调整偏移 */
  background-color: white;
  color: rgb(0, 0, 0);
  padding: 10px;
  border-radius: 10px;
  opacity: 0;
  visibility: hidden;
  transition: opacity 0.4s ease, visibility 0.4s ease;
  z-index: 5;
  width: 130px;
  height: auto; /* 高度自适应内容 */
  border: 1px solid #d3d3d3; /* 浅灰色边框 */
}

.message-info span {
  height: 40px;
  width: 150px;
  font-size: 14px;
  display: flex;
  align-items: center;
  transform: translate(-10px, -7px);
  color: #62666c;
  transition: all 0.3s ease;
  cursor: pointer;
}

.message-info span .text {
  display: flex;
  transform: translateX(20px);
}

.message-info span:hover {
  background-color: #e3e5e7;
}

.message:hover .message-info {
  visibility: visible;
  opacity: 1;
}

.number-style {
  position: absolute;
  z-index: 1;
  padding: 0 3px;
  top: -1px;
  left: -2px;
  min-width: 9px;
  border-radius: 10px;
  text-align: center;
  background-color: #fa5a57;
  color: #fff;
  font-size: 12px;
  line-height: 15px;
}

.message-number-style{
  position: absolute;
  z-index: 1;
  padding: 0 3px;
  min-width: 9px;
  right: 15px;
  border-radius: 10px;
  text-align: center;
  background-color: #fa5a57;
  color: #fff;
  font-size: 12px;
  line-height: 15px;
}

.right{
  position: relative;
  left: 80px;
  top: -100px;
}

</style>
