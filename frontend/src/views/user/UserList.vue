<script setup>
import {computed, onMounted, ref, watch} from 'vue'
import {getFetch, postFetch} from "@/stores/apiClient.js";
import kakaoIcon from '@/assets/icons/kakao.svg'
import naverIcon from '@/assets/icons/naver.svg'
import googleIcon from '@/assets/icons/google.svg'

const currentPage = ref(1);
const currentUserPage = ref(1);
const friendItemsPerPage = 2
const ROWS_PER_PAGE = 7;
const searchQuery = ref('');

const profile = ref('');

const fetchProfile = async () => {
  try {
    const response = await getFetch('http://localhost:8080/api/v1/user/profile');
    profile.value = response.data;
  } catch (error) {
    console.error('프로필을 불러오는데 에러가 발생했습니다.', error.response ? error.response.data : error.message);
  }
};

const providerIcon = computed(() => {
  if (!profile.value || !profile.value.provider) return '';

  switch (profile.value.provider) {
    case 'KAKAO':
      return kakaoIcon;
    case 'NAVER':
      return naverIcon;
    case 'GOOGLE':
      return googleIcon;
    default:
      return '';
  }
});

const friendsList = ref([]);

const fetchFriendList = async () => {
  try {
    const response = await getFetch('http://localhost:8080/api/v1/friend');
    friendsList.value = response.data;
  } catch (error) {
    console.error('친구 목록을 가져오는 중 오류가 발생했습니다:', error);
  }
};

const handleRequestClick = async (toUserId) => {
  try {
    await postFetch('http://localhost:8080/api/v1/friend/request', {
      toUserId: toUserId
    });
    await fetchUserList();
    alert('친구 신청되었습니다.')
  } catch (error) {
    alert('이미 친구 신청 대기 중입니다.')
    console.error('친구 신청 진행 중 오류가 발생했습니다:', error);
  }
};

const totalPages = computed(() =>
    Math.ceil(friendsList.value.length / friendItemsPerPage)
);

const paginatedFriends = computed(() => {
  const start = (currentPage.value - 1) * friendItemsPerPage;
  const end = start + friendItemsPerPage;

  if (friendsList.value.length === 0) return [];
  return friendsList.value.slice(start, end);
});

const nextPage = () => {
  if (currentPage.value < totalPages.value) {
    currentPage.value++;
  }
}

const prevPage = () => {
  if (currentPage.value > 1) {
    currentPage.value--
  }
}

const goToPage = (page) => {
  currentPage.value = page
}

const users = ref([]);

const fetchUserList = async () => {
  try {
    const response = await getFetch('http://localhost:8080/api/v1/user/list');
    users.value = response.data;
  } catch (error) {
    console.error('회원 목록을 불러오는 중 에러가 발생했습니다.', error.response ? error.response.data : error.message);
  }
};

const displayedUsers = computed(() => {
  const filteredUsers = searchQuery.value
      ? users.value.filter(user =>
          user.nickname.toLowerCase().includes(searchQuery.value.toLowerCase())
      )
      : users.value;

  const startIdx = (currentUserPage.value - 1) * ROWS_PER_PAGE;
  const endIdx = startIdx + ROWS_PER_PAGE;
  return filteredUsers.slice(startIdx, endIdx);
});

const emptyRowsCount = computed(() =>
    ROWS_PER_PAGE - displayedUsers.value.length
);

const totalUserPages = computed(() => {
  const filteredUsers = searchQuery.value
      ? users.value.filter(user =>
          user.nickname.toLowerCase().includes(searchQuery.value.toLowerCase())
      )
      : users.value;

  return Math.ceil(filteredUsers.length / ROWS_PER_PAGE);
});

const changeUserPage = (page) => {
  if (page === 'prev' && currentUserPage.value > 1) {
    currentUserPage.value--;
  } else if (page === 'next' && currentUserPage.value < totalUserPages.value) {
    currentUserPage.value++;
  } else if (typeof page === 'number') {
    currentUserPage.value = page;
  }
};

watch(searchQuery, () => {
  currentUserPage.value = 1;
});

onMounted(() => {
  fetchProfile();
  fetchUserList();
  fetchFriendList();
});
</script>

<template>
  <div class="main-container">
    <div class="container">
      <div class="user-container">
        <h1 class="title">회원 목록</h1>

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
            <th></th>
          </tr>
          </thead>
          <tbody>
          <tr v-for="(user, index) in displayedUsers" :key="user.userId">
            <td>{{ (currentUserPage - 1) * ROWS_PER_PAGE + index + 1 }}</td>
            <td>{{ user.email }}</td>
            <td>{{ user.nickname }}</td>
            <td>
              <button v-if="!user.friend" @click="handleRequestClick(user.userId)">친구 신청</button>
            </td>
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
    <div class="profile-container">
      <div class="profile-card">
        <div class="user-header">
          <h2 class="user-id">{{ profile.nickname }}</h2>
          <p class="user-email">
            {{ profile.email }}
            <img v-if="profile.provider" :src="providerIcon" :alt="profile.provider" class="provider-icon"/>
          </p>
        </div>

        <div class="stats-container">
          <div class="stat-item">
            <p class="stat-label">도전한 문제</p>
            <p class="stat-value">{{ profile.doingProblemCnt }}개</p>
          </div>
          <div class="stat-item">
            <p class="stat-label">미해결 문제</p>
            <p class="stat-value">{{ profile.notFinishedProblemCnt }}개</p>
          </div>
          <div class="stat-item">
            <p class="stat-label">해결한 문제</p>
            <p class="stat-value">{{ profile.finishedProblemCnt }}개</p>
          </div>
        </div>
      </div>

      <div class="friends-card">
        <h3 class="friends-title">친구 목록</h3>

        <div class="friends-list">
          <template v-if="friendsList.length > 0">
            <div v-for="friend in paginatedFriends" :key="friend.id" class="friend-item">
              <div class="friend-avatar">
                <img src="@/assets/icons/profile-friend.svg" alt="프로필 사진"/>
              </div>
              <span class="friend-name">{{ friend.nickname }}</span>
            </div>
          </template>
          <div v-else class="no-friends-message">
            <img src="@/assets/icons/profile-friend.svg" alt="친구 아이콘" class="no-friends-icon"/>
            <p class="no-friends-text">아직 등록된 친구가 없습니다</p>
            <p class="no-friends-subtext">새로운 친구를 추가해보세요!</p>
          </div>
        </div>

        <div class="pagination" v-if="friendsList.length > 0">
          <button
              class="pagination-btn"
              @click="prevPage"
              :disabled="currentPage === 1"
          >◀
          </button>

          <template v-for="page in totalPages" :key="page">
            <button
                class="pagination-btn"
                :class="{ active: currentPage === page }"
                @click="goToPage(page)"
            >
              {{ page }}
            </button>
          </template>

          <button
              class="pagination-btn"
              @click="nextPage"
              :disabled="currentPage === totalPages"
          >▶
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.main-container {
  background-color: var(--background-color);
  display: flex;
  justify-content: center;
  padding: 20px;
  gap: 20px;
  margin: 0 auto;
  min-height: calc(100vh - 40px);
}

.container {
  width: 800px;
  background: white;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'Apple SD Gothic Neo', 'Noto Sans KR', sans-serif;
  display: flex;
  justify-content: center;
  gap: 70px;
  height: fit-content;
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

.profile-container {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  width: 400px;
  min-width: 300px;
  margin: 0;
}

.profile-card, .friends-card {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  margin-bottom: 20px;
}

.provider-icon {
  width: 15px;
  height: 15px;
}

.user-header {
  margin-bottom: 20px;
}

.user-id {
  color: #4285f4;
  font-size: 18px;
  margin: 0;
  font-weight: 600;
}

.user-email {
  color: #666;
  font-size: 14px;
  margin: 5px 0 0 0;
}

.stats-container {
  display: flex;
  justify-content: space-between;
  text-align: center;
}

.stat-item {
  flex: 1;
}

.stat-label {
  font-size: 14px;
  color: #666;
  margin: 0 0 5px 0;
}

.stat-value {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin: 0;
}

.friends-title {
  font-size: 16px;
  color: #333;
  margin: 0 0 20px 0;
}

.friends-list {
  margin-bottom: 20px;
}

.friend-item {
  display: flex;
  align-items: center;
  padding: 10px 0;
}

.friend-avatar {
  width: 32px;
  height: 32px;
  margin-left: 12px;
  margin-right: 12px;
  border-radius: 50%;
  overflow: hidden;
}

.friend-name {
  font-size: 14px;
  color: #333;
}

.pagination {
  display: flex;
  justify-content: center;
  gap: 8px;
}

.pagination-btn {
  border: none;
  background: none;
  padding: 5px 10px;
  cursor: pointer;
  color: #666;
  border-radius: 4px;
}

.pagination-btn:hover:not(:disabled) {
  background-color: #f0f0f0;
}

.pagination-btn.active {
  background-color: #4285f4;
  color: white;
}

.pagination-btn:disabled {
  color: #ccc;
  cursor: not-allowed;
}

.provider-icon {
  width: 16px;
  height: 16px;
}

.no-friends-message {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 32px 20px;
  text-align: center;
}

.no-friends-icon {
  width: 48px;
  height: 48px;
  opacity: 0.5;
  margin-bottom: 16px;
}

.no-friends-text {
  font-size: 15px;
  color: #333;
  margin: 0 0 8px 0;
  font-weight: 500;
}

.no-friends-subtext {
  font-size: 13px;
  color: #666;
  margin: 0;
}
</style>