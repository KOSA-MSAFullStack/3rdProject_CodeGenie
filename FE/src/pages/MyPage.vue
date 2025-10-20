<template>
  <div class="mypage-container">
    <div class="mypage-box">
      <h2 class="title">내 정보 수정</h2>

      <!-- 이름 변경 섹션 -->
      <div class="section">
        <h3 class="subtitle">이름 변경</h3>
        <form @submit.prevent="updateName" class="mypage-form">
          <div class="input-group">
            <label for="email">이메일</label>
            <input id="email" v-model="email" type="email" disabled class="readonly" />
          </div>

          <div class="input-group">
            <label for="username">이름</label>
            <input
              id="username"
              v-model="username"
              type="text"
              placeholder="새 이름 입력"
            />
          </div>

          <div class="input-group">
            <label for="currentPasswordName">현재 비밀번호</label>
            <input
              id="currentPasswordName"
              v-model="currentPasswordName"
              type="password"
              placeholder="현재 비밀번호 입력"
            />
          </div>

          <button type="submit" class="update-button">이름 변경</button>
        </form>
      </div>

      <hr class="divider" />

      <!-- 비밀번호 변경 섹션 -->
      <div class="section">
        <h3 class="subtitle">비밀번호 변경</h3>
        <form @submit.prevent="updatePassword" class="mypage-form">
          <div class="input-group">
            <label for="currentPasswordPw">현재 비밀번호</label>
            <input
              id="currentPasswordPw"
              v-model="currentPasswordPw"
              type="password"
              placeholder="현재 비밀번호 입력"
            />
          </div>

          <div class="input-group">
            <label for="newPassword">새 비밀번호</label>
            <input
              id="newPassword"
              v-model="newPassword"
              type="password"
              placeholder="새 비밀번호 입력"
            />
          </div>

          <div class="input-group">
            <label for="confirmPassword">새 비밀번호 확인</label>
            <input
              id="confirmPassword"
              v-model="confirmPassword"
              type="password"
              placeholder="새 비밀번호 확인"
            />
          </div>

          <button type="submit" class="update-button">비밀번호 변경</button>
        </form>
      </div>

      <p v-if="message" class="msg">{{ message }}</p>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import authApi from "@/lib/authApi";

const email = ref("");
const username = ref("");

// 이름 변경용 비밀번호
const currentPasswordName = ref("");

// 비밀번호 변경용
const currentPasswordPw = ref("");
const newPassword = ref("");
const confirmPassword = ref("");

const message = ref("");

// 회원정보 불러오기
onMounted(async () => {
  try {
    const res = await authApi.get("/member/info");
    email.value = res.data.email;
    username.value = res.data.username;
  } catch (e) {
    message.value = "회원 정보를 불러올 수 없습니다.";
  }
});

// 이름 변경
const updateName = async () => {
  if (!currentPasswordName.value) {
    message.value = "현재 비밀번호를 입력해주세요.";
    return;
  }

  try {
    const res = await authApi.put("/member/update", {
      username: username.value,
      currentPassword: currentPasswordName.value,
    });
    message.value = res.data || "이름이 변경되었습니다.";
    currentPasswordName.value = "";
  } catch (e) {
    message.value = e.response?.data || "이름 변경 실패. 다시 시도해주세요.";
  }
};

// 비밀번호 변경
const updatePassword = async () => {
  if (!currentPasswordPw.value || !newPassword.value || !confirmPassword.value) {
    message.value = "모든 비밀번호 입력란을 채워주세요.";
    return;
  }

  if (newPassword.value !== confirmPassword.value) {
    message.value = "새 비밀번호가 일치하지 않습니다.";
    return;
  }

  try {
    const res = await authApi.put("/member/update", {
      currentPassword: currentPasswordPw.value,
      newPassword: newPassword.value,
    });
    message.value = res.data || "비밀번호가 변경되었습니다.";

    currentPasswordPw.value = "";
    newPassword.value = "";
    confirmPassword.value = "";
  } catch (e) {
    message.value = e.response?.data || "비밀번호 변경 실패. 다시 시도해주세요.";
  }
};
</script>

<style scoped>
.mypage-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: calc(100vh - 60px);
  background-color: #ffffff;
}

.mypage-box {
  width: 500px;
  background: #ffffff;
  border-radius: 12px;
  padding: 50px 40px;
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.1);
}

.title {
  font-family: Inter, sans-serif;
  font-size: 24px;
  font-weight: 600;
  color: #000;
  text-align: center;
  margin-bottom: 30px;
}

.subtitle {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin-bottom: 15px;
}

.section {
  margin-bottom: 30px;
}

.divider {
  border: none;
  border-top: 1px solid #ddd;
  margin: 25px 0;
}

.mypage-form {
  display: flex;
  flex-direction: column;
  gap: 15px;
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
  height: 40px;
  border: 1px solid #000;
  border-radius: 6px;
  padding: 0 10px;
  font-size: 14px;
}

.readonly {
  background-color: #f5f5f5;
  color: #666;
}

.update-button {
  width: 100%;
  height: 42px;
  background-color: #14ae5c;
  color: #fff;
  font-size: 14px;
  font-weight: 500;
  border: none;
  border-radius: 6px;
  margin-top: 10px;
  cursor: pointer;
  transition: background 0.2s ease;
}

.update-button:hover {
  background-color: #0e8d4a;
}

.msg {
  margin-top: 20px;
  font-size: 13px;
  color: #555;
  text-align: center;
}
</style>
