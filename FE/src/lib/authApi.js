// src/lib/authApi.js
import axios from 'axios';

const authApi = axios.create({
  baseURL: '/api',
  timeout: 120000,
  headers: { 'Content-Type': 'application/json' },
});

// 요청 인터셉터
authApi.interceptors.request.use((config) => {
  // 로그인/회원가입은 절대 토큰 안 붙임(방어로직)
  const url = config.url || '';
  if (url.startsWith('/login') || url.startsWith('/join')) {
    return config;
  }

  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = token; // "Bearer ..." 그대로
  }
  return config;
});

// 응답 인터셉터 (401 처리)
authApi.interceptors.response.use(
  (res) => res,
  (err) => {
    if (err?.response?.status === 401) {
      localStorage.removeItem('token');
      // 필요하면 라우터로 로그인 페이지 이동
      // window.location.href = '/login';
    }
    return Promise.reject(err);
  }
);

export default authApi;
