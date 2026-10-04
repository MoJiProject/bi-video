<template>
    <div class="loading-indicator" :class="{inline:inline}" :style="{minHeight:minHeight}">
        <img class="loading-icon" src="/img/loading.png" alt=""/>
        <span v-if="text">{{ text }}</span>
    </div>
</template>

<script setup>
defineProps({
    text: { type: String, default: "拼命加载中..." },
    minHeight: { type: String, default: "120px" },
    inline: { type: Boolean, default: false },
});
</script>

<style lang="scss" scoped>
.loading-indicator{
    width: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    padding: 60px 0;
    color: #5C6664;

    &.inline{
        padding: 16px 0;
    }

    .loading-icon{
        width: 30px;
        height: 30px;
        flex-shrink: 0;
    }

    span{
        font-size: 14px;
        line-height: 20px;
        position: relative;
        top: 1px;
    }
}
</style>
