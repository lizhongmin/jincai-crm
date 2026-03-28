const { readAuthSession } = require('../../../utils/session');

Page({
  data: {
    coreActions: [
      { id: 'myCustomers', title: '我的客户', desc: '查看客户列表', url: '/pages/customer/list/index', strong: true },
      { id: 'newCustomer', title: '新增客户', desc: '快速录入客户', url: '/pages/customer/form/index' },
      { id: 'follow', title: '跟进记录', desc: '客户跟进入口', url: '/pages/customer/follow/index' },
      { id: 'tags', title: '客户标签', desc: '标签分层管理', url: '/pages/customer/tags/index' }
    ],
    segmentStats: [
      { id: 'high', label: '高意向', value: 18 },
      { id: 'mid', label: '中意向', value: 26 },
      { id: 'low', label: '低意向', value: 9 }
    ],
    moreEntries: [
      { id: 'pool', title: '公海客户', desc: '共享客户管理', url: '/pages/customer/pool/index' },
      { id: 'analysis', title: '客户分析', desc: '客户结构与转化', url: '/pages/placeholder/index?title=客户分析&desc=客户结构、转化和渠道分析入口' },
      { id: 'approval', title: '审批中心', desc: '待审流程处理', url: '/pages/placeholder/index?title=审批中心&desc=订单审批与流程处理入口' }
    ]
  },

  onShow() {
    const session = readAuthSession();
    if (!session.token) {
      wx.reLaunch({ url: '/pages/launch/index' });
    }
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
  }
});