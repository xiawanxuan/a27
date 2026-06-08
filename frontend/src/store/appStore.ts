import { create } from 'zustand';
import { persist } from 'zustand/middleware';

// 主题类型
export type ThemeMode = 'light' | 'dark';

// 应用状态接口
interface AppState {
  sidebarCollapsed: boolean;
  currentRoute: string;
  theme: ThemeMode;
  primaryColor: string;
}

// 应用操作接口
interface AppActions {
  toggleSidebar: () => void;
  setSidebarCollapsed: (collapsed: boolean) => void;
  setCurrentRoute: (route: string) => void;
  setTheme: (theme: ThemeMode) => void;
  toggleTheme: () => void;
  setPrimaryColor: (color: string) => void;
  resetSettings: () => void;
}

// 默认设置
const defaultState: AppState = {
  sidebarCollapsed: false,
  currentRoute: '/dashboard',
  theme: 'light',
  primaryColor: '#2E7D32'
};

// 应用状态Store
export const useAppStore = create<AppState & AppActions>()(
  persist(
    (set) => ({
      ...defaultState,

      toggleSidebar: () =>
        set((state) => ({ sidebarCollapsed: !state.sidebarCollapsed })),

      setSidebarCollapsed: (collapsed: boolean) =>
        set({ sidebarCollapsed: collapsed }),

      setCurrentRoute: (route: string) => set({ currentRoute: route }),

      setTheme: (theme: ThemeMode) => set({ theme }),

      toggleTheme: () =>
        set((state) => ({
          theme: state.theme === 'light' ? 'dark' : 'light'
        })),

      setPrimaryColor: (color: string) => set({ primaryColor: color }),

      resetSettings: () => set(defaultState)
    }),
    {
      name: 'herbarium-app-storage',
      partialize: (state) => ({
        sidebarCollapsed: state.sidebarCollapsed,
        theme: state.theme,
        primaryColor: state.primaryColor
      })
    }
  )
);

export default useAppStore;
