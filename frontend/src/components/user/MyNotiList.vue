<script setup>
import {computed, inject, onMounted, ref} from "vue";
import {getFetch, putFetch} from "@/stores/apiClient.js";

const ROWS_PER_PAGE = 7;
const currentPage = ref(1);
const notis = ref([]);

const refreshNotiList = inject('refreshNotiList');

const fetchMyNotiList = async () => {
  try {
    const response = await getFetch('http://localhost:8080/api/v1/noti/all');
    notis.value = response.data;
  } catch (error) {
    console.error('알림 목록을 불러오는 중 에러가 발생했습니다.', error.response ? error.response.data : error.message);
  }
};

refreshNotiList.value = fetchMyNotiList;

const handleNotiClick = async (noti) => {
  try {
    if (!noti.isRead) {
      await putFetch(`http://localhost:8080/api/v1/noti`, {
        notiId: noti.notiId
      });
      await fetchMyNotiList();
    }
  } catch (error) {
    console.error('알림 읽음 상태 변경 중 에러가 발생했습니다.', error.response ? error.response.data : error.message);
  }
};

const displayedNotis = computed(() => {
  const startIdx = (currentPage.value - 1) * ROWS_PER_PAGE;
  const endIdx = startIdx + ROWS_PER_PAGE;
  return notis.value.slice(startIdx, endIdx);
});

const emptyRowsCount = computed(() =>
    ROWS_PER_PAGE - displayedNotis.value.length
);

const totalPages = computed(() =>
    Math.ceil(notis.value.length / ROWS_PER_PAGE)
);

const changePage = (page) => {
  if (page === 'prev' && currentPage.value > 1) {
    currentPage.value--;
  } else if (page === 'next' && currentPage.value < totalPages.value) {
    currentPage.value++;
  } else if (typeof page === 'number') {
    currentPage.value = page;
  }
};

const formatDate = (dateString) => {
  const date = new Date(dateString);
  return date.toLocaleString('ko-KR', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  });
};

onMounted(() => {
  fetchMyNotiList();
});
</script>

<template>
  <div class="container">
    <div class="noti-container">
      <h1 class="title">알림 목록</h1>

      <table class="table">
        <thead>
        <tr>
          <th>번호</th>
          <th>알림 내용</th>
          <th>알림 유형</th>
          <th>생성 일자</th>
        </tr>
        </thead>
        <tbody>
        <tr
            v-for="(noti, index) in displayedNotis"
            @click="handleNotiClick(noti)"
        >
          <td>{{ (currentPage - 1) * ROWS_PER_PAGE + index + 1 }}</td>
          <td>{{ noti.content }}</td>
          <td>
            <span :class="['isRead', noti.isRead ? 'isRead-true' : 'isRead-false']">
                {{ noti.isRead ? '읽음' : '안읽음' }}
              </span>
          </td>
          <td>{{ formatDate(noti.createdAt) }}</td>
        </tr>
        <tr v-for="i in emptyRowsCount" :key="`empty-${i}`" class="empty-row">
          <td>-</td>
          <td>-</td>
          <td>-</td>
          <td>-</td>
        </tr>
        </tbody>
      </table>

      <div class="pagination">
        <button
            @click="changePage('prev')"
            :disabled="currentPage === 1"
        >◀
        </button>
        <button
            v-for="page in totalPages"
            :key="page"
            @click="changePage(page)"
            :class="{ active: currentPage === page }"
        >
          {{ page }}
        </button>
        <button
            @click="changePage('next')"
            :disabled="currentPage === totalPages"
        >▶
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.container {
  width: 1000px;
  background: white;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'Apple SD Gothic Neo', 'Noto Sans KR', sans-serif;
  display: flex;
  justify-content: center;
  gap: 70px;
}

.title {
  text-align: center;
  font-size: 20px;
  font-weight: 600;
  margin-bottom: 24px;
}

.try-area {
  display: flex;
  justify-content: space-between;
  margin-bottom: 16px;
  gap: 12px;
  align-items: center;
}

.try-area h3 {
  margin: 0;
  font-size: 16px;
  color: #666;
}

.table {
  width: 100%;
  border-collapse: collapse;
  margin-bottom: 24px;
}

.table th {
  background: #1a1b3a;
  color: white;
  padding: 12px;
  text-align: left;
  font-weight: normal;
}

.table td {
  padding: 12px;
  border-bottom: 1px solid #e1e1e1;
  font-size: 14px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  height: 20px;
  line-height: 20px;
}

.table tr:not(.empty-row) {
  cursor: pointer;
}

.table tr:not(.empty-row):hover {
  background-color: #f5f5f5;
}

button {
  cursor: pointer;
  margin-left: 4px;
  margin-right: 4px;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 8px;
}

.pagination button {
  width: 32px;
  height: 32px;
  border: none;
  background: none;
  cursor: pointer;
  border-radius: 4px;
  font-size: 14px;
}

.pagination button.active {
  background: #1a1b3a;
  color: white;
}

.pagination button:hover:not(.active) {
  background: #f5f5f5;
}

.isRead {
  border-radius: 4px;
  font-size: 12px;
}

.isRead-true {
  color: #666;
}

.isRead-false {
  color: #1a8cff;
}
</style>