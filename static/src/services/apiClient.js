// apiClient.js
import axios from 'axios';

// 创建 axios 实例
const apiClient = axios.create({
    baseURL: "/api",  // 替换为你的 API URL
    timeout: 120000,                   // 设置超时
});


export default apiClient;