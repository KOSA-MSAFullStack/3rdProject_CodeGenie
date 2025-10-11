<template>
  <div class="page">
    <h2>새 문제</h2>
    <form class="form" @submit.prevent="create">
      <label>프로그래밍 언어
        <input v-model="form.language" placeholder="예: Java, Python" />
      </label>

      <label>레벨
        <select v-model="form.level">
          <option>초급</option><option>중급</option><option>고급</option>
        </select>
      </label>

      <label>학습 스타일
        <select v-model="form.style">
          <option>간단요약</option><option>깊이설명</option><option>예시중심</option>
        </select>
      </label>

      <label>요청 상세
        <textarea v-model="form.request_detail"
                  class="textarea-fixed"
                  rows="6"
                  placeholder="원하는 문제/개념 요구사항"></textarea>
      </label>

      <label>학습 주제명
        <input v-model="form.topic" placeholder="예: Java 문자열 입문" />
      </label>

      <button type="submit" :disabled="loading">
        {{ loading ? '생성 중...' : '문제집 생성' }}
      </button>
    </form>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import api from '../lib/api'

const router = useRouter()
const loading = ref(false)
const form = reactive({
  language: '',
  level: '초급',
  style: '간단요약',
  request_detail: '',
  topic: '',
})

// 실제: 백엔드에서 저장 + OpenAI로 10문제 생성 → id 반환
async function create() {
  try {
    loading.value = true
    const { data } = await api.post('/workbooks', { ...form })
    const id = data?.id
    if (!id) throw new Error('응답에 id 없음')
    router.push(`/workbooks/${id}`)
  } catch (e) {
    // 백엔드 준비 전 임시 폴백 (나중에 삭제)
    console.warn('임시 폴백 사용:', e)
    router.push('/workbooks/1')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.page{padding:20px}
.form{display:grid;gap:12px;max-width:560px}
input,select,textarea{width:100%;padding:10px;border:1px solid #ddd;border-radius:8px}
button{padding:10px 14px;border:0;background:#1a4dd9;color:#fff;border-radius:8px;cursor:pointer}
button[disabled]{opacity:.6;cursor:not-allowed}

/* 텍스트 영역: 고정 높이 + 스크롤 + 리사이즈 금지 */
.textarea-fixed{
  height:140px;
  resize:none;
  overflow:auto;
}
</style>
