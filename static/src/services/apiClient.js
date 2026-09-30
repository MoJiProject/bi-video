// apiClient.js
import axios from 'axios';
import { decryptPayload, encryptPayload } from '../utils/apiCrypto';

// 创建 axios 实例
const apiClient = axios.create({
    baseURL: "/api",  // 替换为你的 API URL
    timeout: 300000,   // 设置超时
});

function isFormData(value) {
    return typeof FormData !== 'undefined' && value instanceof FormData;
}

function isJsonBody(value) {
    return value !== undefined
        && value !== null
        && typeof value === 'object'
        && !isFormData(value)
        && !(value instanceof URLSearchParams)
        && !(value instanceof Blob)
        && !(value instanceof ArrayBuffer);
}

function shouldEncryptRequest(config) {
    const method = (config.method || 'get').toLowerCase();
    return ['post', 'put', 'patch'].includes(method) && isJsonBody(config.data);
}

apiClient.interceptors.request.use(async (config) => {
    if (!shouldEncryptRequest(config)) return config;

    config.data = {
        payload: await encryptPayload(config.data),
    };
    config.headers = config.headers || {};
    config.headers['Content-Type'] = 'application/json';
    config.headers['X-Encrypted'] = 'true';

    return config;
});

apiClient.interceptors.response.use(async (response) => {
    if (response.data && typeof response.data.payload === 'string') {
        response.data = await decryptPayload(response.data.payload);
    }

    return response;
});

export default apiClient;