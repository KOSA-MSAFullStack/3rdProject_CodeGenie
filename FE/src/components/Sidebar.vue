<template>
  <aside class="sidebar">
    <!-- 로고 -->
    <RouterLink to="/new" class="brand" title="새 문제로 이동">
      <img :src="logo" alt="CodeGenie" />
    </RouterLink>

    <nav>
      <RouterLink to="/new" class="item" :class="{active: isActive('/new')}">
        새 문제
      </RouterLink>

      <!-- 문제집: 여러 개 누적 노출 -->
      <div class="section">
        <div class="section-title">문제집</div>

        <div v-if="workbooks.length" class="sublist">
          <RouterLink
            v-for="wb in workbooks"
            :key="wb.id"
            :to="`/workbooks/${wb.id}`"
            class="subitem"
            :class="{active: $route.path === `/workbooks/${wb.id}`}"
            :title="wb.topic || '(제목 없음)'"
          >
            {{ wb.topic || '(제목 없음)' }}
          </RouterLink>
        </div>
      </div>

      <div class="section">
        <div class="section-title">저장한 문제</div>
        <div class="sublist"></div>
      </div>
    </nav>

    <RouterLink to="/mypage" class="profile" title="마이페이지">
      <span class="avatar">👤</span> 마이페이지
    </RouterLink>
  </aside>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch } from 'vue'
import { useRoute, RouterLink } from 'vue-router'
import authApi from '../lib/authApi'
import logo from '../assets/logo.png'

const route = useRoute()

/** 여러 개를 보여줄 문제집 리스트 */
const workbooks = ref([])

/** 최대 항목 수 */
const MAX_ITEMS = 20
const STORAGE_KEY = 'cg:sidebar:workbooks'

/** 활성 탭 체크 */
function isActive(prefix) {
  return route.path.startsWith(prefix)
}

/** 중복 제거 + 순서 유지(upsert 유틸) */
function dedupKeepOrder(arr) {
  const seen = new Set()
  const out = []
  for (const x of arr) {
    if (!x) continue
    // id 타입 정규화(문자/숫자 섞여도 동일 취급)
    const key = Number.isNaN(Number(x.id)) ? String(x.id) : String(Number(x.id))
    if (!seen.has(key)) {
      seen.add(key)
      out.push({ id: key, topic: x.topic ?? '' })
    }
  }
  return out
}

/** 저장/불러오기 */
function loadFromStorage() {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    if (!raw) return []
    const parsed = JSON.parse(raw)
    return Array.isArray(parsed) ? dedupKeepOrder(parsed).slice(0, MAX_ITEMS) : []
  } catch {
    return []
  }
}
function saveToStorage(list) {
  try {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(dedupKeepOrder(list).slice(0, MAX_ITEMS)))
  } catch {}
}

/** 최근 목록 + 현재 상세 항목까지 '병합' 로드 (덮어쓰기 금지) */
async function loadWorkbooks() {
  try {
    const token = localStorage.getItem('token')
    if (!token) return

    // 최근 N개 요청
    const listReq = authApi.get(`/workbooks?limit=${MAX_ITEMS}&sort=recent`)

    // 현재 상세라면 우선 포함
    const match = route.path.match(/^\/workbooks\/(\d+)/)
    let current = null
    if (match) {
      const id = match[1]
      const { data } = await authApi.get(`/workbooks/${id}`)
      current = { id: String(data.id), topic: data.topic ?? data.title ?? '' }
    }

    const { data: recent } = await listReq
    const recentArr = Array.isArray(recent) ? recent : (recent ? [recent] : [])
    const mapped = recentArr.map(x => ({ id: String(x.id), topic: x.topic ?? x.title ?? '' }))

    // 1) 서버에서 받은 목록(현재 항목 포함) 우선
    const incoming = dedupKeepOrder(current ? [current, ...mapped] : mapped)

    // 2) 기존(메모리 + localStorage) 병합
    const persisted = loadFromStorage()
    const merged = dedupKeepOrder([...incoming, ...workbooks.value, ...persisted]).slice(0, MAX_ITEMS)

    workbooks.value = merged
    saveToStorage(merged)
  } catch {
    // 실패해도 기존 목록 유지 (덮어쓰기 방지)
  }
}

/** 새 문제집 생성 이벤트를 들으면 즉시 맨 위에 추가(덮어쓰기 X) */
function onWorkbookCreated(e) {
  const { id, topic } = e.detail || {}
  if (!id) return
  const next = dedupKeepOrder([{ id: String(id), topic: topic ?? '' }, ...workbooks.value]).slice(0, MAX_ITEMS)
  workbooks.value = next
  saveToStorage(next)
}

onMounted(() => {
  // localStorage 먼저 복원 → 서버 합치기
  workbooks.value = loadFromStorage()
  loadWorkbooks()
  window.addEventListener('workbook:created', onWorkbookCreated)
})

onUnmounted(() => {
  window.removeEventListener('workbook:created', onWorkbookCreated)
})

/** 라우트 변경 시 재조회(병합) */
watch(() => route.fullPath, () => {
  loadWorkbooks()
})

// workbooks 변경 시 자동 저장(안전망)
watch(workbooks, (v) => saveToStorage(v), { deep: true })
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
.item{padding:12px 16px;color:#333;text-decoration:none;display:block;border-bottom:1px solid #eee}
.item.active{background:#eef4ff;color:#1a4dd9;font-weight:600}
.section{padding:8px 0;border-bottom:1px solid #eee}
.section-title{font-size:13px;font-weight:700;color:#666;padding:8px 16px 4px 16px}
.sublist{display:flex;flex-direction:column;gap:2px;padding:2px 8px 10px 8px}
.subitem{display:block;margin:0 8px;padding:8px 8px;border-radius:8px;color:#333;text-decoration:none;white-space:nowrap;overflow:hidden;text-overflow:ellipsis}
.subitem:hover{background:#f1f3f5}
.subitem.active{background:#e9f0ff;color:#1a4dd9;font-weight:600}
.profile{margin-top:auto;padding:12px 16px;border-top:1px solid #eee;color:#555;text-decoration:none;display:flex;gap:8px;align-items:center}
.avatar{font-size:16px}
</style>