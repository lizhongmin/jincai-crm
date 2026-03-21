<template>
  <a-drawer
    :open="open"
    :title="actionType === 'approve' ? '审批通过' : '审批驳回'"
    placement="right"
    width="460"
    @update:open="(value) => emit('update:open', value)"
    @close="emit('update:open', false)"
  >
    <template #extra>
      <a-space>
        <a-button @click="emit('update:open', false)">取消</a-button>
        <a-button type="primary" :loading="saving" @click="emit('confirm')">确认</a-button>
      </a-space>
    </template>
    <a-form layout="vertical">
      <a-form-item label="审批意见">
        <a-textarea
          :value="comment"
          :rows="3"
          :placeholder="actionType === 'approve' ? '请输入通过意见' : '请输入驳回原因'"
          @update:value="(value) => emit('update:comment', value || '')"
        />
      </a-form-item>
    </a-form>
  </a-drawer>
</template>

<script setup lang="ts">
const props = defineProps<{
  open: boolean;
  saving: boolean;
  actionType: 'approve' | 'reject';
  comment: string;
}>();

const emit = defineEmits<{
  (e: 'update:open', value: boolean): void;
  (e: 'update:comment', value: string): void;
  (e: 'confirm'): void;
}>();
</script>
