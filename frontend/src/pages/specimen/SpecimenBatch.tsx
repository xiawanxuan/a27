import { useState } from 'react';
import {
  Card,
  Upload,
  Button,
  Table,
  Tag,
  message,
  Space,
  Steps,
  Alert,
  Divider
} from 'antd';
import {
  UploadOutlined,
  FileExcelOutlined,
  CheckOutlined,
  ArrowLeftOutlined,
  InboxOutlined
} from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import './SpecimenBatch.css';

const { Step } = Steps;
const { Dragger } = Upload;

const SpecimenBatch = () => {
  const navigate = useNavigate();
  const [currentStep, setCurrentStep] = useState(0);
  const [fileList, setFileList] = useState<any[]>([]);
  const [importing, setImporting] = useState(false);
  const [previewData, setPreviewData] = useState<any[]>([]);
  const [importResult, setImportResult] = useState<any>(null);

  const uploadProps = {
    name: 'file',
    multiple: false,
    accept: '.xlsx,.xls',
    fileList,
    beforeUpload: (file: File) => {
      const isExcel = file.name.endsWith('.xlsx') || file.name.endsWith('.xls');
      if (!isExcel) {
        message.error('只能上传Excel文件！');
        return false;
      }
      const isLt10M = file.size / 1024 / 1024 < 10;
      if (!isLt10M) {
        message.error('文件大小不能超过10MB！');
        return false;
      }
      setFileList([file]);
      generatePreviewData();
      return false;
    },
    onChange(info: any) {
      setFileList(info.fileList);
    },
    onRemove: () => {
      setFileList([]);
      setPreviewData([]);
      setCurrentStep(0);
    }
  };

  const generatePreviewData = () => {
    const mockData = Array.from({ length: 15 }, (_, i) => ({
      key: i + 1,
      specimenNo: `SP20240${i + 1}${String(i + 1).padStart(4, '0')}`,
      name: ['银杏', '水杉', '珙桐', '月季', '玫瑰', '海棠', '桃', '国槐', '垂柳', '白皮松'][i % 10],
      latinName: ['Ginkgo biloba', 'Metasequoia glyptostroboides', 'Davidia involucrata',
        'Rosa chinensis', 'Rosa rugosa', 'Malus spectabilis',
        'Amygdalus persica', 'Sophora japonica', 'Salix babylonica',
        'Pinus bungeana'][i % 10],
      collector: ['张三', '李四', '王五', '赵六', '钱七'][i % 5],
      collectionDate: `2024-0${(i % 9) + 1}-${(i % 28) + 1}`,
      collectionLocation: ['北京植物园', '山东青岛', '江苏南京', '浙江杭州', '陕西西安'][i % 5],
      status: i % 5 === 3 ? 'error' : 'success',
      errorMsg: i % 5 === 3 ? '采集日期格式错误' : ''
    }));
    setPreviewData(mockData);
    setCurrentStep(1);
  };

  const handleImport = () => {
    setImporting(true);
    
    setTimeout(() => {
      const successCount = previewData.filter(item => item.status === 'success').length;
      const failCount = previewData.filter(item => item.status === 'error').length;
      
      setImportResult({
        successCount,
        failCount,
        totalCount: previewData.length
      });
      setCurrentStep(2);
      setImporting(false);
      message.success('导入完成');
    }, 2000);
  };

  const handleReset = () => {
    setCurrentStep(0);
    setFileList([]);
    setPreviewData([]);
    setImportResult(null);
  };

  const columns = [
    {
      title: '序号',
      dataIndex: 'key',
      key: 'key',
      width: 60
    },
    {
      title: '标本编号',
      dataIndex: 'specimenNo',
      key: 'specimenNo',
      width: 140
    },
    {
      title: '中文名',
      dataIndex: 'name',
      key: 'name',
      width: 100
    },
    {
      title: '拉丁名',
      dataIndex: 'latinName',
      key: 'latinName',
      width: 200,
      ellipsis: true
    },
    {
      title: '采集人',
      dataIndex: 'collector',
      key: 'collector',
      width: 80
    },
    {
      title: '采集日期',
      dataIndex: 'collectionDate',
      key: 'collectionDate',
      width: 120
    },
    {
      title: '采集地点',
      dataIndex: 'collectionLocation',
      key: 'collectionLocation',
      width: 150,
      ellipsis: true
    },
    {
      title: '校验状态',
      dataIndex: 'status',
      key: 'status',
      width: 100,
      render: (status: string, record: any) => (
        status === 'success' ? (
          <Tag color="green">通过</Tag>
        ) : (
          <Tag color="red" title={record.errorMsg}>
            错误
          </Tag>
        )
      )
    }
  ];

  return (
    <div className="specimen-batch-page">
      <div className="page-header">
        <Button
          type="text"
          icon={<ArrowLeftOutlined />}
          onClick={() => navigate('/specimen')}
        >
          返回列表
        </Button>
        <h2>批量录入标本</h2>
      </div>

      <Card className="steps-card">
        <Steps current={currentStep} size="small">
          <Step title="上传文件" icon={<UploadOutlined />} />
          <Step title="数据校验" icon={<FileExcelOutlined />} />
          <Step title="导入完成" icon={<CheckOutlined />} />
        </Steps>
      </Card>

      {currentStep === 0 && (
        <Card className="upload-card" title="上传Excel文件">
          <Alert
            message="导入说明"
            description={
              <ul>
                <li>请按照模板格式填写标本数据</li>
                <li>支持 .xlsx 和 .xls 格式的Excel文件</li>
                <li>单次导入最多支持1000条数据</li>
                <li>系统会自动校验数据格式，校验通过后方可导入</li>
              </ul>
            }
            type="info"
            showIcon
            style={{ marginBottom: 24 }}
          />

          <Dragger {...uploadProps}>
            <p className="ant-upload-drag-icon">
              <InboxOutlined />
            </p>
            <p className="ant-upload-text">点击或拖拽Excel文件到此区域上传</p>
            <p className="ant-upload-hint">支持单个Excel文件上传，文件大小不超过10MB</p>
          </Dragger>

          <Divider />

          <div className="template-download">
            <span>没有模板？</span>
            <Button type="link" onClick={() => message.info('模板下载功能开发中')}>
              下载导入模板
            </Button>
          </div>
        </Card>
      )}

      {currentStep === 1 && (
        <Card
          className="preview-card"
          title="数据预览"
          extra={
            <Space>
              <Button onClick={handleReset}>重新上传</Button>
              <Button
                type="primary"
                onClick={handleImport}
                loading={importing}
                icon={<UploadOutlined />}
              >
                确认导入
              </Button>
            </Space>
          }
        >
          <div className="preview-stats">
            <Tag color="green">共 {previewData.length} 条数据</Tag>
            <Tag color="green">
              {previewData.filter(item => item.status === 'success').length} 条通过校验
            </Tag>
            <Tag color="red">
              {previewData.filter(item => item.status === 'error').length} 条校验失败
            </Tag>
          </div>

          <Table
            columns={columns}
            dataSource={previewData}
            rowKey="key"
            scroll={{ x: 1000 }}
            pagination={{
              pageSize: 10,
              showSizeChanger: true,
              showTotal: (total) => `共 ${total} 条`
            }}
          />
        </Card>
      )}

      {currentStep === 2 && importResult && (
        <Card className="result-card">
          <div className="result-content">
            <div className="result-icon success">
              <CheckOutlined />
            </div>
            <h3>导入完成</h3>
            <div className="result-stats">
              <div className="stat-item">
                <span className="stat-label">总计</span>
                <span className="stat-value">{importResult.totalCount}</span>
              </div>
              <div className="stat-item success">
                <span className="stat-label">成功</span>
                <span className="stat-value">{importResult.successCount}</span>
              </div>
              <div className="stat-item error">
                <span className="stat-label">失败</span>
                <span className="stat-value">{importResult.failCount}</span>
              </div>
            </div>
            <Space style={{ marginTop: 24 }}>
              <Button type="primary" onClick={() => navigate('/specimen')}>
                查看标本列表
              </Button>
              <Button onClick={handleReset}>继续导入</Button>
            </Space>
          </div>
        </Card>
      )}
    </div>
  );
};

export default SpecimenBatch;
