import { ref } from "vue";
import { login, logout, join } from "@/lib/authService";

const isAuthenticated = ref(!!localStorage.getItem("token"));

export function useAuth() {
  // 로그인
  const signIn = async (email, password) => {
    try {
      const token = await login(email, password);
      if (token) isAuthenticated.value = true;
    } catch (e) {
      alert("로그인 실패: 이메일 또는 비밀번호를 확인하세요.");
    }
  };

  // 회원가입
  const signUp = async (username, email, password) => {
    try {
      await join(username, email, password);
      alert("회원가입이 완료되었습니다.");
    } catch (e) {
      alert("회원가입 실패: 입력값을 확인하세요.");
    }
  };

  // 로그아웃
  const signOut = () => {
    logout();
    isAuthenticated.value = false;
  };

  return { isAuthenticated, signIn, signUp, signOut };
}
