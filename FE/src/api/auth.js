import axios from "./axios";

export const login = async (email, password) => {
  const response = await axios.post("/api/login", { email, password });
  const token = response.headers["authorization"]; // 헤더에서 토큰 추출
  if (token) localStorage.setItem("token", token);
  return token;
};

export const join = async (username, email, password) => {
  return await axios.post("/api/join", { username, email, password });
};

export const logout = () => {
  localStorage.removeItem("token");
};
