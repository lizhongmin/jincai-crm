<script setup lang="ts">
defineProps<{
  open: boolean;
  saving: boolean;
  title: string;
  placeholder?: string;
  comment: string;
}>();

const emit = defineEmits<{
  (e: 'update:open', value: boolean): void;
  (e: 'update:comment', value: string): void;
  (e: 'confirm'): void;
}>();
</script>

<template>
  <a-drawer
    :open="open"
    :title="title"
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
      <a-form-item label="备注">
        <a-textarea
          :value="comment"
          :rows="3"
          :placeholder="placeholder || '请输入备注信息'"
          @update:value="(value) => emit('update:comment', value || '')"
        />
      </a-form-item>
    </a-form>
  </a-drawer>
</template>
