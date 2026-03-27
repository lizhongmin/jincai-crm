<script setup lang="ts">
import { computed, ref, useAttrs, useSlots } from 'vue';
import { SettingOutlined } from '@ant-design/icons-vue';

const props = defineProps<{
  columns: any[];
}>();

const attrs = useAttrs();
const slots = useSlots();

const visibleKeys = ref<string[]>(props.columns.map((c) => String(c.dataIndex || c.key)));
const activeColumns = computed(() => props.columns.filter((c) => visibleKeys.value.includes(String(c.dataIndex || c.key))));

const mergedPagination = computed(() => {
  if (attrs.pagination === false) return false;
  const base = {
    showSizeChanger: true,
    pageSizeOptions: ['10', '20', '50'],
    showTotal: (total: number) => `共 ${total} 条`
  };
  if (typeof attrs.pagination === 'object' && attrs.pagination !== null) {
    return { ...base, ...attrs.pagination };
  }
  return base;
});

const onColumnChange = (key: string, checked: boolean) => {
  if (checked) {
    visibleKeys.value.push(key);
  } else {
    visibleKeys.value = visibleKeys.value.filter((k) => k !== key);
  }
};
</script>

<template>
  <a-table v-bind="attrs" :columns="activeColumns" :pagination="mergedPagination">
    <template v-for="(_, slotName) in slots" #[slotName]="slotProps">
      <slot v-if="slotName !== 'headerCell'" :name="slotName" v-bind="slotProps || {}"></slot>
    </template>

    <template #headerCell="slotProps">
      <template v-if="(slotProps.column.dataIndex === 'actions' || slotProps.column.key === 'actions')">
        <div style="display: flex; justify-content: space-between; align-items: center; width: 100%">
          <span>{{ slotProps.column.title || '操作' }}</span>
          <a-popover placement="bottomRight" trigger="click" overlay-class-name="column-setting-popover">
            <template #content>
              <div class="column-setting-list">
                <div v-for="col in props.columns" :key="String(col.dataIndex || col.key)" class="column-setting-item">
                  <a-checkbox
                    :checked="visibleKeys.includes(String(col.dataIndex || col.key))"
                    :disabled="(col.dataIndex || col.key) === 'actions'"
                    @change="(e) => onColumnChange(String(col.dataIndex || col.key), e.target.checked)"
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
      <template v-else-if="slots.headerCell">
        <slot name="headerCell" v-bind="slotProps"></slot>
      </template>
    </template>
  </a-table>
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
