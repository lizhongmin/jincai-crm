const { API_BASE_URL } = require('./config');
const { buildAuthHeaders, clearAuthSession, readAuthSession } = require('./session');

function request(options) {
  const opts = options || {};
  const session = readAuthSession();
  const headers = {
    'Content-Type': 'application/json',
    ...(opts.auth === false ? {} : buildAuthHeaders(session.token)),
    ...(opts.header || {})
  };

  return new Promise((resolve, reject) => {
    wx.request({
      url: `${API_BASE_URL}${opts.url}`,
      method: opts.method || 'GET',
      data: opts.data,
      header: headers,
      success(res) {
        const body = res.data || {};
        if (res.statusCode === 401) {
          clearAuthSession();
          reject(new Error(body.message || 'Session expired'));
          return;
        }
        if (res.statusCode >= 400 || body.success === false) {
          reject(new Error(body.message || 'Request failed'));
          return;
        }
        resolve(body.data);
      },
      fail(err) {
        reject(new Error(err.errMsg || 'Network request failed'));
      }
    });
  });
}

module.exports = {
  request
};
