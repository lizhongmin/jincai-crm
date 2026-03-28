const { getCustomerDetail } = require('../../../services/customer');
const { CUSTOMER_TYPE_OPTIONS, SOURCE_OPTIONS, INTENTION_LEVEL_OPTIONS, findOptionLabel } = require('../../../utils/customer-form');
const TEXT = { loadFailed: '\u52a0\u8f7d\u8be6\u60c5\u5931\u8d25' };
Page({
  data: { id: '', customer: null, loading: false },
  onLoad(options) { this.setData({ id: options.id || '' }); this.loadDetail(); },
  onShow() { if (this.data.id) { this.loadDetail(); } },
  handleEdit() { wx.navigateTo({ url: `/pages/customer/form/index?id=${this.data.id}` }); },
  handleOpenModule(event) { const url = event.currentTarget.dataset.url; if (url) { wx.navigateTo({ url }); } },
  loadDetail() {
    if (!this.data.id || this.data.loading) { return; }
    this.setData({ loading: true });
    getCustomerDetail(this.data.id)
      .then((customer) => { this.setData({ customer: { ...customer, customerTypeLabel: findOptionLabel(CUSTOMER_TYPE_OPTIONS, customer.customerType) || '-', sourceLabel: findOptionLabel(SOURCE_OPTIONS, customer.source) || '-', intentionLevelLabel: findOptionLabel(INTENTION_LEVEL_OPTIONS, customer.intentionLevel) || '-' } }); })
      .catch((error) => { wx.showToast({ title: error.message || TEXT.loadFailed, icon: 'none' }); })
      .finally(() => { this.setData({ loading: false }); });
  }
});