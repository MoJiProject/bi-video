<template>
  <div class="img-dynamic" @click="openDynamic(props.dynamic.id)">
      <div class="img-container">
        <img class="dynamic-img" :src="props.dynamic.imgAddress.split(',')[0]">
        <div class="overlay">
                <div>
                    <img src="../img/点赞白.png"><span>{{ dynamic.likeNumber }}</span>
                </div>
        </div>
      </div>
      <div class="introduce" v-html="props.dynamic.content"></div>
  </div> 
</template>

<script setup>
const props=defineProps({
  dynamic: {
    type: Object,
    required: true,
  },
});

//打开动态详情页
function openDynamic(dynamicId){
  window.open(`./dynamicDetail?dynamicId=${dynamicId}`,"dynamicWindow");
}

</script>

<style lang="scss" scoped>

*{
    margin: 0;
    padding: 0;
    box-sizing: border-box;
}


.img-dynamic{
    position: relative;
    display: inline-block;
    width: 214.8px;
    margin-bottom: 24px;
    cursor: pointer;

    .img-container{
    position: relative;
    min-height: 161.84px;
    max-height: 281.73px;
        .dynamic-img{
            position: relative;
            display: flex;
            justify-content: flex-end;
            user-select: none;
            width: 100%;
            min-height: 161.84px;
            max-height: 281.73px;
            object-fit: cover;
            border-radius: 6px;
        }
        .overlay{
            position: absolute;
            user-select: none;
            bottom: 0;
            left: 0;
            z-index: 2;
            box-sizing: border-box;
            padding: 14px 8px 4px;
            width: 100%;
            border-bottom-right-radius: 6px;
            border-bottom-left-radius: 6px;
            background-image: linear-gradient(180deg, #0000, #000c);
            color: #fff;
            display: flex;
            align-items: center;
            justify-content: space-between;
            pointer-events: none;

            div{
            position: relative;
            top: 2px;
            width: 100%;
            margin-right: 12px;
            font-size: 13px;
            height: 18px;
            flex-shrink: 0;

                img{
                    width: 17px;
                    height: 16px;
                    transform: rotate(-1deg);
                }
                span{
                    position: relative;
                    left: 2px;
                    top: -2px;
                }
            }
        }
    }
    .introduce{
    position: relative;
    top: 6px; /* 距离底部 14px */
    font-size: 14px;
    color: #18191C;
    transition: color .3s ease;
    display: -webkit-box;
    -webkit-box-orient: vertical;
    overflow: hidden;
    -webkit-line-clamp: 2;
    text-overflow: ellipsis;
    }
    .introduce:hover{
        color: #00AEEC;
    }
}

</style>