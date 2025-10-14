// src/lib/authService.js
import api from './api';        // 로그인/회원가입용 (토큰 없음)
import authApi from './authApi'; // 인증 필요한 API용 (토큰 자동 첨부)

// 로그인
export const login = async (email, password) => {
  const res = await api.post('/login', { email, password });
  const token = res.headers.authorization || res.headers.Authorization;
  if (token) localStorage.setItem('token', token);
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

export const logout = () => {
  localStorage.removeItem('token');
};
