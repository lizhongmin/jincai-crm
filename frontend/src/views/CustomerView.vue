<template>
  <div class="customer-page">
    <a-card class="section-card" :bordered="false">
      <template #title>客户中心</template>
      <a-tabs v-model:activeKey="activeTab">
        <a-tab-pane key="customers" tab="客户">
          <div class="toolbar-row">
            <a-input-search v-model:value="customerKeyword" placeholder="按客户名/手机号/城市筛选" style="width: 300px" />
            <a-button type="primary" @click="openCustomer()">新增客户</a-button>
            <a-upload :show-upload-list="false" :before-upload="beforeImport">
              <a-button>导入客户</a-button>
            </a-upload>
          </div>
          <customer-table
            style="margin-top: 12px"
            :items="filteredCustomers"
            @view="openCustomerDetail"
            @edit="openCustomer"
            @remove="removeCustomer"
          />
        </a-tab-pane>

        <a-tab-pane key="travelers" tab="出行人">
          <div class="toolbar-row">
            <a-select
              v-model:value="travelerFilterCustomerId"
              allow-clear
              placeholder="按客户筛选"
              style="width: 220px"
              @change="loadTravelers"
            >
              <a-select-option v-for="item in customers" :key="item.id" :value="item.id">{{ item.name }}</a-select-option>
            </a-select>
            <a-button type="primary" @click="openTraveler()">新增出行人</a-button>
          </div>
          <traveler-table
            style="margin-top: 12px"
            :items="decoratedTravelers"
            @edit="openTraveler"
            @remove="removeTraveler"
          />
        </a-tab-pane>
      </a-tabs>
    </a-card>

    <a-drawer v-model:open="detailOpen" width="620" title="客户详情">
      <a-empty v-if="!activeCustomer" description="未选择客户" />
      <template v-else>
        <a-descriptions :column="1" bordered size="small">
          <a-descriptions-item label="客户名称">{{ activeCustomer.name }}</a-descriptions-item>
          <a-descriptions-item label="手机号">{{ activeCustomer.phone }}</a-descriptions-item>
          <a-descriptions-item label="客户类型">{{ mapCustomerType(activeCustomer.customerType) }}</a-descriptions-item>
          <a-descriptions-item label="来源">{{ mapSource(activeCustomer.source) }}</a-descriptions-item>
          <a-descriptions-item label="意向等级">{{ mapIntention(activeCustomer.intentionLevel) }}</a-descriptions-item>
          <a-descriptions-item label="状态">{{ mapStatus(activeCustomer.status) }}</a-descriptions-item>
          <a-descriptions-item label="客户等级">{{ activeCustomer.level || '-' }}</a-descriptions-item>
          <a-descriptions-item label="负责人">{{ activeCustomer.ownerUserName || '-' }}</a-descriptions-item>
          <a-descriptions-item label="城市">{{ activeCustomer.city || '-' }}</a-descriptions-item>
          <a-descriptions-item label="微信">{{ activeCustomer.wechat || '-' }}</a-descriptions-item>
          <a-descriptions-item label="邮箱">{{ activeCustomer.email || '-' }}</a-descriptions-item>
          <a-descriptions-item label="标签">{{ activeCustomer.tags || '-' }}</a-descriptions-item>
          <a-descriptions-item label="备注">{{ activeCustomer.remark || '-' }}</a-descriptions-item>
        </a-descriptions>
        <a-divider orientation="left">关联出行人</a-divider>
        <traveler-table :items="activeCustomerTravelers" @edit="openTraveler" @remove="removeTraveler" />
      </template>
    </a-drawer>

    <a-drawer v-model:open="customerModal" :title="customerForm.id ? '编辑客户' : '新增客户'" placement="right" :width="760">
      <template #extra>
        <a-space>
          <a-button @click="customerModal = false">取消</a-button>
          <a-button type="primary" :loading="saving" @click="saveCustomer">保存</a-button>
        </a-space>
      </template>
      <a-form layout="vertical">
        <div class="grid-2">
          <a-form-item label="客户名称" required>
            <a-input v-model:value="customerForm.name" />
          </a-form-item>
          <a-form-item label="手机号" required>
            <a-input v-model:value="customerForm.phone" />
          </a-form-item>
        </div>
        <div class="grid-2">
          <a-form-item label="客户类型">
            <a-select v-model:value="customerForm.customerType">
              <a-select-option value="PERSONAL">个人</a-select-option>
              <a-select-option value="ENTERPRISE">企业</a-select-option>
            </a-select>
          </a-form-item>
          <a-form-item label="来源">
            <a-select v-model:value="customerForm.source">
              <a-select-option value="MANUAL">手工录入</a-select-option>
              <a-select-option value="REFERRAL">老客推荐</a-select-option>
              <a-select-option value="ONLINE">线上咨询</a-select-option>
              <a-select-option value="PHONE">电话咨询</a-select-option>
              <a-select-option value="WALK_IN">到店</a-select-option>
            </a-select>
          </a-form-item>
        </div>
        <div class="grid-2">
          <a-form-item label="意向等级">
            <a-select v-model:value="customerForm.intentionLevel">
              <a-select-option value="HIGH">高</a-select-option>
              <a-select-option value="MEDIUM">中</a-select-option>
              <a-select-option value="LOW">低</a-select-option>
            </a-select>
          </a-form-item>
          <a-form-item label="客户状态">
            <a-select v-model:value="customerForm.status">
              <a-select-option value="ACTIVE">正常</a-select-option>
              <a-select-option value="INACTIVE">沉默</a-select-option>
              <a-select-option value="BLACKLIST">黑名单</a-select-option>
            </a-select>
          </a-form-item>
        </div>
        <div class="grid-2">
          <a-form-item label="客户等级">
            <a-select v-model:value="customerForm.level">
              <a-select-option value="A">A</a-select-option>
              <a-select-option value="B">B</a-select-option>
              <a-select-option value="C">C</a-select-option>
            </a-select>
          </a-form-item>
          <a-form-item label="负责人">
            <a-select v-model:value="customerForm.ownerUserId" allow-clear placeholder="不选则默认当前登录用户">
              <a-select-option v-for="item in users" :key="item.id" :value="item.id">{{ item.fullName }} ({{ item.username }})</a-select-option>
            </a-select>
          </a-form-item>
        </div>
        <div class="grid-2">
          <a-form-item label="城市">
            <a-input v-model:value="customerForm.city" />
          </a-form-item>
          <a-form-item label="微信">
            <a-input v-model:value="customerForm.wechat" />
          </a-form-item>
        </div>
        <div class="grid-2">
          <a-form-item label="邮箱">
            <a-input v-model:value="customerForm.email" />
          </a-form-item>
          <a-form-item label="标签">
            <a-input v-model:value="customerForm.tags" placeholder="多个标签逗号分隔" />
          </a-form-item>
        </div>
        <a-form-item label="备注">
          <a-textarea v-model:value="customerForm.remark" :rows="3" />
        </a-form-item>
      </a-form>
    </a-drawer>

    <a-drawer v-model:open="travelerModal" :title="travelerForm.id ? '编辑出行人' : '新增出行人'" placement="right" :width="560">
      <template #extra>
        <a-space>
          <a-button @click="travelerModal = false">取消</a-button>
          <a-button type="primary" :loading="saving" @click="saveTraveler">保存</a-button>
        </a-space>
      </template>
      <a-form layout="vertical">
        <a-form-item label="所属客户" required>
          <a-select v-model:value="travelerForm.customerId" placeholder="请选择客户">
            <a-select-option v-for="item in customers" :key="item.id" :value="item.id">{{ item.name }}</a-select-option>
          </a-select>
        </a-form-item>
        <div class="grid-2">
          <a-form-item label="姓名" required>
            <a-input v-model:value="travelerForm.name" />
          </a-form-item>
          <a-form-item label="手机号">
            <a-input v-model:value="travelerForm.phone" />
          </a-form-item>
        </div>
        <div class="grid-2">
          <a-form-item label="证件类型">
            <a-input v-model:value="travelerForm.idType" placeholder="身份证/护照" />
          </a-form-item>
          <a-form-item label="证件号">
            <a-input v-model:value="travelerForm.idNo" />
          </a-form-item>
        </div>
      </a-form>
    </a-drawer>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import CustomerTable from '../components/customer/CustomerTable.vue';
import TravelerTable from '../components/customer/TravelerTable.vue';
import { customerApi, orgApi } from '../api/crm';
import { notifyError, notifySuccess } from '../utils/notify';

const activeTab = ref('customers');
const saving = ref(false);
const customerModal = ref(false);
const travelerModal = ref(false);
const detailOpen = ref(false);
const customerKeyword = ref('');
const travelerFilterCustomerId = ref<number | undefined>();

const customers = ref<any[]>([]);
const travelers = ref<any[]>([]);
const users = ref<any[]>([]);
const activeCustomer = ref<any>(null);

const customerForm = reactive({
  id: undefined as number | undefined,
  name: '',
  phone: '',
  customerType: 'PERSONAL',
  source: 'MANUAL',
  intentionLevel: 'MEDIUM',
  status: 'ACTIVE',
  level: 'B',
  ownerUserId: undefined as number | undefined,
  wechat: '',
  email: '',
  city: '',
  tags: '',
  remark: ''
});
const travelerForm = reactive({
  id: undefined as number | undefined,
  customerId: undefined as number | undefined,
  name: '',
  idType: '',
  idNo: '',
  phone: ''
});

const customerMap = computed(() => Object.fromEntries(customers.value.map((item) => [item.id, item.name])));

const filteredCustomers = computed(() => {
  const keyword = customerKeyword.value.trim().toLowerCase();
  if (!keyword) return customers.value;
  return customers.value.filter((item) =>
    [item.name, item.phone, item.city].filter(Boolean).some((value) => String(value).toLowerCase().includes(keyword))
  );
});

const decoratedTravelers = computed(() =>
  travelers.value.map((item) => ({
    ...item,
    customerName: customerMap.value[item.customerId] || '-'
  }))
);

const activeCustomerTravelers = computed(() =>
  decoratedTravelers.value.filter((item) => item.customerId === activeCustomer.value?.id)
);

const mapCustomerType = (value?: string) => ({ PERSONAL: '个人', ENTERPRISE: '企业' }[value || ''] || value || '-');
const mapSource = (value?: string) => ({ MANUAL: '手工录入', REFERRAL: '老客推荐', ONLINE: '线上咨询', PHONE: '电话咨询', WALK_IN: '到店' }[value || ''] || value || '-');
const mapIntention = (value?: string) => ({ HIGH: '高', MEDIUM: '中', LOW: '低' }[value || ''] || value || '-');
const mapStatus = (value?: string) => ({ ACTIVE: '正常', INACTIVE: '沉默', BLACKLIST: '黑名单' }[value || ''] || value || '-');

const loadCustomers = async () => {
  const { data } = await customerApi.customers();
  customers.value = data.data || [];
};

const loadTravelers = async () => {
  const { data } = await customerApi.travelerList(travelerFilterCustomerId.value);
  travelers.value = data.data || [];
};

const loadUsers = async () => {
  const { data } = await orgApi.users();
  users.value = data.data || [];
};

const openCustomer = (record?: any) => {
  customerForm.id = record?.id;
  customerForm.name = record?.name || '';
  customerForm.phone = record?.phone || '';
  customerForm.customerType = record?.customerType || 'PERSONAL';
  customerForm.source = record?.source || 'MANUAL';
  customerForm.intentionLevel = record?.intentionLevel || 'MEDIUM';
  customerForm.status = record?.status || 'ACTIVE';
  customerForm.level = record?.level || 'B';
  customerForm.ownerUserId = record?.ownerUserId;
  customerForm.wechat = record?.wechat || '';
  customerForm.email = record?.email || '';
  customerForm.city = record?.city || '';
  customerForm.tags = record?.tags || '';
  customerForm.remark = record?.remark || '';
  customerModal.value = true;
};

const openCustomerDetail = async (record: any) => {
  activeCustomer.value = record;
  travelerFilterCustomerId.value = record.id;
  await loadTravelers();
  detailOpen.value = true;
};

const saveCustomer = async () => {
  if (!customerForm.name.trim() || !customerForm.phone.trim()) return;
  saving.value = true;
  try {
    const payload = {
      name: customerForm.name,
      phone: customerForm.phone,
      customerType: customerForm.customerType,
      source: customerForm.source,
      intentionLevel: customerForm.intentionLevel,
      status: customerForm.status,
      level: customerForm.level,
      ownerUserId: customerForm.ownerUserId,
      wechat: customerForm.wechat,
      email: customerForm.email,
      city: customerForm.city,
      tags: customerForm.tags,
      remark: customerForm.remark
    };
    if (customerForm.id) {
      await customerApi.updateCustomer(customerForm.id, payload);
      notifySuccess('客户更新成功');
    } else {
      await customerApi.createCustomer(payload);
      notifySuccess('客户创建成功');
    }
    customerModal.value = false;
    await loadCustomers();
  } catch (error) {
    notifyError(error);
  } finally {
    saving.value = false;
  }
};

const removeCustomer = async (record: any) => {
  try {
    await customerApi.deleteCustomer(record.id);
    notifySuccess('客户删除成功');
    if (activeCustomer.value?.id === record.id) {
      activeCustomer.value = null;
      detailOpen.value = false;
    }
    await loadCustomers();
    await loadTravelers();
  } catch (error) {
    notifyError(error);
  }
};

const openTraveler = (record?: any) => {
  travelerForm.id = record?.id;
  travelerForm.customerId = record?.customerId || activeCustomer.value?.id || travelerFilterCustomerId.value;
  travelerForm.name = record?.name || '';
  travelerForm.idType = record?.idType || '';
  travelerForm.idNo = record?.idNo || '';
  travelerForm.phone = record?.phone || '';
  travelerModal.value = true;
};

const saveTraveler = async () => {
  if (!travelerForm.customerId || !travelerForm.name.trim()) return;
  saving.value = true;
  try {
    const payload = {
      name: travelerForm.name,
      idType: travelerForm.idType,
      idNo: travelerForm.idNo,
      phone: travelerForm.phone
    };
    if (travelerForm.id) {
      await customerApi.updateTraveler(travelerForm.id, payload);
      notifySuccess('出行人更新成功');
    } else {
      await customerApi.createTraveler(travelerForm.customerId, payload);
      notifySuccess('出行人新增成功');
    }
    travelerModal.value = false;
    await loadTravelers();
  } catch (error) {
    notifyError(error);
  } finally {
    saving.value = false;
  }
};

const removeTraveler = async (record: any) => {
  try {
    await customerApi.deleteTraveler(record.id);
    notifySuccess('出行人删除成功');
    await loadTravelers();
  } catch (error) {
    notifyError(error);
  }
};

const beforeImport = async (file: File) => {
  try {
    await customerApi.importCustomers(file);
    notifySuccess('客户导入完成');
    await loadCustomers();
    await loadTravelers();
  } catch (error) {
    notifyError(error);
  }
  return false;
};

onMounted(async () => {
  try {
    await Promise.all([loadCustomers(), loadTravelers(), loadUsers()]);
  } catch (error) {
    notifyError(error);
  }
});
</script>

<style scoped>
.customer-page {
  display: grid;
  gap: 16px;
}
</style>
