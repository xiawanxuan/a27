import { lazy, Suspense, ReactNode, useEffect } from 'react';
import {
  createBrowserRouter,
  Navigate,
  useLocation,
  useNavigate
} from 'react-router-dom';
import { useUserStore } from '../store/userStore';
import { useAppStore } from '../store/appStore';
import { RoleCode, hasRole } from '../utils/auth';

// 页面组件 - 懒加载
const Login = lazy(() => import('../pages/login/Login'));
const Dashboard = lazy(() => import('../pages/dashboard/Dashboard'));
const SpecimenList = lazy(() => import('../pages/specimen/SpecimenList'));
const SpecimenNew = lazy(() => import('../pages/specimen/SpecimenNew'));
const SpecimenDetail = lazy(() => import('../pages/specimen/SpecimenDetail'));
const SpecimenBatch = lazy(() => import('../pages/specimen/SpecimenBatch'));
const Recognition = lazy(() => import('../pages/recognition/Recognition'));
const Analysis = lazy(() => import('../pages/analysis/Analysis'));
const Taxonomy = lazy(() => import('../pages/taxonomy/Taxonomy'));
const UserManagement = lazy(() => import('../pages/user/UserManagement'));
const DataExport = lazy(() => import('../pages/export/DataExport'));
const MainLayout = lazy(() => import('../components/layout/MainLayout'));

// 路由配置接口
interface RouteConfig {
  path?: string;
  index?: boolean;
  element?: ReactNode;
  children?: RouteConfig[];
  meta?: {
    title: string;
    requiresAuth?: boolean;
    requiredRole?: RoleCode | string;
  };
}

// 加载占位组件
const PageLoader = (): JSX.Element => (
  <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh' }}>
    <span>加载中...</span>
  </div>
);

// 路由守卫组件属性
interface ProtectedRouteProps {
  children: ReactNode;
  requiredRole?: RoleCode | string;
}

/**
 * 路由守卫组件
 * 检查登录状态和角色权限
 */
const ProtectedRoute = ({ children, requiredRole }: ProtectedRouteProps): JSX.Element | null => {
  const { token, userInfo } = useUserStore();
  const location = useLocation();
  const navigate = useNavigate();
  const setCurrentRoute = useAppStore((state) => state.setCurrentRoute);

  useEffect(() => {
    setCurrentRoute(location.pathname);
  }, [location.pathname, setCurrentRoute]);

  if (!token) {
    navigate('/login', { replace: true, state: { from: location.pathname } });
    return null;
  }

  if (requiredRole && userInfo) {
    if (!hasRole(userInfo.roleCode, requiredRole)) {
      navigate('/403', { replace: true });
      return null;
    }
  }

  return <>{children}</>;
};

// 公开路由守卫 - 已登录用户访问登录页时重定向
const PublicRoute = ({ children }: { children: ReactNode }): JSX.Element => {
  const { token } = useUserStore();
  const navigate = useNavigate();

  if (token) {
    navigate('/dashboard', { replace: true });
  }

  return <>{children}</>;
};

// 路由配置
const routes: RouteConfig[] = [
  {
    path: '/login',
    element: (
      <PublicRoute>
        <Suspense fallback={<PageLoader />}>
          <Login />
        </Suspense>
      </PublicRoute>
    ),
    meta: { title: '登录', requiresAuth: false }
  },
  {
    path: '/',
    element: (
      <ProtectedRoute>
        <Suspense fallback={<PageLoader />}>
          <MainLayout />
        </Suspense>
      </ProtectedRoute>
    ),
    children: [
      {
        index: true,
        element: <Navigate to="/dashboard" replace />
      },
      {
        path: 'dashboard',
        element: (
          <Suspense fallback={<PageLoader />}>
            <Dashboard />
          </Suspense>
        ),
        meta: { title: '首页仪表盘', requiresAuth: true }
      },
      {
        path: 'specimen',
        children: [
          {
            index: true,
            element: (
              <Suspense fallback={<PageLoader />}>
                <SpecimenList />
              </Suspense>
            ),
            meta: { title: '标本列表', requiresAuth: true }
          },
          {
            path: 'new',
            element: (
              <ProtectedRoute requiredRole={RoleCode.SPECIMEN_ADMIN}>
                <Suspense fallback={<PageLoader />}>
                  <SpecimenNew />
                </Suspense>
              </ProtectedRoute>
            ),
            meta: {
              title: '新增标本',
              requiresAuth: true,
              requiredRole: RoleCode.SPECIMEN_ADMIN
            }
          },
          {
            path: ':id',
            element: (
              <Suspense fallback={<PageLoader />}>
                <SpecimenDetail />
              </Suspense>
            ),
            meta: { title: '标本详情', requiresAuth: true }
          },
          {
            path: 'batch',
            element: (
              <ProtectedRoute requiredRole={RoleCode.SPECIMEN_ADMIN}>
                <Suspense fallback={<PageLoader />}>
                  <SpecimenBatch />
                </Suspense>
              </ProtectedRoute>
            ),
            meta: {
              title: '批量录入',
              requiresAuth: true,
              requiredRole: RoleCode.SPECIMEN_ADMIN
            }
          }
        ]
      },
      {
        path: 'recognition',
        element: (
          <Suspense fallback={<PageLoader />}>
            <Recognition />
          </Suspense>
        ),
        meta: { title: '图像识别', requiresAuth: true }
      },
      {
        path: 'analysis',
        element: (
          <Suspense fallback={<PageLoader />}>
            <Analysis />
          </Suspense>
        ),
        meta: { title: '特征分析', requiresAuth: true }
      },
      {
        path: 'taxonomy',
        element: (
          <ProtectedRoute requiredRole={RoleCode.SPECIMEN_ADMIN}>
            <Suspense fallback={<PageLoader />}>
              <Taxonomy />
            </Suspense>
          </ProtectedRoute>
        ),
        meta: {
          title: '分类管理',
          requiresAuth: true,
          requiredRole: RoleCode.SPECIMEN_ADMIN
        }
      },
      {
        path: 'user',
        element: (
          <ProtectedRoute requiredRole={RoleCode.ADMIN}>
            <Suspense fallback={<PageLoader />}>
              <UserManagement />
            </Suspense>
          </ProtectedRoute>
        ),
        meta: {
          title: '用户管理',
          requiresAuth: true,
          requiredRole: RoleCode.ADMIN
        }
      },
      {
        path: 'export',
        element: (
          <ProtectedRoute requiredRole={RoleCode.SPECIMEN_ADMIN}>
            <Suspense fallback={<PageLoader />}>
              <DataExport />
            </Suspense>
          </ProtectedRoute>
        ),
        meta: {
          title: '数据导出',
          requiresAuth: true,
          requiredRole: RoleCode.SPECIMEN_ADMIN
        }
      }
    ]
  },
  {
    path: '/403',
    element: <div>无权限访问</div>
  },
  {
    path: '*',
    element: <Navigate to="/dashboard" replace />
  }
];

// 创建路由器
const router = createBrowserRouter(routes as any);

export default router;
export { routes, ProtectedRoute, PublicRoute };
export type { RouteConfig };
