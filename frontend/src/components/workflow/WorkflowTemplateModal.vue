<template>
  <a-drawer
    :open="open"
    :title="model.id ? '编辑模板' : '新增模板'"
    placement="right"
    width="860"
    @update:open="(value) => emit('update:open', value)"
    @close="emit('update:open', false)"
  >
    <template #extra>
      <a-space>
        <a-button @click="emit('update:open', false)">取消</a-button>
        <a-button type="primary" :loading="saving" @click="emit('save')">保存</a-button>
      </a-space>
    </template>
    <a-form layout="vertical">
      <div class="grid-2">
        <a-form-item label="模板名称" required>
          <a-input v-model:value="model.name" />
        </a-form-item>
        <a-form-item label="订单类型" required>
          <a-select v-model:value="model.orderType">
            <a-select-option value="GROUP">GROUP</a-select-option>
            <a-select-option value="CUSTOM">CUSTOM</a-select-option>
          </a-select>
        </a-form-item>
      </div>

      <div class="grid-2">
        <a-form-item label="产品分类" required>
          <a-select v-model:value="model.productCategory">
            <a-select-option value="国内游">国内游</a-select-option>
            <a-select-option value="出境游">出境游</a-select-option>
            <a-select-option value="自由行">自由行</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="启用状态">
          <a-switch v-model:checked="model.active" checked-children="启用" un-checked-children="停用" />
        </a-form-item>
      </div>

      <div class="grid-2">
        <a-form-item label="限定线路（可选）">
          <a-select v-model:value="model.routeId" allow-clear placeholder="不限制线路">
            <a-select-option v-for="item in routes" :key="item.id" :value="item.id">{{ item.name }}</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="限定团期（可选）">
          <a-select v-model:value="model.departureId" allow-clear placeholder="不限制团期">
            <a-select-option v-for="item in filteredDepartures" :key="item.id" :value="item.id">
              {{ item.code }} ({{ item.startDate }} ~ {{ item.endDate }})
            </a-select-option>
          </a-select>
        </a-form-item>
      </div>

      <div class="grid-2">
        <a-form-item label="最小金额">
          <a-input-number v-model:value="model.minAmount" :min="0" style="width: 100%" />
        </a-form-item>
        <a-form-item label="最大金额">
          <a-input-number v-model:value="model.maxAmount" :min="0" style="width: 100%" />
        </a-form-item>
      </div>

      <a-divider orientation="left">审批节点</a-divider>
      <a-space direction="vertical" style="width: 100%">
        <a-card v-for="(node, idx) in model.nodes" :key="idx" size="small">
          <div class="node-row">
            <a-input-number v-model:value="node.stepOrder" :min="1" placeholder="顺序" style="width: 100px" />
            <a-input v-model:value="node.nodeName" placeholder="节点名称" />
            <a-select v-model:value="node.approverRoleCode" placeholder="审批角色" style="width: 200px">
              <a-select-option v-for="role in roles" :key="role.code" :value="role.code">
                {{ role.name }}
              </a-select-option>
            </a-select>
            <a-button danger @click="emit('remove-node', idx)">删除</a-button>
          </div>
        </a-card>
        <a-button block @click="emit('add-node')">新增节点</a-button>
      </a-space>
    </a-form>
  </a-drawer>
</template>

<script setup lang="ts">
import { computed } from 'vue';

const props = defineProps<{
  open: boolean;
  saving: boolean;
  model: any;
  routes: any[];
  departures: any[];
  roles: Array<{ code: string; name: string }>;
}>();

const filteredDepartures = computed(() => {
  if (!props.model.routeId) {
    return props.departures || [];
  }
  return (props.departures || []).filter((item: any) => item.routeId === props.model.routeId);
});

const emit = defineEmits<{
  (e: 'update:open', value: boolean): void;
  (e: 'save'): void;
  (e: 'add-node'): void;
  (e: 'remove-node', idx: number): void;
}>();
</script>

<style scoped>
.node-row {
  display: grid;
  grid-template-columns: 100px 1fr 200px 70px;
  gap: 10px;
  align-items: center;
}

@media (max-width: 900px) {
  .node-row {
    grid-template-columns: 1fr;
  }
}
</style>
