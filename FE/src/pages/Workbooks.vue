<template>
  <div class="wrap">
    <!-- 좌측: 문제/제출 -->
    <section class="left">
      <h3 class="title">{{ title }}</h3>

      <!-- 빈 상태 -->
      <div v-if="!hasProblems" class="empty-wrap">
        <div class="empty-title">아직 생성한 문제가 없습니다.</div>
        <p class="empty-desc">좌측의 <b>새 문제</b>에서 문제집을 생성해 보세요.</p>
        <RouterLink class="btn-dark" to="/new">새 문제 만들기</RouterLink>
      </div>

      <template v-else>
        <div class="tabs">
          <button :class="{active: tab==='quiz'}" @click="tab='quiz'">문제</button>
          <button :class="{active: tab==='submit'}" @click="tab='submit'">제출</button>
        </div>

        <!-- 문제 탭 -->
        <div v-if="tab === 'quiz' && cur" class="quiz-area">
          <div class="problem-header">
            <span class="name">문제 {{ index + 1 }}</span>
            <span class="badge">문제</span>
          </div>

          <!-- 제출/정답만 -->
          <table class="spec">
            <tbody>
              <tr>
                <th>제출</th><td>{{ cur.spec.submissions }}</td>
                <th>정답</th><td>{{ cur.spec.accepted }}</td>
              </tr>
            </tbody>
          </table>

          <section class="block"><h4>문제</h4><p>{{ cur.statement }}</p></section>
          <section class="block"><h4>입력</h4><p>{{ cur.input }}</p></section>
          <section class="block"><h4>출력</h4><p>{{ cur.output }}</p></section>

          <section class="block">
            <h4>예제 입력</h4>
            <textarea class="io" readonly>{{ cur.sampleInput }}</textarea>
          </section>

          <!-- 하단 바 -->
          <div class="footer-bar">
            <div class="foot-left">
              <button class="btn-outline" @click="openQna">질문지 보기</button>
            </div>
            <div class="foot-right">
              <button class="btn-subtle" :class="{disabled:isFirst}" @click="goPrev">이전</button>
              <div class="page-no">{{ index + 1 }} / {{ problems.length }}</div>
              <button class="btn-dark" :class="{disabled:isLast}" @click="goNext">다음 문제</button>
            </div>
          </div>
        </div>

        <!-- 제출 탭 -->
        <div v-else-if="tab === 'submit'" class="submit-area">
          <SubmissionEditor v-if="cur" :quiz-id="cur.id" />
        </div>
      </template>
    </section>

    <!-- 우측: 해설/개념 카드 + 즐겨찾기 버튼 -->
    <aside class="right" v-if="hasProblems && cur">
      <button class="bookmark" title="즐겨찾기" @click="bookmark">
        {{ cur.isSaved ? '❤️' : '🤍' }}
      </button>

      <div class="card">
        <div class="card-title">해설</div>
        <button class="btn" @click="showExplain = !showExplain">{{ showExplain ? '접기' : '보기' }}</button>
      </div>
      <transition name="fade">
        <div v-if="showExplain" class="panel">
          <div class="panel-content">{{ cur.explanation }}</div>
        </div>
      </transition>

      <div class="card">
        <div class="card-title">개념</div>
        <button class="btn" @click="showConcept = !showConcept">{{ showConcept ? '접기' : '보기' }}</button>
      </div>
      <transition name="fade">
        <div v-if="showConcept" class="panel">
          <div class="panel-content">{{ cur.concept }}</div>
        </div>
      </transition>
    </aside>

    <!-- 질문지 모달 -->
    <div v-if="showQna" class="modal-root">
      <div class="backdrop" @click="closeQna"></div>
      <div class="modal">
        <h4>질문지</h4>
        <form class="qna-form" @submit.prevent>
          <label>프로그래밍 언어
            <input :value="form?.language || ''" disabled />
          </label>
          <label>레벨
            <input :value="form?.level || ''" disabled />
          </label>
          <label>학습 스타일
            <input :value="form?.style || ''" disabled />
          </label>
          <label>요청 상세
            <textarea class="textarea-fixed" :value="form?.request_detail || ''" disabled></textarea>
          </label>
          <label>학습 주제명
            <input :value="form?.topic || ''" disabled />
          </label>
        </form>
        <div class="modal-actions">
          <button class="btn-dark" @click="closeQna">닫기</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, RouterLink } from 'vue-router'
import authApi from '../lib/authApi'
import SubmissionEditor from '../components/SubmissionEditor.vue'
import { triggerBookmarkUpdate } from '../lib/eventBus';

const route = useRoute()
const id = computed(() => route.params.id)

const loading = ref(false)
const form = ref(null)
const problems = ref([])
const index = ref(0)

const hasProblems = computed(() => Array.isArray(problems.value) && problems.value.length > 0)
const cur = computed(() => hasProblems.value ? problems.value[index.value] : null)
const isFirst = computed(() => index.value === 0)
const isLast  = computed(() => index.value === problems.value.length - 1)

const tab = ref('quiz')
const showExplain = ref(false)
const showConcept = ref(false)
const showQna = ref(false)

const title = computed(() =>
  form.value?.topic || `${form.value?.language || ''}에 관한 문제`
)

function goPrev(){ if(isFirst.value){ alert('첫번째 문제입니다.'); return } index.value-- }
function goNext(){ if(isLast.value){ alert('마지막 문제입니다.'); return } index.value++ }
async function bookmark() {
  if (!cur.value) return;

  try {
    // API 호출해서 is_saved 상태 토글
    await authApi.post(`/bookmarks/${cur.value.id}`);

    // 현재 보고 있는 문제의 isSaved 상태 UI에 즉시 반영
    // problems 배열에서 직접 해당 문제의 isSaved 값 변경
    const problem = problems.value.find(p => p.id === cur.value.id);
    if (problem) {
      problem.isSaved = !problem.isSaved;
    }

    // 다른 컴포넌트에 북마크 변경 알림
    triggerBookmarkUpdate();
    
  } catch (error) {
    console.error("Bookmark toggle failed:", error);
    alert("북마크 변경에 실패했습니다.");
  }
}

function openQna(){ showQna.value = true }
function closeQna(){ showQna.value = false }

onMounted(load)

/** 🔁 id가 바뀌면 다시 로드 (같은 컴포넌트 재사용 시 필수) */
watch(() => route.params.id, () => {
  // 뷰 상태 초기화
  index.value = 0
  tab.value = 'quiz'
  showExplain.value = false
  showConcept.value = false
  showQna.value = false
  load()
})

async function load() {
  loading.value = true
  try {
    const { data: wb } = await authApi.get(`/workbooks/${id.value}`)
    form.value = wb
    const { data: list } = await authApi.get(`/bookmarks/workbook/${id.value}/quizzes`)
    problems.value = Array.isArray(list) ? list : []
  } catch (e) {
    console.error('문제집 로드 실패:', e)
    alert('문제집을 불러오지 못했습니다. 잠시 후 다시 시도하세요.')
    form.value = null
    problems.value = []
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.wrap{display:grid;grid-template-columns:1fr 360px;gap:24px;padding:20px}

/* 좌측 */
.left{background:#fff;border:1px solid #eee;border-radius:12px;padding:16px}
.title{margin:0 0 8px 0}

/* 빈 상태 */
.empty-wrap{display:flex;flex-direction:column;gap:10px;align-items:flex-start;padding:24px 8px}
.empty-title{font-size:18px;font-weight:700}
.empty-desc{color:#666;margin-bottom:6px}

/* 탭 */
.tabs{display:flex;gap:8px;margin-bottom:12px;border-bottom:1px solid #eee}
.tabs button{border:0;background:transparent;padding:8px 12px;border-bottom:2px solid transparent;cursor:pointer}
.tabs button.active{border-color:#7c8cfb;color:#2036ff;font-weight:700}

/* 헤더 */
.problem-header{display:flex;align-items:center;gap:8px;margin:8px 0 12px}
.problem-header .name{font-weight:700}
.badge{background:#eee;border-radius:6px;padding:2px 6px;font-size:12px}

/* 스펙 */
.spec{width:100%;border-collapse:collapse;background:#fafafa;border:1px solid #eee;border-radius:8px;overflow:hidden;margin-bottom:12px}
.spec th,.spec td{padding:8px;border-bottom:1px solid #eee}
.spec th{width:90px;color:#555}

/* 본문 */
.block{margin:14px 0}
.block h4{margin:0 0 6px 0}

/* 예제 입력 텍스트영역: 고정 높이 + 스크롤 + 리사이즈 금지 */
.io{
  width:100%;
  border:1px solid #ddd;
  border-radius:8px;
  padding:8px;
  height:100px;
  resize:none;
  overflow:auto;
}

/* 하단 바 */
.footer-bar{
  display:flex;
  align-items:center;
  justify-content:space-between;
  gap:12px;
  margin-top:16px;
  flex-wrap:wrap;
}
.foot-left{display:flex;align-items:center;gap:8px}
.foot-right{display:flex;align-items:center;gap:12px}
.btn-outline{
  border:1px solid #1a4dd9;
  background:#fff;
  color:#1a4dd9;
  border-radius:20px;
  padding:8px 14px;
  cursor:pointer;
}

/* 버튼 공통 */
.btn-subtle{border:1px solid #ddd;background:#fff;border-radius:20px;padding:8px 14px;cursor:pointer}
.btn-dark{border:0;background:#222;color:#fff;border-radius:20px;padding:8px 14px;cursor:pointer}
.btn-subtle.disabled,.btn-dark.disabled{opacity:.45;cursor:not-allowed}
.page-no{color:#666}

/* 우측 */
.right{position:relative;padding-top:24px}
.bookmark{
  position:absolute;
  right:-10px;
  top:-14px;
  border:0;background:transparent;
  font-size:22px;cursor:pointer;
}
.card{background:#ffe36b;padding:16px;border-radius:12px;min-height:60px;display:flex;align-items:center;justify-content:space-between;margin-bottom:10px}
.card-title{font-weight:700}
.btn{border:0;background:#16a34a;color:#fff;border-radius:20px;padding:8px 14px;cursor:pointer}

/* 해설/개념 펼침 패널 */
.panel{background:#fff;border:1px solid #eee;border-radius:12px;max-height:260px;overflow:auto;padding:12px;margin:-4px 0 14px}
.panel-content{white-space:pre-wrap;line-height:1.45;color:#333}
.fade-enter-active,.fade-leave-active{transition:opacity .18s ease}
.fade-enter-from,.fade-leave-to{opacity:0}

/* 질문지 모달 */
.modal-root{position:fixed;inset:0;z-index:50}
.backdrop{position:absolute;inset:0;background:rgba(0,0,0,.35)}
.modal{
  position:absolute;left:50%;top:50%;
  transform:translate(-50%,-50%);
  background:#fff;border:1px solid #eee;border-radius:12px;
  width:min(680px,90vw);padding:16px;
}
.qna-form{display:grid;gap:10px}
.qna-form input,.qna-form textarea{
  width:100%;padding:10px;border:1px solid #ddd;border-radius:8px;background:#f8f8f8
}
.textarea-fixed{height:140px;resize:none;overflow:auto}
.modal-actions{display:flex;justify-content:flex-end;margin-top:10px}

@media (max-width:1024px){
  .wrap{grid-template-columns:1fr}
  .right{order:-1}
}
</style>
