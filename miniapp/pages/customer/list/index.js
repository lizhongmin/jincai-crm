const { listCustomers } = require('../../../services/customer');
const { readAuthSession } = require('../../../utils/session');
const { SOURCE_OPTIONS, INTENTION_LEVEL_OPTIONS, findOptionLabel } = require('../../../utils/customer-form');
const { readEventValue } = require('../../../utils/event-value');

const TEXT = {
  loadFailed: '\u52a0\u8f7d\u5ba2\u6237\u5931\u8d25',
  unknownSource: '\u672a\u77e5\u6765\u6e90',
  unsetIntention: '\u672a\u8bbe\u7f6e',
  noMoreData: '\u6ca1\u6709\u66f4\u591a\u5ba2\u6237\u4e86'
};

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
    activeView: 'all',
    viewOptions: [
      { key: 'all', label: '全部' },
      { key: 'high', label: '高意向' },
      { key: 'recent', label: '最近更新' }
    ],
    customers: [],
    visibleCustomers: [],
    page: 1,
    size: 10,
    total: 0,
    loading: false,
    summary: {
      total: 0,
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
    if (this.data.customers.length >= this.data.total || this.data.loading) {
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

  handleCreate() {
    wx.navigateTo({ url: '/pages/customer/form/index' });
  },

  handleViewChange(event) {
    const key = event.currentTarget.dataset.key;
    if (!key || key === this.data.activeView) {
      return;
    }

    this.setData({ activeView: key });
    this.applyView();
  },

  handleOpenDetail(event) {
    const id = event.currentTarget.dataset.id;
    if (!id) {
      return;
    }
    wx.navigateTo({ url: `/pages/customer/detail/index?id=${id}` });
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
        const mappedItems = (result.items || []).map(mapCustomer);
        const items = reset ? mappedItems : this.data.customers.concat(mappedItems);

        this.setData({
          customers: items,
          page: result.page || nextPage,
          size: result.size || this.data.size,
          total: result.total || 0
        });

        this.refreshSummary();
        this.applyView();
      })
      .catch((error) => {
        wx.showToast({ title: error.message || TEXT.loadFailed, icon: 'none' });
      })
      .finally(() => {
        this.setData({ loading: false });
        wx.stopPullDownRefresh();
      });
  },

  refreshSummary() {
    const all = this.data.customers;
    const high = all.filter((item) => item.intentionLevel === 'HIGH').length;
    const recent = all.filter((item) => Boolean(item.updatedAt)).slice(0, 7).length;

    this.setData({
      summary: {
        total: all.length,
        high,
        recent
      }
    });
  },

  applyView() {
    const activeView = this.data.activeView;
    const source = this.data.customers;

    let visibleCustomers = source;
    if (activeView === 'high') {
      visibleCustomers = source.filter((item) => item.intentionLevel === 'HIGH');
    } else if (activeView === 'recent') {
      visibleCustomers = source.filter((item) => Boolean(item.updatedAt));
    }

    this.setData({ visibleCustomers });
  }
});