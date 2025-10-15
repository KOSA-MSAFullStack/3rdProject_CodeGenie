// src/lib/authApi.js
import axios from 'axios';

const authApi = axios.create({
  baseURL: '/api',
  timeout: 120000,
  headers: { 'Content-Type': 'application/json' },
  withCredentials: true, 
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

// // 응답 인터셉터 (401 처리)
// authApi.interceptors.response.use(
//   (res) => res,
//   (err) => {
//     if (err?.response?.status === 401) {
//       localStorage.removeItem('token');
//       // 필요하면 라우터로 로그인 페이지 이동
//       // window.location.href = '/login';
//     }
//     return Promise.reject(err);
//   }
// );

// 응답 인터셉터 (401 처리)
authApi.interceptors.response.use(
  (res) => res,
  async (err) => {
    if (err?.response?.status === 401) {
      try {
        const reissueRes = await axios.post('/api/reissue', {}, { withCredentials: true });
        const newToken = reissueRes.headers['authorization'];
        if (newToken) {
          localStorage.setItem('token', newToken);
          err.config.headers['Authorization'] = newToken;
          return authApi.request(err.config); // 원래 요청 재시도
        }
      } catch (refreshErr) {
        localStorage.removeItem('token');
        window.location.href = '/login';
      }
    }
    return Promise.reject(err);
  }
);

// 로그아웃 요청
export const logout = async () => {
  try {
    await authApi.post('/logout', {}, { withCredentials: true });
    localStorage.removeItem('token'); // Access Token 삭제
    window.location.href = '/login';
  } catch (err) {
    console.error('Logout failed', err);
  }
};


export default authApi;
