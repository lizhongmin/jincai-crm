const { clearAuthSession, readAuthSession } = require('../../utils/session');

Page({
  data: {
    profile: null,
    quickEntries: [
      { id: 'customer', title: '客户', url: '/pages/customer/list/index', tab: true },
      { id: 'message', title: '消息', url: '/pages/message/index', tab: true },
      { id: 'approval', title: '审批', url: '/pages/placeholder/index?title=审批中心&desc=订单审批、费用审批与流程处理入口' },
      { id: 'setting', title: '设置', url: '/pages/placeholder/index?title=设置中心&desc=通知设置、偏好配置与账号安全入口' }
    ],
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
    const isTab = event.currentTarget.dataset.tab === true || event.currentTarget.dataset.tab === 'true';

    if (!url) {
      return;
    }

    if (isTab) {
      wx.switchTab({ url });
      return;
    }

    wx.navigateTo({ url });
  },

  handleLogout() {
    clearAuthSession();
    getApp().setProfile(null);
    wx.reLaunch({ url: '/pages/launch/index' });
  }
});