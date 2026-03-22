<template>
  <div class="security-page">
    <a-card class="section-card" :bordered="false">
      <template #title>登录安全策略</template>
      <a-form layout="vertical">
        <a-row :gutter="16">
          <a-col :xs="24" :md="8">
            <a-form-item label="失败N次后要求图形验证码">
              <a-input-number v-model:value="policyForm.captchaAfterFailures" :min="1" :max="50" style="width: 100%" />
            </a-form-item>
          </a-col>
          <a-col :xs="24" :md="8">
            <a-form-item label="失败M次后锁定账号">
              <a-input-number v-model:value="policyForm.lockAfterFailures" :min="2" :max="100" style="width: 100%" />
            </a-form-item>
          </a-col>
          <a-col :xs="24" :md="8">
            <a-form-item label="锁定时长X（分钟）">
              <a-input-number v-model:value="policyForm.lockMinutes" :min="1" :max="1440" style="width: 100%" />
            </a-form-item>
          </a-col>
        </a-row>

        <a-row :gutter="16">
          <a-col :xs="24" :md="8">
            <a-form-item label="验证码有效期（秒）">
              <a-input-number v-model:value="policyForm.captchaExpireSeconds" :min="60" :max="1800" style="width: 100%" />
            </a-form-item>
          </a-col>
        </a-row>

        <a-alert
          type="info"
          show-icon
          :message="`当前策略：失败 ${policyForm.captchaAfterFailures} 次后校验验证码，失败 ${policyForm.lockAfterFailures} 次锁定 ${policyForm.lockMinutes} 分钟。`"
          class="tip-alert"
        />

        <div class="actions">
          <a-button v-if="canEditPolicy" type="primary" :loading="savingPolicy" @click="savePolicy">保存策略</a-button>
        </div>
      </a-form>
    </a-card>

    <a-card class="section-card" :bordered="false">
      <template #title>密码安全策略</template>
      <a-form layout="vertical">
        <a-row :gutter="16">
          <a-col :xs="24" :md="8">
            <a-form-item label="最小长度">
              <a-input-number v-model:value="policyForm.passwordMinLength" :min="6" :max="32" style="width: 100%" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :xs="24" :md="6">
            <a-form-item label="至少一个大写字母">
              <a-switch v-model:checked="policyForm.passwordRequireUppercase" />
            </a-form-item>
          </a-col>
          <a-col :xs="24" :md="6">
            <a-form-item label="至少一个小写字母">
              <a-switch v-model:checked="policyForm.passwordRequireLowercase" />
            </a-form-item>
          </a-col>
          <a-col :xs="24" :md="6">
            <a-form-item label="至少一个数字">
              <a-switch v-model:checked="policyForm.passwordRequireDigit" />
            </a-form-item>
          </a-col>
          <a-col :xs="24" :md="6">
            <a-form-item label="至少一个特殊字符">
              <a-switch v-model:checked="policyForm.passwordRequireSpecial" />
            </a-form-item>
          </a-col>
        </a-row>

        <a-alert type="warning" show-icon class="tip-alert" :message="passwordPolicyHint" />

        <div class="actions">
          <a-button v-if="canEditPolicy" type="primary" :loading="savingPolicy" @click="savePolicy">保存策略</a-button>
        </div>
      </a-form>
    </a-card>

  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { securityApi } from '../api/crm';
import { useAuthStore } from '../stores/auth';
import { hasAnyRole } from '../utils/role';
import { notifyError, notifySuccess } from '../utils/notify';

const savingPolicy = ref(false);
const auth = useAuthStore();

const policyForm = reactive({
  captchaAfterFailures: 3,
  lockAfterFailures: 5,
  lockMinutes: 30,
  captchaExpireSeconds: 300,
  passwordMinLength: 8,
  passwordRequireUppercase: true,
  passwordRequireLowercase: true,
  passwordRequireDigit: true,
  passwordRequireSpecial: false
});

const canEditPolicy = computed(() => hasAnyRole(auth.profile?.roles, ['ADMIN']));

const passwordPolicyHint = computed(() => {
  const requirements = [`长度不少于 ${policyForm.passwordMinLength}`];
  if (policyForm.passwordRequireUppercase) requirements.push('至少 1 个大写字母');
  if (policyForm.passwordRequireLowercase) requirements.push('至少 1 个小写字母');
  if (policyForm.passwordRequireDigit) requirements.push('至少 1 个数字');
  if (policyForm.passwordRequireSpecial) requirements.push('至少 1 个特殊字符');
  return `密码规则：${requirements.join('，')}。`;
});

const loadPolicy = async () => {
  try {
    const { data } = await securityApi.loginPolicy();
    if (data.success && data.data) {
      Object.assign(policyForm, data.data);
    }
  } catch (error) {
    notifyError(error);
  }
};

const savePolicy = async () => {
  if (!canEditPolicy.value) {
    notifyError(new Error('当前账号无权修改登录安全策略'));
    return;
  }
  if (policyForm.lockAfterFailures <= policyForm.captchaAfterFailures) {
    notifyError(new Error('锁定阈值必须大于验证码阈值'));
    return;
  }
  savingPolicy.value = true;
  try {
    const payload = { ...policyForm };
    const { data } = await securityApi.updateLoginPolicy(payload);
    if (data.success && data.data) {
      Object.assign(policyForm, data.data);
    }
    notifySuccess('登录与密码策略已更新');
  } catch (error) {
    notifyError(error);
  } finally {
    savingPolicy.value = false;
  }
};

onMounted(loadPolicy);
</script>

<style scoped>
.security-page {
  display: grid;
  gap: 10px;
}

.tip-alert {
  margin-bottom: 12px;
}

.actions {
  display: flex;
  justify-content: flex-end;
}
</style>
