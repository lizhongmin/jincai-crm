<template>
  <div class="customer-detail-panel">
    <div class="detail-header">
      <div class="header-left">
        <a-button type="text" @click="emit('back')">返回</a-button>
        <h2 class="detail-title">{{ customer?.name || '客户详情' }}</h2>
      </div>
      <a-space>
        <a-button @click="emit('toggle-collapse')">
          {{ basicInfoCollapsed ? '展开基本信息' : '收起基本信息' }}
        </a-button>
        <a-button @click="emit('edit', customer)">编辑</a-button>
        <a-button @click="emit('transfer', customer)">转移</a-button>
        <a-button @click="emit('move-pool', customer)">移入公海</a-button>
        <a-popconfirm title="确认删除该客户？" @confirm="emit('remove', customer)">
          <a-button danger>删除</a-button>
        </a-popconfirm>
      </a-space>
    </div>

    <div class="detail-layout" :class="{ collapsed: basicInfoCollapsed }">
      <a-card v-show="!basicInfoCollapsed" class="section-card detail-side" :bordered="false">
        <div class="side-block">
          <h3>基本信息</h3>
          <div class="info-row"><span>客户名称</span><strong>{{ customer?.name || '-' }}</strong></div>
          <div class="info-row"><span>手机号</span><strong>{{ customer?.phone || '-' }}</strong></div>
          <div class="info-row"><span>客户等级</span><strong>{{ mapLevel(customer?.level) }}</strong></div>
          <div class="info-row"><span>客户类型</span><strong>{{ mapCustomerType(customer?.customerType) }}</strong></div>
          <div class="info-row"><span>客户来源</span><strong>{{ mapSource(customer?.source) }}</strong></div>
          <div class="info-row"><span>意向等级</span><strong>{{ mapIntention(customer?.intentionLevel) }}</strong></div>
          <div class="info-row"><span>客户状态</span><strong>{{ mapStatus(customer?.status) }}</strong></div>
          <div class="info-row"><span>客户标签</span><strong>{{ customer?.tags || '-' }}</strong></div>
        </div>

        <div class="side-block">
          <h3>地址信息</h3>
          <div class="info-row"><span>地区</span><strong>{{ customer?.city || '-' }}</strong></div>
        </div>

        <div class="side-block">
          <h3>负责人信息</h3>
          <div class="info-row"><span>负责人</span><strong>{{ customer?.ownerUserName || '-' }}</strong></div>
          <div class="info-row"><span>部门</span><strong>{{ customer?.ownerDeptName || '-' }}</strong></div>
          <div class="info-row"><span>最新跟进人员</span><strong>{{ customer?.updatedBy || '-' }}</strong></div>
          <div class="info-row"><span>最新跟进时间</span><strong>{{ formatDateTime(customer?.updatedAt) }}</strong></div>
          <div class="info-row"><span>创建人</span><strong>{{ customer?.createdBy || '-' }}</strong></div>
          <div class="info-row"><span>创建时间</span><strong>{{ formatDateTime(customer?.createdAt) }}</strong></div>
          <div class="info-row"><span>更新人</span><strong>{{ customer?.updatedBy || '-' }}</strong></div>
          <div class="info-row"><span>更新时间</span><strong>{{ formatDateTime(customer?.updatedAt) }}</strong></div>
        </div>
      </a-card>

      <a-card class="section-card detail-main" :bordered="false">
        <a-tabs :active-key="detailTab" @update:activeKey="(key) => emit('update:detail-tab', key as 'contact' | 'order')">
          <a-tab-pane key="contact" tab="出行人信息">
            <div class="detail-pane">
              <div class="list-toolbar">
                <a-button type="primary" @click="emit('add-traveler')">新增出行人</a-button>
                <a-input-search
                  :value="detailContactKeyword"
                  placeholder="搜索出行人"
                  style="width: 240px"
                  @update:value="(val) => emit('update:detail-contact-keyword', val || '')"
                />
              </div>
              <TravelerTable :items="detailContacts" :scroll-x="false" @edit="(record) => emit('edit-traveler', record)" @remove="(record) => emit('remove-traveler', record)" />
            </div>
          </a-tab-pane>

          <a-tab-pane key="order" tab="订单信息">
            <div class="detail-pane">
              <div class="list-toolbar">
                <a-input-search
                  :value="detailOrderKeyword"
                  placeholder="按订单号/类型/状态搜索"
                  style="width: 260px"
                  @update:value="(val) => emit('update:detail-order-keyword', val || '')"
                />
              </div>
              <a-table
                :columns="detailOrderColumns"
                :data-source="detailOrders"
                row-key="id"
                :pagination="detailOrderPagination"
                @change="(pagination) => emit('detail-order-table-change', pagination)"
              >
                <template #bodyCell="{ column, record }">
                  <template v-if="column.dataIndex === 'status'">
                    {{ mapOrderStatus(record.status) }}
                  </template>
                  <template v-else-if="column.dataIndex === 'totalAmount'">
                    {{ record.totalAmount ?? '-' }}
                  </template>
                  <template v-else-if="column.dataIndex === 'createdAt'">
                    {{ formatDateTime(record.createdAt) }}
                  </template>
                  <template v-else-if="column.dataIndex === 'actions'">
                    <a-button type="link" @click="emit('view-order', record)">查看</a-button>
                  </template>
                </template>
              </a-table>
            </div>
          </a-tab-pane>
        </a-tabs>
      </a-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import TravelerTable from './TravelerTable.vue';

defineProps<{
  customer: any;
  basicInfoCollapsed: boolean;
  detailTab: 'contact' | 'order';
  detailContactKeyword: string;
  detailOrderKeyword: string;
  detailContacts: any[];
  detailOrders: any[];
  detailOrderColumns: any[];
  detailOrderPagination: Record<string, any>;
  mapLevel: (value?: string) => string;
  mapCustomerType: (value?: string) => string;
  mapSource: (value?: string) => string;
  mapIntention: (value?: string) => string;
  mapStatus: (value?: string) => string;
  mapOrderStatus: (value?: string) => string;
  formatDateTime: (value?: string) => string;
}>();

const emit = defineEmits<{
  (e: 'back'): void;
  (e: 'toggle-collapse'): void;
  (e: 'edit', record?: any): void;
  (e: 'transfer', record?: any): void;
  (e: 'move-pool', record?: any): void;
  (e: 'remove', record?: any): void;
  (e: 'update:detail-tab', value: 'contact' | 'order'): void;
  (e: 'update:detail-contact-keyword', value: string): void;
  (e: 'update:detail-order-keyword', value: string): void;
  (e: 'add-traveler'): void;
  (e: 'edit-traveler', record: any): void;
  (e: 'remove-traveler', record: any): void;
  (e: 'view-order', record: any): void;
  (e: 'detail-order-table-change', pagination: { current?: number; pageSize?: number }): void;
}>();
</script>

<style scoped>
.customer-detail-panel {
  display: grid;
  gap: 10px;
}

.detail-header {
  border: 1px solid var(--line);
  border-radius: 12px;
  background: #fff;
  padding: 10px 14px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.detail-title {
  margin: 0;
  font-size: 24px;
  font-weight: 700;
}

.detail-layout {
  display: grid;
  grid-template-columns: minmax(300px, 360px) minmax(0, 1fr);
  gap: 10px;
  min-height: 620px;
}

.detail-layout.collapsed {
  grid-template-columns: minmax(0, 1fr);
}

.detail-side {
  height: 100%;
  min-width: 0;
}

.detail-main {
  min-width: 0;
}

.side-block {
  border-bottom: 1px solid var(--line);
  padding-bottom: 10px;
  margin-bottom: 10px;
}

.side-block h3 {
  margin: 0 0 10px;
  font-size: 18px;
  font-weight: 700;
}

.info-row {
  display: grid;
  grid-template-columns: 120px minmax(0, 1fr);
  align-items: center;
  gap: 12px;
  padding: 5px 0;
}

.info-row span {
  color: #6b7280;
}

.info-row strong {
  color: #1f2937;
  font-weight: 600;
}

.detail-main {
  display: flex;
  flex-direction: column;
}

.detail-main :deep(.ant-card-body) {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 16px;
}

.detail-main :deep(.ant-tabs) {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.detail-main :deep(.ant-tabs-nav) {
  margin-bottom: 12px;
}

.detail-main :deep(.ant-tabs-content-holder) {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.detail-main :deep(.ant-tabs-content),
.detail-main :deep(.ant-tabs-tabpane) {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.detail-pane {
  flex: 1;
  min-height: 420px;
  border: 1px solid #eef2f7;
  border-radius: 10px;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 16px;
  background: #fff;
}

.list-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

@media (max-width: 1360px) {
  .detail-layout {
    grid-template-columns: 1fr;
  }
}
</style>
