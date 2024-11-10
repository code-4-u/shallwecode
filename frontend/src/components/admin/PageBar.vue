<script setup>
import { computed } from 'vue';

const props = defineProps({
  currentPage: {
    type: Number,
    required: true
  },
  totalPages: {
    type: Number,
    required: true
  },
  totalItems: {
    type: Number,
    required: true
  }
});

const emit = defineEmits(['page-changed']);

// 페이지 변경 함수
const changePage = (page) => {
  if (page >= 1 && page <= props.totalPages) {
    emit('page-changed', page);
  }
};

// 표시할 페이지 목록 계산
const visiblePages = computed(() => {
  const pages = [];
  const range = 5;
  const start = Math.max(1, props.currentPage - range);
  const end = Math.min(props.totalPages, props.currentPage + range);

  for (let i = start; i <= end; i++) {
    pages.push(i);
  }

  return pages;
});
</script>

<template>
  <div v-if="totalPages > 1" class="pagination">
    <button
        :disabled="props.currentPage === 1"
        @click="changePage(props.currentPage - 1)">
      ◀
    </button>
    <span v-for="page in visiblePages" :key="page">
      <button
          :class="{ active: page === props.currentPage }"
          @click="changePage(page)">
        {{ page }}
      </button>
    </span>
    <button
        :disabled="props.currentPage === props.totalPages"
        @click="changePage(props.currentPage + 1)">
      ▶
    </button>
  </div>
</template>

<style scoped>
.pagination {
  display: flex;
  justify-content: center;
  margin-top: 20px;
  gap: 10px;
}

.pagination button {
  background: none;
  border: none;
  cursor: pointer;
  font-size: 1rem;
  padding: 5px 10px;
}

.pagination button:disabled {
  color: gray;
  cursor: default;
}

.pagination button.active {
  color: white;
  background-color: #1a1b3a;
  border-radius: 4px;
}

.pagination button:hover:not(.active):not(:disabled) {
  background: #f5f5f5;
}
</style>
