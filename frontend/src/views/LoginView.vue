<template>
  <div class="login-wrap">
    <div class="bg-layer"></div>
    <div class="grid-mask"></div>
    <div class="light light-a"></div>
    <div class="light light-b"></div>

    <div class="login-stage">
      <section class="brand-panel" aria-label="品牌介绍">
        <p class="brand-kicker">JINCAI TRAVEL CRM</p>
        <h1>旅行社 CRM 运营中台</h1>
        <p class="brand-desc">面向销售、运营、财务的一体化管理后台，统一客户、订单、流程与资金数据。</p>
        <div class="brand-grid">
          <article class="brand-item">
            <p>客户资产</p>
            <strong>全量档案统一沉淀</strong>
          </article>
          <article class="brand-item">
            <p>订单履约</p>
            <strong>审批与库存自动联动</strong>
          </article>
          <article class="brand-item">
            <p>资金状态</p>
            <strong>收支退款全链路可追踪</strong>
          </article>
          <article class="brand-item">
            <p>协同效率</p>
            <strong>跨角色共享实时数据</strong>
          </article>
        </div>
      </section>

      <a-card class="login-card" :bordered="false">
        <div class="form-head">
          <h2>欢迎登录</h2>
          <p>请输入账号信息进入系统</p>
        </div>

        <a-form layout="vertical" :model="form" @finish="submit">
          <a-form-item label="用户名" name="username" :rules="[{ required: true, message: '请输入用户名' }]">
            <a-input v-model:value="form.username" size="large" placeholder="admin" @blur="onUsernameBlur" />
          </a-form-item>
          <a-form-item label="密码" name="password" :rules="[{ required: true, message: '请输入密码' }]">
            <a-input-password v-model:value="form.password" size="large" placeholder="Admin@123" />
          </a-form-item>

          <a-form-item
            v-if="captchaRequired"
            label="图形验证码"
            name="captchaCode"
            :rules="[{ required: true, message: '请输入图形验证码' }]"
          >
            <div class="captcha-row">
              <a-input v-model:value="form.captchaCode" size="large" placeholder="请输入验证码" />
              <button type="button" class="captcha-trigger" @click="loadCaptcha" aria-label="刷新图形验证码">
                <img
                  v-if="captchaImage"
                  class="captcha-image"
                  :src="`data:image/png;base64,${captchaImage}`"
                  alt="点击刷新验证码"
                />
                <span v-else>获取验证码</span>
              </button>
            </div>
          </a-form-item>

          <div v-if="locked && lockSeconds > 0" class="lock-tip">
            账号已锁定，请 {{ lockMinutes }} 分钟后重试
          </div>
          <a-button type="primary" html-type="submit" size="large" block :loading="loading" class="submit-btn">
            登录系统
          </a-button>
        </a-form>

        <div class="hint">体验账号：<code>admin</code> / <code>Admin@123</code></div>
      </a-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue';
import { useRouter } from 'vue-router';
import { message } from 'ant-design-vue';
import { authApi } from '../api/crm';
import { useAuthStore } from '../stores/auth';
import { notifyError } from '../utils/notify';

const router = useRouter();
const auth = useAuthStore();
const loading = ref(false);
const form = reactive({ username: 'admin', password: 'Admin@123', captchaCode: '' });
const captchaRequired = ref(false);
const locked = ref(false);
const lockSeconds = ref(0);
const captchaId = ref('');
const captchaImage = ref('');
const lockMinutes = computed(() => Math.ceil(lockSeconds.value / 60));

const resetCaptcha = () => {
  captchaId.value = '';
  form.captchaCode = '';
  captchaImage.value = '';
};

const refreshLoginState = async () => {
  if (!form.username.trim()) {
    captchaRequired.value = false;
    locked.value = false;
    lockSeconds.value = 0;
    resetCaptcha();
    return;
  }

  const { data } = await authApi.loginState(form.username.trim());
  const state = data.data || { captchaRequired: false, locked: false, lockSeconds: 0 };
  captchaRequired.value = Boolean(state.captchaRequired);
  locked.value = Boolean(state.locked);
  lockSeconds.value = Number(state.lockSeconds || 0);
  if (!captchaRequired.value) {
    resetCaptcha();
  } else if (!captchaImage.value) {
    await loadCaptcha();
  }
};

const loadCaptcha = async () => {
  if (!form.username.trim()) {
    message.error('请先输入用户名');
    return;
  }
  const { data } = await authApi.captcha(form.username.trim());
  captchaId.value = data.data?.captchaId || '';
  captchaImage.value = data.data?.imageBase64 || '';
  form.captchaCode = '';
};

const onUsernameBlur = async () => {
  try {
    await refreshLoginState();
  } catch (error) {
    notifyError(error, '获取登录状态失败');
  }
};

watch(
  () => form.username,
  () => {
    captchaRequired.value = false;
    locked.value = false;
    lockSeconds.value = 0;
    resetCaptcha();
  }
);

const submit = async () => {
  loading.value = true;
  try {
    await refreshLoginState();
    if (locked.value) {
      throw new Error(`账号已锁定，请 ${lockMinutes.value} 分钟后再试`);
    }
    if (captchaRequired.value && (!captchaId.value || !form.captchaCode.trim())) {
      await loadCaptcha();
      throw new Error('请输入图形验证码');
    }
    const { data } = await authApi.login({
      username: form.username,
      password: form.password,
      captchaId: captchaRequired.value ? captchaId.value : undefined,
      captchaCode: captchaRequired.value ? form.captchaCode.trim() : undefined
    });
    if (!data.success) {
      throw new Error(data.message || '登录失败');
    }
    auth.setToken(data.data.token);
    message.success('登录成功');
    router.replace('/dashboard');
  } catch (error: any) {
    notifyError(error, '登录失败');
    try {
      await refreshLoginState();
      if (captchaRequired.value) {
        await loadCaptcha();
      }
    } catch (stateError) {
      notifyError(stateError, '获取登录状态失败');
    }
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped>
.login-wrap {
  min-height: 100vh;
  padding: clamp(24px, 4vw, 48px);
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
  background: linear-gradient(130deg, #081833 0%, #0f2d6f 52%, #1d5fff 100%);
}

.bg-layer {
  position: absolute;
  inset: 0;
  background: radial-gradient(circle at 22% 14%, rgba(115, 200, 255, 0.22), transparent 46%);
}

.grid-mask {
  position: absolute;
  inset: 0;
  background:
    repeating-linear-gradient(
      0deg,
      rgba(255, 255, 255, 0.05) 0,
      rgba(255, 255, 255, 0.05) 1px,
      transparent 1px,
      transparent 44px
    ),
    repeating-linear-gradient(
      90deg,
      rgba(255, 255, 255, 0.05) 0,
      rgba(255, 255, 255, 0.05) 1px,
      transparent 1px,
      transparent 44px
    );
  mask-image: radial-gradient(circle at 50% 50%, #000 30%, transparent 100%);
  opacity: 0.45;
}

.light {
  position: absolute;
  border-radius: 999px;
  filter: blur(22px);
  animation: float 10s ease-in-out infinite;
}

.light-a {
  width: 360px;
  height: 360px;
  top: -140px;
  right: 26%;
  background: rgba(120, 200, 255, 0.34);
}

.light-b {
  width: 320px;
  height: 320px;
  left: -120px;
  bottom: -100px;
  background: rgba(90, 149, 255, 0.44);
  animation-delay: 1.5s;
}

.login-stage {
  position: relative;
  z-index: 2;
  width: min(1160px, 100%);
  display: grid;
  grid-template-columns: minmax(0, 1fr) 440px;
  gap: clamp(28px, 4vw, 72px);
  align-items: center;
}

.brand-panel {
  color: #f4f8ff;
  display: grid;
  gap: 16px;
  animation: fade-up 0.7s ease both;
  max-width: 620px;
}

.brand-kicker {
  margin: 0;
  font-size: 12px;
  letter-spacing: 0.18em;
  color: rgba(222, 237, 255, 0.9);
}

.brand-panel h1 {
  margin: 0;
  font-size: clamp(36px, 4.4vw, 54px);
  line-height: 1.08;
  letter-spacing: 0.01em;
  text-wrap: balance;
}

.brand-desc {
  margin: 0;
  max-width: 540px;
  color: rgba(220, 234, 255, 0.9);
  line-height: 1.8;
  font-size: 15px;
}

.brand-grid {
  margin-top: 10px;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.brand-item {
  margin: 0;
  padding: 14px 16px;
  border-radius: 14px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  background: linear-gradient(140deg, rgba(255, 255, 255, 0.16), rgba(255, 255, 255, 0.06));
  backdrop-filter: blur(6px);
}

.brand-item p {
  margin: 0;
  font-size: 12px;
  color: rgba(224, 237, 255, 0.9);
}

.brand-item strong {
  display: block;
  margin-top: 6px;
  font-size: 14px;
  font-weight: 700;
  color: #ffffff;
}

.login-card {
  border-radius: 20px;
  border: 1px solid rgba(216, 227, 246, 0.92);
  box-shadow: 0 22px 56px rgba(4, 15, 44, 0.34);
  animation: fade-up 0.7s ease 0.15s both;
}

.login-card :deep(.ant-card-body) {
  padding: 34px 34px 26px;
}

.form-head h2 {
  margin: 0;
  font-size: 36px;
  line-height: 1.1;
  color: #111c3b;
}

.form-head p {
  margin: 10px 0 22px;
  color: #6c7a96;
  font-size: 14px;
}

.login-card :deep(.ant-form-item-label > label) {
  font-weight: 600;
  color: #1a2d5d;
}

.login-card :deep(.ant-input-affix-wrapper),
.login-card :deep(.ant-input) {
  border-radius: 12px;
  border-color: #d5deef;
}

.login-card :deep(.ant-input-affix-wrapper:hover),
.login-card :deep(.ant-input:hover) {
  border-color: #3f75f4;
}

.hint {
  margin-top: 16px;
  color: #7483a0;
  font-size: 13px;
}

.hint code {
  padding: 1px 6px;
  border-radius: 999px;
  background: #edf2ff;
  color: #3053b9;
}

.captcha-row {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 136px;
  gap: 10px;
}

.captcha-trigger {
  width: 100%;
  height: 40px;
  padding: 0;
  border: 1px solid #d5deef;
  border-radius: 12px;
  background: #f3f7ff;
  color: #345abb;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
}

.captcha-trigger:hover {
  border-color: #3f75f4;
  background: #eaf1ff;
}

.captcha-image {
  width: 100%;
  height: 40px;
  border-radius: 11px;
  object-fit: cover;
  background: #fff;
}

.lock-tip {
  margin-bottom: 12px;
  color: #bc4d0a;
  font-size: 13px;
}

.submit-btn {
  height: 44px;
  border-radius: 12px;
  font-weight: 700;
  letter-spacing: 0.03em;
  box-shadow: 0 10px 18px rgba(40, 102, 242, 0.28);
}

@keyframes float {
  0%,
  100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-10px);
  }
}

@keyframes fade-up {
  from {
    opacity: 0;
    transform: translateY(16px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@media (max-width: 980px) {
  .login-stage {
    grid-template-columns: 1fr;
    max-width: 520px;
    gap: 18px;
  }

  .brand-panel {
    order: 2;
    gap: 12px;
  }

  .brand-panel h1 {
    font-size: 30px;
  }

  .brand-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 640px) {
  .login-wrap {
    padding: 12px;
  }

  .login-card :deep(.ant-card-body) {
    padding: 24px 18px 20px;
  }

  .captcha-row {
    grid-template-columns: 1fr;
  }

  .form-head h2 {
    font-size: 30px;
  }

  .captcha-trigger,
  .captcha-image {
    height: 44px;
  }
}
</style>
