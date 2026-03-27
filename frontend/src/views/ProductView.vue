<template>
  <div class="product-page">
    <div class="biz-summary">
      <div class="item">
        <span class="label">线路总数</span>
        <strong class="value">{{ routeTotal }}</strong>
      </div>
      <div class="item">
        <span class="label">团期总数</span>
        <strong class="value">{{ departureTotal }}</strong>
      </div>
      <div class="item">
        <span class="label">可售团期</span>
        <strong class="value">{{ openDepartureCount }}</strong>
      </div>
      <div class="item">
        <span class="label">当前团期价格项</span>
        <strong class="value">{{ prices.length }}</strong>
      </div>
    </div>

    <a-card class="section-card" :bordered="false">
      <a-tabs v-model:activeKey="activeTab">
        <a-tab-pane key="routes" tab="线路列表">
          <div class="toolbar-row">
            <a-input-search v-model:value="routeKeyword" placeholder="按线路名称、出发地、目的地筛选" style="width: 320px" />
            <a-button type="primary" @click="openRoute()">新增线路</a-button>
          </div>
          <route-table
            style="margin-top: 10px"
            :items="routeRows"
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
        </a-tab-pane>

        <a-tab-pane key="departures" tab="团期列表">
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
        </a-tab-pane>
      </a-tabs>
    </a-card>

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
        <div class="grid-3">
          <a-form-item label="团期编码">
            <a-input v-model:value="departureForm.code" placeholder="留空自动生成，如 TQ202603221001" />
          </a-form-item>
          <a-form-item label="出发日期" required>
            <a-input v-model:value="departureForm.startDate" placeholder="YYYY-MM-DD" />
          </a-form-item>
          <a-form-item label="返程日期" required>
            <a-input v-model:value="departureForm.endDate" placeholder="YYYY-MM-DD" />
          </a-form-item>
        </div>
        <div class="grid-3">
          <a-form-item label="报名截止">
            <a-input v-model:value="departureForm.registrationDeadline" placeholder="YYYY-MM-DD" />
          </a-form-item>
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

        <a-divider orientation="left">价格项设置（支持自定义项目）</a-divider>
        <div class="toolbar-row" style="margin-bottom: 8px">
          <a-button @click="addDeparturePriceItem">新增价格项</a-button>
        </div>
        <a-empty v-if="!departurePriceItems.length" description="请至少添加一个价格项" />
        <div v-else class="price-item-list">
          <div v-for="(item, index) in departurePriceItems" :key="`${item.id || 'new'}-${index}`" class="price-item-row">
            <a-auto-complete
              v-model:value="item.priceType"
              :options="standardPriceTypeOptions"
              placeholder="价格类型，如 成人价 / 签证费"
            />
            <a-input v-model:value="item.priceLabel" placeholder="显示名称，如 成人价、签证服务费" />
            <a-input-number v-model:value="item.price" :min="0" style="width: 100%" placeholder="价格" />
            <a-select v-model:value="item.currency" :options="currencyOptions" />
            <a-input v-model:value="item.description" placeholder="说明" />
            <a-button danger @click="removeDeparturePriceItem(index)" :disabled="departurePriceItems.length === 1">删除</a-button>
          </div>
        </div>
      </a-form>
    </a-drawer>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue';
import DeparturePriceTable from '../components/product/DeparturePriceTable.vue';
import DepartureTable from '../components/product/DepartureTable.vue';
import RouteTable from '../components/product/RouteTable.vue';
import { productApi } from '../api/crm';
import {
  CURRENCY_LABEL_MAP,
  DEPARTURE_STATUS_LABEL_MAP,
  DEPARTURE_STATUS_OPTIONS,
  DEPOSIT_TYPE_LABEL_MAP,
  LOCK_POLICY_LABEL_MAP,
  PAYMENT_POLICY_LABEL_MAP,
  PRICE_TYPE_OPTIONS,
  enumLabel
} from '../constants/display';
import { notifyError, notifySuccess } from '../utils/notify';

const activeTab = ref('routes');
const saving = ref(false);
const routeModal = ref(false);
const departureModal = ref(false);
const routeDetailOpen = ref(false);
const departureDetailOpen = ref(false);
const routePolicyOpen = ref(false);
const departurePolicyOpen = ref(false);
const routeKeyword = ref('');
const departureFilterRouteId = ref<number | undefined>();
const routePage = ref(1);
const routePageSize = ref(10);
const routeTotal = ref(0);
const departurePage = ref(1);
const departurePageSize = ref(10);
const departureTotal = ref(0);

const routes = ref<any[]>([]);
const departures = ref<any[]>([]);
const routeRows = ref<any[]>([]);
const departureRows = ref<any[]>([]);
const prices = ref<any[]>([]);
const activeRoute = ref<any>(null);
const activeDeparture = ref<any>(null);
const departurePriceItems = ref<any[]>([]);
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
  value: item.label
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
const departureForm = reactive({
  id: undefined as string | undefined,
  routeId: undefined as string | undefined,
  code: '',
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

const routeMap = computed(() => Object.fromEntries(routes.value.map((item) => [item.id, item.name])));
const openDepartureCount = computed(() => departureRows.value.filter((item) => item.status === 'OPEN').length);

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

const loadDepartureOptions = async () => {
  const { data } = await productApi.departures();
  departures.value = data.data || [];
};

const loadRoutePage = async () => {
  const { data } = await productApi.routePage({
    page: routePage.value,
    size: routePageSize.value,
    keyword: routeKeyword.value.trim() || undefined
  });
  routeRows.value = data.data?.items || [];
  routeTotal.value = Number(data.data?.total || 0);
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

const loadPrices = async (departureId: number) => {
  const { data } = await productApi.prices(departureId);
  prices.value = data.data || [];
};

const loadRoutePolicy = async (routeId: number) => {
  const { data } = await productApi.routeOrderPolicy(routeId);
  Object.assign(routePolicyModel, data.data || {});
};

const loadDeparturePolicy = async (departureId: number) => {
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
  if (!value) {
    return '';
  }
  return priceTypeCodeToLabel[value.toUpperCase()] || value;
};
const toPriceTypeCode = (raw?: string) => {
  const value = String(raw || '').trim();
  if (!value) {
    return '';
  }
  const upper = value.toUpperCase();
  if (priceTypeCodeToLabel[upper]) {
    return upper;
  }
  return priceTypeLabelToCode[value] || value;
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
    await Promise.all([loadRouteOptions(), loadRoutePage()]);
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
    await Promise.all([loadRouteOptions(), loadRoutePage(), loadDepartures()]);
  } catch (error) {
    notifyError(error);
  }
};

const openDeparture = async (record?: any) => {
  departureForm.id = record?.id;
  departureForm.routeId = record?.routeId || departureFilterRouteId.value || routes.value[0]?.id;
  departureForm.code = record?.code || '';
  departureForm.startDate = record?.startDate || '2026-04-01';
  departureForm.endDate = record?.endDate || '2026-04-07';
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
      departurePriceItems.value = records.length ? records.map((item: any) => createDeparturePriceItem(item)) : [createDeparturePriceItem()];
    } else {
      departurePriceItems.value = [createDeparturePriceItem({ priceType: 'ADULT', price: 1999, currency: CNY })];
    }
  } catch (error) {
    departurePriceItems.value = [createDeparturePriceItem({ priceType: 'ADULT', price: 1999, currency: CNY })];
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
  if (!departureForm.routeId) return;
  const normalizedItems = departurePriceItems.value
    .map((item) => ({
      id: item.id,
      priceType: toPriceTypeCode(item.priceType),
      priceLabel: String(item.priceLabel || '').trim(),
      price: Number(item.price ?? 0),
      currency: CNY,
      description: String(item.description || '').trim()
    }))
    .filter((item) => item.priceType && item.price > 0);
  if (!normalizedItems.length) {
    notifyError(new Error('请至少配置一个有效价格项（价格类型和价格必填）'));
    return;
  }
  saving.value = true;
  try {
    const payload = {
      ...departureForm,
      code: String(departureForm.code || '').trim() || undefined
    };
    let departureId = departureForm.id as number | undefined;
    if (departureForm.id) {
      await productApi.updateDeparture(departureForm.id, payload);
      notifySuccess('团期更新成功');
    } else {
      const { data } = await productApi.createDeparture(payload);
      departureId = data.data?.id;
      notifySuccess('团期创建成功');
    }
    if (!departureId) {
      throw new Error('团期保存后未返回ID');
    }
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
    await Promise.all([loadDepartureOptions(), loadDepartures(), loadPrices(departureId)]);
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
    await Promise.all([loadDepartureOptions(), loadDepartures()]);
  } catch (error) {
    notifyError(error);
  }
};

const addDeparturePriceItem = () => {
  departurePriceItems.value.push(createDeparturePriceItem());
};

const removeDeparturePriceItem = (index: number) => {
  if (departurePriceItems.value.length === 1) {
    return;
  }
  departurePriceItems.value.splice(index, 1);
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

const openDeparturePolicy = async () => {
  if (!activeDeparture.value?.id) return;
  try {
    await loadDeparturePolicy(activeDeparture.value.id);
    departurePolicyOpen.value = true;
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

const onRoutePageChange = (page: number, pageSize: number) => {
  routePage.value = page;
  routePageSize.value = pageSize;
  void loadRoutePage();
};

const onDeparturePageChange = (page: number, pageSize: number) => {
  departurePage.value = page;
  departurePageSize.value = pageSize;
  void loadDepartures();
};

watch(routeKeyword, () => {
  routePage.value = 1;
  void loadRoutePage();
});

watch(departureFilterRouteId, () => {
  departurePage.value = 1;
  void loadDepartures();
});

onMounted(async () => {
  try {
    await Promise.all([loadRouteOptions(), loadDepartureOptions(), loadRoutePage(), loadDepartures()]);
  } catch (error) {
    notifyError(error);
  }
});
</script>

<style scoped>
.product-page {
  display: grid;
  gap: 10px;
}

.grid-3 {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.grid-2 {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.price-item-list {
  display: grid;
  gap: 8px;
}

.price-item-row {
  display: grid;
  grid-template-columns: 180px 180px 120px 100px 1fr 70px;
  gap: 8px;
}

@media (max-width: 1200px) {
  .grid-3,
  .grid-2,
  .price-item-row {
    grid-template-columns: 1fr;
  }
}
</style>
