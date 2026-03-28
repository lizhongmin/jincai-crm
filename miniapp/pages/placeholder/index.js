const DEFAULT_TITLE = '功能规划中';
const DEFAULT_DESC = '当前页面作为产品框架占位，后续会逐步接入真实业务能力。';
Page({
  data: { title: DEFAULT_TITLE, desc: DEFAULT_DESC },
  onLoad(options) { const title = options.title ? decodeURIComponent(options.title) : DEFAULT_TITLE; const desc = options.desc ? decodeURIComponent(options.desc) : DEFAULT_DESC; this.setData({ title, desc }); wx.setNavigationBarTitle({ title }); },
  handleBack() { wx.navigateBack({ delta: 1, fail() { wx.reLaunch({ url: '/pages/workbench/index' }); } }); },
  handleGoWorkbench() { wx.switchTab({ url: '/pages/workbench/index' }); }
});