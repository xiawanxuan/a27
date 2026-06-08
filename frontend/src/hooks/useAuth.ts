import { useCallback, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useUserStore, UserInfo } from '../store/userStore';
import { hasRole, RoleCode, isLoggedIn } from '../utils/auth';
import { get, post } from '../utils/request';

// 登录请求参数
interface LoginParams {
  username: string;
  password: string;
}

// 登录响应数据
interface LoginResponse {
  token: string;
  userInfo: UserInfo;
}

/**
 * 认证相关自定义Hook
 * @returns 认证相关方法和状态
 */
export const useAuth = () => {
  const navigate = useNavigate();
  const { token, userInfo, login, logout, updateUserInfo } = useUserStore();

  /**
   * 登录
   * @param params 登录参数
   * @returns Promise
   */
  const handleLogin = useCallback(
    async (params: LoginParams): Promise<void> => {
      try {
        const response = await post<LoginResponse>('/auth/login', params);
        const { token, userInfo } = response;
        login(token, userInfo);
        navigate('/dashboard', { replace: true });
      } catch (error) {
        throw error;
      }
    },
    [login, navigate]
  );

  /**
   * 登出
   */
  const handleLogout = useCallback(async (): Promise<void> => {
    try {
      await post('/auth/logout');
    } catch (error) {
      console.error('登出请求失败:', error);
    } finally {
      logout();
      navigate('/login', { replace: true });
    }
  }, [logout, navigate]);

  /**
   * 检查是否已登录
   * @returns 是否已登录
   */
  const checkLoggedIn = useCallback((): boolean => {
    return !!token && isLoggedIn();
  }, [token]);

  /**
   * 检查是否有指定角色权限
   * @param requiredRole 需要的角色编码
   * @returns 是否有权限
   */
  const checkHasRole = useCallback(
    (requiredRole: RoleCode | string): boolean => {
      if (!userInfo) return false;
      return hasRole(userInfo.roleCode, requiredRole);
    },
    [userInfo]
  );

  /**
   * 获取当前用户信息
   */
  const fetchUserInfo = useCallback(async (): Promise<void> => {
    try {
      const response = await get<UserInfo>('/auth/userinfo');
      updateUserInfo(response);
    } catch (error) {
      console.error('获取用户信息失败:', error);
    }
  }, [updateUserInfo]);

  /**
   * 初始化认证状态
   * 如果有token但没有用户信息，则获取用户信息
   */
  useEffect(() => {
    if (token && !userInfo) {
      fetchUserInfo();
    }
  }, [token, userInfo, fetchUserInfo]);

  return {
    token,
    userInfo,
    isLoggedIn: checkLoggedIn(),
    hasRole: checkHasRole,
    login: handleLogin,
    logout: handleLogout,
    fetchUserInfo
  };
};

export default useAuth;
