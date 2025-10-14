<template>
  <div class="dashboard">
    <h2>대시보드</h2>
    <p>로그인 성공! JWT 토큰이 유효합니다 🎉</p>
    <button @click="logoutUser">로그아웃</button>
  </div>
</template>

<script setup>
import { useRouter } from "vue-router";
import { useAuth } from "@/composables/useAuth";

const router = useRouter();
const { signOut } = useAuth();

const logoutUser = () => {
  signOut();
  router.push("/login");
};
</script>

<style scoped>
.dashboard {
  padding-left: 40px; /* 왼쪽 여백 */
  padding-top: 20px; /* 위쪽 여백 */
}

button {
  margin-top: 16px;
  padding: 8px 14px;
  border: none;
  border-radius: 6px;
  background-color: #4caf50;
  color: white;
  cursor: pointer;
}

button:hover {
  background-color: #45a049;
}
</style>
