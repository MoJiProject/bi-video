const MAX_URL_LENGTH = 1000;

function normalizeUrl(url) {
  if (!url) return null;
  //去掉首尾空白以及复制链接时可能带进来的逗号、分号、句号
  let value = String(url).trim().replace(/^[,;.\s]+|[,;.\s]+$/g, "");
  if (!value || value.length > MAX_URL_LENGTH) return null;
  if (value.startsWith("//")) value = "https:" + value;
  const lower = value.toLowerCase();
  if (!lower.startsWith("http://") && !lower.startsWith("https://")) return null;
  if (/\s/.test(value)) return null;
  return value;
}

/**
 * 解析远程视频直链，用户过期后可在编辑页手动更换
 */
export function parseRemoteVideoUrl(raw) {
  if (!raw) return null;
  return normalizeUrl(String(raw));
}

/**
 * 读取远程视频时长，单位秒
 *
 * 注意不能设crossOrigin="anonymous"，否则浏览器会按CORS模式请求，
 * 源站不回Access-Control-Allow-Origin时请求被直接拦掉，metadata都读不到。
 * <video>跨域读metadata本身不需要CORS协议。
 *
 * @returns {Promise<number>} 取不到时返回0
 */
export function readRemoteDuration(url) {
  if (!url) return Promise.resolve(0);
  return new Promise((resolve) => {
    const video = document.createElement("video");
    video.preload = "metadata";
    video.muted = true;
    video.playsInline = true;

    let settled = false;
    const finish = (seconds) => {
      if (settled) return;
      settled = true;
      clearTimeout(timer);
      video.removeAttribute("src");
      video.load();
      resolve(seconds);
    };
    const timer = setTimeout(() => finish(0), 15000);

    video.addEventListener("loadedmetadata", () => {
      finish(
        Number.isFinite(video.duration) && video.duration > 0 ? video.duration : 0
      );
    });
    video.addEventListener("error", () => finish(0));
    video.src = url;
  });
}

/**
 * 秒数转成"mm:ss"
 */
export function formatSecondsToVideoTime(seconds) {
  if (!Number.isFinite(seconds) || seconds <= 0) return "";
  const minutes = Math.floor(seconds / 60);
  const rest = Math.floor(seconds % 60);
  return `${minutes < 10 ? "0" : ""}${minutes}:${rest < 10 ? "0" : ""}${rest}`;
}

/**
 * 远程视频取不到首帧时使用的默认封面
 */
export async function fetchDefaultRemoteCover() {
  const response = await fetch("/img/pageBg7.webp");
  return await response.blob();
}

export function isRemoteVideo(video) {
  return !!video && video.videoSource === 1 && !!video.remoteUrl;
}
