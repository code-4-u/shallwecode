<script setup>
import SearchBar from "@/components/admin/SearchBar.vue";
import ProbListItemComponent from "@/components/admin/ProbListItemComponent.vue";
import PageBar from "@/components/admin/PageBar.vue";
import {onMounted, ref} from "vue";
import axios from "axios";
import router from "@/router/index.js";
import {useAuthStore} from "@/stores/auth.js";

const problemList = ref([]);
const currentPage = ref(1);
const totalPages = ref(0);
const totalItems = ref(0);
const keyword = ref('');
const option = ref('');
const itemsPerPage = 7;

const authStore = useAuthStore();

const fetchProblemList = async (page = 1) => {
  try {
    console.log("fetchProblemList 호출 - 페이지:");
    const response = await axios.get(`http://localhost:8080/api/v1/problem/admin`, {
      headers: {
        Authorization: `Bearer ${authStore.accessToken}`
      },
      params: {
        page,
        size: itemsPerPage,
        keyword: keyword.value,
        option: option.value
      },
      withCredentials: true
    });

    problemList.value = response.data.problemList;
    totalItems.value = response.data.totalItems;

    // 페이지 수 계산
    totalPages.value = Math.max(1, Math.ceil(totalItems.value / itemsPerPage));

    // 현재 페이지가 totalPages 범위를 초과하지 않도록 조정
    currentPage.value = Math.min(page, totalPages.value);
  } catch (error) {
    console.error('문제 목록을 불러오는데 문제가 발생했습니다.');
  }
};

// 검색 함수
const problemSearch = (searchParams) => {
  keyword.value = searchParams.keyword;
  option.value = searchParams.option;
  currentPage.value = 1; // 페이지 번호를 1로 리셋
  fetchProblemList(1); // 첫 페이지부터 검색 결과를 가져옵니다.
};

// 페이지 변경 함수
const handlePageChange = (page) => {
  console.log("페이지 변경 요청:", page);  // 페이지 변경 로그
  currentPage.value = page;
  fetchProblemList(page);
};

const goToProblemSave = () => {
  router.push('/admin/problem');
};

const handleEditProblem = (problemId) => {
  router.push(`/admin/problem/${problemId}`);
};

const handleDeleteProblem = async (problemId) => {
  try {
    await axios.delete(`http://localhost:8080/api/v1/problem/${problemId}`, {
      headers: {
        Authorization: `Bearer ${authStore.accessToken}`,
      },
    });
    await fetchProblemList(currentPage.value);
  } catch (error) {
    console.error('문제 삭제 중 오류가 발생했습니다.', error);
    alert('문제 삭제에 실패했습니다.');
  }
};

onMounted(async () => {
  await fetchProblemList();
});
</script>

<template>
  <SearchBar @problemSearch="problemSearch"/>

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
    <ProbListItemComponent v-for="problem in problemList" :key="problem.problemId" :problem="problem"
                           @edit-problem="handleEditProblem"
                           @delete-problem="handleDeleteProblem"/>
    </tbody>
  </table>

  <PageBar v-if="totalPages"
           :currentPage="currentPage"
           :totalPages="totalPages"
           :totalItems="totalItems"
           @page-changed="handlePageChange"/>

  <div class="line">
    <button @click="goToProblemSave" class="register-button">문제 등록하기</button>
  </div>
</template>

<style scoped>
.table {
  width: 100%;
  border-collapse: collapse;
}

.table th, .table td {
  border: none;
  padding: 12px;
  text-align: center;
}

.table th {
  background-color: #1a1b3a;
  color: white;
  font-weight: normal;
}

.line {
  display: flex;
  justify-content: flex-end;
}
</style>
