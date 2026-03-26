<template>
  <div class="permission-panel">
    <a-card
      v-for="group in groups"
      :key="group.moduleCode"
      size="small"
      class="module-card"
    >
      <template #title>
        <div class="module-title">
          <a-checkbox
            :checked="isGroupAllChecked(group)"
            :indeterminate="isGroupIndeterminate(group)"
            @change="toggleGroup(group, $event.target.checked)"
          />
          <span>{{ group.moduleName }}</span>
        </div>
      </template>

      <div class="sub-menu-list">
        <div
          v-for="sub in group.subMenus"
          :key="sub.code"
          class="sub-menu-row"
        >
          <!-- 2级：子菜单标题 + 全选 -->
          <div class="sub-menu-header">
            <a-checkbox
              :checked="isSubAllChecked(sub)"
              :indeterminate="isSubIndeterminate(sub)"
              @change="toggleSub(sub, $event.target.checked)"
            >
              <span class="sub-menu-name">{{ sub.name }}</span>
            </a-checkbox>
          </div>

          <!-- 3级：权限点 -->
          <div v-if="sub.actions && sub.actions.length" class="action-grid">
            <a-checkbox
              v-for="action in sub.actions"
              :key="action.id"
              :checked="!!checkedMap[action.id]"
              @change="toggleOne(action.id, $event.target.checked)"
            >
              {{ action.name }}
            </a-checkbox>
          </div>
          <div v-else class="no-actions">—</div>
        </div>

        <div v-if="!group.subMenus || !group.subMenus.length" class="no-actions">暂无权限点</div>
      </div>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';

interface ActionItem {
  id: number;
  name: string;
  type: string;
}

interface SubMenuView {
  code: string;
  name: string;
  menuPermission?: ActionItem | null;
  actions: ActionItem[];
}

interface GroupView {
  moduleCode: string;
  moduleName: string;
  menuPermission?: ActionItem | null;
  subMenus: SubMenuView[];
}

const props = defineProps<{
  groups: GroupView[];
  checkedKeys: number[];
}>();

const emit = defineEmits<{
  (event: 'update:checkedKeys', value: number[]): void;
}>();

const checkedMap = computed(() =>
  Object.fromEntries((props.checkedKeys || []).map((id) => [id, true]))
);

/** 获取某个 sub 下所有可勾选的 id（权限点 + 子菜单自身若存在） */
const subAllIds = (sub: SubMenuView): number[] => {
  const ids: number[] = [];
  if (sub.menuPermission?.id != null) ids.push(sub.menuPermission.id);
  (sub.actions || []).forEach((a) => ids.push(a.id));
  return ids;
};

/** 获取某个 group 下所有可勾选的 id */
const groupAllIds = (group: GroupView): number[] => {
  const ids: number[] = [];
  if (group.menuPermission?.id != null) ids.push(group.menuPermission.id);
  (group.subMenus || []).forEach((sub) => subAllIds(sub).forEach((id) => ids.push(id)));
  return ids;
};

const isSubAllChecked = (sub: SubMenuView) => {
  const ids = subAllIds(sub);
  return ids.length > 0 && ids.every((id) => checkedMap.value[id]);
};

const isSubIndeterminate = (sub: SubMenuView) => {
  const ids = subAllIds(sub);
  const checkedCount = ids.filter((id) => checkedMap.value[id]).length;
  return checkedCount > 0 && checkedCount < ids.length;
};

const isGroupAllChecked = (group: GroupView) => {
  const ids = groupAllIds(group);
  return ids.length > 0 && ids.every((id) => checkedMap.value[id]);
};

const isGroupIndeterminate = (group: GroupView) => {
  const ids = groupAllIds(group);
  const checkedCount = ids.filter((id) => checkedMap.value[id]).length;
  return checkedCount > 0 && checkedCount < ids.length;
};

const toggleOne = (id: number, checked: boolean) => {
  const next = new Set(props.checkedKeys || []);
  if (checked) next.add(id);
  else next.delete(id);
  emit('update:checkedKeys', Array.from(next));
};

const toggleSub = (sub: SubMenuView, checked: boolean) => {
  const next = new Set(props.checkedKeys || []);
  subAllIds(sub).forEach((id) => (checked ? next.add(id) : next.delete(id)));
  emit('update:checkedKeys', Array.from(next));
};

const toggleGroup = (group: GroupView, checked: boolean) => {
  const next = new Set(props.checkedKeys || []);
  groupAllIds(group).forEach((id) => (checked ? next.add(id) : next.delete(id)));
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

.module-card :deep(.ant-card-head) {
  min-height: 36px;
  padding: 0 12px;
  background: #f5f7fa;
}

.module-card :deep(.ant-card-head-title) {
  padding: 8px 0;
}

.module-card :deep(.ant-card-body) {
  padding: 10px 12px;
}

.module-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  color: #243042;
}

.sub-menu-list {
  display: grid;
  gap: 0;
}

.sub-menu-row {
  display: grid;
  grid-template-columns: 140px minmax(0, 1fr);
  align-items: start;
  gap: 8px;
  padding: 7px 0;
  border-bottom: 1px solid #f0f2f5;
}

.sub-menu-row:last-child {
  border-bottom: none;
  padding-bottom: 0;
}

.sub-menu-row:first-child {
  padding-top: 0;
}

.sub-menu-header {
  display: flex;
  align-items: center;
  padding-top: 1px;
}

.sub-menu-name {
  font-size: 13px;
  color: #3a4a5c;
  font-weight: 500;
}

.action-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 4px 16px;
  align-items: center;
}

.action-grid :deep(.ant-checkbox-wrapper) {
  font-size: 13px;
  color: #4a5568;
  margin-inline-start: 0 !important;
}

.no-actions {
  color: #bbb;
  font-size: 12px;
  padding-top: 3px;
}
</style>
