const { createCustomer, getCustomerDetail, updateCustomer } = require('../../../services/customer');
const { CUSTOMER_TYPE_OPTIONS, SOURCE_OPTIONS, INTENTION_LEVEL_OPTIONS, buildCustomerPayload, createEmptyCustomerForm, customerToForm, findOptionLabel } = require('../../../utils/customer-form');
const { readEventValue } = require('../../../utils/event-value');
const TEXT = { editTitle: '\u7f16\u8f91\u5ba2\u6237', createTitle: '\u65b0\u589e\u5ba2\u6237', required: '\u8bf7\u586b\u5199\u5ba2\u6237\u540d\u79f0\u548c\u624b\u673a\u53f7', updated: '\u5ba2\u6237\u5df2\u66f4\u65b0', created: '\u5ba2\u6237\u5df2\u521b\u5efa', saveFailed: '\u4fdd\u5b58\u5931\u8d25', loadFailed: '\u52a0\u8f7d\u5ba2\u6237\u5931\u8d25', pleaseSelect: '\u8bf7\u9009\u62e9' };
Page({
  data: { id: '', submitting: false, form: createEmptyCustomerForm(), customerTypeOptions: CUSTOMER_TYPE_OPTIONS, sourceOptions: SOURCE_OPTIONS, intentionLevelOptions: INTENTION_LEVEL_OPTIONS },
  onLoad(options) { const id = options.id || ''; this.setData({ id }); wx.setNavigationBarTitle({ title: id ? TEXT.editTitle : TEXT.createTitle }); if (id) { this.loadDetail(id); } },
  handleInput(event) { const field = event.currentTarget.dataset.field; this.setData({ [`form.${field}`]: readEventValue(event.detail) }); },
  handlePick(event) { const field = event.currentTarget.dataset.field; const options = this.data[`${field}Options`]; const index = Number(event.detail.value || 0); const selected = options[index]; if (selected) { this.setData({ [`form.${field}`]: selected.value }); } },
  submit() { if (this.data.submitting) { return; } const payload = buildCustomerPayload(this.data.form); if (!payload.name || !payload.phone) { wx.showToast({ title: TEXT.required, icon: 'none' }); return; } this.setData({ submitting: true }); const action = this.data.id ? updateCustomer(this.data.id, payload) : createCustomer(payload); action.then((customer) => { wx.showToast({ title: this.data.id ? TEXT.updated : TEXT.created, icon: 'success' }); setTimeout(() => { wx.redirectTo({ url: `/pages/customer/detail/index?id=${customer.id}` }); }, 250); }).catch((error) => { wx.showToast({ title: error.message || TEXT.saveFailed, icon: 'none' }); }).finally(() => { this.setData({ submitting: false }); }); },
  loadDetail(id) { getCustomerDetail(id).then((customer) => { this.setData({ form: customerToForm(customer) }); }).catch((error) => { wx.showToast({ title: error.message || TEXT.loadFailed, icon: 'none' }); }); },
  customerTypeLabel() { return findOptionLabel(CUSTOMER_TYPE_OPTIONS, this.data.form.customerType) || TEXT.pleaseSelect; },
  sourceLabel() { return findOptionLabel(SOURCE_OPTIONS, this.data.form.source) || TEXT.pleaseSelect; },
  intentionLabel() { return findOptionLabel(INTENTION_LEVEL_OPTIONS, this.data.form.intentionLevel) || TEXT.pleaseSelect; }
});
