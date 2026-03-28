const TOKEN_KEY = 'miniapp_token';
const PROFILE_KEY = 'miniapp_profile';

function resolveStorage(storage) {
  return storage || wx;
}

function buildAuthHeaders(token) {
  if (!token) {
    return {};
  }
  return {
    Authorization: `Bearer ${token}`
  };
}

function persistAuthSession(session, storage) {
  const targetStorage = resolveStorage(storage);
  const profile = {
    userId: session.userId,
    username: session.username,
    fullName: session.fullName,
    roles: Array.isArray(session.roles) ? session.roles : []
  };
  targetStorage.setStorageSync(TOKEN_KEY, session.token || '');
  targetStorage.setStorageSync(PROFILE_KEY, profile);
}

function readAuthSession(storage) {
  const targetStorage = resolveStorage(storage);
  const token = targetStorage.getStorageSync(TOKEN_KEY) || '';
  const profile = targetStorage.getStorageSync(PROFILE_KEY) || null;
  return {
    token,
    profile: profile && profile.userId ? profile : null
  };
}

function clearAuthSession(storage) {
  const targetStorage = resolveStorage(storage);
  targetStorage.removeStorageSync(TOKEN_KEY);
  targetStorage.removeStorageSync(PROFILE_KEY);
}

module.exports = {
  TOKEN_KEY,
  PROFILE_KEY,
  buildAuthHeaders,
  persistAuthSession,
  readAuthSession,
  clearAuthSession
};
