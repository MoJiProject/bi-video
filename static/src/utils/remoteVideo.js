const MAX_URL_LENGTH = 1000;
const MAX_REMOTE_FRAMES = 30;

function normalizeUrl(url) {
  if (!url) return null;
  let value = String(url).trim().replace(/^[,;.\s]+|[,;.\s]+$/g, "");
  if (!value || value.length > MAX_URL_LENGTH) return null;
  if (value.startsWith("//")) value = "https:" + value;
  const lower = value.toLowerCase();
  if (!lower.startsWith("http://") && !lower.startsWith("https://")) return null;
  if (/\s/.test(value)) return null;
  return value;
}

export function parseRemoteVideoUrl(raw) {
  if (!raw) return null;
  return normalizeUrl(String(raw));
}

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

export function formatSecondsToVideoTime(seconds) {
  if (!Number.isFinite(seconds) || seconds <= 0) return "";
  const minutes = Math.floor(seconds / 60);
  const rest = Math.floor(seconds % 60);
  return `${minutes < 10 ? "0" : ""}${minutes}:${rest < 10 ? "0" : ""}${rest}`;
}

export async function fetchDefaultRemoteCover() {
  const response = await fetch("/img/page-bg-7.png");
  if (!response.ok) throw new Error("读取默认封面失败");
  return await response.blob();
}

export async function normalizeCoverFile(file) {
  if (!["image/webp", "image/bmp", "image/x-ms-bmp"].includes(file.type)) return file;
  const url = URL.createObjectURL(file);
  try {
    const image = new Image();
    await new Promise((resolve, reject) => {
      image.onload = resolve;
      image.onerror = () => reject(new Error("读取封面失败，请重新选择图片"));
      image.src = url;
    });
    const canvas = document.createElement("canvas");
    canvas.width = image.naturalWidth;
    canvas.height = image.naturalHeight;
    const context = canvas.getContext("2d");
    if (!context) throw new Error("当前浏览器不支持封面转换");
    context.fillStyle = "#ffffff";
    context.fillRect(0, 0, canvas.width, canvas.height);
    context.drawImage(image, 0, 0);
    return await new Promise((resolve, reject) => {
      canvas.toBlob(blob => blob ? resolve(blob) : reject(new Error("封面转换失败")), "image/jpeg", 0.92);
    });
  } finally { URL.revokeObjectURL(url); }
}

export function isRemoteVideo(video) {
  return !!video && video.videoSource === 1 && !!video.remoteUrl;
}

function cleanupHls(hls) {
  if (hls) {
    try { hls.destroy(); } catch (e) { void e; }
  }
}

function waitForEvent(target, eventName, timeoutMs = 15000) {
  return new Promise((resolve, reject) => {
    const timer = setTimeout(() => {
      cleanup();
      reject(new Error("timeout"));
    }, timeoutMs);
    const onOk = () => { cleanup(); resolve(); };
    const onErr = () => { cleanup(); reject(new Error("media error")); };
    const cleanup = () => {
      clearTimeout(timer);
      target.removeEventListener(eventName, onOk);
      target.removeEventListener("error", onErr);
    };
    target.addEventListener(eventName, onOk, { once: true });
    target.addEventListener("error", onErr, { once: true });
  });
}

function seekTo(videoEl, time) {
  return new Promise((resolve, reject) => {
    const timer = setTimeout(() => {
      cleanup();
      reject(new Error("seek timeout"));
    }, 10000);
    const onSeeked = () => { cleanup(); resolve(); };
    const onErr = () => { cleanup(); reject(new Error("seek error")); };
    const cleanup = () => {
      clearTimeout(timer);
      videoEl.removeEventListener("seeked", onSeeked);
      videoEl.removeEventListener("error", onErr);
    };
    videoEl.addEventListener("seeked", onSeeked, { once: true });
    videoEl.addEventListener("error", onErr, { once: true });
    try {
      videoEl.currentTime = Math.max(0, time);
    } catch (e) {
      cleanup();
      reject(e);
    }
  });
}

export async function extractRemoteThumbnails(url, onFrame) {
  if (!url) return [];
  const frames = [];
  const videoEl = document.createElement("video");
  videoEl.preload = "auto";
  videoEl.muted = true;
  videoEl.playsInline = true;
  try {
    videoEl.crossOrigin = "anonymous";
  } catch (e) {
    void e;
  }

  let hls = null;
  const isHls = String(url).toLowerCase().includes(".m3u8");
  if (isHls && window.Hls && window.Hls.isSupported()) {
    hls = new window.Hls();
    hls.loadSource(url);
    hls.attachMedia(videoEl);
  } else {
    videoEl.src = url;
  }

  try {
    await waitForEvent(videoEl, "loadedmetadata");
  } catch (e) {
    cleanupHls(hls);
    return frames;
  }

  const duration = Number.isFinite(videoEl.duration) ? videoEl.duration : 0;
  if (duration <= 0) {
    cleanupHls(hls);
    return frames;
  }

  const count = Math.min(MAX_REMOTE_FRAMES, Math.max(8, Math.floor(duration)));
  const step = duration / (count + 1);
  const times = [];
  for (let i = 0; i < count; i++) {
    times.push(Math.min(duration - 0.1, (i + 1) * step));
  }
  if (times.length === 0) times.push(0);

  const canvas = document.createElement("canvas");
  const ctx = canvas.getContext("2d");

  for (const t of times) {
    try {
      await seekTo(videoEl, t);
      canvas.width = videoEl.videoWidth || 480;
      canvas.height = videoEl.videoHeight || 270;
      if (ctx) {
        ctx.drawImage(videoEl, 0, 0, canvas.width, canvas.height);
        const dataUrl = canvas.toDataURL("image/jpeg");
        frames.push(dataUrl);
        if (typeof onFrame === "function") onFrame(dataUrl, frames.length - 1);
      }
    } catch (e) {
      break;
    }
  }

  cleanupHls(hls);
  try {
    videoEl.pause();
    videoEl.removeAttribute("src");
    videoEl.load();
  } catch (e) {
    void e;
  }
  return frames;
}