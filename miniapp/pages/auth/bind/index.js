const { bindMiniApp, getWxCode } = require('../../../services/auth');
const { clearAuthSession, persistAuthSession } = require('../../../utils/session');
const { readEventValue } = require('../../../utils/event-value');
const TEXT = {
  emptyCreds: '\u8bf7\u8f93\u5165\u8d26\u53f7\u548c\u5bc6\u7801',
  bindSuccess: '\u7ed1\u5b9a\u6210\u529f',
  bindFailed: '\u7ed1\u5b9a\u5931\u8d25'
};
Page({
  data: { username: '', password: '', submitting: false },
  handleInput(event) { const field = event.currentTarget.dataset.field; this.setData({ [field]: readEventValue(event.detail) }); },
  handleBind() {
    const username = String(this.data.username || '').trim();
    const password = String(this.data.password || '').trim();
    if (!username || !password) { wx.showToast({ title: TEXT.emptyCreds, icon: 'none' }); return; }
    if (this.data.submitting) { return; }
    this.setData({ submitting: true });
    getWxCode()
      .then((code) => bindMiniApp(code, username, password))
      .then((session) => {
        persistAuthSession(session);
        getApp().setProfile({ userId: session.userId, username: session.username, fullName: session.fullName, roles: session.roles || [] });
        wx.showToast({ title: TEXT.bindSuccess, icon: 'success' });
        setTimeout(() => { wx.switchTab({ url: '/pages/workbench/index' }); }, 250);
      })
      .catch((error) => {
        clearAuthSession();
        getApp().setProfile(null);
        wx.showToast({ title: error.message || TEXT.bindFailed, icon: 'none' });
      })
      .finally(() => { this.setData({ submitting: false }); });
  }
});
