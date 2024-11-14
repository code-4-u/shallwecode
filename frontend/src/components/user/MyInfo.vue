<script setup>
import {computed, onMounted, ref} from "vue";
import axios from "axios";
import {delFetch, getFetch, putFetch} from "@/stores/apiClient.js";
import kakaoIcon from '@/assets/icons/kakao.svg'
import naverIcon from '@/assets/icons/naver.svg'
import googleIcon from '@/assets/icons/google.svg'
import {useAuthStore} from "@/stores/auth.js";

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

const authStore = useAuthStore();
const profile = ref('');
const isEditing = ref(false);
const tempUsername = ref('');

const fetchProfile = async () => {
  try {
    const response = await getFetch('http://localhost:8080/api/v1/user/profile');
    profile.value = response.data;
  } catch (error) {
    console.error('프로필을 불러오는데 에러가 발생했습니다.', error.response ? error.response.data : error.message);
  }
};

const handleUpdateClick = () => {
  if (isEditing.value) {
    handleSaveClick();
  } else {
    tempUsername.value = profile.value.nickname;
    isEditing.value = true;
  }
}

const handleSaveClick = async () => {
  try {
    if (!tempUsername.value.trim()) {
      alert('닉네임을 입력해주세요.');
      return;
    }

    await putFetch('http://localhost:8080/api/v1/user/nickname', {
      nickname: tempUsername.value
    })

    isEditing.value = false;
    await fetchProfile();
  } catch (error) {
    console.error('닉네임을 수정하는데 에러가 발생했습니다.', error.response ? error.response.data : error.message);
    alert('닉네임 수정에 실패했습니다.');
  }
}

const deleteCookies = () => {
  document.cookie = 'accessToken=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;';
  document.cookie = 'refreshToken=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;';
}

const handleDeleteClick = async () => {
  try {
    await delFetch('http://localhost:8080/api/v1/user');
    authStore.logout();
    deleteCookies();
    alert('계정이 탈퇴되었습니다.');
    window.location.href = "http://localhost:5173";
  } catch (error) {
    console.error('계정을 탈퇴하는데 에러가 발생했습니다.', error.response ? error.response.data : error.message);
    alert('계정 탈퇴에 실패했습니다.');
  }
}

onMounted(() => {
  fetchProfile();
});

</script>

<template>
  <div class="profile-container">
    <div class="info-section">

      <h2 class="section-title">회원 정보</h2>

      <div class="basic-info">
        <div class="profile-image">
          <img src="@/assets/icons/profile.svg" alt="프로필 사진"/>
        </div>

        <div class="user-details">
          <div class="name-info">
            <div class="username" v-if="!isEditing">
              {{ profile.nickname }}
            </div>
            <input
                v-else
                v-model="tempUsername"
                class="username-input"
                type="text"
                @keyup.enter="handleSaveClick"
                :placeholder="profile.nickname"
            />
            <div class="update"
                 v-if="!isEditing"
                 @click="handleUpdateClick">
              수정
            </div>
            <div class="update"
                 v-else
                 @click="handleUpdateClick">
              저장
            </div>
          </div>
          <div class="email">
            {{ profile.email }}
            <img v-if="profile.provider" :src="providerIcon" :alt="profile.provider" class="provider-icon"/>
          </div>
        </div>
      </div>

      <div class="stats-container">
        <div class="stat-item">
          <span class="stat-label">도전한 문제</span>
          <span class="stat-value">{{ profile.doingProblemCnt }}개</span>
        </div>
        <div class="stat-item">
          <span class="stat-label">미해결 문제</span>
          <span class="stat-value">{{ profile.notFinishedProblemCnt }}개</span>
        </div>
        <div class="stat-item">
          <span class="stat-label">해결한 문제</span>
          <span class="stat-value">{{ profile.finishedProblemCnt }}개</span>
        </div>
      </div>
    </div>

    <!-- 계정 탈퇴 섹션 -->
    <div class="withdrawal-section">
      <h2 class="section-title">계정 탈퇴</h2>
      <div class="withdrawal-content">
        <p>계정 탈퇴 시 프로필이 삭제됩니다.</p>
        <button class="withdrawal-btn" @click="handleDeleteClick">탈퇴하기</button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.profile-container {
  max-width: 600px;
  margin: 0 auto;
  padding: 20px;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}

.section-title {
  font-size: 18px;
  font-weight: bold;
  margin-bottom: 20px;
  color: #333;
}

.info-section, .withdrawal-section {
  background-color: #fff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  margin-bottom: 20px;
}

.basic-info {
  display: flex;
  align-items: center;
  margin-bottom: 24px;
}

.profile-image {
  width: 80px;
  height: 80px;
  min-width: 80px;
  min-height: 80px;
  max-width: 80px;
  max-height: 80px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-left: 24px;
  margin-right: 24px;
  background-color: #B2DFDB;
  border-radius: 50%;
  overflow: hidden;
}

.profile-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  opacity: 1;
}

.user-details {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.name-info {
  display: flex;
  gap: 6px;
  align-items: center;
}

.username {
  font-size: 18px;
  font-weight: 600;
  color: #2196F3;
  margin-bottom: 4px;
}

.update {
  font-size: 12px;
  cursor: pointer;
}

.email {
  font-size: 16px;
  color: #666;
  display: flex;
  align-items: center;
  gap: 8px;
}

.naver-badge {
  background-color: #03C75A;
  color: white;
  padding: 2px 4px;
  border-radius: 2px;
  font-size: 12px;
  font-weight: bold;
}

.stats-container {
  display: flex;
  justify-content: space-between;
  border-top: 1px solid #eee;
  padding-top: 20px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex: 1;
}

.stat-label {
  font-size: 14px;
  color: #666;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 16px;
  font-weight: 500;
  color: #333;
}

.withdrawal-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.withdrawal-content p {
  color: #666;
  font-size: 14px;
}

.withdrawal-btn {
  background-color: white;
  color: #FF5252;
  border: 1px solid #FF5252;
  padding: 8px 16px;
  border-radius: 4px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
}

.withdrawal-btn:hover {
  background-color: #FF5252;
  color: white;
}

.username-input {
  font-size: 18px;
  font-weight: 500;
  color: #404040;
  background-color: #F4F4F4;
  border: 1px solid #D9D9D9;
  border-radius: 4px;
  padding: 4px 8px;
  outline: none;
  width: 150px;
}

.username-input:focus {
  border-color: #D9D9D9;
  box-shadow: 0 0 0 2px rgba(33, 150, 243, 0.1);
}

.update {
  font-size: 12px;
  cursor: pointer;
  color: #404040;
  padding: 4px 8px;
  border-radius: 4px;
  transition: background-color 0.2s;
}

.update:hover {
  background-color: rgba(33, 150, 243, 0.1);
}

.provider-icon {
  width: 16px;
  height: 16px;
}
</style>