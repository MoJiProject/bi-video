<template>
    <div class="account-user-info">
        <div class="title"><span></span> 我的信息</div>
        <div class="user-info">
            <div class="content">
                <span class="label">昵称:</span>
                <input type="text" maxlength="10" :disabled="true" v-model="store.userInformation.userName">
            </div>
            <div class="content">
                <span class="label">修改密码:</span>
                <input type="password" maxlength="16" placeholder="请输入旧密码" v-model="password">
                <input type="password" maxlength="16" placeholder="请输入新密码" v-model="newPassword">
            </div>
            <div class="content">
                <span class="label">手机号:</span>
                <input type="text" @input="formatPhone" maxlength="11" placeholder="请输入手机号" v-model="store.userInformation.phone"/>
            </div>
            <div class="content">
                <span class="label">我的签名:</span>
                <textarea maxlength="250" placeholder="设置您的签名- ( ゜- ゜)つロ" v-model="store.userInformation.introduce"/>
            </div>
            <div class="content">
                <span class="label">性别:</span>
                <span class="radio" :class="{'active-radio':radioFlag===1}" @click="radioFlag=1">男</span>
                <span class="radio" :class="{'active-radio':radioFlag===2}" @click="radioFlag=2">女</span>
                <span class="radio" :class="{'active-radio':radioFlag===0}" @click="radioFlag=0">保密</span>
            </div>
            <div class="content">
                <span class="label">出生日期:</span>
                <el-date-picker
                class="date-picker"
                v-model="store.userInformation.birthday"
                type="date"
                placeholder="请选择日期"
                />
            </div>
        </div>
        <div class="btn-container">
             <button @click="submitChange">保存</button>
        </div>
    </div>
  </template>
  
<script setup>
import { useGlobalStore } from "@/store/store";
import { ref, watch } from "vue";
import { ElMessage } from "element-plus";
import { putUserInfo } from "@/api/user/index";

const store = useGlobalStore();
const password = ref("");
const newPassword = ref("");
const radioFlag = ref(store.userInformation.gender);//0保密 1男 2女

watch(() => store.userInformation.userName, () => {
    store.userInformation.userName=store.userInformation.userName.trim();
})

watch(password, () => {
    password.value=password.value.trim();
})

watch(newPassword, () => {
    newPassword.value=newPassword.value.trim();
})

//检验手机号
function formatPhone(event) {
  const rawValue = event.target.value;
  const numericValue = rawValue.replace(/\D/g, ''); // 只保留数字
  store.userInformation.phone = numericValue;
}


//提交修改
let submitFlag = true;
function submitChange() {
    if(!submitFlag){
        ElMessage({
        message: "提交频繁，请稍后再试",
        type: "info",
        duration: 1700,
        });
        return;
    }
    submitFlag = false;
    if (store.userInformation.userName.trim() === "") {
        ElMessage({
        message: "昵称不能为空",
        type: "info",
        plain: true,
        duration: 1700,
        });
        return;
    }
    if (password.value.trim() !== "" && newPassword.value.trim() === "") {
        ElMessage({
        message: "请输入新密码",
        type: "info",
        plain: true,
        duration: 1700,
        });
        return;
    }
    if (password.value.trim() !== "" && newPassword.value.trim() !== "") {
        if (password.value.trim() === newPassword.value.trim()) {
            ElMessage({
            message: "新密码不能与旧密码相同",
            type: "info",
            plain: true,
            duration: 1700,
            });
            return;
        }
        if (password.value.trim().length < 4 || newPassword.value.trim().length < 4) {
            ElMessage({
            message: "密码长度不能小于4位",
            type: "info",
            plain: true,
            duration: 1700,
            });
            return;
        }
    }
    if (store.userInformation.phone.trim() === "") {
        ElMessage({
        message: "手机号不能为空",
        type: "info",
        plain: true,
        duration: 1700,
        });
        return;
    }
    if(store.userInformation.phone.trim().length!==11){
        ElMessage({
        message: "手机号不足11位",
        type: "info",
        plain: true,
        duration: 1700,
        });
        return;
    }
    store.userInformation.gender = radioFlag.value;
    const date = new Date(store.userInformation.birthday);
    const y = date.getFullYear();
    const m = String(date.getMonth() + 1).padStart(2, '0');
    const d = String(date.getDate()).padStart(2, '0');
    store.userInformation.birthday = `${y}-${m}-${d}`;
    store.userInformation.password1 = password.value.trim();
    store.userInformation.newPassword = newPassword.value.trim();
    store.userInformation.control=1;

    putUserInfo(store.token, store.userInformation).then(res => {
        if (res.data.code === 1) {
            ElMessage({
            message: "修改成功",
            type: "info",
            duration: 1700,
            });
        }else{
            ElMessage({
            message: res.data.msg,
            type: "info",
            duration: 1700,
            });
        }
    });
    setTimeout(() => {
       submitFlag = true;
    }, 5000);
}


</script>
  
<style lang="scss" scoped>

*{
    margin: 0;
    padding: 0;
    box-sizing: border-box;
}

.account-user-info{

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
.user-info{
padding: 20px 20px 0;
position: relative;
    .content{
        margin-bottom: 22px;
        .label{
        width: 95px;
        float: left;
        text-align: right;
        margin-right: 20px;
        line-height: 30px;
        font-size: 14px;
        color: #606266;
        }
        input{
        width: 225px;
        height: 30px;
        margin-right: 40px;
        background-color: #fff;
        background-image: none;
        border-radius: 4px;
        border: 1px solid #dcdfe6;
        box-sizing: border-box;
        color: #606266;
        display: inline-block;
        line-height: 40px;
        outline: 0;
        padding: 0 15px;
        transition: border-color .3s ease;
        }
        input:focus{
        border-color: #409eff;
        }
        textarea{
        width: 618px;
        height: 88px;
        resize: none;
        display: block;
        padding: 5px 15px;
        line-height: 1.5;
        color: #606266;
        outline: none;
        background-color: #fff;
        background-image: none;
        border: 1px solid #dcdfe6;
        border-radius: 4px;
        transition: border-color .3s ease;
        }
        textarea:focus{
        border-color: #409eff;
        }
        .label2{
        text-align: right;
        margin-right: 20px;
        line-height: 30px;
        font-size: 14px;
        color: #606266;
        }
        .radio{
        font-size: 14px;    
        margin-right: 20px;
        padding: 5px 8px;
        color: #717171;
        border: 1px solid #bfcbd9;
        border-radius: 5px !important;
        background: #f4f4f4;
        text-align: center;
        cursor: pointer;
        transition: all .3s ease;
        }
        .active-radio{
        background-color: #22a1d6 !important;
        border-color: #22a1d6 !important;
        color: #fff !important;
        }

    }
}
.btn-container{

    button{
    width: 110px;
    display: inline-block;
    line-height: 1;
    cursor: pointer;
    border: 1px solid #00a1d6;
    color: #606266;
    text-align: center;
    outline: 0;
    margin: 0;
    transition: .1s;
    font-weight: 500;
    padding: 12px 20px;
    font-size: 14px;
    border-radius: 4px;
    position: absolute;
    transition: all .3s ease;
    top: 90%;
    left: 50%;
    color: #fff;
    transform: translate(-50%, -50%);
    background-color: #00a1d6;
    }
    button:hover{
        background-color: #00b5e5;
    }
}



}


</style>