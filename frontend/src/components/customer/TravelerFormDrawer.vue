<template>
  <a-drawer
    :open="open"
    :title="form.id ? '编辑出行人' : '新增出行人'"
    placement="right"
    :width="720"
    @update:open="emit('update:open', $event)"
  >
    <template #extra>
      <a-space>
        <a-button @click="emit('update:open', false)">取消</a-button>
        <a-button type="primary" :loading="saving" @click="handleSave">保存</a-button>
      </a-space>
    </template>
    <a-form layout="vertical">
      <a-divider orientation="left">基本信息</a-divider>
      <a-form-item label="所属客户" required>
        <a-select v-model:value="form.customerId" placeholder="请选择客户">
          <a-select-option v-for="item in customers" :key="item.id" :value="item.id">
            {{ item.name }}
          </a-select-option>
        </a-select>
      </a-form-item>
      <div class="grid-2">
        <a-form-item label="姓名" required>
          <a-input v-model:value="form.name" />
        </a-form-item>
        <a-form-item label="手机号" required>
          <a-input v-model:value="form.phone" />
        </a-form-item>
      </div>
      <div class="grid-2">
        <a-form-item label="身份证号码" required>
          <a-input v-model:value="form.documents[0].docNo" placeholder="请输入身份证号码" />
        </a-form-item>
        <a-form-item label="出生日期（身份证自动识别）">
          <a-input :value="resolvedBirthday || '-'" disabled />
        </a-form-item>
      </div>
      <div class="grid-2">
        <a-form-item label="性别（身份证自动识别）">
          <a-input :value="resolvedGenderText" disabled />
        </a-form-item>
        <a-form-item label="民族">
          <a-select v-model:value="form.ethnicity" show-search allow-clear placeholder="请选择民族">
            <a-select-option v-for="item in ethnicityOptions" :key="item" :value="item">{{ item }}</a-select-option>
          </a-select>
        </a-form-item>
      </div>
      <div class="grid-2">
        <a-form-item label="国籍">
          <a-input v-model:value="form.nationality" />
        </a-form-item>
        <a-form-item label="联系地址">
          <a-input v-model:value="form.address" />
        </a-form-item>
      </div>
      <div class="grid-2">
        <a-form-item label="紧急联系人">
          <a-input v-model:value="form.emergencyContact" />
        </a-form-item>
        <a-form-item label="紧急联系电话">
          <a-input v-model:value="form.emergencyPhone" />
        </a-form-item>
      </div>
      <a-form-item label="备注偏好">
        <a-textarea v-model:value="form.preferences" :rows="2" />
      </a-form-item>

      <a-divider orientation="left">其他证件信息（选填）</a-divider>
      <div class="toolbar-row" style="margin-bottom: 8px">
        <a-button @click="addDocument">新增证件</a-button>
      </div>
      <div class="document-list">
        <div v-for="(doc, index) in form.documents.slice(1)" :key="index + 1" class="document-row">
          <a-select v-model:value="form.documents[index + 1].docType" placeholder="证件类型">
            <a-select-option v-for="item in extraIdTypeOptions" :key="item.value" :value="item.value">
              {{ item.label }}
            </a-select-option>
          </a-select>
          <a-input v-model:value="form.documents[index + 1].docNo" placeholder="证件号码" />
          <a-button danger @click="removeDocument(index + 1)">删除</a-button>
        </div>
      </div>
    </a-form>
  </a-drawer>
</template>

<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue';
import { customerApi } from '../../api/crm';
import { notifyError, notifySuccess } from '../../utils/notify';

interface CustomerOption {
  id: string;
  name: string;
}

interface DocumentItem {
  docType: string;
  docNo: string;
}

const props = defineProps<{
  open: boolean;
  record?: any;
  customers: CustomerOption[];
  defaultCustomerId?: number;
}>();

const emit = defineEmits<{
  (e: 'update:open', value: boolean): void;
  (e: 'success'): void;
}>();

const saving = ref(false);

const form = reactive({
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
  documents: [{ docType: 'ID_CARD', docNo: '' }] as DocumentItem[]
});

const idTypeOptions = [
  { label: '身份证', value: 'ID_CARD' },
  { label: '护照', value: 'PASSPORT' },
  { label: '港澳通行证', value: 'HK_MACAO_PASS' },
  { label: '台胞证', value: 'TAIWAN_PASS' },
  { label: '军官证', value: 'MILITARY_ID' },
  { label: '其他', value: 'OTHER' }
];

const extraIdTypeOptions = idTypeOptions.filter(item => item.value !== 'ID_CARD');

const ethnicityOptions = [
  '汉族', '蒙古族', '回族', '藏族', '维吾尔族', '苗族', '彝族', '壮族', '布依族', '朝鲜族', '满族', '侗族',
  '瑶族', '白族', '土家族', '哈尼族', '哈萨克族', '傣族', '黎族', '傈僳族', '佤族', '畲族', '高山族', '拉祜族',
  '水族', '东乡族', '纳西族', '景颇族', '柯尔克孜族', '土族', '达斡尔族', '仫佬族', '羌族', '布朗族', '撒拉族',
  '毛南族', '仡佬族', '锡伯族', '阿昌族', '普米族', '塔吉克族', '怒族', '乌孜别克族', '俄罗斯族', '鄂温克族',
  '德昂族', '保安族', '裕固族', '京族', '塔塔尔族', '独龙族', '鄂伦春族', '赫哲族', '门巴族', '珞巴族', '基诺族'
];

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

const resolvedBirthday = computed(() => {
  const idCardDoc = form.documents.find(item => item.docType === 'ID_CARD' && item.docNo.trim());
  return parseBirthdayFromIdCard(idCardDoc?.docNo) || form.birthday || '';
});

const resolvedGender = computed(() => {
  const idCardDoc = form.documents.find(item => item.docType === 'ID_CARD' && item.docNo.trim());
  if (idCardDoc?.docNo) {
    const idNo = idCardDoc.docNo.trim();
    if (/^\d{17}[\dX]$/i.test(idNo)) {
      return Number(idNo[16]) % 2 === 1 ? 'MALE' : 'FEMALE';
    } else if (/^\d{15}$/.test(idNo)) {
      return Number(idNo[14]) % 2 === 1 ? 'MALE' : 'FEMALE';
    }
  }
  return form.gender || '';
});

const resolvedGenderText = computed(() => {
  if (resolvedGender.value === 'MALE') return '男';
  if (resolvedGender.value === 'FEMALE') return '女';
  return '-';
});

// 当 open 打开时，初始化表单数据
watch(() => props.open, (val) => {
  if (!val) return;
  const record = props.record;
  form.id = record?.id;
  form.customerId = record?.customerId || props.defaultCustomerId;
  form.name = record?.name || '';
  form.birthday = record?.birthday || '';
  form.gender = record?.gender || undefined;
  form.ethnicity = record?.ethnicity || '';
  form.nationality = record?.nationality || '中国';
  form.address = record?.address || '';
  form.phone = record?.phone || '';
  form.emergencyContact = record?.emergencyContact || '';
  form.emergencyPhone = record?.emergencyPhone || '';
  form.preferences = record?.preferences || '';

  let documents: DocumentItem[] = [];
  if (Array.isArray(record?.documents) && record.documents.length) {
    documents = record.documents.map((doc: any) => ({
      docType: normalizeDocType(doc.docType || 'ID_CARD'),
      docNo: doc.docNo || ''
    }));
  } else {
    documents = [{
      docType: normalizeDocType(record?.idType || 'ID_CARD'),
      docNo: record?.idNo || ''
    }];
  }

  // 确保第一项始终为身份证
  const idCardIndex = documents.findIndex(doc => doc.docType === 'ID_CARD');
  if (idCardIndex > 0) {
    const idCardDoc = documents.splice(idCardIndex, 1)[0];
    documents.unshift(idCardDoc);
  } else if (idCardIndex === -1) {
    documents.unshift({ docType: 'ID_CARD', docNo: '' });
  }

  form.documents = documents;
});

const addDocument = () => {
  form.documents.push({ docType: 'PASSPORT', docNo: '' });
};

const removeDocument = (index: number) => {
  if (index === 0) return; // 不允许删除主证件（身份证）
  form.documents.splice(index, 1);
};

const handleSave = async () => {
  if (!form.customerId || !form.name.trim()) {
    notifyError(new Error('请填写出行人姓名并选择所属客户'));
    return;
  }
  if (!form.phone.trim()) {
    notifyError(new Error('请填写出行人手机号'));
    return;
  }
  const primaryDoc = form.documents[0];
  if (!primaryDoc || !primaryDoc.docNo.trim()) {
    notifyError(new Error('请填写身份证号码'));
    return;
  }

  // 强制设置主证件类型为身份证
  primaryDoc.docType = 'ID_CARD';

  const birthdayFromIdCard = parseBirthdayFromIdCard(primaryDoc.docNo);
  if (!birthdayFromIdCard) {
    notifyError(new Error('身份证号格式不正确，无法识别出生日期'));
    return;
  }

  const validDocuments = form.documents
    .map(item => ({ docType: normalizeDocType(item.docType), docNo: item.docNo?.trim() }))
    .filter(item => item.docType && item.docNo);

  saving.value = true;
  try {
    const payload = {
      name: form.name,
      birthday: birthdayFromIdCard,
      gender: resolvedGender.value,
      ethnicity: form.ethnicity,
      nationality: form.nationality,
      address: form.address,
      phone: form.phone,
      emergencyContact: form.emergencyContact,
      emergencyPhone: form.emergencyPhone,
      preferences: form.preferences,
      documents: validDocuments
    };
    if (form.id) {
      await customerApi.updateTraveler(form.id, payload);
      notifySuccess('出行人更新成功');
    } else {
      await customerApi.createTraveler(form.customerId!, payload);
      notifySuccess('出行人新增成功');
    }
    emit('update:open', false);
    emit('success');
  } catch (error) {
    notifyError(error);
  } finally {
    saving.value = false;
  }
};
</script>

<style scoped>
.grid-2 {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.toolbar-row {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.document-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.document-row {
  display: grid;
  grid-template-columns: 160px 1fr auto;
  gap: 8px;
  align-items: center;
}
</style>
