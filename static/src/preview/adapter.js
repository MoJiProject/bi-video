import { PREVIEW_USER_ID } from "./config";
import {
  profiles,
  videos,
  folders,
  posts,
  comments,
  dialogues,
  messages,
  legacyUser,
  legacyVideo,
  replyNotifications,
} from "./fixtures";

function page(rows, params) {
  const size = Math.max(1, Number(params.pageSize || params.size || 20));
  const start = params.cursorId
    ? rows.findIndex((row) => String(row.id) === String(params.cursorId)) + 1
    : (Math.max(1, Number(params.pageNum) || 1) - 1) * size;
  const records = rows.slice(start, start + size);
  return {
    records,
    total: rows.length,
    hasMore: start + size < rows.length,
    cursorId: records[records.length - 1]?.id ?? null,
  };
}

function profile(id) {
  const user = profiles.find((row) => String(row.userId) === String(id));
  if (!user) throw new Error(`未配置此预览用户：${id}`);
  return user;
}

function read(path, params) {
  if (path === "/auth/me") return profiles[0];
  if (/^\/auth\/user\/\d+$/.test(path)) return profile(path.split("/").pop());
  if (path === "/banner") return [];
  if (path === "/video/categories")
    return [
      { id: 1, name: "生活" },
      { id: 2, name: "知识" },
      { id: 3, name: "科技" },
    ];
  if (
    [
      "/video/list",
      "/search/video",
      "/folder/videos",
      "/history/list",
      "/watch-later/list",
    ].includes(path) ||
    /^\/user\/\d+\/videos$/.test(path)
  ) {
    return page(
      videos.filter(
        (row) => !params.keyword || row.title.includes(params.keyword)
      ),
      params
    );
  }
  if (/^\/video\/\d+$/.test(path)) {
    const video = videos.find(
      (row) => String(row.id) === path.split("/").pop()
    );
    if (!video) throw new Error(`未配置此预览视频：${path}`);
    return video;
  }
  if (/^\/video\/\d+\/state$/.test(path))
    return {
      liked: false,
      collected: false,
      myCoinCount: 0,
      watchLater: false,
    };
  if (/^\/video\/\d+\/folders$/.test(path))
    return folders.map((row) => ({ ...row, containsVideo: row.id === 1 }));
  if (/^\/video\/getVideoTitle\/\d+$/.test(path)) return videos[0].title;
  if (path === "/folder/list" || path === "/home/collectsClassify")
    return folders;
  if (
    path === "/home/contribute" ||
    path === "/home/featured" ||
    path === "/home/collectByName"
  )
    return page(videos, params);
  if (path === "/home/love" || path === "/home/throwCoin") return videos;
  if (path === "/home/dynamic" || path === "/post/list") {
    const ownerId = params.homeUserId || params.targetUserId;
    const type = Number(params.type || 1);
    return page(posts.filter((row) => {
      if (ownerId && String(row.authorId) !== String(ownerId)) return false;
      if (type === 2) return row.videoId != null;
      if (type === 3) return path === "/home/dynamic" ? row.imageList.length > 0 : row.videoId == null;
      if (type === 4) return row.imageList.length > 0;
      return true;
    }), params);
  }
  if (path === "/home/dynamicImg") return page(posts, params);
  if (
    path === "/home/relations" ||
    path === "/relation/list" ||
    path === "/user/mentions" ||
    path === "/search/user"
  )
    return page(profiles.slice(1), params);
  if (path === "/post/authors")
    return profiles.slice(1).map((row) => ({
      userId: row.userId,
      nickname: row.nickname,
      avatarUrl: row.avatarUrl,
      unreadCount: 0,
    }));
  if (path === "/search/hot") return ["摄影", "生活记录", "桌面收纳"];
  if (path === "/comment/list")
    return page(
      comments.map((row) => ({
        ...row,
        videoId: Number(params.videoId) || 101,
      })),
      params
    );
  if (/^\/comment\/\d+$/.test(path)) {
    const row = [...comments, ...comments.flatMap((item) => item.replies)].find(
      (item) => String(item.id) === path.split("/").pop()
    );
    if (row) return row;
  }
  if (path === "/danmaku/snapshot" || path === "/danmaku/list") {
    const records = ["本地示例弹幕", "这个画面很舒服", "期待下一次更新"].map(
      (content, index) => ({
        id: 601 + index,
        userId: profiles[index].userId,
        content,
        text: content,
        color: "#ffffff",
        videoTimeMs: (index + 1) * 3000,
        mode: index % 3,
        fontSize: 25,
        colorful: false,
        createdAt: "2026-10-21T12:30:00",
      })
    );
    return { records, count: records.length };
  }
  if (path === "/video/userUploadVideo") {
    const rows = videos.filter(
      (row) =>
        (Number(params.videoStatus) < 0 ||
          row.status === Number(params.videoStatus)) &&
        (!params.videoTitle || row.title.includes(params.videoTitle)) &&
        (!params.subZoneKey ||
          params.subZoneKey === "全部分类" ||
          params.subZoneKey === row.subCategoryName)
    );
    return {
      videos: page(rows, { ...params, pageSize: 10 }).records.map(legacyVideo),
      users: legacyUser(profiles[0]),
      total: rows.length,
      videoAllNumber: videos.length,
      videoWaitNumber: 1,
      videoSuccessNumber: 4,
      videoErrorNumber: 1,
    };
  }
  if (path === "/video/getVideoInfo")
    return legacyVideo(
      videos.find((row) => String(row.id) === String(params.videoId)) ||
        videos[0]
    );
  if (path === "/privateMessage/selectDialogue")
    return Number(params.pageNum || 1) === 1 ? dialogues : [];
  if (path === "/privateMessage/selectNotReadNumber")
    return profiles[0].messageNumber;
  if (path === "/privateMessage/selectMessage")
    return {
      privateMessage:
        Number(params.pageNum) === 1 ? messages(params.dialogueId) : [],
      userInfo: legacyUser(profiles[0]),
      dialogueUserInfo: legacyUser(profile(params.dialogueId)),
    };
  if (path === "/comment/selectReplayComment")
    return Number(params.pageNum || 1) === 1 ? replyNotifications : [];
  if (path === "/comment/selectAt")
    return Number(params.pageNum || 1) === 1
      ? replyNotifications.map((row) => ({
          userId: row.userId,
          userName: row.userName,
          avatarAddress: row.avatarAddress,
          at: { id: row.replyComments.id, videoId: 101 },
          comments: row.replyComments,
          mainComments: null,
          videos: row.videos,
          dynamic: null,
          commentControls: { controls: null },
        }))
      : [];
  if (path === "/comment/selectLike")
    return Number(params.pageNum || 1) === 1
      ? videos.slice(0, 3).map((video, index) => ({
          id: 701 + index,
          likes: { id: 701 + index, likeType: 1 },
          videos: { ...legacyVideo(video), likeWarn: 1 },
          userInfoList: [legacyUser(profiles[index + 1])],
          comments: null,
          dynamic: null,
          likeTime: "2026-10-21 12:30:00",
        }))
      : [];
  if (path === "/comment/selectLikeUser")
    return Number(params.pageNum || 1) === 1
      ? profiles.slice(1).map(legacyUser)
      : [];
  if (path === "/user/selectFollowAndFans")
    return { records: profiles.slice(1).map(legacyUser), total: 3 };
  throw new Error(`未配置此预览数据：GET ${path}`);
}

export async function previewAdapter(config) {
  const url = new URL(config.url, window.location.origin);
  const path = url.pathname.replace(/^\/api(?=\/)/, "");
  const params = { ...Object.fromEntries(url.searchParams), ...config.params };
  const method = (config.method || "get").toLowerCase();
  let data;
  let error;
  try {
    if (method === "get") data = read(path, params);
    else if (method === "post" && path === "/video/selectVideoData")
      data = {
        userId: PREVIEW_USER_ID,
        newFansNumber: 128,
        playNumber: 48620,
        commentNumber: 236,
        scrollingNumber: 618,
        likeNumber: 8264,
        shareNumber: 175,
        collectNumber: 624,
        coinThrowNumber: 128,
      };
    else if (
      method === "put" &&
      [
        "/privateMessage/putMessageNumber",
        "/privateMessage/putMessageStatus",
      ].includes(path)
    ) {
      if (path.endsWith("putMessageStatus")) {
        const dialogue = dialogues.find(
          (row) => String(row.dialogue.dialogueId) === String(params.dialogueId)
        );
        if (dialogue) dialogue.notReadNumber = 0;
      } else {
        const key = {
          2: "replyCommentNumber",
          3: "atNumber",
          4: "likeAllNumber",
        }[params.messageMenu];
        if (key) profiles[0][key] = 0;
      }
      profiles[0].messageNumber = dialogues.reduce(
        (total, row) => total + row.notReadNumber,
        0
      );
      profiles[0].allMessageNumber = [
        "messageNumber",
        "replyCommentNumber",
        "atNumber",
        "likeAllNumber",
      ].reduce((total, key) => total + profiles[0][key], 0);
      data = null;
    } else
      throw new Error(
        `离线预览仅展示界面，未提交此操作：${method.toUpperCase()} ${path}`
      );
  } catch (caught) {
    error = caught.message;
    console.info("[Offline preview]", error);
  }
  return {
    config,
    status: 200,
    statusText: "Offline preview",
    headers: {},
    data: error
      ? { code: 0, msg: error, data: null }
      : {
          code: 1,
          msg: "本地示例数据",
          data: JSON.parse(JSON.stringify(data)),
        },
  };
}
