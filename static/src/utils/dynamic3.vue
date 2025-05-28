<template>
  <div v-show="dynamic.dynamicUserInfo2.id" class="dynamic">
      <div class="dynamic-content">
          <div v-if="dynamic.dynamic.title" class="title">{{ dynamic.dynamic.title }}</div>
          <div class="head">
              <div class="avatar">
                <div class="avatar-container" @mouseover="selectFollowFlag(dynamic.dynamicUserInfo2)" @mouseleave="selectFollowDelayF">
                  <img @click="openHome(1,dynamic.dynamicUserInfo2.id)" :src="dynamic.dynamicUserInfo2.avatarAddress">
                  <userInfo2 class="user-info" :user-info="dynamic.dynamicUserInfo2"/>
                </div>
              </div>
              <div class="user">
                  <div class="user-name"><span @click="openHome(1,dynamic.dynamicUserInfo2.id)">{{ dynamic.dynamicUserInfo2.userName }}</span></div>
                  <div class="date"><span>{{ dynamic.video&&!dynamic.comment?dynamic.video.createTime:dynamic.publishTime }}</span><span v-if="!dynamic.comment&&dynamic.video">&nbsp;·&nbsp;投稿了视频</span></div>
              </div>
          </div>
          <div v-if="dynamic.dynamic.commentId" class="body">
              <div class="content">
                <span v-html="dynamic.comment?dynamic.comment.content:dynamic.dynamic.content"></span>
              </div>
              <template v-if="dynamic.dynamic.imgAddress&&dynamic.dynamic.imgAddress.split(',').length>1">
                <el-image
                v-for="(dynamicImg, index) in dynamic.dynamic.imgAddress
                  .split(',')
                  .slice(0, dynamic.dynamic.imgAddress.split(',').length - 1)"
                :key="dynamicImg"
                class="img"
                :hide-on-click-modal="true"
                :src="dynamicImg"
                :zoom-rate="1.2"
                :max-scale="7"
                :min-scale="0.2"
                :preview-teleported="true"
                :preview-src-list="
                  dynamic.dynamic.imgAddress
                    .split(',')
                    .slice(0, dynamic.dynamic.imgAddress.split(',').length - 1)
                "
                :initial-index="index"
                fit="cover"
                />
              </template>
              <div v-if="dynamic.video" class="video-info-container">
                   <div class="up-info">
                       <div class="left">
                           <div @click="openHome(1,dynamic.upUserInfo2.id)" class="up-avatar">
                               <img :src="dynamic.upUserInfo2.avatarAddress">
                               <userInfo2 @click.stop class="up-user-info" :user-info="dynamic.upUserInfo2"/>
                           </div>
                           <span @click="openHome(1,dynamic.upUserInfo2.id)" class="up-user-name">
                            {{ dynamic.upUserInfo2.userName }}
                            <userInfo2 @click.stop class="up-user-info" :user-info="dynamic.upUserInfo2"/>
                           </span>
                           <span class="end">投稿了视频</span>
                       </div>
                       <div class="right">
                            <div v-show="dynamic.upUserInfo2.isFollowFlag===0" class="follow" @click="followF(dynamic.upUserInfo2)">关注</div>
                            <div v-show="dynamic.upUserInfo2.isFollowFlag===1" class="unfollow" @click="deleteFollowDialogFlag=true">已关注</div>   
                        </div>
                   </div>
                   <videoInfo class="video-info" @click="openVideo(dynamic.video.videoId)" :video="dynamic.video"/> 
              </div>
              <div v-else class="delete-info-container">
                  <div class="delete-info">
                     <svg class="dyn-none__icon" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" viewBox="0 0 24 24" width="24" height="24"><path d="M9.83496 3.5C10.7972 1.83333 13.20285 1.83334 14.16505 3.5L21.7428 16.625C22.705 18.29165 21.5022 20.375 19.5777 20.375L4.42228 20.375C2.49778 20.375 1.29496 18.29165 2.25722 16.625L9.83496 3.5zM12 8.125C11.5858 8.125 11.25 8.46079 11.25 8.875L11.25 13.5C11.25 13.9142 11.5858 14.25 12 14.25C12.4142 14.25 12.75 13.9142 12.75 13.5L12.75 8.875C12.75 8.46079 12.4142 8.125 12 8.125zM12.75 16.125C12.75 15.7108 12.4142 15.375 12 15.375C11.5858 15.375 11.25 15.7108 11.25 16.125L11.25 16.5C11.25 16.9142 11.5858 17.25 12 17.25C12.4142 17.25 12.75 16.9142 12.75 16.5L12.75 16.125z" fill="currentColor"></path></svg>
                     <span>源动态已被作者删除</span>
                  </div>
              </div>
          </div>
          <div v-else class="body">
              <div class="content">
                <span v-html="dynamic.comment?dynamic.comment.content:dynamic.dynamic.content"></span>
              </div>
              <template v-if="dynamic.dynamic.imgAddress&&dynamic.dynamic.imgAddress.split(',').length>1">
                <el-image
                v-for="(dynamicImg, index) in dynamic.dynamic.imgAddress
                  .split(',')
                  .slice(0, dynamic.dynamic.imgAddress.split(',').length - 1)"
                :key="dynamicImg"
                class="img"
                :hide-on-click-modal="true"
                :src="dynamicImg"
                :zoom-rate="1.2"
                :max-scale="7"
                :min-scale="0.2"
                :preview-teleported="true"
                :preview-src-list="
                  dynamic.dynamic.imgAddress
                    .split(',')
                    .slice(0, dynamic.dynamic.imgAddress.split(',').length - 1)
                "
                :initial-index="index"
                fit="cover"
                />
              </template>
          </div>
          <div class="up-btn" :style="{ top: dynamic.dynamic.title? '72px' : '32px' }" @mouseover="selectFollowFlag(dynamic.dynamicUserInfo2)" @mouseleave="selectFollowDelayF">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"><path fill-rule="evenodd" clip-rule="evenodd" d="M13.7485 5.49841C13.7485 6.46404 12.9657 7.24683 12.0001 7.24683C11.0344 7.24683 10.2516 6.46404 10.2516 5.49841C10.2516 4.53279 11.0344 3.75 12.0001 3.75C12.9657 3.75 13.7485 4.53279 13.7485 5.49841ZM13.7484 18.4982C13.7484 19.4639 12.9656 20.2467 11.9999 20.2467C11.0343 20.2467 10.2515 19.4639 10.2515 18.4982C10.2515 17.5326 11.0343 16.7498 11.9999 16.7498C12.9656 16.7498 13.7484 17.5326 13.7484 18.4982ZM11.9999 13.7487C12.9656 13.7487 13.7484 12.9658 13.7484 12.0002C13.7484 11.0345 12.9656 10.2517 11.9999 10.2517C11.0343 10.2517 10.2515 11.0345 10.2515 12.0002C10.2515 12.9658 11.0343 13.7487 11.9999 13.7487Z" fill="currentColor"></path></svg>
            <div v-if="dynamic.dynamicUserInfo2.id!==store.userId" class="up-info">
               <div @click="dynamic.dynamicUserInfo2?.isFollowFlag?unFollowF(dynamic.dynamicUserInfo2):followF(dynamic.dynamicUserInfo2)">{{ dynamic.dynamicUserInfo2?.isFollowFlag?'取消关注':'关注' }}</div>
               <div v-if="store.userInformation.adminFlag" @click="deleteDynamicDialogFlag=true">删除</div>
            </div>
            <div v-else class="up-info2">
               <div @click="putUpStatusF(dynamic.dynamic)">{{ dynamic.dynamic.upFlag===1?'取消置顶':'置顶' }}</div>
               <div @click="deleteDynamicDialogFlag=true">删除</div>
            </div>
          </div>
      </div>
      <el-dialog
      v-model="deleteFollowDialogFlag"
      append-to-body
      width="320"
      :lock-scroll="false"
      align-center
      style="height: 152px; border-radius: 12px"
    >
      <div class="delete-dialog-title">取消关注</div>
      <div class="delete-dialog-content">
        真的要取消对 {{ dynamic.upUserInfo2.userName }} 的关注吗？
      </div>
      <div class="delete-dialog-btn-container">
        <button class="delete-dialog-cancel-btn" @click="deleteFollowDialogFlag=false">
        点错了~
      </button>
      <button
        class="delete-dialog-confirm-btn"
        @click="unFollowF(dynamic.upUserInfo2)"
      >
        取消关注
      </button>
      </div>
      </el-dialog>
      <el-dialog
      v-model="deleteDynamicDialogFlag"
      append-to-body
      width="320"
      :lock-scroll="false"
      align-center
      style="height: 152px; border-radius: 12px"
    >
      <div class="delete-dialog-title">要删除动态吗？</div>
      <div class="delete-dialog-content">
        动态删除后将无法恢复，请谨慎操作
      </div>
      <div class="delete-dialog-btn-container">
        <button class="delete-dialog-cancel-btn" @click="deleteDynamicDialogFlag=false">
        取消
      </button>
      <button
        class="delete-dialog-confirm-btn2"
        @click="deleteDynamicF(dynamic)"
      >
        确认删除
      </button>
      </div>
      </el-dialog>    
  </div>
</template>

<script setup>
import { useGlobalStore } from '../store/store';
import { ElMessage } from "element-plus";
import videoInfo from './videoInfo.vue';
import userInfo2 from './userInfo2.vue';
import {putUpStatus,deleteDynamic} from '@/api/dynamic/index'
import {addFollow,deleteFollow,checkFollow} from '../api/user/index';
import { onMounted, ref } from 'vue';


const props = defineProps({
    dynamic:{
        type: Object,
        required: true
    },
})

const store = useGlobalStore();
const deleteFollowDialogFlag = ref(false);
const deleteDynamicDialogFlag = ref(false);

onMounted(()=>{

  let upVideo=props.dynamic.video;
  if(upVideo){
    store.selectUpVideo.upVideo=upVideo;
  }

})

//打开我的主页
function openHome(menu,id){
  window.open(
  `./home?homeMenu=${menu}&userId=${id}`,
  "_blank",
);
}

// 关注
async function followF(user){

if(store.userId===null)
{
    store.setLoginDialogVisibleFlag(1);   
    return;
}

if(store.userId===user.id){
    ElMessage({
        message: "不能关注自己(ง •̀_•́)ง",
        type: "info",
        plain: true,
        duration: 1700,
    });
    return;
}
await addFollow(store.token,store.userId,user.id).then(res=>{
    if(res.data.code===1){
        user.isFollowFlag=1;
        user.fansNumber++;
        ElMessage({
        message: "关注成功(ง •̀_•́)ง",
        type: "info",
        plain: true,
        duration: 1700,
      });
    }else{
        ElMessage({
        message: "关注失败(ง •̀_•́)ง",
        type: "info",
        plain: true,
        duration: 1700,
        });
    }
})
}

// 取消关注
async function unFollowF(user){

if(store.userId===null)
{
    store.setLoginDialogVisibleFlag(1);   
    return;
}

await deleteFollow(store.token,store.userId,user.id).then(res=>{
    if(res.data.code===1){
        user.isFollowFlag=0;
        ElMessage({
        message: "取消了对 "+user.userName+" 的关注",
        type: "info",
        plain: true,
        duration: 1700,
      });
      user.fansNumber--; 
      deleteFollowDialogFlag.value=false;
    }else{
        ElMessage({
        message: "取消关注失败(ง •̀_•́)ง",
        type: "info",
        plain: true,
        duration: 1700,
        });
    }
})
}

//查询是否关注
let selectFollowDelay=true;
function selectFollowFlag(user){
    if(!store.userId||!selectFollowDelay)
     return;
     selectFollowDelay=false;
     checkFollow(store.userId,user.id,store.token).then(res=>{
        if(res.data.code===1){
            user.isFollowFlag=res.data.data;
        }
     })
}

let selectFollowDelayTimer=null;
function selectFollowDelayF(){
    clearTimeout(selectFollowDelayTimer);
    selectFollowDelayTimer=setTimeout(()=>{
        selectFollowDelay=true;
    },500)
}

//打开视频页
function openVideo(videoId){
  window.open("./video?videoId=BV"+videoId,"videoWindow");
}

//修改动态置顶状态
function putUpStatusF(dynamic){
  if(!store.userId)
  {
    store.setLoginDialogVisibleFlag(1);   
    return;
  }

  putUpStatus(dynamic.id,store.userId,store.token).then(res=>{
    if(res.data.code===1){
      dynamic.upFlag=dynamic.upFlag===1?0:1;
      if(dynamic.upFlag===1)
      {
        store.setUpFlag(dynamic.id);
      }
    }else{
      ElMessage({
        message: res.data.msg,
        type: "info",
        plain: true,
        duration: 1700,
        });
    }
  })

}

//删除动态
function deleteDynamicF(dynamic){
  if(!store.userId)
  {
    store.setLoginDialogVisibleFlag(1);   
    return;
  }
  deleteDynamic(dynamic.dynamic.id,store.userId,store.token).then(res=>{
    if(res.data.code===1){
      ElMessage({
        message: "删除成功",
        type: "info",
        plain: true,
        duration: 1700,
      });
      window.location.href="./dynamic";
    }else{
      ElMessage({
        message: "删除失败(ง •̀_•́)ง",
        type: "info",
        plain: true,
        duration: 1700,
      });
    }
  })
}


</script>

<style lang="scss" scoped>

*{
    margin: 0;
    padding: 0;
    box-sizing: border-box;
}

.dynamic{
    width: 100%;
    background-color: white;
    border-radius: 6px;
    font-weight: 400;
    min-width: 556px;
    position: relative;
    margin-bottom: 8px;

        .dynamic-content{
        position: relative;

            .title{
            margin-top: 20px;  
            color: #18191C;
            font-size: 28px;
            font-weight: 700;
            line-height: 1.4;
            }
            .head{
              position: relative;
              .avatar{
              align-items: center;
              display: flex;
              height: 86.4px;
              justify-content: start;
              left: 0;
              position: absolute;
              top: 0;
              width: 48px;

                .avatar-container{
                  width: 48px;
                  height: 48px;
                  border-radius: 50%;
                    img{
                    width: 48px;
                    height: 48px;
                    border-radius: 50%;
                    object-fit: cover;
                    cursor: pointer;
                  }
                    .user-info{
                    left: 50%;  
                    transition: all .3s ease;
                    transition-delay: 0.3s;  
                    opacity: 0;
                    visibility: hidden;
                  }
                }
                .avatar-container:hover{
                  .user-info{
                    transition-delay: 0.3s;
                    opacity: 1;
                    visibility: visible;
                  }
                }
              }
              .user{
              height: 64px;
              padding: 20px 68px;
                        
                .user-name{
                align-items: center;
                display: flex;
                height: 22px;
                margin: 5px 0 2px 0px;
                width: max-content;

                  span{
                    font-size: 17px;
                    line-height: 32px;
                    transition: color .3s ease;
                    color: #18191C;
                    cursor: pointer;
                    font-weight: 600;
                    transition: all .3s ease;
                  }
              }
              .user-name:hover{
                  span{
                      color: #40C5F1;
                  }
              }
              .date{
              align-items: center;
              display: flex;
              padding-right: 120px;
              
                span{
                  color: #9499A0;
                  font-size: 13px;
                  line-height: 18px;
                }
              }
              }
            }
            .body{
            margin-top: 24px;
                .content{
                font-size: 17px;
                line-height: 1.69em;
                word-break: break-word;
                -webkit-font-smoothing: antialiased;
                font-weight: 400;
                line-height: 25px;
                overflow: hidden;
                }
                .img{
                margin-top: 12px;
                min-width: 114px;
                min-height: 111px;
                border-radius: 6px;
                margin-right: 6px;
                margin-bottom: 10px;
                object-fit: cover;
                background-color: #f1f2f3;
                }
                .video-info-container{
                background-color: #F6F7F8;
                border-radius: 6px;
                margin-top: 12px;
                padding: 20px;

                    .up-info{
                      align-items: center;
                      display: flex;
                      height: 20px;
                      justify-content: space-between;
                      margin-bottom: 8px;
                      width: 100%;

                        .left{
                          align-items: center;
                          display: flex;
                          flex: 1;

                            .up-avatar{
                              position: relative;
                              width: 20px;
                              height: 20px;
                              border-radius: 50%;
                              margin-right: 4px;

                                img{
                                width: 20px;
                                height: 20px;
                                border-radius: 50%;
                                object-fit: cover;
                                }
                                .up-user-info{
                                  opacity: 0;
                                  visibility: hidden;
                                  transition: all .3s ease;
                                  transition-delay: 0.3s;
                                  position: absolute;
                                  left: 50%;
                                }
                            }
                            .up-avatar:hover{
                               .up-user-info{
                                   transition-delay: 0.3s;
                                   opacity: 1;
                                   visibility: visible;
                               }
                            }
                            .up-user-name{
                              position: relative;
                              font-size: 14.5px;
                              line-height: 25px;
                              display: inline-block;
                              transition: color .3s ease;
                              color: #61666D;
                              margin-right: 4px;

                                .up-user-info{
                                    opacity: 0;
                                    visibility: hidden;
                                    transition: all .3s ease;
                                    transition-delay: 0.3s;
                                    position: absolute;
                                    left: 50%;
                                  }
                            }
                            .up-user-name:hover{
                              color: #797F87;

                                .up-user-info{
                                   transition-delay: 0.3s;
                                   opacity: 1;
                                   visibility: visible;
                                }
                            }
                            .end{
                              font-size: 14.5px;
                              line-height: 25px;
                              color: #9499A0;
                            }
                        }
                        .right{
                          flex-shrink: 0;
                          div{
                            border-radius: 4px;
                            color: #00AEEC;
                            cursor: pointer;
                            font-size: 12.5px;
                            font-weight: 500;
                            height: 18px;
                            line-height: 18px;
                            position: relative;
                            text-align: center;
                            user-select: none;
                          }
                          .follow:before{
                          background-image: url(data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMTYiIGhlaWdodD0iMTYiIGZpbGw9Im5vbmUiIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyI+PHBhdGggZmlsbC1ydWxlPSJldmVub2RkIiBjbGlwLXJ1bGU9ImV2ZW5vZGQiIGQ9Ik03Ljk5OSAyLjI1YS42LjYgMCAwIDEgLjYuNlY3LjRoNC41NTFhLjYuNiAwIDEgMSAwIDEuMkg4LjU5OXY0LjU1YS42LjYgMCAxIDEtMS4yIDBWOC42SDIuODVhLjYuNiAwIDAgMSAwLTEuMmg0LjU0OVYyLjg1YS42LjYgMCAwIDEgLjYtLjZaIiBmaWxsPSIjMDBBRUVDIi8+PC9zdmc+);
                          background-position: 50%;
                          background-repeat: no-repeat;
                          background-size: cover;
                          content: "";
                          height: 16px;
                          left: -18px;
                          position: absolute;
                          top: 1px;
                          width: 16px;
                          z-index: 1;
                          }
                          .unfollow{
                            color: #9499A0;
                          }
                        }
          
                    }
                    .video-info{
                      margin-top: 12px;
                    }
                }
                .delete-info-container{
                  background-color: #F6F7F8;
                  border-radius: 6px;
                  margin-top: 12px;
                  padding: 8px 12px;
                  
                  .delete-info{
                    align-items: center;
                    color: #9499A0;
                    display: flex;
                    font-size: 13px;
                    line-height: 18px;
                    text-indent: 6px;
                      
                      svg{
                      height: 16px;
                      vertical-align: -2px;
                      width: 16px;
                      }
                    }
                }
            }
            .body2{
            margin-top: 28px;
            }
            .up-btn{
            position: absolute;
            right: 0px;
            border-radius: 4px;
            height: 24px;
            background-color: white;
            width: 24px;
            user-select: none;
            transition: all .3s ease;
            cursor: pointer;

                svg{
                color: #9499A0;
                }
                .up-info{
                        transition-delay: 0.3s;  
                        width: 123px;
                        z-index: 10000;
                        position: absolute;
                        left: -100px;
                        top: 30px;
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
                .up-info2{
                        transition-delay: 0.3s;  
                        width: 123px;
                        height: 100px;
                        z-index: 10000;
                        position: absolute;
                        left: -100px;
                        bottom: -100px;
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
            }
            .up-btn:hover{
            background-color: #F1F2F3;
              
                .up-info{
                    transition-delay: 0.3s;
                    opacity: 1;
                    visibility: visible;
                }
                .up-info2{
                    transition-delay: 0.3s;
                    opacity: 1;
                    visibility: visible;
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
</style>