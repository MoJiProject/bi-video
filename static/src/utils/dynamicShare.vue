<template>
  <div class="dynamic-share-info" @click.stop>
    <span class="title">分享动态</span>
    <div v-if="shareUserList.length===0" class="not-share-container">
      <span v-if="loadingRecipients">正在加载分享对象...</span>
      <span v-else-if="recipientError">{{ recipientError }} <button @click="selectFollowAndFansF">重试</button></span>
      <span v-else-if="!store.token">请先登录后选择分享对象</span>
      <span v-else>您还没有互关好友，快去寻找吧！</span>
    </div>
    <div v-else-if="shareUserList.length>0" class="share-container" ref="scrollContainer" @wheel.prevent="handleWheel">
      <div class="share-item" v-for="user in shareUserList" :key="user.id" @click="clickUser(user)">
        <img class="avatar" :src="user.avatarAddress">
        <img class="check" v-show="user?.checkFlag" src="/img/选中.png">
        <span style="color: #1C2321;">{{ user.userName }}</span>
      </div>
    </div>
    <div class="line"></div>
    <div class="share-input-row">
      <textarea class="share-textarea" placeholder="请输入分享内容" maxlength="500" :disabled="sending" v-model="shareContent"></textarea>
      <img class="cover" :src="coverUrl" />
    </div>
    <button v-debounce v-if="shareUserList.length>0&&checkSelectUser()" class="share-btn" :disabled="sending" @click="shareDynamicF">{{ sending ? '发送中' : '发送' }}</button>
    <button v-debounce v-else class="not-share-btn">发送</button>
  </div>
</template>

<script>
import { reactive, ref, watch } from "vue";
import { useGlobalStore } from "../store/store";
import { get, post } from '../services/http';
import { ElMessage } from "element-plus";

export default {
  name: "dynamicShare",
  props: {
    dynamic: { type: Object, required: false },
    openFlag: { type: Boolean, required: false },
  },
  setup(props) {
    const scrollContainer = ref(null);
    const shareContent = ref('');
    const store = useGlobalStore();
    const shareUserList = reactive([]);
    const recipientCursor = ref(null);
    const sending = ref(false);
    const loadingRecipients = ref(false);
    const recipientError = ref('');
    let scrolledFlag = true;
    let recipientGeneration = 0;
    let recipientOwner = store.userId;

    function handleWheel(e) {
      if (!scrollContainer.value) return;
      e.preventDefault();
      const delta = Math.abs(e.deltaX) > 10 ? e.deltaX : e.deltaY;
      scrollContainer.value.scrollLeft = Math.max(0, Math.min(
        scrollContainer.value.scrollLeft + delta,
        scrollContainer.value.scrollWidth - scrollContainer.value.clientWidth
      ));
      if (scrolledFlag && scrollContainer.value.scrollWidth - scrollContainer.value.scrollLeft <= scrollContainer.value.clientWidth + 1)
        selectFollowAndFansF();
    }

    async function selectFollowAndFansF() {
      if (!store.token) { store.loginDialogVisible = true; return; }
      if (loadingRecipients.value || !scrolledFlag) return;
      loadingRecipients.value = true;
      recipientError.value = '';
      const generation = recipientGeneration;
      try {
        const page = await get('/share/recipients', { cursorId: recipientCursor.value });
        if (generation !== recipientGeneration) return;
        const ids = new Set(shareUserList.map(u => u.id));
        shareUserList.push(...page.records.filter(u => !ids.has(u.id)));
        recipientCursor.value = page.cursorId;
        scrolledFlag = page.hasMore;
      } catch (error) {
        if (generation === recipientGeneration) {
          recipientError.value = error.message || "加载分享对象失败";
          ElMessage.error(recipientError.value);
        }
      } finally {
        if (generation === recipientGeneration) loadingRecipients.value = false;
      }
    }

    watch(() => [props.openFlag, store.userId, store.token], () => {
      if (recipientOwner !== store.userId) {
        recipientOwner = store.userId;
        recipientGeneration++;
        shareUserList.length = 0;
        recipientCursor.value = null;
        scrolledFlag = true;
        loadingRecipients.value = false;
        recipientError.value = '';
      }
      if (props.openFlag && !shareUserList.length) selectFollowAndFansF();
    }, { immediate: true });

    function checkSelectUser() {
      return shareUserList.some(item => item.checkFlag);
    }

    function clickUser(user) {
      if (sending.value) return;
      if (shareUserList.filter(item => item.checkFlag).length >= 10 && !user.checkFlag) {
        ElMessage.warning("最多只能选择10个用户");
        return;
      }
      user.checkFlag = !user.checkFlag;
    }

    const coverUrl = ref('');
    watch(() => props.dynamic, (d) => {
      coverUrl.value = d?.imageList?.[0] || d?.videoCover || d?.authorAvatar || '/img/cover-fallback.png';
    }, { immediate: true });

    async function shareDynamicF() {
      if (sending.value) return;
      const selected = shareUserList.filter(item => item.checkFlag);
      const userIds = selected.map(item => item.id);
      const postId = props.dynamic?.id;
      if (!postId || !userIds.length) { ElMessage.warning("请选择分享对象"); return; }
      sending.value = true;
      try {
        const result = await post(`/post/${postId}/share`, { content: shareContent.value, userIdList: userIds });
        ElMessage.success("分享成功");
        shareContent.value = '';
        if (props.dynamic) props.dynamic.shareCount = result.shareCount;
        shareUserList.forEach(item => item.checkFlag = false);
      } catch (error) {
        ElMessage.error(error.message || "分享失败");
      } finally {
        sending.value = false;
      }
    }

    return {
      scrollContainer,
      shareContent,
      handleWheel,
      store,
      shareUserList,
      checkSelectUser,
      clickUser,
      shareDynamicF,
      sending,
      loadingRecipients,
      recipientError,
      selectFollowAndFansF,
      coverUrl,
    };
  }
};
</script>

<style lang="scss" scoped>
.dynamic-share-info {
  width: 420px;
  background-color: #fff;
  padding: 12px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.15);
  display: flex;
  flex-direction: column;
  gap: 10px;

  .title { font-size: 16px; font-weight: 600; color: #1C2321; }

  .not-share-container { text-align: center; color: #999; padding: 12px 0; }

  .share-container { display: flex; gap: 12px; overflow-x: auto; padding: 8px 0; }
  .share-item { position: relative; display: flex; flex-direction: column; align-items: center; cursor: pointer; }
  .share-item .avatar { width: 48px; height: 48px; border-radius: 50%; }
  .share-item .check { position: absolute; right: 0; bottom: 20px; width: 16px; height: 16px; border-radius: 50%; background: #fff; }
  .share-item span { font-size: 12px; margin-top: 4px; max-width: 60px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }

  .line { height: 1px; background: #e0e5e3; }

  .share-input-row { display: flex; align-items: flex-start; gap: 10px; } .share-textarea { border: none; resize: none; outline: none; padding-top: 5px; font-size: 14px; width: 80%; height: 70px; } .cover { width: 87px; height: 70px; border-radius: 4px; object-fit: cover; }

  

  .share-btn, .not-share-btn { width: 100%; height: 32px; border: none; border-radius: 4px; color: #fff; cursor: pointer; }
  .share-btn { background: #0fa68e; }
  .share-btn:hover { background: #00b8f6; }
  .not-share-btn { background: #e0e5e3; cursor: not-allowed; }
}
</style>

