<script setup>
import {computed, onMounted, ref} from 'vue'
import {delFetch, getFetch} from "@/stores/apiClient.js";

const currentUserPage = ref(1);
const ROWS_PER_PAGE = 7;
const searchQuery = ref('');
const users = ref([]);
const isSearching = ref(false);

const fetchUserList = async (nickname = '') => {
  try {
    isSearching.value = true;
    const response = await getFetch(`http://localhost:8080/api/v1/user/admin?nickname=${encodeURIComponent(nickname)}`);
    users.value = response.data;
  } catch (error) {
    console.error('회원 목록을 불러오는 중 에러가 발생했습니다.', error.response ? error.response.data : error.message);
  } finally {
    isSearching.value = false;
  }
};

const handleSearch = async () => {
  currentUserPage.value = 1; // 검색 시 첫 페이지로 이동
  await fetchUserList(searchQuery.value);
};

const handleKeyPress = (e) => {
  if (e.key === 'Enter') {
    handleSearch();
  }
};

// 사용자 삭제 함수
const deleteUser = async (userId) => {
  const confirmed = confirm("해당 사용자를 삭제하시겠습니까?");
  if (!confirmed) return;

  try {
    await delFetch(`http://localhost:8080/api/v1/user/${userId}`);
    await delFetch(`http://localhost:8080/api/v1/user/${userId}`);
    alert("해당 사용자의 회원 정보가 삭제되었습니다.")
    // 삭제 성공 후 사용자 목록을 다시 로드
    await fetchUserList();
  } catch (error) {
    if (error.response.status === 403) {
      alert("관리자는 삭제할 수 없습니다.");
    } else {
      console.error('사용자 삭제 중 오류가 발생했습니다.', error.response ? error.response.data : error.message);
    }
  }
};

const displayedUsers = computed(() => {
  const startIdx = (currentUserPage.value - 1) * ROWS_PER_PAGE;
  const endIdx = startIdx + ROWS_PER_PAGE;
  return users.value.slice(startIdx, endIdx);
});

const emptyRowsCount = computed(() =>
    ROWS_PER_PAGE - displayedUsers.value.length
);

const totalUserPages = computed(() =>
    Math.ceil(users.value.length / ROWS_PER_PAGE)
);

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
    <div class="content-wrapper">
      <div class="header-section">
        <h1 class="title">회원 관리</h1>
        <div class="search-area">
          <div class="search-box">
            <i class="search-icon">🔍</i>
            <input
                type="text"
                class="search-input"
                placeholder="닉네임으로 검색"
                v-model="searchQuery"
                @keyup="handleKeyPress"
            >
            <button
                class="search-btn"
                @click="handleSearch"
                :disabled="isSearching"
            >
              {{ isSearching ? '검색 중...' : '검색' }}
            </button>
          </div>
        </div>
      </div>
      <div class="table-container">
        <table class="table">
          <thead>
          <tr>
            <th>번호</th>
            <th>이메일</th>
            <th>닉네임</th>
            <th>가입 사이트</th>
            <th>생성 일자</th>
            <th>관리</th>
          </tr>
          </thead>
          <tbody>
          <tr v-for="(user, index) in displayedUsers" :key="user.userId">
            <td>{{ (currentUserPage - 1) * ROWS_PER_PAGE + index + 1 }}</td>
            <td>{{ user.email }}</td>
            <td>{{ user.nickname }}</td>
            <td>
                <span class="provider-badge" :class="user.provider.toLowerCase()">
                  {{ user.provider }}
                </span>
            </td>
            <td>{{ new Date(user.createdAt).toLocaleDateString() }}</td>
            <td>
              <button class="delete-btn" @click="deleteUser(user.userId)">
                삭제
              </button>
            </td>
          </tr>
          <tr v-if="!displayedUsers.length" class="empty-row">
            <td colspan="6">등록된 회원이 없습니다</td>
          </tr>
          </tbody>
        </table>
      </div>

      <div class="pagination">
        <button
            class="page-btn"
            @click="changeUserPage('prev')"
            :disabled="currentUserPage === 1"
        >◀</button>
        <button
            v-for="page in totalUserPages"
            :key="page"
            @click="changeUserPage(page)"
            :class="['page-btn', { active: currentUserPage === page }]"
        >
          {{ page }}
        </button>
        <button
            class="page-btn"
            @click="changeUserPage('next')"
            :disabled="currentUserPage === totalUserPages"
        >▶</button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.main-container {
  width: 100%;
  min-height: 100%;
  background-color: var(--background-color);
}

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

.search-area {
  max-width: 400px;
  margin: 0 auto;
}

.search-box {
  position: relative;
  width: 100%;
  display: flex;
  gap: 8px;
}

.search-icon {
  position: absolute;
  left: 16px;
  top: 50%;
  transform: translateY(-50%);
  color: #666;
}

.search-input {
  flex: 1;
  padding: 12px 16px 12px 44px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  font-size: 14px;
  transition: all 0.2s;
}

.search-input:focus {
  outline: none;
  border-color: #1a73e8;
  box-shadow: 0 0 0 3px rgba(26, 115, 232, 0.1);
}

.search-btn {
  padding: 12px 24px;
  background-color: #1a73e8;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.search-btn:hover:not(:disabled) {
  background-color: #1557b0;
}

.search-btn:disabled {
  background-color: #a8c7f0;
  cursor: not-allowed;
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
  text-align: left;
  font-size: 14px;
}

.table td {
  padding: 16px;
  border-bottom: 1px solid #eaeaea;
  font-size: 14px;
  color: #333;
}

.table tr:hover:not(.empty-row) {
  background-color: #f8f9ff;
}

.provider-badge {
  padding: 6px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.provider-badge.kakao {
  background-color: #FFE500;
  color: #000000;
}

.provider-badge.naver {
  background-color: #03C75A;
  color: white;
}

.provider-badge.google {
  background-color: #4285F4;
  color: white;
}

.delete-btn {
  padding: 6px 16px;
  border: 1px solid #ff4d4f;
  background: white;
  color: #ff4d4f;
  border-radius: 6px;
  font-size: 13px;
  transition: all 0.2s;
}

.delete-btn:hover {
  background: #ff4d4f;
  color: white;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 8px;
  margin-top: 32px;
}

.page-btn {
  min-width: 36px;
  height: 36px;
  border: none;
  background: none;
  border-radius: 8px;
  font-size: 14px;
  color: #666;
  cursor: pointer;
  transition: all 0.2s;
}

.page-btn:hover:not(:disabled) {
  background: #f0f7ff;
  color: #1a73e8;
}

.page-btn.active {
  background: #1a73e8;
  color: white;
  font-weight: 500;
}

.page-btn:disabled {
  color: #ccc;
  cursor: not-allowed;
}

.empty-row td {
  text-align: center;
  color: #666;
  padding: 32px;
  background: #fafafa;
}
</style>