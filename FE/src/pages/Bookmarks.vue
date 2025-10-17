<template>
  <div>
    <h1>저장한 문제</h1>
    <div v-if="isLoading">Loading...</div>
    <div v-else-if="error">{{ error }}</div>
    <div v-else-if="groupedBookmarks.length === 0">저장된 북마크가 없습니다.</div>
    <div v-else class="bookmark-groups">
      <div v-for="group in groupedBookmarks" :key="group.workbookTopic" class="bookmark-group-item">
        <h2>문제집: {{ group.workbookTopic }}</h2>
        <div v-for="bookmark in group.bookmarks" :key="bookmark.quiz.id" class="bookmark-item">
          <h3>Q. {{ bookmark.quiz.quiz }}</h3>
          <details>
            <summary>해설 및 개념 보기</summary>
            <p><strong>해설:</strong> {{ bookmark.quiz.explanation }}</p>
            <p><strong>개념:</strong> {{ bookmark.quiz.concept }}</p>
          </details>
          <div v-if="bookmark.lastSubmission" class="submission-details">
            <h4>마지막 제출 기록</h4>
            <p><strong>상태:</strong> {{ bookmark.lastSubmission.status }}</p>
            <p><strong>제출 시간:</strong> {{ new Date(bookmark.lastSubmission.submittedAt).toLocaleString() }}</p>
            <pre><code>{{ bookmark.lastSubmission.answer }}</code></pre>
          </div>
          <div v-else>
            <p>아직 제출 기록이 없습니다.</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import authApi from '../lib/authApi';
import { ref, onMounted, watch } from 'vue'; // Added watch
import { useRoute } from 'vue-router'; // Added useRoute
import { bookmarkUpdateEvent } from '../lib/eventBus';

const route = useRoute(); // Initialize useRoute

const bookmarks = ref([]); // BookmarkDetailDTO 평면 리스트
const groupedBookmarks = ref([]); // 렌더링을 위한 그룹화된 데이터
const isLoading = ref(true);
const error = ref(null);

// 문제집 주제(topic) 가져오기 함수
async function fetchWorkbookTopic(workbookId) {
  try {
    const response = await authApi.get(`/workbooks/${workbookId}`);
    return response.data.topic;
  } catch (err) {
    console.error(`문제집 주제 가져오기 실패 (ID: ${workbookId}):`, err);
    return '(알 수 없는 문제집)'; // 대체 주제
  }
}

// 북마크 처리 및 그룹화 함수
async function processBookmarks() {
  isLoading.value = true;
  error.value = null;
  try {
    const response = await authApi.get('/bookmarks/saved');
    bookmarks.value = response.data;

    // 문제집 ID별로 북마크 그룹화
    const bookmarksByWorkbookId = {};
    const workbookIdsToFetch = new Set();

    for (const bookmark of bookmarks.value) {
      const workbookId = bookmark.quiz.workbook_id; // JSON 응답 구조에 맞게 수정
      if (!bookmarksByWorkbookId[workbookId]) {
        bookmarksByWorkbookId[workbookId] = [];
        workbookIdsToFetch.add(workbookId);
      }
      bookmarksByWorkbookId[workbookId].push(bookmark);
    }

    // 고유한 문제집 ID에 대해 문제집 주제 가져오기
    const workbookTopics = {};
    for (const workbookId of workbookIdsToFetch) {
      workbookTopics[workbookId] = await fetchWorkbookTopic(workbookId);
    }

    // 렌더링을 위한 그룹화된 데이터 생성
    const tempGroupedBookmarks = [];
    for (const workbookId in bookmarksByWorkbookId) {
      tempGroupedBookmarks.push({
        workbookTopic: workbookTopics[workbookId] || '(제목 없음)',
        bookmarks: bookmarksByWorkbookId[workbookId],
      });
    }
    
    // 라우트 쿼리 파라미터에 따라 필터 적용
    const filterTopic = route.query.topic;
    if (filterTopic) {
      groupedBookmarks.value = tempGroupedBookmarks.filter(group => group.workbookTopic === filterTopic);
    } else {
      groupedBookmarks.value = tempGroupedBookmarks;
    }

  } catch (err) {
    console.error('북마크 가져오기 실패:', err);
    error.value = '북마크를 불러오는데 실패했습니다.';
  } finally {
    isLoading.value = false;
  }
}

onMounted(processBookmarks);

// 라우트 쿼리 파라미터 변경 감지하여 북마크 재처리
watch(() => route.query.topic, processBookmarks);

// 북마크 추가/삭제 이벤트 감지하여 북마크 목록 새로고침
watch(bookmarkUpdateEvent, () => {
  console.log('Bookmarks.vue: 북마크 업데이트 이벤트를 감지했습니다.');
  processBookmarks();
});
</script>

<style scoped>
.bookmark-groups {
  display: flex;
  flex-direction: column;
  gap: 30px; /* 간격 추가 */
}
.bookmark-group-item {
  border: 1px solid #ddd;
  border-radius: 10px;
  padding: 20px;
  background-color: #f9f9f9;
}
.bookmark-group-item h2 {
  margin-top: 0;
  color: #1a4dd9;
  border-bottom: 2px solid #1a4dd9;
  padding-bottom: 10px;
  margin-bottom: 20px;
}
.bookmark-list { /* 이 스타일은 더 이상 직접 사용되지 않지만, 참고용으로 유지 */
  display: flex;
  flex-direction: column;
  gap: 20px;
}
.bookmark-item {
  border: 1px solid #ccc;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 15px; /* 그룹 내 아이템 간 간격 */
  background-color: #fff;
}
.submission-details {
  margin-top: 16px;
  border-top: 1px dashed #ccc;
  padding-top: 16px;
}
pre {
  background-color: #f5f5f5;
  padding: 10px;
  border-radius: 4px;
  white-space: pre-wrap;
  word-wrap: break-word;
  margin: 0;
}
</style>
