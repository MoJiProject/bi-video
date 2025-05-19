import { createApp } from 'vue';
import App from '../src/App.vue';
import ElementPlus from 'element-plus';
import 'element-plus/dist/index.css';
import { createPinia } from 'pinia';
import global from "./assets/css/global.css"
import router from './router';
import ExternalLinkDirective from './directives/external-link.js'//这是点击链接新增网页页面的自定义指令

createApp(App)
.use(ElementPlus)
.use(createPinia())
.use(router)
.directive('external-link', ExternalLinkDirective)
.mount('#app')
