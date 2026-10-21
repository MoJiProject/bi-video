import apiClient from '@/services/apiClient';

export function selectHistoryList(params, config = {}) {
  return apiClient.get('/history/list', { ...config, params });
}

export function deleteHistory(videoIds) {
  return apiClient.delete('/history/batch', { data: { videoIds } });
}

export function deleteAllHistory() {
  return apiClient.delete('/history');
}
