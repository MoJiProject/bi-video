import { del, post, put } from '../../services/http';

function request(folder) {
  return {
    name: folder.collectName,
    description: folder.content || '',
    coverUrl: folder.coverAddress || '',
    visibility: folder.status ?? 1,
  };
}

function envelope(data) {
  return { data: { code: 1, data } };
}

export async function addCollectClassify(token, folder) {
  return envelope(await post('/folder', request(folder)));
}

export async function deleteCollectClassify(token, userId, id) {
  return envelope(await del(`/folder/${id}`));
}

export async function updateCollectClassify(token, folder) {
  return envelope(await put(`/folder/${folder.id}`, request(folder)));
}
