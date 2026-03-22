<template>
  <a-drawer
    :open="open"
    :title="title"
    placement="right"
    width="520"
    @update:open="(value) => emit('update:open', value)"
    @close="emit('update:open', false)"
  >
    <template #extra>
      <a-space>
        <a-button @click="emit('update:open', false)">取消</a-button>
        <a-button type="primary" :loading="saving" @click="emit('confirm')">保存</a-button>
      </a-space>
    </template>
    <a-form layout="vertical">
      <template v-if="mode === 'receivable' || mode === 'payable'">
        <a-form-item label="项目" required>
          <a-input v-model:value="model.itemName" />
        </a-form-item>
        <a-form-item label="金额" required>
          <a-input-number v-model:value="model.amount" :min="0" style="width: 100%" />
        </a-form-item>
      </template>

      <template v-else-if="mode === 'refund'">
        <a-form-item label="退款金额" required>
          <a-input-number v-model:value="model.amount" :min="0" style="width: 100%" />
        </a-form-item>
        <a-form-item label="退款原因">
          <a-input v-model:value="model.reason" />
        </a-form-item>
      </template>

      <template v-else>
        <a-form-item label="金额" required>
          <a-input-number v-model:value="model.amount" :min="0" style="width: 100%" />
        </a-form-item>
        <a-form-item label="备注">
          <a-input v-model:value="model.remark" />
        </a-form-item>
      </template>
    </a-form>
  </a-drawer>
</template>

<script setup lang="ts">
defineProps<{
  open: boolean;
  title: string;
  mode: 'receivable' | 'payable' | 'refund' | 'receipt' | 'payment';
  saving: boolean;
  model: any;
}>();

const emit = defineEmits<{
  (e: 'update:open', value: boolean): void;
  (e: 'confirm'): void;
}>();
</script>
