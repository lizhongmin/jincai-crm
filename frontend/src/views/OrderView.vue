<template>
  <div class="order-page">
    <a-card class="section-card" :bordered="false" title="订单中心">
      <div class="toolbar-row">
        <a-input-search v-model:value="keyword" placeholder="按订单号、客户、线路筛选" style="width: 320px" />
        <a-button type="primary" @click="openCreate()">新建订单</a-button>
        <a-upload :show-upload-list="false" :before-upload="beforeImportOrder">
          <a-button>导入订单</a-button>
        </a-upload>
      </div>

      <order-table
        style="margin-top: 12px"
        :items="filteredOrders"
        @view="selectOrder"
        @edit="openCreate"
        @submit="submit"
        @approve="openApprove"
        @reject="openReject"
        @remove="removeOrder"
      />
    </a-card>

    <order-detail-panel
      :detail="currentOrderDetail"
      :logs="logs"
      :audits="audits"
      :attachments="attachments"
      @upload="beforeUploadAttachment"
      @download="downloadAttachment"
    />

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
      @customer-change="handleCustomerChange"
      @departure-change="handleDepartureChange"
      @request-quote="requestQuote"
    />

    <order-action-modal
      v-model:open="actionModal"
      :saving="saving"
      :action-type="actionType"
      :comment="actionComment"
      @update:comment="(value) => (actionComment = value)"
      @confirm="confirmAction"
    />
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { auditApi, customerApi, fileApi, orderApi, productApi } from '../api/crm';
import OrderActionModal from '../components/order/OrderActionModal.vue';
import OrderDetailPanel from '../components/order/OrderDetailPanel.vue';
import OrderFormModal from '../components/order/OrderFormModal.vue';
import OrderTable from '../components/order/OrderTable.vue';
import { notifyError, notifySuccess } from '../utils/notify';

const orders = ref<any[]>([]);
const logs = ref<any[]>([]);
const audits = ref<any[]>([]);
const attachments = ref<any[]>([]);
const currentOrderDetail = ref<any | null>(null);

const customers = ref<any[]>([]);
const routes = ref<any[]>([]);
const departures = ref<any[]>([]);
const customerTravelers = ref<any[]>([]);
const departurePrices = ref<any[]>([]);
const quote = ref<any | null>(null);

const keyword = ref('');
const currentOrderId = ref<number>();
const saving = ref(false);

const createModal = ref(false);
const actionModal = ref(false);
const actionType = ref<'approve' | 'reject'>('approve');
const actionOrder = ref<any>(null);
const actionComment = ref('');

const createForm = reactive({
  id: undefined as number | undefined,
  orderNo: '',
  customerId: undefined as number | undefined,
  routeId: undefined as number | undefined,
  departureId: undefined as number | undefined,
  orderType: 'GROUP',
  productCategory: '国内游',
  travelerSelections: [] as Array<{ travelerId: number; departurePriceId?: number }>,
  extraSelections: [] as Array<{ departurePriceId?: number; quantity: number }>
});

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

const decoratedOrders = computed(() => orders.value.map(decorateOrder));

const filteredOrders = computed(() => {
  const kw = keyword.value.trim().toLowerCase();
  if (!kw) {
    return decoratedOrders.value;
  }
  return decoratedOrders.value.filter((item) =>
    [item.orderNo, item.customerName, item.routeName, item.orderType]
      .filter(Boolean)
      .some((value) => String(value).toLowerCase().includes(kw))
  );
});

const loadBase = async () => {
  const [orderRes, customerRes, routeRes, departureRes] = await Promise.all([
    orderApi.list(),
    customerApi.customers(),
    productApi.routes(),
    productApi.departures()
  ]);
  orders.value = orderRes.data.data || [];
  customers.value = customerRes.data.data || [];
  routes.value = routeRes.data.data || [];
  departures.value = departureRes.data.data || [];
};

const loadCustomerTravelers = async (customerId?: number) => {
  if (!customerId) {
    customerTravelers.value = [];
    return;
  }
  const { data } = await customerApi.travelers(customerId);
  customerTravelers.value = data.data || [];
};

const loadDeparturePrices = async (departureId?: number) => {
  if (!departureId) {
    departurePrices.value = [];
    return;
  }
  const { data } = await productApi.prices(departureId);
  departurePrices.value = data.data || [];
};

const selectOrder = async (record: any) => {
  currentOrderId.value = record.id;
  await loadOrderDetail(record.id);
};

const loadOrderDetail = async (orderId: number) => {
  const [detailRes, logRes, auditRes, fileRes] = await Promise.all([
    orderApi.detail(orderId),
    orderApi.logs(orderId),
    auditApi.list('ORDER', orderId),
    fileApi.list('ORDER', orderId)
  ]);
  const detail = detailRes.data.data;
  currentOrderDetail.value = {
    ...detail,
    order: decorateOrder(detail.order)
  };
  logs.value = logRes.data.data || [];
  audits.value = auditRes.data.data || [];
  attachments.value = fileRes.data.data || [];
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
      .filter((item) => item.travelerId && item.departurePriceId)
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
  const hasTravelerSelections = createForm.travelerSelections.length > 0
    && createForm.travelerSelections.every((item) => item.departurePriceId);
  if (!hasTravelerSelections) {
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
  await Promise.all([
    loadCustomerTravelers(createForm.customerId),
    loadDeparturePrices(createForm.departureId)
  ]);
  await requestQuote();
  createModal.value = true;
};

const handleCustomerChange = async (customerId?: number) => {
  await loadCustomerTravelers(customerId);
};

const handleDepartureChange = async (departureId?: number) => {
  await loadDeparturePrices(departureId);
  await requestQuote();
};

const saveOrder = async () => {
  if (!createForm.customerId || !createForm.routeId || !createForm.departureId) {
    return;
  }
  if (!quote.value) {
    notifyError(new Error('请先完成出行人和价格项配置')); 
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
    if (currentOrderId.value) {
      await loadOrderDetail(currentOrderId.value);
    }
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
    if (currentOrderId.value === order.id) {
      currentOrderId.value = undefined;
      currentOrderDetail.value = null;
      logs.value = [];
      audits.value = [];
      attachments.value = [];
    }
    await loadBase();
  } catch (error) {
    notifyError(error);
  }
};

const submit = async (order: any) => {
  try {
    await orderApi.submit(order.id);
    notifySuccess('订单已提交审批');
    await loadBase();
    if (currentOrderId.value === order.id) {
      await loadOrderDetail(order.id);
    }
  } catch (error) {
    notifyError(error);
  }
};

const openApprove = (record: any) => {
  actionType.value = 'approve';
  actionOrder.value = record;
  actionComment.value = '审批通过';
  actionModal.value = true;
};

const openReject = (record: any) => {
  actionType.value = 'reject';
  actionOrder.value = record;
  actionComment.value = '资料待补充';
  actionModal.value = true;
};

const confirmAction = async () => {
  if (!actionOrder.value) {
    return;
  }
  saving.value = true;
  try {
    if (actionType.value === 'approve') {
      await orderApi.approve(actionOrder.value.id, actionComment.value || '审批通过');
      notifySuccess('审批已通过');
    } else {
      await orderApi.reject(actionOrder.value.id, actionComment.value || '审批驳回');
      notifySuccess('审批已驳回');
    }
    actionModal.value = false;
    await loadBase();
    if (currentOrderId.value === actionOrder.value.id) {
      await loadOrderDetail(actionOrder.value.id);
    }
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

const beforeUploadAttachment = async (file: File) => {
  if (!currentOrderDetail.value?.order) {
    return false;
  }
  try {
    await fileApi.upload('ORDER', currentOrderDetail.value.order.id, file);
    notifySuccess('附件上传成功');
    await loadOrderDetail(currentOrderDetail.value.order.id);
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
    if (!response.ok) {
      throw new Error('下载失败');
    }
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

onMounted(async () => {
  try {
    await loadBase();
  } catch (error) {
    notifyError(error);
  }
});
</script>

<style scoped>
.order-page {
  display: grid;
  gap: 16px;
}
</style>