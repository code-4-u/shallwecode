<script setup>
import { reactive, watch } from 'vue';

const search = reactive({
  keyword: '',
  option: ''
});

const emit = defineEmits(['problemSearch']);

// 검색어 변경 시 부모 컴포넌트에 이벤트 전달
const emitSearch = () => {
  emit('problemSearch', {
    option: search.option || null,
    keyword: search.keyword || null
  });
};

// 검색어가 변경될 때마다 emitSearch 호출
watch(() => search.keyword, emitSearch);
watch(() => search.option, emitSearch);

</script>

<template>
  <div class="filter-bar">
    <select v-model="search.option">
      <option value="">난이도</option>
      <option value="1">Lv. 1</option>
      <option value="2">Lv. 2</option>
      <option value="3">Lv. 3</option>
      <option value="4">Lv. 4</option>
      <option value="5">Lv. 5</option>
    </select>
    <input type="text" v-model="search.keyword" placeholder="검색할 문제 입력">
    <button @click="emitSearch">검색</button>
  </div>
</template>

<style scoped>
.filter-bar {
  display: flex;
  justify-content: space-between;
  margin-bottom: 20px;
}

.filter-bar select, .filter-bar input {
  padding: 5px;
  font-size: 1rem;
  border: 1px solid #ddd;
  border-radius: 5px;
}

.filter-bar input {
  width: 200px;
}
</style>
