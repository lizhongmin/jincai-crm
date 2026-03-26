
<template>
  <div class="customer-manage">
    <a-card v-if="!detailMode" class="section-card customer-shell" :bordered="false">
      <a-tabs v-model:activeKey="moduleTab" class="module-tabs">
        <a-tab-pane key="customer" tab="客户" />
        <a-tab-pane key="contact" tab="出行人" />
        <a-tab-pane key="pool" tab="公海" />
      </a-tabs>

      <div class="list-toolbar">
        <div class="toolbar-row">
          <a-button v-if="moduleTab !== 'contact'" type="primary" @click="openCustomer()">新建客户</a-button>
          <a-button v-if="moduleTab === 'contact'" type="primary" @click="openTraveler()">新建出行人</a-button>
          <a-upload v-if="moduleTab === 'customer'" :show-upload-list="false" :before-upload="beforeImport">
            <a-button>导入客户</a-button>
          </a-upload>
          <a-button v-if="moduleTab !== 'contact'" @click="exportCustomerCsv">导出所有页</a-button>
        </div>

        <div class="toolbar-row">
          <a-input-search
            v-model:value="keyword"
            :placeholder="moduleTab === 'contact' ? '通过出行人关键词搜索' : '通过客户名称搜索'"
            style="width: 280px"
            @search="onSearch"
          >
            <template #enterButton>
              <a-button type="primary">搜索</a-button>
            </template>
          </a-input-search>
          <a-select v-if="moduleTab !== 'contact'" v-model:value="viewLabel" style="width: 170px">
            <a-select-option value="所有客户">所有客户</a-select-option>
            <a-select-option value="重点客户">重点客户</a-select-option>
            <a-select-option value="普通客户">普通客户</a-select-option>
          </a-select>
        </div>
      </div>

      <a-table
        v-if="moduleTab !== 'contact'"
        class="customer-table"
        :data-source="visibleCustomers"
        :columns="customerColumns"
        row-key="id"
        :pagination="{
          current: customerPage,
          pageSize: customerPageSize,
          total: customerTotal,
          showSizeChanger: true,
          pageSizeOptions: ['10', '20', '50'],
          showTotal: (total: number) => `共 ${total} 条`
        }"
        :scroll="{ x: 'max-content' }"
        @change="onCustomerTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.dataIndex === 'name'">
            <a class="name-link" @click="openCustomerDetail(record)">{{ record.name }}</a>
          </template>
          <template v-else-if="column.dataIndex === 'level'">
            {{ mapLevel(record.level) }}
          </template>
          <template v-else-if="column.dataIndex === 'customerType'">
            {{ mapCustomerType(record.customerType) }}
          </template>
          <template v-else-if="column.dataIndex === 'source'">
            {{ mapSource(record.source) }}
          </template>
          <template v-else-if="column.dataIndex === 'intentionLevel'">
            {{ mapIntention(record.intentionLevel) }}
          </template>
          <template v-else-if="column.dataIndex === 'status'">
            {{ mapStatus(record.status) }}
          </template>
          <template v-else-if="column.dataIndex === 'tags'">
            {{ record.tags || '-' }}
          </template>
          <template v-else-if="column.dataIndex === 'city'">
            {{ record.city || '-' }}
          </template>
          <template v-else-if="column.dataIndex === 'actions'">
            <a-space :size="8">
              <a-button type="link" @click="openCustomer(record)">编辑</a-button>
              <a-button type="link" @click="openTransfer(record)">转移</a-button>
              <a-dropdown>
                <a-button type="link">更多</a-button>
                <template #overlay>
                  <a-menu @click="(info) => onMoreAction(String(info.key), record)">
                    <a-menu-item key="movePool">移入公海</a-menu-item>
                    <a-menu-item key="delete">
                      <span class="danger-text">删除</span>
                    </a-menu-item>
                  </a-menu>
                </template>
              </a-dropdown>
            </a-space>
          </template>
        </template>
      </a-table>

      <TravelerTable
        v-else
        class="customer-table"
        :items="visibleContacts"
        @edit="openTraveler"
        @remove="removeTraveler"
      />
    </a-card>
    <CustomerDetailPanel
      v-else
      :customer="selectedCustomer"
      :basic-info-collapsed="basicInfoCollapsed"
      :detail-tab="detailTab"
      :detail-contact-keyword="detailContactKeyword"
      :detail-order-keyword="detailOrderKeyword"
      :detail-contacts="detailContacts"
      :detail-orders="detailOrders"
      :detail-order-columns="detailOrderColumns"
      :detail-order-pagination="{
        current: detailOrderPage,
        pageSize: detailOrderPageSize,
        total: detailOrderTotal,
        showSizeChanger: true,
        pageSizeOptions: ['8', '16', '32'],
        showTotal: (total: number) => `共 ${total} 条`
      }"
      :map-level="mapLevel"
      :map-customer-type="mapCustomerType"
      :map-source="mapSource"
      :map-intention="mapIntention"
      :map-status="mapStatus"
      :map-order-status="mapOrderStatus"
      :format-date-time="formatDateTime"
      @back="backToList"
      @toggle-collapse="basicInfoCollapsed = !basicInfoCollapsed"
      @edit="openCustomer(selectedCustomer || undefined)"
      @transfer="openTransfer(selectedCustomer || undefined)"
      @move-pool="moveCustomerToPool(selectedCustomer)"
      @remove="removeCustomer(selectedCustomer)"
      @update:detail-tab="detailTab = $event"
      @update:detail-contact-keyword="detailContactKeyword = $event"
      @update:detail-order-keyword="detailOrderKeyword = $event"
      @add-traveler="openTraveler()"
      @edit-traveler="openTraveler"
      @remove-traveler="removeTraveler"
      @view-order="router.push(`/orders/${$event.id}`)"
      @detail-order-table-change="onDetailOrderTableChange"
    />

    <a-modal v-model:open="transferOpen" title="客户转移" :confirm-loading="saving" @ok="confirmTransfer">
      <a-form layout="vertical">
        <a-form-item label="当前客户">
          <a-input :value="transferCustomer?.name || '-'" disabled />
        </a-form-item>
        <a-form-item label="转移到" required>
          <a-select v-model:value="transferOwnerUserId" placeholder="请选择接收负责人">
            <a-select-option v-for="item in users" :key="item.id" :value="item.id">
              {{ item.fullName }} ({{ item.username }})
            </a-select-option>
          </a-select>
        </a-form-item>
      </a-form>
    </a-modal>

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

    <TravelerFormDrawer
      v-model:open="travelerModal"
      :record="selectedTravelerRecord"
      :customers="customers"
      :default-customer-id="selectedCustomer?.id"
      @success="loadTravelers"
    />
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue';
import { useRouter } from 'vue-router';
import TravelerTable from '../components/customer/TravelerTable.vue';
import TravelerFormDrawer from '../components/customer/TravelerFormDrawer.vue';
import CustomerDetailPanel from '../components/customer/CustomerDetailPanel.vue';
import { customerApi, orderApi } from '../api/crm';
import { useAuthStore } from '../stores/auth';
import { notifyError, notifySuccess } from '../utils/notify';

type ListFilter = 'all' | 'mine' | 'cooperate';
type ModuleTab = 'customer' | 'contact' | 'pool';
type DetailTab = 'contact' | 'order';

const router = useRouter();
const auth = useAuthStore();

const moduleTab = ref<ModuleTab>('customer');
const listFilter = ref<ListFilter>('all');
const detailTab = ref<DetailTab>('contact');
const detailMode = ref(false);
const basicInfoCollapsed = ref(false);
const saving = ref(false);
const customerModal = ref(false);
const travelerModal = ref(false);
const transferOpen = ref(false);
const customerPage = ref(1);
const customerPageSize = ref(10);
const customerTotal = ref(0);

const keyword = ref('');
const viewLabel = ref('所有客户');
const detailContactKeyword = ref('');
const detailOrderKeyword = ref('');

const customers = ref<any[]>([]);
const travelers = ref<any[]>([]);
const users = ref<any[]>([]);
const detailOrderRows = ref<any[]>([]);
const detailOrderPage = ref(1);
const detailOrderPageSize = ref(8);
const detailOrderTotal = ref(0);
const selectedCustomer = ref<any | null>(null);
const selectedTravelerRecord = ref<any | null>(null);
const transferCustomer = ref<any | null>(null);
const transferOwnerUserId = ref<number | undefined>();

const customerColumns = [
  {
    title: '序号',
    dataIndex: 'seq',
    width: 60,
    fixed: 'left' as const,
    customRender: ({ index }: any) => (customerPage.value - 1) * customerPageSize.value + index + 1
  },
  { title: '客户名称', dataIndex: 'name', width: 160, fixed: 'left' as const },
  { title: '手机号', dataIndex: 'phone', width: 130 },
  { title: '客户等级', dataIndex: 'level', width: 90 },
  { title: '客户类型', dataIndex: 'customerType', width: 90 },
  { title: '客户来源', dataIndex: 'source', width: 100 },
  { title: '意向等级', dataIndex: 'intentionLevel', width: 90 },
  { title: '状态', dataIndex: 'status', width: 80 },
  { title: '标签', dataIndex: 'tags', width: 140, ellipsis: true },
  { title: '地址', dataIndex: 'city', width: 100 },
  { title: '操作', dataIndex: 'actions', width: 220, fixed: 'right' as const }
];

const detailOrderColumns = [
  { title: '订单号', dataIndex: 'orderNo', ellipsis: true },
  { title: '订单类型', dataIndex: 'orderType' },
  { title: '状态', dataIndex: 'status' },
  { title: '金额', dataIndex: 'totalAmount' },
  { title: '币种', dataIndex: 'currency' },
  { title: '创建时间', dataIndex: 'createdAt' },
  { title: '操作', dataIndex: 'actions' }
];

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

const customerMap = computed(() => Object.fromEntries(customers.value.map((item) => [item.id, item.name])));

const decoratedTravelers = computed(() =>
  travelers.value.map((item) => ({
    ...item,
    customerName: customerMap.value[item.customerId] || '-',
    age: calcAge(item.birthday)
  }))
);

const visibleCustomers = computed(() => {
  const normalizedKeyword = keyword.value.trim().toLowerCase();
  const currentUserId = auth.profile?.userId;
  const isPublicPool = (item: any) => item.status === 'INACTIVE' || item.status === 'BLACKLIST';

  let rows = customers.value.filter((item) => (moduleTab.value === 'pool' ? isPublicPool(item) : !isPublicPool(item)));
  if (moduleTab.value === 'customer') {
    if (listFilter.value === 'mine' && currentUserId) {
      rows = rows.filter((item) => item.ownerUserId === currentUserId);
    } else if (listFilter.value === 'cooperate' && currentUserId) {
      rows = rows.filter((item) => item.ownerUserId !== currentUserId);
    }
  }

  if (!normalizedKeyword) {
    return rows;
  }
  return rows.filter((item) =>
    [item.name, item.phone, item.tags, item.city].filter(Boolean).some((value) => String(value).toLowerCase().includes(normalizedKeyword))
  );
});

const visibleContacts = computed(() => {
  const normalizedKeyword = keyword.value.trim().toLowerCase();
  if (!normalizedKeyword) {
    return decoratedTravelers.value;
  }
  return decoratedTravelers.value.filter((item) =>
    [item.name, item.phone, item.customerName, item.idNo].filter(Boolean).some((value) => String(value).toLowerCase().includes(normalizedKeyword))
  );
});

const detailContacts = computed(() => {
  const normalizedKeyword = detailContactKeyword.value.trim().toLowerCase();
  const rows = decoratedTravelers.value.filter((item) => item.customerId === selectedCustomer.value?.id);
  if (!normalizedKeyword) {
    return rows;
  }
  return rows.filter((item) =>
    [item.name, item.phone, item.idNo].filter(Boolean).some((value) => String(value).toLowerCase().includes(normalizedKeyword))
  );
});

const detailOrders = computed(() => {
  return detailOrderRows.value;
});

const mapCustomerType = (value?: string) => ({ PERSONAL: '个人', ENTERPRISE: '企业' }[value || ''] || value || '-');
const mapSource = (value?: string) => ({ MANUAL: '手工录入', REFERRAL: '老客推荐', ONLINE: '线上咨询', PHONE: '电话咨询', WALK_IN: '到店' }[value || ''] || value || '-');
const mapLevel = (value?: string) => ({ A: '重要客户', B: '普通客户', C: '观察客户' }[value || ''] || value || '-');
const mapIntention = (value?: string) => ({ HIGH: '高', MEDIUM: '中', LOW: '低' }[value || ''] || value || '-');
const mapStatus = (value?: string) => ({ ACTIVE: '正常', INACTIVE: '沉默', BLACKLIST: '黑名单' }[value || ''] || value || '-');
const mapOrderStatus = (value?: string) =>
  ({
    DRAFT: '草稿',
    PENDING_APPROVAL: '待审批',
    APPROVED: '已审批',
    REJECTED: '已驳回',
    IN_TRAVEL: '履约中',
    TRAVEL_FINISHED: '已回团',
    SETTLING: '结算中',
    COMPLETED: '已完结',
    CANCELED: '已取消'
  }[value || ''] || value || '-');

const calcAge = (birthday?: string) => {
  if (!birthday) return null;
  const [year, month, day] = String(birthday).split('-').map((item) => Number(item));
  if (!year || !month || !day) return null;
  const birth = new Date(year, month - 1, day);
  if (Number.isNaN(birth.getTime())) return null;
  const today = new Date();
  let age = today.getFullYear() - year;
  const hasBirthdayPassed = (today.getMonth() + 1 > month)
    || (today.getMonth() + 1 === month && today.getDate() >= day);
  if (!hasBirthdayPassed) {
    age -= 1;
  }
  return age < 0 ? null : age;
};

const formatDateTime = (value?: string) => {
  if (!value) return '-';
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return '-';
  const pad = (n: number) => String(n).padStart(2, '0');
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}`;
};

const loadCustomers = async () => {
  const tabParam = moduleTab.value === 'pool' ? 'pool' : 'customer';
  const ownerScopeParam = moduleTab.value === 'customer' ? listFilter.value : undefined;
  const { data } = await customerApi.customersPage({
    page: customerPage.value,
    size: customerPageSize.value,
    keyword: keyword.value.trim() || undefined,
    tab: tabParam,
    ownerScope: ownerScopeParam
  });
  customers.value = data.data?.items || [];
  customerTotal.value = Number(data.data?.total || 0);
  if (selectedCustomer.value) {
    selectedCustomer.value = customers.value.find((item) => item.id === selectedCustomer.value?.id) || null;
    if (!selectedCustomer.value) {
      detailMode.value = false;
    }
  }
};

const loadTravelers = async () => {
  const { data } = await customerApi.travelerList();
  travelers.value = data.data || [];
};

const loadUsers = async () => {
  const { data } = await customerApi.ownerOptions();
  users.value = data.data || [];
};

const loadDetailOrders = async () => {
  if (!selectedCustomer.value?.id) {
    detailOrderRows.value = [];
    detailOrderTotal.value = 0;
    return;
  }
  const { data } = await orderApi.page({
    page: detailOrderPage.value,
    size: detailOrderPageSize.value,
    customerId: selectedCustomer.value.id,
    keyword: detailOrderKeyword.value.trim() || undefined
  });
  detailOrderRows.value = data.data?.items || [];
  detailOrderTotal.value = Number(data.data?.total || 0);
};

const onSearch = () => {
  if (moduleTab.value === 'contact') {
    // 前端过滤，直接依赖计算属性变化，但如果未来要请求可以加在这里
  } else {
    customerPage.value = 1;
    void loadCustomers();
  }
};

const onCustomerTableChange = (pagination: { current?: number; pageSize?: number }) => {
  customerPage.value = pagination.current || 1;
  customerPageSize.value = pagination.pageSize || 10;
  void loadCustomers();
};

const onDetailOrderTableChange = (pagination: { current?: number; pageSize?: number }) => {
  detailOrderPage.value = pagination.current || 1;
  detailOrderPageSize.value = pagination.pageSize || 8;
  void loadDetailOrders();
};

const buildCustomerPayload = (base: any, overrides: Record<string, any> = {}) => ({
  name: base.name || '',
  phone: base.phone || '',
  customerType: base.customerType || 'PERSONAL',
  source: base.source || 'MANUAL',
  intentionLevel: base.intentionLevel || 'MEDIUM',
  status: base.status || 'ACTIVE',
  level: base.level || 'B',
  ownerUserId: base.ownerUserId,
  wechat: base.wechat || '',
  email: base.email || '',
  city: base.city || '',
  tags: base.tags || '',
  remark: base.remark || '',
  ...overrides
});

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

const openCustomerDetail = (record: any, targetTab: DetailTab = 'contact') => {
  selectedCustomer.value = record;
  detailTab.value = targetTab;
  detailOrderPage.value = 1;
  detailOrderPageSize.value = 8;
  if (targetTab === 'order') {
    void loadDetailOrders();
  }
  basicInfoCollapsed.value = false;
  detailMode.value = true;
};

const backToList = () => {
  detailMode.value = false;
  detailTab.value = 'contact';
  basicInfoCollapsed.value = false;
  detailContactKeyword.value = '';
  detailOrderKeyword.value = '';
  detailOrderRows.value = [];
  detailOrderTotal.value = 0;
};

const saveCustomer = async () => {
  if (!customerForm.name.trim() || !customerForm.phone.trim()) {
    notifyError(new Error('请填写客户名称和手机号'));
    return;
  }
  saving.value = true;
  try {
    const payload = buildCustomerPayload(customerForm);
    if (customerForm.id) {
      await customerApi.updateCustomer(customerForm.id, payload);
      notifySuccess('客户更新成功');
    } else {
      await customerApi.createCustomer(payload);
      notifySuccess('客户创建成功');
    }
    customerModal.value = false;
    await loadCustomers();
    await loadTravelers();
  } catch (error) {
    notifyError(error);
  } finally {
    saving.value = false;
  }
};

const removeCustomer = async (record?: any) => {
  if (!record?.id) return;
  try {
    await customerApi.deleteCustomer(record.id);
    notifySuccess('客户删除成功');
    if (selectedCustomer.value?.id === record.id) {
      backToList();
      selectedCustomer.value = null;
    }
    await loadCustomers();
    await loadTravelers();
  } catch (error) {
    notifyError(error);
  }
};

const moveCustomerToPool = async (record?: any) => {
  if (!record?.id) return;
  try {
    await customerApi.updateCustomer(record.id, buildCustomerPayload(record, { status: 'INACTIVE' }));
    notifySuccess('客户已移入公海');
    await loadCustomers();
  } catch (error) {
    notifyError(error);
  }
};

const openTransfer = (record?: any) => {
  if (!record?.id) return;
  transferCustomer.value = record;
  transferOwnerUserId.value = record.ownerUserId;
  transferOpen.value = true;
};

const confirmTransfer = async () => {
  if (!transferCustomer.value?.id || !transferOwnerUserId.value) {
    notifyError(new Error('请选择转移后的负责人'));
    return;
  }
  saving.value = true;
  try {
    await customerApi.updateCustomer(
      transferCustomer.value.id,
      buildCustomerPayload(transferCustomer.value, { ownerUserId: transferOwnerUserId.value, status: 'ACTIVE' })
    );
    notifySuccess('客户转移成功');
    transferOpen.value = false;
    await loadCustomers();
  } catch (error) {
    notifyError(error);
  } finally {
    saving.value = false;
  }
};

const onMoreAction = async (action: string, record: any) => {
  if (action === 'movePool') {
    await moveCustomerToPool(record);
    return;
  }
  if (action === 'delete') {
    await removeCustomer(record);
  }
};

const openTraveler = (record?: any) => {
  selectedTravelerRecord.value = record || null;
  travelerModal.value = true;
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
    await Promise.all([loadCustomers(), loadTravelers()]);
  } catch (error) {
    notifyError(error);
  }
  return false;
};

watch([moduleTab, listFilter], async () => {
  customerPage.value = 1;
  if (moduleTab.value === 'contact') {
    await loadTravelers();
  } else {
    await loadCustomers();
  }
});

watch(keyword, async () => {
  if (moduleTab.value === 'contact') {
    return;
  }
  customerPage.value = 1;
  await loadCustomers();
});

watch(detailTab, async (tab) => {
  if (tab !== 'order' || !detailMode.value) {
    return;
  }
  detailOrderPage.value = 1;
  await loadDetailOrders();
});

watch(detailOrderKeyword, async () => {
  if (!detailMode.value || detailTab.value !== 'order') {
    return;
  }
  detailOrderPage.value = 1;
  await loadDetailOrders();
});

const exportCustomerCsv = () => {
  const head = ['客户名称', '手机号', '客户等级', '客户类型', '客户来源', '客户标签', '城市', '负责人'];
  const rows = customers.value.map((item) => [
    item.name || '',
    item.phone || '',
    mapLevel(item.level),
    mapCustomerType(item.customerType),
    mapSource(item.source),
    item.tags || '',
    item.city || '',
    item.ownerUserName || ''
  ]);
  const escapeCell = (value: string) => `"${String(value).replace(/"/g, '""')}"`;
  const csv = [head, ...rows].map((row) => row.map((cell) => escapeCell(String(cell))).join(',')).join('\n');
  const blob = new Blob([`\uFEFF${csv}`], { type: 'text/csv;charset=utf-8;' });
  const url = URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = url;
  link.download = `customers-${new Date().toISOString().slice(0, 10)}.csv`;
  link.click();
  URL.revokeObjectURL(url);
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
.customer-manage {
  display: grid;
  gap: 10px;
  min-width: 0;
}

.customer-shell {
  min-width: 0;
}

.customer-shell :deep(.ant-card-body) {
  display: grid;
  gap: 10px;
  min-width: 0;
}

.module-tabs {
  min-width: 0;
}

.module-tabs :deep(.ant-tabs-nav) {
  margin-bottom: 0;
}

.list-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.toolbar-row {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.list-filter-bar {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.customer-table {
  margin-top: 2px;
  min-width: 0;
}

.customer-table :deep(.ant-table-wrapper) {
  min-width: 0;
}

.customer-table :deep(.ant-spin-nested-loading),
.customer-table :deep(.ant-spin-container),
.customer-table :deep(.ant-table),
.customer-table :deep(.ant-table-container),
.customer-table :deep(.ant-table-content) {
  min-width: 0;
}

.customer-table :deep(.ant-table-content) {
  overflow-x: auto;
  overflow-y: hidden;
}

.customer-table :deep(.ant-table-content)::-webkit-scrollbar {
  height: 6px;
}

.customer-table :deep(.ant-table-content)::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.customer-table :deep(.ant-table-content)::-webkit-scrollbar-thumb:hover {
  background: #999;
}

.customer-table :deep(.ant-table-content)::-webkit-scrollbar-track {
  background: #f0f0f0;
  border-radius: 3px;
}

.customer-table :deep(.ant-table-cell) {
  white-space: nowrap;
}

.customer-table :deep(.ant-table-pagination.ant-pagination) {
  margin-bottom: 0;
}

.list-footer {
  color: #6b7280;
  font-size: 13px;
  padding-top: 2px;
}

.name-link {
  color: #0891b2;
  font-weight: 600;
}

.danger-text {
  color: #db4b4b;
}

</style>
