import { useState, useEffect } from 'react';
import {
  Card,
  Descriptions,
  Button,
  Tag,
  Image,
  Tabs,
  Empty,
  message
} from 'antd';
import {
  ArrowLeftOutlined,
  EditOutlined,
  BarChartOutlined,
  PictureOutlined
} from '@ant-design/icons';
import { useNavigate, useParams } from 'react-router-dom';
import { getSpecimenDetail } from '../../api/specimen';
import { getSpecimenFeature, extractFeature } from '../../api/feature';
import { Specimen } from '../../types';
import { useUserStore } from '../../store/userStore';
import { RoleCode, hasRole } from '../../utils/auth';
import type { FeatureExtractResult } from '../../api/feature';
import './SpecimenDetail.css';

const SpecimenDetail = () => {
  const navigate = useNavigate();
  const { id } = useParams<{ id: string }>();
  const { userInfo } = useUserStore();
  const [loading, setLoading] = useState(false);
  const [featureLoading, setFeatureLoading] = useState(false);
  const [specimen, setSpecimen] = useState<Specimen | null>(null);
  const [featureData, setFeatureData] = useState<FeatureExtractResult | null>(null);
  const [activeTab, setActiveTab] = useState('info');

  const canManage = userInfo?.roleCode && hasRole(userInfo.roleCode, RoleCode.SPECIMEN_ADMIN);

  useEffect(() => {
    if (id) {
      loadSpecimenDetail(Number(id));
      loadFeatureData(Number(id));
    }
  }, [id]);

  const loadSpecimenDetail = async (specimenId: number) => {
    setLoading(true);
    try {
      const res: any = await getSpecimenDetail(specimenId);
      setSpecimen(res.data || res);
    } catch (error: any) {
      message.error(error.message || '加载标本详情失败');
    } finally {
      setLoading(false);
    }
  };

  const loadFeatureData = async (specimenId: number) => {
    try {
      const res: any = await getSpecimenFeature(specimenId);
      setFeatureData(res.data || res);
    } catch (error) {
      // 特征数据可能不存在，不显示错误
    }
  };

  const handleExtractFeature = async () => {
    if (!id) return;
    setFeatureLoading(true);
    try {
      const data = await extractFeature(Number(id));
      setFeatureData(data);
      message.success('特征提取成功');
    } catch (error: any) {
      message.error(error.message || '特征提取失败');
    } finally {
      setFeatureLoading(false);
    }
  };

  const tabItems = [
    {
      key: 'info',
      label: '基本信息',
      children: specimen ? (
        <Descriptions column={{ xs: 1, sm: 2, md: 3 }} bordered size="middle">
          <Descriptions.Item label="标本编号">{specimen.specimenNo}</Descriptions.Item>
          <Descriptions.Item label="中文名">
            <strong>{specimen.name}</strong>
          </Descriptions.Item>
          <Descriptions.Item label="拉丁名" className="latin-text">
            {specimen.latinName || '-'}
          </Descriptions.Item>
          <Descriptions.Item label="分类">
            <Tag color="green">{specimen.taxonomyName || '未分类'}</Tag>
          </Descriptions.Item>
          <Descriptions.Item label="采集人">{specimen.collector || '-'}</Descriptions.Item>
          <Descriptions.Item label="采集日期">{specimen.collectionDate || '-'}</Descriptions.Item>
          <Descriptions.Item label="采集地点" span={2}>
            {specimen.collectionLocation || '-'}
          </Descriptions.Item>
          <Descriptions.Item label="状态">
            <Tag color={specimen.status === 1 ? 'green' : 'red'}>
              {specimen.status === 1 ? '正常' : '禁用'}
            </Tag>
          </Descriptions.Item>
          <Descriptions.Item label="纬度">{specimen.latitude || '-'}</Descriptions.Item>
          <Descriptions.Item label="经度">{specimen.longitude || '-'}</Descriptions.Item>
          <Descriptions.Item label="生境" span={2}>{specimen.habitat || '-'}</Descriptions.Item>
          <Descriptions.Item label="创建人">{specimen.creatorName || '-'}</Descriptions.Item>
          <Descriptions.Item label="创建时间">{specimen.createdAt || '-'}</Descriptions.Item>
          <Descriptions.Item label="更新时间">{specimen.updatedAt || '-'}</Descriptions.Item>
          <Descriptions.Item label="描述" span={3}>
            {specimen.description || '暂无描述'}
          </Descriptions.Item>
        </Descriptions>
      ) : <Empty description="暂无数据" />
    },
    {
      key: 'images',
      label: '标本图片',
      icon: <PictureOutlined />,
      children: specimen?.images && specimen.images.length > 0 ? (
        <div className="image-gallery">
          <Image.PreviewGroup>
            {specimen.images.map((img, index) => (
              <div key={index} className="image-item">
                <Image
                  src={img.imageUrl}
                  alt={`${specimen.name}-${index + 1}`}
                  width="100%"
                  height={200}
                  style={{ objectFit: 'cover', borderRadius: 8 }}
                />
              </div>
            ))}
          </Image.PreviewGroup>
        </div>
      ) : <Empty description="暂无图片" />
    },
    {
      key: 'feature',
      label: '形态特征',
      icon: <BarChartOutlined />,
      children: (
        <div className="feature-section">
          {featureData ? (
            <>
              <Descriptions column={{ xs: 1, sm: 2, md: 3 }} bordered size="middle">
                <Descriptions.Item label="叶片长度">
                  {featureData.leafLength} mm
                </Descriptions.Item>
                <Descriptions.Item label="叶片宽度">
                  {featureData.leafWidth} mm
                </Descriptions.Item>
                <Descriptions.Item label="叶片面积">
                  {featureData.leafArea} mm²
                </Descriptions.Item>
                <Descriptions.Item label="叶片周长">
                  {featureData.leafPerimeter} mm
                </Descriptions.Item>
                <Descriptions.Item label="长宽比">
                  {featureData.aspectRatio}
                </Descriptions.Item>
                <Descriptions.Item label="叶形">
                  <Tag color="blue">{featureData.leafShape}</Tag>
                </Descriptions.Item>
                <Descriptions.Item label="叶缘">
                  <Tag color="green">{featureData.leafMargin}</Tag>
                </Descriptions.Item>
                <Descriptions.Item label="叶端">
                  <Tag color="orange">{featureData.leafApex}</Tag>
                </Descriptions.Item>
                <Descriptions.Item label="叶基">
                  <Tag color="purple">{featureData.leafBase}</Tag>
                </Descriptions.Item>
                <Descriptions.Item label="质地">
                  <Tag color="cyan">{featureData.texture}</Tag>
                </Descriptions.Item>
                <Descriptions.Item label="提取时间" span={2}>
                  {featureData.extractedAt}
                </Descriptions.Item>
              </Descriptions>

              {featureData.colorFeatures && (
                <div className="color-features">
                  <h4>颜色特征</h4>
                  <pre>{JSON.stringify(featureData.colorFeatures, null, 2)}</pre>
                </div>
              )}
            </>
          ) : (
            <Empty
              description="暂无特征数据"
              image={Empty.PRESENTED_IMAGE_SIMPLE}
            >
              <Button
                type="primary"
                icon={<BarChartOutlined />}
                onClick={handleExtractFeature}
                loading={featureLoading}
              >
                提取形态特征
              </Button>
            </Empty>
          )}
        </div>
      )
    }
  ];

  return (
    <div className="specimen-detail-page">
      <div className="page-header">
        <Button
          type="text"
          icon={<ArrowLeftOutlined />}
          onClick={() => navigate('/specimen')}
        >
          返回列表
        </Button>
        <div className="header-actions">
          {canManage && (
            <Button
              type="primary"
              icon={<EditOutlined />}
              onClick={() => navigate(`/specimen/new?id=${id}`)}
            >
              编辑
            </Button>
          )}
        </div>
      </div>

      <Card className="detail-card" loading={loading}>
        <div className="specimen-title">
          <h2>{specimen?.name || '标本详情'}</h2>
          <Tag color="green">{specimen?.specimenNo}</Tag>
        </div>

        <Tabs
          activeKey={activeTab}
          onChange={setActiveTab}
          items={tabItems}
          className="detail-tabs"
        />
      </Card>
    </div>
  );
};

export default SpecimenDetail;
