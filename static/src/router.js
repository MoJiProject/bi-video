import { createRouter, createWebHistory } from 'vue-router';

// 路由配置
const routes = [
      // 首页
      {
        path: '/',
        name: 'index',
        component: () => import('./components/index.vue'),
      },
      // 视频
      {
        path: '/video',
        name: 'video',
        component: () => import('./video/videoPage.vue'),
      },
      // 搜索
      {
        path: '/search',
        name:'search',
        component: () => import('./search/mainSearch.vue'),
      },
      // 上传
      {
        path: '/contribute',
        name: 'contribute',
        component: () => import('./contribute/Contribute.vue'),
      },
      {
        path: '/contribute/edit',
        name: 'edit',
        component: () => import('./contribute/Contribute.vue'),
      },
      {
        path: '/contribute/subpage1',
        name:'subpage1',
        component: () => import('./contribute/Contribute.vue'),
      },
      {
        path: '/contribute/subpage2',
        name:'subpage2',
        component: () => import('./contribute/Contribute.vue'),
      },
      // 系统管理后台
      {
        path: '/systemManagement',
        redirect: '/systemManagement/overview',
      },
      {
        path: '/systemManagement/overview',
        name: 'systemOverview',
        component: () => import('./systemManagement/SystemManagement.vue'),
      },
      {
        path: '/systemManagement/user',
        name: 'systemUser',
        component: () => import('./systemManagement/SystemManagement.vue'),
      },
      {
        path: '/systemManagement/video',
        name: 'systemVideo',
        component: () => import('./systemManagement/SystemManagement.vue'),
      },
      {
        path: '/systemManagement/comment',
        name: 'systemComment',
        component: () => import('./systemManagement/SystemManagement.vue'),
      },
      {
        path: '/systemManagement/dynamic',
        name: 'systemDynamic',
        component: () => import('./systemManagement/SystemManagement.vue'),
      },
      {
        path: '/systemManagement/message',
        name: 'systemMessage',
        component: () => import('./systemManagement/SystemManagement.vue'),
      },
      {
        path: '/systemManagement/keyWord',
        name: 'systemKeyWord',
        component: () => import('./systemManagement/SystemManagement.vue'),
      },
      {
        path: '/systemManagement/log',
        name: 'systemLog',
        component: () => import('./systemManagement/SystemManagement.vue'),
      },
      // 消息
      {
        path: '/message',
        newPage: 'message',
        component: () => import('./message/messagePage.vue'),
      },
      // 主页
      {
        path: '/home',
        name: 'home',
        component: () => import('./home/homePage.vue'),
      },
      // 用户信息
      {
        path: '/account',
        name: 'account',
        component: () => import('./account/accountPage.vue'),
      },
      // 历史记录
      {
        path: '/history',
        name: 'history',
        component: () => import('./history/historyPage.vue'),
      },
      // 待看清单
      {
        path: '/waitWatch',
        name: 'waitWatch',
        component: () => import('./waitWatch/waitWatchPage.vue'),
      },
      // 动态
      {
        path: '/dynamic',
        name: 'dynamic',
        component: () => import('./dynamic/dynamicPage.vue'),
      },
      // 动态详情
      {
        path: '/dynamicDetail',
        name: 'dynamicDetail',
        component: () => import('./dynamic/dynamicDetailPage.vue'),
      },
      // 视频404页面
      {
        path: '/videoNotFound',
        name: 'videoNotFound',
        component: () => import('./utils/videoNotFound.vue'),
      },
      // 动态404页面
      {
        path: '/dynamicNotFound',
        name: 'dynamicNotFound',
        component: () => import('./utils/dynamicNotFound.vue'),
      },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
  //导航前就把页面瞬间归位。
  //放在 beforeEach 而不是 scrollBehavior，是因为 beforeEach 触发时
  //旧组件还没卸载、文档高度没有塌陷，此时不会触发浏览器的滚动锚定补偿，
  //也就不会出现「先跳上去又被弹回来」的抖动。
  beforeEach(to, from, next) {
    window.scrollTo(0, 0);
    next();
  },
  scrollBehavior(to, from, savedPosition) {
    //浏览器前进/后退时恢复历史位置，其余情况保持顶部
    return savedPosition || { top: 0, left: 0 };
  },
});


export default router;
