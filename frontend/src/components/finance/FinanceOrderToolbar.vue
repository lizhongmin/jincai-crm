<template>
  <a-card class="toolbar-card" :bordered="false">
    <div class="toolbar-row">
      <a-select
        v-model:value="localActiveOrderId"
        style="width: 340px"
        placeholder="选择订单"
        @change="handleOrderChange"
      >
        <a-select-option
          v-for="item in orders"
          :key="item.id"
          :value="item.id"
        >
          {{ item.orderNo }} ({{ orderStatusLabel(item.status) }})
        </a-select-option>
      </a-select>

      <a-button
        type="primary"
        :disabled="!localActiveOrderId || !props.canReceivablePermission"
        @click="emit('add-receivable')"
      >
        新增应收
      </a-button>

      <a-button
        :disabled="!localActiveOrderId || !props.canPayablePermission"
        @click="emit('add-payable')"
      >
        新增应付
      </a-button>

      <a-button
        danger
        :disabled="!localActiveOrderId || !props.canRefundPermission"
        @click="emit('add-refund')"
      >
        新增退款
      </a-button>
    </div>

    <a-alert
      v-if="activeOrder"
      style="margin-top: 10px"
      type="info"
      show-icon
      :message="`当前订单：${activeOrder.orderNo} / ${orderStatusLabel(activeOrder.status)}`"
      :description="`客户ID：${activeOrder.customerId}，订单金额：${activeOrder.totalAmount} ${activeOrder.currency}`"
    />
  </a-card>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue';

const props = withDefaults(defineProps<{
  orders: any[];
  activeOrderId: string | null;
  activeOrder: any;
  orderStatusLabel: (value?: string) => string;
  canReceivablePermission?: boolean;
  canPayablePermission?: boolean;
  canRefundPermission?: boolean;
}>(), {
  canReceivablePermission: true,
  canPayablePermission: true,
  canRefundPermission: true
});

const emit = defineEmits<{
  (e: 'add-receivable'): void;
  (e: 'add-payable'): void;
  (e: 'add-refund'): void;
  (e: 'order-change', orderId: string | null): void;
}>();

const localActiveOrderId = ref(props.activeOrderId);

watch(
  () => props.activeOrderId,
  (newVal) => {
    localActiveOrderId.value = newVal;
  }
);

const handleOrderChange = (orderId: string | null) => {
  emit('order-change', orderId);
};
</script>

<style scoped>
.toolbar-card {
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
  border-radius: 4px;
}

.toolbar-row {
  display: flex;
  gap: 12px;
  align-items: center;
  flex-wrap: wrap;
}

.toolbar-row > * {
  flex-shrink: 0;
}

@media (max-width: 768px) {
  .toolbar-row {
    flex-direction: column;
    align-items: stretch;
  }

  .toolbar-row > * {
    width: 100%;
  }
}
</style>