import { get, post, put, del } from '../utils/request';
import { User, PageResult, Role } from '../types';

export interface UserCreateParams {
  username: string;
  password: string;
  realName?: string;
  email?: string;
  avatar?: string;
  roleId: number;
  status?: number;
}

export interface UserUpdateParams {
  realName?: string;
  email?: string;
  avatar?: string;
  roleId?: number;
  status?: number;
}

export const getUserList = (params: {
  page?: number;
  pageSize?: number;
  keyword?: string;
  status?: number;
}) => {
  return get<PageResult<User>>('/users', params);
};

export const getUserDetail = (id: number) => {
  return get<User>(`/users/${id}`);
};

export const createUser = (data: UserCreateParams) => {
  return post<User>('/users', data);
};

export const updateUser = (id: number, data: UserUpdateParams) => {
  return put<User>(`/users/${id}`, data);
};

export const deleteUser = (id: number) => {
  return del<void>(`/users/${id}`);
};

export const updateUserPassword = (id: number, oldPassword: string, newPassword: string) => {
  return put<void>(`/users/${id}/password`, { oldPassword, newPassword });
};

export const getRoleList = () => {
  return get<Role[]>('/users/roles');
};
