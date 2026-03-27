<template>
  <div class="departure-page">
    <a-card class="section-card" :bordered="false">
      <div class="toolbar-row">
        <a-select
          v-model:value="departureFilterRouteId"
          allow-clear
          placeholder="按线路筛选"
          style="width: 240px"
        >
          <a-select-option v-for="item in routes" :key="item.id" :value="item.id">{{ item.name }}</a-select-option>
        </a-select>
        <a-button type="primary" @click="openDeparture()">新增团期</a-button>
      </div>
      <departure-table
        style="margin-top: 10px"
        :items="decoratedDepartureRows"
        @view="openDepartureDetail"
        @edit="openDeparture"
        @remove="removeDeparture"
      />
      <a-pagination
        style="margin-top: 12px; text-align: right"
        :current="departurePage"
        :page-size="departurePageSize"
        :total="departureTotal"
        show-size-changer
        :page-size-options="['10', '20', '50']"
        @change="onDeparturePageChange"
        @showSizeChange="onDeparturePageChange"
      />
    </a-card>

    <a-drawer v-model:open="departureDetailOpen" width="760" title="团期详情" placement="right">
      <a-empty v-if="!activeDeparture" description="未选择团期" />
      <template v-else>
        <div class="toolbar-row" style="margin-bottom: 8px">
          <a-button type="primary" @click="openDeparturePolicy">编辑团期策略覆盖</a-button>
        </div>
        <a-descriptions :column="2" bordered size="small">
          <a-descriptions-item label="团期编码">{{ activeDeparture.code }}</a-descriptions-item>
          <a-descriptions-item label="线路">{{ activeDeparture.routeName || '-' }}</a-descriptions-item>
          <a-descriptions-item label="出发日期">{{ activeDeparture.startDate }}</a-descriptions-item>
          <a-descriptions-item label="返程日期">{{ activeDeparture.endDate }}</a-descriptions-item>
          <a-descriptions-item label="截止报名">{{ activeDeparture.registrationDeadline || '-' }}</a-descriptions-item>
          <a-descriptions-item label="状态">{{ enumLabel(DEPARTURE_STATUS_LABEL_MAP, activeDeparture.status) }}</a-descriptions-item>
          <a-descriptions-item label="库存">{{ activeDeparture.stock }}</a-descriptions-item>
          <a-descriptions-item label="成团人数">{{ activeDeparture.minGroupSize || 0 }} / {{ activeDeparture.maxGroupSize || '不限' }}</a-descriptions-item>
          <a-descriptions-item label="集合地" :span="2">{{ activeDeparture.gatheringPlace || '-' }}</a-descriptions-item>
          <a-descriptions-item label="出团说明" :span="2">{{ activeDeparture.departureNotice || '-' }}</a-descriptions-item>
        </a-descriptions>
        <a-divider orientation="left">价格表</a-divider>
        <departure-price-table :items="prices" :editable="false" />
        <a-divider orientation="left">生效下单策略</a-divider>
        <a-descriptions :column="2" bordered size="small">
          <a-descriptions-item label="签约要求">{{ departureEffectivePolicy?.contractRequired ? '必须签约' : '无需签约' }}</a-descriptions-item>
          <a-descriptions-item label="锁位策略">{{ enumLabel(LOCK_POLICY_LABEL_MAP, departureEffectivePolicy?.lockPolicy) }}</a-descriptions-item>
          <a-descriptions-item label="收款策略">{{ enumLabel(PAYMENT_POLICY_LABEL_MAP, departureEffectivePolicy?.paymentPolicy) }}</a-descriptions-item>
          <a-descriptions-item label="定金规则">{{ enumLabel(DEPOSIT_TYPE_LABEL_MAP, departureEffectivePolicy?.depositType) }} / {{ departureEffectivePolicy?.depositValue ?? '-' }}</a-descriptions-item>
          <a-descriptions-item label="定金时限">{{ departureEffectivePolicy?.depositDeadlineDays ?? '-' }} 天</a-descriptions-item>
          <a-descriptions-item label="尾款时限">{{ departureEffectivePolicy?.balanceDeadlineDays ?? '-' }} 天</a-descriptions-item>
          <a-descriptions-item label="自动取消">{{ departureEffectivePolicy?.autoCancelHours ?? '-' }} 小时</a-descriptions-item>
        </a-descriptions>
      </template>
    </a-drawer>

    <a-drawer v-model:open="departurePolicyOpen" title="团期策略覆盖" placement="right" width="560">
      <template #extra>
        <a-space>
          <a-button @click="departurePolicyOpen = false">取消</a-button>
          <a-button type="primary" :loading="saving" @click="saveDeparturePolicy">保存</a-button>
        </a-space>
      </template>
      <a-alert type="info" show-icon style="margin-bottom: 12px" message="留空表示继承线路默认策略" />
      <a-form layout="vertical">
        <div class="grid-2">
          <a-form-item label="签约要求覆盖">
            <a-select v-model:value="departurePolicyModel.contractRequired" allow-clear>
              <a-select-option :value="true">必须签约</a-select-option>
              <a-select-option :value="false">无需签约</a-select-option>
            </a-select>
          </a-form-item>
          <a-form-item label="锁位策略覆盖">
            <a-select v-model:value="departurePolicyModel.lockPolicy" allow-clear>
              <a-select-option v-for="item in lockPolicyOptions" :key="item.value" :value="item.value">{{ item.label }}</a-select-option>
            </a-select>
          </a-form-item>
        </div>
        <div class="grid-2">
          <a-form-item label="收款策略覆盖">
            <a-select v-model:value="departurePolicyModel.paymentPolicy" allow-clear>
              <a-select-option v-for="item in paymentPolicyOptions" :key="item.value" :value="item.value">{{ item.label }}</a-select-option>
            </a-select>
          </a-form-item>
          <a-form-item label="定金类型覆盖">
            <a-select v-model:value="departurePolicyModel.depositType" allow-clear>
              <a-select-option v-for="item in depositTypeOptions" :key="item.value" :value="item.value">{{ item.label }}</a-select-option>
            </a-select>
          </a-form-item>
        </div>
        <div class="grid-2">
          <a-form-item label="定金值覆盖">
            <a-input-number v-model:value="departurePolicyModel.depositValue" :min="0" style="width: 100%" />
          </a-form-item>
          <a-form-item label="定金最晚(天)覆盖">
            <a-input-number v-model:value="departurePolicyModel.depositDeadlineDays" :min="0" style="width: 100%" />
          </a-form-item>
        </div>
        <div class="grid-2">
          <a-form-item label="尾款最晚(天)覆盖">
            <a-input-number v-model:value="departurePolicyModel.balanceDeadlineDays" :min="0" style="width: 100%" />
          </a-form-item>
          <a-form-item label="自动取消(小时)覆盖">
            <a-input-number v-model:value="departurePolicyModel.autoCancelHours" :min="0" style="width: 100%" />
          </a-form-item>
        </div>
      </a-form>
    </a-drawer>

    <a-drawer v-model:open="departureModal" :title="departureForm.id ? '编辑团期' : '新增团期'" placement="right" :width="920">
      <template #extra>
        <a-space>
          <a-button @click="departureModal = false">取消</a-button>
          <a-button type="primary" :loading="saving" @click="saveDeparture">保存</a-button>
        </a-space>
      </template>
      <a-form layout="vertical">
        <a-form-item label="所属线路" required>
          <a-select v-model:value="departureForm.routeId" placeholder="请选择线路">
            <a-select-option v-for="item in routes" :key="item.id" :value="item.id">{{ item.name }}</a-select-option>
          </a-select>
        </a-form-item>
        <div class="grid-2">
          <a-form-item label="团期编码">
            <a-input v-model:value="departureForm.code" placeholder="留空自动生成，如 TQ202603221001" />
          </a-form-item>
          <a-form-item label="团期名称">
            <a-input v-model:value="departureForm.name" placeholder="如：五一黄金周精品团" />
          </a-form-item>
        </div>
        <div class="grid-2">
          <a-form-item label="出发 / 返程日期" required>
            <a-range-picker
              v-model:value="departureDateRange"
              style="width: 100%"
              value-format="YYYY-MM-DD"
              :placeholder="['出发日期', '返程日期']"
              @change="onDepartureDateRangeChange"
            />
          </a-form-item>
          <a-form-item label="报名截止">
            <a-date-picker
              v-model:value="departureForm.registrationDeadline"
              style="width: 100%"
              value-format="YYYY-MM-DD"
              placeholder="报名截止日期"
            />
          </a-form-item>
        </div>
        <div class="grid-3">
          <a-form-item label="库存" required>
            <a-input-number v-model:value="departureForm.stock" :min="0" style="width: 100%" />
          </a-form-item>
          <a-form-item label="团期状态">
            <a-select v-model:value="departureForm.status">
              <a-select-option v-for="item in DEPARTURE_STATUS_OPTIONS" :key="item.value" :value="item.value">
                {{ item.label }}
              </a-select-option>
            </a-select>
          </a-form-item>
          <a-form-item></a-form-item>
        </div>
        <div class="grid-2">
          <a-form-item label="最小成团人数">
            <a-input-number v-model:value="departureForm.minGroupSize" :min="0" style="width: 100%" />
          </a-form-item>
          <a-form-item label="最大收客人数">
            <a-input-number v-model:value="departureForm.maxGroupSize" :min="0" style="width: 100%" />
          </a-form-item>
        </div>
        <a-form-item label="集合地">
          <a-input v-model:value="departureForm.gatheringPlace" />
        </a-form-item>
        <a-form-item label="出团说明">
          <a-textarea v-model:value="departureForm.departureNotice" :rows="2" />
        </a-form-item>

        <a-divider orientation="left">价格项设置（非附加项只能添加一次）</a-divider>
        <div class="toolbar-row" style="margin-bottom: 8px">
          <a-button @click="addDeparturePriceItem">新增价格项</a-button>
        </div>
        <a-empty v-if="!departurePriceItems.length" description="请至少添加一个价格项" />
        <div v-else class="price-item-list">
          <div v-for="(item, index) in departurePriceItems" :key="`${item.id || 'new'}-${index}`" class="price-item-row">
            <a-select
              v-model:value="item.priceType"
              :options="standardPriceTypeOptions"
              placeholder="价格类型"
              style="width: 150px"
              @change="(val) => handlePriceTypeChange(item, val)"
            />
            <a-input v-model:value="item.priceLabel" placeholder="显示名称，附加项必填" :disabled="item.priceType !== '附加项'" />
            <a-input-number v-model:value="item.price" :min="0" style="width: 100%" placeholder="价格" />
            <a-select v-model:value="item.currency" :options="currencyOptions" style="width: 150px" />
            <a-input v-model:value="item.description" placeholder="说明" />
            <a-button danger @click="removeDeparturePriceItem(index)" :disabled="departurePriceItems.length === 1">删除</a-button>
          </div>
        </div>
      </a-form>
    </a-drawer>
  </div>
</template>

<script setup lang="ts">
import dayjs from 'dayjs';
import { computed, onMounted, reactive, ref, watch } from 'vue';
import DeparturePriceTable from '../../components/product/DeparturePriceTable.vue';
import DepartureTable from '../../components/product/DepartureTable.vue';
import { productApi } from '../../api/crm';
import {
  CURRENCY_LABEL_MAP,
  DEPARTURE_STATUS_LABEL_MAP,
  DEPARTURE_STATUS_OPTIONS,
  DEPOSIT_TYPE_LABEL_MAP,
  LOCK_POLICY_LABEL_MAP,
  PAYMENT_POLICY_LABEL_MAP,
  PRICE_TYPE_OPTIONS,
  enumLabel
} from '../../constants/display';
import { notifyError, notifySuccess } from '../../utils/notify';

const saving = ref(false);
const departureModal = ref(false);
const departureDetailOpen = ref(false);
const departurePolicyOpen = ref(false);
const departureFilterRouteId = ref<string | undefined>();
const departurePage = ref(1);
const departurePageSize = ref(10);
const departureTotal = ref(0);

const routes = ref<any[]>([]);
const departureRows = ref<any[]>([]);
const prices = ref<any[]>([]);
const activeDeparture = ref<any>(null);
const departurePriceItems = ref<any[]>([]);
const departurePolicyModel = reactive<any>({
  contractRequired: undefined as boolean | undefined,
  lockPolicy: undefined as string | undefined,
  paymentPolicy: undefined as string | undefined,
  depositType: undefined as string | undefined,
  depositValue: undefined as number | undefined,
  depositDeadlineDays: undefined as number | undefined,
  balanceDeadlineDays: undefined as number | undefined,
  autoCancelHours: undefined as number | undefined
});
const departureEffectivePolicy = ref<any | null>(null);

const CNY = 'CNY';
const standardPriceTypeOptions = PRICE_TYPE_OPTIONS.map((item) => ({
  value: item.label,
  label: item.label
}));
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
const currencyOptions = [{ value: CNY, label: CURRENCY_LABEL_MAP[CNY] }];
const priceTypeCodeToLabel = PRICE_TYPE_OPTIONS.reduce((acc, item) => {
  acc[item.value] = item.label;
  return acc;
}, {} as Record<string, string>);
const priceTypeLabelToCode = PRICE_TYPE_OPTIONS.reduce((acc, item) => {
  acc[item.label] = item.value;
  return acc;
}, {} as Record<string, string>);

const departureForm = reactive({
  id: undefined as string | undefined,
  routeId: undefined as string | undefined,
  code: '',
  name: '',
  startDate: '2026-04-01',
  endDate: '2026-04-07',
  registrationDeadline: '',
  stock: 30,
  minGroupSize: 1,
  maxGroupSize: 30,
  status: 'OPEN',
  gatheringPlace: '',
  departureNotice: ''
});

const departureDateRange = ref<[string, string] | null>(null);

const onDepartureDateRangeChange = (val: [string, string] | null) => {
  if (val) {
    departureForm.startDate = val[0];
    departureForm.endDate = val[1];
  } else {
    departureForm.startDate = '';
    departureForm.endDate = '';
  }
};

const routeMap = computed(() => Object.fromEntries(routes.value.map((item) => [item.id, item.name])));

const decoratedDepartureRows = computed(() =>
  departureRows.value.map((item) => ({
    ...item,
    routeName: routeMap.value[item.routeId] || '-',
    statusLabel: enumLabel(DEPARTURE_STATUS_LABEL_MAP, item.status)
  }))
);

const loadRouteOptions = async () => {
  const { data } = await productApi.routes();
  routes.value = data.data || [];
};

const loadDepartures = async () => {
  const { data } = await productApi.departurePage({
    page: departurePage.value,
    size: departurePageSize.value,
    routeId: departureFilterRouteId.value,
    keyword: undefined
  });
  departureRows.value = data.data?.items || [];
  departureTotal.value = Number(data.data?.total || 0);
};

const loadPrices = async (departureId: string) => {
  const { data } = await productApi.prices(departureId);
  prices.value = data.data || [];
};

const loadDeparturePolicy = async (departureId: string) => {
  const { data } = await productApi.departureOrderPolicy(departureId);
  const payload = data.data || {};
  departureEffectivePolicy.value = payload.effective || null;
  Object.assign(departurePolicyModel, {
    contractRequired: payload.override?.contractRequired,
    lockPolicy: payload.override?.lockPolicy,
    paymentPolicy: payload.override?.paymentPolicy,
    depositType: payload.override?.depositType,
    depositValue: payload.override?.depositValue,
    depositDeadlineDays: payload.override?.depositDeadlineDays,
    balanceDeadlineDays: payload.override?.balanceDeadlineDays,
    autoCancelHours: payload.override?.autoCancelHours
  });
};

const createDeparturePriceItem = (record?: any) => ({
  id: record?.id as number | undefined,
  priceType: toPriceTypeDisplay(record?.priceType || 'ADULT'),
  priceLabel: record?.priceLabel || '',
  price: Number(record?.price ?? 0),
  currency: record?.currency || CNY,
  description: record?.description || ''
});

const toPriceTypeDisplay = (raw?: string) => {
  const value = String(raw || '').trim();
  if (!value) return '';
  return priceTypeCodeToLabel[value.toUpperCase()] || value;
};

const toPriceTypeCode = (raw?: string) => {
  const value = String(raw || '').trim();
  if (!value) return '';
  const upper = value.toUpperCase();
  if (priceTypeCodeToLabel[upper]) return upper;
  return priceTypeLabelToCode[value] || value;
};

// 处理价格类型变更：选择非附加项时自动填充名称，并验证去重
const handlePriceTypeChange = (item: any, value: string) => {
  if (value !== '附加项') {
    // 检查是否已经有这个类型
    const count = departurePriceItems.value.filter(p => p.priceType === value).length;
    if (count > 1) {
      notifyError(new Error(`不能重复添加 ${value}`));
      item.priceType = '';
      item.priceLabel = '';
      return;
    }
    item.priceLabel = value; // 自动填充
  } else {
    item.priceLabel = ''; // 附加项需要手动填写
  }
};

const openDeparture = async (record?: any) => {
  departureForm.id = record?.id;
  departureForm.routeId = record?.routeId || departureFilterRouteId.value || undefined;
  departureForm.code = record?.code || '';
  departureForm.name = record?.name || '';
  departureForm.startDate = record?.startDate || dayjs().add(7, 'day').format('YYYY-MM-DD');
  departureForm.endDate = record?.endDate || dayjs().add(10, 'day').format('YYYY-MM-DD');
  departureDateRange.value = [departureForm.startDate, departureForm.endDate];
  departureForm.registrationDeadline = record?.registrationDeadline || '';
  departureForm.stock = record?.stock || 30;
  departureForm.minGroupSize = record?.minGroupSize || 1;
  departureForm.maxGroupSize = record?.maxGroupSize || 30;
  departureForm.status = record?.status || 'OPEN';
  departureForm.gatheringPlace = record?.gatheringPlace || '';
  departureForm.departureNotice = record?.departureNotice || '';
  
  try {
    if (record?.id) {
      const { data } = await productApi.prices(record.id);
      const records = data.data || [];
      departurePriceItems.value = records.length ? records.map((item: any) => createDeparturePriceItem(item)) : [
        createDeparturePriceItem({ priceType: 'ADULT', priceLabel: '成人价', price: undefined, currency: CNY }),
        createDeparturePriceItem({ priceType: 'CHILD', priceLabel: '儿童价', price: undefined, currency: CNY })
      ];
    } else {
      departurePriceItems.value = [
        createDeparturePriceItem({ priceType: 'ADULT', priceLabel: '成人价', price: undefined, currency: CNY }),
        createDeparturePriceItem({ priceType: 'CHILD', priceLabel: '儿童价', price: undefined, currency: CNY })
      ];
    }
  } catch (error) {
    departurePriceItems.value = [
      createDeparturePriceItem({ priceType: 'ADULT', priceLabel: '成人价', price: undefined, currency: CNY }),
      createDeparturePriceItem({ priceType: 'CHILD', priceLabel: '儿童价', price: undefined, currency: CNY })
    ];
    notifyError(error);
  }
  departureModal.value = true;
};

const openDepartureDetail = async (record: any) => {
  activeDeparture.value = record;
  await Promise.all([loadPrices(record.id), loadDeparturePolicy(record.id)]);
  departureDetailOpen.value = true;
};

const saveDeparture = async () => {
  if (!departureForm.routeId) {
    notifyError(new Error('请选择线路'));
    return;
  }
  if (!departureForm.startDate || !departureForm.endDate) {
    notifyError(new Error('请选择出发和返程日期'));
    return;
  }

  // 前端验证：非附加项查重，附加项查名称是否填写
  const typeCounts: Record<string, number> = {};
  let hasError = false;
  const normalizedItems = departurePriceItems.value.map(item => {
    if (item.priceType !== '附加项') {
      typeCounts[item.priceType] = (typeCounts[item.priceType] || 0) + 1;
      if (typeCounts[item.priceType] > 1) {
        notifyError(new Error(`${item.priceType} 不能重复添加`));
        hasError = true;
      }
    } else {
      if (!item.priceLabel || !item.priceLabel.trim()) {
        notifyError(new Error('附加项的显示名称不能为空'));
        hasError = true;
      }
    }
    return {
      id: item.id,
      priceType: toPriceTypeCode(item.priceType),
      priceLabel: String(item.priceLabel || '').trim(),
      price: Number(item.price ?? 0),
      currency: CNY,
      description: String(item.description || '').trim()
    };
  }).filter((item) => item.priceType && item.priceLabel && item.price > 0);

  if (hasError) return;

  if (!normalizedItems.length) {
    notifyError(new Error('请至少配置一个有效价格项（价格类型、显示名称和价格必填）'));
    return;
  }
  
  saving.value = true;
  try {
    const payload = {
      ...departureForm,
      code: String(departureForm.code || '').trim() || undefined
    };
    let departureId = departureForm.id as string | undefined;
    if (departureForm.id) {
      await productApi.updateDeparture(departureForm.id, payload);
      notifySuccess('团期更新成功');
    } else {
      const { data } = await productApi.createDeparture(payload);
      departureId = data.data?.id;
      notifySuccess('团期创建成功');
    }
    if (!departureId) throw new Error('团期保存后未返回ID');

    const { data: existingPriceRes } = await productApi.prices(departureId);
    const existingPrices = existingPriceRes.data || [];
    const incomingIds = new Set(normalizedItems.filter((item) => item.id).map((item) => item.id));
    for (const oldItem of existingPrices) {
      if (!incomingIds.has(oldItem.id)) {
        await productApi.deletePrice(departureId, oldItem.id);
      }
    }
    for (const item of normalizedItems) {
      const pricePayload = {
        priceType: item.priceType,
        priceLabel: item.priceLabel,
        price: item.price,
        currency: item.currency,
        description: item.description
      };
      if (item.id) {
        await productApi.updatePrice(departureId, item.id, pricePayload);
      } else {
        await productApi.createPrice(departureId, pricePayload);
      }
    }
    departureModal.value = false;
    await Promise.all([loadDepartures(), loadPrices(departureId)]);
  } catch (error) {
    notifyError(error);
  } finally {
    saving.value = false;
  }
};

const removeDeparture = async (record: any) => {
  try {
    await productApi.deleteDeparture(record.id);
    notifySuccess('团期删除成功');
    if (activeDeparture.value?.id === record.id) {
      activeDeparture.value = null;
      departureDetailOpen.value = false;
      prices.value = [];
    }
    await loadDepartures();
  } catch (error) {
    notifyError(error);
  }
};

const addDeparturePriceItem = () => {
  departurePriceItems.value.push(createDeparturePriceItem({ priceType: 'EXTRA', priceLabel: '', price: undefined, currency: CNY }));
};

const removeDeparturePriceItem = (index: number) => {
  if (departurePriceItems.value.length === 1) return;
  departurePriceItems.value.splice(index, 1);
};

const openDeparturePolicy = async () => {
  if (!activeDeparture.value?.id) return;
  try {
    await loadDeparturePolicy(activeDeparture.value.id);
    departurePolicyOpen.value = true;
  } catch (error) {
    notifyError(error);
  }
};

const saveDeparturePolicy = async () => {
  if (!activeDeparture.value?.id) return;
  saving.value = true;
  try {
    await productApi.updateDepartureOrderPolicy(activeDeparture.value.id, { ...departurePolicyModel });
    departurePolicyOpen.value = false;
    notifySuccess('团期策略覆盖保存成功');
    await loadDeparturePolicy(activeDeparture.value.id);
  } catch (error) {
    notifyError(error);
  } finally {
    saving.value = false;
  }
};

const onDeparturePageChange = (page: number, pageSize: number) => {
  departurePage.value = page;
  departurePageSize.value = pageSize;
  void loadDepartures();
};

watch(departureFilterRouteId, () => {
  departurePage.value = 1;
  void loadDepartures();
});

onMounted(async () => {
  try {
    await Promise.all([loadRouteOptions(), loadDepartures()]);
  } catch (error) {
    notifyError(error);
  }
});
</script>

<style scoped>
.departure-page {
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

.price-item-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.price-item-row {
  display: flex;
  gap: 8px;
  align-items: center;
}
</style>
