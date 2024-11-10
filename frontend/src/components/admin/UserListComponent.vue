<script setup>
import { computed, onMounted, ref } from 'vue';
import axios from "axios";
import { useAuthStore } from "@/stores/auth.js";

const authStore = useAuthStore();

const currentUserPage = ref(1);
const ROWS_PER_PAGE = 7;
const searchQuery = ref('');
const users = ref([]);

// 사용자 목록 가져오기
const fetchUserList = async () => {
  try {
    const response = await axios.get('http://localhost:8080/api/v1/user/admin', {
      headers: {
        Authorization: `Bearer ${authStore.accessToken}`
      }
    });
    users.value = response.data;
  } catch (error) {
    console.error('회원 목록을 불러오는 중 에러가 발생했습니다.', error.response ? error.response.data : error.message);
  }
};

// 사용자 삭제 함수
const deleteUser = async (userId) => {
  const confirmed = confirm("해당 사용자를 삭제하시겠습니까?");
  if (!confirmed) return;

  try {
    await axios.delete(`http://localhost:8080/api/v1/user/${userId}`, {
      headers: {
        Authorization: `Bearer ${authStore.accessToken}`
      }
    });
    // 삭제 성공 후 사용자 목록을 다시 로드
    await fetchUserList();
  } catch (error) {
    console.error('사용자 삭제 중 오류가 발생했습니다.', error.response ? error.response.data : error.message);
  }
};

// 검색어를 기반으로 필터링된 사용자 목록 계산
const filteredUsers = computed(() => {
  if (!searchQuery.value) {
    return users.value;
  }
  return users.value.filter(user => user.nickname.includes(searchQuery.value));
});

// 현재 페이지에 보여질 사용자 목록 계산
const displayedUsers = computed(() => {
  const startIdx = (currentUserPage.value - 1) * ROWS_PER_PAGE;
  const endIdx = startIdx + ROWS_PER_PAGE;
  return filteredUsers.value.slice(startIdx, endIdx);
});

// 빈 행 수 계산 (페이징을 맞추기 위해)
const emptyRowsCount = computed(() =>
    ROWS_PER_PAGE - displayedUsers.value.length
);

// 전체 페이지 수 계산
const totalUserPages = computed(() =>
    Math.ceil(filteredUsers.value.length / ROWS_PER_PAGE)
);

// 페이지 변경 함수
const changeUserPage = (page) => {
  if (page === 'prev' && currentUserPage.value > 1) {
    currentUserPage.value--;
  } else if (page === 'next' && currentUserPage.value < totalUserPages.value) {
    currentUserPage.value++;
  } else if (typeof page === 'number') {
    currentUserPage.value = page;
  }
};

onMounted(() => {
  fetchUserList();
});
</script>

<template>
  <div class="main-container">
    <div class="container">
      <div class="user-container">
        <h1 class="title">회원 관리 목록</h1>

        <div class="search-area">
          <input
              type="text"
              class="search-input"
              placeholder="닉네임 검색"
              v-model="searchQuery"
          >
        </div>

        <table class="table">
          <thead>
          <tr>
            <th>번호</th>
            <th>이메일</th>
            <th>닉네임</th>
            <th>가입 사이트</th>
            <th>생성 일자</th>
            <th></th> <!-- 삭제 버튼을 위한 열 -->
          </tr>
          </thead>
          <tbody>
          <tr v-for="(user, index) in displayedUsers" :key="user.userId">
            <td>{{ (currentUserPage - 1) * ROWS_PER_PAGE + index + 1 }}</td> <!-- 순번 -->
            <td>{{ user.email }}</td> <!-- 이메일 -->
            <td>{{ user.nickname }}</td> <!-- 닉네임 -->
            <td>{{ user.provider }}</td> <!-- 가입 사이트 -->
            <td>{{ new Date(user.createdAt).toLocaleDateString() }}</td> <!-- 생성 일자 -->
            <td><button @click="deleteUser(user.userId)">삭제</button></td> <!-- 삭제 버튼 -->
          </tr>
          </tbody>
        </table>

        <div class="pagination">
          <button
              @click="changeUserPage('prev')"
              :disabled="currentUserPage === 1"
          >◀
          </button>
          <button
              v-for="page in totalUserPages"
              :key="page"
              @click="changeUserPage(page)"
              :class="{ active: currentUserPage === page }"
          >
            {{ page }}
          </button>
          <button
              @click="changeUserPage('next')"
              :disabled="currentUserPage === totalUserPages"
          >▶
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.main-container {
  display: flex;
  padding: 20px;
  gap: 20px;
}

.container {
  width: 1000px;
  background: white;
  border-radius: 16px;
  padding: 24px;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'Apple SD Gothic Neo', 'Noto Sans KR', sans-serif;
  display: flex;
  justify-content: center;
  gap: 70px;
}

.title {
  text-align: center;
  font-size: 20px;
  font-weight: 600;
  margin-bottom: 12px;
}

.search-area {
  margin-bottom: 12px;
  text-align: center;
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
  text-align: center;
  font-weight: normal;
}

.table td {
  width: 200px;
  padding: 12px;
  border-bottom: 1px solid #e1e1e1;
  font-size: 14px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  height: 20px;
  line-height: 20px;
  text-align: center;
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

.pagination {
  display: flex;
  justify-content: center;
  gap: 8px;
}

</style>