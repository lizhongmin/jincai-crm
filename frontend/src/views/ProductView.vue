<template>
  <div class="product-page">
    <a-card class="section-card" :bordered="false">
      <template #title>产品与团期</template>
      <a-tabs v-model:activeKey="activeTab">
        <a-tab-pane key="routes" tab="线路">
          <div class="toolbar-row">
            <a-input-search v-model:value="routeKeyword" placeholder="按线路名称、目的地筛选" style="width: 280px" />
            <a-button type="primary" @click="openRoute()">新增线路</a-button>
          </div>
          <route-table
            style="margin-top: 12px"
            :items="filteredRoutes"
            @view="openRouteDetail"
            @edit="openRoute"
            @remove="removeRoute"
          />
        </a-tab-pane>

        <a-tab-pane key="departures" tab="团期">
          <div class="toolbar-row">
            <a-select
              v-model:value="departureFilterRouteId"
              allow-clear
              placeholder="按线路筛选"
              style="width: 240px"
              @change="loadDepartures"
            >
              <a-select-option v-for="item in routes" :key="item.id" :value="item.id">{{ item.name }}</a-select-option>
            </a-select>
            <a-button type="primary" @click="openDeparture()">新增团期</a-button>
          </div>
          <departure-table
            style="margin-top: 12px"
            :items="decoratedDepartures"
            @view="openDepartureDetail"
            @edit="openDeparture"
            @remove="removeDeparture"
          />
        </a-tab-pane>
      </a-tabs>
    </a-card>

    <a-drawer v-model:open="routeDetailOpen" width="720" title="线路详情">
      <a-empty v-if="!activeRoute" description="未选择线路" />
      <template v-else>
        <a-descriptions :column="2" bordered size="small">
          <a-descriptions-item label="编码">{{ activeRoute.code }}</a-descriptions-item>
          <a-descriptions-item label="线路名称">{{ activeRoute.name }}</a-descriptions-item>
          <a-descriptions-item label="产品分类">{{ activeRoute.category }}</a-descriptions-item>
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
      </template>
    </a-drawer>

    <a-drawer v-model:open="departureDetailOpen" width="760" title="团期详情">
      <a-empty v-if="!activeDeparture" description="未选择团期" />
      <template v-else>
        <a-descriptions :column="2" bordered size="small">
          <a-descriptions-item label="团期编码">{{ activeDeparture.code }}</a-descriptions-item>
          <a-descriptions-item label="线路">{{ activeDeparture.routeName || '-' }}</a-descriptions-item>
          <a-descriptions-item label="出发日期">{{ activeDeparture.startDate }}</a-descriptions-item>
          <a-descriptions-item label="返程日期">{{ activeDeparture.endDate }}</a-descriptions-item>
          <a-descriptions-item label="截止报名">{{ activeDeparture.registrationDeadline || '-' }}</a-descriptions-item>
          <a-descriptions-item label="状态">{{ activeDeparture.status || '-' }}</a-descriptions-item>
          <a-descriptions-item label="库存">{{ activeDeparture.stock }}</a-descriptions-item>
          <a-descriptions-item label="成团人数">{{ activeDeparture.minGroupSize || 0 }} / {{ activeDeparture.maxGroupSize || '不限' }}</a-descriptions-item>
          <a-descriptions-item label="集合地" :span="2">{{ activeDeparture.gatheringPlace || '-' }}</a-descriptions-item>
          <a-descriptions-item label="出团说明" :span="2">{{ activeDeparture.departureNotice || '-' }}</a-descriptions-item>
        </a-descriptions>
        <a-divider orientation="left">价格表</a-divider>
        <div class="toolbar-row" style="margin-bottom: 12px">
          <a-button type="primary" @click="openPrice()">新增价格项</a-button>
        </div>
        <departure-price-table :items="prices" @edit="openPrice" @remove="removePrice" />
      </template>
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
          <a-form-item label="线路编码" required>
            <a-input v-model:value="routeForm.code" />
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
          <a-form-item label="团期编码" required>
            <a-input v-model:value="departureForm.code" />
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
              <a-select-option value="OPEN">OPEN</a-select-option>
              <a-select-option value="CLOSED">CLOSED</a-select-option>
              <a-select-option value="FULL">FULL</a-select-option>
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
      </a-form>
    </a-drawer>

    <a-drawer v-model:open="priceModal" :title="priceForm.id ? '编辑价格项' : '新增价格项'" placement="right" :width="720">
      <template #extra>
        <a-space>
          <a-button @click="priceModal = false">取消</a-button>
          <a-button type="primary" :loading="saving" @click="savePrice">保存</a-button>
        </a-space>
      </template>
      <a-form layout="vertical">
        <div class="grid-2">
          <a-form-item label="价格类型" required>
            <a-select v-model:value="priceForm.priceType">
              <a-select-option value="ADULT">ADULT</a-select-option>
              <a-select-option value="CHILD">CHILD</a-select-option>
              <a-select-option value="SINGLE_ROOM">SINGLE_ROOM</a-select-option>
              <a-select-option value="EXTRA">EXTRA</a-select-option>
            </a-select>
          </a-form-item>
          <a-form-item label="显示名称">
            <a-input v-model:value="priceForm.priceLabel" placeholder="如：成人价、儿童不占床" />
          </a-form-item>
        </div>
        <div class="grid-2">
          <a-form-item label="价格" required>
            <a-input-number v-model:value="priceForm.price" :min="0" style="width: 100%" />
          </a-form-item>
          <a-form-item label="币种">
            <a-input v-model:value="priceForm.currency" />
          </a-form-item>
        </div>
        <a-form-item label="说明">
          <a-textarea v-model:value="priceForm.description" :rows="2" />
        </a-form-item>
      </a-form>
    </a-drawer>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import DeparturePriceTable from '../components/product/DeparturePriceTable.vue';
import DepartureTable from '../components/product/DepartureTable.vue';
import RouteTable from '../components/product/RouteTable.vue';
import { productApi } from '../api/crm';
import { notifyError, notifySuccess } from '../utils/notify';

const activeTab = ref('routes');
const saving = ref(false);
const routeModal = ref(false);
const departureModal = ref(false);
const priceModal = ref(false);
const routeDetailOpen = ref(false);
const departureDetailOpen = ref(false);
const routeKeyword = ref('');
const departureFilterRouteId = ref<number | undefined>();

const routes = ref<any[]>([]);
const departures = ref<any[]>([]);
const prices = ref<any[]>([]);
const activeRoute = ref<any>(null);
const activeDeparture = ref<any>(null);

const routeForm = reactive({
  id: undefined as number | undefined,
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
  id: undefined as number | undefined,
  routeId: undefined as number | undefined,
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
const priceForm = reactive({
  id: undefined as number | undefined,
  priceType: 'ADULT',
  priceLabel: '',
  price: 1999,
  currency: 'CNY',
  description: ''
});

const routeMap = computed(() => Object.fromEntries(routes.value.map((item) => [item.id, item.name])));

const filteredRoutes = computed(() => {
  const keyword = routeKeyword.value.trim().toLowerCase();
  if (!keyword) return routes.value;
  return routes.value.filter((item) =>
    [item.name, item.destinationCity, item.departureCity].filter(Boolean).some((value) => String(value).toLowerCase().includes(keyword))
  );
});

const decoratedDepartures = computed(() =>
  departures.value.map((item) => ({
    ...item,
    routeName: routeMap.value[item.routeId] || '-'
  }))
);

const loadRoutes = async () => {
  const { data } = await productApi.routes();
  routes.value = data.data || [];
};

const loadDepartures = async () => {
  const { data } = await productApi.departures(departureFilterRouteId.value);
  departures.value = data.data || [];
};

const loadPrices = async (departureId: number) => {
  const { data } = await productApi.prices(departureId);
  prices.value = data.data || [];
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
  routeDetailOpen.value = true;
};

const saveRoute = async () => {
  if (!routeForm.code.trim() || !routeForm.name.trim()) return;
  saving.value = true;
  try {
    const payload = { ...routeForm };
    if (routeForm.id) {
      await productApi.updateRoute(routeForm.id, payload);
      notifySuccess('线路更新成功');
    } else {
      await productApi.createRoute(payload);
      notifySuccess('线路创建成功');
    }
    routeModal.value = false;
    await loadRoutes();
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
    await Promise.all([loadRoutes(), loadDepartures()]);
  } catch (error) {
    notifyError(error);
  }
};

const openDeparture = (record?: any) => {
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
  departureModal.value = true;
};

const openDepartureDetail = async (record: any) => {
  activeDeparture.value = record;
  await loadPrices(record.id);
  departureDetailOpen.value = true;
};

const saveDeparture = async () => {
  if (!departureForm.routeId) return;
  saving.value = true;
  try {
    const payload = { ...departureForm };
    if (departureForm.id) {
      await productApi.updateDeparture(departureForm.id, payload);
      notifySuccess('团期更新成功');
    } else {
      await productApi.createDeparture(payload);
      notifySuccess('团期创建成功');
    }
    departureModal.value = false;
    await loadDepartures();
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

const openPrice = (record?: any) => {
  if (!activeDeparture.value) return;
  priceForm.id = record?.id;
  priceForm.priceType = record?.priceType || 'ADULT';
  priceForm.priceLabel = record?.priceLabel || '';
  priceForm.price = record?.price || 1999;
  priceForm.currency = record?.currency || 'CNY';
  priceForm.description = record?.description || '';
  priceModal.value = true;
};

const savePrice = async () => {
  if (!activeDeparture.value) return;
  saving.value = true;
  try {
    const payload = {
      priceType: priceForm.priceType,
      priceLabel: priceForm.priceLabel,
      price: priceForm.price,
      currency: priceForm.currency,
      description: priceForm.description
    };
    if (priceForm.id) {
      await productApi.updatePrice(activeDeparture.value.id, priceForm.id, payload);
      notifySuccess('价格项更新成功');
    } else {
      await productApi.createPrice(activeDeparture.value.id, payload);
      notifySuccess('价格项新增成功');
    }
    priceModal.value = false;
    await loadPrices(activeDeparture.value.id);
  } catch (error) {
    notifyError(error);
  } finally {
    saving.value = false;
  }
};

const removePrice = async (record: any) => {
  if (!activeDeparture.value) return;
  try {
    await productApi.deletePrice(activeDeparture.value.id, record.id);
    notifySuccess('价格项删除成功');
    await loadPrices(activeDeparture.value.id);
  } catch (error) {
    notifyError(error);
  }
};

onMounted(async () => {
  try {
    await Promise.all([loadRoutes(), loadDepartures()]);
  } catch (error) {
    notifyError(error);
  }
});
</script>

<style scoped>
.product-page {
  display: grid;
  gap: 16px;
}

.grid-3 {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

@media (max-width: 1200px) {
  .grid-3 {
    grid-template-columns: 1fr;
  }
}
</style>
