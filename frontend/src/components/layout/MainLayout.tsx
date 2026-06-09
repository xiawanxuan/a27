import { useState } from 'react';
import { Layout, Menu, Drawer, Button } from 'antd';
import {
  DashboardOutlined,
  AppstoreOutlined,
  PictureOutlined,
  BarChartOutlined,
  ShareAltOutlined,
  TeamOutlined,
  DownloadOutlined,
  MenuOutlined,
  UserOutlined,
  LogoutOutlined,
  EnvironmentOutlined
} from '@ant-design/icons';
import { Outlet, useNavigate, useLocation } from 'react-router-dom';
import { useUserStore } from '../../store/userStore';
import { useAppStore } from '../../store/appStore';
import { RoleCode, hasRole } from '../../utils/auth';
import './MainLayout.css';

const { Header, Sider, Content } = Layout;

interface MenuItemType {
  key: string;
  icon?: React.ReactNode;
  label: string;
  roles?: string[];
  children?: MenuItemType[];
}

const MainLayout = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { userInfo, logout } = useUserStore();
  const { sidebarCollapsed, toggleSidebar } = useAppStore();
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
  const [userMenuVisible, setUserMenuVisible] = useState(false);

  const menuItems = [
    {
      key: '/dashboard',
      icon: <DashboardOutlined />,
      label: '首页仪表盘',
      roles: [RoleCode.ADMIN, RoleCode.SPECIMEN_ADMIN, RoleCode.USER]
    },
    {
      key: '/specimen',
      icon: <AppstoreOutlined />,
      label: '标本管理',
      roles: [RoleCode.ADMIN, RoleCode.SPECIMEN_ADMIN, RoleCode.USER],
      children: [
        {
          key: '/specimen',
          label: '标本列表',
          roles: [RoleCode.ADMIN, RoleCode.SPECIMEN_ADMIN, RoleCode.USER]
        },
        {
          key: '/specimen/new',
          label: '新增标本',
          roles: [RoleCode.ADMIN, RoleCode.SPECIMEN_ADMIN]
        },
        {
          key: '/specimen/batch',
          label: '批量录入',
          roles: [RoleCode.ADMIN, RoleCode.SPECIMEN_ADMIN]
        }
      ]
    },
    {
      key: '/recognition',
      icon: <PictureOutlined />,
      label: '图像识别',
      roles: [RoleCode.ADMIN, RoleCode.SPECIMEN_ADMIN, RoleCode.USER]
    },
    {
      key: '/analysis',
      icon: <BarChartOutlined />,
      label: '特征分析',
      roles: [RoleCode.ADMIN, RoleCode.SPECIMEN_ADMIN, RoleCode.USER]
    },
    {
      key: '/taxonomy',
      icon: <ShareAltOutlined />,
      label: '分类管理',
      roles: [RoleCode.ADMIN, RoleCode.SPECIMEN_ADMIN]
    },
    {
      key: '/user',
      icon: <TeamOutlined />,
      label: '用户管理',
      roles: [RoleCode.ADMIN]
    },
    {
      key: '/export',
      icon: <DownloadOutlined />,
      label: '数据导出',
      roles: [RoleCode.ADMIN, RoleCode.SPECIMEN_ADMIN]
    }
  ];

  const filterMenuByRole = (items: MenuItemType[]): MenuItemType[] => {
    return items
      .filter((item: MenuItemType) => {
        if (!item.roles) return true;
        return item.roles.some((role: string) => 
          userInfo?.roleCode && hasRole(userInfo.roleCode, role)
        );
      })
      .map((item: MenuItemType) => {
        if (item.children) {
          return {
            ...item,
            children: filterMenuByRole(item.children)
          };
        }
        return item;
      });
  };

  const filteredMenuItems = filterMenuByRole(menuItems);

  const handleMenuClick = ({ key }: { key: string }) => {
    navigate(key);
    setMobileMenuOpen(false);
  };

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const selectedKeys = [location.pathname];
  const openKeys = ['/' + location.pathname.split('/')[1]];

  return (
    <Layout className="main-layout">
      <Sider
        trigger={null}
        collapsible
        collapsed={sidebarCollapsed}
        className="sidebar"
        width={240}
        collapsedWidth={80}
        breakpoint="md"
        onBreakpoint={(broken) => {
          if (broken && mobileMenuOpen) {
            setMobileMenuOpen(false);
          }
        }}
      >
        <div className="logo-container">
          <div className="logo-icon">
            <EnvironmentOutlined />
          </div>
          {!sidebarCollapsed && (
            <span className="logo-text">植物标本管理</span>
          )}
        </div>
        <Menu
          theme="dark"
          mode="inline"
          selectedKeys={selectedKeys}
          defaultOpenKeys={openKeys}
          items={filteredMenuItems as any}
          onClick={handleMenuClick}
          className="sidebar-menu"
        />
      </Sider>

      <Drawer
        placement="left"
        open={mobileMenuOpen}
        onClose={() => setMobileMenuOpen(false)}
        width={240}
        className="md:hidden"
      >
        <div className="logo-container-mobile">
          <div className="logo-icon">
            <EnvironmentOutlined />
          </div>
          <span className="logo-text">植物标本管理</span>
        </div>
        <Menu
          mode="inline"
          selectedKeys={selectedKeys}
          defaultOpenKeys={openKeys}
          items={filteredMenuItems as any}
          onClick={handleMenuClick}
        />
      </Drawer>

      <Layout className="main-content">
        <Header className="header">
          <div className="header-left">
            <Button
              type="text"
              icon={<MenuOutlined />}
              onClick={toggleSidebar}
              className="hidden md:flex"
            />
            <Button
              type="text"
              icon={<MenuOutlined />}
              onClick={() => setMobileMenuOpen(true)}
              className="md:hidden"
            />
            <span className="page-title hidden sm:block">
              {filteredMenuItems.find((item: MenuItemType) => 
                item.key === location.pathname || 
                location.pathname.startsWith(item.key + '/')
              )?.label || '首页'}
            </span>
          </div>
          <div className="header-right">
            <div 
              className="user-info"
              onClick={() => setUserMenuVisible(!userMenuVisible)}
            >
              <div className="user-avatar">
                <UserOutlined />
              </div>
              <span className="user-name hidden sm:inline">
                {userInfo?.realName || userInfo?.username}
              </span>
            </div>
            {userMenuVisible && (
              <div className="user-dropdown">
                <div className="dropdown-item" onClick={handleLogout}>
                  <LogoutOutlined />
                  <span>退出登录</span>
                </div>
              </div>
            )}
          </div>
        </Header>

        <Content className="content-area">
          <Outlet />
        </Content>
      </Layout>
    </Layout>
  );
};

export default MainLayout;
