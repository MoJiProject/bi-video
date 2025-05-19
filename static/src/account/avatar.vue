<template>
<div class="account-user-avatar">
    <div class="title" :class="{active: uploadFlag}"><span v-show="!uploadFlag" class="icon"></span> <span class="avatar" @click="uploadFlag=false">我的头像</span> <span v-show="uploadFlag" class="change-avatar"> > 更换头像</span></div>
    <div class="avatar-container" v-show="!uploadFlag">
        <div class="content">
                <img :src="store.userInformation.avatarAddress">
                <div class="change-avatar" @click="uploadFlag=true"><span>更换头像</span></div>
                <div class="remove-avatar"><span>卸下挂件</span></div>
        </div>
    </div>
    <div class="avatar-container2" v-show="uploadFlag&&!avatarAddress">
         <div class="btn">
            <div @click="openFile">
                <img src="../img/local_icon.png">
                <span>选择本地图片</span>
            </div>
         </div>
         <div class="border"></div>
         <div class="avatar-container">
            <img :src="store.userInformation.avatarAddress">
            <div>当前头像</div>
         </div>
         <input ref="fileInput" type="file" style="display: none;" @change="changeBackground"  accept="image/jpeg,image/png,image/webp">
    </div>
    <div class="avatar-container3" v-if="uploadFlag&&avatarAddress">
         <!-- 裁剪器 -->
         <div class="cropper">
            <CropperDefault
            :imgUrl="avatarAddress"
            :wrapWidth="wrapWidth"
            :wrapHeight="wrapHeight"
            v-model:prevWidth="prevWidth"
            v-model:prevHeight="prevHeight"
            v-model:prevOffsetX="prevOffsetX"
            v-model:prevOffsetY="prevOffsetY"
            v-model:cutOrder="cutOrder"
            @cut="send"
            ></CropperDefault>
            <div class="reset-img" @click="resetImg">
                <svg t="1710752071995" class="icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="4266" width="14" height="14"><path d="M264.26481778 266.05795555c130.55317333-130.54407111 337.08259555-139.81923555 478.53112889-27.97112888l-83.01226667 0.08192 0.08192 81.92 181.4528-0.17294222a40.96 40.96 0 0 0 40.91448889-40.31374223l2.82168889-177.41141333-81.91089778-1.30161778-1.26520889 79.62624c-173.71591111-144.33393778-432.74695111-135.17710222-595.54019556 27.62524444-83.95889778 83.95889778-129.98883555 195.57034667-129.63384888 314.30883556l81.92-0.24576c-0.30037333-96.75662222 37.22808889-187.72423111 105.64039111-256.14563556zM865.37557333 501.55064889c0.30037333 96.76572445-37.21898667 187.72423111-105.64039111 256.14563556-68.12103111 68.12103111-158.69724445 105.64949333-255.04426667 105.64949333-83.79505778 0-163.20284445-28.40803555-227.2915911-80.61838223l87.6179911-0.08192-0.08192-81.92-181.4528 0.17294223a40.96 40.96 0 0 0-40.91448888 40.31374222l-2.83079112 177.41141333 81.91089778 1.30161778 1.21059556-75.97624889c79.15292445 65.59971555 177.72999111 101.32593778 281.82300444 101.32593778 118.22876445 0 229.376-46.03904 312.97080889-129.63384889 83.95889778-83.95889778 129.99793778-195.57944889 129.63384889-314.31793778l-81.91089778 0.22755556zM223.82364445 783.51018667l0.00910222-0.74638222h0.72817778l-0.73728 0.74638222z" p-id="4267"></path></svg>
                <span>重新选择</span>
            </div>
         </div> 
         <div class="border-line"></div>
         <!-- 预览 -->
         <div class="img-preview-wrap">
            <div class="pre-container" :style="`width: ${wrapWidth}px; height: ${wrapHeight}px;`">
                <img :src="avatarAddress"
                    :style="`width: ${prevWidth}px;
                            height: ${prevHeight}px;
                            transform: translateX(-${prevOffsetX}px) translateY(-${prevOffsetY}px);`">
            </div>
            <div class="pre-info">预览头像</div>
         </div>
    </div>
    <div v-show="uploadFlag" class="info">请选择图片上传：大小180 * 180像素支持JPG、PNG等格式，图片需小于5M</div>
    <div v-show="uploadFlag" class="update-btn" :class="{'active-update-btn': avatarAddress}" @click="cutOrder=true">更新</div>
</div>
</template>

<script setup>
import { ref } from "vue";
import { ElMessage } from "element-plus";
import { useGlobalStore } from "../store/store";
import CropperDefault from "@/utils/CropperDefault.vue";
import { uploadAvatar } from "@/api/user";

const store = useGlobalStore();
const avatarAddress = ref(null);
const fileInput = ref(null);
const uploadFlag = ref(false);
const wrapWidth = ref(96);
const wrapHeight = ref(96);
const prevWidth = ref(96);
const prevHeight = ref(96);
const prevOffsetX = ref(0);
const prevOffsetY = ref(0);
const cutOrder = ref(false);

//打开文件选择框
function openFile() {
    fileInput.value.click();
}

//更换头像
function changeBackground(event) {
    const file = event.target.files[0];
    if(!file)
        return;
    if(!file.type.startsWith("image/"))
    {
        ElMessage({
            message: '请上传图片',
            type: "info",
            plain: true,
            duration: 1700,
            });
        event.target.value='';    
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
        event.target.value='';    
        return;
    }
    const reader = new FileReader();
    reader.readAsDataURL(file);
    reader.onload = (e) => {
        avatarAddress.value = e.target.result;
    };
    event.target.value='';
}

//重新选择
function resetImg() {
    avatarAddress.value = null;
    fileInput.value.click();
}

//发送裁剪图片
function send(img) {

    if(!img)
        return;

    uploadAvatar(store.token,store.userId,img).then(res => {
        if(res.data.code === 1) {
            ElMessage({
                message: '头像更新成功',
                type: "info",
                plain: true,
                duration: 1700,
            });
            store.userInformation.avatarAddress = img;
            uploadFlag.value = false;
            avatarAddress.value = null;
            cutOrder.value = false;
        } else {
            ElMessage({
                message: '头像更新失败',
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

.account-user-avatar{

.title{
display: block;
width: 100%;
height: 50px;
line-height: 50px;
font-size: 14px;
color: #181818;
cursor: default;
text-align: left;
padding-left: 30px;
color: #00a1d6;
border-bottom: 1px solid #ddd;

    .icon{
    float: left;
    width: 4px;
    height: 16px;
    margin-top: 18px;
    background-color: #00a1d6;
    border-radius: 4px;
    margin-right: 5px;
    }
}
.active{
margin-left: -10px;

    .avatar{
    cursor: pointer;
    color: #6d757a;
    transition: all 0.3s ease;
    }
    .avatar:hover{
    color: #00a1d6;
    }
    .change-avatar{
    color: #222;
    }
}
.avatar-container{
padding: 20px 0; 

    .content{
    width: 200px;
    height: 200px;
    border: 1px solid #abaeaf;
    border-radius: 50%;
    position: relative;
    margin: 0 auto;

        img{
        width: 96px;
        height: 96px;
        border-radius: 50%;
        position: absolute;
        top: 50%;
        left: 50%;
        transform: translate(-50%, -50%);
        }
        .change-avatar{
        width: 54px;
        height: 54px;
        border-radius: 50%;
        background: #00a1d6;
        border: 1px solid #00a1d6;
        position: absolute;
        left: -27px;
        user-select: none;
        cursor: pointer;
        top: 51%;
        transform: translateY(-50%);

            span{
            width: 2em;
            font-size: 14px;
            line-height: 18px;
            position: absolute;
            color: white;
            top: 50%;
            left: 50%;
            transform: translate(-50%, -50%); 
            }
        }
        .remove-avatar{
        width: 54px;
        height: 54px;
        border-radius: 50%;
        background: #fff;
        border: 1px solid #ccd0d7;
        position: absolute;
        right: -27px;
        user-select: none;
        cursor: pointer;
        top: 51%;
        transform: translateY(-50%);
        cursor: not-allowed;

            span{
            width: 2em;
            font-size: 14px;
            line-height: 18px;
            position: absolute;
            color: #eee;
            top: 50%;
            left: 50%;
            transform: translate(-50%, -50%); 
            }
        }
    }
}
.avatar-container2{
padding: 150px 230px;
display: flex;
height: 182px;
justify-content: space-around;
align-items: center;

   .btn{
    float: left;
    margin-right: 40px;
     
        div{
        cursor: pointer;
        background: #f1f2f5;
        width: 178px;
        height: 84px;
        top: 0;
        left: 0;
        border: 1px solid #e5e9ef;
        border-radius: 4px;
        transition: all .6s ease;
        display: flex;
        align-items: center;
        padding-left: 10px;

            img{
            width: 36px;
            height: 36px;
            }
            span{
            display: block;
            font-family: MicrosoftYaHei;
            font-size: 14px;
            color: #5a6267;
            line-height: 20px;
            letter-spacing: 0;
            margin-left: 6px;
            }
        }
        div:hover{
        background: #e5e9ef;
        }
   }
   .border{
    height: 182px;
    width: 1px;
    background: #abaeaf;
    float: left;
   }
   .avatar-container{
    margin-left: 40px;
    float: left;
    text-align: center;

        img{
        width: 96px;
        height: 96px;
        overflow: hidden;
        border-radius: 50%;
        border: 1px solid #e6eaf0;
        background-size: cover;
        }
        div{
        margin-top: 20px;
        font-size: 12px;
        color: #99a2aa;
        }
   }
}
.info{
margin-top: 25px;    
text-align: center;  
line-height: 16px;
color: #8c959c;
font-size: 12px;
}
.update-btn{
position: relative;
margin-top: 38px;
left: 50%;
transform: translateX(-50%);
width: 140px;
height: 40px;
font-size: 14px;
line-height: 32px;
border-radius: 4px;
background: #f4f5f7;
border-color: #f4f5f7;
display: flex;
justify-content: center;
align-items: center;
cursor: not-allowed;
color: #ccd0d7;
}
.active-update-btn{
border: 1px solid #00a1d6;
transition: all 0.3s ease;
color: #fff;
background-color: #00a1d6;
cursor: pointer;
}
.active-update-btn:hover{
background-color: #00b5e5;
border-color: #00b5e5;
}
.avatar-container3{
padding: 61.5px 20px 56px;
position: relative;
top: 24px;
display: flex;
justify-content: center;
align-items: center;

    .cropper{
    height: 182px;
    width: 182px;
    position: relative;
    top: -2px;
    margin-right: 40px;
    z-index: 1000;
    }
    .reset-img {
    position: absolute;
    left: 50%;
    bottom: -25px;
    transform: translate(-50%);
    display: flex;
    font-size: 12px;
    transition: all 0.3s ease;
    color: #6d757a;
    fill: #6d757a;
    cursor: pointer;
    }
    .reset-img:hover {
        color: #00b5e5;
        fill: #00b5e5;
    }
    .reset-img svg {
        margin-right: 6px;
    }
    .border-line {
    height: 182px;
    width: 1px;
    background: #abaeaf;
    float: left;
    }
    .img-preview-wrap {
    margin-left: 40px;
    }
    .pre-container {
    overflow: hidden;
    border-radius: 50%;
    border: 1px solid #e6eaf0;
    position: relative;
    box-sizing: initial;
    background: #f1f2f5;
    }
    .pre-info {
    margin-top: 20px;
    font-size: 12px;
    color: #949697;
    text-align: center;
    }   
}

}

</style>