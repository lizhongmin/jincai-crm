<template>
  <div class="audit-log-manage">
    <a-card class="section-card" :bordered="false">
      <template #title>
        <div class="card-title">
          <span class="title-text">API审计日志</span>
        </div>
      </template>

      <a-table
        :columns="columns"
        :data-source="logList"
        :row-key="record => record.id"
        :pagination="pagination"
        :loading="loading"
        @change="handleTableChange"
        bordered
        size="middle"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.dataIndex === 'requestUrl'">
            <div style="word-break: break-all; max-width: 300px;">{{ record.requestUrl }}</div>
          </template>
          <template v-if="column.dataIndex === 'requestArgs'">
            <a-tooltip :title="record.requestArgs">
              <div style="max-width: 200px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;">
                {{ record.requestArgs }}
              </div>
            </a-tooltip>
          </template>
          <template v-if="column.dataIndex === 'responseResult'">
            <a-tooltip :title="record.responseResult">
              <div style="max-width: 200px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;">
                {{ record.responseResult }}
              </div>
            </a-tooltip>
          </template>
          <template v-if="column.dataIndex === 'timeConsuming'">
            <span :class="{'text-danger': record.timeConsuming > 1000}">{{ record.timeConsuming }} ms</span>
          </template>
          <template v-if="column.dataIndex === 'createdAt'">
            {{ formatDateTime(record.createdAt) }}
          </template>
        </template>
      </a-table>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { auditApi } from '../api/crm';
import { notifyError } from '../utils/notify';
import dayjs from 'dayjs';

const loading = ref(false);
const logList = ref<any[]>([]);
const pagination = ref({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showTotal: (total: number) => `共 ${total} 条`
});

const columns = [
  { title: 'Trace ID', dataIndex: 'traceId', width: 280 },
  { title: '请求方式', dataIndex: 'httpMethod', width: 100 },
  { title: '请求URL', dataIndex: 'requestUrl', width: 250 },
  { title: '来源IP', dataIndex: 'sourceIp', width: 150 },
  { title: '类与方法', dataIndex: 'classMethod', width: 250 },
  { title: '耗时(ms)', dataIndex: 'timeConsuming', width: 100 },
  { title: '请求参数', dataIndex: 'requestArgs', width: 200 },
  { title: '响应结果', dataIndex: 'responseResult', width: 200 },
  { title: '请求时间', dataIndex: 'createdAt', width: 180 }
];

const loadData = async () => {
  loading.value = true;
  try {
    const res = await auditApi.apiLogsPage({
      page: pagination.value.current,
      size: pagination.value.pageSize
    });
    if (res.data.code === 200) {
      logList.value = res.data.data.items || res.data.data.records;
      pagination.value.total = res.data.data.total;
    } else {
      notifyError('加载审计日志失败', res.data.message);
    }
  } catch (error: any) {
    notifyError('加载审计日志失败', error.message);
  } finally {
    loading.value = false;
  }
};

const handleTableChange = (pag: any) => {
  pagination.value.current = pag.current;
  pagination.value.pageSize = pag.pageSize;
  loadData();
};

const formatDateTime = (val?: string) => {
  if (!val) return '-';
  return dayjs(val).format('YYYY-MM-DD HH:mm:ss');
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
.text-danger {
  color: #ff4d4f;
  font-weight: bold;
}
</style>
