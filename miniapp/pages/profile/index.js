const { clearAuthSession, readAuthSession } = require('../../utils/session');

Page({
  data: {
    profile: null,
    entries: [
      { id: 'settings', title: '设置中心', desc: '通知、偏好与账号安全', url: '/pages/placeholder/index?title=设置中心&desc=通知设置、偏好配置与账号安全入口' },
      { id: 'about', title: '关于袋鼠旅客通', desc: '版本信息与帮助反馈', url: '/pages/placeholder/index?title=关于袋鼠旅客通&desc=版本说明、产品介绍与反馈入口' }
    ]
  },

  onShow() {
    const session = readAuthSession();
    if (!session.token) {
      wx.reLaunch({ url: '/pages/launch/index' });
      return;
    }

    const app = getApp();
    const profile = app.globalData.profile || session.profile || null;
    this.setData({ profile });
  },

  handleOpen(event) {
    const url = event.currentTarget.dataset.url;
    if (url) {
      wx.navigateTo({ url });
    }
  },

  handleLogout() {
    clearAuthSession();
    getApp().setProfile(null);
    wx.reLaunch({ url: '/pages/launch/index' });
  }
});