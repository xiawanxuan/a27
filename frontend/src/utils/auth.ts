// Token存储键名
const TOKEN_KEY = 'herbarium_token';
// 用户信息存储键名
const USER_INFO_KEY = 'herbarium_user_info';

/**
 * 获取Token
 * @returns Token字符串
 */
export const getToken = (): string | null => {
  return localStorage.getItem(TOKEN_KEY);
};

/**
 * 设置Token
 * @param token Token字符串
 */
export const setToken = (token: string): void => {
  localStorage.setItem(TOKEN_KEY, token);
};

/**
 * 移除Token
 */
export const removeToken = (): void => {
  localStorage.removeItem(TOKEN_KEY);
};

/**
 * 获取用户信息
 * @returns 用户信息对象
 */
export const getUserInfo = <T = any>(): T | null => {
  const userInfoStr = localStorage.getItem(USER_INFO_KEY);
  if (userInfoStr) {
    try {
      return JSON.parse(userInfoStr) as T;
    } catch {
      return null;
    }
  }
  return null;
};

/**
 * 设置用户信息
 * @param userInfo 用户信息对象
 */
export const setUserInfo = <T = any>(userInfo: T): void => {
  localStorage.setItem(USER_INFO_KEY, JSON.stringify(userInfo));
};

/**
 * 移除用户信息
 */
export const removeUserInfo = (): void => {
  localStorage.removeItem(USER_INFO_KEY);
};

/**
 * 清除所有认证信息
 */
export const clearAuth = (): void => {
  removeToken();
  removeUserInfo();
};

/**
 * 检查是否已登录
 * @returns 是否已登录
 */
export const isLoggedIn = (): boolean => {
  return !!getToken();
};

/**
 * 角色编码枚举
 */
export enum RoleCode {
  ADMIN = 'admin',
  SPECIMEN_ADMIN = 'specimen_admin',
  USER = 'user'
}

/**
 * 角色权限等级映射（数值越大权限越高）
 */
const roleLevelMap: Record<RoleCode, number> = {
  [RoleCode.USER]: 1,
  [RoleCode.SPECIMEN_ADMIN]: 2,
  [RoleCode.ADMIN]: 3
};

/**
 * 检查用户是否有指定角色权限
 * @param userRole 用户角色编码
 * @param requiredRole 需要的角色编码
 * @returns 是否有权限
 */
export const hasRole = (userRole: RoleCode | string, requiredRole: RoleCode | string): boolean => {
  const userLevel = roleLevelMap[userRole as RoleCode] || 0;
  const requiredLevel = roleLevelMap[requiredRole as RoleCode] || 0;
  return userLevel >= requiredLevel;
};
