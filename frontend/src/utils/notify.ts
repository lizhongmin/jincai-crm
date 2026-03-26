import { message } from 'ant-design-vue';

export const notifyError = (error: any, fallback = '请求失败') => {
  // Check if there are field validation errors
  const responseData = error?.response?.data;
  let msg = responseData?.message || error?.message || fallback;
  
  if (responseData?.data && typeof responseData.data === 'object' && !Array.isArray(responseData.data)) {
    const fieldErrors = Object.entries(responseData.data)
      .map(([field, err]) => `${field}: ${err}`)
      .join(', ');
    if (fieldErrors) {
      msg = `${msg} (${fieldErrors})`;
    }
  }

  message.error(msg);
};

export const notifySuccess = (text: string) => {
  message.success(text);
};
