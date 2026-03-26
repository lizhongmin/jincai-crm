<template>
  <div class="customer-info-panel">
    <a-spin :spinning="loading">
      <a-descriptions :column="{ xs: 1, sm: 1, md: 2 }" bordered size="small">
        <a-descriptions-item label="客户名称">
          {{ customer.name || '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="手机号">
          {{ customer.phone || '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="客户类型">
          {{ mapCustomerType(customer.customerType) }}
        </a-descriptions-item>
        <a-descriptions-item label="客户等级">
          <a-tag v-if="customer.level === '重点客户'" color="blue">{{ customer.level }}</a-tag>
          <a-tag v-else color="green">{{ customer.level || '普通客户' }}</a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="客户来源">
          {{ customer.source || '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="意向等级">
          {{ mapIntention(customer.intentionLevel) }}
        </a-descriptions-item>
        <a-descriptions-item label="状态">
          <a-tag :color="statusColor(customer.status)">{{ mapStatus(customer.status) }}</a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="城市">
          {{ customer.city || '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="微信">
          {{ customer.wechat || '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="邮箱">
          {{ customer.email || '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="负责人">
          {{ customer.ownerUserName || '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="所属部门">
          {{ customer.ownerDeptName || '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="标签" :span="2">
          {{ customer.tags || '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="创建时间">
          {{ customer.createdAt || '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="更新时间">
          {{ customer.updatedAt || '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="客户备注" :span="2">
          {{ customer.remark || '-' }}
        </a-descriptions-item>
      </a-descriptions>

      <div class="info-actions">
        <a-button type="primary" @click="editCustomer">编辑客户</a-button>
        <a-button @click="refresh">刷新</a-button>
      </div>
    </a-spin>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';

const props = defineProps<{
  customer: any;
}>();

const emit = defineEmits<{
  (e: 'refresh'): void;
  (e: 'edit', customer: any): void;
}>();

const loading = ref(false);

const mapCustomerType = (value?: string) => ({ PERSONAL: '个人', ENTERPRISE: '企业' }[value || ''] || value || '-');
const mapIntention = (value?: string) => ({ HIGH: '高', MEDIUM: '中', LOW: '低' }[value || ''] || value || '-');
const mapStatus = (value?: string) => ({ ACTIVE: '正常', INACTIVE: '沉默', BLACKLIST: '黑名单' }[value || ''] || value || '-');
const statusColor = (value?: string) => ({ ACTIVE: 'green', INACTIVE: 'orange', BLACKLIST: 'red' }[value || ''] || 'default');

const editCustomer = () => {
  emit('edit', props.customer);
};

const refresh = () => {
  loading.value = true;
  setTimeout(() => {
    emit('refresh');
    loading.value = false;
  }, 300);
};
</script>

<style scoped>
.customer-info-panel {
  padding: 12px 0;
}

.info-actions {
  margin-top: 16px;
  display: flex;
  gap: 8px;
  justify-content: flex-end;
}
</style>
