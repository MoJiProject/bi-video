import { ref } from 'vue';
import { ElMessage } from 'element-plus';

// 一起看的 WebSocket 连接只由 WatchTogetherPanel 持有，页面里的其他组件
// （相关推荐、自动连播列表等）通过这里请求切换房间视频，避免各自再连一条连接。
const bridge = ref(null);

export function registerWatchTogetherBridge(api) {
  bridge.value = api;
}

export function unregisterWatchTogetherBridge(api) {
  if (bridge.value === api) bridge.value = null;
}

export function useWatchTogetherBridge() {
  return bridge;
}

// 列表里点视频时的统一跳转：房内且有管理权限就切房间视频，让所有人一起跳；
// 否则按普通跳转处理，避免普通成员被房间视频弹回原视频。
export function navigateToVideo(video) {
  const videoId = video?.videoId;
  if (!videoId) return;
  const api = bridge.value;
  if (api?.inRoom()) {
    if (api.canSwitch() && videoId !== api.currentVideoId()) {
      api.switchVideo({ videoId, videoTitle: video.videoTitle });
      return;
    }
    const message = '一起看中只有房主或管理员可以切换房间视频，已单独打开该视频';
    // 全屏时面板在原生全屏节点内，提示要挂到面板里才能看见。
    api.notify ? api.notify('info', message) : ElMessage({ type: 'info', message });
    return;
  }
  window.location.href = `./video?videoId=BV${videoId}`;
}