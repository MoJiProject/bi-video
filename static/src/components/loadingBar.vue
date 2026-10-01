<template>
    <div v-show="visible" class="loading-bar" :class="{fading:fading}">
        <div class="loading-bar-inner" :style="{width:width+'%'}"></div>
    </div>
</template>

<script setup>
import { onBeforeUnmount, ref, watch } from 'vue';

const props = defineProps({
    active: { type: Boolean, default: false },
    percent: { type: Number, default: null },
    minDuration: { type: Number, default: 400 },
    holdDuration: { type: Number, default: 160 },
    fadeDuration: { type: Number, default: 220 },
    timeout: { type: Number, default: 15000 },
});

const visible = ref(false);
const fading = ref(false);
const width = ref(0);

const CREEP_MAX = 92;
const CREEP_TIME = 3200;
const FOLLOW_STEP = 0.16;
const FOLLOW_MIN = 0.35;

let frameId = null;
let completeTimer = null;
let hideTimer = null;
let startTime = 0;
let target = 0;
let finished = false;
let running = false;

function creepTarget(elapsed){
    if(props.percent!==null)
        return Math.min(Math.max(props.percent,0),CREEP_MAX);
    return CREEP_MAX*(1-Math.exp(-elapsed/CREEP_TIME));
}

function loop(){
    const elapsed = performance.now()-startTime;
    let goal = finished?100:creepTarget(elapsed);
    if(goal>target)
        target=goal;

    const gap = target-width.value;
    if(gap>0.2)
        width.value+=Math.max(gap*FOLLOW_STEP,FOLLOW_MIN);
    else if(finished)
        width.value=target;

    if(finished&&width.value>=99.9){
        hide();
        return;
    }

    if(!finished&&elapsed>props.timeout)
        finished=true;

    frameId=requestAnimationFrame(loop);
}

function clearTimers(){
    if(completeTimer!==null){
        clearTimeout(completeTimer);
        completeTimer=null;
    }
    if(hideTimer!==null){
        clearTimeout(hideTimer);
        hideTimer=null;
    }
}

function stopLoop(){
    if(frameId!==null){
        cancelAnimationFrame(frameId);
        frameId=null;
    }
}

function start(){
    clearTimers();
    stopLoop();
    finished=false;
    target=0;
    width.value=0;
    startTime=performance.now();
    visible.value=true;
    fading.value=false;
    running=true;
    frameId=requestAnimationFrame(loop);
}

function complete(){
    if(!running||finished||completeTimer!==null)
        return;
    const wait=Math.max(0,props.minDuration-(performance.now()-startTime));
    completeTimer=setTimeout(()=>{
        completeTimer=null;
        finished=true;
        if(frameId===null){
            running=true;
            frameId=requestAnimationFrame(loop);
        }
    },wait);
}

function hide(){
    stopLoop();
    running=false;
    hideTimer=setTimeout(()=>{
        hideTimer=null;
        fading.value=true;
        hideTimer=setTimeout(()=>{
            hideTimer=null;
            visible.value=false;
            fading.value=false;
            width.value=0;
            target=0;
            finished=false;
        },props.fadeDuration);
    },props.holdDuration);
}

watch(()=>props.active,(value)=>{
    value?start():complete();
},{immediate:true});

onBeforeUnmount(()=>{
    clearTimers();
    stopLoop();
});
</script>

<style lang="scss" scoped>
.loading-bar{
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    height: 3px;
    z-index: 10001;
    pointer-events: none;
    opacity: 1;
    transition: opacity .22s ease;
    background-color: rgba(0,174,236,.12);

    &.fading{
        opacity: 0;
    }

    .loading-bar-inner{
        height: 100%;
        border-radius: 0 3px 3px 0;
        background: linear-gradient(90deg,#00AEEC,#40C5F1);
        box-shadow: 0 0 6px rgba(0,174,236,.55);
        transition: width .12s linear;
    }
}
</style>
