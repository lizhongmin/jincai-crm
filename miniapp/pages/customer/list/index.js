const { listCustomers } = require('../../../services/customer');
const { readAuthSession } = require('../../../utils/session');
const { SOURCE_OPTIONS, INTENTION_LEVEL_OPTIONS, findOptionLabel } = require('../../../utils/customer-form');
const { readEventValue } = require('../../../utils/event-value');

const TEXT = {
  loadFailed: '加载客户失败',
  unknownSource: '未知来源',
  unsetIntention: '未设置'
};

const VIEW_MAP = ['all', 'high', 'recent'];

function mapCustomer(item) {
  return {
    ...item,
    sourceLabel: findOptionLabel(SOURCE_OPTIONS, item.source) || TEXT.unknownSource,
    intentionLevelLabel: findOptionLabel(INTENTION_LEVEL_OPTIONS, item.intentionLevel) || TEXT.unsetIntention
  };
}

Page({
  data: {
    keyword: '',
    activeTab: 0,
    customers: [],
    visibleCustomers: [],
    page: 1,
    size: 10,
    total: 0,
    loading: false,
    stats: {
      all: 0,
      high: 0,
      recent: 0
    }
  },

  onShow() {
    const session = readAuthSession();
    if (!session.token) {
      wx.reLaunch({ url: '/pages/launch/index' });
      return;
    }

    this.loadCustomers(true);
  },

  onPullDownRefresh() {
    this.loadCustomers(true);
  },

  onReachBottom() {
    if (this.data.loading || this.data.customers.length >= this.data.total) {
      return;
    }

    this.loadCustomers(false);
  },

  handleKeywordInput(event) {
    this.setData({ keyword: readEventValue(event.detail) });
  },

  handleSearch() {
    this.loadCustomers(true);
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

  handleOpenDetail(event) {
    const id = event.currentTarget.dataset.id;
    if (!id) {
      return;
    }

    wx.navigateTo({ url: `/pages/customer/detail/index?id=${id}` });
  },

  handleCreate() {
    wx.navigateTo({ url: '/pages/customer/form/index' });
  },

  loadCustomers(reset) {
    if (this.data.loading) {
      return;
    }

    const nextPage = reset ? 1 : this.data.page + 1;
    this.setData({ loading: true });

    listCustomers({
      page: nextPage,
      size: this.data.size,
      keyword: String(this.data.keyword || '').trim() || undefined,
      tab: 'customer',
      ownerScope: 'mine'
    })
      .then((result) => {
        const incoming = (result.items || []).map(mapCustomer);
        const customers = reset ? incoming : this.data.customers.concat(incoming);

        this.setData({
          customers,
          page: result.page || nextPage,
          size: result.size || this.data.size,
          total: result.total || 0
        });

        this.updateStats();
        this.applyFilter();
      })
      .catch((error) => {
        wx.showToast({ title: error.message || TEXT.loadFailed, icon: 'none' });
      })
      .finally(() => {
        this.setData({ loading: false });
        wx.stopPullDownRefresh();
      });
  },

  updateStats() {
    const all = this.data.customers;
    const high = all.filter((item) => item.intentionLevel === 'HIGH').length;
    const recent = all.filter((item) => Boolean(item.updatedAt)).length;

    this.setData({
      stats: {
        all: all.length,
        high,
        recent
      }
    });
  },

  applyFilter() {
    const viewKey = VIEW_MAP[this.data.activeTab] || 'all';
    const source = this.data.customers;

    let visibleCustomers = source;
    if (viewKey === 'high') {
      visibleCustomers = source.filter((item) => item.intentionLevel === 'HIGH');
    } else if (viewKey === 'recent') {
      visibleCustomers = source.filter((item) => Boolean(item.updatedAt));
    }

    this.setData({ visibleCustomers });
  }
});