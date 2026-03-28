const test = require('node:test');
const assert = require('node:assert/strict');

const {
  createEmptyCustomerForm,
  customerToForm,
  buildCustomerPayload
} = require('../utils/customer-form');

test('createEmptyCustomerForm returns the miniapp MVP defaults', () => {
  assert.deepEqual(createEmptyCustomerForm(), {
    name: '',
    phone: '',
    customerType: 'PERSONAL',
    source: 'MANUAL',
    intentionLevel: 'MEDIUM',
    remark: ''
  });
});

test('customerToForm maps existing customer detail into editable form fields', () => {
  assert.deepEqual(
    customerToForm({
      name: 'Alice',
      phone: '13800000000',
      customerType: 'ENTERPRISE',
      source: 'ONLINE',
      intentionLevel: 'HIGH',
      remark: '重点客户'
    }),
    {
      name: 'Alice',
      phone: '13800000000',
      customerType: 'ENTERPRISE',
      source: 'ONLINE',
      intentionLevel: 'HIGH',
      remark: '重点客户'
    }
  );
});

test('buildCustomerPayload trims values and keeps only the MVP fields', () => {
  assert.deepEqual(
    buildCustomerPayload({
      name: ' Alice ',
      phone: ' 13800000000 ',
      customerType: 'PERSONAL',
      source: 'ONLINE',
      intentionLevel: 'HIGH',
      remark: '  来自小程序  ',
      level: 'A',
      tags: 'should-not-leak'
    }),
    {
      name: 'Alice',
      phone: '13800000000',
      customerType: 'PERSONAL',
      source: 'ONLINE',
      intentionLevel: 'HIGH',
      remark: '来自小程序'
    }
  );
});
