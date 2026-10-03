import { ElMessage, ElMessageBox } from "element-plus";

//统一的提示，直接用 Element Plus 自带样式
export function toast(message, type = "info") {
  //只展示字符串，非字符串或空文案不弹框，避免出现空白提示
  const text = typeof message === "string" ? message.trim() : "";
  if (!text) return;
  ElMessage({
    message: text,
    type,
    duration: 1700,
  });
}

/**
 * 取后端返回的提示文案。
 * R 只有 success(Object) 没有 success(String) 重载，
 * 所以 R.success("操作成功") 实际是把文案放进 data 而不是 msg；
 * 而 R.success(Map) 时 data 是对象，不能直接当文案展示。
 * 这里只接受非空字符串，取不到就返回兜底文案，保证不会弹空框。
 */
export function msgOf(res, fallback = "操作成功") {
  const body = (res && res.data) || {};
  const candidates = [body.msg, body.data];
  for (const value of candidates) {
    if (typeof value === "string" && value.trim()) return value;
  }
  return fallback;
}

//危险操作二次确认
export function confirmDanger(message, confirmText = "确定") {
  return ElMessageBox.confirm(message, "操作确认", {
    confirmButtonText: confirmText,
    cancelButtonText: "取消",
    type: "warning",
  });
}

//抛错占位，保持与原实现一致
export function throwIfFailed(res) {
  if (!res || res.code !== 1) {
    toast((res && res.msg) || "未知错误");
    return new Error((res && res.msg) || "未知错误");
  }
  return null;
}

//日期只保留到分钟，列表里更好读
export function shortTime(value) {
  if (!value) return "-";
  return String(value).replace("T", " ").slice(0, 16);
}

//数字超过一万显示为 x.x万
export function compactNumber(value) {
  if (value === null || value === undefined) return "-";
  const number = Number(value);
  if (Number.isNaN(number)) return "-";
  if (number >= 10000) return (number / 10000).toFixed(1).replace(/\.0$/, "") + "万";
  return String(number);
}

//跳转用户主页
export function goUserHome(userId) {
  if (!userId) return;
  window.open(`/home?homeMenu=1&userId=${userId}`, "_blank");
}

//跳转视频详情
export function goVideoDetail(videoId) {
  if (!videoId) return;
  window.open(`/video?videoId=${videoId}`, "_blank");
}

//翻页后把滚动位置带回顶部
export function scrollToTop() {
  window.scrollTo({ top: 0, behavior: "smooth" });
}

//模块中文名，列表里直接展示
export const MODULE_LABEL = {
  user: "用户管理",
  video: "视频管理",
  comment: "评论管理",
  dynamic: "动态管理",
  message: "私信管理",
  keyWord: "搜索热词",
  recycleBin: "回收站",
};

//操作动作中文名
export const ACTION_LABEL = {
  putAdmin: "设置管理员",
  banUser: "封禁用户",
  unbanUser: "解除封禁",
  examineVideo: "审核通过",
  rejectVideo: "审核退回",
  takeDownVideo: "强制下架",
  deleteVideo: "删除视频",
  deleteComment: "删除评论",
  deleteDynamic: "删除动态",
  deleteMessage: "删除私信",
  addKeyWord: "新增搜索词",
  putKeyWord: "修改搜索词",
  putKeyWordCount: "修改搜索次数",
  deleteKeyWord: "删除搜索词",
  recycleVideo: "视频移入回收站",
  restoreVideo: "还原视频",
  recycleComment: "评论移入回收站",
  restoreComment: "还原评论",
  recycleDynamic: "动态移入回收站",
  restoreDynamic: "还原动态",
  purgeRecycleBin: "彻底清除",
  cleanRestoredRecycleBin: "清理已还原记录",
};

//视频状态中文名与配色
export const VIDEO_STATUS = [
  { value: -1, label: "全部", type: "" },
  { value: 0, label: "待审核", type: "warning" },
  { value: 1, label: "已通过", type: "success" },
  { value: 2, label: "未通过/已下架", type: "danger" },
];

export function videoStatusType(status) {
  const item = VIDEO_STATUS.find((s) => s.value === status);
  return item ? item.type : "grey";
}

export function videoStatusLabel(status) {
  const item = VIDEO_STATUS.find((s) => s.value === status);
  return item ? item.label : "未知";
}

//截断长文本，表格里避免换行撑开
export function ellipsis(value, max = 40) {
  if (!value) return "-";
  const text = String(value);
  return text.length > max ? text.slice(0, max) + "…" : text;
}
