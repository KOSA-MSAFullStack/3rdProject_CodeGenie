import { createRouter, createWebHistory } from 'vue-router'
import NewWorkbook from '../pages/NewWorkbook.vue'
import WorkbooksDetail from '../pages/Workbooks.vue'
import SavedQuizzes from '../pages/SavedQuizzes.vue'
import MyPage from '../pages/MyPage.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', redirect: '/new' },
    { path: '/new', component: NewWorkbook, meta: { title: '새 학습' } },
    // 상세는 /workbooks/:id
    { path: '/workbooks/:id', component: WorkbooksDetail, meta: { title: '문제집' } },
    { path: '/saved', component: SavedQuizzes, meta: { title: '저장한 문제' } },
    { path: '/mypage', component: MyPage, meta: { title: '마이페이지' } },
  ],
})

router.afterEach((to) => {
  document.title = `CodeGenie - ${to.meta?.title ?? ''}`
})

export default router
