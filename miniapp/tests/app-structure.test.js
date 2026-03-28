const test = require('node:test');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');

const appJsonPath = path.join(__dirname, '..', 'app.json');
const appJson = JSON.parse(fs.readFileSync(appJsonPath, 'utf8'));

test('app.json exposes the core product pages in a stable order', () => {
  assert.deepEqual(appJson.pages, [
    'pages/launch/index',
    'pages/auth/bind/index',
    'pages/workbench/index',
    'pages/customer/home/index',
    'pages/customer/list/index',
    'pages/customer/detail/index',
    'pages/customer/form/index',
    'pages/customer/follow/index',
    'pages/customer/tags/index',
    'pages/customer/pool/index',
    'pages/message/index',
    'pages/profile/index',
    'pages/placeholder/index'
  ]);
});

test('app.json defines a four-tab product shell', () => {
  assert.equal(appJson.tabBar.list.length, 4);
  assert.deepEqual(
    appJson.tabBar.list.map((item) => item.pagePath),
    [
      'pages/workbench/index',
      'pages/customer/list/index',
      'pages/message/index',
      'pages/profile/index'
    ]
  );
});