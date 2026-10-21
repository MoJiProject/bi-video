export const PREVIEW_USER_ID = 1001;
export const PREVIEW_TOKEN = "offline-preview-not-a-real-token";
const PREVIEW_KEY = "bi_video_offline_preview";

function resolvePreviewMode() {
  if (process.env.NODE_ENV !== "development") return false;
  const choice = new URLSearchParams(window.location.search).get("preview");
  try {
    if (choice === "1") localStorage.setItem(PREVIEW_KEY, "1");
    if (choice === "0") localStorage.removeItem(PREVIEW_KEY);
    return (
      choice === "1" ||
      (choice !== "0" && localStorage.getItem(PREVIEW_KEY) === "1")
    );
  } catch (error) {
    console.warn("Offline preview preference could not be stored:", error);
    return choice === "1";
  }
}

export const offlinePreviewEnabled = resolvePreviewMode();
