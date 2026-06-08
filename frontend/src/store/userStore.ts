import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import { RoleCode } from '../utils/auth';

// 用户信息接口
export interface UserInfo {
  id: number;
  username: string;
  realName: string;
  email: string;
  avatar: string;
  roleId: number;
  roleCode: RoleCode | string;
  roleName: string;
  status: number;
}

// 用户Store状态接口
interface UserState {
  token: string;
  userInfo: UserInfo | null;
}

// 用户Store操作接口
interface UserActions {
  setToken: (token: string) => void;
  setUserInfo: (userInfo: UserInfo) => void;
  login: (token: string, userInfo: UserInfo) => void;
  logout: () => void;
  updateUserInfo: (partialInfo: Partial<UserInfo>) => void;
}

// 用户状态Store
export const useUserStore = create<UserState & UserActions>()(
  persist(
    (set) => ({
      token: '',
      userInfo: null,

      setToken: (token: string) => set({ token }),

      setUserInfo: (userInfo: UserInfo) => set({ userInfo }),

      login: (token: string, userInfo: UserInfo) => set({ token, userInfo }),

      logout: () => set({ token: '', userInfo: null }),

      updateUserInfo: (partialInfo: Partial<UserInfo>) =>
        set((state) => ({
          userInfo: state.userInfo ? { ...state.userInfo, ...partialInfo } : null
        }))
    }),
    {
      name: 'herbarium-user-storage',
      partialize: (state) => ({
        token: state.token,
        userInfo: state.userInfo
      })
    }
  )
);

export default useUserStore;
