const CHUNK_SIZE = 4 * 1024 * 1024;
const MAX_CONCURRENT_UPLOADS = 3;

function createUploadId() {
  if (window.crypto && window.crypto.randomUUID) {
    return window.crypto.randomUUID().replace(/-/g, "");
  }
  return `${Date.now()}${Math.random().toString(16).slice(2)}`;
}

function getUploadedSize(chunkProgress) {
  return Array.from(chunkProgress.values()).reduce((total, loaded) => total + loaded, 0);
}

export async function uploadVideoByChunks({ apiClient, file, userId, token, onProgress, onUploadId, signal }) {
  const uploadId = createUploadId();
  onUploadId?.(uploadId);
  const totalChunks = Math.ceil(file.size / CHUNK_SIZE);
  const chunkProgress = new Map();
  let nextChunkIndex = 0;

  const updateProgress = () => {
    if (!onProgress) return;
    const uploadedSize = getUploadedSize(chunkProgress);
    const percentage = Math.min(99, Math.floor((uploadedSize / file.size) * 99));
    onProgress(percentage);
  };

  const uploadChunk = async (chunkIndex) => {
    const start = chunkIndex * CHUNK_SIZE;
    const end = Math.min(start + CHUNK_SIZE, file.size);
    const formData = new FormData();

    formData.append("file", file.slice(start, end));
    formData.append("uploadId", uploadId);
    formData.append("chunkIndex", chunkIndex);
    formData.append("totalChunks", totalChunks);
    formData.append("userId", userId);

    const response = await apiClient.post("/upload/uploadVideoChunk", formData, {
      headers: {
        "Content-Type": "multipart/form-data",
        "Authorization": token,
      },
      signal,
      onUploadProgress(event) {
        chunkProgress.set(chunkIndex, event.loaded);
        updateProgress();
      },
    });

    if (response.data.code !== 1) {
      throw new Error(response.data.msg || "视频分片上传失败");
    }

    chunkProgress.set(chunkIndex, end - start);
    updateProgress();
  };

  const uploadWorker = async () => {
    while (nextChunkIndex < totalChunks) {
      const chunkIndex = nextChunkIndex;
      nextChunkIndex += 1;
      await uploadChunk(chunkIndex);
    }
  };

  onProgress?.(0);
  const workerCount = Math.min(MAX_CONCURRENT_UPLOADS, totalChunks);
  await Promise.all(Array.from({ length: workerCount }, () => uploadWorker()));

  const mergeData = new FormData();
  mergeData.append("uploadId", uploadId);
  mergeData.append("totalChunks", totalChunks);
  mergeData.append("originalFilename", file.name);
  mergeData.append("userId", userId);

  const response = await apiClient.post("/upload/mergeVideoChunks", mergeData, {
    headers: {
      "Content-Type": "multipart/form-data",
      "Authorization": token,
    },
    signal,
  });

  if (response.data.code !== 1) {
    throw new Error(response.data.msg || "视频分片合并失败");
  }

  onProgress?.(100);
  return response.data.data;
}