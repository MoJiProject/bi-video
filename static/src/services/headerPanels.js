import { socialApi, userContentApi, videoApi } from '../api/product';

/**
 * 页头下拉面板的数据适配层。
 *
 * 页头模板目前用的是老字段名（collectName / watchVideoDate / videoId…），
 * 这里把新接口的数据转成模板要的形状，模板本身不用动。
 * 等页头按新结构重写之后，这个文件连同它做的映射一起删掉。
 */

function dayLabel(iso) {
  if (!iso) return '更早';
  const d = new Date(iso);
  const today = new Date();
  const same = (a, b) => a.toDateString() === b.toDateString();
  if (same(d, today)) return '今天';
  const yesterday = new Date(today.getTime() - 86400000);
  if (same(d, yesterday)) return '昨天';
  return '更早';
}

/** 收藏夹列表：老字段 collectName / collectNumber -> name / itemCount。 */
export async function fetchFolders() {
  const list = await userContentApi.folders();
  return (list || []).map((f) => ({
    id: f.id,
    collectName: f.name,
    collectNumber: f.itemCount ?? 0,
    isDefault: !!f.isDefault,
  }));
}

/** 收藏夹里的视频：老字段 videoId / videoTitle / videoCoverAddress… -> 新字段。 */
export async function fetchFolderVideos(folderId) {
  const page = await userContentApi.folderVideos({ folderId, pageNum: 1, pageSize: 10 });
  const rows = page?.records || [];
  return rows.map((v) => ({
    ...v,
    videoId: v.id,
    videoTitle: v.title,
    videoCoverAddress: v.coverUrl,
    videoDuration: formatDuration(v.durationSeconds),
    videoPlayNumber: v.playCount,
    videoDanmakuNumber: v.danmakuCount,
  }));
}

export async function fetchWatchLater() {
  const page = await userContentApi.watchLater({ size: 10 });
  return (page?.records || []).map((v) => ({
    ...v,
    videoId: v.id,
    videoTitle: v.title,
    videoCoverAddress: v.coverUrl,
    videoDuration: formatDuration(v.durationSeconds),
    videoPlayNumber: v.playCount,
    watchWatchDate: '今天',
  }));
}

/**
 * 观看记录：老接口按「今天/昨天/更早」分组，这里补上 watchVideoDate 字段，
 * 分组逻辑仍由调用方做。
 */
export async function fetchHistory() {
  const page = await userContentApi.history({ size: 20 });
  return (page?.records || []).map((v) => ({
    ...v,
    videoId: v.id,
    videoTitle: v.title,
    videoCoverAddress: v.coverUrl,
    videoDuration: formatDuration(v.durationSeconds),
    videoPlayNumber: v.playCount,
    watchVideoDate: '更早',
    watchedAt: v.publishedAt,
    dayLabel: dayLabel(v.publishedAt),
  }));
}

/** 动态：新接口返回的是平铺列表，按作者分组成老面板要的形状。 */
export async function fetchDynamicPanels() {
  const page = await socialApi.posts({ size: 30 });
  const rows = page?.records || [];
  const groups = new Map();
  for (const p of rows) {
    const key = p.authorId;
    if (!groups.has(key)) {
      groups.set(key, {
        userId: p.authorId,
        userName: p.authorNickname,
        userAvatar: p.authorAvatar,
        videoNumber: 0,
        newDynamicNumber: 0,
        videos: [],
      });
    }
    const g = groups.get(key);
    g.videos.push({
      ...p,
      videoId: p.videoId,
      videoTitle: p.videoTitle,
      videoCoverAddress: p.videoCover,
      watchWatch: 0,
    });
  }
  return [...groups.values()].map((g) => ({
    ...g,
    videoNumber: g.videos.length,
    newDynamicNumber: g.videos.length,
  }));
}

export function toggleWatchLater(videoId) {
  return videoApi.watchLater(videoId);
}

function formatDuration(seconds) {
  const total = Number(seconds) || 0;
  const m = Math.floor(total / 60);
  const s = total % 60;
  return `${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`;
}