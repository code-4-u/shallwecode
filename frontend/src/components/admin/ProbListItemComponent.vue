<script setup>
const props = defineProps({
  problem: {
    type: Object,
    required: true
  }
});


const emit = defineEmits(['edit-problem', 'delete-problem']);

const handleEdit = () => {
  const confirmed = confirm('이 문제를 수정하시겠습니까?');
  if (confirmed){
    emit('edit-problem', props.problem.problemId);
  }
};

const handleDelete = () => {
  const confirmed = confirm('이 문제를 삭제하시겠습니까?');
  if (confirmed) {
    emit('delete-problem', props.problem.problemId);
  }
};

</script>

<template>
  <tr class="problem-row">
    <td>{{ props.problem.problemId }}</td>
    <td>{{ props.problem.title }}</td>
    <td :class="`level level-${props.problem.problemLevel}`">Lv. {{ props.problem.problemLevel }}</td>
    <td><button @click="handleEdit">수정</button></td>
    <td><button @click="handleDelete">삭제</button></td>
  </tr>
</template>

<style scoped>
.problem-row {
  text-align: center;
  border-bottom: 1px solid #ddd; /* 행 간 경계선 추가 */
}

.problem-row td {
  border: none; /* 열 사이 경계선 제거 */
  padding: 12px;
}

/* 테두리 스타일 설정 */
.problem-row td:first-child {
  border-left: 1px solid #ddd;
}
.problem-row td:last-child {
  border-right: 1px solid #ddd;
}
</style>