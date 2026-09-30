<template>
    <div class="eit" ref="eitRef" tabindex="-1" @keydown.stop>
    <div class="eit-title">选择或输入你想@的人</div>

        <div v-show="filteredFriendList.length" class="group-name">我的关注</div>
        <div
            v-for="item in filteredFriendList"
            :key="item.id"
            class="eit-item"
            :class="{ 'eit-item-active': activeIndex === getItemIndex(item) }"
            @click="selectUser(item)"
            @mouseenter="activeIndex = getItemIndex(item)"
        >
            <img class="eit-item-avatar" :src="item.avatarAddress">
            <div class="eit-item-info">
                <div class="eit-item-username" v-html="highlightText(item.userName)"></div>
                <div class="eit-item-fans">{{ item.fansNumber }}粉丝</div>
            </div>
        </div>

        <div v-show="filteredOtherList.length" class="group-name">其他</div>
        <div
            v-for="item in filteredOtherList"
            :key="item.id"
            class="eit-item"
            :class="{ 'eit-item-active': activeIndex === getItemIndex(item) }"
            @click="selectUser(item)"
            @mouseenter="activeIndex = getItemIndex(item)"
        >
            <img class="eit-item-avatar" :src="item.avatarAddress">
            <div class="eit-item-info">
                <div class="eit-item-username" v-html="highlightText(item.userName)"></div>
                <div class="eit-item-fans">{{ item.fansNumber }}粉丝</div>
            </div>
        </div>

        <div v-show="!selectableList.length" class="eit-empty">暂无匹配用户</div>
  </div>
</template>

<script>
import { useGlobalStore } from "../store/store";
import { computed, nextTick, onMounted, onUnmounted, ref, watch } from 'vue';

export default {
    name: 'Eit',
    props: {
        eitList: {
            type: [Object, Array],
            required: true,
    },
        eitKeyWord: {
            type: String,
            required: true,
    },
    },
    setup(props) {
    const store = useGlobalStore();
        const eitRef = ref(null);
        const activeIndex = ref(0);

        const keyword = computed(() => (props.eitKeyWord || '').trim());
        const friendList = computed(() => getList(props.eitList?.friendList));
        const otherList = computed(() => getList(props.eitList?.otherList));
        const filteredFriendList = computed(() => filterList(friendList.value));
        const filteredOtherList = computed(() => keyword.value ? filterList(otherList.value) : []);
        const selectableList = computed(() => [...filteredFriendList.value, ...filteredOtherList.value]);

        watch(selectableList, () => {
            activeIndex.value = 0;
            nextTick(scrollActiveIntoView);
        });

        onMounted(() => {
            window.addEventListener('keydown', handleKeydown, true);
        });

        onUnmounted(() => {
            window.removeEventListener('keydown', handleKeydown, true);
        });

        function getList(list) {
            return Array.isArray(list) ? list : [];
        }

        function filterList(list) {
            if (!keyword.value) return list;
            return list.filter(item => item.userName?.includes(keyword.value));
        }

        function selectUser(item) {
            if (!item) return;
            store.setEitUserName(item.userName);
            store.setEitUserId(item.id);
        }

        function getItemIndex(item) {
            return selectableList.value.findIndex(user => user.id === item.id);
        }

        function isVisible() {
            const el = eitRef.value;
            return !!(el && el.offsetParent !== null);
        }

        function handleKeydown(event) {
            if (!isVisible() || !selectableList.value.length) return;

            if (event.key === 'ArrowDown') {
                event.preventDefault();
                event.stopPropagation();
                activeIndex.value = (activeIndex.value + 1) % selectableList.value.length;
                nextTick(scrollActiveIntoView);
                return;
            }

            if (event.key === 'ArrowUp') {
                event.preventDefault();
                event.stopPropagation();
                activeIndex.value = (activeIndex.value - 1 + selectableList.value.length) % selectableList.value.length;
                nextTick(scrollActiveIntoView);
                return;
            }

            if (event.key === 'Enter') {
                event.preventDefault();
                event.stopPropagation();
                selectUser(selectableList.value[activeIndex.value]);
            }
        }

        function scrollActiveIntoView() {
            const el = eitRef.value;
            if (!el) return;
            const item = el.querySelector('.eit-item-active');
            if (item) item.scrollIntoView({ block: 'nearest' });
        }

        function escapeRegExp(text) {
            return text.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
        }

        function escapeHtml(text) {
            return String(text)
                .replace(/&/g, '&amp;')
                .replace(/</g, '&lt;')
                .replace(/>/g, '&gt;')
                .replace(/"/g, '&quot;')
                .replace(/'/g, '&#39;');
        }

        function highlightText(text) {
            const escapedText = escapeHtml(text || '');
            if (!keyword.value) return escapedText;
            const regex = new RegExp(`(${escapeRegExp(keyword.value)})`, 'g');
            return escapedText.replace(regex, '<span style="color: #00aeec;">$1</span>');
    }

        return {
            eitRef,
            activeIndex,
            filteredFriendList,
            filteredOtherList,
            selectableList,
            getItemIndex,
            selectUser,
            highlightText,
            store,
        };
    },
}
</script>

<style lang="scss" scoped>
* {
    margin: 0;
    padding: 0;
    box-sizing: border-box;
}

.eit {
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

    .group-name {
    width: 100%;
    height: 18px;
    padding: 0px 12px;
    color: #61666d;
    font-size: 12px;
    font-weight: 500;
    }

    .eit-item {
    position: relative;
    display: flex;
    flex-direction: column;
    height: 52px;
    cursor: pointer;
    margin-bottom: 10px;

        .eit-item-avatar {
            left: 10px;
            position: absolute;
            width: 36px;
            height: 36px;
            border-radius: 50%;
            pointer-events: none;
            top: 50%;
            transform: translateY(-50%);
    }

        .eit-item-info {
            position: relative;
            display: flex;
            flex-direction: column;
            padding: 12px 0;
            left: 60px;

            .eit-item-username {
        width: 130px;
        display: -webkit-box;
        -webkit-box-orient: vertical;
        overflow: hidden;
        color: #18191c;
                -webkit-line-clamp: 1;
        line-clamp: 1;
        text-overflow: ellipsis;
        word-wrap: break-word;
            }

            .eit-item-fans {
                color: #9499a0;
            }
    }
    }

    .eit-item:hover,
    .eit-item-active {
        background-color: #f1f2f3;
    }
}

.eit-empty {
    position: absolute;
    inset: 50% 0 auto;
    transform: translateY(-50%);
    text-align: center;
    color: #9499a0;
}

.eit::-webkit-scrollbar {
    width: 3.5px; /* 滚动条的宽度 */
    border-radius: 30px; /* 滚动条滑块的圆角 */
}

.eit::-webkit-scrollbar-thumb {
    background: #bcbcbc; /* 滚动条的滑块 */
    height: 10px;
    border-radius: 10px;
}
</style>