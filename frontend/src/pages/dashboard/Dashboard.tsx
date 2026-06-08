import { useState, useEffect } from 'react';
import { Card, Row, Col, Statistic, List, Tag, Button, Space } from 'antd';
import {
  AppstoreOutlined,
  PictureOutlined,
  BarChartOutlined,
  TeamOutlined,
  ArrowRightOutlined,
  PlusOutlined
} from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import ReactECharts from 'echarts-for-react';
import { getSpecimenStats, getRecentSpecimens } from '../../api/specimen';
import { Specimen } from '../../types';
import { useUserStore } from '../../store/userStore';
import { RoleCode, hasRole } from '../../utils/auth';
import './Dashboard.css';

const Dashboard = () => {
  const navigate = useNavigate();
  const { userInfo } = useUserStore();
  const [specimenCount, setSpecimenCount] = useState(0);
  const [recentSpecimens, setRecentSpecimens] = useState<Specimen[]>([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    setLoading(true);
    try {
      const [countRes, recentRes] = await Promise.all([
        getSpecimenStats(),
        getRecentSpecimens(5)
      ]);
      
      setSpecimenCount((countRes as any) || 0);
      setRecentSpecimens((recentRes as any) || []);
    } catch (error) {
      console.error('加载数据失败', error);
    } finally {
      setLoading(false);
    }
  };

  const chartOption = {
    tooltip: {
      trigger: 'axis'
    },
    legend: {
      data: ['标本数量', '识别次数']
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: ['1月', '2月', '3月', '4月', '5月', '6月']
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: '标本数量',
        type: 'line',
        smooth: true,
        data: [120, 132, 101, 134, 90, 230],
        itemStyle: {
          color: '#2E7D32'
        },
        areaStyle: {
          color: {
            type: 'linear',
            x: 0,
            y: 0,
            x2: 0,
            y2: 1,
            colorStops: [
              { offset: 0, color: 'rgba(46, 125, 50, 0.3)' },
              { offset: 1, color: 'rgba(46, 125, 50, 0.05)' }
            ]
          }
        }
      },
      {
        name: '识别次数',
        type: 'line',
        smooth: true,
        data: [220, 182, 191, 234, 290, 330],
        itemStyle: {
          color: '#1565C0'
        },
        areaStyle: {
          color: {
            type: 'linear',
            x: 0,
            y: 0,
            x2: 0,
            y2: 1,
            colorStops: [
              { offset: 0, color: 'rgba(21, 101, 192, 0.3)' },
              { offset: 1, color: 'rgba(21, 101, 192, 0.05)' }
            ]
          }
        }
      }
    ]
  };

  const pieOption = {
    tooltip: {
      trigger: 'item'
    },
    legend: {
      orient: 'vertical',
      left: 'left'
    },
    series: [
      {
        name: '分类分布',
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: false,
          position: 'center'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 20,
            fontWeight: 'bold'
          }
        },
        labelLine: {
          show: false
        },
        data: [
          { value: 1048, name: '被子植物', itemStyle: { color: '#2E7D32' } },
          { value: 735, name: '裸子植物', itemStyle: { color: '#66BB6A' } },
          { value: 580, name: '蕨类植物', itemStyle: { color: '#81C784' } },
          { value: 484, name: '苔藓植物', itemStyle: { color: '#A5D6A7' } }
        ]
      }
    ]
  };

  const canManage = userInfo?.roleCode && hasRole(userInfo.roleCode, RoleCode.SPECIMEN_ADMIN);

  const quickActions = [
    {
      icon: <AppstoreOutlined />,
      title: '标本管理',
      desc: '查看和管理标本',
      path: '/specimen',
      color: '#2E7D32'
    },
    {
      icon: <PictureOutlined />,
      title: '图像识别',
      desc: 'AI智能识别植物',
      path: '/recognition',
      color: '#1565C0'
    },
    {
      icon: <BarChartOutlined />,
      title: '特征分析',
      desc: '形态特征提取',
      path: '/analysis',
      color: '#795548'
    },
    {
      icon: <TeamOutlined />,
      title: '用户管理',
      desc: '用户和权限管理',
      path: '/user',
      color: '#EF6C00',
      admin: true
    }
  ].filter(item => !item.admin || (userInfo?.roleCode && hasRole(userInfo.roleCode, RoleCode.ADMIN)));

  return (
    <div className="dashboard-page">
      <div className="welcome-section">
        <div>
          <h2>欢迎回来，{userInfo?.realName || userInfo?.username}！</h2>
          <p>今天是个好天气，适合研究植物标本 🌿</p>
        </div>
        {canManage && (
          <Button type="primary" icon={<PlusOutlined />} onClick={() => navigate('/specimen/new')}>
            新增标本
          </Button>
        )}
      </div>

      <Row gutter={[16, 16]} className="stats-row">
        <Col xs={12} sm={12} md={6}>
          <Card className="stat-card">
            <Statistic
              title="标本总数"
              value={specimenCount}
              prefix={<AppstoreOutlined style={{ color: '#2E7D32' }} />}
              valueStyle={{ color: '#2E7D32' }}
            />
          </Card>
        </Col>
        <Col xs={12} sm={12} md={6}>
          <Card className="stat-card">
            <Statistic
              title="分类数量"
              value={48}
              prefix={<PictureOutlined style={{ color: '#1565C0' }} />}
              valueStyle={{ color: '#1565C0' }}
            />
          </Card>
        </Col>
        <Col xs={12} sm={12} md={6}>
          <Card className="stat-card">
            <Statistic
              title="识别次数"
              value={1256}
              prefix={<BarChartOutlined style={{ color: '#795548' }} />}
              valueStyle={{ color: '#795548' }}
            />
          </Card>
        </Col>
        <Col xs={12} sm={12} md={6}>
          <Card className="stat-card">
            <Statistic
              title="用户数量"
              value={128}
              prefix={<TeamOutlined style={{ color: '#EF6C00' }} />}
              valueStyle={{ color: '#EF6C00' }}
            />
          </Card>
        </Col>
      </Row>

      <Row gutter={[16, 16]} className="chart-row">
        <Col xs={24} lg={16}>
          <Card title="数据趋势" className="chart-card">
            <ReactECharts option={chartOption} style={{ height: 300 }} />
          </Card>
        </Col>
        <Col xs={24} lg={8}>
          <Card title="分类分布" className="chart-card">
            <ReactECharts option={pieOption} style={{ height: 300 }} />
          </Card>
        </Col>
      </Row>

      <Row gutter={[16, 16]}>
        <Col xs={24} md={12}>
          <Card
            title="快捷操作"
            className="quick-actions-card"
          >
            <Row gutter={[12, 12]}>
              {quickActions.map((action, index) => (
                <Col span={12} key={index}>
                  <div
                    className="action-item"
                    onClick={() => navigate(action.path)}
                    style={{ borderColor: action.color + '30' }}
                  >
                    <div className="action-icon" style={{ background: action.color + '15', color: action.color }}>
                      {action.icon}
                    </div>
                    <div className="action-info">
                      <h4>{action.title}</h4>
                      <p>{action.desc}</p>
                    </div>
                  </div>
                </Col>
              ))}
            </Row>
          </Card>
        </Col>

        <Col xs={24} md={12}>
          <Card
            title="最近录入"
            extra={<a onClick={() => navigate('/specimen')}>查看全部 <ArrowRightOutlined /></a>}
            className="recent-card"
          >
            <List
              dataSource={recentSpecimens}
              loading={loading}
              renderItem={(item) => (
                <List.Item
                  className="recent-item"
                  onClick={() => navigate(`/specimen/${item.id}`)}
                >
                  <List.Item.Meta
                    title={item.name}
                    description={item.specimenNo}
                  />
                  <Space>
                    <Tag color="green">{item.taxonomyName || '未分类'}</Tag>
                  </Space>
                </List.Item>
              )}
            />
          </Card>
        </Col>
      </Row>
    </div>
  );
};

export default Dashboard;
