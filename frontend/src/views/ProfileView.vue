<template>
  <div class="profile-container">
    <a-card class="section-card" :bordered="false" title="基本信息">
      <a-descriptions :column="{ xs: 1, sm: 1, md: 1 }" bordered>
        <a-descriptions-item label="用户名">
          {{ profile.username }}
        </a-descriptions-item>
        <a-descriptions-item label="姓名">
          {{ profile.name }}
        </a-descriptions-item>
        <a-descriptions-item label="邮箱">
          {{ profile.email || '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="手机号">
          {{ profile.phone || '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="部门">
          {{ profile.departmentName || '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="角色">
          <a-tag v-for="role in profile.roles" :key="role.id" color="blue">
            {{ role.name }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="创建时间">
          {{ profile.createdAt ? formatDate(profile.createdAt) : '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="最后更新">
          {{ profile.updatedAt ? formatDate(profile.updatedAt) : '-' }}
        </a-descriptions-item>
      </a-descriptions>
    </a-card>

    <a-card class="section-card" :bordered="false" title="修改密码">
      <a-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef" layout="vertical">
        <a-form-item label="当前密码" name="currentPassword">
          <a-input-password v-model:value="passwordForm.oldPassword" />
        </a-form-item>
        <a-form-item label="新密码" name="newPassword">
          <a-input-password v-model:value="passwordForm.newPassword" />
        </a-form-item>
        <a-form-item label="确认新密码" name="confirmPassword">
          <a-input-password v-model:value="passwordForm.confirmPassword" />
        </a-form-item>
        <a-form-item>
          <a-button type="primary" @click="handleChangePassword">修改密码</a-button>
        </a-form-item>
      </a-form>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { message } from 'ant-design-vue';
import { useAuthStore } from '../stores/auth';
import { authApi } from '../api/crm';
import { notifyError } from '../utils/notify';
import type { FormInstance } from 'ant-design-vue';

interface Profile {
  id: string;
  username: string;
  name: string;
  email?: string;
  phone?: string;
  departmentName?: string;
  roles: Array<{ id: string; name: string }>;
  createdAt?: string;
  updatedAt?: string;
}

const auth = useAuthStore();
const profile = ref<Profile>({} as Profile);

const passwordFormRef = ref<FormInstance>();
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
});

const passwordRules = {
  oldPassword: [{ required: true, message: '请输入当前密码' }],
  newPassword: [
    { required: true, message: '请输入新密码' },
    { min: 6, message: '密码至少6位' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码' },
    {
      validator: (_: any, value: string) => {
        if (value && value !== passwordForm.newPassword) {
          return Promise.reject('两次输入的密码不一致');
        }
        return Promise.resolve();
      }
    }
  ]
};

const formatDate = (dateString: string) => {
  return new Date(dateString).toLocaleString('zh-CN');
};

const loadProfile = async () => {
  try {
    const { data } = await authApi.me();
    if (data.success) {
      profile.value = data.data;
    }
  } catch (error) {
    notifyError(error);
  }
};

const handleChangePassword = async () => {
  try {
    await passwordFormRef.value?.validate();

    await authApi.changePassword({
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword,
      confirmPassword: passwordForm.confirmPassword
    });

    message.success('密码修改成功');
    passwordForm.oldPassword = '';
    passwordForm.newPassword = '';
    passwordForm.confirmPassword = '';
  } catch (error: any) {
    if (error.response?.data?.message) {
      message.error(error.response.data.message);
    } else {
      notifyError(error);
    }
  }
};

onMounted(() => {
  loadProfile();
});
</script>

<style scoped>
.profile-container {
  display: grid;
  gap: 10px;
}

.ant-descriptions {
  margin-bottom: 0;
}
</style>
