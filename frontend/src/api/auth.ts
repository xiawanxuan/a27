import { get, post } from '../utils/request';
import { LoginRequest, LoginResponse } from '../types';

export const loginApi = (data: LoginRequest) => {
  return post<LoginResponse>('/auth/login', data);
};

export const logoutApi = () => {
  return post<void>('/auth/logout');
};

export const getUserInfoApi = () => {
  return get<LoginResponse['userInfo']>('/auth/userinfo');
};
