<template>
  <div class="problem-register">
    <h2>문제 수정 하기</h2>

    <div class="form-group">
      <label for="title">제목</label>
      <input type="text" id="title" v-model="title" placeholder="문제 제목을 입력해 주세요" />
    </div>

    <div class="form-group">
      <label for="content">내용</label>
      <textarea id="content" v-model="content" rows="10" placeholder="문제 설명을 입력하세요"></textarea>
    </div>

    <div class="form-group">
      <label for="problemLevel">난이도</label>
      <select id="problemLevel" v-model="problemLevel">
        <option value="1">Lv.1</option>
        <option value="2">Lv.2</option>
        <option value="3">Lv.3</option>
        <option value="4">Lv.4</option>
        <option value="5">Lv.5</option>
      </select>
    </div>

    <div class="form-group">
      <label>테스트 케이스</label>
      <button @click="addTestCase" class="add-button">추가</button>
      <table class="test-case-table">
        <thead>
        <tr>
          <th>입력</th>
          <th>출력</th>
        </tr>
        </thead>
        <tbody>
        <tr v-for="(testCase, index) in testCases" :key="index">
          <td><input type="text" v-model="testCase.input" /></td>
          <td><input type="text" v-model="testCase.output" /></td>
        </tr>
        </tbody>
      </table>
    </div>

    <div class="button-group">
      <button @click="cancel" class="cancel-button">취소</button>
      <button @click="updateProblem" class="register-button">수정</button>
    </div>
  </div>
</template>

<script>
import { ref, onMounted } from "vue";
import axios from "axios";
import { useRouter, useRoute } from "vue-router";
import { useAuthStore } from "@/stores/auth.js";

export default {
  setup() {
    const authStore = useAuthStore();
    const router = useRouter();
    const route = useRoute();
    const title = ref("");
    const content = ref("");
    const problemLevel = ref(1);
    const testCases = ref([{ input: "", output: "" }]);

    onMounted(async () => {
      const problemId = route.params.problemId;
      if (!problemId) {
        console.error("문제 ID가 없습니다.");
        return;
      }
      try {
        const response = await axios.get(`http://localhost:8080/api/v1/problem/${problemId}`, {
          headers: {
            Authorization: `Bearer ${authStore.accessToken}`,
          },
        });
        const data = response.data;
        title.value = data.title;
        content.value = data.content;
        problemLevel.value = data.problemLevel;
        testCases.value = data.testcases;
      } catch (error) {
        console.error("문제 정보를 불러오는데 실패했습니다:", error);
        alert("문제 정보를 불러오는데 실패했습니다.");
      }
    });

    const addTestCase = () => {
      testCases.value.push({input: "", output: ""});
    };

    const cancel = () => {
      window.history.back();
    };

    const updateProblem = async () => {
      const problemId = route.params.problemId;
      const formData = {
        title: title.value,
        content: content.value,
        problemLevel: problemLevel.value,
        testcases: testCases.value,
      };
      try {
        const response = await axios.put(`http://localhost:8080/api/v1/problem/${problemId}`, formData, {
          headers: {
            Authorization: `Bearer ${authStore.accessToken}`,
          },
        });
        console.log("문제가 성공적으로 수정되었습니다:", response.data);
        await router.push("/admin");
      } catch (error) {
        console.error("문제 수정 중 오류가 발생했습니다:", error);
        alert("문제 수정에 실패했습니다. 다시 시도해주세요.");
      }
    };

    return {
      title,
      content,
      problemLevel,
      testCases,
      addTestCase,
      cancel,
      updateProblem,
    };
  },
};
</script>

<style scoped>
.problem-register {
  padding: 20px;
  background-color: #f9f9f9;
  border-radius: 8px;
  max-width: 800px;
  margin: 0 auto;
}

h2 {
  text-align: center;
}

.form-group {
  margin-bottom: 20px;
}

label {
  font-weight: bold;
  display: block;
  margin-bottom: 5px;
}

input[type="text"],
textarea,
select {
  width: 100%;
  padding: 10px;
  font-size: 16px;
  border: 1px solid #ddd;
  border-radius: 4px;
}

textarea {
  resize: vertical;
}

.test-case-table {
  width: 100%;
  border-collapse: collapse;
  margin-top: 10px;
}

.test-case-table th,
.test-case-table td {
  border: 1px solid #ddd;
  padding: 8px;
  text-align: center;
}

.add-button {
  background-color: #e0e0e0;
  color: #333;
  border: none;
  padding: 8px 16px;
  cursor: pointer;
  margin-bottom: 10px;
}

.add-button:hover {
  background-color: #d0d0d0;
}

.button-group {
  display: flex;
  justify-content: center;
  gap: 10px;
}

.cancel-button,
.register-button {
  padding: 10px 20px;
  font-size: 16px;
  cursor: pointer;
  border: none;
  border-radius: 4px;
}

.cancel-button {
  background-color: #ccc;
  color: #333;
}

.register-button {
  background-color: #4CAF50;
  color: white;
}

.register-button:hover {
  background-color: #45a049;
}
</style>
