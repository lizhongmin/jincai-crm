<template>
  <a-drawer
    :open="open"
    :title="model.id ? '编辑订单' : '新建订单'"
    placement="right"
    width="1080"
    @update:open="(value) => emit('update:open', value)"
    @close="emit('update:open', false)"
  >
    <template #extra>
      <a-space>
        <a-button @click="emit('update:open', false)">取消</a-button>
        <a-button type="primary" :loading="saving" @click="emit('save')">保存</a-button>
      </a-space>
    </template>
    <a-form layout="vertical">
      <div class="grid-2">
        <a-form-item label="订单号">
          <a-input v-model:value="model.orderNo" placeholder="为空时自动生成" />
        </a-form-item>
        <a-form-item label="订单类型" required>
          <a-select v-model:value="model.orderType">
            <a-select-option value="GROUP">跟团游</a-select-option>
            <a-select-option value="CUSTOM">定制团</a-select-option>
          </a-select>
        </a-form-item>
      </div>

      <div class="grid-3">
        <a-form-item label="线路" required>
          <a-select v-model:value="model.routeId" placeholder="请选择线路" @change="handleRouteChange">
            <a-select-option v-for="item in routes" :key="item.id" :value="item.id">
              {{ item.name }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="团期" required>
          <a-select v-model:value="model.departureId" placeholder="请选择团期" @change="handleDepartureChange">
            <a-select-option v-for="item in routeDepartures" :key="item.id" :value="item.id">
              {{ item.code }} ({{ item.startDate }} ~ {{ item.endDate }})
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="产品分类">
          <a-input :value="selectedRoute?.category || model.productCategory || '-'" disabled />
        </a-form-item>
      </div>

      <div class="grid-3">
        <a-form-item label="客户" required>
          <a-select v-model:value="model.customerId" placeholder="请选择客户" @change="handleCustomerChange">
            <a-select-option v-for="item in customers" :key="item.id" :value="item.id">
              {{ item.name }} ({{ item.phone }})
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="出行人">
          <a-select
            :value="selectedTravelerIds"
            mode="multiple"
            placeholder="请选择该客户下的出行人"
            :options="travelerOptions"
            @change="handleTravelerIdsChange"
          />
        </a-form-item>
        <a-form-item label="实时报价">
          <a-input :value="quote ? `${quote.totalAmount} ${quote.currency}` : '请选择出行人后自动计算'" disabled />
        </a-form-item>
      </div>

      <a-card size="small" title="团期价格表" class="inner-card">
        <a-empty v-if="!prices.length" description="先选择团期后加载价格表" />
        <a-table v-else :columns="priceColumns" :data-source="prices" row-key="id" :pagination="false" size="small" />
      </a-card>

      <a-card size="small" title="出行人价格配置（按年龄自动匹配）" class="inner-card">
        <a-empty v-if="!model.travelerSelections.length" description="先选择出行人，系统会按出生日期自动匹配价格类型" />
        <div v-else class="selection-list">
          <div v-for="item in model.travelerSelections" :key="item.travelerId" class="selection-row">
            <a-input :value="travelerName(item.travelerId)" disabled />
            <a-input :value="travelerAgeText(item.travelerId)" disabled />
            <a-input :value="travelerMatchedPriceText(item.travelerId)" disabled />
          </div>
        </div>
      </a-card>

      <a-card size="small" title="附加收费项" class="inner-card">
        <div class="toolbar-row" style="margin-bottom: 10px">
          <a-button @click="addExtraSelection">新增收费项</a-button>
        </div>
        <a-empty v-if="!model.extraSelections.length" description="如单房差、附加包等可在此添加" />
        <div v-else class="selection-list">
          <div v-for="(item, index) in model.extraSelections" :key="index" class="selection-row extra-row">
            <a-select
              v-model:value="item.departurePriceId"
              placeholder="请选择价格项"
              @change="emit('request-quote')"
            >
              <a-select-option v-for="price in prices" :key="price.id" :value="price.id">
                {{ priceLabel(price) }} - {{ price.price }} {{ currencyLabel(price.currency) }}
              </a-select-option>
            </a-select>
            <a-input-number v-model:value="item.quantity" :min="1" style="width: 120px" @change="emit('request-quote')" />
            <a-button danger @click="removeExtraSelection(index)">删除</a-button>
          </div>
        </div>
      </a-card>

      <a-card size="small" title="报价明细" class="inner-card">
        <a-empty v-if="!quote?.priceItems?.length" description="配置完成后自动生成报价" />
        <template v-else>
          <a-table :columns="quoteColumns" :data-source="quote.priceItems" row-key="id" :pagination="false" size="small" />
          <div class="quote-summary">
            出行人数：{{ quote.travelerCount }} 人
            <span class="quote-total">总价：{{ quote.totalAmount }} {{ quote.currency }}</span>
          </div>
        </template>
      </a-card>
    </a-form>
  </a-drawer>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { CURRENCY_LABEL_MAP, PRICE_TYPE_LABEL_MAP, enumLabel } from '../../constants/display';

const props = defineProps<{
  open: boolean;
  saving: boolean;
  model: any;
  customers: any[];
  routes: any[];
  departures: any[];
  travelers: any[];
  prices: any[];
  quote: any | null;
}>();

const emit = defineEmits<{
  (e: 'update:open', value: boolean): void;
  (e: 'save'): void;
  (e: 'customer-change', customerId?: string): void;
  (e: 'departure-change', departureId?: string): void;
  (e: 'request-quote'): void;
}>();

const routeDepartures = computed(() => props.departures.filter((item) => item.routeId === props.model.routeId));
const selectedRoute = computed(() => props.routes.find((item) => item.id === props.model.routeId));
const selectedTravelerIds = computed(() => props.model.travelerSelections.map((item: any) => item.travelerId));
const travelerOptions = computed(() => props.travelers.map((item) => ({ label: `${item.name}${item.phone ? ` (${item.phone})` : ''}`, value: item.id })));
const travelerQuoteMap = computed(() => {
  const map = new Map<string, any>();
  (props.quote?.priceItems || []).forEach((item: any) => {
    if (item.travelerId) {
      map.set(item.travelerId, item);
    }
  });
  return map;
});

const priceColumns = [
  { title: '价格类型', dataIndex: 'priceType', width: 140, customRender: ({ text }: any) => enumLabel(PRICE_TYPE_LABEL_MAP, text) },
  { title: '显示名称', dataIndex: 'priceLabel', width: 180 },
  { title: '价格', dataIndex: 'price', width: 120 },
  { title: '说明', dataIndex: 'description' }
];

const quoteColumns = [
  { title: '出行人', dataIndex: 'travelerName', width: 140, customRender: ({ text }: any) => text || '附加项' },
  { title: '价格项', dataIndex: 'itemName', width: 180 },
  { title: '单价', dataIndex: 'unitPrice', width: 120 },
  { title: '数量', dataIndex: 'quantity', width: 100 },
  { title: '金额', dataIndex: 'amount', width: 120 }
];

const travelerName = (travelerId: string) => props.travelers.find((item) => item.id === travelerId)?.name || `出行人#${travelerId}`;
const priceLabel = (price: any) => price.priceLabel || enumLabel(PRICE_TYPE_LABEL_MAP, price.priceType);
const currencyLabel = (value?: string) => enumLabel(CURRENCY_LABEL_MAP, value);
const travelerAgeText = (travelerId: string) => {
  const traveler = props.travelers.find((item) => item.id === travelerId);
  if (!traveler?.birthday) return '年龄：未填写生日';
  const [year, month, day] = String(traveler.birthday).split('-').map((value) => Number(value));
  if (!year || !month || !day) return '年龄：生日格式无效';
  const departure = props.departures.find((item) => item.id === props.model.departureId);
  const baseDate = departure?.startDate ? new Date(departure.startDate) : new Date();
  let age = baseDate.getFullYear() - year;
  const hasBirthdayPassed = (baseDate.getMonth() + 1 > month)
    || (baseDate.getMonth() + 1 === month && baseDate.getDate() >= day);
  if (!hasBirthdayPassed) {
    age -= 1;
  }
  return age < 0 ? '年龄：生日无效' : `年龄：${age}岁`;
};
const travelerMatchedPriceText = (travelerId: string) => {
  const priceItem = travelerQuoteMap.value.get(travelerId);
  if (!priceItem) return '待匹配价格';
  return `${priceItem.itemName} / ${priceItem.unitPrice} ${currencyLabel(priceItem.currency)}`;
};

const handleRouteChange = () => {
  if (!routeDepartures.value.some((item) => item.id === props.model.departureId)) {
    props.model.departureId = routeDepartures.value[0]?.id;
  }
  handleDepartureChange();
};

const handleDepartureChange = () => {
  props.model.travelerSelections = props.model.travelerSelections.map((item: any) => ({ ...item, departurePriceId: undefined }));
  props.model.extraSelections = [];
  emit('departure-change', props.model.departureId);
};

const handleCustomerChange = () => {
  props.model.travelerSelections = [];
  emit('customer-change', props.model.customerId);
  emit('request-quote');
};

const handleTravelerIdsChange = (travelerIds: string[]) => {
  const currentMap = new Map(props.model.travelerSelections.map((item: any) => [item.travelerId, item]));
  props.model.travelerSelections = travelerIds.map((travelerId) => currentMap.get(travelerId) || {
    travelerId,
    departurePriceId: undefined
  });
  emit('request-quote');
};

const addExtraSelection = () => {
  props.model.extraSelections.push({ departurePriceId: undefined, quantity: 1 });
};

const removeExtraSelection = (index: number) => {
  props.model.extraSelections.splice(index, 1);
  emit('request-quote');
};
</script>

<style scoped>
.inner-card {
  margin-top: 10px;
}

.selection-list {
  display: grid;
  gap: 10px;
}

.selection-row {
  display: grid;
  grid-template-columns: 220px 180px 1fr;
  gap: 12px;
}

.extra-row {
  grid-template-columns: 1fr 120px 80px;
}

.quote-summary {
  margin-top: 12px;
  display: flex;
  justify-content: space-between;
  font-weight: 600;
}

.quote-total {
  color: #cf1322;
}

.grid-3 {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

@media (max-width: 1200px) {
  .grid-3,
  .selection-row,
  .extra-row {
    grid-template-columns: 1fr;
  }
}
</style>
