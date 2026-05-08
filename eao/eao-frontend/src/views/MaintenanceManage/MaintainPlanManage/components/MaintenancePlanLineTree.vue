<script setup>
const props = defineProps({
  lines: {
    type: Array,
    default: () => []
  },
  activeLineId: {
    type: [String, Number],
    default: ''
  },
  loading: {
    type: Boolean,
    default: false
  },
  title: {
    type: String,
    default: '检修计划编制'
  }
})

const emit = defineEmits(['select'])
</script>

<template>
  <aside class="tree-card" v-loading="loading">
    <div class="tree-title">检修计划编制</div>
    <div class="tree-body">
      <div class="tree-tip">
        <el-radio checked>请按需自动化生产线</el-radio>
      </div>
      <div class="line-list">
        <div
          v-for="line in lines"
          :key="line.id"
          :class="['line-item', { active: line.id === activeLineId }]"
          @click="emit('select', line)"
        >
          <div class="line-code">{{ line.lineCode || '-' }}</div>
          <div class="line-name">{{ line.lineName || '未命名生产线' }}</div>
        </div>
      </div>
    </div>
  </aside>
</template>

<style scoped>
.tree-card {
  border: 1px solid #d9e2ef;
  background: #fff;
  min-height: calc(100vh - 92px);
}
.tree-title {
  padding: 10px 14px;
  border-bottom: 1px solid #e6edf6;
  color: #33507b;
  font-size: 13px;
  font-weight: 700;
}
.tree-body {
  padding: 12px;
}
.tree-tip {
  margin-bottom: 12px;
  color: #4b5d73;
  font-size: 13px;
}
.line-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.line-item {
  padding: 10px 12px;
  border: 1px solid #dfe7f3;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s ease;
}
.line-item:hover {
  border-color: #91c2ff;
  background: #f7fbff;
}
.line-item.active {
  border-color: #409eff;
  background: #ecf5ff;
}
.line-code {
  color: #1f2d3d;
  font-size: 14px;
  font-weight: 700;
}
.line-name {
  margin-top: 4px;
  color: #5c6b7a;
  font-size: 13px;
}
</style>
