import axios from 'axios';

const TOKEN_KEY = 'product_token';
const BASE_URL = import.meta.env?.VITE_API_BASE || '/api';

/**
 * 统一请求实例（对接 product-app）。
 *
 * 与后端的约定：
 *  - token 放在 `token` 请求头（Sa-Token 的 token-name）
 *  - 响应体固定 { code, msg, data }，code === 1 为成功
 *  - 新后端不加密，旧版那套 AES 载荷已下线，这里不再包 payload
 */
const http = axios.create({
  baseURL: BASE_URL,
  timeout: 30000,
});

export function getToken() {
  try {
    return localStorage.getItem(TOKEN_KEY) || '';
  } catch {
    return '';
  }
}

export function setToken(token) {
  try {
    if (token) localStorage.setItem(TOKEN_KEY, token);
    else localStorage.removeItem(TOKEN_KEY);
  } catch {
    /* 隐私模式下 localStorage 不可用，忽略 */
  }
}

http.interceptors.request.use((config) => {
  const token = getToken();
  if (token) config.headers.token = token;
  return config;
});

/** 业务错误：后端 code !== 1 时抛出，调用方 catch 后读 err.message。 */
export class ApiError extends Error {
  constructor(code, message) {
    super(message || '请求失败');
    this.name = 'ApiError';
    this.code = code;
  }
}

http.interceptors.response.use(
  (response) => {
    const body = response.data;
    // 文件流等非 JSON 响应原样返回
    if (!body || typeof body !== 'object' || typeof body.code === 'undefined') {
      return response;
    }
    if (body.code !== 1) {
      throw new ApiError(body.code, body.msg);
    }
    return response;
  },
  (error) => {
    const body = error.response?.data;
    if (body && typeof body.code === 'number' && body.code !== 1) {
      return Promise.reject(new ApiError(body.code, body.msg));
    }
    const status = error.response?.status;
    const fallback =
      status === 401 || status === 403
        ? '登录状态已失效，请重新登录'
        : status
          ? `服务异常（${status}）`
          : '网络连接失败';
    return Promise.reject(new ApiError(body?.code ?? -1, body?.msg || fallback));
  }
);

/** GET，取 data 字段。 */
export async function get(url, params) {
  const res = await http.get(url, { params });
  return res.data.data;
}

/** POST。body 为对象时按 JSON 发；传 FormData 走文件上传。第三个参数是 axios config。 */
export async function post(url, body, config) {
  const res = await http.post(url, body, config);
  return res.data.data;
}

export async function put(url, body, config) {
  const res = await http.put(url, body, config);
  return res.data.data;
}

export async function del(url, params) {
  const res = await http.delete(url, params === undefined ? undefined : { params });
  return res.data.data;
}

export default http;