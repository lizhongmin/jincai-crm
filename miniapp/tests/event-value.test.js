const test = require('node:test');
const assert = require('node:assert/strict');
const { readEventValue } = require('../utils/event-value');

test('readEventValue returns detail.value when detail is an object', () => {
  assert.equal(readEventValue({ value: 'crm-user' }), 'crm-user');
});

test('readEventValue returns raw detail when it is already a string', () => {
  assert.equal(readEventValue('13800138000'), '13800138000');
});

test('readEventValue falls back to empty string for nullish detail', () => {
  assert.equal(readEventValue(undefined), '');
  assert.equal(readEventValue(null), '');
});
