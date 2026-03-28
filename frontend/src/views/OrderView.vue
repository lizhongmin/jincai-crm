<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue';
import { useRouter } from 'vue-router';
import { orderApi } from '../api/crm';
import OrderActionModal from '../components/order/OrderActionModal.vue';
import OrderFormModal from '../components/order/OrderFormModal.vue';
import OrderTable from '../components/order/OrderTable.vue';
import { useAuthStore } from '../stores/auth';
import { canOrderReviewByRole } from '../utils/role';
import { hasButtonPermission } from '../utils/permission';
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

const router = useRouter();
const auth = useAuthStore();

const orders = ref<any[]>([]);
const customers = ref<any[]>([]);
const routes = ref<any[]>([]);
const departures = ref<any[]>([]);
const customerTravelers = ref<any[]>([]);
const departurePrices = ref<any[]>([]);
const quote = ref<any | null>(null);

const keyword = ref('');
const statusFilter = ref('ALL');
const activeListTab = ref('all');
const orderPage = ref(1);
const orderPageSize = ref(10);
const orderTotal = ref(0);
const saving = ref(false);

const createModal = ref(false);
const actionModal = ref(false);
const actionOrder = ref<any>(null);
const actionType = ref<OrderAction>('APPROVE');
const actionTitle = ref('订单动作');
const actionPlaceholder = ref('请输入备注信息');
const actionComment = ref('');

const createForm = reactive({
  id: undefined as string | undefined,
  orderNo: '',
  customerId: undefined as string | undefined,
  routeId: undefined as string | undefined,
  departureId: undefined as string | undefined,
  orderType: 'GROUP',
  productCategory: '国内游',
  travelerSelections: [] as Array<{ travelerId: string; departurePriceId?: string }>,
  extraSelections: [] as Array<{ departurePriceId?: string; quantity: number }>
});

const statusLabelMap: Record<string, string> = {
  DRAFT: '草稿',
  PENDING_APPROVAL: '待审批',
  APPROVED: '已审批',
  REJECTED: '已驳回',
  IN_TRAVEL: '履约中',
  TRAVEL_FINISHED: '已回团',
  SETTLING: '结算中',
  COMPLETED: '已完结',
  CANCELED: '已取消'
};

const statusOrder = ['PENDING_APPROVAL', 'DRAFT', 'APPROVED', 'REJECTED', 'IN_TRAVEL', 'TRAVEL_FINISHED', 'SETTLING', 'COMPLETED', 'CANCELED'];

const statusOptions = computed(() =>
  statusOrder.map((value) => ({ value, label: statusLabelMap[value] || value }))
);

const canReviewPermission = computed(() => hasButtonPermission('BTN_ORDER_APPROVE'));

const customerMap = computed(() => Object.fromEntries(customers.value.map((item) => [item.id, item.name])));
const routeMap = computed(() => Object.fromEntries(routes.value.map((item) => [item.id, item.name])));
const departureMap = computed(() =>
  Object.fromEntries(
    departures.value.map((item) => [
      item.id,
      {
        name: item.name || item.code || `团期#${item.id}`,
        dateRange: item.startDate && item.endDate ? `${item.startDate} ~ ${item.endDate}` : '-'
      }
    ])
  )
);

const decorateOrder = (item: any) => ({
  ...item,
  customerName: customerMap.value[item.customerId] || `客户#${item.customerId}`,
  routeName: routeMap.value[item.routeId] || `线路#${item.routeId}`,
  departureName: departureMap.value[item.departureId]?.name || `团期#${item.departureId}`,
  departureDateRange: departureMap.value[item.departureId]?.dateRange || '-',
  departureLabel: departureMap.value[item.departureId]?.name || `团期#${item.departureId}`
});

const decoratedOrders = computed(() => orders.value.map(decorateOrder));
const allOrders = computed(() => decoratedOrders.value);

const groupDefs = [
  { key: 'pendingApproval', label: '待审批', match: (item: any) => item.status === 'PENDING_APPROVAL' },
  { key: 'pendingContract', label: '待签约', match: (item: any) => ['APPROVED', 'SETTLING'].includes(item.status) && item.contractStatus === 'PENDING_SIGN' },
  {
    key: 'pendingCollection',
    label: '待收款',
    match: (item: any) => ['APPROVED', 'SETTLING'].includes(item.status) && ['UNPAID', 'PARTIAL', 'REFUNDING'].includes(item.paymentStatus)
  },
  {
    key: 'pendingDeparture',
    label: '待出团',
    match: (item: any) =>
      ['APPROVED', 'SETTLING'].includes(item.status)
      && item.inventoryStatus === 'LOCKED'
      && item.paymentStatus === 'PAID'
      && item.contractStatus !== 'PENDING_SIGN'
  },
  { key: 'inTravel', label: '履约中', match: (item: any) => item.status === 'IN_TRAVEL' },
  { key: 'settling', label: '结算中', match: (item: any) => item.status === 'TRAVEL_FINISHED' || item.status === 'SETTLING' },
  { key: 'completed', label: '已完结', match: (item: any) => item.status === 'COMPLETED' },
  { key: 'canceled', label: '已取消', match: (item: any) => item.status === 'CANCELED' }
];

const statusGroups = computed(() =>
  groupDefs
    .map((group) => ({
      key: group.key,
      label: group.label,
      items: decoratedOrders.value.filter(group.match)
    }))
    .filter((group) => group.items.length > 0)
);

const pendingOrders = computed(() => decoratedOrders.value.filter((item) => item.status === 'PENDING_APPROVAL'));
const pendingCount = computed(() => pendingOrders.value.length);
const approvedCount = computed(() =>
  decoratedOrders.value.filter((item) => ['APPROVED', 'IN_TRAVEL', 'TRAVEL_FINISHED', 'SETTLING'].includes(item.status)).length
);
const completedCount = computed(() => decoratedOrders.value.filter((item) => item.status === 'COMPLETED').length);

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

const openAction = (record: any, action: OrderAction) => {
  if ((action === 'APPROVE' || action === 'REJECT') && !canReviewPermission.value) {
    return;
  }
  actionOrder.value = record;
  actionType.value = action;
  actionTitle.value = actionConfig[action].title;
  actionPlaceholder.value = actionConfig[action].placeholder;
  actionComment.value = actionConfig[action].defaultComment;
  actionModal.value = true;
};

const listStatusParam = computed(() => {
  if (activeListTab.value === 'pending') {
    return 'PENDING_APPROVAL';
  }
  if (activeListTab.value === 'all' && statusFilter.value !== 'ALL') {
    return statusFilter.value;
  }
  return undefined;
});

const loadOrders = async () => {
  const { data } = await orderApi.page({
    page: orderPage.value,
    size: orderPageSize.value,
    keyword: keyword.value.trim() || undefined,
    status: listStatusParam.value
  });
  orders.value = data.data?.items || [];
  orderTotal.value = Number(data.data?.total || 0);
};

const loadContextOptions = async () => {
  const { data } = await orderApi.contextOptions();
  customers.value = data.data?.customers || [];
  routes.value = data.data?.routes || [];
  // departures 不再从 contextOptions 全量加载，改为按需加载
  departures.value = [];
};

const loadBase = async () => {
  await Promise.all([
    loadOrders(),
    loadContextOptions()
  ]);
};

const loadCustomerTravelers = async (customerId?: string) => {
  if (!customerId) {
    customerTravelers.value = [];
    return;
  }
  const { data } = await orderApi.customerTravelers(customerId);
  customerTravelers.value = data.data || [];
};

const loadDeparturePrices = async (departureId?: string) => {
  if (!departureId) {
    departurePrices.value = [];
    return;
  }
  const { data } = await orderApi.departurePrices(departureId);
  departurePrices.value = data.data || [];
};

const viewOrder = (record: any) => {
  router.push(`/orders/${record.id}`);
};

const resetForm = () => {
  createForm.id = undefined;
  createForm.orderNo = '';
  createForm.customerId = customers.value[0]?.id;
  createForm.routeId = routes.value[0]?.id;
  createForm.departureId = departures.value.find((item) => item.routeId === createForm.routeId)?.id;
  createForm.orderType = 'GROUP';
  createForm.productCategory = routes.value.find((item) => item.id === createForm.routeId)?.category || '国内游';
  createForm.travelerSelections = [];
  createForm.extraSelections = [];
  quote.value = null;
};

const buildPayload = () => {
  const priceSelections = [
    ...createForm.travelerSelections
      .filter((item) => item.travelerId)
      .map((item) => ({ travelerId: item.travelerId, departurePriceId: item.departurePriceId, quantity: 1 })),
    ...createForm.extraSelections
      .filter((item) => item.departurePriceId)
      .map((item) => ({ departurePriceId: item.departurePriceId, quantity: item.quantity || 1 }))
  ];
  return {
    orderNo: createForm.orderNo || undefined,
    customerId: createForm.customerId,
    routeId: createForm.routeId,
    departureId: createForm.departureId,
    orderType: createForm.orderType,
    productCategory: routes.value.find((item) => item.id === createForm.routeId)?.category || createForm.productCategory,
    travelerCount: quote.value?.travelerCount,
    totalAmount: quote.value?.totalAmount,
    currency: quote.value?.currency || 'CNY',
    priceSelections
  };
};

const requestQuote = async () => {
  if (!createForm.customerId || !createForm.routeId || !createForm.departureId) {
    quote.value = null;
    return;
  }
  if (!createForm.travelerSelections.length) {
    quote.value = null;
    return;
  }
  try {
    const { data } = await orderApi.quote(buildPayload());
    quote.value = data.data;
  } catch (error) {
    quote.value = null;
    notifyError(error);
  }
};

const openCreate = async (record?: any) => {
  resetForm();
  if (record) {
    const { data } = await orderApi.detail(record.id);
    const detail = data.data;
    createForm.id = detail.order.id;
    createForm.orderNo = detail.order.orderNo || '';
    createForm.customerId = detail.order.customerId;
    createForm.routeId = detail.order.routeId;
    createForm.departureId = detail.order.departureId;
    createForm.orderType = detail.order.orderType || 'GROUP';
    createForm.productCategory = detail.order.productCategory || '国内游';
    createForm.travelerSelections = (detail.travelers || []).map((traveler: any) => {
      const priceItem = (detail.priceItems || []).find((item: any) => item.travelerId === traveler.travelerId);
      return {
        travelerId: traveler.travelerId,
        departurePriceId: priceItem?.departurePriceId
      };
    });
    createForm.extraSelections = (detail.priceItems || [])
      .filter((item: any) => !item.travelerId)
      .map((item: any) => ({ departurePriceId: item.departurePriceId, quantity: item.quantity }));
  }
  
  // 如果有 routeId，先加载该线路的团期
  if (createForm.routeId) {
    const { data } = await orderApi.routeDepartures(createForm.routeId);
    departures.value = data.data || [];
  }

  await Promise.all([
    loadCustomerTravelers(createForm.customerId),
    loadDeparturePrices(createForm.departureId)
  ]);
  await requestQuote();
  createModal.value = true;
};

const handleRouteChange = async (routeId?: string) => {
  if (!routeId) {
    departures.value = [];
    return;
  }
  const { data } = await orderApi.routeDepartures(routeId);
  departures.value = data.data || [];
  
  if (!departures.value.some((item) => item.id === createForm.departureId)) {
    createForm.departureId = departures.value[0]?.id;
  }
  await handleDepartureChange(createForm.departureId);
};

const handleCustomerChange = async (customerId?: string) => {
  await loadCustomerTravelers(customerId);
};

const handleDepartureChange = async (departureId?: string) => {
  await loadDeparturePrices(departureId);
  await requestQuote();
};

const saveOrder = async () => {
  if (!createForm.customerId || !createForm.routeId || !createForm.departureId) return;
  if (!quote.value) {
    notifyError(new Error('请先选择出行人并生成报价'));
    return;
  }
  saving.value = true;
  try {
    const payload = buildPayload();
    if (createForm.id) {
      await orderApi.update(createForm.id, payload);
      notifySuccess('订单更新成功');
    } else {
      await orderApi.create(payload);
      notifySuccess('订单创建成功');
    }
    createModal.value = false;
    await loadBase();
  } catch (error) {
    notifyError(error);
  } finally {
    saving.value = false;
  }
};

const removeOrder = async (order: any) => {
  try {
    await orderApi.remove(order.id);
    notifySuccess('订单删除成功');
    await loadBase();
  } catch (error) {
    notifyError(error);
  }
};

const confirmAction = async () => {
  if (!actionOrder.value) return;
  if ((actionType.value === 'APPROVE' || actionType.value === 'REJECT') && !canReviewPermission.value) return;
  saving.value = true;
  try {
    await orderApi.action(actionOrder.value.id, {
      action: actionType.value,
      comment: actionComment.value || actionConfig[actionType.value].defaultComment
    });
    notifySuccess(actionConfig[actionType.value].successText);
    actionModal.value = false;
    await loadBase();
  } catch (error) {
    notifyError(error);
  } finally {
    saving.value = false;
  }
};

const beforeImportOrder = async (file: File) => {
  try {
    await orderApi.importOrders(file);
    notifySuccess('订单导入完成');
    await loadBase();
  } catch (error) {
    notifyError(error);
  }
  return false;
};

const onOrderPageChange = (page: number, pageSize: number) => {
  orderPage.value = page;
  orderPageSize.value = pageSize;
  void loadOrders();
};

watch([keyword, statusFilter, activeListTab], async () => {
  orderPage.value = 1;
  await loadOrders();
});

onMounted(async () => {
  try {
    await loadBase();
  } catch (error) {
    notifyError(error);
  }
});
</script>

<template>
  <div class="order-page">
    <div class="biz-summary">
      <div class="item">
        <span class="label">订单总数</span>
        <strong class="value">{{ orderTotal }}</strong>
      </div>
      <div class="item">
        <span class="label">待审批</span>
        <strong class="value">{{ pendingCount }}</strong>
      </div>
      <div class="item">
        <span class="label">待履约</span>
        <strong class="value">{{ approvedCount }}</strong>
      </div>
      <div class="item">
        <span class="label">已完结</span>
        <strong class="value">{{ completedCount }}</strong>
      </div>
    </div>

    <a-card class="section-card" :bordered="false">
      <div class="toolbar-row">
        <a-input-search v-model:value="keyword" placeholder="按订单号、客户、线路筛选" style="width: 340px" />
        <a-button type="primary" :disabled="!hasButtonPermission('BTN_ORDER_CREATE')" @click="openCreate()">新建订单</a-button>
        <a-upload :show-upload-list="false" :before-upload="beforeImportOrder">
          <a-button :disabled="!hasButtonPermission('BTN_ORDER_CREATE')">导入订单</a-button>
        </a-upload>
      </div>

      <a-tabs v-model:activeKey="activeListTab" class="list-tabs">
        <a-tab-pane key="all" :tab="`订单列表 (${orderTotal})`">
          <div class="toolbar-row" style="margin-top: 10px">
            <a-select v-model:value="statusFilter" style="width: 180px">
              <a-select-option value="ALL">全部状态</a-select-option>
              <a-select-option v-for="item in statusOptions" :key="item.value" :value="item.value">{{ item.label }}</a-select-option>
            </a-select>
          </div>
          <order-table
            style="margin-top: 10px"
            :items="allOrders"
            :can-review-permission="canReviewPermission"
            :can-edit-permission="hasButtonPermission('BTN_ORDER_EDIT')"
            :can-delete-permission="hasButtonPermission('BTN_ORDER_DELETE')"
            @view="viewOrder"
            @edit="openCreate"
            @action="openAction"
            @remove="removeOrder"
          />
          <a-pagination
            style="margin-top: 12px; text-align: right"
            :current="orderPage"
            :page-size="orderPageSize"
            :total="orderTotal"
            :show-size-changer="true"
            :page-size-options="['10', '20', '50']"
            :show-total="(total:number) => `共 ${total} 条`"
            @change="onOrderPageChange"
          />
        </a-tab-pane>

        <a-tab-pane key="pending" :tab="`待审核订单 (${orderTotal})`">
          <order-table
            style="margin-top: 10px"
            :items="pendingOrders"
            :can-review-permission="canReviewPermission"
            :can-edit-permission="hasButtonPermission('BTN_ORDER_EDIT')"
            :can-delete-permission="hasButtonPermission('BTN_ORDER_DELETE')"
            @view="viewOrder"
            @edit="openCreate"
            @action="openAction"
            @remove="removeOrder"
          />
          <a-pagination
            style="margin-top: 12px; text-align: right"
            :current="orderPage"
            :page-size="orderPageSize"
            :total="orderTotal"
            :show-size-changer="true"
            :page-size-options="['10', '20', '50']"
            :show-total="(total:number) => `共 ${total} 条`"
            @change="onOrderPageChange"
          />
        </a-tab-pane>

        <a-tab-pane key="group" tab="按业务分组">
          <a-empty v-if="!statusGroups.length" description="暂无订单数据" />
          <a-collapse v-else class="status-collapse" :bordered="false">
            <a-collapse-panel v-for="group in statusGroups" :key="group.key">
              <template #header>
                <div class="group-head">
                  <span>{{ group.label }}</span>
                  <a-tag>{{ group.items.length }}</a-tag>
                </div>
              </template>
              <order-table
                :items="group.items"
                :can-review-permission="canReviewPermission"
                :can-edit-permission="hasButtonPermission('BTN_ORDER_EDIT')"
                :can-delete-permission="hasButtonPermission('BTN_ORDER_DELETE')"
                @view="viewOrder"
                @edit="openCreate"
                @action="openAction"
                @remove="removeOrder"
              />
            </a-collapse-panel>
          </a-collapse>
        </a-tab-pane>
      </a-tabs>
    </a-card>

    <order-form-modal
      v-model:open="createModal"
      :saving="saving"
      :model="createForm"
      :customers="customers"
      :routes="routes"
      :departures="departures"
      :travelers="customerTravelers"
      :prices="departurePrices"
      :quote="quote"
      @save="saveOrder"
      @route-change="handleRouteChange"
      @customer-change="handleCustomerChange"
      @departure-change="handleDepartureChange"
      @request-quote="requestQuote"
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
.order-page {
  display: grid;
  gap: 10px;
  overflow-x: auto;
}

.list-tabs {
  margin-top: 8px;
}

.group-head {
  display: flex;
  align-items: center;
  gap: 8px;
}

.status-collapse {
  margin-top: 10px;
}
</style>
