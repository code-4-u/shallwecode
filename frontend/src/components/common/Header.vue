<script setup>
import {inject, onMounted, onUnmounted, ref} from 'vue'
import axios from "axios";
import {useAuthStore} from "@/stores/auth.js";
import {getFetch, putFetch} from "@/stores/apiClient.js";

const authStore = useAuthStore();

const showNotis = ref(false);
const notis = ref([]);
const refreshNotiList = inject('refreshNotiList');
const store = useAuthStore();
const isLogin = ref(0);

const fetchMyNotReadNotiList = async () => {
  try {
    const response = await getFetch('http://localhost:8080/api/v1/noti');
    notis.value = response.data;
  } catch (error) {
    console.error('알림 목록을 불러오는 중 에러가 발생했습니다.', error.response ? error.response.data : error.message);
  }
};

const handleNotiClick = async (noti) => {
  try {
    await putFetch(`http://localhost:8080/api/v1/noti`, {
      notiId: noti.notiId
    });
    await fetchMyNotReadNotiList();
    refreshNotiList.value();
  } catch (error) {
    console.error('알림 읽음 상태 변경 중 에러가 발생했습니다.', error.response ? error.response.data : error.message);
  }
};

const toggleNotis = async () => {
  showNotis.value = !showNotis.value
  if (showNotis.value) {
    await fetchMyNotReadNotiList();
  }
}

const closeNotis = (event) => {
  const dropdown = document.querySelector('.notifications-dropdown')
  const notiBtn = document.querySelector('.notification-btn')

  if (dropdown && !dropdown.contains(event.target) && !notiBtn.contains(event.target)) {
    showNotis.value = false
  }
}

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

const logout = async () => {
  const response = await axios.post('http://localhost:8080/api/v1/user/logout', {}, {
    headers: {
      Authorization: `Bearer ${store.accessToken}`
    },
    withCredentials: true, // 쿠키를 포함시키기 위해
  });

  if (response.status === 200) { // 응답 상태가 200인지 확인
    alert('로그아웃 성공');
    deleteCookies();
    window.location.href = "http://localhost:5173"; // 홈 페이지로 리다이렉트
  } else {
    alert('로그아웃에 실패했습니다.');
  }
}

const deleteCookies = () => {
  document.cookie = 'accessToken=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;';
  document.cookie = 'refreshToken=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;';
}

const handleLogoutClick = () => {
  logout();
  store.logout();
  deleteCookies();
  alert('로그아웃 되었습니다.');
  window.location.reload();
}

function getCookie(name) {
  const value = `; ${document.cookie}`;
  const parts = value.split(`; ${name}=`);
  if (parts.length === 2) return parts.pop().split(';').shift();
  return null;
}

onMounted(() => {

  if (!store.accessToken) {
    const token = getCookie('accessToken');  // 쿠키에서 'token' 값 가져오기
    const token2 = getCookie('refreshToken');
    if (token) {
      console.log('쿠키에서 토큰을 가져왔습니다:', token);
      console.log('리프레시 토큰 : ', token2);
      store.login(token, token2);  // 로그인설정
    } else {
      console.log('쿠키에 토큰이 없습니다.');
    }
  }

  if (store.accessToken != null) {
    if (store.userRole === 'ADMIN') {
      isLogin.value = 2;
    } else {
      isLogin.value = 1;
    }
  }

  if (isLogin.value) {
    fetchMyNotReadNotiList();
    document.addEventListener('click', closeNotis)
  }
})

onUnmounted(() => {
  document.removeEventListener('click', closeNotis)
})
</script>

<template>
  <header class="header">
    <router-link to="/" class="logo">ShallWeCode</router-link>
    <div v-if='isLogin' class="menu">
      <router-link v-if="isLogin === 2" to="/admin" class="menu-item">관리자 모드</router-link>
      <router-link to="/mypage" class="menu-item">마이페이지</router-link>
      <router-link to="/" class="menu-item">문제 목록</router-link>
      <router-link to="/user-list" class="menu-item">회원 목록</router-link>
      <div class="menu-item" @click="handleLogoutClick">로그아웃</div>
      <div class="notification-container">
        <button @click="toggleNotis" class="notification-btn">
          알림
        </button>
        <div v-if="showNotis" class="notifications-dropdown">
          <div v-if="notis.length" class="notifications-list">
            <div v-for="noti in notis"
                 class="notification-item"
                 @click="handleNotiClick(noti)">
              <span class="notification-message">{{ noti.content }}</span>
              <span class="notification-date">{{ formatDate(noti.createdAt) }}</span>
            </div>
          </div>
          <div v-else class="no-notifications">
            새로운 알림이 없습니다.
          </div>
        </div>
      </div>
    </div>
    <div v-else class="menu">
      <router-link to="/login" class="menu-item">로그인</router-link>
    </div>
  </header>
</template>

<style scoped>
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.75rem 2rem;
  background-color: #1a1b2e;
  color: white;
  height: 30px;
}

.logo {
  font-size: 1.25rem;
  font-weight: bold;
  color: #8be9fd;
  text-decoration: none;
}

.menu {
  display: flex;
  gap: 1.5rem;
}

.menu-item:not(.notification-btn) {
  color: white;
  text-decoration: none;
  font-size: 0.875rem;
  background: none;
  border: none;
  padding: 0;
  cursor: pointer;
}

.menu-item:hover {
  color: #8be9fd;
}

.notification-btn {
  color: white;
  text-decoration: none;
  font-size: 0.875rem;
  background: none;
  border: none;
  padding: 0;
  cursor: pointer;
  line-height: 1;
  display: flex;
  align-items: center;
  height: 100%;
  margin-top: 1.5px;
}

.notification-btn:hover {
  color: #8be9fd;
}

.notification-container {
  position: relative;
}

.notifications-dropdown {
  position: absolute;
  top: calc(100% + 10px);
  right: 0;
  background-color: white;
  border: 1px solid #eee;
  border-radius: 4px;
  width: 300px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  z-index: 1000;
}

.notifications-list {
  max-height: 300px;
  overflow-y: auto;
}

.notification-item {
  padding: 12px;
  border-bottom: 1px solid #eee;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.notification-item:last-child {
  border-bottom: none;
}

.notification-message {
  color: #333;
  font-size: 0.875rem;
}

.notification-date {
  color: #666;
  font-size: 0.75rem;
}

.no-notifications {
  padding: 12px;
  text-align: center;
  color: #666;
  font-size: 0.875rem;
}


.notification-item {
  cursor: pointer;
}

.notification-item:hover {
  background-color: #f5f5f5;
}
</style>