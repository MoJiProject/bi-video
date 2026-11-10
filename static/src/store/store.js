// store.js
import { defineStore } from 'pinia'
import { offlinePreviewEnabled, PREVIEW_USER_ID, PREVIEW_TOKEN } from '../preview/config'

const TOKEN_KEY = 'qingmang_video_token'
const USER_ID_KEY = 'qingmang_video_user_id'
const USER_NAME_COLOR_KEY = 'qingmang_video_user_name_color'
//彩色名字哨兵值：不是真实颜色，仅本地标记，展示端识别后渲染彩虹渐变
const USER_NAME_COLOR_RAINBOW = 'rainbow'

//用户名字颜色，仅本地保存，不上传服务器；离线预览模式下同样可用
function getStoredUserNameColor() {
  if (typeof localStorage === 'undefined') return null
  const raw = localStorage.getItem(USER_NAME_COLOR_KEY)
  if (!raw) return null
  if (raw === USER_NAME_COLOR_RAINBOW) return USER_NAME_COLOR_RAINBOW
  return /^#[0-9a-fA-F]{6}$/.test(raw) ? raw : null
}

function getStoredToken() {
  if (offlinePreviewEnabled) return PREVIEW_TOKEN
  return typeof localStorage === 'undefined' ? null : localStorage.getItem(TOKEN_KEY)
}

function getStoredUserId() {
  if (offlinePreviewEnabled) return PREVIEW_USER_ID
  if (typeof localStorage === 'undefined') return null
  const raw = localStorage.getItem(USER_ID_KEY)
  if (raw === null || raw === '') return null
  const parsed = parseInt(raw, 10)
  return Number.isNaN(parsed) ? null : parsed
}

export const useGlobalStore = defineStore('global', {
  state: () => {
    return {
      searchDisplayFlag: false,
      collectSelect: null,
      placeholderWord: "",
      userIp: null,
      searchDisplayFlag2: false,
      loginDialogVisible: false,
      userId: getStoredUserId(),
      loginLoadFlag: false,
      token: getStoredToken(),
      eitUserName: "", 
      eitUserId: null,
      eitList: [],//eit列表
      reVideoPlayerFlag: false,//回放视频播放器标识
      addFollowFlag: 0,//关注 0没有 1关注 2取消关注
      LTCAFlag: 0,//点赞收藏分享 0没有 1点赞 2收藏
      autoVideoList: [],//自动播放列表 或 收藏夹视频选择列表
      AutoPlayVideoList: false,//是否自动播放列表
      commentNumber: 0,//评论数
      loginDialogVisibleFlag: false,//登录弹窗标识
      userInformation: {},//用户信息
      updateVideoInfoFlag: false,//更新视频信息标识
      upUserId: null,//视频创作者id
      eitUserInfo: null,//eit用户信息
      eitUserInfoHover: false,//eit用户信息hover标识
      messageMenu: 1, //消息菜单 1消息 2回复 3@ 4点赞 5消息设置
      selectUpVideo: {},//当前播放视频信息
      shareVideoFocusFlag: false,//分享视频输入框焦点标识
      shareHover: false,//分享按钮hover标识
      homeMenu: 0,//1主页 2动态 3投稿 5收藏 6追剧 7设置 8关注 9粉丝 10搜索
      homeUserInformation: {},//home用户信息
      homeLoad:{
        homeHead: false,
        homeAside: false,
        homeBody: false,
      },
      upFlag: false,//置顶标识
      deleteFlag: false,//删除标识
      commentOpenFlag: false,//评论弹窗标识
      keyword: "",//搜索关键字
      collectClassifyNumber: 0,//收藏夹数量
      collectControlsDialogFlag: false,//收藏夹控制弹窗标识
      copyOrMove: 1,//复制或移动 1复制 2移动
      collectIds: [],//收藏夹id
      accountMenu: 1,//1个人信息 2头像 3背景
      mouseX: 0,//鼠标x轴坐标
      collectNumber: 0,
      homeLoadMenuList: [],//home加载菜单列表
      newFansNumber: 0,//新增粉丝数量，未查看粉丝列表前显示提醒
      userNameColor: getStoredUserNameColor(),//用户名字颜色（本地设置，默认黑色）
    }
  },
  getters: {
    userLevel: (state) => Number(state.userInformation?.grade ?? 0),
    colorfulVisibleLevel: () => 4,
    colorfulSendableLevel: () => 5,
    colorfulVisible: (state) => Number(state.userInformation?.grade ?? 0) >= 4,
    colorfulSendable: (state) => Number(state.userInformation?.grade ?? 0) >= 5,
    danmakuEmojiSendable: (state) => Number(state.userInformation?.grade ?? 0) >= 6,
    //弹幕表情等级限制，6级解锁
    danmakuEmojiLevel: () => 6,
    danmakuEmojiMax: () => 10,
    danmakuTextMax: () => 20,
    //表情包扩充，7级解锁
    danmakuEmojiSendableLevel7: (state) => Number(state.userInformation?.grade ?? 0) >= 7,
    //表情包再次扩充，8级解锁
    danmakuEmojiSendableLevel8: (state) => Number(state.userInformation?.grade ?? 0) >= 8,
    //名字颜色 Lv.9 解锁
    userNameColorSettableLevel: () => 9,
    userNameColorSettable: (state) => Number(state.userInformation?.grade ?? 0) >= 9,
    //彩色名字哨兵值，展示端识别后渲染彩虹渐变
    userNameColorRainbow: () => USER_NAME_COLOR_RAINBOW,
    //彩色名字 Lv.10 解锁
    userNameRainbowSettableLevel: () => 10,
    userNameRainbowSettable: (state) => Number(state.userInformation?.grade ?? 0) >= 10,
  },
  actions: {
    setSearchDisplayFlag(newValue) {
      this.searchDisplayFlag = newValue
    },
    setCollectSelect(newValue) {
      this.collectSelect = newValue
    },
    setPlaceholderWord(newValue) {
      this.placeholderWord = newValue
    },
    setUserIp(newValue) {
      this.userIp = newValue
    },
    setSearchDisplayFlag2(newValue) {
      this.searchDisplayFlag2 = newValue
    },
    setLoginDialogVisible(newValue) {
      this.loginDialogVisible = newValue
    },
    setUserId(newValue) {
      if (newValue === null || newValue === undefined || newValue === '') {
        this.userId = null
        if (!offlinePreviewEnabled && typeof localStorage !== 'undefined') {
          localStorage.removeItem(USER_ID_KEY)
        }
      } else {
        const parsed = parseInt(newValue, 10)
        if (Number.isNaN(parsed)) {
          this.userId = null
          if (!offlinePreviewEnabled && typeof localStorage !== 'undefined') {
            localStorage.removeItem(USER_ID_KEY)
          }
          return
        }
        this.userId = parsed
        if (!offlinePreviewEnabled && typeof localStorage !== 'undefined') {
          localStorage.setItem(USER_ID_KEY, String(this.userId))
        }
      }
    },
    setLoginLoadFlag(newValue) {
      this.loginLoadFlag = newValue
    },
    setToken(newValue) {
      if (newValue === undefined) return
      this.token = newValue
      if (offlinePreviewEnabled || typeof localStorage === 'undefined') return
      if (newValue === null || newValue === '') {
        localStorage.removeItem(TOKEN_KEY)
      } else {
        localStorage.setItem(TOKEN_KEY, newValue)
      }
    },
    setEitUserName(newValue) {
      this.eitUserName = newValue
    },
    setEitUserId(newValue) {
      this.eitUserId = newValue
    },
    setEitList(newValue) {
      this.eitList = newValue
    },
    setReVideoPlayerFlag(newValue) {
      this.reVideoPlayerFlag = newValue
    },
    setAddFollowFlag(newValue) {
      this.addFollowFlag = newValue
    },
    setLTCAFlag(newValue) {
      this.LTCAFlag = newValue
    },
    setAutoVideoList(newValue) {
      Object.assign(this.autoVideoList, newValue)
    },
    setAutoPlayVideoList(newValue) {
      this.AutoPlayVideoList = newValue
    },
    setCommentNumber(newValue) {
      this.commentNumber = newValue
    },
    setLoginDialogVisibleFlag(newValue) {
      this.loginDialogVisibleFlag = newValue
    },
    setUserInformation(newValue) {
      this.userInformation = newValue
      //服务端返回的 nameColor 优先，同步成全局预览值（登录/资料刷新都会走这里）
      this.userNameColor = newValue?.nameColor ?? null
    },
    setUserNameColor(newValue) {
      if (typeof localStorage === 'undefined') return
      if (newValue === null || newValue === undefined || newValue === '') {
        //清除后恢复默认黑色
        this.userNameColor = null
        localStorage.removeItem(USER_NAME_COLOR_KEY)
        return
      }
      //彩色哨兵值或 #RRGGBB 均可保存
      if (newValue !== USER_NAME_COLOR_RAINBOW && !/^#[0-9a-fA-F]{6}$/.test(newValue)) return
      this.userNameColor = newValue
      localStorage.setItem(USER_NAME_COLOR_KEY, newValue)
    },
    setUpdateVideoInfoFlag(newValue) {
      this.updateVideoInfoFlag = newValue
    },
    setUpUserId(newValue) {
      this.upUserId = newValue
    },
    setEitUserInfo(newValue) {
      this.eitUserInfo = newValue
    },
    setEitUserInfoHover(newValue) {
      this.eitUserInfoHover = newValue
    },
    setMessageMenu(newValue,flag) {
      this.messageMenu = newValue
      if(flag){
      const url = new URL(window.location);
      url.searchParams.set("messageMenu", newValue);  
      window.history.pushState({}, "", url);
      }  
    },
    setSelectUpVideo(newValue) {
      this.selectUpVideo = newValue
    },
    setShareVideoFocusFlag(newValue) {
      this.shareVideoFocusFlag = newValue
    },
    setShareHover(newValue) {
      this.shareHover = newValue
    },
    setHomeMenu(newValue,userId,flag,collectId) {
      this.homeMenu = newValue
      this.homeLoad.homeBody = false;
      if(flag){
      const url = new URL(window.location);
      url.searchParams.set("homeMenu", newValue);
      url.searchParams.set("userId", userId);
      if(this.homeMenu===10)
        url.searchParams.set("keyword", this.keyword);
      else
        url.searchParams.delete("keyword");

      if(this.homeMenu===5&&collectId)
        url.searchParams.set("collectId", collectId);

      window.history.pushState({}, "", url);
      }
    },
    setHomeUserInformation(newValue) {
      this.homeUserInformation = newValue
    },
    setHomeLoad(newValue,key) {
      this.homeLoad[key] = newValue
    },
    setUpFlag(newValue) {
      this.upFlag = newValue
    },
    setDeleteFlag(newValue) {
      this.deleteFlag = newValue
    },
    setKeyword(newValue) {
      this.keyword = newValue
    },
    setCollectClassifyNumber(newValue) {
      this.collectClassifyNumber = newValue
    },
    setCollectControlsDialogFlag(newValue) {
      this.collectControlsDialogFlag = newValue
    },
    setCopyOrMove(newValue) {
      this.copyOrMove = newValue
    },
    setCollectIds(newValue) {
      this.collectIds = newValue
    },
    setAccountMenu(newValue,flag) {
      this.accountMenu = newValue;
      if(flag){
      const url = new URL(window.location);
      url.searchParams.set("accountMenu", newValue);
      window.history.pushState({}, "", url);
      }
    },
    setMouseX(newValue) {
      this.mouseX = newValue
    },
    setCollectNumber(newValue) {
      this.collectNumber = newValue
    },
    setHomeLoadMenuList(newValue) {
      this.homeLoadMenuList.push(newValue)
    },
    setNewFansNumber(newValue) {
      this.newFansNumber = newValue
    },
  }
})
