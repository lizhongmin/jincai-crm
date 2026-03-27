<template>
  <div class="route-page">
    <a-card class="section-card" :bordered="false">
      <div class="toolbar-row">
        <a-input-search v-model:value="routeKeyword" placeholder="按线路名称、出发地、目的地筛选" style="width: 320px" />
        <a-button type="primary" @click="openRoute()">新增线路</a-button>
      </div>
      <route-table
        style="margin-top: 10px"
        :items="routeRows"
        :pagination="false"
        @view="openRouteDetail"
        @edit="openRoute"
        @remove="removeRoute"
      />
      <a-pagination
        style="margin-top: 12px; text-align: right"
        :current="routePage"
        :page-size="routePageSize"
        :total="routeTotal"
        show-size-changer
        :page-size-options="['10', '20', '50']"
        @change="onRoutePageChange"
        @showSizeChange="onRoutePageChange"
      />
    </a-card>

    <!-- 线路详情 Drawer -->
    <a-drawer v-model:open="routeDetailOpen" width="720" title="线路详情" placement="right">
      <a-empty v-if="!activeRoute" description="未选择线路" />
      <template v-else>
        <div class="toolbar-row" style="margin-bottom: 8px">
          <a-button type="primary" @click="openRoutePolicy">编辑下单策略</a-button>
        </div>
        <a-descriptions :column="2" bordered size="small">
          <a-descriptions-item label="编码">{{ activeRoute.code }}</a-descriptions-item>
          <a-descriptions-item label="线路名称">{{ activeRoute.name }}</a-descriptions-item>
          <a-descriptions-item label="产品分类">{{ activeRoute.category || '-' }}</a-descriptions-item>
          <a-descriptions-item label="出发地">{{ activeRoute.departureCity || '-' }}</a-descriptions-item>
          <a-descriptions-item label="目的地">{{ activeRoute.destinationCity || '-' }}</a-descriptions-item>
          <a-descriptions-item label="行程天数">{{ activeRoute.durationDays || '-' }}天{{ activeRoute.durationNights ?? '-' }}晚</a-descriptions-item>
          <a-descriptions-item label="交通方式">{{ activeRoute.transportation || '-' }}</a-descriptions-item>
          <a-descriptions-item label="酒店标准">{{ activeRoute.hotelStandard || '-' }}</a-descriptions-item>
        </a-descriptions>
        <a-divider orientation="left">线路亮点</a-divider>
        <div>{{ activeRoute.highlights || '-' }}</div>
        <a-divider orientation="left">费用包含</a-divider>
        <div>{{ activeRoute.feeIncludes || '-' }}</div>
        <a-divider orientation="left">费用不含</a-divider>
        <div>{{ activeRoute.feeExcludes || '-' }}</div>
        <a-divider orientation="left">预订须知</a-divider>
        <div>{{ activeRoute.bookingNotice || '-' }}</div>
        <a-divider orientation="left">补充描述</a-divider>
        <div>{{ activeRoute.description || '-' }}</div>
        <a-divider orientation="left">下单策略</a-divider>
        <a-descriptions :column="2" bordered size="small">
          <a-descriptions-item label="签约要求">{{ routePolicyModel.contractRequired ? '必须签约' : '无需签约' }}</a-descriptions-item>
          <a-descriptions-item label="锁位策略">{{ enumLabel(LOCK_POLICY_LABEL_MAP, routePolicyModel.lockPolicy) }}</a-descriptions-item>
          <a-descriptions-item label="收款策略">{{ enumLabel(PAYMENT_POLICY_LABEL_MAP, routePolicyModel.paymentPolicy) }}</a-descriptions-item>
          <a-descriptions-item label="定金规则">{{ enumLabel(DEPOSIT_TYPE_LABEL_MAP, routePolicyModel.depositType) }} / {{ routePolicyModel.depositValue ?? '-' }}</a-descriptions-item>
          <a-descriptions-item label="定金时限">{{ routePolicyModel.depositDeadlineDays ?? '-' }} 天</a-descriptions-item>
          <a-descriptions-item label="尾款时限">{{ routePolicyModel.balanceDeadlineDays ?? '-' }} 天</a-descriptions-item>
          <a-descriptions-item label="自动取消">{{ routePolicyModel.autoCancelHours ?? '-' }} 小时</a-descriptions-item>
        </a-descriptions>
      </template>
    </a-drawer>

    <!-- 线路下单策略 Drawer -->
    <a-drawer v-model:open="routePolicyOpen" title="线路下单策略" placement="right" width="560">
      <template #extra>
        <a-space>
          <a-button @click="routePolicyOpen = false">取消</a-button>
          <a-button type="primary" :loading="saving" @click="saveRoutePolicy">保存</a-button>
        </a-space>
      </template>
      <a-form layout="vertical">
        <div class="grid-2">
          <a-form-item label="签约要求">
            <a-switch v-model:checked="routePolicyModel.contractRequired" checked-children="必须签约" un-checked-children="无需签约" />
          </a-form-item>
          <a-form-item label="锁位策略">
            <a-select v-model:value="routePolicyModel.lockPolicy">
              <a-select-option v-for="item in lockPolicyOptions" :key="item.value" :value="item.value">{{ item.label }}</a-select-option>
            </a-select>
          </a-form-item>
        </div>
        <div class="grid-2">
          <a-form-item label="收款策略">
            <a-select v-model:value="routePolicyModel.paymentPolicy">
              <a-select-option v-for="item in paymentPolicyOptions" :key="item.value" :value="item.value">{{ item.label }}</a-select-option>
            </a-select>
          </a-form-item>
          <a-form-item label="定金规则类型">
            <a-select v-model:value="routePolicyModel.depositType">
              <a-select-option v-for="item in depositTypeOptions" :key="item.value" :value="item.value">{{ item.label }}</a-select-option>
            </a-select>
          </a-form-item>
        </div>
        <div class="grid-2">
          <a-form-item label="定金值">
            <a-input-number v-model:value="routePolicyModel.depositValue" :min="0" style="width: 100%" />
          </a-form-item>
          <a-form-item label="定金最晚(天)">
            <a-input-number v-model:value="routePolicyModel.depositDeadlineDays" :min="0" style="width: 100%" />
          </a-form-item>
        </div>
        <div class="grid-2">
          <a-form-item label="尾款最晚(出团前天数)">
            <a-input-number v-model:value="routePolicyModel.balanceDeadlineDays" :min="0" style="width: 100%" />
          </a-form-item>
          <a-form-item label="自动取消(小时)">
            <a-input-number v-model:value="routePolicyModel.autoCancelHours" :min="0" style="width: 100%" />
          </a-form-item>
        </div>
      </a-form>
    </a-drawer>

    <!-- 新增/编辑线路 Drawer -->
    <a-drawer v-model:open="routeModal" :title="routeForm.id ? '编辑线路' : '新增线路'" placement="right" :width="920">
      <template #extra>
        <a-space>
          <a-button @click="routeModal = false">取消</a-button>
          <a-button type="primary" :loading="saving" @click="saveRoute">保存</a-button>
        </a-space>
      </template>
      <a-form layout="vertical">
        <div class="grid-3">
          <a-form-item label="线路编码">
            <a-input v-model:value="routeForm.code" placeholder="留空自动生成，如 LX202603221001" />
          </a-form-item>
          <a-form-item label="线路名称" required>
            <a-input v-model:value="routeForm.name" />
          </a-form-item>
          <a-form-item label="产品分类" required>
            <a-select v-model:value="routeForm.category">
              <a-select-option value="国内游">国内游</a-select-option>
              <a-select-option value="出境游">出境游</a-select-option>
              <a-select-option value="自由行">自由行</a-select-option>
            </a-select>
          </a-form-item>
        </div>
        <div class="grid-3">
          <a-form-item label="出发地">
            <a-input v-model:value="routeForm.departureCity" />
          </a-form-item>
          <a-form-item label="目的地">
            <a-input v-model:value="routeForm.destinationCity" />
          </a-form-item>
          <a-form-item label="交通方式">
            <a-input v-model:value="routeForm.transportation" />
          </a-form-item>
        </div>
        <div class="grid-3">
          <a-form-item label="行程天数">
            <a-input-number v-model:value="routeForm.durationDays" :min="1" style="width: 100%" />
          </a-form-item>
          <a-form-item label="住宿晚数">
            <a-input-number v-model:value="routeForm.durationNights" :min="0" style="width: 100%" />
          </a-form-item>
          <a-form-item label="酒店标准">
            <a-input v-model:value="routeForm.hotelStandard" />
          </a-form-item>
        </div>
        <a-form-item label="线路亮点">
          <a-textarea v-model:value="routeForm.highlights" :rows="2" />
        </a-form-item>
        <a-form-item label="费用包含">
          <a-textarea v-model:value="routeForm.feeIncludes" :rows="2" />
        </a-form-item>
        <a-form-item label="费用不含">
          <a-textarea v-model:value="routeForm.feeExcludes" :rows="2" />
        </a-form-item>
        <a-form-item label="预订须知">
          <a-textarea v-model:value="routeForm.bookingNotice" :rows="2" />
        </a-form-item>
        <a-form-item label="补充描述">
          <a-textarea v-model:value="routeForm.description" :rows="2" />
        </a-form-item>
      </a-form>
    </a-drawer>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref, watch } from 'vue';
import RouteTable from '../../components/product/RouteTable.vue';
import { productApi } from '../../api/crm';
import {
  DEPOSIT_TYPE_LABEL_MAP,
  LOCK_POLICY_LABEL_MAP,
  PAYMENT_POLICY_LABEL_MAP,
  enumLabel
} from '../../constants/display';
import { notifyError, notifySuccess } from '../../utils/notify';

const saving = ref(false);
const routeModal = ref(false);
const routeDetailOpen = ref(false);
const routePolicyOpen = ref(false);
const routeKeyword = ref('');
const routePage = ref(1);
const routePageSize = ref(10);
const routeTotal = ref(0);

const routeRows = ref<any[]>([]);
const activeRoute = ref<any>(null);
const routePolicyModel = reactive<any>({
  contractRequired: false,
  lockPolicy: 'ON_DEPOSIT',
  paymentPolicy: 'DEPOSIT_BALANCE',
  depositType: 'PERCENT',
  depositValue: 30,
  depositDeadlineDays: 3,
  balanceDeadlineDays: 7,
  autoCancelHours: 24
});

const lockPolicyOptions = [
  { value: 'ON_APPROVAL', label: '审批通过锁位' },
  { value: 'ON_DEPOSIT', label: '定金到账锁位' },
  { value: 'MANUAL', label: '手工锁位' }
];
const paymentPolicyOptions = [
  { value: 'FULL', label: '一次性全款' },
  { value: 'DEPOSIT_BALANCE', label: '定金+尾款' }
];
const depositTypeOptions = [
  { value: 'PERCENT', label: '比例(%)' },
  { value: 'FIXED', label: '固定金额' }
];

const routeForm = reactive({
  id: undefined as string | undefined,
  code: '',
  name: '',
  category: '国内游',
  departureCity: '',
  destinationCity: '',
  durationDays: 5,
  durationNights: 4,
  transportation: '',
  hotelStandard: '',
  highlights: '',
  feeIncludes: '',
  feeExcludes: '',
  bookingNotice: '',
  description: ''
});

const loadRoutePage = async () => {
  const { data } = await productApi.routePage({
    page: routePage.value,
    size: routePageSize.value,
    keyword: routeKeyword.value.trim() || undefined
  });
  routeRows.value = data.data?.items || [];
  routeTotal.value = Number(data.data?.total || 0);
};

const loadRoutePolicy = async (routeId: string) => {
  const { data } = await productApi.routeOrderPolicy(routeId);
  Object.assign(routePolicyModel, data.data || {});
};

const openRoute = (record?: any) => {
  routeForm.id = record?.id;
  routeForm.code = record?.code || '';
  routeForm.name = record?.name || '';
  routeForm.category = record?.category || '国内游';
  routeForm.departureCity = record?.departureCity || '';
  routeForm.destinationCity = record?.destinationCity || '';
  routeForm.durationDays = record?.durationDays || 5;
  routeForm.durationNights = record?.durationNights || 4;
  routeForm.transportation = record?.transportation || '';
  routeForm.hotelStandard = record?.hotelStandard || '';
  routeForm.highlights = record?.highlights || '';
  routeForm.feeIncludes = record?.feeIncludes || '';
  routeForm.feeExcludes = record?.feeExcludes || '';
  routeForm.bookingNotice = record?.bookingNotice || '';
  routeForm.description = record?.description || '';
  routeModal.value = true;
};

const openRouteDetail = (record: any) => {
  activeRoute.value = record;
  void loadRoutePolicy(record.id);
  routeDetailOpen.value = true;
};

const saveRoute = async () => {
  if (!routeForm.name.trim()) return;
  saving.value = true;
  try {
    const payload = {
      ...routeForm,
      code: String(routeForm.code || '').trim() || undefined
    };
    if (routeForm.id) {
      await productApi.updateRoute(routeForm.id, payload);
      notifySuccess('线路更新成功');
    } else {
      await productApi.createRoute(payload);
      notifySuccess('线路创建成功');
    }
    routeModal.value = false;
    await loadRoutePage();
  } catch (error) {
    notifyError(error);
  } finally {
    saving.value = false;
  }
};

const removeRoute = async (record: any) => {
  try {
    await productApi.deleteRoute(record.id);
    notifySuccess('线路删除成功');
    if (activeRoute.value?.id === record.id) {
      activeRoute.value = null;
      routeDetailOpen.value = false;
    }
    await loadRoutePage();
  } catch (error) {
    notifyError(error);
  }
};

const openRoutePolicy = async () => {
  if (!activeRoute.value?.id) return;
  try {
    await loadRoutePolicy(activeRoute.value.id);
    routePolicyOpen.value = true;
  } catch (error) {
    notifyError(error);
  }
};

const saveRoutePolicy = async () => {
  if (!activeRoute.value?.id) return;
  saving.value = true;
  try {
    await productApi.updateRouteOrderPolicy(activeRoute.value.id, { ...routePolicyModel });
    routePolicyOpen.value = false;
    notifySuccess('线路下单策略保存成功');
    await loadRoutePolicy(activeRoute.value.id);
  } catch (error) {
    notifyError(error);
  } finally {
    saving.value = false;
  }
};

const onRoutePageChange = (page: number, pageSize: number) => {
  routePage.value = page;
  routePageSize.value = pageSize;
  void loadRoutePage();
};

watch(routeKeyword, () => {
  routePage.value = 1;
  void loadRoutePage();
});

onMounted(async () => {
  try {
    await loadRoutePage();
  } catch (error) {
    notifyError(error);
  }
});
</script>

<style scoped>
.route-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.section-card {
  border-radius: 12px;
}

.toolbar-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 8px;
}

.grid-2 {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0 16px;
}

.grid-3 {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr;
  gap: 0 16px;
}
</style>
