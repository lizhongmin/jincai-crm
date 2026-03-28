const test = require('node:test');
const assert = require('node:assert/strict');

const {
  buildAuthHeaders,
  persistAuthSession,
  readAuthSession,
  clearAuthSession
} = require('../utils/session');

function createStorage() {
  const data = new Map();
  return {
    setStorageSync(key, value) {
      data.set(key, value);
    },
    getStorageSync(key) {
      return data.get(key);
    },
    removeStorageSync(key) {
      data.delete(key);
    }
  };
}

test('persistAuthSession stores token and profile for later reads', () => {
  const storage = createStorage();

  persistAuthSession(
    {
      token: 'token-123',
      userId: 'user-1',
      username: 'admin',
      fullName: '系统管理员',
      roles: ['ADMIN']
    },
    storage
  );

  assert.deepEqual(readAuthSession(storage), {
    token: 'token-123',
    profile: {
      userId: 'user-1',
      username: 'admin',
      fullName: '系统管理员',
      roles: ['ADMIN']
    }
  });
});

test('buildAuthHeaders returns bearer token only when token exists', () => {
  assert.deepEqual(buildAuthHeaders('token-123'), {
    Authorization: 'Bearer token-123'
  });
  assert.deepEqual(buildAuthHeaders(''), {});
});

test('clearAuthSession removes all stored auth values', () => {
  const storage = createStorage();
  persistAuthSession(
    {
      token: 'token-123',
      userId: 'user-1',
      username: 'admin',
      fullName: '系统管理员',
      roles: ['ADMIN']
    },
    storage
  );

  clearAuthSession(storage);

  assert.deepEqual(readAuthSession(storage), {
    token: '',
    profile: null
  });
});
