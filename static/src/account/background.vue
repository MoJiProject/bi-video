<template>
<div class="account-user-background" v-loading.fullscreen="loading" element-loading-background="rgba(122, 122, 122, 0)">
    <div class="title"><span></span> 我的背景</div>
    <div class="background-container">
        <div class="title2">当前背景</div>
        <div v-if="!store.userInformation.backgroundAddress&&!backgroundAddress" class="default-background" :style="{backgroundImage:'url(../img/主页背景图.webp)'}"></div>
        <div v-else :style="{backgroundImage: `url(${backgroundAddress||store.userInformation.backgroundAddress})` }" class="default-background"></div>
        <div v-show="backgroundAddress" class="reset-img" @click="resetImg">
                <svg t="1710752071995" class="icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="4266" width="14" height="14"><path d="M264.26481778 266.05795555c130.55317333-130.54407111 337.08259555-139.81923555 478.53112889-27.97112888l-83.01226667 0.08192 0.08192 81.92 181.4528-0.17294222a40.96 40.96 0 0 0 40.91448889-40.31374223l2.82168889-177.41141333-81.91089778-1.30161778-1.26520889 79.62624c-173.71591111-144.33393778-432.74695111-135.17710222-595.54019556 27.62524444-83.95889778 83.95889778-129.98883555 195.57034667-129.63384888 314.30883556l81.92-0.24576c-0.30037333-96.75662222 37.22808889-187.72423111 105.64039111-256.14563556zM865.37557333 501.55064889c0.30037333 96.76572445-37.21898667 187.72423111-105.64039111 256.14563556-68.12103111 68.12103111-158.69724445 105.64949333-255.04426667 105.64949333-83.79505778 0-163.20284445-28.40803555-227.2915911-80.61838223l87.6179911-0.08192-0.08192-81.92-181.4528 0.17294223a40.96 40.96 0 0 0-40.91448888 40.31374222l-2.83079112 177.41141333 81.91089778 1.30161778 1.21059556-75.97624889c79.15292445 65.59971555 177.72999111 101.32593778 281.82300444 101.32593778 118.22876445 0 229.376-46.03904 312.97080889-129.63384889 83.95889778-83.95889778 129.99793778-195.57944889 129.63384889-314.31793778l-81.91089778 0.22755556zM223.82364445 783.51018667l0.00910222-0.74638222h0.72817778l-0.73728 0.74638222z" p-id="4267"></path></svg>
                <span>重新选择</span>
        </div>
        <div class="info">请选择图片上传：大小800 * 100像素支持JPG、PNG等格式，图片需小于10M</div>
        <div v-show="!backgroundAddress" class="change-background" @click="openFile">更换背景</div>
        <div v-show="backgroundAddress" class="change-background" @click="updateBackground">更新</div>
        <input ref="fileInput" type="file" style="display: none;" @change="changeBackground"  accept="image/jpeg,image/png,image/webp">
    </div>
</div>
</template>

<script setup>
import { ref } from 'vue';
import { useGlobalStore } from '../store/store';
import { ElMessage } from "element-plus";
import { uploadBackground } from '@/api/user/index';

const store = useGlobalStore();
const fileInput = ref(null);
const backgroundAddress = ref(null);
const loading = ref(false);

//更换背景
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
    if(file.size>10*1024*1024)
    {
        ElMessage({
            message: '图片大小不能超过10M',
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
        backgroundAddress.value = e.target.result;
    };
    event.target.value='';
}

//打开文件选择框
function openFile() {
fileInput.value.click();
}

//更新背景
function updateBackground() {

    if(!backgroundAddress.value)
    {
        ElMessage({
            message: '请选择图片',
            type: "info",
            plain: true,
            duration: 1700,
        });
        return;
    }
    loading.value = true;
    uploadBackground(store.token,store.userId,backgroundAddress.value).then(res => {
        if(res.data.code===1)
        {
            store.userInformation.backgroundAddress = backgroundAddress.value;
            backgroundAddress.value = null;
            ElMessage({
                message: res.data.data,
                type: "info",
                plain: true,
                duration: 1700,
            });
        }
        else
        {
            ElMessage({
                message: res.data.msg,
                type: "info",
                plain: true,
                duration: 1700,
            });
        }
        loading.value = false;
    })
}

//重置背景
function resetImg() {
    backgroundAddress.value = null;
    openFile();
}

</script>

<style lang="scss" scoped>

*{
    margin: 0;
    padding: 0;
    box-sizing: border-box;
}

.account-user-background{

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

    span{
    float: left;
    width: 4px;
    height: 16px;
    margin-top: 18px;
    background-color: #00a1d6;
    border-radius: 4px;
    margin-right: 5px;
    }
}
.background-container{
    padding: 50px 20px;

    .title2{
    font-size: 14px;
    color: #8c959c;
    }
    .default-background{
    margin-top: 20px;    
    width: 100%;
    height: 150px;
    background-size: cover;
    background-position: 50% 67%;
    background-repeat: no-repeat;
    }
}
.reset-img {
position: relative;
margin-top: 15px;
display: flex;
justify-content: center;
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
.info{
margin-top: 25px;    
text-align: center;  
line-height: 16px;
color: #8c959c;
font-size: 12px;
}
.change-background{
position: relative;
margin-top: 38px;
left: 50%;
transform: translateX(-50%);
width: 140px;
height: 40px;
font-size: 14px;
line-height: 32px;
border-radius: 4px;
display: flex;
justify-content: center;
align-items: center;
border: 1px solid #00a1d6;
transition: all 0.3s ease;
color: #fff;
background-color: #00a1d6;
cursor: pointer;
}
.change-background:hover{
background-color: #00b5e5;
border-color: #00b5e5;}

}

</style>