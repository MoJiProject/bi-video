import apiClient from "../../services/apiClient";

//统一的请求头
function authHeaders(token) {
  return {
    "Content-Type": "application/json",
    Authorization: token,
  };
}

//仪表盘概览
export async function getOverview(token, operatorId) {
  const response = await apiClient.get("/system/overview/getOverview", {
    headers: authHeaders(token),
    params: { operatorId },
  });
  return response;
}

//用户管理 - 分页查询
export async function searchUsers(token, params) {
  const response = await apiClient.post("/system/user/searchUsers", params, {
    headers: authHeaders(token),
  });
  return response;
}

//用户管理 - 设置/取消管理员
export async function putAdmin(token, params, targetId) {
  const response = await apiClient.put(
    "/system/user/putAdmin",
    params,
    {
      headers: authHeaders(token),
      params: { targetId },
    }
  );
  return response;
}

//用户管理 - 封禁用户
export async function banUser(token, params, targetId) {
  const response = await apiClient.post(
    "/system/user/banUser",
    params,
    {
      headers: authHeaders(token),
      params: { targetId },
    }
  );
  return response;
}

//用户管理 - 解除封禁
export async function unbanUser(token, params, targetId) {
  const response = await apiClient.post(
    "/system/user/unbanUser",
    params,
    {
      headers: authHeaders(token),
      params: { targetId },
    }
  );
  return response;
}

//用户管理 - 查询封禁记录
export async function searchBanList(token, params) {
  const response = await apiClient.get("/system/user/searchBanList", {
    headers: authHeaders(token),
    params,
  });
  return response;
}

//评论管理 - 分页查询
export async function searchComments(token, params) {
  const response = await apiClient.post("/system/content/searchComments", params, {
    headers: authHeaders(token),
  });
  return response;
}

//评论管理 - 删除评论(支持批量)
export async function deleteComment(token, params) {
  const response = await apiClient.post("/system/content/deleteComment", params, {
    headers: authHeaders(token),
  });
  return response;
}

//动态管理 - 分页查询
export async function searchDynamics(token, params) {
  const response = await apiClient.post("/system/content/searchDynamics", params, {
    headers: authHeaders(token),
  });
  return response;
}

//动态管理 - 删除动态(支持批量)
export async function deleteDynamic(token, params) {
  const response = await apiClient.post("/system/content/deleteDynamic", params, {
    headers: authHeaders(token),
  });
  return response;
}

//私信管理 - 分页查询
export async function searchMessages(token, params) {
  const response = await apiClient.post("/system/content/searchMessages", params, {
    headers: authHeaders(token),
  });
  return response;
}

//私信管理 - 删除私信(支持批量)
export async function deleteMessage(token, params) {
  const response = await apiClient.post("/system/content/deleteMessage", params, {
    headers: authHeaders(token),
  });
  return response;
}

//私信管理 - 管理员直接给用户发私信
//复用站内已有的 /privateMessage/sendMessage，
//管理员身份已在后端豁免「未回复只能发1条」的限制
export async function sendPrivateMessage(token, privateMessage) {
  const response = await apiClient.post("/privateMessage/sendMessage", privateMessage, {
    headers: authHeaders(token),
  });
  return response;
}

//搜索热词管理 - 分页查询
export async function searchKeyWords(token, params) {
  const response = await apiClient.post("/system/keyWord/searchKeyWords", params, {
    headers: authHeaders(token),
  });
  return response;
}

//搜索热词管理 - 新增
export async function addKeyWord(token, params, word) {
  const response = await apiClient.post(
    "/system/keyWord/addKeyWord",
    params,
    {
      headers: authHeaders(token),
      params: { word },
    }
  );
  return response;
}

//搜索热词管理 - 修改搜索词
export async function putKeyWord(token, params, keyWordId, word) {
  const response = await apiClient.put(
    "/system/keyWord/putKeyWord",
    params,
    {
      headers: authHeaders(token),
      params: { keyWordId, word },
    }
  );
  return response;
}

//搜索热词管理 - 修改搜索次数
export async function putKeyWordCount(token, params, keyWordId, count) {
  const response = await apiClient.put(
    "/system/keyWord/putKeyWordCount",
    params,
    {
      headers: authHeaders(token),
      params: { keyWordId, count },
    }
  );
  return response;
}

//搜索热词管理 - 删除(支持批量)
export async function deleteKeyWord(token, params) {
  const response = await apiClient.post("/system/keyWord/deleteKeyWord", params, {
    headers: authHeaders(token),
  });
  return response;
}

//操作日志 - 分页查询
export async function searchLogs(token, params) {
  const response = await apiClient.post("/system/log/searchLogs", params, {
    headers: authHeaders(token),
  });
  return response;
}

//视频管理 - 删除视频(真删不可恢复，支持批量)
export async function deleteVideo(token, params) {
  const response = await apiClient.post("/system/video/deleteVideo", params, {
    headers: authHeaders(token),
  });
  return response;
}

//评论下架 / 取消下架
export async function switchCommentOffShelf(token, params, offShelf) {
  const response = await apiClient.post(
    "/system/content/switchCommentOffShelf",
    params,
    {
      headers: authHeaders(token),
      params: { offShelf },
    }
  );
  return response;
}

//动态下架 / 取消下架
export async function switchDynamicOffShelf(token, params, offShelf) {
  const response = await apiClient.post(
    "/system/content/switchDynamicOffShelf",
    params,
    {
      headers: authHeaders(token),
      params: { offShelf },
    }
  );
  return response;
}

//视频管理 - 把已下架/已退回的视频恢复为已通过
export async function restoreVideo(token, params, videoId) {
  const response = await apiClient.put(
    "/system/video/restoreVideo",
    params,
    {
      headers: authHeaders(token),
      params: { videoId },
    }
  );
  return response;
}

//视频管理 - 分页查询(含各状态数量)
export async function searchVideos(token, params) {
  const response = await apiClient.post("/system/video/searchVideos", params, {
    headers: authHeaders(token),
  });
  return response;
}

//视频管理 - 获取已有分区列表
export async function getSubZoneKeys(token, operatorId) {
  const response = await apiClient.get("/system/video/getSubZoneKeys", {
    headers: authHeaders(token),
    params: { operatorId },
  });
  return response;
}

//视频管理 - 审核通过
export async function examineVideo(token, params, videoId) {
  const response = await apiClient.post(
    "/system/video/examineVideo",
    params,
    {
      headers: authHeaders(token),
      params: { videoId },
    }
  );
  return response;
}

//视频管理 - 审核退回
export async function rejectVideo(token, params, videoId) {
  const response = await apiClient.post(
    "/system/video/rejectVideo",
    params,
    {
      headers: authHeaders(token),
      params: { videoId },
    }
  );
  return response;
}

//视频管理 - 下架已通过的视频
export async function takeDownVideo(token, params, videoId) {
  const response = await apiClient.post(
    "/system/video/takeDownVideo",
    params,
    {
      headers: authHeaders(token),
      params: { videoId },
    }
  );
  return response;
}

