const { readAuthSession } = require('../../utils/session');

Page({
  data: {
    metrics: [
      { id: 'follow', value: 12, label: '待跟进' },
      { id: 'approval', value: 4, label: '待审批' },
      { id: 'lead', value: 8, label: '新增线索' }
    ],
    quickActions: [
      { id: 'myCustomer', title: '我的客户', url: '/pages/customer/list/index', tab: true },
      { id: 'newCustomer', title: '新增客户', url: '/pages/customer/form/index' },
      { id: 'approvalCenter', title: '审批中心', url: '/pages/placeholder/index?title=审批中心&desc=订单审批、费用审批与流程处理入口' },
      { id: 'orderCenter', title: '订单中心', url: '/pages/placeholder/index?title=订单中心&desc=用于承接订单处理、出行协同与交付状态' }
    ],
    todos: [
      { id: 'todo1', title: '高意向客户待跟进', meta: '3 位客户 · 24 小时内', level: '高优先', url: '/pages/customer/list/index', tab: true },
      { id: 'todo2', title: '审批待处理', meta: '4 条待审批记录', level: '处理中', url: '/pages/placeholder/index?title=审批中心&desc=订单审批、费用审批与流程处理入口' },
      { id: 'todo3', title: '系统消息未读', meta: '2 条新消息', level: '提醒', url: '/pages/message/index', tab: true }
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