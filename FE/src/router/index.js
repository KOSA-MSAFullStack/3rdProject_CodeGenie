import { createRouter, createWebHistory } from 'vue-router'

// 학습 페이지
import NewWorkbook from '../pages/NewWorkbook.vue'
import WorkbooksDetail from '../pages/Workbooks.vue'
import SavedQuizzes from '../pages/SavedQuizzes.vue'
import MyPage from '../pages/MyPage.vue'

// 인증 관련 페이지
import LoginView from '@/views/LoginView.vue'
import JoinView from '@/views/JoinView.vue'
import DashboardView from '@/views/DashboardView.vue' // 로그인 후 홈(테스트용)

// 라우터 정의
const routes = [
  // ------------------------------
  // 인증 관련
  // ------------------------------
  { path: '/login', component: LoginView, meta: { title: '로그인' } },
  { path: '/join', component: JoinView, meta: { title: '회원가입' } },
  {
    path: '/dashboard',
    component: DashboardView,
    meta: { title: '대시보드', requiresAuth: true },
  },

  // ------------------------------
  // CodeGenie 학습 관련
  // ------------------------------
  { path: '/', redirect: '/new' },
  { path: '/new', component: NewWorkbook, meta: { title: '새 학습', requiresAuth: true } },
  // 상세 페이지 (문제집 보기)
  { path: '/workbooks/:id', component: WorkbooksDetail, meta: { title: '문제집', requiresAuth: true } },
  { path: '/saved', component: SavedQuizzes, meta: { title: '저장한 문제', requiresAuth: true } },
  { path: '/mypage', component: MyPage, meta: { title: '마이페이지', requiresAuth: true } },

  // 존재하지 않는 페이지 처리 (optional)
  { path: '/:pathMatch(.*)*', redirect: '/login' },
]

// ------------------------------
// 라우터 생성
// ------------------------------
const router = createRouter({
  history: createWebHistory(),
  routes,
})

// ------------------------------
// 인증 라우터 가드
// ------------------------------
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')

  // 로그인이나 회원가입은 인증 불필요
  if (to.path === '/login' || to.path === '/join') {
    if (token && to.path === '/login') {
      // 이미 로그인된 상태에서 /login 접근 시 대시보드로 리다이렉트
      return next('/dashboard')
    }
    return next()
  }

  // 인증 필요한 페이지 접근 시
  if (to.meta.requiresAuth && !token) {
    return next('/login')
  }

  next()
})

// ------------------------------
// 페이지 제목 동기화
// ------------------------------
router.afterEach((to) => {
  document.title = `CodeGenie - ${to.meta?.title ?? ''}`
})

export default router
