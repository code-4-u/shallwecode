<script setup>
import {ref, onMounted, shallowRef} from "vue";
import * as Y from 'yjs';
import {WebsocketProvider} from "y-websocket";
import {MonacoBinding} from "y-monaco";
import * as monaco from 'monaco-editor';
import axios from "axios";
import {useAuthStore} from "@/stores/auth.js";
import {postFetch} from "@/stores/apiClient.js";

const props = defineProps({
  problemId: {
    type: String,
    required: true
  },
  codingRoomId: {
    type: String,
    required: true
  }
});

const MONACO_EDITOR_OPTIONS = {
  automaticLayout: true,
  formatOnType: true,
  formatOnPaste: true,
  theme: 'vs-dark'
};

const languages = [
  {id: 'java', name: 'JAVA'},
  {id: 'python', name: 'Python'}
];

const authStore = useAuthStore();

const code = ref('public class Solution {\n' +
    '    public static void main(String[] args) {\n' +
    '        \n' +
    '    }\n' +
    '}');
const editorInstance = shallowRef(null);
const showDropdown = ref(false);
const selectedLanguage = ref(languages[0]);
const isRunning = ref(false);
const isSubmitting = ref(false);
const monacoEl = ref(null);

// 실행 결과를 저장할 refs
const output = ref('');
const compileError = ref('');
const runtimeError = ref('');
const systemError = ref('');

const result = ref('');
const score = ref(0);
const isExist = ref(false);

onMounted(() => {
  const ydocument = new Y.Doc();
  const provider = new WebsocketProvider('ws://localhost:1234', 'monaco', ydocument);
  const type = ydocument.getText('monaco');

  const editor = monaco.editor.create(monacoEl.value, {
    value: code.value,
    language: selectedLanguage.value.id.toLowerCase(),
    ...MONACO_EDITOR_OPTIONS
  });

  editorInstance.value = editor;

  const binding = new MonacoBinding(
      type,
      editor.getModel(),
      new Set([editor]),
      provider.awareness
  );

  editor.onDidChangeModelContent(() => {
    code.value = editor.getValue();
  });
});

const toggleDropdown = () => {
  showDropdown.value = !showDropdown.value;
};

const selectLanguage = (language) => {
  selectedLanguage.value = language;
  showDropdown.value = false;

  if (editorInstance.value) {
    monaco.editor.setModelLanguage(
        editorInstance.value.getModel(),
        language.id.toLowerCase()
    );

    const defaultCode = language.id === 'python' ?
        '# Python code here...' :
        'public class Solution {\n' +
        '    public static void main(String[] args) {\n' +
        '        \n' +
        '    }\n' +
        '}';

    editorInstance.value.setValue(defaultCode);
  }
};

const clearOutputs = () => {
  output.value = '';
  compileError.value = '';
  runtimeError.value = '';
  systemError.value = '';
};

const runCode = async () => {
  isRunning.value = true;
  clearOutputs();

  try {
    if (editorInstance.value) {
      code.value = editorInstance.value.getValue();
    }

    const response = await axios.post('http://localhost:8080/api/v1/compile/run',
        {
          code: code.value,
          language: selectedLanguage.value.id.toLowerCase()},
        {
          headers: {
            Authorization: `Bearer ${authStore.accessToken}`,
            'Authorization-refresh': `Bearer ${authStore.refreshToken}`
          }
        });

    isExist.value = false;

    // 각각의 응답 처리
    output.value = response.data.output || '';
    compileError.value = response.data.compileError || '';
    runtimeError.value = response.data.runtimeError || '';
    systemError.value = response.data.systemError || '';

  } catch (error) {
    isExist.value = false;
    console.error('코드 실행 중 에러가 발생했습니다. ', error);
    systemError.value = '서버 연결 중 오류가 발생했습니다.';
  } finally {
    isRunning.value = false;
  }
};

const submitCode = async () => {

  isSubmitting.value = true;
  clearOutputs();

  try {
    if (editorInstance.value) {
      code.value = editorInstance.value.getValue();
    }

    const response = await axios.post(`http://localhost:8080/api/v1/compile/codingroom/${props.codingRoomId}/submission`,
        {
          code: code.value,
          language: selectedLanguage.value.id.toLowerCase(),
          problemId: props.problemId},
        {
          headers: {
            Authorization: `Bearer ${authStore.accessToken}`,
            'Authorization-refresh': `Bearer ${authStore.refreshToken}`
          }
        });

    // 각각의 응답 처리
    result.value = response.data.result;
    score.value = response.data.score;

    isExist.value = true;

    console.log(result.value);
    console.log(score.value);

  } catch (error) {
    console.error('코드 실행 중 에러가 발생했습니다. ', error);
    systemError.value = '서버 연결 중 오류가 발생했습니다.';
  } finally {
    isSubmitting.value = false;
  }
};
</script>

<template>
  <div class="code-container">
    <div class="setting">
      <div class="language-selector" @click="toggleDropdown">
        <div class="selected-language">
          {{ selectedLanguage.name }}
          <span class="dropdown-icon">▼</span>
        </div>
        <div v-if="showDropdown" class="dropdown-menu">
          <div
              v-for="lang in languages"
              :key="lang.id"
              class="dropdown-item"
              @click="selectLanguage(lang)"
          >
            {{ lang.name }}
          </div>
        </div>
      </div>
      <button
          class="run-button"
          @click="runCode"
          :disabled="isRunning"
      >
        <span class="run-text">{{ isRunning ? '실행중...' : '실행' }}</span>
        <span v-if="!isRunning" class="run-icon" :class="{ 'spinning': isRunning }">▶</span>
        <span v-else class="loader"></span>
      </button>
    </div>
    <div ref="monacoEl" class="editor-container"></div>
    <!-- 실행 결과 출력 영역 -->
    <div class="output-container">
      <div class="output-header">
        <span>실행 결과</span>
        <button
            v-if="output || compileError || runtimeError || systemError"
            class="clear-button"
            @click="clearOutputs"
        >
          Clear
        </button>
        <button
            class="run-button"
            @click="submitCode"
            :disabled="isSubmitting"
        >
          <span class="run-text">{{ isSubmitting ? '실행중...' : '제출' }}</span>
          <span v-if="!isSubmitting" class="run-icon" :class="{ 'spinning': isSubmitting }">▶</span>
          <span v-else class="loader"></span>
        </button>
      </div>
      <div class="output-content">
        <!-- 정상 출력 -->
        <div v-if="output" class="output-section">
          <div class="section-header">실행 결과:</div>
          <pre class="section-content">{{ output }}</pre>
        </div>

        <div v-if="isExist" class="output-section">
          <div class="section-header">실행 결과:</div>
          <pre class="section-content">결과: {{ result }}</pre>
          <pre class="section-content">점수: {{ score }}</pre>
        </div>

        <!-- 컴파일 에러 -->
        <div v-if="compileError" class="output-section error">
          <div class="section-header">컴파일 에러:</div>
          <pre class="section-content">{{ compileError }}</pre>
        </div>

        <!-- 런타임 에러 -->
        <div v-if="runtimeError" class="output-section error">
          <div class="section-header">런타임 에러:</div>
          <pre class="section-content">{{ runtimeError }}</pre>
        </div>

        <!-- 시스템 에러 -->
        <div v-if="systemError" class="output-section error">
          <div class="section-header">시스템 에러:</div>
          <pre class="section-content">{{ systemError }}</pre>
        </div>

        <!-- 결과가 없을 때 -->
        <div v-if="!output && !compileError && !runtimeError && !systemError" class="empty-message">
          코드를 실행하면 여기에 결과가 표시됩니다.
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.setting {
  background-color: var(--editor-color);
  color: var(--background-color);
  height: 40px;
  padding: 0 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px var(--background-color) solid;
}

.editor-container {
  height: 400px;
}

.output-container {
  background-color: var(--editor-color);
  color: var(--background-color);
  border-top: 1px solid var(--background-color);
  height: 300px;
  overflow-y: auto;
}

.output-header {
  padding: 8px 16px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 500;
  position: sticky;
  top: 0;
  background-color: var(--editor-color);
  z-index: 1;
}

.output-content {
  padding: 16px;
}

.output-section {
  margin-bottom: 16px;
}

.output-section:last-child {
  margin-bottom: 0;
}

.section-header {
  font-weight: 500;
  margin-bottom: 8px;
  color: #a0a0a0;
}

.section-content {
  margin: 0;
  padding: 8px;
  background-color: rgba(0, 0, 0, 0.2);
  border-radius: 4px;
  font-family: monospace;
  white-space: pre-wrap;
  word-wrap: break-word;
}

.error .section-header {
  color: #ff6b6b;
}

.error .section-content {
  color: #ff6b6b;
  background-color: rgba(255, 107, 107, 0.1);
}

.empty-message {
  color: #a0a0a0;
  text-align: center;
  padding: 20px;
}

.clear-button {
  background: transparent;
  border: 1px solid var(--background-color);
  color: var(--background-color);
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 0.8em;
  cursor: pointer;
  transition: all 0.2s;
}

.clear-button:hover {
  background-color: rgba(255, 255, 255, 0.1);
}

.language-selector {
  position: relative;
  cursor: pointer;
  user-select: none;
}

.selected-language {
  display: flex;
  align-items: center;
  gap: 8px;
}

.dropdown-icon {
  font-size: 0.8em;
  opacity: 0.7;
}

.dropdown-menu {
  position: absolute;
  top: 100%;
  left: 0;
  background-color: var(--editor-color);
  border: 1px solid var(--background-color);
  border-radius: 4px;
  margin-top: 4px;
  min-width: 80px;
  z-index: 1000;
}

.dropdown-item {
  padding: 8px 16px;
  cursor: pointer;
  transition: background-color 0.2s;
}

.dropdown-item:hover {
  background-color: rgba(255, 255, 255, 0.1);
}

.run-button {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 12px;
  border: none;
  border-radius: 4px;
  background-color: transparent;
  color: var(--background-color);
  cursor: pointer;
  font-size: 0.9em;
  transition: background-color 0.2s;
}

.run-button:hover {
  background-color: rgba(255, 255, 255, 0.1);
}

.run-button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.run-text {
  font-weight: 500;
}

.run-icon {
  font-size: 0.8em;
  margin-bottom: 2px;
}

.loader {
  width: 16px;
  height: 16px;
  border: 2px solid var(--background-color);
  border-bottom-color: transparent;
  border-radius: 50%;
  display: inline-block;
  animation: rotation 1s linear infinite;
}

@keyframes rotation {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}
</style>