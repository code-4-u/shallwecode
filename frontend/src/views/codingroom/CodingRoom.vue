<script setup>
import CodeEditor from "@/components/codingroom/CodeEditor.vue";
import ProblemDescription from "@/components/codingroom/ProblemDescription.vue";
import Chat from "@/components/codingroom/ChatArea.vue";

import { useRoute } from 'vue-router';
import {reactive, onMounted} from 'vue';
import axios from "axios";
import {useAuthStore} from "@/stores/auth.js";

const route = useRoute();
const codingRoomId = route.params.codingRoomId;
const problemId = route.params.problemId;
const useAuth = useAuthStore();


// 조회한 문제 정보를 담을 객체
const problemInfo = reactive({
  problemId : 0,
  title : '',
  content : '',
  problemLevel : ''
});

const authObjectInfo = {
  userId : useAuth.userId,
  accessToken : useAuth.accessToken,
  refreshToken : useAuth.refreshToken
}

const fetchProblemInfo = async (problemId) => {
  const response = await axios.get(`http://localhost:8080/api/v1/problem/${problemId}`, {
    headers : {
      Authorization: `Bearer ${authObjectInfo.accessToken}`
    }
  });

  problemInfo.problemId = response.data.problemId;
  problemInfo.title = response.data.title;
  problemInfo.content = response.data.content;
  problemInfo.problemLevel = response.data.problemLevel;
}

onMounted(async () => {
  await fetchProblemInfo(problemId);
});

</script>

<template>
  <div class="container">
    <div class="problem-container">
      <div class="content-wrapper">
        <ProblemDescription :content="problemInfo.content" />
        <Chat :codingRoomId="codingRoomId" />
      </div>
    </div>
    <div class="problem-container">
      <div class="content-wrapper">
        <CodeEditor :problemId="problemInfo.problemId"
        :codingRoomId="codingRoomId"/>
        <CodeResult/>
      </div>
    </div>
  </div>
</template>

<style scoped>
.container {
  display: flex;
  max-width: 100%;
}

.content-wrapper {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.problem-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  width: 50%;
  min-width: 600px;
  height: 641px;
  background-color: var(--editor-color);
  overflow: hidden;
}
</style>