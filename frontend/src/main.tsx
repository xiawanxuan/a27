import React from 'react'
import ReactDOM from 'react-dom/client'
import { BrowserRouter } from 'react-router-dom'
import { ConfigProvider } from 'antd'
import zhCN from 'antd/locale/zh_CN'
import App from './App'
import './styles/global.css'

// React 入口文件
// 配置路由、Ant Design 国际化等全局配置
ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <ConfigProvider
      locale={zhCN}
      theme={{
        token: {
          colorPrimary: '#2E7D32',
          colorInfo: '#1565C0',
          colorSuccess: '#2E7D32',
          colorWarning: '#795548',
          fontFamily: 'Inter, system-ui, -apple-system, sans-serif',
          colorBgBase: '#FAFAF5',
        },
      }}
    >
      <BrowserRouter>
        <App />
      </BrowserRouter>
    </ConfigProvider>
  </React.StrictMode>,
)
