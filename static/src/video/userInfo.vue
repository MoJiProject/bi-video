<template>
  <div
    v-if="store.eitUserInfo!==null"
    style="position: fixed;"
    @mouseover="store.setEitUserInfoHover(true)"
    @mouseleave="store.setEitUserInfo(null),store.setEitUserInfoHover(false)"
    class="up-user-info">
    <img src="../img/主页背景图.webp" class="up-user-info-bg" />
    <div class="up-user-info-container">
    <img
        :src="store.eitUserInfo.avatarAddress"
        class="up-user-info-avatar"
    />
    <div class="up-user-info-container-header">
        <span
        class="up-user-info-name"
        :style="{
            color: store.eitUserInfo.grade > 4 ? '#fb7299' : '#000',
        }"
        >{{ store.eitUserInfo.userName }}</span
        >
        <img
        v-if="store.eitUserInfo.gender === 1"
        src="../img/man.png"
        class="up-user-info-gender"
        />
        <img
        v-if="store.eitUserInfo.gender === 2"
        src="../img/man.png"
        class="up-user-info-gender"
        />
        <img
        v-if="store.eitUserInfo.grade"
        :src="'../img/' + store.eitUserInfo.grade + '级.png'"
        class="up-user-info-level"
        />
    </div>
    <div class="up-user-info-container-content">
        <span style="color: #95999f; cursor: pointer"
        ><span style="color: black">{{
            store.eitUserInfo.followNumber
        }}</span>
        &nbsp;关注</span
        >
        <span style="margin-left: 25px; color: #95999f; cursor: pointer"
        ><span style="color: black">{{
            store.eitUserInfo.fansNumber
        }}</span>
        &nbsp;粉丝</span
        >
        <span style="margin-left: 25px; color: #95999f; cursor: pointer"
        ><span style="color: black">{{
            store.eitUserInfo.likeNumber
        }}</span>
        &nbsp;获赞</span
        >
    </div>
    <div class="up-user-info-container-introduce">
        <span class="up-user-info-introduce-content">
        {{ store.eitUserInfo.introduce }}
        </span>
    </div>
    <div class="up-user-info-container-footer">
        <span
        v-show="store.eitUserInfo.isFansFlag===0"
        class="up-user-info-container-footer-add-follow"
        @click="addFollowAxios(store.eitUserInfo.id)"
        ><img
            src="../img/加关注.png"
            style="width: 14px; margin-right: 4px"
        />关注</span
        >
        <span
        v-show="store.eitUserInfo.isFansFlag===1"
        class="up-user-info-container-footer-delete-follow"
        @click="deleteFollowAxios(store.eitUserInfo.id)"
        @mouseover="deleteFoolowFlag=true"
        @mouseleave="deleteFoolowFlag=false"
        >
        <span
        v-show="!deleteFoolowFlag"
            >已关注</span
        >
        <span 
        v-show="deleteFoolowFlag"
            >取消关注</span
        ></span
        >
        <span class="up-user-info-container-footer-message" @click="addDialogueF(store.eitUserInfo.id)">发消息</span>
    </div>
    </div>
  </div>
</template>

<script setup>
import { ElMessage } from "element-plus";
import { reactive, ref } from "vue";
import apiClient from "../services/apiClient";
import { useGlobalStore } from "../store/store";
import {addDialogue} from '../api/privateMessage/index';


const store = useGlobalStore();
const deleteFoolowFlag=ref(false);


//关注用户
async function addFollowAxios(upUserId) {
      try {
        store.setAddFollowFlag(0);

        if (store.userId === null) {
          loginDialogVisibleFlag.value =
            loginDialogVisibleFlag.value === 0 ? 1 : 0;
          return;
        }

        if (store.userId === upUserId) {
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
            followId: upUserId,
            fansId: store.userId,
          },
          {
            headers: {
              "Content-Type": "application/json",
              Authorization: store.token,
            },
          },
        );
        if(response.data.code === 1)
        {
        const eitUserInfo=reactive(store.eitUserInfo);
        eitUserInfo.isFansFlag=1;
        store.setEitUserInfo(eitUserInfo);
        }
      } catch (error) {}
}

//取消关注用户
async function deleteFollowAxios(upUserId) {
      try {
        store.setAddFollowFlag(0);

        if (store.userId === null) {
          loginDialogVisibleFlag.value =
            loginDialogVisibleFlag.value === 0 ? 1 : 0;
          return;
        }

        const response = await apiClient.post(
          "/video/deleteFollow",
          {
            followId: upUserId,
            fansId: store.userId,
          },
          {
            headers: {
              "Content-Type": "application/json",
              Authorization: store.token,
            },
          },
        );
        if (response.data.code === 1) 
       { 
        const eitUserInfo=reactive(store.eitUserInfo);
        eitUserInfo.isFansFlag=0;
        store.setEitUserInfo(eitUserInfo);
       }
      } catch (error) {

      }
}

//添加私信对话并跳转
async function addDialogueF(dialogueId) {

if (store.userId === null) {
    loginDialogVisibleFlag.value =
      loginDialogVisibleFlag.value === 0 ? 1 : 0;
    return;
  }

let dialogue = {
  userId: store.userId,
  dialogueId,
}
addDialogue(store.token,dialogue).then(res=>{

  if(res.data.code===1)
  {
    window.open(
    `./message.html?dialogueId=${dialogueId}`,
    "_blank",
    );
  }

})


}

</script>

<style scoped>
.up-user-info {
  position: absolute;
  top: 100%;
  left: 210%;
  transform: translate(0, 0);
  background-color: white;
  color: #a6acaf;
  border-radius: 10px;
  z-index: 1000;
  width: 350px;
  height: 220px;
  border: 1px solid #d3d3d3; /* 浅灰色边框 */
  box-shadow: 1px 0px 5px #e5e5e5;
  overflow: hidden;
  padding-bottom: 40px;
}

.up-user-avatar-container {
  display: flex;
  background-color: yellow;
  width: 0px;
}

.up-user-info-bg {
  width: 100%;
  height: 40%;
}

.up-user-info-container {
  height: 120px;
  padding-top: 5px;
  padding-left: 10px;
  padding-right: 20px;
  padding-bottom: 15px;
}

.up-user-info-avatar {
  display: flex;
  width: 47px;
  border-radius: 50%;
  cursor: pointer;
}

.up-user-info-container-header {
  display: flex;
  transform: translate(60px, -42px);
}

.up-user-info-name {
  font-size: 14.5px;
  cursor: pointer;
  max-width: 160px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-right: 4px;
}

.up-user-info-gender {
  width: 16px;
  height: 15px;
  margin-top: 2.5px;
  margin-right: 5px;
}

.up-user-info-level {
  width: 22px;
  height: 11px;
  margin-top: 3.5px;
}

.up-user-info-container-content {
  display: flex;
  transform: translate(60px, -30px);
  font-size: 12px;
}

.up-user-info-container-introduce {
  display: flex;
  transform: translate(60px, -20px);
  font-size: 12px;
  padding-right: 60px;
}

.up-user-info-introduce-content {
  display: -webkit-box; /* 必须使用这个 */
  -webkit-box-orient: vertical; /* 垂直方向排列 */
  -webkit-line-clamp: 2;
  overflow: hidden; /* 隐藏超出部分 */
  word-wrap: break-word; /* 超出部分换行 */
  text-overflow: ellipsis; /* 显示省略号 */
}

.up-user-info-container-footer {
  display: flex;
  transform: translate(60px, -5px);
  user-select: none;
  width: 100%;
}

.up-user-info-container-footer-add-follow {
  width: 102px;
  font-size: 14px;
  height: 32px;
  background-color: #00aeec;
  display: flex;
  justify-content: center;
  align-items: center;
  border-radius: 5px;
  margin-right: 5px;
  color: white;
  cursor: pointer;
}

.up-user-info-container-footer-delete-follow {
  width: 102px;
  font-size: 14px;
  height: 32px;
  background-color: #e3e5e7;
  display: flex;
  justify-content: center;
  align-items: center;
  border-radius: 5px;
  margin-right: 5px;
  color: #959595;
  cursor: pointer;
}

.up-user-info-container-footer-delete-follow:hover {
  background-color: #f1f2f3;
}

.up-user-info-container-footer-message {
  width: 100px;
  font-size: 14px;
  height: 30px;
  border: 1px solid #62666c;
  border-radius: 5px;
  display: flex;
  justify-content: center;
  align-items: center;
  color: #62666c;
  cursor: pointer;
}

.up-user-info-container-footer-add-follow:hover {
  opacity: 0.9;
}

.up-user-info-container-footer-message:hover {
  color: #00aeec;
  border: 1px solid #00aeec;
}
</style>