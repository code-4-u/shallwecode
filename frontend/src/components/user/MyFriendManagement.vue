<script setup>
import {computed, onMounted, ref, watch} from "vue";
import axios from "axios";
import {useAuthStore} from "@/stores/auth.js";
import {getFetch, postFetch} from "@/stores/apiClient.js";

const authStore = useAuthStore();

const ROWS_PER_PAGE = 7;

const currentFriendPage = ref(1);
const currentRequestPage = ref(1);
const searchFriendQuery = ref('');
const searchRequestQuery = ref('');

const isLoading = ref(false);

const friends = ref([]);
const requests = ref([]);

const fetchMyFriendList = async () => {
  try {
    const response = await getFetch('http://localhost:8080/api/v1/friend');
    friends.value = response.data;
  } catch (error) {
    console.error('친구 목록을 불러오는 중 에러가 발생했습니다.', error.response ? error.response.data : error.message);
  }
};

const fetchMyRequestList = async () => {
  try {
    const response = await getFetch('http://localhost:8080/api/v1/friend/request');
    requests.value = response.data;
  } catch (error) {
    console.error('친구 요청 목록을 불러오는 중 에러가 발생했습니다.', error.response ? error.response.data : error.message);
  }
};

const acceptFriendRequest = async (request) => {
  isLoading.value = true;
  try {
    await postFetch(`http://localhost:8080/api/v1/friend/request/accept`, {
      fromUserId: request.userId
    });
    alert('친구 요청을 수락했습니다.');
    await fetchMyRequestList();
    await fetchMyFriendList();
  } catch (error) {
    console.error('친구 요청 수락 중 에러가 발생했습니다.', error.response ? error.response.data : error.message);
    alert('친구 요청 수락에 실패했습니다.');
  } finally {
    isLoading.value = false;
  }
};

const rejectFriendRequest = async (request) => {
  isLoading.value = true;
  try {
    await postFetch(`http://localhost:8080/api/v1/friend/request/reject`, {
      fromUserId: request.userId
    });
    alert('친구 요청을 거절했습니다.');
    await fetchMyRequestList();
    await fetchMyFriendList();
  } catch (error) {
    console.error('친구 요청 거절 중 에러가 발생했습니다.', error.response ? error.response.data : error.message);
    alert('친구 요청 거절에 실패했습니다.');
  } finally {
    isLoading.value = false;
  }
};

const deleteFriend = async (friend) => {
  isLoading.value = true;
  try {
    await axios.delete(`http://localhost:8080/api/v1/friend`, {
      params: {
        userId: friend.userId
      },
      headers: {
        Authorization: `Bearer ${authStore.accessToken}`,
      }
    });
    alert('친구를 삭제했습니다.');
    await fetchMyFriendList();
  } catch (error) {
    console.error('친구 삭제 중 에러가 발생했습니다.', error.response ? error.response.data : error.message);
    alert('친구 삭제 실패했습니다.');
  } finally {
    isLoading.value = false;
  }
};

const filteredFriends = computed(() => {
  if (!searchFriendQuery.value) return friends.value;
  return friends.value.filter(friend =>
      friend.nickname.toLowerCase().includes(searchFriendQuery.value.toLowerCase())  // nickName -> nickname
  );
});

const filteredRequests = computed(() => {
  if (!searchRequestQuery.value) return requests.value;
  return requests.value.filter(request =>
      request.nickname.toLowerCase().includes(searchRequestQuery.value.toLowerCase())  // nickName -> nickname
  );
});

const displayedFriends = computed(() => {
  const startIdx = (currentFriendPage.value - 1) * ROWS_PER_PAGE;
  const endIdx = startIdx + ROWS_PER_PAGE;
  return filteredFriends.value.slice(startIdx, endIdx);
});

const displayedRequests = computed(() => {
  const startIdx = (currentRequestPage.value - 1) * ROWS_PER_PAGE;
  const endIdx = startIdx + ROWS_PER_PAGE;
  return filteredRequests.value.slice(startIdx, endIdx);
});

const emptyRowsFriendCount = computed(() =>
    ROWS_PER_PAGE - displayedFriends.value.length
);

const emptyRowsRequestCount = computed(() =>
    ROWS_PER_PAGE - displayedRequests.value.length
);

const totalFriendPages = computed(() =>
    Math.ceil(filteredFriends.value.length / ROWS_PER_PAGE)
);

const totalRequestPages = computed(() =>
    Math.ceil(filteredRequests.value.length / ROWS_PER_PAGE)
);

const changeFriendPage = (page) => {
  if (page === 'prev' && currentFriendPage.value > 1) {
    currentFriendPage.value--;
  } else if (page === 'next' && currentFriendPage.value < totalFriendPages.value) {
    currentFriendPage.value++;
  } else if (typeof page === 'number') {
    currentFriendPage.value = page;
    currentFriendPage.value = page;
  }
};

const changeRequestPage = (page) => {
  if (page === 'prev' && currentRequestPage.value > 1) {
    currentRequestPage.value--;
  } else if (page === 'next' && currentRequestPage.value < totalRequestPages.value) {
    currentRequestPage.value++;
  } else if (typeof page === 'number') {
    currentRequestPage.value = page;
  }
};

watch(searchFriendQuery, () => {
  currentFriendPage.value = 1;
});

watch(searchRequestQuery, () => {
  currentRequestPage.value = 1;
});

onMounted(() => {
  fetchMyFriendList();
  fetchMyRequestList()
});
</script>

<template>
  <div class="container">
    <div class="friend-container">
      <h1 class="title">친구 목록</h1>

      <div class="search-area">
        <input
            type="text"
            class="search-input"
            placeholder="닉네임 검색"
            v-model="searchFriendQuery"
        >
      </div>

      <table class="table">
        <thead>
        <tr>
          <th>번호</th>
          <th>닉네임</th>
          <th></th>
        </tr>
        </thead>
        <tbody>
        <tr v-for="(friend, index) in displayedFriends">
          <td>{{ (currentFriendPage - 1) * ROWS_PER_PAGE + index + 1 }}</td>
          <td>{{ friend.nickname }}</td>
          <td class="delete-button">
            <button
                @click="deleteFriend(friend)"
                :disabled="isLoading"
            >
              {{ isLoading ? '처리중...' : '삭제' }}
            </button>
          </td>
        </tr>
        <tr v-for="i in emptyRowsFriendCount" :key="`empty-${i}`" class="empty-row">
          <td>-</td>
          <td>-</td>
          <td>-</td>
        </tr>
        </tbody>
      </table>

      <div class="pagination">
        <button
            @click="changeFriendPage('prev')"
            :disabled="currentFriendPage === 1"
        >◀
        </button>
        <button
            v-for="page in totalFriendPages"
            :key="page"
            @click="changeFriendPage(page)"
            :class="{ active: currentFriendPage === page }"
        >
          {{ page }}
        </button>
        <button
            @click="changeFriendPage('next')"
            :disabled="currentFriendPage === totalFriendPages"
        >▶
        </button>
      </div>
    </div>

    <div class="request-container">
      <h1 class="title">친구 요청 목록</h1>

      <div class="search-area">
        <input
            type="text"
            class="search-input"
            placeholder="닉네임 검색"
            v-model="searchRequestQuery"
        >
      </div>

      <table class="table">
        <thead>
        <tr>
          <th>번호</th>
          <th>닉네임</th>
          <th></th>
        </tr>
        </thead>
        <tbody>
        <tr v-for="(request, index) in displayedRequests">
          <td>{{ (currentRequestPage - 1) * ROWS_PER_PAGE + index + 1 }}</td>
          <td>{{ request.nickname }}</td>
          <td>
            <button class="accept-btn"
                    @click="acceptFriendRequest(request)"
                    :disabled="isLoading"
            >
              {{ isLoading ? '처리중...' : '수락' }}
            </button>
            <button class="reject-btn"
                    @click="rejectFriendRequest(request)"
                    :disabled="isLoading"
            >
              {{ isLoading ? '처리중...' : '거절' }}
            </button>
          </td>
        </tr>
        <tr v-for="i in emptyRowsRequestCount" :key="`empty-${i}`" class="empty-row">
          <td>-</td>
          <td>-</td>
          <td>-</td>
        </tr>
        </tbody>
      </table>

      <div class="pagination">
        <button
            @click="changeRequestPage('prev')"
            :disabled="currentRequestPage === 1"
        >◀
        </button>
        <button
            v-for="page in totalRequestPages"
            :key="page"
            @click="changeRequestPage(page)"
            :class="{ active: currentRequestPage === page }"
        >
          {{ page }}
        </button>
        <button
            @click="changeRequestPage('next')"
            :disabled="currentRequestPage === totalRequestPages"
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

.accept-btn {
  background-color: #71E46D;
  color: black;
  border: 1px solid black;
  border-radius: 2px;
}

.reject-btn {
  background-color: #F15959;
  color: black;
  border: 1px solid black;
  border-radius: 2px;
}

.accept-btn:hover {
  background-color: #45a049;
}

.reject-btn:hover {
  background-color: #da190b;
}
</style>