// src/lib/authService.js
import api from './api';        // 로그인/회원가입용 (토큰 없음)
import authApi from './authApi'; // 인증 필요한 API용 (토큰 자동 첨부)

// 로그인 (쿠키 저장 위해 withCredentials 추가)
export const login = async (email, password) => {
  const res = await api.post(
    '/login',
    { email, password },
    { withCredentials: true } // Refresh 쿠키를 받기 위해 반드시 필요
  );

  const token = res.headers.authorization || res.headers.Authorization;
  if (token) localStorage.setItem('token', token); // Access Token 저장
  return token;
};

// 회원가입
export const join = async (email, username, password) => {
  return await api.post('/join', { email, username, password });
};

// 회원정보 조회
export const getMemberInfo = async () => {
  const res = await authApi.get('/member/info');
  return res.data;
};

// 로그아웃
export const logout = () => {
  localStorage.removeItem('token');
};
