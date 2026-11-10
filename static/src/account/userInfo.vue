<template>
    <div class="account-user-info">
        <div class="title"><span></span> 我的信息</div>
        <div class="user-info">
            <div class="content">
                <span class="label">昵称:</span>
                <input type="text" maxlength="20" v-model="store.userInformation.userName">
            </div>
            <div class="content">
                <span class="label">我的签名:</span>
                <textarea maxlength="250" placeholder="介绍一下自己，让别人更懂你" v-model="store.userInformation.introduce"/>
            </div>
            <div class="content">
                <span class="label">性别:</span>
                <div class="radio-options">
                    <span class="radio" :class="{'active-radio':radioFlag===1}" @click="radioFlag=1">男</span>
                    <span class="radio" :class="{'active-radio':radioFlag===2}" @click="radioFlag=2">女</span>
                    <span class="radio" :class="{'active-radio':radioFlag===0}" @click="radioFlag=0">保密</span>
                </div>
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
            <div class="content">
                <span class="label">名字颜色:</span>
                <div class="name-color-row">
                    <el-color-picker
                        :model-value="store.userNameColor"
                        :disabled="!store.userNameColorSettable"
                        :predefine="nameColorPresets"
                        @change="changeNameColor"
                    />
                    <span class="color-hint">
                        {{ store.userNameColorSettable ? (store.userNameColor ? '点击色块可修改，清空恢复默认黑色（仅保存在本地）' : '未设置，默认黑色（仅保存在本地）') : `Lv.${store.userNameColorSettableLevel} 解锁` }}
                    </span>
                </div>
            </div>
        </div>
        <div class="btn-container">
             <button v-debounce :disabled="saving" @click="submitChange">{{ saving ? "保存中..." : "保存" }}</button>
        </div>
    </div>
  </template>
  
<script setup>
import { useGlobalStore } from "@/store/store";
import { ref } from "vue";
import { ElMessage } from "element-plus";
import { updateAccount } from "@/api/user/index";

const store = useGlobalStore();
const radioFlag = ref(store.userInformation.gender ?? 0);//0保密 1男 2女

//名字颜色本地预设色板
const nameColorPresets = [
    "#000000", "#F56C6C", "#E6A23C", "#F0A03C", "#67C23A",
    "#0E9C85", "#409EFF", "#8E44AD", "#E05C9B", "#7F8C8D",
];

//名字颜色仅保存在本地，选中即生效；置空恢复默认黑色
function changeNameColor(value) {
    if (!store.userNameColorSettable) return;
    store.setUserNameColor(value && value !== "null" ? value : null);
}

//提交修改
const saving = ref(false);
function formatBirthday(value) {
    if (!value) return null;
    if (typeof value === "string") return value.slice(0, 10);
    const year = value.getFullYear();
    const month = String(value.getMonth() + 1).padStart(2, "0");
    const day = String(value.getDate()).padStart(2, "0");
    return `${year}-${month}-${day}`;
}

async function submitChange() {
    if (saving.value) return;
    const nickname = store.userInformation.userName?.trim();
    if (!nickname) {
        ElMessage.warning("昵称不能为空");
        return;
    }

    saving.value = true;
    try {
        await updateAccount(store.token, {
            nickname,
            signature: store.userInformation.introduce || "",
            gender: radioFlag.value,
            birthday: formatBirthday(store.userInformation.birthday),
        });
        store.userInformation.userName = nickname;
        store.userInformation.gender = radioFlag.value;
        ElMessage.success("修改成功");
    } catch (error) {
        ElMessage.error(error.message || "保存个人信息失败");
    } finally {
        saving.value = false;
    }
}


</script>
  
<style lang="scss" scoped>

*{
    margin: 0;
    padding: 0;
    box-sizing: border-box;
}

.account-user-info{
min-height: 100%;

.title{
display: block;
width: 100%;
min-height: 50px;
line-height: 1.4;
font-size: 14px;
color: #1C2321;
cursor: default;
text-align: left;
padding: 17px 30px;
color: #0E9C85;
border-bottom: 1px solid #ddd;
display: flex;
align-items: center;

    span{
    flex: none;
    width: 4px;
    height: 16px;
    background-color: #0E9C85;
    border-radius: 4px;
    margin-right: 5px;
    }
}
.user-info{
padding: 24px clamp(16px, 4vw, 40px) 0;
position: relative;
    .content{
        margin-bottom: 20px;
        display: grid;
        grid-template-columns: minmax(95px, 115px) minmax(0, 1fr);
        align-items: start;
        gap: 10px 16px;
        .label{
        text-align: right;
        line-height: 30px;
        font-size: 14px;
        color: #606266;
        }
        input{
        width: min(100%, 420px);
        min-width: 0;
        height: 30px;
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
        width: min(100%, 618px);
        min-width: 0;
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
        .radio-options{
        min-width: 0;
        display: flex;
        flex-wrap: wrap;
        gap: 8px 12px;
        }
        .radio{
        font-size: 14px;    
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
        background-color: #1E9E8C !important;
        border-color: #1E9E8C !important;
        color: #fff !important;
        }
        .name-color-row{
        min-width: 0;
        display: flex;
        align-items: center;
        flex-wrap: wrap;
        gap: 8px 12px;
        .color-hint{
            font-size: 12px;
            color: #909399;
            line-height: 1.4;
        }
        }

    }
}
.btn-container{
    display: flex;
    justify-content: center;
    padding: 28px 16px 32px;

    button{
    width: 110px;
    min-height: 40px;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    line-height: 1;
    cursor: pointer;
    border: 1px solid #0E9C85;
    color: #606266;
    text-align: center;
    outline: 0;
    margin: 0;
    transition: .1s;
    font-weight: 500;
    padding: 12px 20px;
    font-size: 14px;
    border-radius: 4px;
    transition: all .3s ease;
    color: #fff;
    background-color: #0E9C85;
    }
    button:hover{
        background-color: #19AC96;
        transition: all 0.3s ease;
}
    button:disabled{
        cursor: wait;
        opacity: .7;
    }
}

}

@media (max-width: 720px){
    .account-user-info{
        .title{
            padding: 15px 18px;
        }

        .user-info{
            padding: 20px 16px 0;

            .content{
                grid-template-columns: minmax(0, 1fr);
                gap: 8px;

                .label{
                    width: auto;
                    text-align: left;
                    line-height: 22px;
                }

                input,
                textarea{
                    width: 100%;
                }
            }
        }
    }
}

@media (max-width: 380px){
    .account-user-info .user-info{
        padding-right: 12px;
        padding-left: 12px;
    }
}

</style>