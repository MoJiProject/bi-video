import http, { get, post } from '../../services/http';

const envelope = data => ({ data: { code: 1, data } });

export function mapRelatedVideo(video) {
  return {
    ...video, videoId: video.id, videoTitle: video.title, videoAddress: video.playUrl,
    coverAddress: video.coverUrl, videoTime: formatDuration(video.durationSeconds),
    playNumber: video.playCount, scrollingNumber: video.danmakuCount,
    videoPlayNumber: video.playCount, videoScrollingNumber: video.danmakuCount,
    userId: video.ownerId, userName: video.ownerNickname, waitWatch: video.watchLater ? 1 : 0,
  };
}

function formatDuration(seconds = 0) {
  return `${Math.floor(seconds / 60).toString().padStart(2, '0')}:${(seconds % 60).toString().padStart(2, '0')}`;
}

/** 视频详情及结束推荐沿用模板数据形状，不向已下线的旧端点发送请求。 */
export default {
  defaults: http.defaults,
  async get(url, config = {}) {
    if (url === '/video/getByIdCollectClassify') {
      const folders = await get(`/video/${config.params.videoId}/folders`);
      return envelope(folders.map(folder => ({
        id: folder.id, collectName: folder.name, videoNumber: folder.itemCount, flag: folder.containsVideo,
      })));
    }
    return http.get(url, config);
  },
  async post(url, body, config) {
    if (url === '/video/getLTCAxios') {
      const id = body.upVideo.id;
      const state = await get(`/video/${id}/state`);
      if (body.controlsType === 1 || !state.liked) await post(`/video/${id}/like`);
      if (body.controlsType === 3 && !state.collected) await post(`/video/${id}/favorite`);
      const updated = await get(`/video/${id}`);
      const flags = await get(`/video/${id}/state`);
      return envelope({
        upVideo: { ...body.upVideo, likeNumber: updated.likeCount, collectNumber: updated.favoriteCount },
        likeVideoClickFlag: flags.liked, videoCollectClickFlag: flags.collected,
      });
    }
    if (url === '/collect/addCollect') {
      const folders = await get(`/video/${body.videoId}/folders`);
      for (const item of body.allInFlags) {
        const folder = folders.find(row => row.name === item.name);
        if (folder && !!folder.containsVideo !== !!item.flag)
          await post(`/video/${body.videoId}/favorite`, null, { params: { folderId: folder.id } });
      }
      return envelope(await get(`/video/${body.videoId}/state`));
    }
    if (url === '/history/getHistoryById') {
      const video = await get(`/video/${body.id}`);
      return envelope({ watchCurrentTime: Math.max(0, video.watchProgress), watchVideoTime: formatDuration(Math.max(0, video.watchProgress)) });
    }
    if (url === '/video/getVideoPageByVideo') {
      const page = await get('/video/list', { pageNum: 1, pageSize: 13, sort: 'hot' });
      return envelope(page.records.filter(video => String(video.id) !== String(body.id)).map(mapRelatedVideo));
    }
    return http.post(url, body, config);
  },
  async put(url, body, config) {
    if (url === '/dynamic/updateWaitWatch') {
      const state = await post(`/video/${body.videoId}/watch-later`);
      return envelope(state.watchLater ? '1' : '0');
    }
    if (url.startsWith('/video/updateVideoPlay?')) {
      const id = new URLSearchParams(url.split('?')[1]).get('videoId');
      return envelope(await post(`/video/${id}/play`));
    }
    return http.put(url, body, config);
  },
};
