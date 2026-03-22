
<template>
  <div class="customer-manage">
    <a-card v-if="!detailMode" class="section-card customer-shell" :bordered="false">
      <a-tabs v-model:activeKey="moduleTab" class="module-tabs">
        <a-tab-pane key="customer" tab="客户" />
        <a-tab-pane key="contact" tab="联系人" />
        <a-tab-pane key="pool" tab="公海" />
      </a-tabs>

      <div class="list-toolbar">
        <div class="toolbar-row">
          <a-button v-if="moduleTab !== 'contact'" type="primary" @click="openCustomer()">新建客户</a-button>
          <a-button v-if="moduleTab === 'contact'" type="primary" @click="openTraveler()">新建联系人</a-button>
          <a-upload v-if="moduleTab === 'customer'" :show-upload-list="false" :before-upload="beforeImport">
            <a-button>导入客户</a-button>
          </a-upload>
          <a-button v-if="moduleTab !== 'contact'" @click="exportCustomerCsv">导出所有页</a-button>
        </div>

        <div class="toolbar-row">
          <a-input-search
            v-model:value="keyword"
            :placeholder="moduleTab === 'contact' ? '通过联系人关键词搜索' : '通过客户名称搜索'"
            style="width: 280px"
          />
          <a-select v-if="moduleTab !== 'contact'" v-model:value="viewLabel" style="width: 170px">
            <a-select-option value="所有客户">所有客户</a-select-option>
            <a-select-option value="重点客户">重点客户</a-select-option>
            <a-select-option value="普通客户">普通客户</a-select-option>
          </a-select>
        </div>
      </div>

      <div v-if="moduleTab === 'customer'" class="list-filter-bar">
        <a-button :type="listFilter === 'all' ? 'primary' : 'default'" ghost @click="listFilter = 'all'">所有客户</a-button>
        <a-button :type="listFilter === 'mine' ? 'primary' : 'default'" ghost @click="listFilter = 'mine'">我的客户</a-button>
        <a-button :type="listFilter === 'cooperate' ? 'primary' : 'default'" ghost @click="listFilter = 'cooperate'">协作客户</a-button>
        <a-button>+ 新建视图</a-button>
      </div>

      <a-table
        v-if="moduleTab !== 'contact'"
        class="customer-table"
        :data-source="visibleCustomers"
        :columns="customerColumns"
        row-key="id"
        :pagination="false"
        :scroll="{ x: 1280 }"
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
              <a-button type="link" @click="openCustomerDetail(record, 'contact')">跟进</a-button>
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

      <div class="list-footer">共 {{ moduleTab === 'contact' ? visibleContacts.length : visibleCustomers.length }} 条</div>
    </a-card>
    <template v-else>
      <div class="detail-header">
        <div class="header-left">
          <a-button type="text" @click="backToList">返回</a-button>
          <h2 class="detail-title">{{ selectedCustomer?.name || '客户详情' }}</h2>
        </div>
        <a-space>
          <a-button @click="basicInfoCollapsed = !basicInfoCollapsed">
            {{ basicInfoCollapsed ? '展开基本信息' : '收起基本信息' }}
          </a-button>
          <a-button @click="openCustomer(selectedCustomer || undefined)">编辑</a-button>
          <a-button @click="openTransfer(selectedCustomer || undefined)">转移</a-button>
          <a-button @click="moveCustomerToPool(selectedCustomer)">移入公海</a-button>
          <a-popconfirm title="确认删除该客户？" @confirm="removeCustomer(selectedCustomer)">
            <a-button danger>删除</a-button>
          </a-popconfirm>
        </a-space>
      </div>

      <div class="detail-layout" :class="{ collapsed: basicInfoCollapsed }">
        <a-card v-show="!basicInfoCollapsed" class="section-card detail-side" :bordered="false">
          <div class="side-block">
            <h3>基本信息</h3>
            <div class="info-row"><span>客户名称</span><strong>{{ selectedCustomer?.name || '-' }}</strong></div>
            <div class="info-row"><span>手机号</span><strong>{{ selectedCustomer?.phone || '-' }}</strong></div>
            <div class="info-row"><span>客户等级</span><strong>{{ mapLevel(selectedCustomer?.level) }}</strong></div>
            <div class="info-row"><span>客户类型</span><strong>{{ mapCustomerType(selectedCustomer?.customerType) }}</strong></div>
            <div class="info-row"><span>客户来源</span><strong>{{ mapSource(selectedCustomer?.source) }}</strong></div>
            <div class="info-row"><span>意向等级</span><strong>{{ mapIntention(selectedCustomer?.intentionLevel) }}</strong></div>
            <div class="info-row"><span>客户状态</span><strong>{{ mapStatus(selectedCustomer?.status) }}</strong></div>
            <div class="info-row"><span>客户标签</span><strong>{{ selectedCustomer?.tags || '-' }}</strong></div>
          </div>

          <div class="side-block">
            <h3>地址信息</h3>
            <div class="info-row"><span>地区</span><strong>{{ selectedCustomer?.city || '-' }}</strong></div>
          </div>

          <div class="side-block">
            <h3>负责人信息</h3>
            <div class="info-row"><span>负责人</span><strong>{{ selectedCustomer?.ownerUserName || '-' }}</strong></div>
            <div class="info-row"><span>部门</span><strong>{{ selectedCustomer?.ownerDeptName || '-' }}</strong></div>
            <div class="info-row"><span>最新跟进人员</span><strong>{{ selectedCustomer?.updatedBy || '-' }}</strong></div>
            <div class="info-row"><span>最新跟进时间</span><strong>{{ formatDateTime(selectedCustomer?.updatedAt) }}</strong></div>
            <div class="info-row"><span>创建人</span><strong>{{ selectedCustomer?.createdBy || '-' }}</strong></div>
            <div class="info-row"><span>创建时间</span><strong>{{ formatDateTime(selectedCustomer?.createdAt) }}</strong></div>
            <div class="info-row"><span>更新人</span><strong>{{ selectedCustomer?.updatedBy || '-' }}</strong></div>
            <div class="info-row"><span>更新时间</span><strong>{{ formatDateTime(selectedCustomer?.updatedAt) }}</strong></div>
          </div>
        </a-card>

        <a-card class="section-card detail-main" :bordered="false">
          <a-tabs v-model:activeKey="detailTab">
            <a-tab-pane key="contact" tab="联系人信息" />
            <a-tab-pane key="order" tab="订单信息" />
          </a-tabs>

          <div v-if="detailTab === 'contact'" class="detail-pane">
            <div class="list-toolbar">
              <a-button type="primary" @click="openTraveler()">新增联系人</a-button>
              <a-input-search v-model:value="detailContactKeyword" placeholder="搜索联系人" style="width: 240px" />
            </div>
            <TravelerTable :items="detailContacts" :scroll-x="false" @edit="openTraveler" @remove="removeTraveler" />
          </div>

          <div v-else class="detail-pane">
            <div class="list-toolbar">
              <a-input-search v-model:value="detailOrderKeyword" placeholder="按订单号/类型/状态搜索" style="width: 260px" />
            </div>
            <a-table :columns="detailOrderColumns" :data-source="detailOrders" row-key="id" :pagination="{ pageSize: 8 }">
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
                  <a-button type="link" @click="router.push(`/orders/${record.id}`)">查看</a-button>
                </template>
              </template>
            </a-table>
          </div>
        </a-card>
      </div>
    </template>

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

    <a-drawer v-model:open="travelerModal" :title="travelerForm.id ? '编辑联系人' : '新增联系人'" placement="right" :width="720">
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
          <a-form-item label="出生日期（身份证自动识别）" required>
            <a-input :value="travelerResolvedBirthday || '-'" disabled />
          </a-form-item>
          <a-form-item label="年龄">
            <a-input :value="travelerAgeText" disabled />
          </a-form-item>
        </div>
        <div class="grid-2">
          <a-form-item label="性别">
            <a-select v-model:value="travelerForm.gender" allow-clear>
              <a-select-option value="MALE">男</a-select-option>
              <a-select-option value="FEMALE">女</a-select-option>
            </a-select>
          </a-form-item>
          <a-form-item label="民族">
            <a-select v-model:value="travelerForm.ethnicity" show-search allow-clear placeholder="请选择民族">
              <a-select-option v-for="item in ethnicityOptions" :key="item" :value="item">{{ item }}</a-select-option>
            </a-select>
          </a-form-item>
        </div>
        <div class="grid-2">
          <a-form-item label="国籍">
            <a-input v-model:value="travelerForm.nationality" />
          </a-form-item>
          <a-form-item label="联系地址">
            <a-input v-model:value="travelerForm.address" />
          </a-form-item>
        </div>
        <div class="grid-2">
          <a-form-item label="紧急联系人">
            <a-input v-model:value="travelerForm.emergencyContact" />
          </a-form-item>
          <a-form-item label="紧急联系电话">
            <a-input v-model:value="travelerForm.emergencyPhone" />
          </a-form-item>
        </div>
        <a-form-item label="备注偏好">
          <a-textarea v-model:value="travelerForm.preferences" :rows="2" />
        </a-form-item>

        <a-divider orientation="left">证件信息</a-divider>
        <div class="id-required-tip">身份证为必填，系统将根据身份证自动识别出生日期与年龄</div>
        <div class="toolbar-row" style="margin-bottom: 8px">
          <a-button @click="addTravelerDocument">新增证件</a-button>
        </div>
        <a-empty v-if="!travelerForm.documents.length" description="请至少添加一个证件信息" />
        <div v-else class="document-list">
          <div v-for="(doc, index) in travelerForm.documents" :key="index" class="document-row">
            <a-select v-model:value="doc.docType" placeholder="证件类型">
              <a-select-option v-for="item in idTypeOptions" :key="item.value" :value="item.value">
                {{ item.label }}
              </a-select-option>
            </a-select>
            <a-input v-model:value="doc.docNo" placeholder="证件号码" />
            <a-button danger @click="removeTravelerDocument(index)" :disabled="travelerForm.documents.length === 1">删除</a-button>
          </div>
        </div>
      </a-form>
    </a-drawer>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import TravelerTable from '../components/customer/TravelerTable.vue';
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

const keyword = ref('');
const viewLabel = ref('所有客户');
const detailContactKeyword = ref('');
const detailOrderKeyword = ref('');

const customers = ref<any[]>([]);
const travelers = ref<any[]>([]);
const users = ref<any[]>([]);
const orders = ref<any[]>([]);
const selectedCustomer = ref<any | null>(null);
const transferCustomer = ref<any | null>(null);
const transferOwnerUserId = ref<number | undefined>();

const customerColumns = [
  { title: '序号', dataIndex: 'seq', width: 74, customRender: ({ index }: any) => index + 1 },
  { title: '客户名称', dataIndex: 'name', width: 180 },
  { title: '手机号', dataIndex: 'phone', width: 140 },
  { title: '客户等级', dataIndex: 'level', width: 120 },
  { title: '客户类型', dataIndex: 'customerType', width: 120 },
  { title: '客户来源', dataIndex: 'source', width: 140 },
  { title: '意向等级', dataIndex: 'intentionLevel', width: 120 },
  { title: '状态', dataIndex: 'status', width: 110 },
  { title: '客户标签', dataIndex: 'tags', width: 200 },
  { title: '地址', dataIndex: 'city', width: 140 },
  { title: '操作', dataIndex: 'actions', width: 250, fixed: 'right' as const }
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

const idTypeOptions = [
  { label: '身份证', value: 'ID_CARD' },
  { label: '护照', value: 'PASSPORT' },
  { label: '港澳通行证', value: 'HK_MACAO_PASS' },
  { label: '台胞证', value: 'TAIWAN_PASS' },
  { label: '军官证', value: 'MILITARY_ID' },
  { label: '其他', value: 'OTHER' }
];

const ethnicityOptions = [
  '汉族', '蒙古族', '回族', '藏族', '维吾尔族', '苗族', '彝族', '壮族', '布依族', '朝鲜族', '满族', '侗族',
  '瑶族', '白族', '土家族', '哈尼族', '哈萨克族', '傣族', '黎族', '傈僳族', '佤族', '畲族', '高山族', '拉祜族',
  '水族', '东乡族', '纳西族', '景颇族', '柯尔克孜族', '土族', '达斡尔族', '仫佬族', '羌族', '布朗族', '撒拉族',
  '毛南族', '仡佬族', '锡伯族', '阿昌族', '普米族', '塔吉克族', '怒族', '乌孜别克族', '俄罗斯族', '鄂温克族',
  '德昂族', '保安族', '裕固族', '京族', '塔塔尔族', '独龙族', '鄂伦春族', '赫哲族', '门巴族', '珞巴族', '基诺族'
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

const travelerForm = reactive({
  id: undefined as number | undefined,
  customerId: undefined as number | undefined,
  name: '',
  birthday: '',
  gender: undefined as string | undefined,
  ethnicity: '',
  nationality: '中国',
  address: '',
  phone: '',
  emergencyContact: '',
  emergencyPhone: '',
  preferences: '',
  documents: [{ docType: 'ID_CARD', docNo: '' }] as Array<{ docType: string; docNo: string }>
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
  const normalizedKeyword = detailOrderKeyword.value.trim().toLowerCase();
  const rows = orders.value.filter((item) => item.customerId === selectedCustomer.value?.id);
  if (!normalizedKeyword) {
    return rows;
  }
  return rows.filter((item) =>
    [item.orderNo, item.orderType, item.status].filter(Boolean).some((value) => String(value).toLowerCase().includes(normalizedKeyword))
  );
});

const travelerResolvedBirthday = computed(() => {
  const idCardDoc = travelerForm.documents.find((item) => item.docType === 'ID_CARD' && item.docNo.trim());
  const parsedBirthday = parseBirthdayFromIdCard(idCardDoc?.docNo);
  return parsedBirthday || travelerForm.birthday || '';
});

const travelerAgeText = computed(() => {
  const age = calcAge(travelerResolvedBirthday.value);
  return age === null ? '-' : `${age}岁`;
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

const normalizeDocType = (rawType?: string) => {
  const value = String(rawType || '').trim();
  const upper = value.toUpperCase();
  if (upper === 'ID_CARD' || value.includes('身份证')) return 'ID_CARD';
  if (upper === 'PASSPORT' || value.includes('护照')) return 'PASSPORT';
  if (upper === 'HK_MACAO_PASS' || value.includes('港澳')) return 'HK_MACAO_PASS';
  if (upper === 'TAIWAN_PASS' || value.includes('台胞') || value.includes('台湾')) return 'TAIWAN_PASS';
  if (upper === 'MILITARY_ID' || value.includes('军官')) return 'MILITARY_ID';
  return upper || 'OTHER';
};

const parseBirthdayFromIdCard = (rawIdNo?: string) => {
  if (!rawIdNo) return '';
  const idNo = String(rawIdNo).trim().toUpperCase();
  let year = '';
  let month = '';
  let day = '';
  if (/^\d{17}[\dX]$/.test(idNo)) {
    year = idNo.slice(6, 10);
    month = idNo.slice(10, 12);
    day = idNo.slice(12, 14);
  } else if (/^\d{15}$/.test(idNo)) {
    year = `19${idNo.slice(6, 8)}`;
    month = idNo.slice(8, 10);
    day = idNo.slice(10, 12);
  } else {
    return '';
  }
  const birthday = `${year}-${month}-${day}`;
  const date = new Date(`${birthday}T00:00:00`);
  if (Number.isNaN(date.getTime())) return '';
  if (date.getFullYear() !== Number(year) || date.getMonth() + 1 !== Number(month) || date.getDate() !== Number(day)) {
    return '';
  }
  return birthday;
};

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
  const { data } = await customerApi.customers();
  customers.value = data.data || [];
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

const loadOrders = async () => {
  const { data } = await orderApi.list();
  orders.value = data.data || [];
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
  basicInfoCollapsed.value = false;
  detailMode.value = true;
};

const backToList = () => {
  detailMode.value = false;
  detailTab.value = 'contact';
  basicInfoCollapsed.value = false;
  detailContactKeyword.value = '';
  detailOrderKeyword.value = '';
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
  travelerForm.id = record?.id;
  travelerForm.customerId = record?.customerId || selectedCustomer.value?.id;
  travelerForm.name = record?.name || '';
  travelerForm.birthday = record?.birthday || '';
  travelerForm.gender = record?.gender || undefined;
  travelerForm.ethnicity = record?.ethnicity || '';
  travelerForm.nationality = record?.nationality || '中国';
  travelerForm.address = record?.address || '';
  travelerForm.phone = record?.phone || '';
  travelerForm.emergencyContact = record?.emergencyContact || '';
  travelerForm.emergencyPhone = record?.emergencyPhone || '';
  travelerForm.preferences = record?.preferences || '';
  const documents = Array.isArray(record?.documents) && record.documents.length
    ? record.documents.map((doc: any) => ({
      docType: normalizeDocType(doc.docType || 'ID_CARD'),
      docNo: doc.docNo || ''
    }))
    : [{
      docType: normalizeDocType(record?.idType || 'ID_CARD'),
      docNo: record?.idNo || ''
    }];
  travelerForm.documents = documents;
  travelerModal.value = true;
};

const saveTraveler = async () => {
  if (!travelerForm.customerId || !travelerForm.name.trim()) {
    notifyError(new Error('请填写联系人姓名并选择所属客户'));
    return;
  }
  const idCardDocument = travelerForm.documents.find((item) => item.docType === 'ID_CARD' && item.docNo.trim());
  if (!idCardDocument) {
    notifyError(new Error('身份证信息为必填'));
    return;
  }
  const birthdayFromIdCard = parseBirthdayFromIdCard(idCardDocument.docNo);
  if (!birthdayFromIdCard) {
    notifyError(new Error('身份证号格式不正确，无法识别出生日期'));
    return;
  }

  const validDocuments = travelerForm.documents
    .map((item) => ({ docType: normalizeDocType(item.docType), docNo: item.docNo.trim() }))
    .filter((item) => item.docType && item.docNo);

  if (!validDocuments.length) {
    notifyError(new Error('请至少填写一个有效证件'));
    return;
  }

  saving.value = true;
  try {
    const payload = {
      name: travelerForm.name,
      birthday: birthdayFromIdCard,
      gender: travelerForm.gender,
      ethnicity: travelerForm.ethnicity,
      nationality: travelerForm.nationality,
      address: travelerForm.address,
      phone: travelerForm.phone,
      emergencyContact: travelerForm.emergencyContact,
      emergencyPhone: travelerForm.emergencyPhone,
      preferences: travelerForm.preferences,
      documents: validDocuments
    };
    if (travelerForm.id) {
      await customerApi.updateTraveler(travelerForm.id, payload);
      notifySuccess('联系人更新成功');
    } else {
      await customerApi.createTraveler(travelerForm.customerId, payload);
      notifySuccess('联系人新增成功');
    }
    travelerModal.value = false;
    await loadTravelers();
  } catch (error) {
    notifyError(error);
  } finally {
    saving.value = false;
  }
};

const addTravelerDocument = () => {
  travelerForm.documents.push({ docType: 'OTHER', docNo: '' });
};

const removeTravelerDocument = (index: number) => {
  if (travelerForm.documents.length === 1) return;
  travelerForm.documents.splice(index, 1);
};

const removeTraveler = async (record: any) => {
  try {
    await customerApi.deleteTraveler(record.id);
    notifySuccess('联系人删除成功');
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
    await Promise.all([loadCustomers(), loadTravelers(), loadUsers(), loadOrders()]);
  } catch (error) {
    notifyError(error);
  }
});
</script>

<style scoped>
.customer-manage {
  display: grid;
  gap: 10px;
  overflow-x: hidden;
}

.customer-shell :deep(.ant-card-body) {
  display: grid;
  gap: 10px;
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

.list-filter-bar {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.customer-table {
  margin-top: 2px;
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
  font-size: 21px;
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

.detail-main :deep(.ant-card-body) {
  height: 100%;
  display: grid;
  grid-template-rows: auto minmax(0, 1fr);
  gap: 10px;
}

.detail-main :deep(.ant-tabs-nav) {
  margin: 0;
}

.detail-pane {
  min-height: 420px;
  border: 1px solid #eef2f7;
  border-radius: 10px;
  padding: 10px;
  display: grid;
  align-content: start;
  gap: 12px;
  overflow: hidden;
}

.id-required-tip {
  margin-bottom: 8px;
  color: #cf1322;
}

.document-list {
  display: grid;
  gap: 8px;
}

.document-row {
  display: grid;
  grid-template-columns: 180px 1fr 80px;
  gap: 10px;
}

@media (max-width: 1360px) {
  .detail-layout {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 1200px) {
  .document-row {
    grid-template-columns: 1fr;
  }
}
</style>
