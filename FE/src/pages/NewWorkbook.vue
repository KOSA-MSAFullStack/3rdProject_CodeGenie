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

      <label>요청 상세
        <textarea
          v-model="form.request_detail"
          class="textarea-fixed"
          rows="6"
          placeholder="원하는 문제/개념 요구사항"
        ></textarea>
      </label>

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

import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import authApi from '../lib/authApi'

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
    loading.value = true
    // ✅ 서버 DTO 키(camelCase)로 명시 매핑
    const payload = {
      language: form.language,
      level: form.level,
      style: form.style,
      requestDetail: form.request_detail, // ← 백엔드와 일치
      topic: form.topic,
    }
    const { data } = await authApi.post('/workbooks', payload)
    const id = data?.id
    if (!id) throw new Error('응답에 id 없음')

    // ✅ 사이드바 갱신 이벤트 (라우터 이동보다 먼저)
    window.dispatchEvent(
      new CustomEvent('workbook:created', {
        detail: { id, topic: form.topic || data.topic || '' },
      })
    )

    router.push(`/workbooks/${id}`)
  } catch (e) {
    console.error('문제집 생성 실패:', e)
    alert('문제집 생성에 실패했습니다. 잠시 후 다시 시도하거나 콘솔을 확인하세요.')
  } finally {
    loading.value = false;
  }
}
</script>

<style scoped>
.page{padding:20px}
.form{display:grid;gap:12px;max-width:560px}
input,select,textarea{width:100%;padding:10px;border:1px solid #ddd;border-radius:8px}
button{padding:10px 14px;border:0;background:#1a4dd9;color:#fff;border-radius:8px;cursor:pointer}
button[disabled]{opacity:.6;cursor:not-allowed}
.textarea-fixed{height:140px;resize:none;overflow:auto}
</style>
