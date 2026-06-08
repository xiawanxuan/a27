import axios, { AxiosInstance, AxiosRequestConfig, AxiosResponse, InternalAxiosRequestConfig } from 'axios';
import { getToken, clearAuth } from './auth';

// 统一响应接口
export interface ApiResponse<T = any> {
  code: number;
  message: string;
  data: T;
}

// 请求配置
const requestConfig = {
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json'
  }
};

// 创建Axios实例
const instance: AxiosInstance = axios.create(requestConfig);

// 请求拦截器
instance.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = getToken();
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// 响应拦截器
instance.interceptors.response.use(
  (response: AxiosResponse<ApiResponse>) => {
    const { data } = response;
    
    if (data.code === 200) {
      return data.data;
    }
    
    if (data.code === 401) {
      clearAuth();
      window.location.href = '/login';
      return Promise.reject(new Error(data.message || '登录已过期，请重新登录'));
    }
    
    return Promise.reject(new Error(data.message || '请求失败'));
  },
  (error) => {
    if (error.response?.status === 401) {
      clearAuth();
      window.location.href = '/login';
    }
    const message = error.response?.data?.message || error.message || '网络请求失败';
    return Promise.reject(new Error(message));
  }
);

/**
 * GET请求
 * @param url 请求地址
 * @param params 请求参数
 * @param config 请求配置
 * @returns Promise
 */
export const get = <T = any>(
  url: string,
  params?: Record<string, any>,
  config?: AxiosRequestConfig
): Promise<T> => {
  return instance.get<any, T>(url, { params, ...config });
};

/**
 * POST请求
 * @param url 请求地址
 * @param data 请求数据
 * @param config 请求配置
 * @returns Promise
 */
export const post = <T = any>(
  url: string,
  data?: Record<string, any>,
  config?: AxiosRequestConfig
): Promise<T> => {
  return instance.post<any, T>(url, data, config);
};

/**
 * PUT请求
 * @param url 请求地址
 * @param data 请求数据
 * @param config 请求配置
 * @returns Promise
 */
export const put = <T = any>(
  url: string,
  data?: Record<string, any>,
  config?: AxiosRequestConfig
): Promise<T> => {
  return instance.put<any, T>(url, data, config);
};

/**
 * DELETE请求
 * @param url 请求地址
 * @param config 请求配置
 * @returns Promise
 */
export const del = <T = any>(
  url: string,
  config?: AxiosRequestConfig
): Promise<T> => {
  return instance.delete<any, T>(url, config);
};

export default instance;
