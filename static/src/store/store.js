// store.js
import { defineStore } from 'pinia'

export const useGlobalStore = defineStore('global', {
  state: () => {
    return {
      searchDisplayFlag: false,
      collectSelect: null,
      placeholderWord: "",
      userIp: null,
      searchDisplayFlag2: false,
      loginDialogVisible: false,
      userId: null,
      loginLoadFlag: false,
      token: null,
      eitUserName: "", 
      eitUserId: null,
      eitList: [],//eit列表
      reVideoPlayerFlag: false,//回放视频播放器标识
      addFollowFlag: 0,//关注 0没有 1关注 2取消关注
      LTCAFlag: 0,//点赞投币收藏分享 0没有 1点赞 2投币 3收藏
      autoVideoList: [],//自动播放列表 或 收藏夹视频选择列表
      AutoPlayVideoList: false,//是否自动播放列表
      commentNumber: 0,//评论数
      loginDialogVisibleFlag: false,//登录弹窗标识
      userInformation: {},//用户信息
      updateVideoInfoFlag: false,//更新视频信息标识
      upUserId: null,//视频up主id
      eitUserInfo: null,//eit用户信息
      eitUserInfoHover: false,//eit用户信息hover标识
      messageMenu: 1, //消息菜单 1消息 2回复 3@ 4点赞 5消息设置
      selectUpVideo: {},//当前播放视频信息
      shareVideoFocusFlag: false,//分享视频输入框焦点标识
      shareHover: false,//分享按钮hover标识
      homeMenu: 1,//1主页 2动态 3投稿 4合集 5收藏 6追番追剧 7设置 8关注 9粉丝 10搜索
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
      collectNumber: 0
    }
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
      if(newValue===null)
        this.userId = null
      else
      this.userId = parseInt(newValue)
    },
    setLoginLoadFlag(newValue) {
      this.loginLoadFlag = newValue
    },
    setToken(newValue) {
      this.token = newValue
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
    }
  }
})
