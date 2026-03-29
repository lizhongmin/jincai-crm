const test = require('node:test');
const assert = require('node:assert/strict');

const pageModules = [
  '../pages/launch/index.js',
  '../pages/auth/bind/index.js',
  '../pages/workbench/index.js',
  '../pages/customer/home/index.js',
  '../pages/customer/list/index.js',
  '../pages/customer/detail/index.js',
  '../pages/customer/form/index.js',
  '../pages/customer/follow/index.js',
  '../pages/customer/tags/index.js',
  '../pages/customer/pool/index.js',
  '../pages/message/index.js',
  '../pages/profile/index.js',
  '../pages/placeholder/index.js'
];

function installMiniAppGlobals() {
  global.wx = {
    showToast() {},
    reLaunch() {},
    navigateTo() {},
    switchTab() {},
    redirectTo() {},
    stopPullDownRefresh() {},
    setNavigationBarTitle() {},
    login() {}
  };
  global.getApp = () => ({
    setProfile() {}
  });
  global.Page = (config) => config;
}

test('miniapp page modules can all be loaded without missing dependencies', () => {
  installMiniAppGlobals();

  for (const modulePath of pageModules) {
    assert.doesNotThrow(() => {
      const resolved = require.resolve(modulePath);
      delete require.cache[resolved];
      require(modulePath);
    }, `Expected ${modulePath} to load successfully`);
  }
});
