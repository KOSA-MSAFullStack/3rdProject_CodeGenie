<template>
  <div class="mypage-container">
    <div class="mypage-box">
      <!-- 제목 -->
      <h2 class="title">내 정보 수정</h2>

      <!-- 회원정보 수정 폼 -->
      <form @submit.prevent="updateProfile" class="mypage-form">
        <!-- 이메일 (읽기 전용) -->
        <div class="input-group">
          <label for="email">이메일</label>
          <input id="email" v-model="email" type="email" disabled class="readonly" />
        </div>

        <!-- 이름 -->
        <div class="input-group">
          <label for="username">이름</label>
          <input id="username" v-model="username" type="text" placeholder="이름 변경 가능" />
        </div>

        <!-- 현재 비밀번호 -->
        <div class="input-group">
          <label for="currentPassword">현재 비밀번호</label>
          <input
            id="currentPassword"
            v-model="currentPassword"
            type="password"
            placeholder="현재 비밀번호"
          />
        </div>

        <!-- 새 비밀번호 -->
        <div class="input-group">
          <label for="newPassword">새 비밀번호</label>
          <input
            id="newPassword"
            v-model="newPassword"
            type="password"
            placeholder="새 비밀번호"
          />
        </div>

        <!-- 새 비밀번호 확인 -->
        <div class="input-group">
          <label for="confirmPassword">새 비밀번호 확인</label>
          <input
            id="confirmPassword"
            v-model="confirmPassword"
            type="password"
            placeholder="새 비밀번호 확인"
          />
        </div>

        <button type="submit" class="update-button">변경</button>
      </form>

      <p v-if="message" class="msg">{{ message }}</p>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import authApi from "@/lib/authApi";

const email = ref("");
const username = ref("");
const currentPassword = ref("");
const newPassword = ref("");
const confirmPassword = ref("");
const message = ref("");

onMounted(async () => {
  try {
    const res = await authApi.get("/member/info");
    email.value = res.data.email;
    username.value = res.data.username;
  } catch (e) {
    message.value = "회원 정보를 불러올 수 없습니다.";
  }
});

const updateProfile = async () => {
  if (newPassword.value !== confirmPassword.value) {
    message.value = "비밀번호가 일치하지 않습니다.";
    return;
  }

  try {
    const res = await authApi.put("/member/update", {
      username: username.value,
      currentPassword: currentPassword.value,
      newPassword: newPassword.value,
    });
    message.value = res.data || "회원정보가 변경되었습니다.";
  } catch (e) {
    message.value = e.response?.data || "수정 실패. 다시 시도해주세요.";
  }
};
</script>

<style scoped>
/* 전체 컨테이너 */
.mypage-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: calc(100vh - 60px);
  background-color: #ffffff;
}

/* 메인 박스 */
.mypage-box {
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
.mypage-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* 입력 필드 */
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

/* 읽기 전용 필드 */
.readonly {
  background-color: #f5f5f5;
  color: #666;
}

/* 버튼 */
.update-button {
  width: 100%;
  height: 46px;
  background-color: #14ae5c; /* 기존 초록색 유지 */
  color: #fff;
  font-size: 14px;
  font-weight: 500;
  border: none;
  border-radius: 6px;
  margin-top: 15px;
  cursor: pointer;
  transition: background 0.2s ease;
}

.update-button:hover {
  background-color: #0e8d4a;
}

/* 메시지 */
.msg {
  margin-top: 20px;
  font-size: 13px;
  color: #555;
}
</style>
