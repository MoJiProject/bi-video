// apiClient.js
import axios from 'axios';
import { decryptPayload, encryptPayload } from '../utils/apiCrypto';

const TOKEN_KEY = 'bi_video_token';

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

function getStoredToken() {
    return typeof localStorage === 'undefined' ? null : localStorage.getItem(TOKEN_KEY);
}

function saveTokenFromResponse(data) {
    const token = data && data.data && data.data.token;
    if (typeof token === 'string' && token.length > 0 && typeof localStorage !== 'undefined') {
        localStorage.setItem(TOKEN_KEY, token);
    }
}

apiClient.interceptors.request.use(async (config) => {
    config.headers = config.headers || {};
    if (!config.headers.Authorization) {
        const token = getStoredToken();
        if (token) {
            config.headers.Authorization = token;
        }
    }

    if (!shouldEncryptRequest(config)) return config;

    config.data = {
        payload: await encryptPayload(config.data),
    };
    config.headers['Content-Type'] = 'application/json';
    config.headers['X-Encrypted'] = 'true';

    return config;
});

apiClient.interceptors.response.use(async (response) => {
    if (response.data && typeof response.data.payload === 'string') {
        response.data = await decryptPayload(response.data.payload);
    }

    saveTokenFromResponse(response.data);

    return response;
});

export default apiClient;