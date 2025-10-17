// eventBus.js
import { ref } from 'vue';

// 공유 상태 생성
export const bookmarkUpdateEvent = ref(0);

// 상태를 변경하여 이벤트를 트리거하는 함수
export function triggerBookmarkUpdate() {
  bookmarkUpdateEvent.value++;
}
