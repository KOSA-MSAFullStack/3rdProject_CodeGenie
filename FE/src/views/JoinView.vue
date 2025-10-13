<template>
  <div class="signup-container">
    <div class="signup-box">
      <!-- 제목 -->
      <h2 class="title">Sign Up</h2>

      <!-- 회원가입 폼 -->
      <form @submit.prevent="handleSignup" class="signup-form">
        <div class="input-group">
          <label for="email">이메일</label>
          <input id="email" v-model="email" type="email" placeholder="이메일" />
        </div>

        <div class="input-group">
          <label for="username">이름</label>
          <input id="username" v-model="username" type="text" placeholder="이름" />
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

        <div class="input-group">
          <label for="confirmPassword">비밀번호 확인</label>
          <input
            id="confirmPassword"
            v-model="confirmPassword"
            type="password"
            placeholder="비밀번호 확인"
          />
        </div>

        <button type="submit" class="signup-button">가입하기</button>
      </form>

      <p class="login-text">
        이미 계정이 있으신가요?
        <router-link to="/login" class="login-link">로그인</router-link>
      </p>
    </div>
  </div>
</template>

<script setup>
import { ref } from "vue";
import { useRouter } from "vue-router";
import { useAuth } from "@/composables/useAuth";

const router = useRouter();
const { signUp } = useAuth();

const email = ref("");
const username = ref("");
const password = ref("");
const confirmPassword = ref("");

const handleSignup = async () => {
  if (password.value !== confirmPassword.value) {
    alert("비밀번호가 일치하지 않습니다.");
    return;
  }

  await signUp(email.value, username.value, password.value);
  router.push("/login");
};
</script>

<style scoped>
/* 전체 컨테이너: 로그인과 동일한 구조 */
.signup-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: calc(100vh - 60px);
  background-color: #ffffff;
}

/* 메인 박스 */
.signup-box {
  width: 480px;
  background: #ffffff;
  border-radius: 12px;
  padding: 60px 50px;
  text-align: center;
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.1);
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
.signup-form {
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

/* 버튼 */
.signup-button {
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

.signup-button:hover {
  background-color: #1f1f1f;
}

/* 하단 텍스트 */
.login-text {
  margin-top: 20px;
  font-size: 13px;
  color: #8c8c8c;
}

.login-link {
  color: #2c2c2c;
  text-decoration: underline;
  margin-left: 4px;
}
</style>
