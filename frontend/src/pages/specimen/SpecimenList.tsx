import { useState, useEffect } from 'react';
import {
  Table,
  Button,
  Input,
  Space,
  Tag,
  Popconfirm,
  message,
  Card,
  Form,
  DatePicker,
  Select,
  TreeSelect
} from 'antd';
import {
  PlusOutlined,
  SearchOutlined,
  EditOutlined,
  DeleteOutlined,
  EyeOutlined
} from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import { getSpecimenList, deleteSpecimen } from '../../api/specimen';
import { getTaxonomyTree } from '../../api/taxonomy';
import { Specimen, Taxonomy } from '../../types';
import { useUserStore } from '../../store/userStore';
import { RoleCode, hasRole } from '../../utils/auth';
import './SpecimenList.css';

const { RangePicker } = DatePicker;
const { Option } = Select;

const SpecimenList = () => {
  const navigate = useNavigate();
  const { userInfo } = useUserStore();
  const [form] = Form.useForm();
  const [loading, setLoading] = useState(false);
  const [data, setData] = useState<Specimen[]>([]);
  const [total, setTotal] = useState(0);
  const [page, setPage] = useState(1);
  const [pageSize, setPageSize] = useState(10);
  const [taxonomyTree, setTaxonomyTree] = useState<Taxonomy[]>([]);

  const canManage = userInfo?.roleCode && hasRole(userInfo.roleCode, RoleCode.SPECIMEN_ADMIN);

  useEffect(() => {
    loadTaxonomyTree();
    loadData();
  }, [page, pageSize]);

  const loadTaxonomyTree = async () => {
    try {
      const data: any = await getTaxonomyTree();
      setTaxonomyTree(data || []);
    } catch (error) {
      console.error('加载分类树失败', error);
    }
  };

  const loadData = async () => {
    setLoading(true);
    try {
      const values = form.getFieldsValue();
      const params: any = {
        page,
        pageSize,
        keyword: values.keyword,
        collector: values.collector,
        status: values.status
      };

      if (values.dateRange && values.dateRange.length === 2) {
        params.startDate = values.dateRange[0].format('YYYY-MM-DD');
        params.endDate = values.dateRange[1].format('YYYY-MM-DD');
      }

      const res: any = await getSpecimenList(params);
      const result = res.data || res;
      setData(result.list || []);
      setTotal(result.total || 0);
    } catch (error: any) {
      message.error(error.message || '加载数据失败');
    } finally {
      setLoading(false);
    }
  };

  const handleSearch = () => {
    setPage(1);
    loadData();
  };

  const handleReset = () => {
    form.resetFields();
    setPage(1);
    loadData();
  };

  const handleDelete = async (id: number) => {
    try {
      await deleteSpecimen(id);
      message.success('删除成功');
      loadData();
    } catch (error: any) {
      message.error(error.message || '删除失败');
    }
  };

  const transformTaxonomyTree = (items: Taxonomy[]): any[] => {
    return items.map(item => ({
      title: item.name,
      value: item.id,
      key: item.id,
      children: item.children ? transformTaxonomyTree(item.children) : undefined
    }));
  };

  const columns = [
    {
      title: '标本编号',
      dataIndex: 'specimenNo',
      key: 'specimenNo',
      width: 140,
      ellipsis: true
    },
    {
      title: '中文名',
      dataIndex: 'name',
      key: 'name',
      width: 120,
      render: (text: string) => <strong>{text}</strong>
    },
    {
      title: '拉丁名',
      dataIndex: 'latinName',
      key: 'latinName',
      width: 180,
      ellipsis: true,
      className: 'latin-name'
    },
    {
      title: '分类',
      dataIndex: 'taxonomyName',
      key: 'taxonomyName',
      width: 100,
      render: (text: string) => (
        <Tag color="green">{text || '未分类'}</Tag>
      )
    },
    {
      title: '采集人',
      dataIndex: 'collector',
      key: 'collector',
      width: 100
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
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: 80,
      render: (status: number) => (
        <Tag color={status === 1 ? 'green' : 'red'}>
          {status === 1 ? '正常' : '禁用'}
        </Tag>
      )
    },
    {
      title: '操作',
      key: 'action',
      width: 160,
      fixed: 'right' as const,
      render: (_: any, record: Specimen) => (
        <Space size="small">
          <Button
            type="link"
            size="small"
            icon={<EyeOutlined />}
            onClick={() => navigate(`/specimen/${record.id}`)}
          >
            查看
          </Button>
          {canManage && (
            <>
              <Button
                type="link"
                size="small"
                icon={<EditOutlined />}
                onClick={() => navigate(`/specimen/new?id=${record.id}`)}
              >
                编辑
              </Button>
              <Popconfirm
                title="确定要删除该标本吗？"
                onConfirm={() => handleDelete(record.id)}
                okText="确定"
                cancelText="取消"
              >
                <Button
                  type="link"
                  size="small"
                  danger
                  icon={<DeleteOutlined />}
                >
                  删除
                </Button>
              </Popconfirm>
            </>
          )}
        </Space>
      )
    }
  ];

  return (
    <div className="specimen-list-page">
      <Card className="filter-card">
        <Form form={form} layout="inline" onFinish={handleSearch}>
          <Form.Item name="keyword" label="关键词">
            <Input
              placeholder="编号/名称"
              allowClear
              prefix={<SearchOutlined />}
              style={{ width: 180 }}
            />
          </Form.Item>
          <Form.Item name="taxonomyId" label="分类">
            <TreeSelect
              placeholder="请选择分类"
              allowClear
              style={{ width: 160 }}
              treeData={transformTaxonomyTree(taxonomyTree)}
              treeNodeFilterProp="title"
              showSearch
            />
          </Form.Item>
          <Form.Item name="collector" label="采集人">
            <Input placeholder="请输入采集人" allowClear style={{ width: 140 }} />
          </Form.Item>
          <Form.Item name="dateRange" label="采集日期">
            <RangePicker style={{ width: 260 }} />
          </Form.Item>
          <Form.Item name="status" label="状态">
            <Select placeholder="全部" allowClear style={{ width: 100 }}>
              <Option value={1}>正常</Option>
              <Option value={0}>禁用</Option>
            </Select>
          </Form.Item>
          <Form.Item>
            <Space>
              <Button type="primary" htmlType="submit" icon={<SearchOutlined />}>
                搜索
              </Button>
              <Button onClick={handleReset}>重置</Button>
            </Space>
          </Form.Item>
        </Form>
      </Card>

      <Card
        className="table-card"
        title={
          <div className="card-header">
            <span>标本列表</span>
            {canManage && (
              <Button
                type="primary"
                icon={<PlusOutlined />}
                onClick={() => navigate('/specimen/new')}
              >
                新增标本
              </Button>
            )}
          </div>
        }
      >
        <Table
          columns={columns}
          dataSource={data}
          rowKey="id"
          loading={loading}
          scroll={{ x: 1000 }}
          pagination={{
            current: page,
            pageSize,
            total,
            showSizeChanger: true,
            showQuickJumper: true,
            showTotal: (total) => `共 ${total} 条记录`,
            onChange: (p, ps) => {
              setPage(p);
              setPageSize(ps);
            }
          }}
        />
      </Card>
    </div>
  );
};

export default SpecimenList;
