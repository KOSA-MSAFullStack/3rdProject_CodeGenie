import axios from 'axios'

const api = axios.create({
  baseURL: '/api', // vite proxy로 백엔드(예: http://localhost:8080)로 전달
  timeout: 120000,
  headers: {
    "Content-Type": "application/json",
  },
})

export default api