<template>
  <div class="finance-page">
    <div class="biz-summary">
      <div class="item">
        <span class="label">应收合计</span>
        <strong class="value">{{ receivableTotal }}</strong>
      </div>
      <div class="item">
        <span class="label">实收合计</span>
        <strong class="value">{{ receivedTotal }}</strong>
      </div>
      <div class="item">
        <span class="label">应付合计</span>
        <strong class="value">{{ payableTotal }}</strong>
      </div>
      <div class="item">
        <span class="label">退款合计</span>
        <strong class="value">{{ refundTotal }}</strong>
      </div>
    </div>

    <a-card class="section-card" :bordered="false">
      <div class="toolbar-row">
        <a-select v-model:value="activeOrderId" style="width: 340px" placeholder="选择订单" @change="loadForOrder">
          <a-select-option v-for="item in orderOptions" :key="item.id" :value="item.id">
            {{ item.orderNo }} ({{ orderStatusLabel(item.status) }})
          </a-select-option>
        </a-select>
        <a-button type="primary" :disabled="!activeOrderId" @click="openReceivable()">新增应收</a-button>
        <a-button :disabled="!activeOrderId" @click="openPayable()">新增应付</a-button>
        <a-button danger :disabled="!activeOrderId" @click="openRefund()">新增退款</a-button>
      </div>

      <a-alert
        v-if="activeOrder"
        style="margin-top: 10px"
        type="info"
        show-icon
        :message="`当前订单：${activeOrder.orderNo} / ${orderStatusLabel(activeOrder.status)}`"
        :description="`客户ID：${activeOrder.customerId}，订单金额：${activeOrder.totalAmount} ${activeOrder.currency}`"
      />

      <div class="grid-3" style="margin-top: 10px">
        <finance-ledger-table
          title="应收列表"
          mode="receivable"
          :items="receivables"
          @primary="openReceipt"
          @secondary="loadReceipts"
          @edit="openReceivable"
          @remove="removeReceivable"
        />
        <finance-ledger-table
          title="应付列表"
          mode="payable"
          :items="payables"
          @primary="openPayment"
          @secondary="loadPayments"
          @edit="openPayable"
          @remove="removePayable"
        />
        <finance-ledger-table
          title="退款列表"
          mode="refund"
          :items="refunds"
          :can-review-permission="canReviewPermission"
          @approve="(record) => reviewRefund(record, true)"
          @reject="(record) => reviewRefund(record, false)"
          @edit="openRefund"
          @remove="removeRefund"
        />
      </div>
    </a-card>

    <a-card class="section-card" :bordered="false" title="审核记录">
      <a-tabs v-model:activeKey="recordTab">
        <a-tab-pane key="receipt" tab="收款记录">
          <finance-record-table
            mode="receipt"
            :items="receiptRecords"
            :can-review-permission="canReviewPermission"
            @review="(record, approved) => reviewRecord(record, 'RECEIPT', approved)"
          />
        </a-tab-pane>
        <a-tab-pane key="payment" tab="付款记录">
          <finance-record-table
            mode="payment"
            :items="paymentRecords"
            :can-review-permission="canReviewPermission"
            @review="(record, approved) => reviewRecord(record, 'PAYMENT', approved)"
          />
        </a-tab-pane>
      </a-tabs>
    </a-card>

    <finance-form-modal
      v-model:open="itemModalOpen"
      :title="itemModalTitle"
      :mode="itemModalMode"
      :saving="saving"
      :model="itemModalModel"
      @confirm="submitItemModal"
    />

    <finance-form-modal
      v-model:open="flowModalOpen"
      :title="flowModalTitle"
      :mode="flowModalMode"
      :saving="saving"
      :model="flowModalModel"
      @confirm="submitFlowModal"
    />
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { financeApi } from '../api/crm';
import FinanceFormModal from '../components/finance/FinanceFormModal.vue';
import FinanceLedgerTable from '../components/finance/FinanceLedgerTable.vue';
import FinanceRecordTable from '../components/finance/FinanceRecordTable.vue';
import { ORDER_STATUS_LABEL_MAP, enumLabel } from '../constants/display';
import { useAuthStore } from '../stores/auth';
import { canFinanceReviewByRole } from '../utils/role';
import { notifyError, notifySuccess } from '../utils/notify';

const auth = useAuthStore();
const orders = ref<any[]>([]);
const activeOrderId = ref<string | null>(null);
const receivables = ref<any[]>([]);
const payables = ref<any[]>([]);
const refunds = ref<any[]>([]);
const receiptRecords = ref<any[]>([]);
const paymentRecords = ref<any[]>([]);
const recordTab = ref('receipt');
const saving = ref(false);

const itemModalOpen = ref(false);
const itemModalMode = ref<'receivable' | 'payable' | 'refund'>('receivable');
const flowModalOpen = ref(false);
const flowModalMode = ref<'receipt' | 'payment'>('receipt');
const activeReceivableId = ref<string | null>(null);
const activePayableId = ref<string | null>(null);

const receivableForm = reactive({ id: undefined as string | undefined, itemName: '团费', amount: 2000 });
const payableForm = reactive({ id: undefined as string | undefined, itemName: '地接成本', amount: 1200 });
const refundForm = reactive({ id: undefined as string | undefined, amount: 200, reason: '客户取消订单' });
const receiptForm = reactive({ amount: 1000, remark: '到账确认' });
const paymentForm = reactive({ amount: 600, remark: '已打款' });

const orderOptions = computed(() =>
  orders.value.filter((item) => ['APPROVED', 'IN_TRAVEL', 'TRAVEL_FINISHED', 'SETTLING', 'COMPLETED'].includes(item.status))
);
const activeOrder = computed(() => orders.value.find((item) => item.id === activeOrderId.value) || null);

const receivableTotal = computed(() => Number(receivables.value.reduce((sum, item) => sum + Number(item.amount || 0), 0).toFixed(2)));
const receivedTotal = computed(() => Number(receivables.value.reduce((sum, item) => sum + Number(item.received || 0), 0).toFixed(2)));
const payableTotal = computed(() => Number(payables.value.reduce((sum, item) => sum + Number(item.amount || 0), 0).toFixed(2)));
const refundTotal = computed(() => Number(refunds.value.reduce((sum, item) => sum + Number(item.amount || 0), 0).toFixed(2)));
const canReviewPermission = computed(() => canFinanceReviewByRole(auth.profile?.roles));
const orderStatusLabel = (value?: string) => enumLabel(ORDER_STATUS_LABEL_MAP, value);

const itemModalTitle = computed(() => {
  const labelMap = {
    receivable: receivableForm.id ? '编辑应收' : '新增应收',
    payable: payableForm.id ? '编辑应付' : '新增应付',
    refund: refundForm.id ? '编辑退款' : '新增退款'
  };
  return labelMap[itemModalMode.value];
});
const flowModalTitle = computed(() => (flowModalMode.value === 'receipt' ? '新增收款' : '新增付款'));
const itemModalModel = computed(() => {
  if (itemModalMode.value === 'receivable') return receivableForm;
  if (itemModalMode.value === 'payable') return payableForm;
  return refundForm;
});
const flowModalModel = computed(() => (flowModalMode.value === 'receipt' ? receiptForm : paymentForm));

const loadOrders = async () => {
  const { data } = await financeApi.orderOptions();
  orders.value = data.data || [];
  if (!activeOrderId.value && orderOptions.value.length) {
    activeOrderId.value = orderOptions.value[0].id;
    await loadForOrder(activeOrderId.value);
  }
};

const loadForOrder = async (orderId?: string | null) => {
  if (!orderId) {
    receivables.value = [];
    payables.value = [];
    refunds.value = [];
    return;
  }
  try {
    const [receivableRes, payableRes, refundRes] = await Promise.all([
      financeApi.receivables(orderId),
      financeApi.payables(orderId),
      financeApi.refunds(orderId)
    ]);
    receivables.value = receivableRes.data.data || [];
    payables.value = payableRes.data.data || [];
    refunds.value = refundRes.data.data || [];
  } catch (error) {
    notifyError(error);
  }
};

const resetReceivableForm = (record?: any) => {
  receivableForm.id = record?.id;
  receivableForm.itemName = record?.itemName || '团费';
  receivableForm.amount = Number(record?.amount || 2000);
};

const resetPayableForm = (record?: any) => {
  payableForm.id = record?.id;
  payableForm.itemName = record?.itemName || '地接成本';
  payableForm.amount = Number(record?.amount || 1200);
};

const resetRefundForm = (record?: any) => {
  refundForm.id = record?.id;
  refundForm.amount = Number(record?.amount || 200);
  refundForm.reason = record?.reason || '客户取消订单';
};

const openReceivable = (record?: any) => {
  itemModalMode.value = 'receivable';
  resetReceivableForm(record);
  itemModalOpen.value = true;
};

const openPayable = (record?: any) => {
  itemModalMode.value = 'payable';
  resetPayableForm(record);
  itemModalOpen.value = true;
};

const openRefund = (record?: any) => {
  itemModalMode.value = 'refund';
  resetRefundForm(record);
  itemModalOpen.value = true;
};

const submitItemModal = async () => {
  if (!activeOrderId.value) {
    return;
  }
  saving.value = true;
  try {
    if (itemModalMode.value === 'receivable') {
      if (receivableForm.id) {
        await financeApi.updateReceivable(receivableForm.id, {
          itemName: receivableForm.itemName,
          amount: receivableForm.amount
        });
        notifySuccess('应收更新成功');
      } else {
        await financeApi.createReceivable(activeOrderId.value, {
          itemName: receivableForm.itemName,
          amount: receivableForm.amount
        });
        notifySuccess('应收创建成功');
      }
    } else if (itemModalMode.value === 'payable') {
      if (payableForm.id) {
        await financeApi.updatePayable(payableForm.id, {
          orderId: activeOrderId.value,
          itemName: payableForm.itemName,
          amount: payableForm.amount
        });
        notifySuccess('应付更新成功');
      } else {
        await financeApi.createPayable({
          orderId: activeOrderId.value,
          itemName: payableForm.itemName,
          amount: payableForm.amount
        });
        notifySuccess('应付创建成功');
      }
    } else {
      if (refundForm.id) {
        await financeApi.updateRefund(refundForm.id, {
          orderId: activeOrderId.value,
          amount: refundForm.amount,
          reason: refundForm.reason
        });
        notifySuccess('退款更新成功');
      } else {
        await financeApi.createRefund({
          orderId: activeOrderId.value,
          amount: refundForm.amount,
          reason: refundForm.reason
        });
        notifySuccess('退款创建成功');
      }
    }
    itemModalOpen.value = false;
    await Promise.all([loadOrders(), loadForOrder(activeOrderId.value)]);
  } catch (error) {
    notifyError(error);
  } finally {
    saving.value = false;
  }
};

const removeReceivable = async (record: any) => {
  try {
    await financeApi.deleteReceivable(record.id);
    notifySuccess('应收删除成功');
    if (activeOrderId.value) {
      await loadForOrder(activeOrderId.value);
    }
  } catch (error) {
    notifyError(error);
  }
};

const removePayable = async (record: any) => {
  try {
    await financeApi.deletePayable(record.id);
    notifySuccess('应付删除成功');
    if (activeOrderId.value) {
      await loadForOrder(activeOrderId.value);
    }
  } catch (error) {
    notifyError(error);
  }
};

const removeRefund = async (record: any) => {
  try {
    await financeApi.deleteRefund(record.id);
    notifySuccess('退款删除成功');
    if (activeOrderId.value) {
      await loadForOrder(activeOrderId.value);
    }
  } catch (error) {
    notifyError(error);
  }
};

const openReceipt = (record: any) => {
  activeReceivableId.value = record.id;
  receiptForm.amount = Number(record.amount || 0) - Number(record.received || 0);
  receiptForm.remark = '到账确认';
  flowModalMode.value = 'receipt';
  flowModalOpen.value = true;
};

const openPayment = (record: any) => {
  activePayableId.value = record.id;
  paymentForm.amount = Number(record.amount || 0) - Number(record.paid || 0);
  paymentForm.remark = '已打款';
  flowModalMode.value = 'payment';
  flowModalOpen.value = true;
};

const submitFlowModal = async () => {
  saving.value = true;
  try {
    if (flowModalMode.value === 'receipt') {
      if (!activeReceivableId.value) {
        return;
      }
      const { data } = await financeApi.createReceipt({
        receivableId: activeReceivableId.value,
        amount: receiptForm.amount,
        remark: receiptForm.remark
      });
      notifySuccess('收款记录创建成功');
      flowModalOpen.value = false;
      await reviewRecord(data.data, 'RECEIPT', true);
      await loadReceipts({ id: activeReceivableId.value });
    } else {
      if (!activePayableId.value) {
        return;
      }
      const { data } = await financeApi.createPayment({
        payableId: activePayableId.value,
        amount: paymentForm.amount,
        remark: paymentForm.remark
      });
      notifySuccess('付款记录创建成功');
      flowModalOpen.value = false;
      await reviewRecord(data.data, 'PAYMENT', true);
      await loadPayments({ id: activePayableId.value });
    }
  } catch (error) {
    notifyError(error);
  } finally {
    saving.value = false;
  }
};

const loadReceipts = async (receivable: any) => {
  const { data } = await financeApi.receipts(receivable.id);
  receiptRecords.value = data.data || [];
  recordTab.value = 'receipt';
};

const loadPayments = async (payable: any) => {
  const { data } = await financeApi.payments(payable.id);
  paymentRecords.value = data.data || [];
  recordTab.value = 'payment';
};

const reviewRefund = async (refund: any, approved: boolean) => {
  if (!canReviewPermission.value) {
    return;
  }
  try {
    await financeApi.review(refund.id, {
      targetType: 'REFUND',
      approved,
      comment: approved ? '退款审核通过' : '退款审核驳回'
    });
    notifySuccess(`退款审核已${approved ? '通过' : '驳回'}`);
    if (activeOrderId.value) {
      await Promise.all([loadOrders(), loadForOrder(activeOrderId.value)]);
    }
  } catch (error) {
    notifyError(error);
  }
};

const reviewRecord = async (record: any, type: 'RECEIPT' | 'PAYMENT', approved: boolean) => {
  if (!canReviewPermission.value) {
    return;
  }
  try {
    await financeApi.review(record.id, {
      targetType: type,
      approved,
      comment: approved ? '财务审核通过' : '财务审核驳回'
    });
    notifySuccess(`${type === 'RECEIPT' ? '收款' : '付款'}审核已${approved ? '通过' : '驳回'}`);
    if (type === 'RECEIPT' && activeReceivableId.value) {
      await loadReceipts({ id: activeReceivableId.value });
    }
    if (type === 'PAYMENT' && activePayableId.value) {
      await loadPayments({ id: activePayableId.value });
    }
    if (activeOrderId.value) {
      await Promise.all([loadOrders(), loadForOrder(activeOrderId.value)]);
    }
  } catch (error) {
    notifyError(error);
  }
};

onMounted(async () => {
  try {
    await loadOrders();
  } catch (error) {
    notifyError(error);
  }
});
</script>

<style scoped>
.finance-page {
  display: grid;
  gap: 10px;
}

.grid-3 {
  display: grid;
  gap: 10px;
  grid-template-columns: repeat(3, minmax(0, 1fr));
}

@media (max-width: 1200px) {
  .grid-3 {
    grid-template-columns: 1fr;
  }
}
</style>
