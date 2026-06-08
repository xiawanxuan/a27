import { Routes, Route, Navigate } from 'react-router-dom'
import { Layout } from 'antd'

const { Header, Content, Footer } = Layout

// 根组件
// 配置应用路由和整体布局
function App() {
  return (
    <Layout className="min-h-screen bg-background">
      <Header className="bg-white shadow-sm">
        <div className="text-xl font-serif font-semibold text-primary-600">
          植物标本数字化管理平台
        </div>
      </Header>
      <Content className="px-4 py-6 md:px-8 lg:px-12">
        <Routes>
          <Route path="/" element={<Navigate to="/dashboard" replace />} />
          <Route path="/dashboard" element={<div className="text-center py-20">
            <h1 className="font-serif text-3xl font-bold text-primary-700 mb-4">欢迎使用植物标本数字化管理平台</h1>
            <p className="text-gray-600">平台正在建设中...</p>
          </div>} />
          <Route path="*" element={<div className="text-center py-20">
            <h1 className="font-serif text-2xl font-bold text-gray-700 mb-4">404 - 页面未找到</h1>
            <p className="text-gray-500">您访问的页面不存在</p>
          </div>} />
        </Routes>
      </Content>
      <Footer className="text-center text-gray-500 bg-white">
        植物标本数字化管理平台 ©{new Date().getFullYear()}
      </Footer>
    </Layout>
  )
}

export default App
