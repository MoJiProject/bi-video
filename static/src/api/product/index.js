import { del, get, post, put } from '../../services/http';

/** 视频：列表、详情、分类。 */
export const videoApi = {
  categories: () => get('/video/categories'),
  list: (params) => get('/video/list', params),
  detail: (videoId) => get(`/video/${videoId}`),

  /** 一次拿全互动状态，代替每个按钮各发一次请求。 */
  state: (videoId) => get(`/video/${videoId}/state`),
  like: (videoId) => post(`/video/${videoId}/like`),
  coin: (videoId, coinCount = 1) => post(`/video/${videoId}/coin`, { coinCount }),
  favorite: (videoId, folderId) => post(`/video/${videoId}/favorite`, null, { params: { folderId } }),
  watchLater: (videoId) => post(`/video/${videoId}/watch-later`),
  reportWatch: (videoId, progressSeconds) => post(`/video/${videoId}/watch`, null, { params: { progressSeconds } }),
};

/** 评论与弹幕。 */
export const commentApi = {
  list: (params) => get('/comment/list', params),
  create: (body) => post('/comment', body),
  like: (commentId, dislike = false) => post(`/comment/${commentId}/like`, null, { params: { dislike } }),
  remove: (commentId) => del(`/comment/${commentId}`),
};

export const danmakuApi = {
  list: (videoId, limit = 2000) => get('/danmaku/list', { videoId, limit }),
  create: (body) => post('/danmaku', body),
};

/** 用户自己的内容。 */
export const userContentApi = {
  folders: () => get('/folder/list'),
  folderVideos: (params) => get('/folder/videos', params),
  createFolder: (body) => post('/folder', body),
  renameFolder: (folderId, body) => put(`/folder/${folderId}`, body),
  deleteFolder: (folderId) => del(`/folder/${folderId}`),
  removeFromFolder: (folderId, videoId) => del(`/folder/${folderId}/video/${videoId}`),

  watchLater: (params) => get('/watch-later/list', params),
  clearWatchLater: () => del('/watch-later'),

  history: (params) => get('/history/list', params),
  deleteHistory: (videoId) => del(`/history/${videoId}`),
  clearHistory: () => del('/history'),

  userVideos: (userId, params) => get(`/user/${userId}/videos`, params),
};

/** 关注关系与动态。 */
export const socialApi = {
  follow: (userId) => post(`/user/${userId}/follow`),
  posts: (params) => get('/post/list', params),
  createPost: (body) => post('/post', body),
  deletePost: (postId) => del(`/post/${postId}`),
  relations: (params) => get('/relation/list', params),
};

/** 搜索。 */
export const searchApi = {
  videos: (params) => get('/search/video', params),
  users: (params) => get('/search/user', params),
  hot: (limit = 10) => get('/search/hot', { limit }),
};

/** 账号。 */
export const authApi = {
  register: (body) => post('/auth/register', body),
  login: (body) => post('/auth/login', body),
  logout: () => post('/auth/logout'),
  me: () => get('/auth/me'),
  profile: (userId) => get(`/auth/user/${userId}`),
};