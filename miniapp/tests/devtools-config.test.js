const test = require('node:test');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');

const configPath = path.join(__dirname, '..', 'project.config.json');
const config = JSON.parse(fs.readFileSync(configPath, 'utf8'));

test('devtools keeps npm files during local dependency analysis', () => {
  assert.equal(config.setting.ignoreDevUnusedFiles, false);
});

test('devtools npm packaging relation is explicitly configured', () => {
  assert.equal(config.setting.packNpmManually, true);
  assert.ok(Array.isArray(config.setting.packNpmRelationList));
  assert.ok(config.setting.packNpmRelationList.length > 0);
  assert.deepEqual(config.setting.packNpmRelationList[0], {
    packageJsonPath: './package.json',
    miniprogramNpmDistDir: './miniprogram_npm'
  });
});
