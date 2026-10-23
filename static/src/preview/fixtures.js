import { PREVIEW_USER_ID } from "./config";

function artwork(index, avatar = false) {
  const colors = [
    "#0FA68E",
    "#527DAD",
    "#C27855",
    "#697D63",
    "#8870A3",
    "#B68A46",
  ];
  const color = colors[index % colors.length];
  const svg = avatar
    ? `<svg xmlns="http://www.w3.org/2000/svg" width="160" height="160"><rect width="160" height="160" rx="80" fill="${color}"/><circle cx="80" cy="62" r="27" fill="#fff" opacity=".85"/><path d="M28 146c0-57 104-57 104 0" fill="#fff" opacity=".85"/></svg>`
    : `<svg xmlns="http://www.w3.org/2000/svg" width="1280" height="720" viewBox="0 0 1280 720"><defs><linearGradient id="g" x2="1" y2="1"><stop stop-color="${color}"/><stop offset="1" stop-color="#152D38"/></linearGradient></defs><rect width="1280" height="720" fill="url(#g)"/><circle cx="${
        880 - index * 45
      }" cy="210" r="160" fill="#fff" opacity=".16"/><path d="M0 610 350 230 720 570 1030 330 1280 610V720H0" fill="#fff" opacity=".18"/><path d="M0 675 450 450 860 660 1280 490V720H0" fill="#fff" opacity=".12"/><rect x="80" y="90" width="76" height="8" rx="4" fill="#fff" opacity=".7"/></svg>`;
  return `data:image/svg+xml;charset=utf-8,${encodeURIComponent(svg)}`;
}

export const profiles = ["预览用户", "林间记录", "镜头日记", "慢慢创作"].map(
  (nickname, index) => ({
    userId: PREVIEW_USER_ID + index,
    nickname,
    avatarUrl: artwork(index, true),
    backgroundUrl: artwork(index),
    signature:
      index === 0
        ? "这是本地预览资料，不对应真实账号。"
        : "摄影、旅行与生活记录",
    level: 5 - index,
    role: 1,
    coinBalance: 128,
    favoriteCount: 24,
    videoCount: 6,
    postCount: 3,
    followingCount: 36,
    followerCount: 1280,
    likeReceived: 8264,
    playTotal: 48620,
    createdAt: "2025-03-12T10:30:00",
    birthday: "1998-06-12",
    gender: index % 3,
    followed: index !== 0,
    showFavoriteList: true,
    showFollowList: true,
    showFansList: true,
    showCoinList: true,
    showLikeList: true,
    showBirthday: true,
    recordHistory: true,
    messageNumber: 2,
    replyCommentNumber: 2,
    atNumber: 1,
    likeAllNumber: 3,
    allMessageNumber: 8,
    messageWarn: 1,
    replyMessageWarn: 1,
    atMessageWarn: 1,
    likeMessageWarn: 1,
    backgroundModel: 0,
  })
);

export const videos = [
  "周末的山间徒步记录",
  "一座城市的清晨与傍晚",
  "桌面收纳与工作区整理",
  "从零开始学习构图",
  "雨天在家做一份简单晚餐",
  "用镜头记录四季变化",
].map((title, index) => {
  const owner = profiles[index === 0 ? 1 : index < 3 ? 0 : 1 + (index % 3)];
  return {
    id: 101 + index,
    title,
    description: "本地示例内容，用于查看视频介绍、长文本换行和页面布局。",
    descriptionHtml:
      "<p>本地示例内容，用于查看视频介绍、长文本换行和页面布局。</p><p>当前仅展示界面，未连接后端或加载真实视频。</p>",
    coverUrl: artwork(index),
    playUrl: "",
    source: 0,
    durationSeconds: 186 + index * 73,
    ownerId: owner.userId,
    ownerNickname: owner.nickname,
    ownerAvatar: owner.avatarUrl,
    ownerFollowed: owner.followed,
    playCount: 12580 + index * 1840,
    danmakuCount: 3,
    likeCount: 286 + index * 57,
    favoriteCount: 68 + index * 13,
    commentCount: 2,
    shareCount: 16 + index * 4,
    coinCount: 24,
    originalType: 0,
    allowRemix: false,
    tags: "生活,摄影,记录",
    subCategoryName: "生活",
    publishedAt: `2026-10-${String(20 - index).padStart(2, "0")}T10:30:00`,
    watchProgress: 0,
    watchLater: index % 2 === 0,
    status: index === 4 ? 0 : index === 5 ? 2 : 1,
    listEntryId: 500 + index,
    progressSeconds: 20 + index * 10,
    lastWatchedAt: new Date(Date.now() - index * 3600000).toISOString(),
  };
});

export function legacyUser(profile) {
  return {
    ...profile,
    id: profile.userId,
    userName: profile.nickname,
    avatarAddress: profile.avatarUrl,
    backgroundAddress: profile.backgroundUrl,
    grade: profile.level,
    introduce: profile.signature,
  };
}

export function legacyVideo(video) {
  return {
    ...video,
    userId: video.ownerId,
    userName: video.ownerNickname,
    coverAddress: video.coverUrl,
    videoAddress: video.playUrl,
    videoTime: `${Math.floor(video.durationSeconds / 60)}:${String(
      video.durationSeconds % 60
    ).padStart(2, "0")}`,
    content: video.description,
    contentHtml: video.descriptionHtml,
    tag: video.tags,
    videoSource: 0,
    type: 0,
    allowTwo: 0,
    subZoneKey: "生活",
    playNumber: video.playCount,
    likeNumber: video.likeCount,
    scrollingNumber: video.danmakuCount,
    commentNumber: video.commentCount,
    collectNumber: video.favoriteCount,
    coinThrowNumber: video.coinCount,
    shareNumber: video.shareCount,
    createTime: video.publishedAt.replace("T", " "),
    examineFiledMessage:
      video.status === 2 ? "示例退回原因：请补充视频说明。" : "",
  };
}

export const folders = [
  {
    id: 1,
    ownerId: PREVIEW_USER_ID,
    name: "默认收藏夹",
    description: "本地示例收藏",
    visibility: 1,
    itemCount: 4,
    isDefault: true,
    coverUrl: videos[0].coverUrl,
  },
  {
    id: 2,
    ownerId: PREVIEW_USER_ID,
    name: "摄影与生活",
    description: "本地示例收藏",
    visibility: 1,
    itemCount: 2,
    isDefault: false,
    coverUrl: videos[3].coverUrl,
  },
];

export const posts = videos.slice(0, 3).map((video, index) => ({
  id: 201 + index,
  authorId: PREVIEW_USER_ID,
  authorNickname: profiles[0].nickname,
  authorAvatar: profiles[0].avatarUrl,
  content: [
    "整理了上周拍摄的照片。",
    "最近完成的视频已经发布。",
    "今天试着换一种构图。",
  ][index],
  title: "",
  imageList: index === 1 ? [] : [video.coverUrl],
  videoId: index === 1 ? video.id : null,
  videoTitle: video.title,
  videoCover: video.coverUrl,
  videoDescription: video.description,
  videoPlayCount: video.playCount,
  videoDanmakuCount: video.danmakuCount,
  createdAt: video.publishedAt,
  likeCount: 28 + index * 5,
  commentCount: 2,
  shareCount: 3,
  liked: false,
  isPinned: index === 0,
  isSelf: true,
  sourceType: 0,
  visibility: 0,
  watchLater: false,
}));

export const comments = profiles.slice(1, 3).map((author, index) => ({
  id: 301 + index,
  videoId: 101,
  authorId: author.userId,
  authorNickname: author.nickname,
  authorAvatar: author.avatarUrl,
  authorLevel: author.level,
  rootId: null,
  content:
    index === 0
      ? "画面和配色很舒服，期待下一次更新。"
      : "这是一条稍长的示例评论，用来查看正文换行、作者信息和互动按钮的排列效果。",
  imageUrls: [],
  createdAt: "2026-10-21T12:30:00",
  likeCount: 12 + index,
  pinned: false,
  liked: false,
  disliked: false,
  replyCount: index === 0 ? 1 : 0,
  replies:
    index === 0
      ? [
          {
            id: 303,
            videoId: 101,
            authorId: PREVIEW_USER_ID,
            authorNickname: profiles[0].nickname,
            authorAvatar: profiles[0].avatarUrl,
            authorLevel: 5,
            rootId: 301,
            content: "谢谢你的留言。",
            imageUrls: [],
            createdAt: "2026-10-21T13:00:00",
            likeCount: 2,
            pinned: false,
            replyCount: 0,
            replies: [],
          },
        ]
      : [],
}));

export const dialogues = profiles.slice(1).map((user, index) => ({
  dialogue: {
    id: 401 + index,
    userId: PREVIEW_USER_ID,
    dialogueId: user.userId,
    dnd: index === 2 ? 1 : 0,
    upDateTime: index === 0 ? "2026-10-21 12:00:00" : null,
    newContent: [
      "你好，想和你交流一下拍摄方法。",
      "收到，谢谢分享！",
      "下次一起去拍照吧。",
    ][index],
    updateTime: "2026-10-21 12:30:00",
  },
  userInfo: legacyUser(user),
  notReadNumber: index === 0 ? 2 : 0,
  allMessageNumber: 3,
}));

export function messages(peerId) {
  return [
    {
      id: 501,
      senderId: Number(peerId),
      receiverId: PREVIEW_USER_ID,
      content: "你好，想和你交流一下拍摄方法。",
      messageType: 1,
      status: 1,
      sendTime: "2026-10-21 12:20:00",
    },
    {
      id: 502,
      senderId: PREVIEW_USER_ID,
      receiverId: Number(peerId),
      content: "可以的，你平时更喜欢拍摄风景还是人像？",
      messageType: 1,
      status: 1,
      sendTime: "2026-10-21 12:21:00",
    },
    {
      id: 503,
      senderId: Number(peerId),
      receiverId: PREVIEW_USER_ID,
      content: "最近在练习风景摄影，想看看不同设备下的构图效果。",
      messageType: 1,
      status: 0,
      sendTime: "2026-10-21 12:22:00",
    },
  ].map((row) => ({
    ...row,
    createTime: row.sendTime,
    messageTime: row.sendTime,
  }));
}

export const replyNotifications = comments.map((row) => ({
  userId: row.authorId,
  userName: row.authorNickname,
  avatarAddress: row.authorAvatar,
  videoTitle: videos[0].title,
  coverAddress: videos[0].coverUrl,
  replyComments: {
    id: row.id,
    videoId: 101,
    content: row.content,
    imgAddress: "",
    commentTime: row.createdAt.replace("T", " "),
    mainCommentId: null,
    replyCommentId: null,
    deleteSign: 0,
    status: 0,
  },
  mainComments: null,
  dynamic: null,
  videos: legacyVideo(videos[0]),
  video: legacyVideo(videos[0]),
  replyCommentControls: { controls: null },
}));
