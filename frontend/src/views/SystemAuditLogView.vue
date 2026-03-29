<template>
  <div class="audit-log-manage">
    <a-card class="section-card" :bordered="false">
      <template #title>
        <div class="card-title">
          <span class="title-text">API审计日志</span>
        </div>
      </template>

      <div class="toolbar">
        <a-form layout="inline" :model="queryForm" class="query-form">
          <a-form-item>
            <a-input
              v-model:value="queryForm.keyword"
              allow-clear
              placeholder="搜索 Trace ID / URL / 类方法"
              style="width: 280px"
              @pressEnter="handleSearch"
            />
          </a-form-item>
          <a-form-item>
            <a-input
              v-model:value="queryForm.operator"
              allow-clear
              placeholder="执行人"
              style="width: 140px"
              @pressEnter="handleSearch"
            />
          </a-form-item>
          <a-form-item>
            <a-select
              v-model:value="queryForm.httpMethod"
              allow-clear
              placeholder="请求方式"
              style="width: 140px"
              :options="httpMethodOptions"
            />
          </a-form-item>
          <a-form-item>
            <a-range-picker
              v-model:value="dateRange"
              show-time
              value-format="YYYY-MM-DDTHH:mm:ss"
              @change="handleDateChange"
            />
          </a-form-item>
          <a-form-item>
            <a-space>
              <a-button type="primary" @click="handleSearch">查询</a-button>
              <a-button @click="handleReset">重置</a-button>
            </a-space>
          </a-form-item>
        </a-form>
      </div>

      <a-table
        :columns="columns"
        :data-source="logList"
        :row-key="record => record.id"
        :pagination="pagination"
        :loading="loading"
        :scroll="{ x: 1420 }"
        bordered
        size="middle"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.dataIndex === 'traceId'">
            <a-tooltip :title="record.traceId">
              <div class="ellipsis-cell">{{ record.traceId || '-' }}</div>
            </a-tooltip>
          </template>

          <template v-else-if="column.dataIndex === 'requestUrl'">
            <a-tooltip :title="record.requestUrl">
              <div class="ellipsis-cell url-cell">{{ record.requestUrl || '-' }}</div>
            </a-tooltip>
          </template>

          <template v-else-if="column.dataIndex === 'operator'">
            {{ record.createdBy || '-' }}
          </template>

          <template v-else-if="column.dataIndex === 'httpStatus'">
            <a-tag :color="getHttpStatusColor(record.httpStatus)">
              {{ record.httpStatus || '-' }}
            </a-tag>
          </template>

          <template v-else-if="column.dataIndex === 'timeConsuming'">
            <span :class="{ 'text-danger': Number(record.timeConsuming) > 1000 }">
              {{ record.timeConsuming ?? '-' }} ms
            </span>
          </template>

          <template v-else-if="column.dataIndex === 'createdAt'">
            {{ formatDateTime(record.createdAt) }}
          </template>

          <template v-else-if="column.dataIndex === 'action'">
            <a-button type="link" size="small" @click="openDetail(record)">详情</a-button>
          </template>
        </template>
      </a-table>
    </a-card>

    <a-drawer
      v-model:open="detailVisible"
      title="审计日志详情"
      width="720"
      destroy-on-close
    >
      <a-descriptions :column="1" bordered size="small">
        <a-descriptions-item label="Trace ID">{{ activeRecord?.traceId || '-' }}</a-descriptions-item>
        <a-descriptions-item label="执行人">{{ activeRecord?.createdBy || '-' }}</a-descriptions-item>
        <a-descriptions-item label="请求方式">{{ activeRecord?.httpMethod || '-' }}</a-descriptions-item>
        <a-descriptions-item label="响应状态">
          <a-tag :color="getHttpStatusColor(activeRecord?.httpStatus)">
            {{ activeRecord?.httpStatus || '-' }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="请求URL">{{ activeRecord?.requestUrl || '-' }}</a-descriptions-item>
        <a-descriptions-item label="来源IP">{{ activeRecord?.sourceIp || '-' }}</a-descriptions-item>
        <a-descriptions-item label="类与方法">{{ activeRecord?.classMethod || '-' }}</a-descriptions-item>
        <a-descriptions-item label="耗时">{{ activeRecord?.timeConsuming ?? '-' }} ms</a-descriptions-item>
        <a-descriptions-item label="请求时间">{{ formatDateTime(activeRecord?.createdAt) }}</a-descriptions-item>
      </a-descriptions>

      <div class="detail-block">
        <div class="detail-title">请求参数</div>
        <pre class="detail-json">{{ formatJson(activeRecord?.requestArgs) }}</pre>
      </div>

      <div class="detail-block">
        <div class="detail-title">响应结果</div>
        <pre class="detail-json">{{ formatJson(activeRecord?.responseResult) }}</pre>
      </div>
    </a-drawer>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { auditApi } from '../api/crm';
import { notifyError } from '../utils/notify';
import dayjs from 'dayjs';

const loading = ref(false);
const logList = ref<any[]>([]);
const detailVisible = ref(false);
const activeRecord = ref<any | null>(null);
const dateRange = ref<[string, string] | []>([]);

const queryForm = ref({
  keyword: '',
  operator: '',
  httpMethod: undefined as string | undefined,
  startTime: undefined as string | undefined,
  endTime: undefined as string | undefined
});

const pagination = ref({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  pageSizeOptions: ['10', '20', '50'],
  showTotal: (total: number) => `共 ${total} 条`
});

const columns = [
  { title: 'Trace ID', dataIndex: 'traceId', width: 240 },
  { title: '请求方式', dataIndex: 'httpMethod', width: 100 },
  { title: '状态码', dataIndex: 'httpStatus', width: 90 },
  { title: '请求URL', dataIndex: 'requestUrl', width: 280 },
  { title: '执行人', dataIndex: 'operator', width: 140 },
  { title: '来源IP', dataIndex: 'sourceIp', width: 150 },
  { title: '类与方法', dataIndex: 'classMethod', width: 260 },
  { title: '耗时(ms)', dataIndex: 'timeConsuming', width: 110 },
  { title: '请求时间', dataIndex: 'createdAt', width: 180 },
  { title: '操作', dataIndex: 'action', width: 90, fixed: 'right' as const }
];

const httpMethodOptions = [
  { label: 'GET', value: 'GET' },
  { label: 'POST', value: 'POST' },
  { label: 'PUT', value: 'PUT' },
  { label: 'PATCH', value: 'PATCH' },
  { label: 'DELETE', value: 'DELETE' }
];

const buildParams = () => ({
  page: pagination.value.current,
  size: pagination.value.pageSize,
  keyword: queryForm.value.keyword.trim() || undefined,
  operator: queryForm.value.operator.trim() || undefined,
  httpMethod: queryForm.value.httpMethod,
  startTime: queryForm.value.startTime,
  endTime: queryForm.value.endTime
});

const loadData = async () => {
  loading.value = true;
  try {
    const res = await auditApi.apiLogsPage(buildParams());
    if (res.data.success) {
      logList.value = res.data.data?.items || [];
      pagination.value.total = Number(res.data.data?.total || 0);
    } else {
      notifyError('加载审计日志失败', res.data.message);
    }
  } catch (error: any) {
    notifyError('加载审计日志失败', error.message);
  } finally {
    loading.value = false;
  }
};

const handleSearch = () => {
  pagination.value.current = 1;
  loadData();
};

const handleReset = () => {
  queryForm.value.keyword = '';
  queryForm.value.operator = '';
  queryForm.value.httpMethod = undefined;
  queryForm.value.startTime = undefined;
  queryForm.value.endTime = undefined;
  dateRange.value = [];
  pagination.value.current = 1;
  loadData();
};

const handleDateChange = (values: string[] | null) => {
  queryForm.value.startTime = values?.[0] || undefined;
  queryForm.value.endTime = values?.[1] || undefined;
};

const handleTableChange = (pag: any) => {
  pagination.value.current = pag.current;
  pagination.value.pageSize = pag.pageSize;
  loadData();
};

const openDetail = (record: any) => {
  activeRecord.value = record;
  detailVisible.value = true;
};

const formatDateTime = (val?: string) => {
  if (!val) return '-';
  return dayjs(val).format('YYYY-MM-DD HH:mm:ss');
};

const getHttpStatusColor = (status?: number) => {
  if (!status) return 'default';
  if (status >= 200 && status < 300) return 'success';
  if (status >= 300 && status < 400) return 'processing';
  if (status >= 400 && status < 500) return 'warning';
  return 'error';
};

const formatJson = (value?: string) => {
  if (!value) return '-';
  try {
    return JSON.stringify(JSON.parse(value), null, 2);
  } catch {
    return value;
  }
};

onMounted(() => {
  loadData();
});
</script>

<style scoped>
.audit-log-manage {
  height: 100%;
}

.section-card {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.section-card :deep(.ant-card-body) {
  flex: 1;
  padding: 16px;
  overflow: auto;
}

.card-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.title-text {
  font-size: 16px;
  font-weight: bold;
}

.toolbar {
  margin-bottom: 16px;
}

.query-form {
  row-gap: 12px;
}

.ellipsis-cell {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.url-cell {
  max-width: 260px;
}

.text-danger {
  color: #ff4d4f;
  font-weight: bold;
}

.detail-block {
  margin-top: 16px;
}

.detail-title {
  margin-bottom: 8px;
  font-size: 14px;
  font-weight: 600;
  color: #1f2937;
}

.detail-json {
  margin: 0;
  padding: 12px;
  overflow: auto;
  color: #111827;
  white-space: pre-wrap;
  word-break: break-all;
  background: #f8fafc;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
}
</style>
