<script setup>
import {computed, onMounted, ref} from "vue";
import {getFetch} from "@/stores/apiClient.js";
import router from "@/router/index.js";

const ROWS_PER_PAGE = 7;
const currentPage = ref(1);
const codingRooms = ref([]);

const fetchMyCodingRoomList = async () => {
  try {
    const response = await getFetch('http://localhost:8080/api/v1/codingroom');
    codingRooms.value = response.data;
  } catch (error) {
    console.error('참여중인 코딩방 목록을 불러오는 중 에러가 발생했습니다.', error.response ? error.response.data : error.message);
  }
};

const handleCodingRoomClick = (codingRoom) => {
  goToCodingRoom(codingRoom.codingRoomId, codingRoom.problemId);
}

const goToCodingRoom = (codingRoomId, problemId) => {
  router.push({path:`/codingroom/${String(codingRoomId)}/${String(problemId)}`});
};

const displayedCodingRooms = computed(() => {
  const startIdx = (currentPage.value - 1) * ROWS_PER_PAGE;
  const endIdx = startIdx + ROWS_PER_PAGE;
  return codingRooms.value.slice(startIdx, endIdx);
});

const emptyRowsCount = computed(() =>
    ROWS_PER_PAGE - displayedCodingRooms.value.length
);

const totalPages = computed(() =>
    Math.ceil(codingRooms.value.length / ROWS_PER_PAGE)
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

onMounted(() => {
  fetchMyCodingRoomList();
});
</script>

<template>
  <div class="container">
    <div class="codingRoom-container">
      <h1 class="title">참여중인 코딩방 목록</h1>

      <table class="table">
        <thead>
        <tr>
          <th>번호</th>
          <th>문제명</th>
          <th>참여중인 인원</th>
          <th>열림 여부</th>
        </tr>
        </thead>
        <tbody>
        <tr
            v-for="(codingRoom, index) in displayedCodingRooms"
            @click="handleCodingRoomClick(codingRoom)"
        >
          <td>{{ (currentPage - 1) * ROWS_PER_PAGE + index + 1 }}</td>
          <td>{{ codingRoom.problemTitle }}</td>
          <td>{{ codingRoom.coopCount }}/5</td>
          <td>
            <span :class="['isOpen', codingRoom.open ? 'isOpen-true' : 'isOpen-false']">
                {{ codingRoom.open ? '열림' : '폐쇄됨' }}
            </span>
          </td>
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

.isOpen {
  border-radius: 4px;
  font-size: 12px;
}

.isOpen-true {
  color: #666;
}

.isOpen-false {
  color: #1a8cff;
}
</style>