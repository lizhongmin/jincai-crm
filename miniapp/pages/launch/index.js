const { getWxCode, miniAppLogin } = require('../../services/auth');
const { clearAuthSession, persistAuthSession } = require('../../utils/session');
const TEXT = {
  loadingPreparing: '\u6b63\u5728\u51c6\u5907\u767b\u5f55...',
  loadingSigning: '\u6b63\u5728\u5c1d\u8bd5\u767b\u5f55...',
  loginFailed: '\u767b\u5f55\u5931\u8d25',
  loginFailedLater: '\u767b\u5f55\u5931\u8d25\uff0c\u8bf7\u7a0d\u540e\u91cd\u8bd5'
};
Page({
  data: { loadingText: TEXT.loadingPreparing },
  onShow() { this.bootstrap(); },
  bootstrap() {
    if (this.loading) { return; }
    this.loading = true;
    this.setData({ loadingText: TEXT.loadingSigning });
    getWxCode()
      .then((code) => miniAppLogin(code))
      .then((session) => {
        if (session.bound && session.token) {
          persistAuthSession(session);
          getApp().setProfile({ userId: session.userId, username: session.username, fullName: session.fullName, roles: session.roles || [] });
          wx.switchTab({ url: '/pages/workbench/index' });
          return;
        }
        clearAuthSession();
        getApp().setProfile(null);
        wx.reLaunch({ url: '/pages/auth/bind/index' });
      })
      .catch((error) => {
        clearAuthSession();
        getApp().setProfile(null);
        this.setData({ loadingText: error.message || TEXT.loginFailedLater });
        wx.showToast({ title: error.message || TEXT.loginFailed, icon: 'none' });
      })
      .finally(() => { this.loading = false; });
  }
});