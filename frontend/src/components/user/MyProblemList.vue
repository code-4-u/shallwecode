<script setup>
import {computed, onMounted, ref, watch} from 'vue'
import {getFetch} from "@/stores/apiClient.js";

const ROWS_PER_PAGE = 7;
const itemsPerPage = 7;

const problems = ref([]);
const trys = ref([]);
const selectedProblem = ref(null);
const currentPage = ref(1);
const tryCurrentPage = ref(1);
const searchQuery = ref('');
const curTry = ref('');
const isModalOpen = ref(false);
const problemFilterStatus = ref('all');
const tryFilterStatus = ref('all');
const isProblemFilterOpen = ref(false);
const isTryFilterOpen = ref(false);

const fetchMyProblemList = async () => {
  try {
    const response = await getFetch('http://localhost:8080/api/v1/problem/mylist');
    problems.value = response.data;
  } catch (error) {
    console.error('내 풀이 문제 목록을 불러오는 중 에러가 발생했습니다.', error.response ? error.response.data : error.message);
  }
};

const fetchTryList = async (problemId) => {
  try {
    const response = await getFetch(`http://localhost:8080/api/v1/problem/${problemId}/try`);
    trys.value = response.data;
  } catch (error) {
    console.error('풀이 시도 목록을 불러오는 중 에러가 발생했습니다.', error.response ? error.response.data : error.message);
  }
};

const handleTryClick = async (tryId) => {
  try {
    const response = await getFetch(`http://localhost:8080/api/v1/problem/try/${tryId}`);
    curTry.value = response.data;
    isModalOpen.value = true;
  } catch (error) {
    console.error('풀이 시도 상세 조회 실패', error);
  }
};

const closeModal = () => {
  isModalOpen.value = false;
  curTry.value = null;
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

const filteredProblems = computed(() => {
  let filtered = problems.value;

  if (searchQuery.value) {
    filtered = filtered.filter(problem =>
        problem.title.toLowerCase().includes(searchQuery.value.toLowerCase())
    );
  }

  if (problemFilterStatus.value !== 'all') {
    filtered = filtered.filter(problem =>
        problemFilterStatus.value === 'solved' ? problem.solved : !problem.solved
    );
  }

  return filtered;
});

const filteredTrys = computed(() => {
  let filtered = trys.value;

  if (tryFilterStatus.value !== 'all') {
    filtered = filtered.filter(try$ =>
        tryFilterStatus.value === 'solved' ? try$.solved : !try$.solved
    );
  }

  return filtered;
});

const totalProblemPages = computed(() =>
    Math.ceil(filteredProblems.value.length / ROWS_PER_PAGE)
);

const totalTryPages = computed(() =>
    Math.ceil(filteredTrys.value.length / ROWS_PER_PAGE)
);

const displayedProblems = computed(() => {
  const startIdx = (currentPage.value - 1) * ROWS_PER_PAGE;
  const endIdx = startIdx + ROWS_PER_PAGE;
  return filteredProblems.value.slice(startIdx, endIdx);
});

const displayedTrys = computed(() => {
  const startIdx = (tryCurrentPage.value - 1) * ROWS_PER_PAGE;
  const endIdx = startIdx + ROWS_PER_PAGE;
  return filteredTrys.value.slice(startIdx, endIdx);
});

const emptyRowsProblemCount = computed(() =>
    ROWS_PER_PAGE - displayedProblems.value.length
);

const emptyRowsTryCount = computed(() =>
    ROWS_PER_PAGE - displayedTrys.value.length
);

const changeProblemPage = (page) => {
  if (page === 'prev' && currentPage.value > 1) {
    currentPage.value--;
  } else if (page === 'next' && currentPage.value < totalProblemPages.value) {
    currentPage.value++;
  } else if (typeof page === 'number') {
    currentPage.value = page;
  }
};

const changeTryPage = (page) => {
  if (page === 'prev' && tryCurrentPage.value > 1) {
    tryCurrentPage.value--;
  } else if (page === 'next' && tryCurrentPage.value < totalTryPages.value) {
    tryCurrentPage.value++;
  } else if (typeof page === 'number') {
    tryCurrentPage.value = page;
  }
};

const selectProblem = async (problem) => {
  selectedProblem.value = problem;
  tryCurrentPage.value = 1;
  await fetchTryList(problem.problemId);
};

const toggleProblemFilter = () => {
  isProblemFilterOpen.value = !isProblemFilterOpen.value;
  isTryFilterOpen.value = false;
};

const toggleTryFilter = () => {
  isTryFilterOpen.value = !isTryFilterOpen.value;
  isProblemFilterOpen.value = false;
};

const setProblemFilter = (status) => {
  problemFilterStatus.value = status;
  isProblemFilterOpen.value = false;
  currentPage.value = 1;
};

const setTryFilter = (status) => {
  tryFilterStatus.value = status;
  isTryFilterOpen.value = false;
  tryCurrentPage.value = 1;
};

watch(searchQuery, () => {
  currentPage.value = 1;
});

onMounted(() => {
  fetchMyProblemList();
});
</script>

<template>
  <div class="container">
    <div class="problem-container">
      <h1 class="title">풀이 문제 목록</h1>

      <div class="search-area">
        <input
            type="text"
            class="search-input"
            placeholder="문제 검색"
            v-model="searchQuery"
        >
        <div class="filter-dropdown">
          <button class="filter-button" @click="toggleProblemFilter">
            {{ problemFilterStatus === 'all' ? '전체' :
              problemFilterStatus === 'solved' ? '해결' : '미해결' }} ▼
          </button>
          <div v-if="isProblemFilterOpen" class="dropdown-content">
            <div @click="setProblemFilter('all')" :class="{ active: problemFilterStatus === 'all' }">전체</div>
            <div @click="setProblemFilter('solved')" :class="{ active: problemFilterStatus === 'solved' }">해결</div>
            <div @click="setProblemFilter('unsolved')" :class="{ active: problemFilterStatus === 'unsolved' }">미해결</div>
          </div>
        </div>
      </div>

      <table class="problem-table">
        <thead>
        <tr>
          <th>번호</th>
          <th>문제명</th>
          <th>난이도</th>
          <th>상태</th>
        </tr>
        </thead>
        <tbody>
        <tr
            v-for="(problem, index) in displayedProblems"
            :key="problem.id"
            @click="selectProblem(problem)"
            :class="{ 'selected-row': selectedProblem?.problemId === problem.problemId }"
        >
          <td>{{ (currentPage - 1) * itemsPerPage + index + 1 }}</td>
          <td>{{ problem.title }}</td>
          <td><span class="level-badge">Lv. {{ problem.problemLevel }}</span></td>
          <td>
              <span :class="['status', problem.solved ? 'status-solved' : 'status-unsolved']">
                {{ problem.solved ? '해결' : '미해결' }}
              </span>
          </td>
        </tr>
        <tr v-for="i in emptyRowsProblemCount" :key="`empty-${i}`" class="empty-row">
          <td>-</td>
          <td>-</td>
          <td>-</td>
          <td>-</td>
        </tr>
        </tbody>
      </table>

      <div class="pagination">
        <button
            @click="changeProblemPage('prev')"
            :disabled="currentPage === 1"
        >◀</button>
        <button
            v-for="page in totalProblemPages"
            :key="page"
            @click="changeProblemPage(page)"
            :class="{ active: currentPage === page }"
        >
          {{ page }}
        </button>
        <button
            @click="changeProblemPage('next')"
            :disabled="currentPage === totalProblemPages"
        >▶</button>
      </div>
    </div>

    <div class="try-container">
      <h1 class="title">풀이 시도</h1>

      <div class="try-area">
        <h3 v-if="selectedProblem">
          "{{ selectedProblem.title }}" 문제의 풀이 시도
        </h3>
        <h3 v-else>
          왼쪽에서 문제를 선택해주세요
        </h3>
        <div class="filter-dropdown">
          <button class="filter-button" @click="toggleTryFilter">
            {{ tryFilterStatus === 'all' ? '전체' :
              tryFilterStatus === 'solved' ? '해결' : '미해결' }} ▼
          </button>
          <div v-if="isTryFilterOpen" class="dropdown-content">
            <div @click="setTryFilter('all')" :class="{ active: tryFilterStatus === 'all' }">전체</div>
            <div @click="setTryFilter('solved')" :class="{ active: tryFilterStatus === 'solved' }">해결</div>
            <div @click="setTryFilter('unsolved')" :class="{ active: tryFilterStatus === 'unsolved' }">미해결</div>
          </div>
        </div>
      </div>

      <table class="problem-table">
        <thead>
        <tr>
          <th>번호</th>
          <th>언어</th>
          <th>상태</th>
          <th>제출 일자</th>
        </tr>
        </thead>
        <tbody>
        <tr v-for="(try$, index) in displayedTrys"
            :key="try$.id"
            @click="handleTryClick(try$.tryId)"
        >
          <td>{{ (tryCurrentPage - 1) * itemsPerPage + index + 1 }}</td>
          <td>{{ try$.tryLanguage }}</td>
          <td>
              <span :class="['status', try$.solved ? 'status-solved' : 'status-unsolved']">
                {{ try$.solved ? '해결' : '미해결' }}
              </span>
          </td>
          <td>{{ formatDate(try$.createdAt) }}</td>
        </tr>
        <tr v-for="i in emptyRowsTryCount" :key="`empty-${i}`" class="empty-row">
          <td>-</td>
          <td>-</td>
          <td>-</td>
          <td>-</td>
        </tr>
        </tbody>
      </table>

      <div class="pagination">
        <button
            @click="changeTryPage('prev')"
            :disabled="tryCurrentPage === 1"
        >◀</button>
        <button
            v-for="page in totalTryPages"
            :key="page"
            @click="changeTryPage(page)"
            :class="{ active: tryCurrentPage === page }"
        >
          {{ page }}
        </button>
        <button
            @click="changeTryPage('next')"
            :disabled="tryCurrentPage === totalTryPages"
        >▶</button>
      </div>
    </div>
    <div v-if="isModalOpen" class="modal-overlay" @click="closeModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h2 class="modal-title">풀이 시도 상세</h2>
          <button class="modal-close" @click="closeModal">&times;</button>
        </div>
        <div class="modal-body">
          <div v-if="curTry" class="try-detail">
            <div class="info-group">
              <div class="info-row">
                <span class="info-label">제출 언어:</span>
                <span class="info-value">{{ curTry.tryLanguage }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">제출 상태:</span>
                <span :class="['info-value', 'status', curTry.solved ? 'status-solved' : 'status-unsolved']">
                  {{ curTry.solved ? '해결' : '미해결' }}
                </span>
              </div>
              <div class="info-row">
                <span class="info-label">제출 시간:</span>
                <span class="info-value">{{ formatDate(curTry.createdAt) }}</span>
              </div>
            </div>
            <div class="code-section">
              <h3>제출한 코드</h3>
              <pre class="code-block">{{ curTry.codeContent }}</pre>
            </div>
          </div>
        </div>
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
  gap: 50px;
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

.search-area {
  display: flex;
  justify-content: space-between;
  margin-bottom: 16px;
  gap: 12px;
}

.search-input {
  flex: 1;
  padding: 8px 16px;
  border: 1px solid #e1e1e1;
  border-radius: 8px;
  font-size: 14px;
}

.filter-dropdown {
  position: relative;
  display: inline-block;
}

.filter-button {
  padding: 8px 16px;
  border: 1px solid #e1e1e1;
  border-radius: 8px;
  background: white;
  font-size: 14px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 4px;
}

.dropdown-content {
  position: absolute;
  top: 100%;
  right: 0;
  background: white;
  border: 1px solid #e1e1e1;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  z-index: 1000;
  min-width: 120px;
}

.dropdown-content div {
  padding: 8px 16px;
  cursor: pointer;
  font-size: 14px;
}

.dropdown-content div:hover {
  background: #f5f5f5;
}

.dropdown-content div.active {
  background: #1a1b3a;
  color: white;
}

.problem-table {
  width: 100%;
  border-collapse: collapse;
  margin-bottom: 24px;
}

.problem-table th {
  background: #1a1b3a;
  color: white;
  padding: 12px;
  text-align: left;
  font-weight: normal;
}

.problem-table td {
  padding: 12px;
  border-bottom: 1px solid #e1e1e1;
  font-size: 14px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.problem-table tr:not(.empty-row) {
  cursor: pointer;
}

.problem-table tr:not(.empty-row):hover {
  background-color: #f5f5f5;
}

.selected-row {
  background-color: #f0f7ff !important;
}

.level-badge {
  background: #f0f0f0;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.status {
  border-radius: 4px;
  font-size: 12px;
}

.status-solved {
  color: #1a8cff;
}

.status-unsolved {
  color: #666;
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

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  border-radius: 8px;
  width: 90%;
  max-width: 800px;
  max-height: 90vh;
  overflow-y: auto;
  position: relative;
}

.modal-header {
  padding: 16px 24px;
  border-bottom: 1px solid #e1e1e1;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.modal-title {
  font-size: 20px;
  font-weight: 600;
  margin: 0;
}

.modal-close {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  padding: 0;
  color: #666;
}

.modal-body {
  padding: 24px;
}

.try-detail {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.info-group {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.info-row {
  display: flex;
  gap: 8px;
  align-items: center;
}

.info-label {
  font-weight: 500;
  color: #666;
  width: 100px;
}

.info-value {
  flex: 1;
}

.code-section {
  background: #f5f5f5;
  border-radius: 8px;
  padding: 16px;
}

.code-section h3 {
  margin: 0 0 16px 0;
  font-size: 16px;
  color: #333;
}

.code-block {
  background: #fff;
  padding: 16px;
  border-radius: 4px;
  border: 1px solid #e1e1e1;
  overflow-x: auto;
  font-family: monospace;
  line-height: 1.5;
  margin: 0;
  white-space: pre-wrap;
}
</style>