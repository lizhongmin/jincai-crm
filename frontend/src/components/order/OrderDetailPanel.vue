<template>
  <a-card class="section-card" :bordered="false" title="订单详情">
    <template #extra>
      <a-upload :disabled="!detail?.order" :show-upload-list="false" :before-upload="beforeUploadAttachment">
        <a-button :disabled="!detail?.order">上传附件</a-button>
      </a-upload>
    </template>

    <a-empty v-if="!detail?.order" description="选择订单后查看详情、日志和附件" />

    <template v-else>
      <a-descriptions bordered size="small" :column="2">
        <a-descriptions-item label="订单号">{{ detail.order.orderNo }}</a-descriptions-item>
        <a-descriptions-item label="状态">{{ detail.order.status }}</a-descriptions-item>
        <a-descriptions-item label="客户">{{ detail.order.customerName }}</a-descriptions-item>
        <a-descriptions-item label="线路">{{ detail.order.routeName }}</a-descriptions-item>
        <a-descriptions-item label="团期">{{ detail.order.departureLabel }}</a-descriptions-item>
        <a-descriptions-item label="订单类型">{{ detail.order.orderType }}</a-descriptions-item>
        <a-descriptions-item label="产品分类">{{ detail.order.productCategory }}</a-descriptions-item>
        <a-descriptions-item label="出行人数">{{ detail.order.travelerCount }}</a-descriptions-item>
        <a-descriptions-item label="订单金额">{{ detail.order.totalAmount }} {{ detail.order.currency }}</a-descriptions-item>
        <a-descriptions-item label="销售">{{ detail.order.salesUserId || '-' }}</a-descriptions-item>
      </a-descriptions>

      <a-tabs style="margin-top: 16px">
        <a-tab-pane key="travelers" tab="出行人">
          <a-table :columns="travelerColumns" :data-source="detail.travelers || []" row-key="id" :pagination="false" />
        </a-tab-pane>
        <a-tab-pane key="pricing" tab="价格明细">
          <a-table :columns="priceColumns" :data-source="detail.priceItems || []" row-key="id" :pagination="false" />
        </a-tab-pane>
        <a-tab-pane key="logs" tab="状态流转">
          <a-table :columns="logColumns" :data-source="logs" row-key="id" :pagination="false" />
        </a-tab-pane>
        <a-tab-pane key="audits" tab="审计日志">
          <a-table :columns="auditColumns" :data-source="audits" row-key="id" :pagination="false" />
        </a-tab-pane>
        <a-tab-pane key="files" tab="附件列表">
          <a-table :columns="fileColumns" :data-source="attachments" row-key="id" :pagination="false">
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
const props = defineProps<{
  detail: any | null;
  logs: any[];
  audits: any[];
  attachments: any[];
}>();

const emit = defineEmits<{
  (e: 'upload', file: File): void;
  (e: 'download', record: any): void;
}>();

const travelerColumns = [
  { title: '姓名', dataIndex: 'name', width: 140 },
  { title: '证件类型', dataIndex: 'idType', width: 120 },
  { title: '证件号', dataIndex: 'idNo' },
  { title: '手机号', dataIndex: 'phone', width: 140 }
];

const priceColumns = [
  { title: '出行人', dataIndex: 'travelerName', width: 140, customRender: ({ text }: any) => text || '附加项' },
  { title: '价格项', dataIndex: 'itemName', width: 180 },
  { title: '价格类型', dataIndex: 'priceType', width: 140 },
  { title: '单价', dataIndex: 'unitPrice', width: 120 },
  { title: '数量', dataIndex: 'quantity', width: 100 },
  { title: '金额', dataIndex: 'amount', width: 120 }
];

const logColumns = [
  { title: '从状态', dataIndex: 'fromStatus' },
  { title: '到状态', dataIndex: 'toStatus' },
  { title: '备注', dataIndex: 'remark' },
  { title: '时间', dataIndex: 'createdAt', width: 180 }
];

const auditColumns = [
  { title: '字段', dataIndex: 'fieldName' },
  { title: '变更前', dataIndex: 'beforeValue' },
  { title: '变更后', dataIndex: 'afterValue' },
  { title: '操作人', dataIndex: 'createdBy', width: 90 },
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