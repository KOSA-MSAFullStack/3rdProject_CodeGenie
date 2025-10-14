<template>
  <aside class="sidebar">
    <!-- ✅ 로고: 클릭 시 새 문제(/new)로 이동 -->
    <RouterLink to="/new" class="brand" title="새 문제로 이동">
      <img :src="logo" alt="CodeGenie" />
    </RouterLink>

    <nav>
      <!-- ✅ 새 문제 -->
      <RouterLink to="/new" class="item" :class="{active: isActive('/new')}">
        새 문제
      </RouterLink>

      <!-- 문제집: 하나만 노출 (현재 열람 or 최근 1건) -->
      <div class="section">
        <div class="section-title">문제집</div>

        <div v-if="workbook" class="sublist">
          <RouterLink
            :to="`/workbooks/${workbook.id}`"
            class="subitem"
            :class="{active: $route.path === `/workbooks/${workbook.id}`}"
            :title="workbook.topic || '(제목 없음)'"
          >
            {{ workbook.topic || '(제목 없음)' }}
          </RouterLink>
        </div>
        <!-- 아직 문제집이 없다면 아무 것도 렌더링하지 않음 -->
      </div>

      <!-- 저장한 문제: UI만 남겨두고 항목은 비움(비동기 추가 예정) -->
      <div class="section">
        <div class="section-title">저장한 문제</div>
        <div class="sublist"><!-- 비워둠 --></div>
      </div>
    </nav>

    <!-- 마이페이지 -->
    <RouterLink to="/mypage" class="profile" title="마이페이지">
      <span class="avatar">👤</span> 마이페이지
    </RouterLink>
  </aside>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, RouterLink } from 'vue-router'
import api from '../lib/api'
import logo from '../assets/logo.png' // ✅ 프로젝트의 로고 이미지 경로

const route = useRoute()

// 하나만 보여줄 문제집(현재 or 최근 1건)
const workbook = ref(null)
const currentWorkbookId = ref(null)

const currentWorkbookLink = computed(() =>
  currentWorkbookId.value ? `/workbooks/${currentWorkbookId.value}` : '/new'
)

function isActive(prefix) {
  return route.path.startsWith(prefix)
}

/** 비동기 로딩: 현재 라우트 기준으로 하나만 가져오기 */
async function loadWorkbook() {
  try {
    // /workbooks/:id 라우트면 해당 id 로드
    const match = route.path.match(/^\/workbooks\/(\d+)/)
    if (match) {
      const id = Number(match[1])
      const { data } = await api.get(`/workbooks/${id}`)
      workbook.value = { id: data.id, topic: data.topic ?? data.title ?? '' }
      currentWorkbookId.value = data.id
      return
    }

    // 아니면 최근 1건만
    const { data } = await api.get('/workbooks?limit=1&sort=recent')
    const latest = Array.isArray(data) ? data[0] : data
    if (latest) {
      workbook.value = { id: latest.id, topic: latest.topic ?? latest.title ?? '' }
      currentWorkbookId.value = latest.id
    } else {
      workbook.value = null
      currentWorkbookId.value = null
    }
  } catch {
    // 백엔드 준비 전엔 표시 안 함
    workbook.value = null
    currentWorkbookId.value = null
  }
}

onMounted(loadWorkbook)
watch(() => route.fullPath, loadWorkbook)
</script>

<style scoped>
.sidebar{
  width:240px;
  background:#fafafa;
  border-right:1px solid #eee;
  height:100vh;
  position:sticky;
  top:0;
  display:flex;
  flex-direction:column;
  overflow-y:auto;
}
.brand{display:block;padding:12px 16px}
.brand img{height:60px;display:block}
nav{display:flex;flex-direction:column;margin-top:6px}

/* 상위 항목 */
.item{
  padding:12px 16px;
  color:#333;
  text-decoration:none;
  display:block;
  border-bottom:1px solid #eee;
}
.item.active{background:#eef4ff;color:#1a4dd9;font-weight:600}

/* 섹션 */
.section{padding:8px 0;border-bottom:1px solid #eee}
.section-title{
  font-size:13px;
  font-weight:700;
  color:#666;
  padding:8px 16px 4px 16px;
}

/* 소제목 리스트 */
.sublist{display:flex;flex-direction:column;gap:2px;padding:2px 8px 10px 8px}
.subitem{
  display:block;
  margin:0 8px;
  padding:8px 8px;
  border-radius:8px;
  color:#333;text-decoration:none;
  white-space:nowrap; overflow:hidden; text-overflow:ellipsis;
}
.subitem:hover{background:#f1f3f5}
.subitem.active{background:#e9f0ff;color:#1a4dd9;font-weight:600}

/* 하단 */
.profile{
  margin-top:auto;
  padding:12px 16px;
  border-top:1px solid #eee;
  color:#555;
  text-decoration:none;
  display:flex; gap:8px; align-items:center;
}
.avatar{font-size:16px}
</style>