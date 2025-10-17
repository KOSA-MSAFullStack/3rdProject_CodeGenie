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

     <!-- 사용자 정보 / 프로필 -->
    <div class="profile-wrapper">
      <div class="profile" @click="toggleMenu" title="내 계정">
        <img class="avatar-img" :src="genieProfile" alt="Profile" />
        <span v-if="username">{{ username }}</span>
        <span v-else>Welcome!</span>
      </div>

      <!-- 드롭다운 메뉴 -->
      <transition name="fade">
        <div v-if="showMenu" class="dropdown" @click.stop>
          <RouterLink to="/mypage" class="dropdown-item">마이페이지</RouterLink>
          <button class="dropdown-item logout" @click="logoutUser">로그아웃</button>
        </div>
      </transition>
    </div>
  </aside>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch } from 'vue'
import { useRoute, useRouter, RouterLink } from 'vue-router' 
import authApi from '../lib/authApi'
import logo from '../assets/logo.png'
import genieProfile from '../assets/profileGenie.png'
import { useAuth } from "@/composables/useAuth";

const route = useRoute()
const router = useRouter()
const { signOut } = useAuth()

const username = ref('') // 사용자 이름
const showMenu = ref(false);

/** 여러 개를 보여줄 문제집 리스트 */
const workbooks = ref([])

/** 최대 항목 수 */
const MAX_ITEMS = 20

/** 활성 탭 체크 */
function isActive(prefix) {
  return route.path.startsWith(prefix)
}

/** 중복 제거 + 순서 유지 */
function dedupKeepOrder(arr) {
  const seen = new Set()
  const out = []
  for (const x of arr) {
    if (x && !seen.has(x.id)) {
      seen.add(x.id)
      out.push(x)
    }
  }
  return out
}

/** 최근 목록을 서버에서 가져와 기존 목록과 병합 (현재 상세를 맨 위로 올리지 않음) */
async function loadWorkbooks() {
  try {
    const token = localStorage.getItem('token')
    if (!token) return

    const { data: recent } = await authApi.get('/workbooks?limit=10&sort=recent')
    const recentArr = Array.isArray(recent) ? recent : (recent ? [recent] : [])
    const mapped = recentArr.map(x => ({ id: x.id, topic: x.topic ?? x.title ?? '' }))

    // 서버 정렬(최근순)을 우선 유지하고, 기존 목록에서 빠진 것만 뒤에 붙임
    const merged = dedupKeepOrder([...mapped, ...workbooks.value])
    workbooks.value = merged.slice(0, MAX_ITEMS)
  } catch {
    // 실패해도 기존 목록 유지
  }
}

/** 새 문제집 생성 이벤트: 이 때만 맨 위에 올림 */
function onWorkbookCreated(e) {
  const { id, topic } = e.detail || {}
  if (!id) return
  workbooks.value = dedupKeepOrder([{ id, topic: topic ?? '' }, ...workbooks.value]).slice(0, MAX_ITEMS)
}

/** 회원 이름 */
async function loadMemberInfo() {
  try {
    const token = localStorage.getItem("token");
    if (!token) {
      username.value = "";
      return;
    }
    const { data } = await authApi.get("/member/info");
    username.value = data.username || "";
  } catch {
    username.value = "";
  }
}

/** 드롭다운 열기/닫기 */
function toggleMenu() {
  showMenu.value = !showMenu.value;
}

/** 로그아웃 */
function logoutUser() {
  signOut();
  localStorage.removeItem("token");
  username.value = "";
  showMenu.value = false;
  workbooks.value = []; // 문제집 목록 초기화

  // Sidebar 상태 갱신
  window.dispatchEvent(new Event("auth:changed"));
  router.push("/login");
}

/** 외부 클릭 시 닫기 */
function handleClickOutside(e) {
  const menu = document.querySelector(".dropdown");
  const profile = document.querySelector(".profile");
  if (menu && profile && !menu.contains(e.target) && !profile.contains(e.target)) {
    showMenu.value = false;
  }
}

onMounted(() => {
  loadWorkbooks();
  loadMemberInfo();
  window.addEventListener("workbook:created", loadWorkbooks);
  window.addEventListener("auth:changed", loadMemberInfo);
  document.addEventListener("click", handleClickOutside);
});

onUnmounted(() => {
  window.removeEventListener("workbook:created", loadWorkbooks);
  window.removeEventListener("auth:changed", loadMemberInfo);
  document.removeEventListener("click", handleClickOutside);
});

/** 라우트가 바뀌면 다시 조회(정렬 유지, 승격 없음) */
watch(() => route.fullPath, loadWorkbooks)
</script>

<style scoped>
.sidebar {
  width: 240px;
  background: #fafafa;
  border-right: 1px solid #eee;
  height: 100vh;
  position: sticky;
  top: 0;
  display: flex;
  flex-direction: column;
  overflow-y: auto;
}
.brand {
  display: block;
  padding: 12px 16px;
}
.brand img {
  height: 60px;
  display: block;
}
nav {
  display: flex;
  flex-direction: column;
  margin-top: 6px;
}
.item {
  padding: 12px 16px;
  color: #333;
  text-decoration: none;
  display: block;
  border-bottom: 1px solid #eee;
}
.item.active {
  background: #eef4ff;
  color: #1a4dd9;
  font-weight: 600;
}
.section {
  padding: 8px 0;
  border-bottom: 1px solid #eee;
}
.section-title {
  font-size: 13px;
  font-weight: 700;
  color: #666;
  padding: 8px 16px 4px 16px;
}
.sublist {
  display: flex;
  flex-direction: column;
  gap: 2px;
  padding: 2px 8px 10px 8px;
}
.subitem {
  display: block;
  margin: 0 8px;
  padding: 8px 8px;
  border-radius: 8px;
  color: #333;
  text-decoration: none;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.subitem:hover {
  background: #f1f3f5;
}
.subitem.active {
  background: #e9f0ff;
  color: #1a4dd9;
  font-weight: 600;
}

/* 프로필 드롭다운 */
.profile-wrapper {
  margin-top: auto;
  position: relative;
  border-top: 1px solid #eee;
}
.profile {
  padding: 12px 16px;
  color: #555;
  cursor: pointer;
  display: flex;
  gap: 8px;
  align-items: center;
  user-select: none;
}
.profile:hover {
  background: #f5f6f7;
}

.avatar-img {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  object-fit: cover;
  background-color: #fff;
  border: 2px solid #e0e0e0;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

/* hover 시 약간 확대 + 살짝 회전 */
.avatar-img:hover {
  transform: scale(1.1) rotate(5deg);
  animation: goldenGlow 1.5s infinite ease-in-out;
}


/* 드롭다운 */
.dropdown {
  position: absolute;
  bottom: 55px;
  left: 16px;
  background: white;
  border: 1px solid #ddd;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  border-radius: 8px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  min-width: 140px;
  z-index: 1000;
}
.dropdown-item {
  padding: 10px 14px;
  text-align: left;
  font-size: 14px;
  color: #333;
  background: none;
  border: none;
  cursor: pointer;
  text-decoration: none;
}
.dropdown-item:hover {
  background: #f2f2f2;
}
.logout {
  color: #d33;
}
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.15s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>