const test = require('node:test');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');

const miniappRoot = path.join(__dirname, '..');

function readJson(relativePath) {
  return JSON.parse(fs.readFileSync(path.join(miniappRoot, relativePath), 'utf8'));
}

test('miniapp package declares @vant/weapp dependency', () => {
  const pkg = readJson('package.json');
  assert.equal(pkg.dependencies['@vant/weapp'], '^1.11.7');
});

test('app.wxss imports vant common styles from miniprogram_npm path', () => {
  const appWxss = fs.readFileSync(path.join(miniappRoot, 'app.wxss'), 'utf8');
  assert.ok(
    appWxss.includes('@import "./miniprogram_npm/@vant/weapp/common/index.wxss";'),
    'app.wxss should import vant common styles via local miniprogram_npm path'
  );
});

test('core product pages register required Vant components', () => {
  const expectations = [
    ['pages/launch/index.json', ['van-button', 'van-loading', 'van-notice-bar', 'van-tag']],
    ['pages/auth/bind/index.json', ['van-button', 'van-cell-group', 'van-field', 'van-notice-bar', 'van-tag']],
    ['pages/workbench/index.json', ['van-button', 'van-cell', 'van-cell-group', 'van-grid', 'van-grid-item', 'van-notice-bar', 'van-tag']],
    ['pages/customer/home/index.json', ['van-button', 'van-tag']],
    ['pages/customer/list/index.json', ['van-button', 'van-cell', 'van-cell-group', 'van-empty', 'van-search', 'van-tab', 'van-tabs', 'van-tag']],
    ['pages/customer/detail/index.json', ['van-button', 'van-cell', 'van-cell-group', 'van-tag']],
    ['pages/customer/form/index.json', ['van-button', 'van-field', 'van-cell-group']],
    ['pages/message/index.json', ['van-cell', 'van-cell-group', 'van-notice-bar', 'van-tab', 'van-tabs', 'van-tag']],
    ['pages/profile/index.json', ['van-button', 'van-cell', 'van-cell-group', 'van-grid', 'van-grid-item', 'van-tag']]
  ];

  for (const [jsonPath, componentNames] of expectations) {
    const json = readJson(jsonPath);
    for (const componentName of componentNames) {
      assert.ok(
        json.usingComponents && json.usingComponents[componentName],
        `${jsonPath} should declare ${componentName}`
      );
      assert.ok(
        String(json.usingComponents[componentName]).startsWith('@vant/weapp/'),
        `${jsonPath} should reference @vant/weapp path for ${componentName}`
      );
    }
  }
});