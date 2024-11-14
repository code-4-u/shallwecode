<script setup>
import { onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import axios from 'axios';

const router = useRouter();
const route = useRoute();

const userId = ref(null);
const nickname = ref('');
const email = ref('');

const registerUser = async () => {
  try {
    console.log(email.value)
    await axios.put('http://localhost:8080/api/v1/user',
        {
          nickname: nickname.value,
          userId: userId.value,
          email: email.value,
        }
    );
    alert('회원가입이 완료되었습니다! 해당 계정으로 로그인해주세요.')
    await router.push('/');
  } catch (error) {
    console.error('가입 실패:', error);
  }
};

onMounted(() => {
  userId.value = route.query.userId;
})
</script>

<template>
  <div class="signup-container">
    <div class="signup-card">
      <h1 class="signup-title">회원가입</h1>
      <form @submit.prevent="registerUser" class="signup-form">
        <div class="form-group">
          <label for="nickname">닉네임</label>
          <input
              type="text"
              v-model="nickname"
              id="nickname"
              required
              class="form-input"
              placeholder="닉네임을 입력하세요"
          />
        </div>
        <div class="form-group">
          <label for="email">이메일</label>
          <input
              type="email"
              v-model="email"
              id="email"
              required
              class="form-input"
              placeholder="이메일을 입력하세요"
          />
        </div>
        <button type="submit" class="submit-button">가입하기</button>
      </form>
    </div>
  </div>
</template>

<style scoped>
.signup-container {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: var(--background-color);
  padding: 20px;
}

.signup-card {
  background: white;
  padding: 2rem;
  border-radius: 12px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  width: 100%;
  max-width: 400px;
}

.signup-title {
  text-align: center;
  color: #333;
  font-size: 1.8rem;
  margin-bottom: 2rem;
  font-weight: 600;
}

.signup-form {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.form-group label {
  color: #555;
  font-size: 0.9rem;
  font-weight: 500;
}

.form-input {
  padding: 0.75rem;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 1rem;
  transition: border-color 0.2s, box-shadow 0.2s;
}

.form-input:focus {
  outline: none;
  border-color: #4a90e2;
  box-shadow: 0 0 0 2px rgba(74, 144, 226, 0.2);
}

.submit-button {
  background-color: #4a90e2;
  color: white;
  padding: 0.75rem;
  border: none;
  border-radius: 6px;
  font-size: 1rem;
  font-weight: 500;
  cursor: pointer;
  transition: background-color 0.2s;
  margin-top: 1rem;
}

.submit-button:hover {
  background-color: #357abd;
}

.submit-button:active {
  transform: translateY(1px);
}
</style>