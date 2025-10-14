<template>
  <div class="workbook-container">
    <div class="workbook-box">
      <h2 class="title">새 문제 생성</h2>

      <form @submit.prevent="create" class="workbook-form">
        <!-- 첫 번째 줄: 언어 + 레벨 -->
        <div class="input-row">
          <div class="input-group">
            <label>프로그래밍 언어</label>
            <input v-model="form.language" placeholder="예: Java, Python" />
          </div>

          <div class="input-group">
            <label>레벨</label>
            <select v-model="form.level">
              <option>초급</option>
              <option>중급</option>
              <option>고급</option>
            </select>
          </div>
        </div>

        <!-- 두 번째 줄부터 기존대로 -->
        <div class="input-group">
          <label>학습 스타일</label>
          <select v-model="form.style">
            <option>간단요약</option>
            <option>깊이설명</option>
            <option>예시중심</option>
          </select>
        </div>

        <div class="input-group">
          <label>요청 상세</label>
          <textarea
            v-model="form.request_detail"
            class="textarea-fixed"
            placeholder="원하는 문제/개념 요구사항을 입력하세요."
          ></textarea>
        </div>

        <div class="input-group">
          <label>학습 주제명</label>
          <input v-model="form.topic" placeholder="예: Java 문자열 입문" />
        </div>

        <button type="submit" class="workbook-button" :disabled="loading">
          {{ loading ? '생성 중...' : '문제집 생성' }}
        </button>
      </form>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from "vue";
import { useRouter } from "vue-router";
import api from "../lib/api";

const router = useRouter();
const loading = ref(false);
const form = reactive({
  language: "",
  level: "초급",
  style: "간단요약",
  request_detail: "",
  topic: "",
});

async function create() {
  try {
    loading.value = true;
    const { data } = await api.post("/workbooks", { ...form });
    const id = data?.id;
    if (!id) throw new Error("응답에 id 없음");
    router.push(`/workbooks/${id}`);
  } catch (e) {
    console.warn("임시 폴백 사용:", e);
    router.push("/workbooks/1");
  } finally {
    loading.value = false;
  }
}
</script>

<style scoped>
/* === 레이아웃 === */
.workbook-container {
  display: flex;
  justify-content: center;
  align-items: flex-start;
  min-height: calc(100vh - 56px);
  padding-top: 40px;
  background-color: #ffffff;
}

/* === 메인 박스 === */
.workbook-box {
  width: 480px;
  background: #ffffff;
  border-radius: 12px;
  padding: 60px 50px;
  text-align: center;
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.1);
}

/* === 제목 === */
.title {
  font-family: Inter, sans-serif;
  font-size: 24px;
  font-weight: 600;
  color: #000;
  margin-bottom: 30px;
}

/* === 폼 === */
.workbook-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* 언어 + 레벨 한 줄 */
.input-row {
  display: flex;
  gap: 16px;
  flex-wrap: wrap; /* 모바일 대응 */
}

.input-row .input-group {
  flex: 1; /* 두 칸 동일한 비율 */
  min-width: 180px;
}

/* === 입력 그룹 === */
.input-group {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}

.input-group label {
  font-size: 13px;
  color: #8c8c8c;
  margin-bottom: 6px;
}

/* 공통 input/select 스타일 (textarea 제외) */
.input-group input,
.input-group select {
  width: 100%;
  border: 1px solid #000;
  border-radius: 6px;
  padding: 10px 12px;
  font-size: 14px;
  font-family: Inter, sans-serif;
  line-height: 1.5;
  height: 42px;
  box-sizing: border-box;
  appearance: none;
  background-color: #fff;
}

/* select 전용 화살표 커스텀 */
.input-group select {
  background-image: url("data:image/svg+xml,%3Csvg viewBox='0 0 140 140' xmlns='http://www.w3.org/2000/svg'%3E%3Cpolygon points='20,50 70,100 120,50' fill='%23000'/%3E%3C/svg%3E");
  background-repeat: no-repeat;
  background-position: right 12px center;
  background-size: 10px;
  cursor: pointer;
}

/* === 요청 상세 textarea === */
.input-group textarea.textarea-fixed {
  width: 100%;
  border: 1px solid #000;
  border-radius: 6px;
  padding: 10px 12px;
  font-size: 14px;
  font-family: Inter, sans-serif;
  line-height: 1.5;
  box-sizing: border-box;
  background-color: #fff;

  height: 120px;
  resize: none;
  overflow-y: auto;
}

/* === 버튼 === */
.workbook-button {
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

.workbook-button:hover {
  background-color: #1f1f1f;
}

.workbook-button[disabled] {
  opacity: 0.6;
  cursor: not-allowed;
}
</style>
