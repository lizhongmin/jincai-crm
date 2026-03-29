const { readAuthSession } = require('../../utils/session');
const { readEventValue } = require('../../utils/event-value');

const TYPE_MAP = ['all', 'approval', 'system'];

Page({
  data: {
    activeTab: 0,
    notices: [
      {
        id: 'm1',
        type: 'approval',
        title: '订单审批待处理',
        summary: '2 条审批在队列中',
        time: '刚刚',
        unread: true,
        url: '/pages/placeholder/index?title=审批中心&desc=订单审批、费用审批与流程处理入口'
      },
      {
        id: 'm2',
        type: 'system',
        title: '系统通知',
        summary: '客户模块已更新到新的页面逻辑',
        time: '10:20',
        unread: false,
        url: '/pages/placeholder/index?title=系统通知&desc=查看系统通知详情'
      },
      {
        id: 'm3',
        type: 'approval',
        title: '客户转交提醒',
        summary: '1 条客户转交申请待确认',
        time: '昨天',
        unread: true,
        url: '/pages/placeholder/index?title=审批中心&desc=订单审批、费用审批与流程处理入口'
      }
    ],
    visibleNotices: []
  },

  onShow() {
    const session = readAuthSession();
    if (!session.token) {
      wx.reLaunch({ url: '/pages/launch/index' });
      return;
    }

    this.applyFilter();
  },

  handleTabChange(event) {
    const detail = readEventValue(event.detail);
    const nextTab = Number(detail && detail.name !== undefined ? detail.name : detail);
    if (Number.isNaN(nextTab) || nextTab === this.data.activeTab) {
      return;
    }

    this.setData({ activeTab: nextTab });
    this.applyFilter();
  },

  handleOpen(event) {
    const id = event.currentTarget.dataset.id;
    const target = this.data.notices.find((item) => item.id === id);
    if (!target) {
      return;
    }

    const updated = this.data.notices.map((item) => {
      if (item.id === id) {
        return { ...item, unread: false };
      }
      return item;
    });

    this.setData({ notices: updated }, () => this.applyFilter());

    if (target.url) {
      wx.navigateTo({ url: target.url });
    }
  },

  applyFilter() {
    const type = TYPE_MAP[this.data.activeTab] || 'all';
    const visibleNotices = type === 'all'
      ? this.data.notices
      : this.data.notices.filter((item) => item.type === type);

    this.setData({ visibleNotices });
  }
});