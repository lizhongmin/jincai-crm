const { request } = require('../utils/request');

function listCustomers(params) {
  return request({
    url: '/customers/page',
    method: 'GET',
    data: params || {}
  });
}

function getCustomerDetail(id) {
  return request({
    url: `/customers/${id}`,
    method: 'GET'
  });
}

function createCustomer(payload) {
  return request({
    url: '/customers',
    method: 'POST',
    data: payload
  });
}

function updateCustomer(id, payload) {
  return request({
    url: `/customers/${id}`,
    method: 'PUT',
    data: payload
  });
}

module.exports = {
  listCustomers,
  getCustomerDetail,
  createCustomer,
  updateCustomer
};
