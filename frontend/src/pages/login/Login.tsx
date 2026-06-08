import { useState } from 'react';
import { Form, Input, Button, Checkbox, Card, message } from 'antd';
import { UserOutlined, LockOutlined, EnvironmentOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import { useUserStore } from '../../store/userStore';
import { loginApi } from '../../api/auth';
import './Login.css';

const Login = () => {
  const navigate = useNavigate();
  const { login } = useUserStore();
  const [loading, setLoading] = useState(false);

  const onFinish = async (values: any) => {
    setLoading(true);
    try {
      const response: any = await loginApi({
        username: values.username,
        password: values.password,
        remember: values.remember
      });
      
      const data = response.data || response;
      if (data) {
        login(data.token, {
          id: data.userInfo.id,
          username: data.userInfo.username,
          realName: data.userInfo.realName,
          email: data.userInfo.email,
          avatar: data.userInfo.avatar,
          roleId: data.userInfo.roleId,
          roleCode: data.userInfo.roleCode,
          roleName: data.userInfo.roleName,
          status: data.userInfo.status
        });
        message.success('登录成功');
        navigate('/dashboard');
      }
    } catch (error: any) {
      message.error(error.message || '登录失败');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-page">
      <div className="login-bg"></div>
      <div className="login-container">
        <div className="login-left hidden lg:flex">
          <div className="login-brand">
            <div className="brand-icon">
              <EnvironmentOutlined />
            </div>
            <h1 className="brand-title">植物标本数字化管理平台</h1>
            <p className="brand-subtitle">
              Herbarium Digital Management &amp; Morphological Analysis Platform
            </p>
          </div>
          <div className="login-features">
            <div className="feature-item">
              <div className="feature-icon">📷</div>
              <div className="feature-text">
                <h3>智能图像识别</h3>
                <p>基于AI的植物图像自动识别</p>
              </div>
            </div>
            <div className="feature-item">
              <div className="feature-icon">📊</div>
              <div className="feature-text">
                <h3>形态特征分析</h3>
                <p>精准提取植物形态特征参数</p>
              </div>
            </div>
            <div className="feature-item">
              <div className="feature-icon">📚</div>
              <div className="feature-text">
                <h3>分类管理体系</h3>
                <p>完善的植物分类层级管理</p>
              </div>
            </div>
          </div>
        </div>

        <div className="login-right">
          <Card className="login-card">
            <div className="login-header">
              <h2>欢迎登录</h2>
              <p>请输入您的账号信息</p>
            </div>

            <Form
              name="login"
              onFinish={onFinish}
              initialValues={{ remember: true }}
              size="large"
            >
              <Form.Item
                name="username"
                rules={[{ required: true, message: '请输入用户名' }]}
              >
                <Input
                  prefix={<UserOutlined />}
                  placeholder="请输入用户名"
                  autoComplete="username"
                />
              </Form.Item>

              <Form.Item
                name="password"
                rules={[{ required: true, message: '请输入密码' }]}
              >
                <Input.Password
                  prefix={<LockOutlined />}
                  placeholder="请输入密码"
                  autoComplete="current-password"
                />
              </Form.Item>

              <Form.Item name="remember" valuePropName="checked">
                <Checkbox>记住我</Checkbox>
              </Form.Item>

              <Form.Item>
                <Button
                  type="primary"
                  htmlType="submit"
                  loading={loading}
                  block
                  className="login-btn"
                >
                  登 录
                </Button>
              </Form.Item>
            </Form>

            <div className="login-footer">
              <p className="demo-account">
                演示账号：admin / admin123
              </p>
            </div>
          </Card>
        </div>
      </div>
    </div>
  );
};

export default Login;
