import { message } from 'ant-design-vue';

export const notifyError = (error: any, fallback = '请求失败') => {
  const msg = error?.response?.data?.message || error?.message || fallback;
  message.error(msg);
};

export const notifySuccess = (text: string) => {
  message.success(text);
};
