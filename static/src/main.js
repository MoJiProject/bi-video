import { createApp } from "vue";
import App from "../src/App.vue";
import AppEmptyState from "./components/AppEmptyState.vue";
import ElementPlus from "element-plus";
import zhCn from "element-plus/es/locale/lang/zh-cn";
import "element-plus/dist/index.css";
import { createPinia } from "pinia";
import "./assets/css/global.css";
import router from "./router";
import ExternalLinkDirective from "./directives/external-link.js"; //这是点击链接新增网页页面的自定义指令
import buttonDebounce from "./directives/buttonDebounce";
import { offlinePreviewEnabled } from "./preview/config";
import http from "./services/http";
import { useGlobalStore } from "./store/store";

async function mountApp() {
  const pinia = createPinia();
  const app = createApp(App);
  app.config.globalProperties.$offlinePreview = offlinePreviewEnabled;
  if (offlinePreviewEnabled) {
    const [
      { previewAdapter },
      { profiles },
      { mapUser },
      { default: toolbar },
    ] = await Promise.all([
      import("./preview/adapter"),
      import("./preview/fixtures"),
      import("./api/home/user"),
      import("./preview/PreviewToolbar.vue"),
    ]);
    http.defaults.adapter = previewAdapter;
    useGlobalStore(pinia).setUserInformation(mapUser(profiles[0]));
    app.component("OfflinePreviewToolbar", toolbar);
  }
  app
    .use(ElementPlus, { locale: zhCn })
    .use(pinia)
    .use(router)
    .component("AppEmptyState", AppEmptyState)
    .directive("debounce", buttonDebounce)
    .directive("external-link", ExternalLinkDirective)
    .mount("#app");
}

mountApp().catch((error) => {
  console.error("Application startup failed:", error);
  document.getElementById("app").textContent =
    "页面初始化失败，请查看控制台后刷新重试。";
});
