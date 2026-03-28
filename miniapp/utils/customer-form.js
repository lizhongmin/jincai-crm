const CUSTOMER_TYPE_OPTIONS = [
  { label: '\u4e2a\u4eba', value: 'PERSONAL' },
  { label: '\u4f01\u4e1a', value: 'ENTERPRISE' }
];

const SOURCE_OPTIONS = [
  { label: '\u624b\u5de5\u5f55\u5165', value: 'MANUAL' },
  { label: '\u7ebf\u4e0a\u54a8\u8be2', value: 'ONLINE' },
  { label: '\u8001\u5ba2\u63a8\u8350', value: 'REFERRAL' },
  { label: '\u7535\u8bdd\u54a8\u8be2', value: 'PHONE' },
  { label: '\u5230\u5e97', value: 'WALK_IN' }
];

const INTENTION_LEVEL_OPTIONS = [
  { label: '\u9ad8', value: 'HIGH' },
  { label: '\u4e2d', value: 'MEDIUM' },
  { label: '\u4f4e', value: 'LOW' }
];

function createEmptyCustomerForm() {
  return {
    name: '',
    phone: '',
    customerType: 'PERSONAL',
    source: 'MANUAL',
    intentionLevel: 'MEDIUM',
    remark: ''
  };
}

function customerToForm(customer) {
  return {
    ...createEmptyCustomerForm(),
    name: customer && customer.name ? customer.name : '',
    phone: customer && customer.phone ? customer.phone : '',
    customerType: customer && customer.customerType ? customer.customerType : 'PERSONAL',
    source: customer && customer.source ? customer.source : 'MANUAL',
    intentionLevel: customer && customer.intentionLevel ? customer.intentionLevel : 'MEDIUM',
    remark: customer && customer.remark ? customer.remark : ''
  };
}

function buildCustomerPayload(form) {
  return {
    name: String(form.name || '').trim(),
    phone: String(form.phone || '').trim(),
    customerType: form.customerType || 'PERSONAL',
    source: form.source || 'MANUAL',
    intentionLevel: form.intentionLevel || 'MEDIUM',
    remark: String(form.remark || '').trim()
  };
}

function findOptionLabel(options, value) {
  const match = options.find((item) => item.value === value);
  return match ? match.label : '';
}

module.exports = {
  CUSTOMER_TYPE_OPTIONS,
  SOURCE_OPTIONS,
  INTENTION_LEVEL_OPTIONS,
  createEmptyCustomerForm,
  customerToForm,
  buildCustomerPayload,
  findOptionLabel
};