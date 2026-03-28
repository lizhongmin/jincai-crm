const { readAuthSession } = require('./utils/session');

App({
  globalData: {
    profile: null
  },

  onLaunch() {
    const session = readAuthSession();
    this.globalData.profile = session.profile;
  },

  setProfile(profile) {
    this.globalData.profile = profile || null;
  }
});
