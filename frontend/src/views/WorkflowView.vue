<template>
  <div class="workflow-page">
    <div class="biz-summary">
      <div class="item">
        <span class="label">模板总数</span>
        <strong class="value">{{ templates.length }}</strong>
      </div>
      <div class="item">
        <span class="label">启用模板</span>
        <strong class="value">{{ activeCount }}</strong>
      </div>
      <div class="item">
        <span class="label">停用模板</span>
        <strong class="value">{{ templates.length - activeCount }}</strong>
      </div>
      <div class="item">
        <span class="label">平均节点数</span>
        <strong class="value">{{ avgNodes }}</strong>
      </div>
    </div>

    <a-card class="section-card" :bordered="false">
      <template #extra>
        <a-button type="primary" @click="openCreate">新增模板</a-button>
      </template>

      <workflow-template-table :items="templates" @edit="openEdit" @remove="remove" />
    </a-card>

    <workflow-template-modal
      v-model:open="modalOpen"
      :saving="saving"
      :model="form"
      :routes="routes"
      :departures="departures"
      :roles="roleOptions"
      @save="save"
      @add-node="addNode"
      @remove-node="removeNode"
    />
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { workflowApi } from '../api/crm';
import WorkflowTemplateModal from '../components/workflow/WorkflowTemplateModal.vue';
import WorkflowTemplateTable from '../components/workflow/WorkflowTemplateTable.vue';
import { notifyError, notifySuccess } from '../utils/notify';

const templates = ref<any[]>([]);
const roleOptions = ref<Array<{ code: string; name: string }>>([]);
const routes = ref<any[]>([]);
const departures = ref<any[]>([]);
const modalOpen = ref(false);
const saving = ref(false);

const form = reactive<any>({
  id: undefined as number | undefined,
  name: '',
  orderType: 'GROUP',
  productCategory: '国内游',
  routeId: undefined as number | undefined,
  departureId: undefined as number | undefined,
  minAmount: undefined,
  maxAmount: undefined,
  active: true,
  nodes: [] as Array<{ stepOrder: number; nodeName: string; approverRoleCode: string }>
});

const activeCount = computed(() => templates.value.filter((item) => item.template?.active).length);
const avgNodes = computed(() => {
  if (!templates.value.length) return 0;
  const total = templates.value.reduce((sum, item) => sum + (item.nodes?.length || 0), 0);
  return Number((total / templates.value.length).toFixed(1));
});

const normalize = (item: any) => ({
  ...item,
  amount: `${item.template.minAmount ?? '-'} ~ ${item.template.maxAmount ?? '-'}`
});

const load = async () => {
  try {
    const [workflowRes, contextRes] = await Promise.all([
      workflowApi.list(),
      workflowApi.contextOptions()
    ]);
    templates.value = (workflowRes.data.data || []).map(normalize);
    roleOptions.value = (contextRes.data.data?.roles || []).map((item: any) => ({ code: item.code, name: item.name }));
    routes.value = contextRes.data.data?.routes || [];
    departures.value = contextRes.data.data?.departures || [];
  } catch (error) {
    notifyError(error);
  }
};

const resetForm = () => {
  form.id = undefined;
  form.name = '';
  form.orderType = 'GROUP';
  form.productCategory = '国内游';
  form.routeId = undefined;
  form.departureId = undefined;
  form.minAmount = undefined;
  form.maxAmount = undefined;
  form.active = true;
  form.nodes = [
    {
      stepOrder: 1,
      nodeName: '销售经理审批',
      approverRoleCode: roleOptions.value.find((item) => item.code === 'SALES_MANAGER')?.code || roleOptions.value[0]?.code || ''
    }
  ];
};

const openCreate = () => {
  resetForm();
  modalOpen.value = true;
};

const openEdit = (record: any) => {
  form.id = record.template.id;
  form.name = record.template.name;
  form.orderType = record.template.orderType;
  form.productCategory = record.template.productCategory;
  form.routeId = record.template.routeId;
  form.departureId = record.template.departureId;
  form.minAmount = record.template.minAmount;
  form.maxAmount = record.template.maxAmount;
  form.active = record.template.active;
  form.nodes = (record.nodes || []).map((item: any) => ({
    stepOrder: item.stepOrder,
    nodeName: item.nodeName,
    approverRoleCode: item.approverRoleCode
  }));
  modalOpen.value = true;
};

const addNode = () => {
  form.nodes.push({
    stepOrder: form.nodes.length + 1,
    nodeName: `节点${form.nodes.length + 1}`,
    approverRoleCode: roleOptions.value[0]?.code || ''
  });
};

const removeNode = (idx: number) => {
  if (form.nodes.length <= 1) {
    return;
  }
  form.nodes.splice(idx, 1);
};

const save = async () => {
  if (!form.name.trim() || !form.orderType || !form.productCategory || !form.nodes.length) {
    return;
  }
  saving.value = true;
  const payload = {
    name: form.name,
    orderType: form.orderType,
    productCategory: form.productCategory,
    routeId: form.routeId,
    departureId: form.departureId,
    minAmount: form.minAmount,
    maxAmount: form.maxAmount,
    active: form.active,
    nodes: form.nodes.map((item: any) => ({
      stepOrder: item.stepOrder,
      nodeName: item.nodeName,
      approverRoleCode: item.approverRoleCode
    }))
  };
  try {
    if (form.id) {
      await workflowApi.update(form.id, payload);
      notifySuccess('模板更新成功');
    } else {
      await workflowApi.create(payload);
      notifySuccess('模板创建成功');
    }
    modalOpen.value = false;
    await load();
  } catch (error) {
    notifyError(error);
  } finally {
    saving.value = false;
  }
};

const remove = async (id: number) => {
  try {
    await workflowApi.remove(id);
    notifySuccess('模板删除成功');
    await load();
  } catch (error) {
    notifyError(error);
  }
};

onMounted(load);
</script>

<style scoped>
.workflow-page {
  display: grid;
  gap: 10px;
}
</style>
