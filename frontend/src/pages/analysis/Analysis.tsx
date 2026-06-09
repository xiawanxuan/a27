import { useState, useEffect } from 'react';
import {
  Card,
  Descriptions,
  Button,
  Select,
  Tag,
  Empty,
  Space,
  List,
  message,
  Row,
  Col,
  Statistic
} from 'antd';
import {
  BarChartOutlined
} from '@ant-design/icons';
import ReactECharts from 'echarts-for-react';
import { getSpecimenList } from '../../api/specimen';
import { getSpecimenFeature, extractFeature, compareFeatures } from '../../api/feature';
import { Specimen } from '../../types';
import type { FeatureExtractResult, FeatureCompareResult } from '../../api/feature';
import './Analysis.css';

const { Option } = Select;

const Analysis = () => {
  const [specimens, setSpecimens] = useState<Specimen[]>([]);
  const [selectedSpecimens, setSelectedSpecimens] = useState<number[]>([]);
  const [currentSpecimen, setCurrentSpecimen] = useState<Specimen | null>(null);
  const [featureData, setFeatureData] = useState<FeatureExtractResult | null>(null);
  const [compareResult, setCompareResult] = useState<FeatureCompareResult | null>(null);
  const [loading, setLoading] = useState(false);
  const [extractLoading, setExtractLoading] = useState(false);
  const [activeTab, setActiveTab] = useState<'single' | 'compare'>('single');

  useEffect(() => {
    loadSpecimens();
  }, []);

  const loadSpecimens = async () => {
    try {
      const result: any = await getSpecimenList({ page: 1, pageSize: 20 });
      setSpecimens(result.list || []);
    } catch (error) {
      console.error('加载标本列表失败', error);
    }
  };

  const handleSpecimenSelect = async (specimenId: number) => {
    setCurrentSpecimen(specimens.find(s => s.id === specimenId) || null);
    setLoading(true);
    try {
      const res: any = await getSpecimenFeature(specimenId);
      setFeatureData(res.data || res);
    } catch (error) {
      setFeatureData(null);
    } finally {
      setLoading(false);
    }
  };

  const handleExtractFeature = async () => {
    if (!currentSpecimen) return;
    setExtractLoading(true);
    try {
      const res: any = await extractFeature(currentSpecimen.id);
      setFeatureData(res.data || res);
      message.success('特征提取成功');
    } catch (error: any) {
      message.error(error.message || '特征提取失败');
    } finally {
      setExtractLoading(false);
    }
  };

  const handleAddToCompare = (specimenId: number) => {
    if (selectedSpecimens.length >= 5) {
      message.warning('最多选择5个标本进行对比');
      return;
    }
    if (!selectedSpecimens.includes(specimenId)) {
      setSelectedSpecimens([...selectedSpecimens, specimenId]);
    }
  };

  const handleRemoveFromCompare = (specimenId: number) => {
    setSelectedSpecimens(selectedSpecimens.filter(id => id !== specimenId));
    if (compareResult) {
      setCompareResult(null);
    }
  };

  const handleCompare = async () => {
    if (selectedSpecimens.length < 2) {
      message.warning('请至少选择2个标本进行对比');
      return;
    }
    setLoading(true);
    try {
      const res: any = await compareFeatures({
        specimenIds: selectedSpecimens,
        features: ['leafLength', 'leafWidth', 'leafArea', 'leafPerimeter', 'aspectRatio']
      });
      setCompareResult(res.data || res);
      message.success('对比完成');
    } catch (error: any) {
      message.error(error.message || '对比失败');
    } finally {
      setLoading(false);
    }
  };

  const getRadarOption = () => {
    if (!compareResult || !compareResult.featureValues) return {};

    const indicators = compareResult.featureNames.map(name => ({
      name: getFeatureLabel(name),
      max: getFeatureMax(name)
    }));

    const series = compareResult.specimenIds.map(id => {
      const specimen = specimens.find(s => s.id === id);
      const values = compareResult.featureNames.map(name => 
        compareResult.featureValues[id]?.[name] || 0
      );
      return {
        value: values,
        name: specimen?.name || `标本${id}`
      };
    });

    return {
      tooltip: {
        trigger: 'item'
      },
      legend: {
        data: series.map(s => s.name),
        bottom: 0
      },
      radar: {
        indicator: indicators,
        shape: 'polygon',
        splitNumber: 5,
        axisName: {
          color: '#333'
        }
      },
      series: [{
        type: 'radar',
        data: series.map((s, i) => ({
          value: s.value,
          name: s.name,
          areaStyle: {
            opacity: 0.2
          },
          lineStyle: {
            width: 2
          },
          itemStyle: {
            color: ['#2E7D32', '#1565C0', '#795548', '#EF6C00', '#6A1B9A'][i % 5]
          }
        }))
      }]
    };
  };

  const getBarOption = () => {
    if (!compareResult || !compareResult.featureValues) return {};

    const categories = compareResult.featureNames.map(name => getFeatureLabel(name));
    
    const series = compareResult.specimenIds.map((id, index) => {
      const specimen = specimens.find(s => s.id === id);
      const data = compareResult.featureNames.map(name => 
        compareResult.featureValues[id]?.[name] || 0
      );
      return {
        name: specimen?.name || `标本${id}`,
        type: 'bar',
        data,
        itemStyle: {
          color: ['#2E7D32', '#1565C0', '#795548', '#EF6C00', '#6A1B9A'][index % 5]
        }
      };
    });

    return {
      tooltip: {
        trigger: 'axis',
        axisPointer: {
          type: 'shadow'
        }
      },
      legend: {
        data: series.map(s => s.name)
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '3%',
        containLabel: true
      },
      xAxis: {
        type: 'category',
        data: categories
      },
      yAxis: {
        type: 'value'
      },
      series
    };
  };

  const getFeatureLabel = (name: string) => {
    const labels: Record<string, string> = {
      leafLength: '叶片长度',
      leafWidth: '叶片宽度',
      leafArea: '叶片面积',
      leafPerimeter: '叶片周长',
      aspectRatio: '长宽比'
    };
    return labels[name] || name;
  };

  const getFeatureMax = (name: string) => {
    const maxes: Record<string, number> = {
      leafLength: 200,
      leafWidth: 100,
      leafArea: 20000,
      leafPerimeter: 600,
      aspectRatio: 5
    };
    return maxes[name] || 100;
  };

  return (
    <div className="analysis-page">
      <Card className="tab-card">
        <div className="tab-header">
          <div className="tabs">
            <span 
              className={`tab ${activeTab === 'single' ? 'active' : ''}`}
              onClick={() => setActiveTab('single')}
            >
              单标本分析
            </span>
            <span 
              className={`tab ${activeTab === 'compare' ? 'active' : ''}`}
              onClick={() => setActiveTab('compare')}
            >
              多标本对比
            </span>
          </div>
        </div>

        {activeTab === 'single' && (
          <div className="single-analysis">
            <div className="select-section">
              <span className="label">选择标本</span>
              <Select
                showSearch
                placeholder="请选择标本"
                style={{ width: 300 }}
                optionFilterProp="children"
                onChange={handleSpecimenSelect}
                filterOption={(input, option) =>
                  String(option?.label ?? '').toLowerCase().includes(input.toLowerCase())
                }
              >
                {specimens.map(specimen => (
                  <Option key={specimen.id} value={specimen.id}>
                    {specimen.name} ({specimen.specimenNo})
                  </Option>
                ))}
              </Select>
            </div>

            {currentSpecimen && featureData ? (
              <div className="feature-content">
                <Row gutter={[16, 16]}>
                  <Col xs={24} md={12}>
                    <Card title="几何特征" className="feature-card">
                      <div className="feature-stats">
                        <div className="stat-item">
                          <Statistic
                            title="叶片长度"
                            value={featureData.leafLength}
                            suffix="mm"
                            valueStyle={{ color: '#2E7D32' }}
                          />
                        </div>
                        <div className="stat-item">
                          <Statistic
                            title="叶片宽度"
                            value={featureData.leafWidth}
                            suffix="mm"
                            valueStyle={{ color: '#1565C0' }}
                          />
                        </div>
                        <div className="stat-item">
                          <Statistic
                            title="叶片面积"
                            value={featureData.leafArea}
                            suffix="mm²"
                            valueStyle={{ color: '#795548' }}
                          />
                        </div>
                        <div className="stat-item">
                          <Statistic
                            title="叶片周长"
                            value={featureData.leafPerimeter}
                            suffix="mm"
                            valueStyle={{ color: '#EF6C00' }}
                          />
                        </div>
                      </div>
                    </Card>
                  </Col>
                  <Col xs={24} md={12}>
                    <Card title="形态特征" className="feature-card">
                      <Descriptions column={2} size="small">
                        <Descriptions.Item label="叶形">
                          <Tag color="green">{featureData.leafShape}</Tag>
                        </Descriptions.Item>
                        <Descriptions.Item label="叶缘">
                          <Tag color="blue">{featureData.leafMargin}</Tag>
                        </Descriptions.Item>
                        <Descriptions.Item label="叶端">
                          <Tag color="orange">{featureData.leafApex}</Tag>
                        </Descriptions.Item>
                        <Descriptions.Item label="叶基">
                          <Tag color="purple">{featureData.leafBase}</Tag>
                        </Descriptions.Item>
                        <Descriptions.Item label="质地" span={2}>
                          <Tag color="cyan">{featureData.texture}</Tag>
                        </Descriptions.Item>
                        <Descriptions.Item label="长宽比" span={2}>
                          {featureData.aspectRatio}
                        </Descriptions.Item>
                      </Descriptions>
                    </Card>
                  </Col>
                </Row>

                {featureData.colorFeatures && (
                  <Card title="颜色特征" className="feature-card">
                    <pre className="color-features-json">
                      {JSON.stringify(featureData.colorFeatures, null, 2)}
                    </pre>
                  </Card>
                )}
              </div>
            ) : currentSpecimen && !featureData && !loading ? (
              <div className="empty-feature">
                <Empty
                  description="该标本暂无特征数据"
                  image={Empty.PRESENTED_IMAGE_SIMPLE}
                >
                  <Button
                    type="primary"
                    icon={<BarChartOutlined />}
                    onClick={handleExtractFeature}
                    loading={extractLoading}
                  >
                    提取形态特征
                  </Button>
                </Empty>
              </div>
            ) : (
              <div className="empty-feature">
                <Empty
                  description="请选择标本查看特征数据"
                  image={Empty.PRESENTED_IMAGE_SIMPLE}
                />
              </div>
            )}
          </div>
        )}

        {activeTab === 'compare' && (
          <div className="compare-analysis">
            <div className="compare-select">
              <div className="select-section">
                <span className="label">添加对比标本</span>
                <Select
                  showSearch
                  placeholder="选择标本添加到对比"
                  style={{ width: 300 }}
                  optionFilterProp="children"
                  onChange={handleAddToCompare}
                  filterOption={(input, option) =>
                  String(option?.label ?? '').toLowerCase().includes(input.toLowerCase())
                }
                >
                  {specimens
                    .filter(s => !selectedSpecimens.includes(s.id))
                    .map(specimen => (
                      <Option key={specimen.id} value={specimen.id}>
                        {specimen.name} ({specimen.specimenNo})
                      </Option>
                    ))}
                </Select>
              </div>

              {selectedSpecimens.length > 0 && (
                <div className="selected-list">
                  <span className="label">已选择 {selectedSpecimens.length} 个标本</span>
                  <Space wrap>
                    {selectedSpecimens.map(id => {
                      const specimen = specimens.find(s => s.id === id);
                      return (
                        <Tag
                          key={id}
                          color="green"
                          closable
                          onClose={() => handleRemoveFromCompare(id)}
                          style={{ fontSize: 14, padding: '4px 8px' }}
                        >
                          {specimen?.name || `标本${id}`}
                        </Tag>
                      );
                    })}
                  </Space>
                </div>
              )}

              <Button
                type="primary"
                icon={<BarChartOutlined />}
                onClick={handleCompare}
                disabled={selectedSpecimens.length < 2}
                loading={loading}
              >
                开始对比
              </Button>
            </div>

            {compareResult && (
              <div className="compare-result">
                <Row gutter={[16, 16]}>
                  <Col xs={24} lg={12}>
                    <Card title="雷达图对比" className="chart-card">
                      <ReactECharts option={getRadarOption()} style={{ height: 350 }} />
                    </Card>
                  </Col>
                  <Col xs={24} lg={12}>
                    <Card title="柱状图对比" className="chart-card">
                      <ReactECharts option={getBarOption()} style={{ height: 350 }} />
                    </Card>
                  </Col>
                </Row>

                {compareResult.similarSpecimens && compareResult.similarSpecimens.length > 0 && (
                  <Card title="相似度分析" className="similarity-card">
                    <List
                      dataSource={compareResult.similarSpecimens}
                      renderItem={(item) => (
                        <List.Item>
                          <Tag color="green">高相似度</Tag>
                          <span>{item}</span>
                        </List.Item>
                      )}
                    />
                  </Card>
                )}
              </div>
            )}
          </div>
        )}
      </Card>
    </div>
  );
};

export default Analysis;
