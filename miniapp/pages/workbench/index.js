const { readAuthSession } = require('../../utils/session');

Page({
  data: {
    metrics: [
      { id: 'pendingFollow', value: 12, label: '待跟进' },
      { id: 'pendingApproval', value: 4, label: '待审批' },
      { id: 'newLead', value: 8, label: '新增线索' }
    ],
    quickActions: [
      { id: 'myCustomer', title: '我的客户', url: '/pages/customer/list/index', tab: true },
      { id: 'newCustomer', title: '新增客户', url: '/pages/customer/form/index' },
      { id: 'approvalCenter', title: '审批中心', url: '/pages/placeholder/index?title=审批中心&desc=订单审批、费用审批与流程处理入口' },
      { id: 'message', title: '消息中心', url: '/pages/message/index', tab: true }
    ],
    todos: [
      { id: 'todo1', title: '高意向客户待跟进', meta: '3 位客户 · 24 小时内', level: '高优先', url: '/pages/customer/list/index', tab: true },
      { id: 'todo2', title: '审批待处理', meta: '4 条审批记录', level: '处理中', url: '/pages/placeholder/index?title=审批中心&desc=订单审批、费用审批与流程处理入口' },
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