<template>
  <div class="login-wrap">
    <div class="bg-orb a"></div>
    <div class="bg-orb b"></div>
    <a-card class="login-card" :bordered="false">
      <h1>旅行社 CRM</h1>
      <p>Ant Design Pro 风格管理后台</p>

      <a-form layout="vertical" :model="form" @finish="submit">
        <a-form-item label="用户名" name="username" :rules="[{ required: true, message: '请输入用户名' }]">
          <a-input v-model:value="form.username" size="large" placeholder="admin" />
        </a-form-item>
        <a-form-item label="密码" name="password" :rules="[{ required: true, message: '请输入密码' }]">
          <a-input-password v-model:value="form.password" size="large" placeholder="Admin@123" />
        </a-form-item>
        <a-button type="primary" html-type="submit" size="large" block :loading="loading">登录系统</a-button>
      </a-form>

      <div class="hint">默认账号：admin / Admin@123</div>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { message } from 'ant-design-vue';
import { authApi } from '../api/crm';
import { useAuthStore } from '../stores/auth';

const router = useRouter();
const auth = useAuthStore();
const loading = ref(false);
const form = reactive({ username: 'admin', password: 'Admin@123' });

const submit = async () => {
  loading.value = true;
  try {
    const { data } = await authApi.login(form);
    if (!data.success) {
      throw new Error(data.message || '登录失败');
    }
    auth.setToken(data.data.token);
    message.success('登录成功');
    router.replace('/dashboard');
  } catch (error: any) {
    message.error(error?.response?.data?.message || error.message || '登录失败');
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped>
.login-wrap {
  min-height: 100vh;
  display: grid;
  place-items: center;
  position: relative;
  overflow: hidden;
  background: linear-gradient(165deg, #0c1f3d, #133977 48%, #1677ff 100%);
}

.bg-orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(8px);
}

.bg-orb.a {
  width: 380px;
  height: 380px;
  background: rgba(157, 220, 255, 0.24);
  left: -120px;
  top: -90px;
}

.bg-orb.b {
  width: 300px;
  height: 300px;
  background: rgba(255, 191, 112, 0.24);
  right: -90px;
  bottom: -80px;
}

.login-card {
  width: min(430px, 92vw);
  border-radius: 16px;
  box-shadow: 0 26px 60px rgba(5, 19, 38, 0.42);
  z-index: 2;
}

.login-card h1 {
  margin: 0;
  font-size: 30px;
}

.login-card p {
  margin: 8px 0 20px;
  color: var(--muted);
}

.hint {
  margin-top: 14px;
  color: #7d89a1;
  font-size: 12px;
}
</style>
