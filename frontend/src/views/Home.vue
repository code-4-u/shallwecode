<script setup>
import {computed, onMounted, onUnmounted, ref, watch} from 'vue';
import {getFetch} from "@/stores/apiClient.js";
import kakaoIcon from '@/assets/icons/kakao.svg'
import naverIcon from '@/assets/icons/naver.svg'
import googleIcon from '@/assets/icons/google.svg'
import {useAuthStore} from "@/stores/auth.js";
import axios from "axios";
import router from "@/router/index.js";

const store = useAuthStore();

const currentPage = ref(1);
const currentProblemPage = ref(1);
const friendItemsPerPage = 2;
const ROWS_PER_PAGE = 7;
const searchQuery = ref('');
const selectedStatus = ref(null);
const selectedLevel = ref(null);
const isStatusDropdownOpen = ref(false);
const isLevelDropdownOpen = ref(false);
const loading = ref(false);
const isLogin = ref(false);

const levelOptions = [
  { value: null, label: '난이도' },
  { value: 1, label: 'Lv.1' },
  { value: 2, label: 'Lv.2' },
  { value: 3, label: 'Lv.3' },
  { value: 4, label: 'Lv.4' },
  { value: 5, label: 'Lv.5' },
];

const statusOptions = [
  { value: null, label: '상태' },
  { value: true, label: '해결' },
  { value: false, label: '미해결' },
];

const profile = ref('');

const fetchProfile = async () => {
  try {
    const response = await getFetch('/user/profile');
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
    const response = await getFetch('/friend');
    friendsList.value = response.data;
  } catch (error) {
    console.error('친구 목록을 가져오는 중 오류가 발생했습니다:', error);
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

const problems = ref([]);

const fetchProblemList = async () => {
  try {
    loading.value = true;
    const params = {
      isSolved: selectedStatus.value,
      problemLevel: selectedLevel.value,
    };

    if(store.accessToken) {
      const response = await axios.get('http://localhost:8080/api/v1/problem', { params,
        headers: {
          Authorization: `Bearer ${store.accessToken}`
        }
      });
      problems.value = response.data;
    } else {
      const response = await axios.get('http://localhost:8080/api/v1/problem/guest', { params });
      problems.value = response.data;
    }
  } catch (error) {
    console.error('문제 목록을 불러오는 중 에러가 발생했습니다.', error.response ? error.response.data : error.message);
  }
};

watch(selectedLevel, () => {
  currentProblemPage.value = 1;
  fetchProblemList();
});

const goToCodingRoom = (codingRoomId, problemId) => {
  router.push({path:`/codingroom/${String(codingRoomId)}/${String(problemId)}`});
};

const handleProblemClick = async (problem) => {
  const result = confirm("해당 문제를 도전 하시겠습니까? \n확인을 누르시면 새로운 코딩 협업창으로 들어갑니다.");
  if(result){
    try {
      const response = await axios.post(`http://localhost:8080/api/v1/codingroom/${problem.problemId}`, {} , {
        headers: {
          Authorization: `Bearer ${store.accessToken}`
        }
      });

      const problemId = response.data.problemId;
      const codingRoomId = response.data.codingRoomId;

      goToCodingRoom(codingRoomId, problemId);
    } catch (error) {
      console.error('코딩방 생성 중 에러가 발생했습니다.', error.response ? error.response.data : error.message);
    }
  }
};

const handleLevelSelect = (level) => {
  selectedLevel.value = level;
  isLevelDropdownOpen.value = false;
};

const selectedLevelText = computed(() => {
  const selected = levelOptions.find(option => option.value === selectedLevel.value);
  return selected ? selected.label : '난이도';
});

const handleStatusSelect = (status) => {
  selectedStatus.value = status;
  isStatusDropdownOpen.value = false;
  currentProblemPage.value = 1;
  fetchProblemList();
};

const selectedStatusText = computed(() => {
  const selected = statusOptions.find(option => option.value === selectedStatus.value);
  return selected ? selected.label : '상태';
});

const handleClickOutside = (event) => {
  const levelDropdown = document.querySelector('.level-dropdown');
  const statusDropdown = document.querySelector('.status-dropdown');

  if (levelDropdown && !levelDropdown.contains(event.target) &&
      !event.target.classList.contains('level-filter-button')) {
    isLevelDropdownOpen.value = false;
  }

  if (statusDropdown && !statusDropdown.contains(event.target) &&
      !event.target.classList.contains('status-filter-button')) {
    isStatusDropdownOpen.value = false;
  }
};

const displayedProblems = computed(() => {
  let filteredProblems = problems.value;

  if (searchQuery.value) {
    filteredProblems = filteredProblems.filter(problem =>
        problem.title.toLowerCase().includes(searchQuery.value.toLowerCase())
    );
  }

  const startIdx = (currentProblemPage.value - 1) * ROWS_PER_PAGE;
  const endIdx = startIdx + ROWS_PER_PAGE;
  return filteredProblems.slice(startIdx, endIdx);
});

const emptyRowsCount = computed(() =>
    ROWS_PER_PAGE - displayedProblems.value.length
);

const totalProblemPages = computed(() => {
  let filteredProblems = problems.value;

  if (searchQuery.value) {
    filteredProblems = filteredProblems.filter(problem =>
        problem.title.toLowerCase().includes(searchQuery.value.toLowerCase())
    );
  }

  return Math.ceil(filteredProblems.length / ROWS_PER_PAGE);
});

const changeProblemPage = (page) => {
  if (page === 'prev' && currentProblemPage.value > 1) {
    currentProblemPage.value--;
  } else if (page === 'next' && currentProblemPage.value < totalProblemPages.value) {
    currentProblemPage.value++;
  } else if (typeof page === 'number') {
    currentProblemPage.value = page;
  }
};

function getCookie(name) {
  const value = `; ${document.cookie}`;
  const parts = value.split(`; ${name}=`);
  if (parts.length === 2) return parts.pop().split(';').shift();
  return null;
}

watch(searchQuery, () => {
  currentProblemPage.value = 1;
});

watch(selectedLevel, () => {
  currentProblemPage.value = 1;
});

const handleLevelFilter = (level) => {
  selectedLevel.value = selectedLevel.value === level ? null : level;
};

const goToLogin = () => {
  router.push('/login');
}

onMounted(() => {
  document.addEventListener('click', handleClickOutside);

  if (!store.accessToken) {
    const token = getCookie('accessToken');  // 쿠키에서 'token' 값 가져오기
    const token2 = getCookie('refreshToken');
    if (token) {
      store.login(token, token2);  // 로그인설정
    } else {
      console.log('쿠키에 토큰이 없습니다.');
    }
  }

  if (store.accessToken) {
    fetchProfile();
    fetchFriendList();
    isLogin.value = true;
  }
  fetchProblemList();

});

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside);
});
</script>


<template>
  <div class="main-container">
    <div class="container">
      <div class="problem-container">
        <h1 class="title">문제 목록</h1>

        <div class="search-area">
          <input
              type="text"
              class="search-input"
              placeholder="문제 제목 검색"
              v-model="searchQuery"
          >
        </div>

        <div class="filter-area">
          <div class="status-filter-container" v-if="isLogin">
            <button
                class="filter-button status-filter-button"
                @click="isStatusDropdownOpen = !isStatusDropdownOpen"
            >
              {{ selectedStatusText }} ▼
            </button>
            <div class="status-dropdown" v-if="isStatusDropdownOpen">
              <div
                  v-for="option in statusOptions"
                  :key="String(option.value)"
                  class="status-option"
                  :class="{ active: selectedStatus === option.value }"
                  @click="handleStatusSelect(option.value)"
              >
                {{ option.label }}
              </div>
            </div>
          </div>
          <div class="level-filter-container">
            <button
                class="filter-button level-filter-button"
                @click="isLevelDropdownOpen = !isLevelDropdownOpen"
            >
              {{ selectedLevelText }} ▼
            </button>
            <div class="level-dropdown" v-if="isLevelDropdownOpen">
              <div
                  v-for="option in levelOptions"
                  :key="option.value"
                  class="level-option"
                  :class="{ active: selectedLevel === option.value }"
                  @click="handleLevelSelect(option.value)"
              >
                {{ option.label }}
              </div>
            </div>
          </div>
        </div>

        <table class="table">
          <thead>
          <tr>
            <th>번호</th>
            <th>제목</th>
            <th>난이도</th>
            <th>상태</th>
          </tr>
          </thead>
          <tbody>
          <tr
              v-for="(problem, index) in displayedProblems"
              @click="handleProblemClick(problem)"
          >
            <td>{{ (currentProblemPage - 1) * ROWS_PER_PAGE + index + 1 }}</td>
            <td>{{ problem.title }}</td>
            <td><span class="level-badge">Lv. {{ problem.problemLevel }}</span></td>
            <td>
              <span :class="['status', problem.solved ? 'status-solved' : 'status-unsolved']">
                {{ problem.solved ? '해결' : '미해결' }}
              </span>
            </td>
          </tr>
          </tbody>
        </table>

        <div class="pagination">
          <button
              @click="changeProblemPage('prev')"
              :disabled="currentProblemPage === 1"
          >◀
          </button>
          <button
              v-for="page in totalProblemPages"
              :key="page"
              @click="changeProblemPage(page)"
              :class="{ active: currentProblemPage === page }"
          >
            {{ page }}
          </button>
          <button
              @click="changeProblemPage('next')"
              :disabled="currentProblemPage === totalProblemPages"
          >▶
          </button>
        </div>
      </div>
    </div>
    <div class="profile-container" v-if="isLogin">
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
    <div class="profile-container" v-else>
      <div class="profile-card login-card">
        <div class="login-message">환영합니다! 👋</div>
        <div class="login-description">
          로그인하시면 친구와 함께 풀기 등<br>
          더 많은 기능을 사용하실 수 있습니다.
        </div>
        <button class="login-button" @click="goToLogin">
          로그인하러 가기
        </button>
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

.filter-area {
  display: flex;
  justify-content: center;
  gap: 10px;
  margin-bottom: 12px;
}

.filter-button {
  padding: 6px 12px;
  border: 1px solid #e1e1e1;
  border-radius: 4px;
  background: white;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
}

.filter-button:hover {
  background: #f5f5f5;
}

.filter-button.active {
  background: #1a1b3a;
  color: white;
  border-color: #1a1b3a;
}

.status-filter-container {
  position: relative;
  display: inline-block;
}

.status-filter-button {
  min-width: 60px;
  text-align: left;
}

.status-dropdown {
  position: absolute;
  top: 100%;
  left: 0;
  z-index: 1000;
  min-width: 120px;
  background: white;
  border: 1px solid #e1e1e1;
  border-radius: 4px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  margin-top: 4px;
  transform-origin: top;
  animation: dropdownAnimation 0.2s ease-out forwards;
}

.status-option {
  padding: 8px 16px;
  cursor: pointer;
  transition: all 0.2s;
  text-align: left;
}

.status-option:hover {
  background: #f5f5f5;
}

.status-option.active {
  background: #1a1b3a;
  color: white;
}

.status-option:not(:last-child) {
  border-bottom: 1px solid #f0f0f0;
}

.level-filter-container {
  position: relative;
  display: inline-block;
}

.level-filter-button {
  min-width: 60px;
  text-align: left;
}

.level-dropdown {
  position: absolute;
  top: 100%;
  left: 0;
  z-index: 1000;
  min-width: 120px;
  background: white;
  border: 1px solid #e1e1e1;
  border-radius: 4px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  margin-top: 4px;
}

.level-option {
  padding: 8px 16px;
  cursor: pointer;
  transition: all 0.2s;
  text-align: left;
}

.level-option:hover {
  background: #f5f5f5;
}

.level-option.active {
  background: #1a1b3a;
  color: white;
}

.level-option:not(:last-child) {
  border-bottom: 1px solid #f0f0f0;
}

.level-dropdown {
  transform-origin: top;
  animation: dropdownAnimation 0.2s ease-out forwards;
}

@keyframes dropdownAnimation {
  from {
    opacity: 0;
    transform: scaleY(0);
  }
  to {
    opacity: 1;
    transform: scaleY(1);
  }
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
  margin-left: 4px;
  margin-right: 4px;
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

.login-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  padding: 32px 24px;
}

.login-message {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin-bottom: 12px;
}

.login-description {
  font-size: 14px;
  color: #666;
  margin-bottom: 24px;
  line-height: 1.6;
}

.login-button {
  background-color: #4285f4;
  color: white;
  border: none;
  padding: 12px 24px;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  gap: 8px;
}

.login-button:hover {
  background-color: #3367d6;
  transform: translateY(-1px);
}

.login-button svg {
  width: 20px;
  height: 20px;
}

.provider-icon {
  width: 16px;
  height: 16px;
}
</style>