import api from "./api";

api.interceptors.request.use((config) => {
  const token = localStorage.getItem("token");
  if (token) {
    config.headers.Authorization = token; // "Bearer ..." 포함
  }
  return config;
});

export default api;
