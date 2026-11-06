import { ElMessage } from "element-plus";

function getFullscreenPlayer() {
  const fullscreenElement =
    document.fullscreenElement ||
    document.webkitFullscreenElement ||
    document.msFullscreenElement;

  return fullscreenElement?.matches?.(".up-video-container")
    ? fullscreenElement
    : null;
}

function showVideoMessage(options = {}, type) {
  const normalizedOptions =
    typeof options === "string" ? { message: options } : { ...options };
  const fullscreenPlayer = getFullscreenPlayer();

  return ElMessage({
    ...normalizedOptions,
    type: type || normalizedOptions.type,
    appendTo: normalizedOptions.appendTo || fullscreenPlayer || document.body,
    zIndex: Math.max(normalizedOptions.zIndex || 0, 4000),
  });
}

export const videoMessage = Object.assign(
  (options) => showVideoMessage(options),
  {
    success: (options) => showVideoMessage(options, "success"),
    warning: (options) => showVideoMessage(options, "warning"),
    error: (options) => showVideoMessage(options, "error"),
    info: (options) => showVideoMessage(options, "info"),
  },
);
