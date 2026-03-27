<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { auditApi, fileApi, orderApi } from '../api/crm';
import OrderActionModal from '../components/order/OrderActionModal.vue';
import OrderDetailPanel from '../components/order/OrderDetailPanel.vue';
import { INVENTORY_STATUS_LABEL_MAP, ORDER_STATUS_LABEL_MAP, PAYMENT_STATUS_LABEL_MAP, enumLabel } from '../constants/display';
import { useAuthStore } from '../stores/auth';
import { canOrderReviewByRole } from '../utils/role';
import { notifyError, notifySuccess } from '../utils/notify';

type OrderAction =
  | 'SUBMIT'
  | 'RESUBMIT'
  | 'WITHDRAW'
  | 'APPROVE'
  | 'REJECT'
  | 'SIGN_CONTRACT'
  | 'LOCK_INVENTORY'
  | 'MARK_IN_TRAVEL'
  | 'MARK_TRAVEL_FINISHED'
  | 'CANCEL';

const route = useRoute();
const router = useRouter();
const auth = useAuthStore();

const detailState = ref<any | null>(null);
const logs = ref<any[]>([]);
const audits = ref<any[]>([]);
const attachments = ref<any[]>([]);
const customers = ref<any[]>([]);
const routes = ref<any[]>([]);
const departures = ref<any[]>([]);

const saving = ref(false);
const actionModal = ref(false);
const actionType = ref<OrderAction>('APPROVE');
const actionComment = ref('');
const actionTitle = ref('订单动作');
const actionPlaceholder = ref('请输入备注信息');

const canReviewPermission = computed(() => canOrderReviewByRole(auth.profile?.roles));
const orderId = computed(() => route.params.id as string);
const currentOrder = computed(() => detailState.value?.order);

const customerMap = computed(() => Object.fromEntries(customers.value.map((item) => [item.id, item.name])));
const routeMap = computed(() => Object.fromEntries(routes.value.map((item) => [item.id, item.name])));
const departureMap = computed(() =>
  Object.fromEntries(departures.value.map((item) => [item.id, `${item.code} (${item.startDate} ~ ${item.endDate})`]))
);

const decorateOrder = (item: any) => ({
  ...item,
  customerName: customerMap.value[item.customerId] || `客户#${item.customerId}`,
  routeName: routeMap.value[item.routeId] || `线路#${item.routeId}`,
  departureLabel: departureMap.value[item.departureId] || `团期#${item.departureId}`
});

const actionConfig: Record<OrderAction, { title: string; placeholder: string; defaultComment: string; successText: string }> = {
  SUBMIT: { title: '提交审批', placeholder: '请输入提交说明', defaultComment: '提交审批', successText: '订单已提交审批' },
  RESUBMIT: { title: '重新提交', placeholder: '请输入重提说明', defaultComment: '驳回后重提', successText: '订单已重新提交' },
  WITHDRAW: { title: '撤回审批', placeholder: '请输入撤回原因', defaultComment: '发起人撤回', successText: '审批流程已撤回' },
  APPROVE: { title: '审批通过', placeholder: '请输入通过意见', defaultComment: '审批通过', successText: '审批已通过' },
  REJECT: { title: '审批驳回', placeholder: '请输入驳回原因', defaultComment: '审批驳回', successText: '审批已驳回' },
  SIGN_CONTRACT: { title: '确认签约', placeholder: '请输入签约说明', defaultComment: '合同已签署', successText: '签约状态已更新' },
  LOCK_INVENTORY: { title: '锁定库存', placeholder: '请输入锁位说明', defaultComment: '执行锁位', successText: '库存已锁定' },
  MARK_IN_TRAVEL: { title: '开始出团', placeholder: '请输入出团说明', defaultComment: '已出团', successText: '订单已进入履约中' },
  MARK_TRAVEL_FINISHED: { title: '回团确认', placeholder: '请输入回团说明', defaultComment: '已回团', successText: '订单已标记回团' },
  CANCEL: { title: '取消订单', placeholder: '请输入取消原因', defaultComment: '订单取消', successText: '订单已取消' }
};

const availableActions = computed(() => {
  const order = currentOrder.value;
  if (!order) return [] as OrderAction[];
  const actions: OrderAction[] = [];
  if (order.status === 'DRAFT') actions.push('SUBMIT');
  if (order.status === 'REJECTED') actions.push('RESUBMIT');
  if (order.status === 'PENDING_APPROVAL') actions.push('WITHDRAW');
  if (order.status === 'PENDING_APPROVAL' && canReviewPermission.value) {
    actions.push('APPROVE', 'REJECT');
  }
  if (['APPROVED', 'SETTLING'].includes(order.status) && order.contractStatus === 'PENDING_SIGN') {
    actions.push('SIGN_CONTRACT');
  }
  if (['APPROVED', 'SETTLING'].includes(order.status) && order.inventoryStatus !== 'LOCKED') {
    actions.push('LOCK_INVENTORY');
  }
  if (
    ['APPROVED', 'SETTLING'].includes(order.status)
    && order.inventoryStatus === 'LOCKED'
    && order.paymentStatus === 'PAID'
    && order.contractStatus !== 'PENDING_SIGN'
  ) {
    actions.push('MARK_IN_TRAVEL');
  }
  if (order.status === 'IN_TRAVEL') {
    actions.push('MARK_TRAVEL_FINISHED');
  }
  if (!['COMPLETED', 'CANCELED', 'IN_TRAVEL'].includes(order.status)) {
    actions.push('CANCEL');
  }
  return actions;
});

const actionButtonLabel = (action: OrderAction) => {
  const map: Record<OrderAction, string> = {
    SUBMIT: '提交审批',
    RESUBMIT: '重新提交',
    WITHDRAW: '撤回审批',
    APPROVE: '审批通过',
    REJECT: '审批驳回',
    SIGN_CONTRACT: '确认签约',
    LOCK_INVENTORY: '锁位',
    MARK_IN_TRAVEL: '开始出团',
    MARK_TRAVEL_FINISHED: '回团',
    CANCEL: '取消订单'
  };
  return map[action];
};

const orderStatusLabel = (value?: string) => enumLabel(ORDER_STATUS_LABEL_MAP, value);
const paymentStatusLabel = (value?: string) => enumLabel(PAYMENT_STATUS_LABEL_MAP, value);
const inventoryStatusLabel = (value?: string) => enumLabel(INVENTORY_STATUS_LABEL_MAP, value);

const openAction = (action: OrderAction) => {
  if ((action === 'APPROVE' || action === 'REJECT') && !canReviewPermission.value) return;
  actionType.value = action;
  actionTitle.value = actionConfig[action].title;
  actionPlaceholder.value = actionConfig[action].placeholder;
  actionComment.value = actionConfig[action].defaultComment;
  actionModal.value = true;
};

const loadDetail = async () => {
  if (!orderId.value) {
    notifyError(new Error('订单ID不合法'));
    router.replace('/orders');
    return;
  }
  try {
    const [
      detailRes,
      logRes,
      auditRes,
      fileRes,
      contextRes
    ] = await Promise.all([
      orderApi.detail(orderId.value),
      orderApi.logs(orderId.value),
      auditApi.list('ORDER', orderId.value),
      fileApi.list('ORDER', orderId.value),
      orderApi.contextOptions()
    ]);

    customers.value = contextRes.data.data?.customers || [];
    routes.value = contextRes.data.data?.routes || [];
    departures.value = contextRes.data.data?.departures || [];

    const detail = detailRes.data.data;
    detailState.value = {
      ...detail,
      order: decorateOrder(detail.order)
    };
    logs.value = logRes.data.data || [];
    audits.value = auditRes.data.data || [];
    attachments.value = fileRes.data.data || [];
  } catch (error) {
    notifyError(error);
  }
};

const goBack = () => router.push('/orders');

const confirmAction = async () => {
  if (!currentOrder.value?.id) return;
  if ((actionType.value === 'APPROVE' || actionType.value === 'REJECT') && !canReviewPermission.value) return;
  saving.value = true;
  try {
    await orderApi.action(currentOrder.value.id, {
      action: actionType.value,
      comment: actionComment.value || actionConfig[actionType.value].defaultComment
    });
    notifySuccess(actionConfig[actionType.value].successText);
    actionModal.value = false;
    await loadDetail();
  } catch (error) {
    notifyError(error);
  } finally {
    saving.value = false;
  }
};

const beforeUploadAttachment = async (file: File) => {
  if (!currentOrder.value) return false;
  try {
    await fileApi.upload('ORDER', currentOrder.value.id, file);
    notifySuccess('附件上传成功');
    await loadDetail();
  } catch (error) {
    notifyError(error);
  }
  return false;
};

const downloadAttachment = async (record: any) => {
  try {
    const response = await fetch(`/api/files/${record.id}/download`, {
      headers: {
        Authorization: `Bearer ${localStorage.getItem('crm_token') || ''}`
      }
    });
    if (!response.ok) throw new Error('下载失败');
    const blob = await response.blob();
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = record.fileName || `attachment-${record.id}`;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    URL.revokeObjectURL(url);
  } catch (error) {
    notifyError(error);
  }
};

watch(() => route.params.id, () => {
  void loadDetail();
});

onMounted(() => {
  void loadDetail();
});
</script>

<template>
  <div class="order-detail-page">
    <a-card class="section-card" :bordered="false">
      <div class="toolbar-row detail-toolbar">
        <a-button @click="goBack">返回订单管理</a-button>
        <a-space v-if="currentOrder" wrap>
          <a-button
            v-for="action in availableActions"
            :key="action"
            :type="action === 'APPROVE' ? 'primary' : 'default'"
            :danger="action === 'REJECT' || action === 'CANCEL'"
            @click="openAction(action)"
          >
            {{ actionButtonLabel(action) }}
          </a-button>
        </a-space>
      </div>
      <div v-if="currentOrder" class="brief-line">
        <span>订单号：{{ currentOrder.orderNo }}</span>
        <span>主状态：{{ orderStatusLabel(currentOrder.status) }}</span>
        <span>客户：{{ currentOrder.customerName }}</span>
        <span>收款：{{ paymentStatusLabel(currentOrder.paymentStatus) }}</span>
        <span>锁位：{{ inventoryStatusLabel(currentOrder.inventoryStatus) }}</span>
      </div>
    </a-card>

    <order-detail-panel
      :detail="detailState"
      :logs="logs"
      :audits="audits"
      :attachments="attachments"
      @upload="beforeUploadAttachment"
      @download="downloadAttachment"
    />

    <order-action-modal
      v-model:open="actionModal"
      :saving="saving"
      :title="actionTitle"
      :placeholder="actionPlaceholder"
      :comment="actionComment"
      @update:comment="(value) => (actionComment = value)"
      @confirm="confirmAction"
    />
  </div>
</template>

<style scoped>
.order-detail-page {
  display: grid;
  gap: 10px;
}

.detail-toolbar {
  justify-content: space-between;
}

.brief-line {
  margin-top: 10px;
  display: flex;
  gap: 20px;
  color: var(--muted);
  font-size: 13px;
  flex-wrap: wrap;
}

@media (max-width: 1100px) {
  .detail-toolbar,
  .brief-line {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
}
</style>
