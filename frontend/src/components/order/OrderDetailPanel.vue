<template>
  <a-card class="section-card" :bordered="false" title="订单详情">
    <template #extra>
      <a-upload :disabled="!detail?.order" :show-upload-list="false" :before-upload="beforeUploadAttachment">
        <a-button :disabled="!detail?.order">上传附件</a-button>
      </a-upload>
    </template>

    <a-empty v-if="!detail?.order" description="选择订单后查看详情、流转和附件。" />

    <template v-else>
      <div class="status-grid">
        <div class="status-item">
          <span class="label">主状态</span>
          <a-tag color="blue">{{ orderStatusLabel(detail.order.status) }}</a-tag>
        </div>
        <div class="status-item">
          <span class="label">签约</span>
          <a-tag>{{ contractStatusLabel(detail.order.contractStatus) }}</a-tag>
        </div>
        <div class="status-item">
          <span class="label">收款</span>
          <a-tag>{{ paymentStatusLabel(detail.order.paymentStatus) }}</a-tag>
        </div>
        <div class="status-item">
          <span class="label">锁位</span>
          <a-tag>{{ inventoryStatusLabel(detail.order.inventoryStatus) }}</a-tag>
        </div>
        <div class="status-item">
          <span class="label">结算</span>
          <a-tag>{{ settlementStatusLabel(detail.order.settlementStatus) }}</a-tag>
        </div>
      </div>

      <a-descriptions bordered size="small" :column="2">
        <a-descriptions-item label="订单号">{{ detail.order.orderNo }}</a-descriptions-item>
        <a-descriptions-item label="订单状态">{{ orderStatusLabel(detail.order.status) }}</a-descriptions-item>
        <a-descriptions-item label="客户">{{ detail.order.customerName }}</a-descriptions-item>
        <a-descriptions-item label="线路">{{ detail.order.routeName }}</a-descriptions-item>
        <a-descriptions-item label="团期">{{ detail.order.departureLabel }}</a-descriptions-item>
        <a-descriptions-item label="订单类型">{{ orderTypeLabel(detail.order.orderType) }}</a-descriptions-item>
        <a-descriptions-item label="产品分类">{{ detail.order.productCategory }}</a-descriptions-item>
        <a-descriptions-item label="出行人数">{{ detail.order.travelerCount }}</a-descriptions-item>
        <a-descriptions-item label="订单金额">{{ detail.order.totalAmount }} {{ currencyLabel(detail.order.currency) }}</a-descriptions-item>
        <a-descriptions-item label="销售">{{ detail.order.salesUserId || '-' }}</a-descriptions-item>
      </a-descriptions>

      <a-tabs style="margin-top: 12px">
        <a-tab-pane key="travelers" tab="出行人">
          <a-table :columns="travelerColumns" :data-source="detail.travelers || []" row-key="id" :pagination="false" :scroll="{ x: 760 }" />
        </a-tab-pane>
        <a-tab-pane key="pricing" tab="价格明细">
          <a-table :columns="priceColumns" :data-source="detail.priceItems || []" row-key="id" :pagination="false" :scroll="{ x: 900 }" />
        </a-tab-pane>
        <a-tab-pane key="logs" tab="状态流转">
          <a-timeline>
            <a-timeline-item v-for="item in logs" :key="item.id">
              <div class="timeline-title">{{ orderStatusLabel(item.fromStatus) }} → {{ orderStatusLabel(item.toStatus) }}</div>
              <div class="timeline-meta">{{ item.remark || '-' }} · {{ item.createdAt }}</div>
            </a-timeline-item>
          </a-timeline>
          <a-divider />
          <a-table :columns="logColumns" :data-source="logs" row-key="id" :pagination="false" :scroll="{ x: 780 }" />
        </a-tab-pane>
        <a-tab-pane key="audits" tab="审计日志">
          <a-table :columns="auditColumns" :data-source="audits" row-key="id" :pagination="false" :scroll="{ x: 900 }" />
        </a-tab-pane>
        <a-tab-pane key="files" tab="附件">
          <a-table :columns="fileColumns" :data-source="attachments" row-key="id" :pagination="false" :scroll="{ x: 900 }">
            <template #bodyCell="{ column, record }">
              <template v-if="column.dataIndex === 'actions'">
                <a-button type="link" @click="emit('download', record)">下载</a-button>
              </template>
            </template>
          </a-table>
        </a-tab-pane>
      </a-tabs>
    </template>
  </a-card>
</template>

<script setup lang="ts">
import {
  CONTRACT_STATUS_LABEL_MAP,
  CURRENCY_LABEL_MAP,
  INVENTORY_STATUS_LABEL_MAP,
  ORDER_STATUS_LABEL_MAP,
  ORDER_TYPE_LABEL_MAP,
  PAYMENT_STATUS_LABEL_MAP,
  PRICE_TYPE_LABEL_MAP,
  SETTLEMENT_STATUS_LABEL_MAP,
  enumLabel
} from '../../constants/display';

defineProps<{
  detail: any | null;
  logs: any[];
  audits: any[];
  attachments: any[];
}>();

const emit = defineEmits<{
  (e: 'upload', file: File): void;
  (e: 'download', record: any): void;
}>();

const orderStatusLabel = (value?: string) => enumLabel(ORDER_STATUS_LABEL_MAP, value);
const contractStatusLabel = (value?: string) => enumLabel(CONTRACT_STATUS_LABEL_MAP, value);
const paymentStatusLabel = (value?: string) => enumLabel(PAYMENT_STATUS_LABEL_MAP, value);
const inventoryStatusLabel = (value?: string) => enumLabel(INVENTORY_STATUS_LABEL_MAP, value);
const settlementStatusLabel = (value?: string) => enumLabel(SETTLEMENT_STATUS_LABEL_MAP, value);
const orderTypeLabel = (value?: string) => enumLabel(ORDER_TYPE_LABEL_MAP, value);
const currencyLabel = (value?: string) => enumLabel(CURRENCY_LABEL_MAP, value);

const travelerColumns = [
  { title: '姓名', dataIndex: 'name', width: 140 },
  { title: '证件类型', dataIndex: 'idType', width: 120 },
  { title: '证件号', dataIndex: 'idNo', width: 280 },
  { title: '手机号', dataIndex: 'phone', width: 140 }
];

const priceColumns = [
  { title: '出行人', dataIndex: 'travelerName', width: 140, customRender: ({ text }: any) => text || '附加项' },
  { title: '价格项', dataIndex: 'itemName', width: 180 },
  { title: '价格类型', dataIndex: 'priceType', width: 140, customRender: ({ text }: any) => enumLabel(PRICE_TYPE_LABEL_MAP, text) },
  { title: '单价', dataIndex: 'unitPrice', width: 120 },
  { title: '数量', dataIndex: 'quantity', width: 100 },
  { title: '金额', dataIndex: 'amount', width: 120 }
];

const logColumns = [
  { title: '原状态', dataIndex: 'fromStatus', customRender: ({ text }: any) => orderStatusLabel(text) },
  { title: '新状态', dataIndex: 'toStatus', customRender: ({ text }: any) => orderStatusLabel(text) },
  { title: '备注', dataIndex: 'remark' },
  { title: '时间', dataIndex: 'createdAt', width: 180 }
];

const auditColumns = [
  { title: '字段', dataIndex: 'fieldName' },
  { title: '变更前', dataIndex: 'beforeValue' },
  { title: '变更后', dataIndex: 'afterValue' },
  { title: '操作人', dataIndex: 'createdBy', width: 120 },
  { title: '时间', dataIndex: 'createdAt', width: 180 }
];

const fileColumns = [
  { title: '文件名', dataIndex: 'fileName' },
  { title: '大小(byte)', dataIndex: 'fileSize', width: 120 },
  { title: '类型', dataIndex: 'contentType', width: 180 },
  { title: '上传时间', dataIndex: 'createdAt', width: 180 },
  { title: '操作', dataIndex: 'actions', width: 90 }
];

const beforeUploadAttachment = (file: File) => {
  emit('upload', file);
  return false;
};
</script>

<style scoped>
.status-grid {
  display: grid;
  grid-template-columns: repeat(5, minmax(120px, 1fr));
  gap: 8px;
  margin-bottom: 12px;
}

.status-item {
  border: 1px solid var(--line);
  border-radius: 8px;
  padding: 8px;
  display: grid;
  gap: 6px;
}

.status-item .label {
  color: var(--muted);
  font-size: 12px;
}

.timeline-title {
  font-weight: 600;
}

.timeline-meta {
  color: var(--muted);
  font-size: 12px;
}

@media (max-width: 1100px) {
  .status-grid {
    grid-template-columns: repeat(2, minmax(120px, 1fr));
  }
}
</style>
