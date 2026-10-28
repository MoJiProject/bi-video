import { del, get, post, put } from '../../services/http';

// Keep the legacy page envelope while using product-app authentication and data.
async function request(path, params, transform = (data) => data) {
  const data = await get(`/home/${path}`, params);
  return { data: { code: 1, data: transform(data) } };
}

export function mapFolder(folder) {
  return {
    ...folder,
    userId: folder.ownerId,
    collectName: folder.name,
    content: folder.description,
    coverAddress: folder.coverUrl,
    status: folder.visibility,
    videoNumber: folder.itemCount,
  };
}

export function homeContributeVideos(homeUserId, userId, sort, pageNum, keyWord) {
  return request('contribute', { homeUserId, sort, pageNum, keyWord });
}

export function homeMagnumVideos(homeUserId) {
  return request('featured', { homeUserId });
}

export function updateFeaturedVideos(videoIds) {
  return put('/home/featured', videoIds);
}

export function homeLoveVideos(userId, homeUserId) {
  return request('love', { homeUserId });
}

export function dynamicImg(homeUserId, type, pageNum) {
  return request('dynamicImg', { homeUserId, type, pageNum });
}

export function homeDynamic(homeUserId, userId, pageNum, type, keyWord) {
  return request('dynamic', { homeUserId, pageNum, type, keyWord });
}

export function getCollectsClassify(homeUserId) {
  return request('collectsClassify', { homeUserId }, (folders) => folders.map(mapFolder));
}

export function getCollectByName(collectName, homeUserId, userId, pageNum, type, sort, keyWord, folderId) {
  return request('collectByName', { collectName, homeUserId, pageNum, type, sort, keyWord, folderId });
}

export function batchFolderVideos(folderId, operation, videoIds, targetFolderId) {
  return post(`/home/folders/${folderId}/batch`, { operation, videoIds, targetFolderId });
}

export function clearInvalidFolderVideos(folderId) {
  return del(`/home/folders/${folderId}/invalid`);
}
