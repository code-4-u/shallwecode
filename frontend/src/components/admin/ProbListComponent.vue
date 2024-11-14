<script setup>
import SearchBar from "@/components/admin/SearchBar.vue";
import ProbListItemComponent from "@/components/admin/ProbListItemComponent.vue";
import PageBar from "@/components/admin/PageBar.vue";
import {onMounted, ref} from "vue";
import axios from "axios";
import router from "@/router/index.js";
import {useAuthStore} from "@/stores/auth.js";

const problemList = ref([]);
const currentPage = ref(0);
const totalPages = ref(0);
const totalItems = ref(0);
const keyword = ref('');
const option = ref('');

const authStore = useAuthStore();

const fetchProblemList = async (page = 1) => {
  try {
    const response = await axios.get(`http://localhost:8080/api/v1/problem/admin`, {
      headers: {
        Authorization: `Bearer ${authStore.accessToken}`
      },
      params: {
        page,
        keyword: keyword.value,
        option: option.value
      },
      withCredentials: true
    });

    problemList.value = response.data.problemList;
    currentPage.value = response.data.currentPage;
    totalPages.value = response.data.totalPages;
    totalItems.value = response.data.totalItems;
  } catch (error) {
    console.error('문제 목록을 불러오는데 문제가 발생했습니다.');
  }
}

const problemSearch = (searchParams) => {
  keyword.value = searchParams.keyword;
  option.value = searchParams.option;
  fetchProblemList(1);
}

function goToProblemSave() {
  router.push('/admin/problem')
}

// 문제 수정 처리 함수
const handleEditProblem = (problemId) => {

  console.log('수정할 문제 ID:', problemId);
  // 문제 수정 페이지로 이동
  router.push(`/admin/problem/${problemId}`);
};


// 문제 삭제 처리 함수
const handleDeleteProblem = async (problemId) => {

  try {
    await axios.delete(`http://localhost:8080/api/v1/problem/${problemId}`, {
      headers: {
        Authorization: `Bearer ${authStore.accessToken}`,
      },
    });
    console.log('문제가 삭제되었습니다:', problemId);
    await fetchProblemList(currentPage.value); // 페이지를 새로고침하여 목록 업데이트
  } catch (error) {
    console.error('문제 삭제 중 오류가 발생했습니다.', error);
    alert('문제 삭제에 실패했습니다.');
  }

};

onMounted(async () => {
  await fetchProblemList();
})

</script>

<template>
  <div class="content-wrapper">
    <div class="header-section">
      <h1 class="title">문제 관리</h1>
      <SearchBar @problemSearch="problemSearch"/>
    </div>

    <div class="table-container">
      <table class="table">
        <thead>
        <tr>
          <th>번호</th>
          <th>제목</th>
          <th>난이도</th>
          <th>수정</th>
          <th>삭제</th>
        </tr>
        </thead>
        <tbody>
        <ProbListItemComponent
            v-for="problem in problemList"
            :key="problem.problemId"
            :problem="problem"
            @edit-problem="handleEditProblem"
            @delete-problem="handleDeleteProblem"
        />
        </tbody>
      </table>
    </div>
    <div class="footer-section">
      <div class="left-space"></div>
      <PageBar
          :currentPage="currentPage"
          :totalPages="totalPages"
          :totalItems="totalItems"
          @page-change="problemSearch"
      />
      <div class="right-space">
        <button @click="goToProblemSave" class="register-button">
          <span class="button-icon">✚</span>
          문제 등록하기
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.content-wrapper {
  background: white;
  border-radius: 16px;
  padding: 32px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
}

.header-section {
  margin-bottom: 32px;
}

.title {
  font-size: 24px;
  font-weight: 600;
  color: #1a1b3a;
  margin-bottom: 24px;
  text-align: center;
  position: relative;
}

.title:after {
  content: '';
  position: absolute;
  bottom: -8px;
  left: 50%;
  transform: translateX(-50%);
  width: 40px;
  height: 3px;
  background: #1a73e8;
  border-radius: 2px;
}

.table-container {
  margin: 20px 0;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 0 0 1px #eaeaea;
}

.table {
  width: 100%;
  border-collapse: separate;
  border-spacing: 0;
}

.table th {
  background: #1a73e8;
  color: white;
  padding: 16px;
  font-weight: 500;
  text-align: center;
  font-size: 14px;
  border: none;
}

.table td {
  padding: 16px;
  border-bottom: 1px solid #eaeaea;
  font-size: 14px;
  color: #333;
  text-align: center;
}

.table tr:hover {
  background-color: #f8f9ff;
  transition: background-color 0.2s ease;
}

.table th:first-child {
  border-top-left-radius: 8px;
}

.table th:last-child {
  border-top-right-radius: 8px;
}

.footer-section {
  display: grid;
  grid-template-columns: 1fr auto 1fr;
  align-items: center;
  margin-top: 24px;
  padding-top: 24px;
  border-top: 1px solid #eaeaea;
  gap: 16px;
}

.left-space {
}

.right-space {
  display: flex;
  justify-content: flex-end;
}

.register-button {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 24px;
  background-color: #1a73e8;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
}

.register-button:hover {
  background-color: #1557b0;
  transform: translateY(-1px);
}

.button-icon {
  font-size: 16px;
  font-weight: bold;
}

/* 반응형 디자인 */
@media (max-width: 768px) {
  .content-wrapper {
    padding: 20px;
  }

  .footer-section {
    flex-direction: column-reverse;
    gap: 20px;
  }

  .register-button {
    width: 100%;
    justify-content: center;
  }
}

/* 애니메이션 효과 */
@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.content-wrapper {
  animation: fadeIn 0.3s ease-out;
}
</style>