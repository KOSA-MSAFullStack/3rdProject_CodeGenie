import api from "./api";

export const login = async (email, password) => {
  const response = await api.post("/login", { email, password }); // /api/login 으로 proxy됨
  const token = response.headers["authorization"];
  if (token) localStorage.setItem("token", token);
  return token;
};

export const join = async (email, username, password) => {
  return await api.post("/join", { email, username, password }); // /api/join 으로 proxy됨
};

export const logout = () => {
  localStorage.removeItem("token");
};
