const { bindMiniApp, getWxCode } = require('../../../services/auth');
const { clearAuthSession, persistAuthSession } = require('../../../utils/session');
const { readEventValue } = require('../../../utils/event-value');

const TEXT = {
  emptyCreds: '请输入账号和密码',
  bindSuccess: '绑定成功',
  bindFailed: '绑定失败，请稍后重试',
  networkHint: '网络连接失败，请确认后端服务已启动'
};

function toFriendlyError(error) {
  const raw = String((error && error.message) || '').trim();
  if (!raw) {
    return TEXT.bindFailed;
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
    username: '',
    password: '',
    submitting: false,
    errorText: ''
  },

  handleInput(event) {
    const field = event.currentTarget.dataset.field;
    this.setData({ [field]: readEventValue(event.detail) });
  },

  handleBind() {
    const username = String(this.data.username || '').trim();
    const password = String(this.data.password || '').trim();

    if (!username || !password) {
      this.setData({ errorText: TEXT.emptyCreds });
      wx.showToast({ title: TEXT.emptyCreds, icon: 'none' });
      return;
    }

    if (this.data.submitting) {
      return;
    }

    this.setData({ submitting: true, errorText: '' });

    getWxCode()
      .then((code) => bindMiniApp(code, username, password))
      .then((session) => {
        persistAuthSession(session);
        getApp().setProfile({
          userId: session.userId,
          username: session.username,
          fullName: session.fullName,
          roles: session.roles || []
        });

        wx.showToast({ title: TEXT.bindSuccess, icon: 'success' });
        setTimeout(() => wx.switchTab({ url: '/pages/workbench/index' }), 200);
      })
      .catch((error) => {
        clearAuthSession();
        getApp().setProfile(null);

        const message = toFriendlyError(error);
        this.setData({ errorText: message });
        wx.showToast({ title: message, icon: 'none' });
      })
      .finally(() => {
        this.setData({ submitting: false });
      });
  },

  handleBackLogin() {
    wx.reLaunch({ url: '/pages/launch/index' });
  }
});