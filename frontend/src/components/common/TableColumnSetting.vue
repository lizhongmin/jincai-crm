<script setup lang="ts">
import { SettingOutlined } from '@ant-design/icons-vue';

const props = defineProps<{
  columns: any[];
  visibleKeys: string[];
}>();

const emit = defineEmits<{
  (e: 'update:visibleKeys', keys: string[]): void;
}>();

const onColumnChange = (key: string, checked: boolean) => {
  if (checked) {
    emit('update:visibleKeys', [...props.visibleKeys, key]);
  } else {
    emit('update:visibleKeys', props.visibleKeys.filter((k) => k !== key));
  }
};
</script>

<template>
  <div style="display: flex; justify-content: space-between; align-items: center; width: 100%">
    <span>操作</span>
    <a-popover placement="bottomRight" trigger="click" overlay-class-name="column-setting-popover">
      <template #content>
        <div class="column-setting-list">
          <div v-for="col in props.columns" :key="String(col.dataIndex)" class="column-setting-item">
            <a-checkbox
              :checked="props.visibleKeys.includes(String(col.dataIndex))"
              :disabled="col.dataIndex === 'actions'"
              @change="(e) => onColumnChange(String(col.dataIndex), e.target.checked)"
            >
              {{ col.title }}
            </a-checkbox>
          </div>
        </div>
      </template>
      <SettingOutlined style="cursor: pointer; font-size: 14px; color: #888" @click.stop />
    </a-popover>
  </div>
</template>

<style scoped>
.column-setting-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  max-height: 300px;
  overflow-y: auto;
  padding: 4px;
}
.column-setting-item {
  display: flex;
  align-items: center;
}
</style>
