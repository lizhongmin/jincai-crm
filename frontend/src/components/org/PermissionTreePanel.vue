<template>
  <div class="permission-panel">
    <a-card v-for="group in groups" :key="group.moduleCode" size="small" class="module-card">
      <template #title>{{ group.moduleName }}</template>
      <div v-if="menus(group.permissions || []).length" class="menu-row">
        <span class="label">菜单权限</span>
        <a-space wrap>
          <a-checkbox
            v-for="menu in menus(group.permissions || [])"
            :key="menu.id"
            :checked="checkedMap[menu.id]"
            @change="toggle(menu.id, $event.target.checked)"
          >
            {{ menu.name }}
          </a-checkbox>
        </a-space>
      </div>
      <a-divider v-if="menus(group.permissions || []).length" style="margin: 8px 0" />
      <div class="button-row">
        <span class="label">按钮权限</span>
        <div class="button-grid">
          <a-checkbox
            v-for="btn in buttons(group.permissions || [])"
            :key="btn.id"
            :checked="checkedMap[btn.id]"
            @change="toggle(btn.id, $event.target.checked)"
          >
            {{ btn.name }}
          </a-checkbox>
        </div>
      </div>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';

const props = defineProps<{
  groups: any[];
  checkedKeys: number[];
}>();

const emit = defineEmits<{
  (event: 'update:checkedKeys', value: number[]): void;
}>();

const checkedMap = computed(() => Object.fromEntries((props.checkedKeys || []).map((id) => [id, true])));

const menus = (permissions: any[]) => permissions.filter((item) => String(item.type || '').toUpperCase() === 'MENU');
const buttons = (permissions: any[]) => permissions.filter((item) => String(item.type || '').toUpperCase() !== 'MENU');

const toggle = (id: number, checked: boolean) => {
  const next = new Set(props.checkedKeys || []);
  if (checked) {
    next.add(id);
  } else {
    next.delete(id);
  }
  emit('update:checkedKeys', Array.from(next));
};

</script>

<style scoped>
.permission-panel {
  display: grid;
  gap: 12px;
}

.module-card {
  border-radius: 8px;
}

.menu-row,
.button-row {
  display: grid;
  gap: 8px;
}

.label {
  color: #666;
  font-size: 12px;
}

.button-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
  gap: 8px 12px;
  align-items: center;
}
</style>
