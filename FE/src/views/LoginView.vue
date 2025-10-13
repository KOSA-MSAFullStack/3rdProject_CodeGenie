<template>
  <div class="login-container">
    <div class="login-box">
      <!-- 이미지 -->
      <img src="@/assets/Genie.png" alt="Genie Illustration" class="illustration" />

      <!-- 제목 -->
      <h2 class="title">Login</h2>

      <!-- 로그인 폼 -->
      <form @submit.prevent="handleLogin" class="login-form">
        <div class="input-group">
          <label for="email">이메일</label>
          <input id="email" v-model="email" type="email" placeholder="이메일" />
        </div>

        <div class="input-group">
          <label for="password">비밀번호</label>
          <input
            id="password"
            v-model="password"
            type="password"
            placeholder="비밀번호"
          />
        </div>

        <button type="submit" class="login-button">로그인</button>
      </form>

      <p class="signup-text">
        아직 계정이 없으신가요?
        <router-link to="/join" class="signup-link">회원가입</router-link>
      </p>
    </div>
  </div>
</template>

<script setup>
import { ref } from "vue";
import { useRouter } from "vue-router";
import { useAuth } from "@/composables/useAuth";

const router = useRouter();
const { signIn } = useAuth();

const email = ref("");
const password = ref("");

const handleLogin = async () => {
  await signIn(email.value, password.value);
  router.push("/new");
};
</script>

<style scoped>
/* 전체 화면에서 중앙 정렬 */
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: calc(100vh - 60px); /* 헤더 제외 */
  background-color: #ffffff;
}

/* 로그인 박스: 넓게, 여백 많게 */
.login-box {
  width: 480px; /* 기존보다 넓게 */
  background: #ffffff;
  border-radius: 12px;
  padding: 60px 50px; /* 내부 여백 늘림 */
  text-align: center;
}

/* 상단 이미지 */
.illustration {
  width: 160px; /* 이미지 크기 약간 확대 */
  height: auto;
  display: block;
  margin: 0 auto 25px auto; /* 중앙 정렬 + 여백 */
  object-fit: contain;
}

/* 제목 */
.title {
  font-family: Inter, sans-serif;
  font-size: 24px;
  font-weight: 600;
  color: #000;
  margin-bottom: 30px;
}

/* 폼 */
.login-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.input-group {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}

.input-group label {
  font-size: 13px;
  color: #8c8c8c;
  margin-bottom: 5px;
}

.input-group input {
  width: 100%;
  height: 42px;
  border: 1px solid #000;
  border-radius: 6px;
  padding: 0 12px;
  font-size: 14px;
}

/* 로그인 버튼 */
.login-button {
  width: 100%;
  height: 46px;
  background-color: #2c2c2c;
  color: #f5f5f5;
  font-size: 14px;
  font-weight: 500;
  border: none;
  border-radius: 6px;
  margin-top: 15px;
  cursor: pointer;
  transition: background 0.2s ease;
}

.login-button:hover {
  background-color: #1f1f1f;
}

/* 회원가입 안내 */
.signup-text {
  margin-top: 20px;
  font-size: 13px;
  color: #8c8c8c;
}

.signup-link {
  color: #2c2c2c;
  text-decoration: underline;
  margin-left: 4px;
}
</style>
