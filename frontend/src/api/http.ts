import axios from 'axios';
import { useAuthStore } from '../stores/auth';

const http = axios.create({
  baseURL: '/api',
  timeout: 20000
});

http.interceptors.request.use((config) => {
  const auth = useAuthStore();
  if (auth.token) {
    config.headers.Authorization = `Bearer ${auth.token}`;
  }
  return config;
});

http.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      const auth = useAuthStore();
      auth.logout();
      if (location.pathname !== '/login') {
        location.href = '/login';
      }
    }
    // We don't call notifyError(error) globally here because
    // it's typically handled in the specific catch blocks of API calls.
    // This prevents duplicate error messages.
    return Promise.reject(error);
  }
);

export default http;
