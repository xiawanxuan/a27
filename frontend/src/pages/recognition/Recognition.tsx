import { useState } from 'react';
import {
  Card,
  Upload,
  Button,
  List,
  Tag,
  Progress,
  Empty,
  message,
  Divider,
  Input,
  Modal
} from 'antd';
import {
  InboxOutlined,
  CheckOutlined,
  ReloadOutlined
} from '@ant-design/icons';
import { identifyImage, getRecognitionHistory, confirmRecognition } from '../../api/recognition';
import type { RecognitionResultDetail } from '../../api/recognition';
import { RecognitionRecord } from '../../types';
import './Recognition.css';

const { Dragger } = Upload;

const Recognition = () => {
  const [loading, setLoading] = useState(false);
  const [result, setResult] = useState<RecognitionResultDetail | null>(null);
  const [history, setHistory] = useState<RecognitionRecord[]>([]);
  const [confirmModalVisible, setConfirmModalVisible] = useState(false);
  const [confirmedName, setConfirmedName] = useState('');
  const [currentRecordId, setCurrentRecordId] = useState<number | null>(null);

  const uploadProps = {
    name: 'file',
    multiple: false,
    accept: 'image/*',
    beforeUpload: (file: File) => {
      const isImage = file.type.startsWith('image/');
      if (!isImage) {
        message.error('只能上传图片文件！');
        return false;
      }
      const isLt10M = file.size / 1024 / 1024 < 10;
      if (!isLt10M) {
        message.error('图片大小不能超过10MB！');
        return false;
      }
      return false;
    },
    customRequest: async (options: any) => {
      handleIdentify(options.file);
    }
  };

  const handleIdentify = async (file: File) => {
    setLoading(true);
    try {
      const res: any = await identifyImage(file);
      setResult(res.data || res);
      message.success('识别完成');
      loadHistory();
    } catch (error: any) {
      message.error(error.message || '识别失败');
    } finally {
      setLoading(false);
    }
  };

  const loadHistory = async () => {
    try {
      const result: any = await getRecognitionHistory({ page: 1, pageSize: 10 });
      setHistory(result.list || []);
    } catch (error) {
      console.error('加载历史记录失败', error);
    }
  };

  const handleConfirm = (record: RecognitionResultDetail) => {
    setCurrentRecordId(record.recordId);
    setConfirmedName(record.predictedName);
    setConfirmModalVisible(true);
  };

  const handleSubmitConfirm = async () => {
    if (!currentRecordId) return;
    try {
      await confirmRecognition(currentRecordId, confirmedName);
      message.success('确认成功');
      setConfirmModalVisible(false);
      loadHistory();
    } catch (error: any) {
      message.error(error.message || '确认失败');
    }
  };

  const getConfidenceColor = (confidence: number) => {
    if (confidence >= 0.8) return '#2E7D32';
    if (confidence >= 0.6) return '#1565C0';
    if (confidence >= 0.4) return '#EF6C00';
    return '#F44336';
  };

  return (
    <div className="recognition-page">
      <div className="recognition-left">
        <Card title="图像上传" className="upload-section">
          <Dragger {...uploadProps} disabled={loading}>
            <p className="ant-upload-drag-icon">
              <InboxOutlined />
            </p>
            <p className="ant-upload-text">
              {loading ? '识别中...' : '点击或拖拽图片到此区域'}
            </p>
            <p className="ant-upload-hint">支持JPG、PNG等图片格式，单张图片不超过10MB</p>
          </Dragger>
        </Card>

        {result && (
          <Card
            title="识别结果"
            className="result-card"
            extra={
              <Button
                type="primary"
                size="small"
                icon={<CheckOutlined />}
                onClick={() => handleConfirm(result)}
              >
                确认结果
              </Button>
            }
          >
            <div className="result-preview">
            {result.imageUrl && (
              <img
                src={result.imageUrl}
                alt="识别图片"
                className="preview-image"
              />
            )}
            </div>

            <div className="result-main">
              <h3 className="predicted-name">{result.predictedName}</h3>
              <p className="predicted-latin">{result.predictedLatinName}</p>
              
              <div className="confidence-section">
                <span className="confidence-label">置信度</span>
                <Progress
                  percent={Math.round(result.confidence * 100)}
                  strokeColor={getConfidenceColor(result.confidence)}
                  size="small"
                />
              </div>
            </div>

            <Divider />

            <div className="top-predictions">
              <h4>Top-N 预测结果</h4>
              <List
                dataSource={result.topPredictions}
                renderItem={(item, index) => (
                  <List.Item className="prediction-item">
                    <div className="prediction-rank">{index + 1}</div>
                    <div className="prediction-info">
                      <div className="prediction-name">{item.name}</div>
                      <div className="prediction-latin">{item.latinName}</div>
                    </div>
                    <div className="prediction-confidence">
                      <Progress
                        type="circle"
                        size="small"
                        percent={Math.round(item.confidence * 100)}
                        strokeColor={getConfidenceColor(item.confidence)}
                      />
                    </div>
                  </List.Item>
                )}
              />
            </div>
          </Card>
        )}

        {!result && !loading && (
          <Card className="empty-card">
            <Empty
              image={Empty.PRESENTED_IMAGE_SIMPLE}
              description="上传图片开始识别"
              imageStyle={{ height: 80 }}
            >
              <div className="empty-tips">
                <p>支持叶片、花朵、整株植物均可识别</p>
                <p>图片越清晰，识别准确率越高</p>
              </div>
            </Empty>
          </Card>
        )}
      </div>

      <div className="recognition-right">
        <Card
          title="识别历史"
          className="history-card"
          extra={
            <Button
              type="text"
              size="small"
              icon={<ReloadOutlined />}
              onClick={loadHistory}
            >
              刷新
            </Button>
          }
        >
          {history.length > 0 ? (
            <List
              dataSource={history}
              renderItem={(item) => (
                <List.Item className="history-item">
                  <div className="history-thumb">
                    <img src={item.imageUrl} alt="" />
                  </div>
                  <div className="history-info">
                    <div className="history-name">{item.predictedName || '未知'}</div>
                    <div className="history-meta">
                      <Tag color={item.isConfirmed ? 'green' : 'orange'}>
                        {item.isConfirmed ? '已确认' : '待确认'}
                      </Tag>
                      {item.confidence && (
                        <span className="history-confidence">
                          置信度 {Math.round((item.confidence as number) * 100)}%
                        </span>
                      )}
                    </div>
                  </div>
                </List.Item>
              )}
            />
          ) : (
            <Empty description="暂无识别记录" image={Empty.PRESENTED_IMAGE_SIMPLE} />
          )}
        </Card>
      </div>

      <Modal
        title="确认识别结果"
        open={confirmModalVisible}
        onOk={handleSubmitConfirm}
        onCancel={() => setConfirmModalVisible(false)}
        okText="确认"
        cancelText="取消"
      >
        <div className="confirm-form">
          <label>确认名称</label>
          <Input
            value={confirmedName}
            onChange={(e) => setConfirmedName(e.target.value)}
            placeholder="请输入确认的名称"
          />
        </div>
      </Modal>
    </div>
  );
};

export default Recognition;
