import { useState, useEffect } from 'react';
import {
  Card,
  Form,
  Input,
  Button,
  Select,
  DatePicker,
  InputNumber,
  Upload,
  message,
  Space,
  Row,
  Col,
  Divider
} from 'antd';
import {
  ArrowLeftOutlined,
  UploadOutlined,
  SaveOutlined
} from '@ant-design/icons';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { createSpecimen, updateSpecimen, getSpecimenDetail, uploadSpecimenImage } from '../../api/specimen';
import { getTaxonomyTree } from '../../api/taxonomy';
import { Taxonomy, Specimen } from '../../types';
import dayjs from 'dayjs';
import './SpecimenForm.css';

const { TextArea } = Input;
const { Option } = Select;

const SpecimenNew = () => {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const editId = searchParams.get('id');
  const [form] = Form.useForm();
  const [loading, setLoading] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [taxonomyTree, setTaxonomyTree] = useState<Taxonomy[]>([]);
  const [imageList, setImageList] = useState<any[]>([]);

  const isEdit = !!editId;

  useEffect(() => {
    loadTaxonomyTree();
    if (isEdit) {
      loadSpecimenDetail(Number(editId));
    }
  }, [editId]);

  const loadTaxonomyTree = async () => {
    try {
      const data: any = await getTaxonomyTree();
      setTaxonomyTree(data || []);
    } catch (error) {
      console.error('加载分类树失败', error);
    }
  };

  const loadSpecimenDetail = async (id: number) => {
    setLoading(true);
    try {
      const specimen: Specimen = await getSpecimenDetail(id);
      
      form.setFieldsValue({
        ...specimen,
        collectionDate: specimen.collectionDate ? dayjs(specimen.collectionDate) : null
      });
      
      if (specimen.images && specimen.images.length > 0) {
        setImageList(specimen.images.map((img, index) => ({
          uid: String(index),
          name: `图片${index + 1}`,
          status: 'done',
          url: img.imageUrl
        })));
      }
    } catch (error: any) {
      message.error(error.message || '加载标本详情失败');
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = async (values: any) => {
    setSubmitting(true);
    try {
      const data = {
        ...values,
        collectionDate: values.collectionDate ? values.collectionDate.format('YYYY-MM-DD') : undefined,
        imageUrls: imageList.filter(item => item.url || item.response?.url).map(item => item.url || item.response?.url)
      };

      if (isEdit) {
        await updateSpecimen(Number(editId), data);
        message.success('更新成功');
      } else {
        await createSpecimen(data);
        message.success('创建成功');
      }
      
      navigate('/specimen');
    } catch (error: any) {
      message.error(error.message || (isEdit ? '更新失败' : '创建失败'));
    } finally {
      setSubmitting(false);
    }
  };

  const uploadProps = {
    name: 'file',
    action: '',
    customRequest: async (options: any) => {
      try {
        const data: any = await uploadSpecimenImage(options.file);
        const url = data.url;
        options.onSuccess?.({ url }, options.file);
        return { url };
      } catch (error: any) {
        options.onError?.(error);
        throw error;
      }
    },
    onChange: (info: any) => {
      if (info.file.status === 'done') {
        message.success(`${info.file.name} 上传成功`);
      } else if (info.file.status === 'error') {
        message.error(`${info.file.name} 上传失败`);
      }
      setImageList(info.fileList);
    },
    fileList: imageList,
    listType: 'picture-card' as const,
    multiple: true,
    accept: 'image/*'
  };

  const renderTaxonomyOptions = (items: Taxonomy[], level = 0): any[] => {
    let options: any[] = [];
    items.forEach(item => {
      options.push(
        <Option key={item.id} value={item.id}>
          {'　'.repeat(level)}{item.name}
        </Option>
      );
      if (item.children && item.children.length > 0) {
        options = options.concat(renderTaxonomyOptions(item.children, level + 1));
      }
    });
    return options;
  };

  return (
    <div className="specimen-form-page">
      <div className="page-header">
        <Button
          type="text"
          icon={<ArrowLeftOutlined />}
          onClick={() => navigate('/specimen')}
        >
          返回列表
        </Button>
        <h2>{isEdit ? '编辑标本' : '新增标本'}</h2>
      </div>

      <Card className="form-card" loading={loading}>
        <Form
          form={form}
          layout="vertical"
          onFinish={handleSubmit}
          initialValues={{ status: 1 }}
        >
          <Divider orientation="left">基本信息</Divider>
          
          <Row gutter={24}>
            <Col xs={24} md={12}>
              <Form.Item
                name="name"
                label="中文名"
                rules={[{ required: true, message: '请输入中文名' }]}
              >
                <Input placeholder="请输入中文名" />
              </Form.Item>
            </Col>
            <Col xs={24} md={12}>
              <Form.Item name="latinName" label="拉丁名">
                <Input placeholder="请输入拉丁名" />
              </Form.Item>
            </Col>
          </Row>

          <Row gutter={24}>
            <Col xs={24} md={12}>
              <Form.Item name="taxonomyId" label="分类">
                <Select
                  placeholder="请选择分类"
                  showSearch
                  optionFilterProp="children"
                  filterOption
                >
                  {renderTaxonomyOptions(taxonomyTree)}
                </Select>
              </Form.Item>
            </Col>
            <Col xs={24} md={12}>
              <Form.Item name="status" label="状态">
                <Select>
                  <Option value={1}>正常</Option>
                  <Option value={0}>禁用</Option>
                </Select>
              </Form.Item>
            </Col>
          </Row>

          <Divider orientation="left">采集信息</Divider>

          <Row gutter={24}>
            <Col xs={24} md={12}>
              <Form.Item name="collector" label="采集人">
                <Input placeholder="请输入采集人" />
              </Form.Item>
            </Col>
            <Col xs={24} md={12}>
              <Form.Item name="collectionDate" label="采集日期">
                <DatePicker style={{ width: '100%' }} />
              </Form.Item>
            </Col>
          </Row>

          <Row gutter={24}>
            <Col xs={24} md={24}>
              <Form.Item name="collectionLocation" label="采集地点">
                <Input placeholder="请输入采集地点" />
              </Form.Item>
            </Col>
          </Row>

          <Row gutter={24}>
            <Col xs={24} md={12}>
              <Form.Item name="latitude" label="纬度">
                <InputNumber
                  style={{ width: '100%' }}
                  placeholder="请输入纬度"
                  step={0.0000001}
                  precision={7}
                  min={-90}
                  max={90}
                />
              </Form.Item>
            </Col>
            <Col xs={24} md={12}>
              <Form.Item name="longitude" label="经度">
                <InputNumber
                  style={{ width: '100%' }}
                  placeholder="请输入经度"
                  step={0.0000001}
                  precision={7}
                  min={-180}
                  max={180}
                />
              </Form.Item>
            </Col>
          </Row>

          <Row gutter={24}>
            <Col xs={24} md={24}>
              <Form.Item name="habitat" label="生境">
                <Input placeholder="请输入生境描述" />
              </Form.Item>
            </Col>
          </Row>

          <Row gutter={24}>
            <Col xs={24} md={24}>
              <Form.Item name="description" label="描述">
                <TextArea
                  rows={4}
                  placeholder="请输入标本描述"
                  showCount
                  maxLength={2000}
                />
              </Form.Item>
            </Col>
          </Row>

          <Divider orientation="left">标本图片</Divider>

          <Form.Item label="图片上传">
            <Upload.Dragger {...uploadProps}>
              <p className="ant-upload-drag-icon">
                <UploadOutlined />
              </p>
              <p className="ant-upload-text">点击或拖拽图片到此区域上传</p>
              <p className="ant-upload-hint">支持多张图片上传，单张图片不超过50MB</p>
            </Upload.Dragger>
          </Form.Item>

          <Divider />

          <Form.Item>
            <Space>
              <Button
                type="primary"
                htmlType="submit"
                icon={<SaveOutlined />}
                loading={submitting}
              >
                {isEdit ? '保存修改' : '创建标本'}
              </Button>
              <Button onClick={() => navigate('/specimen')}>
                取消
              </Button>
            </Space>
          </Form.Item>
        </Form>
      </Card>
    </div>
  );
};

export default SpecimenNew;
