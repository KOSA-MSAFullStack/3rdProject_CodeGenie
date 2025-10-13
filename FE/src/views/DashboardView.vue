<template>
  <div>
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
