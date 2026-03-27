<template>
  <div class="permission-panel">
    <div class="perm-table">
      <div class="perm-table-header">
        <div class="col-module">功能</div>
        <div class="col-menu">操作对象</div>
        <div class="col-actions">权限</div>
        <div class="col-select-all">
           <!-- header select all placeholder -->
        </div>
      </div>

      <div class="perm-table-body">
        <div
          v-for="group in groups"
          :key="group.moduleCode"
          class="perm-group-row"
        >
          <div class="col-module group-name">
            {{ group.moduleName }}
          </div>
          <div class="group-sub-menus">
            <div
              v-for="sub in group.subMenus"
              :key="sub.code"
              class="sub-row"
            >
              <div class="col-menu sub-name">
                {{ sub.name }}
              </div>
              <div class="col-actions">
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
              <div class="col-select-all">
                <a-checkbox
                  v-if="sub.actions && sub.actions.length"
                  :checked="isSubAllChecked(sub)"
                  :indeterminate="isSubIndeterminate(sub)"
                  @change="toggleSub(sub, $event.target.checked)"
                />
              </div>
            </div>
            <div v-if="!group.subMenus || !group.subMenus.length" class="sub-row">
               <div class="col-menu"></div>
               <div class="col-actions no-actions">暂无权限点</div>
               <div class="col-select-all"></div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';

interface ActionItem {
  id: string;
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
  checkedKeys: string[];
}>();

const emit = defineEmits<{
  (event: 'update:checkedKeys', value: string[]): void;
}>();

const checkedMap = computed(() =>
  Object.fromEntries((props.checkedKeys || []).map((id) => [id, true]))
);

/** 获取某个 sub 下所有可勾选的 id（权限点 + 子菜单自身若存在） */
const subAllIds = (sub: SubMenuView): string[] => {
  const ids: string[] = [];
  if (sub.menuPermission?.id != null) ids.push(sub.menuPermission.id);
  (sub.actions || []).forEach((a) => ids.push(a.id));
  return ids;
};

/** 获取某个 group 下所有可勾选的 id */
const groupAllIds = (group: GroupView): string[] => {
  const ids: string[] = [];
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

const toggleOne = (id: string, checked: boolean) => {
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
  width: 100%;
}

.perm-table {
  border: 1px solid #f0f2f5;
  border-radius: 4px;
  overflow: hidden;
}

.perm-table-header {
  display: flex;
  background-color: #fafafa;
  border-bottom: 1px solid #f0f2f5;
  font-weight: 500;
  color: #243042;
}

.perm-table-header > div {
  padding: 12px 16px;
}

.col-module {
  width: 140px;
  flex-shrink: 0;
  border-right: 1px solid #f0f2f5;
}

.col-menu {
  width: 140px;
  flex-shrink: 0;
  border-right: 1px solid #f0f2f5;
}

.col-actions {
  flex: 1;
  min-width: 0;
  border-right: 1px solid #f0f2f5;
}

.col-select-all {
  width: 60px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.perm-group-row {
  display: flex;
  border-bottom: 1px solid #f0f2f5;
}

.perm-group-row:last-child {
  border-bottom: none;
}

.group-name {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  color: #3a4a5c;
  font-weight: 500;
}

.group-sub-menus {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
}

.sub-row {
  display: flex;
  border-bottom: 1px solid #f0f2f5;
}

.sub-row:last-child {
  border-bottom: none;
}

.sub-name {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  color: #4a5568;
}

.sub-row .col-actions {
  padding: 12px 16px;
}

.action-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 8px 16px;
  align-items: center;
}

.action-grid :deep(.ant-checkbox-wrapper) {
  font-size: 13px;
  color: #4a5568;
  margin-inline-start: 0 !important;
}

.no-actions {
  color: #bbb;
  font-size: 13px;
  padding-top: 2px;
}
</style>
