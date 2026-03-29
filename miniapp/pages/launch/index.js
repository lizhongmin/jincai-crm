const { getWxCode, miniAppLogin } = require('../../services/auth');
const { clearAuthSession, persistAuthSession } = require('../../utils/session');

const TEXT = {
  checking: '正在校验微信身份',
  signing: '正在尝试登录',
  ready: '登录成功，正在进入工作台',
  unbound: '账号未绑定，正在进入绑定流程',
  loginFailed: '登录失败，请稍后重试',
  networkHint: '网络连接失败，请确认后端服务已启动'
};

function toFriendlyError(error) {
  const raw = String((error && error.message) || '').trim();
  if (!raw) {
    return TEXT.loginFailed;
  }

  const lower = raw.toLowerCase();
  if (
    lower.includes('request:fail') ||
    lower.includes('connection refused') ||
    lower.includes('err_connection_refused') ||
    lower.includes('timeout')
  ) {
    return TEXT.networkHint;
  }

  return raw;
}

Page({
  data: {
    statusText: TEXT.checking,
    statusTag: '处理中',
    loading: true,
    errorText: '',
    showRetry: false
  },

  onShow() {
    this.bootstrap();
  },

  bootstrap() {
    if (this.loading) {
      return;
    }

    this.loading = true;
    this.setData({
      statusText: TEXT.signing,
      statusTag: '处理中',
      loading: true,
      errorText: '',
      showRetry: false
    });

    getWxCode()
      .then((code) => miniAppLogin(code))
      .then((session) => {
        if (session.bound && session.token) {
          persistAuthSession(session);
          getApp().setProfile({
            userId: session.userId,
            username: session.username,
            fullName: session.fullName,
            roles: session.roles || []
          });

          this.setData({ statusText: TEXT.ready, statusTag: '完成', loading: false });
          setTimeout(() => wx.switchTab({ url: '/pages/workbench/index' }), 180);
          return;
        }

        clearAuthSession();
        getApp().setProfile(null);
        this.setData({ statusText: TEXT.unbound, statusTag: '待绑定', loading: false });
        setTimeout(() => wx.reLaunch({ url: '/pages/auth/bind/index' }), 180);
      })
      .catch((error) => {
        clearAuthSession();
        getApp().setProfile(null);

        const message = toFriendlyError(error);
        this.setData({
          statusText: message,
          statusTag: '异常',
          loading: false,
          errorText: message,
          showRetry: true
        });
        wx.showToast({ title: message, icon: 'none' });
      })
      .finally(() => {
        this.loading = false;
      });
  },

  handleRetry() {
    this.bootstrap();
  },

  handleGoBind() {
    wx.reLaunch({ url: '/pages/auth/bind/index' });
  }
});