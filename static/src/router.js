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
        component: () => import('./contribute/EditContribute.vue'),
      },
      {
        path: '/contribute/subpage1',
        name:'subpage1',
        component: () => import('./contribute/Page1.vue'),
      },
      {
        path: '/contribute/subpage2',
        name:'subpage2',
        component: () => import('./contribute/Page2.vue'),
      },
      {
        path: '/contribute/subpage3',
        name:'subpage3',
        component: () => import('./contribute/Page3.vue'),
      },
      {
        path: '/contribute/subpage4',
        name:'subpage4',
        component: () => import('./contribute/Page4.vue'),
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
      // 稍后再看
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
});


export default router;
