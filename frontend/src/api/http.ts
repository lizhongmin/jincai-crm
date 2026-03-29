import axios from 'axios';
import { useAuthStore } from '../stores/auth';

const TRACE_HEADER_NAME = 'X-Trace-Id';

function createTraceId(): string {
  if (typeof crypto !== 'undefined' && typeof crypto.randomUUID === 'function') {
    return crypto.randomUUID().replaceAll('-', '');
  }
  const fallback = `${Date.now().toString(16)}${Math.random().toString(16).slice(2)}${Math.random().toString(16).slice(2)}`;
  return fallback.slice(0, 32);
}

function readHeaderValue(
  headers: { get?: (name: string) => string | undefined } | Record<string, string | undefined> | undefined,
  headerName: string
): string | undefined {
  if (!headers) {
    return undefined;
  }
  if (typeof headers.get === 'function') {
    return headers.get(headerName) ?? headers.get(headerName.toLowerCase());
  }
  const matchedKey = Object.keys(headers).find((key) => key.toLowerCase() === headerName.toLowerCase());
  return matchedKey ? headers[matchedKey] : undefined;
}

/**
 * HTTP 客户端实例配置
 * 基于 axios 封装，提供统一的 API 请求处理
 *
 * 配置说明：
 * - baseURL: 所有请求的基础路径，代理到后端服务
 * - timeout: 请求超时时间（20秒）
 *
 * 拦截器功能：
 * 1. 请求拦截器：自动添加 JWT 认证头
 * 2. 响应拦截器：统一处理 401 未认证错误
 */
const http = axios.create({
  baseURL: '/api',      // 后端 API 基础路径
  timeout: 20000        // 请求超时时间：20秒
});

/**
 * 请求拦截器
 * 职责：
 * 1. 为已登录用户自动添加 Authorization 头
 * 2. 确保所有请求携带有效的 JWT 令牌
 */
http.interceptors.request.use((config) => {
  config.headers = config.headers ?? {};

  const auth = useAuthStore();
  if (auth.token) {
    // 添加 JWT 认证头
    config.headers.Authorization = `Bearer ${auth.token}`;
  }

  if (!readHeaderValue(config.headers, TRACE_HEADER_NAME)) {
    config.headers[TRACE_HEADER_NAME] = createTraceId();
  }
  return config;
});

/**
 * 响应拦截器
 * 职责：
 * 1. 统一处理 401 未认证错误（自动登出并跳转到登录页）
 * 2. 传递其他错误给具体的 API 调用处理
 */
http.interceptors.response.use(
  (response) => response,
  (error) => {
    // 处理 401 未认证错误
    if (error.response?.status === 401) {
      const auth = useAuthStore();
      // 清除本地认证信息
      auth.logout();
      // 如果当前不在登录页，则跳转到登录页
      if (location.pathname !== '/login') {
        location.href = '/login';
      }
    }
    // 不在拦截器中全局调用 notifyError(error)
    // 因为具体的 API 调用通常会在 catch 块中处理错误
    // 这样可以避免重复显示错误消息
    return Promise.reject(error);
  }
);

export default http;
