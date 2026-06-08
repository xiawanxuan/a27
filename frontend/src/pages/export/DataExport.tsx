import { useState, useEffect } from 'react';
import {
  Card,
  Button,
  Form,
  Select,
  DatePicker,
  Checkbox,
  Table,
  Tag,
  Space,
  message,
  Progress,
  Modal,
  Radio
} from 'antd';
import {
  ExportOutlined,
  DownloadOutlined,
  FileExcelOutlined,
  ReloadOutlined
} from '@ant-design/icons';
import { exportSpecimens, getExportTaskList, getExportDownloadUrl } from '../../api/export';
import { ExportTask } from '../../types';
import './DataExport.css';

const { RangePicker } = DatePicker;
const { Option } = Select;

const DataExport = () => {
  const [form] = Form.useForm();
  const [loading, setLoading] = useState(false);
  const [tasks, setTasks] = useState<ExportTask[]>([]);
  const [exporting, setExporting] = useState(false);
  const [progress, setProgress] = useState(0);
  const [showProgress, setShowProgress] = useState(false);
  const [exportType, setExportType] = useState('specimen');

  useEffect(() => {
    loadTasks();
  }, []);

  const loadTasks = async () => {
    setLoading(true);
    try {
      const result: any = await getExportTaskList({ page: 1, pageSize: 10 });
      setTasks(result.list || []);
    } catch (error: any) {
      message.error(error.message || '加载导出任务失败');
    } finally {
      setLoading(false);
    }
  };

  const handleExport = async () => {
    try {
      const values = await form.validateFields();
      
      setShowProgress(true);
      setExporting(true);
      setProgress(0);

      const interval = setInterval(() => {
        setProgress(p => {
          if (p >= 90) {
            clearInterval(interval);
            return p;
          }
          return p + 10;
        });
      }, 300);

      await exportSpecimens({
        ...values
      });

      clearInterval(interval);
      setProgress(100);
      setExporting(false);
      message.success('导出成功');
      
      setTimeout(() => {
        setShowProgress(false);
        loadTasks();
      }, 1000);
    } catch (error: any) {
      setExporting(false);
      message.error(error.message || '导出失败');
    }
  };

  const handleDownload = async (task: ExportTask) => {
    if (task.status !== 'SUCCESS') return;
    
    try {
      const result: any = await getExportDownloadUrl(task.id);
      if (result && result.url) {
        window.open(result.url);
      }
      message.success('开始下载');
    } catch (error: any) {
      message.error(error.message || '下载失败');
    }
  };

  const fieldOptions = {
    specimen: [
      { label: '标本编号', value: 'specimenNo' },
      { label: '中文名', value: 'name' },
      { label: '拉丁名', value: 'latinName' },
      { label: '分类', value: 'taxonomy' },
      { label: '采集人', value: 'collector' },
      { label: '采集日期', value: 'collectionDate' },
      { label: '采集地点', value: 'collectionLocation' },
      { label: '生境', value: 'habitat' },
      { label: '描述', value: 'description' }
    ],
    feature: [
      { label: '叶片长度', value: 'leafLength' },
      { label: '叶片宽度', value: 'leafWidth' },
      { label: '叶片面积', value: 'leafArea' },
      { label: '叶片周长', value: 'leafPerimeter' },
      { label: '长宽比', value: 'aspectRatio' },
      { label: '叶形', value: 'leafShape' },
      { label: '叶缘', value: 'leafMargin' },
      { label: '叶端', value: 'leafApex' },
      { label: '叶基', value: 'leafBase' }
    ]
  };

  const columns = [
    {
      title: '任务名称',
      dataIndex: 'taskName',
      key: 'taskName',
      ellipsis: true
    },
    {
      title: '导出类型',
      dataIndex: 'type',
      key: 'type',
      width: 100,
      render: (type: string) => {
        const typeMap: Record<string, string> = {
          specimen: '标本数据',
          feature: '特征数据'
        };
        return typeMap[type] || type;
      }
    },
    {
      title: '文件格式',
      dataIndex: 'format',
      key: 'format',
      width: 100,
      render: (format: string) => (
        <Tag icon={<FileExcelOutlined />} color="green">
          {format?.toUpperCase() || 'Excel'}
        </Tag>
      )
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: 100,
      render: (status: string) => {
        const statusMap: Record<string, { color: string; text: string }> = {
          PENDING: { color: 'orange', text: '等待中' },
          PROCESSING: { color: 'blue', text: '处理中' },
          SUCCESS: { color: 'green', text: '已完成' },
          FAILED: { color: 'red', text: '失败' }
        };
        const s = statusMap[status] || { color: 'default', text: status };
        return <Tag color={s.color}>{s.text}</Tag>;
      }
    },
    {
      title: '数据条数',
      dataIndex: 'totalCount',
      key: 'totalCount',
      width: 100
    },
    {
      title: '创建时间',
      dataIndex: 'createdAt',
      key: 'createdAt',
      width: 160
    },
    {
      title: '操作',
      key: 'action',
      width: 120,
      render: (_: any, record: ExportTask) => (
        <Space size="small">
          {record.status === 'SUCCESS' ? (
            <Button
              type="link"
              size="small"
              icon={<DownloadOutlined />}
              onClick={() => handleDownload(record)}
            >
              下载
            </Button>
          ) : (
            <span style={{ color: '#999' }}>-</span>
          )}
        </Space>
      )
    }
  ];

  const getSelectedFields = () => {
    const fields = fieldOptions[exportType as keyof typeof fieldOptions] || [];
    return fields.map(f => f.value);
  };

  return (
    <div className="data-export-page">
      <div className="export-main">
        <Card title="数据导出" className="export-card">
          <div className="export-type-select">
            <Radio.Group
              value={exportType}
              onChange={(e) => {
                setExportType(e.target.value);
                form.setFieldsValue({ fields: getSelectedFields() });
              }}
              buttonStyle="solid"
              size="large"
            >
              <Radio.Button value="specimen">标本数据</Radio.Button>
              <Radio.Button value="feature">特征数据</Radio.Button>
              <Radio.Button value="recognition">识别记录</Radio.Button>
            </Radio.Group>
          </div>

          <Form
            form={form}
            layout="vertical"
            className="export-form"
            initialValues={{ fields: getSelectedFields(), format: 'xlsx' }}
          >
            <div className="form-row">
              <Form.Item
                name="dateRange"
                label="日期范围"
                className="form-item"
              >
                <RangePicker
                  style={{ width: '100%' }}
                  placeholder={['开始日期', '结束日期']}
                />
              </Form.Item>

              <Form.Item
                name="format"
                label="文件格式"
                className="form-item"
              >
                <Select>
                  <Option value="xlsx">Excel (.xlsx)</Option>
                  <Option value="xls">Excel (.xls)</Option>
                  <Option value="csv">CSV (.csv)</Option>
                </Select>
              </Form.Item>
            </div>

            {exportType !== 'recognition' && (
              <Form.Item
                name="fields"
                label="导出字段"
              >
                <Checkbox.Group style={{ width: '100%' }}>
                  <div className="field-options">
                    {(fieldOptions[exportType as keyof typeof fieldOptions] || []).map(option => (
                      <Checkbox key={option.value} value={option.value}>
                        {option.label}
                      </Checkbox>
                    ))}
                  </div>
                </Checkbox.Group>
              </Form.Item>
            )}

            <Form.Item>
              <Button
                type="primary"
                size="large"
                icon={<ExportOutlined />}
                onClick={handleExport}
                loading={exporting}
                block
              >
                开始导出
              </Button>
            </Form.Item>
          </Form>
        </Card>

        <Card
          title="导出历史"
          className="history-card"
          extra={
            <Button
              type="text"
              icon={<ReloadOutlined />}
              onClick={loadTasks}
            >
              刷新
            </Button>
          }
        >
          <Table
            columns={columns}
            dataSource={tasks}
            rowKey="id"
            loading={loading}
            pagination={{
              pageSize: 5,
              showSizeChanger: false,
              showTotal: (total) => `共 ${total} 条`
            }}
          />
        </Card>
      </div>

      <Modal
        title="导出进度"
        open={showProgress}
        onCancel={() => !exporting && setShowProgress(false)}
        footer={null}
        closable={!exporting}
      >
        <div className="progress-modal">
          <Progress
            type="circle"
            percent={progress}
            strokeColor="#2E7D32"
            size={120}
          />
          <p className="progress-text">
            {exporting ? '正在处理数据...' : '导出完成！'}
          </p>
          {!exporting && progress === 100 && (
            <Button
              type="primary"
              icon={<DownloadOutlined />}
              onClick={() => setShowProgress(false)}
            >
              查看结果
            </Button>
          )}
        </div>
      </Modal>
    </div>
  );
};

export default DataExport;
