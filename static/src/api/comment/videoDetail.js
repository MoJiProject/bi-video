import http, { get, post, del } from '../../services/http';
import { mapUser } from '../home/user';
import { sanitizeRichText, extractMentionIds } from '../../utils/richText';

const envelope = data => ({ data: { code: 1, data } });

export function mapVideoComment(row) {
  return {
    comments: {
      id: row.id, videoId: row.videoId, userId: row.authorId,
      mainCommentId: row.rootId ?? null,
      content: sanitizeRichText(row.content),
      imgAddress: row.imageUrls?.length ? `${row.imageUrls.join(',')},` : '',
      commentTime: (row.createdAt || '').replace('T', ' '),
      likeCommentNumber: row.likeCount || 0,
      upFlag: row.pinned ? 1 : 0,
    },
    userInfo: {
      id: row.authorId, userName: row.authorNickname,
      avatarAddress: row.authorAvatar || '/img/avatar-default.png', grade: row.authorLevel || 0,
      followNumber: 0, fansNumber: 0, likeNumber: 0, introduce: '', isFansFlag: 0,
    },
    commentControls: { controls: row.liked ? 1 : row.disliked ? 0 : null },
    replyNumber: row.replyCount || 0,
    replyCommentList: (row.replies || []).map(mapVideoComment),
    replyFlag: false, page: 1,
  };
}

export async function pinVideoComment(id) {
  return post(`/comment/${id}/pin`);
}

export async function fetchVideoComment(id, videoId) {
  const row = await get(`/comment/${id}`);
  if (String(row.videoId) !== String(videoId)) throw new Error('评论不属于当前视频');
  return mapVideoComment(row);
}

/** 只供视频详情旧模板使用；请求实际发送到 product 的评论/用户接口。 */
export function createVideoCommentClient(videoId) {
  let cursorId;
  let hasMore = true;
  const client = {
    async get(url, config = {}) {
      const params = config.params || {};
      if (url === '/comment/selectCommentByVideoId') {
        if (params.pageNum === 1) { cursorId = undefined; hasMore = true; }
        if (!hasMore) return envelope({ records: [], hasMore: false });
        const page = await get('/comment/list', {
          videoId, cursorId, size: 20, sort: params.commentSortFlag === 1 ? 'hot' : 'new',
        });
        cursorId = page.cursorId;
        hasMore = page.hasMore;
        return envelope({ ...page, records: page.records.map(mapVideoComment) });
      }
      if (url === '/comment/selectReplyById') {
        const root = await fetchVideoComment(params.commentId, videoId);
        const start = (Math.max(1, params.pageNum || 1) - 1) * 10;
        return envelope({ records: root.replyCommentList.slice(start, start + 10), total: root.replyNumber });
      }
      if (url === '/user/selectFollow' || url === '/user/selectUserInfoById') {
        const profile = await get(`/auth/user/${params.viewUserId || params.eitUserId}`);
        return envelope(mapUser(profile));
      }
      return http.get(url, config);
    },
    async post(url, body, config) {
      if (url === '/comment/addComment') {
        const c = body.comments;
        const content = sanitizeRichText(c.content);
        const mentionUserIds = extractMentionIds(content);
        const imageUrls = c.imgAddress ? c.imgAddress.split(',,').filter(Boolean) : [];
        const created = await post('/comment', {
          videoId, content, imageUrls, mentionUserIds, rootId: c.replyCommentId || c.mainCommentId || null,
        });
        const root = await fetchVideoComment(created.commentId, videoId);
        // 单独保留“同时转发到动态”的行为，不把失败的转发当成评论发送失败。
        if (c.dynamicFlag) {
          try { await post('/post', { content, imageUrls, mentionUserIds, videoId: Number(videoId), visibility: 0 }); }
          catch (error) { root.dynamicError = error.message; }
        }
        return envelope(root);
      }
      if (url === '/video/addFollow' || url === '/video/deleteFollow') {
        return envelope(await post(`/user/${body.followId}/follow`));
      }
      return http.post(url, body, config);
    },
    async delete(url, config) {
      if (url === '/comment/deleteComment') {
        await del(`/comment/${config.params.commentId}`);
        return envelope('删除成功');
      }
      return http.delete(url, config);
    },
  };
  return client;
}
