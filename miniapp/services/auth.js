const { request } = require('../utils/request');

function getWxCode() {
  return new Promise((resolve, reject) => {
    wx.login({
      success(res) {
        if (res.code) {
          resolve(res.code);
          return;
        }
        reject(new Error('Failed to get WeChat login code'));
      },
      fail(err) {
        reject(new Error(err.errMsg || 'Failed to get WeChat login code'));
      }
    });
  });
}

function miniAppLogin(code) {
  return request({
    url: '/miniapp/auth/login',
    method: 'POST',
    data: { code },
    auth: false
  });
}

function bindMiniApp(code, username, password) {
  return request({
    url: '/miniapp/auth/bind',
    method: 'POST',
    data: { code, username, password },
    auth: false
  });
}

function getAuthStatus() {
  return request({
    url: '/miniapp/auth/status',
    method: 'GET'
  });
}

module.exports = {
  getWxCode,
  miniAppLogin,
  bindMiniApp,
  getAuthStatus
};
