<template>
  <div class="eit">
    <div class="eit-title">选择或输入你想@的人</div>
        <div v-show="friendListFlag||!propss.eitKeyWord" class="group-name">我的关注</div>
        <div @click="store.setEitUserName(item.userName),store.setEitUserId(item.id)" v-show="friendListFlag||!propss.eitKeyWord" class="eit-item" v-for="item in newEitList.friendList||eitList.friendList" :key="item">
            <img class="eit-item-avatar" :src="item.avatarAddress">
          <div class="eit-item-info">
             <div class="eit-item-username" v-html="highlightText(item.userName)"></div>
             <div class="eit-item-fans">{{ item.fansNumber }}粉丝</div>
          </div>
     </div>
        <div v-show="otherListFlag" class="group-name">其他</div>
        <div @click="store.setEitUserName(item.userName),store.setEitUserId(item.id)" v-show="otherListFlag" class="eit-item" v-for="item in newEitList.otherList" :key="item">
                <img class="eit-item-avatar" :src="item.avatarAddress">
            <div class="eit-item-info">
                <div class="eit-item-username" v-html="highlightText(item.userName)"></div>
                <div class="eit-item-fans">{{ item.fansNumber }}粉丝</div>
            </div>
        </div>
  </div>
</template>

<script>
import {useGlobalStore} from "../store/store";
import { reactive, ref, watch } from 'vue';
export default {
   name: 'Eit',
   props: {

    eitList:{
        type: Object,
        reqiured: true,
    },
    eitKeyWord:{
        type: String,
        reqiured: true,
    }

   },
   setup(props){
   
    const store = useGlobalStore();
    const eitList = reactive(props.eitList);
    const propss=reactive(props);
    const newEitList = reactive({});
    const friendListFlag=ref(false);
    const otherListFlag=ref(false);

    watch(propss,()=>{
        
        newEitList.friendList = eitList.friendList.filter(item => item.userName.includes(propss.eitKeyWord));
        newEitList.otherList = eitList.otherList.filter(item => item.userName.includes(propss.eitKeyWord));

        if(eitList.friendList.filter(item => item.userName.includes(propss.eitKeyWord)).length>0)
        friendListFlag.value=true;
        else
        friendListFlag.value=false; 

        if(eitList.otherList.filter(item => item.userName.includes(propss.eitKeyWord)).length>0&&propss.eitKeyWord.length>0)
        otherListFlag.value=true;
        else
        otherListFlag.value=false; 

    },{deep:true})


    function highlightText(text) {
        const regex = new RegExp(`(${propss.eitKeyWord})`, "g");
      return text.replace(regex, '<span style="color: #00aeec;" class="eit-item-username">$1</span>');
    }



    return{
        newEitList,
        propss,
        friendListFlag,
        otherListFlag,
        highlightText,
        store,

    }

   }
}
</script>

<style lang="scss" scoped>

*{
    margin: 0;
    padding: 0;
    box-sizing: border-box;
}

.eit{
    position: relative;
    top: -100px;
    width: 221px;
    height: 325px;
    background-color: white;
    border: 1px solid #e3e5e7;
    border-radius: 6px;
    font-size: 12px;
    box-shadow: rgba(0, 0, 0, 0.08) 0px 2px 10px;
    z-index: 10000;
    overflow: hidden;
    overflow-y: scroll;
    padding-bottom: 120px;

    .eit-title {
    width: 100%;
    height: 41px;
    padding: 12px;
    color: #61666d;
    font-size: 12px;
    font-weight: 500;
    }

    .group-name{    
    width: 100%;
    height: 18px;
    padding: 0px 12px;
    color: #61666d;
    font-size: 12px;
    font-weight: 500;
    }

    .eit-item{
    position: relative;
    display: flex;
    flex-direction: column;
    height: 52px;
    cursor: pointer;
    margin-bottom: 10px;
    .eit-item-avatar{
    left: 10px;    
    position: absolute;       
    width: 36px;
    height: 36px;  
    border-radius: 50%;  
    pointer-events: none;
    top: 50%;
    transform: translateY(-50%);
    }

    .eit-item-info{
        position: relative;
        display: flex;
        flex-direction: column;
        padding: 12px 0;
        left: 60px;

        .eit-item-username{
        width: 130px;
        display: -webkit-box;
        -webkit-box-orient: vertical;
        overflow: hidden;
        color: #18191c;
        -webkit-line-clamp: 1; 
        text-overflow: ellipsis;
        word-wrap: break-word;
       
        }
        .eit-item-fans{
            color: #9499a0;
        }
    }
    }

    .eit-item:hover{
        background-color: #f1f2f3;
    }
}

.eit::-webkit-scrollbar 
{
width: 3.5px; /* 滚动条的宽度 */
border-radius: 30px; /* 滚动条滑块的圆角 */
}

.eit::-webkit-scrollbar-thumb 
{
background: #bcbcbc; /* 滚动条的滑块 */
height: 10px;
border-radius: 10px;
}

</style>