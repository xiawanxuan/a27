import { useState, useEffect } from 'react';
import {
  Card,
  Table,
  Button,
  Form,
  Input,
  Modal,
  message,
  Space,
  Tag,
  Select,
  Switch,
  Popconfirm
} from 'antd';
import {
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  SearchOutlined,
  ReloadOutlined
} from '@ant-design/icons';
import { getUserList, createUser, updateUser, deleteUser } from '../../api/user';
import { User } from '../../types';
import './UserManagement.css';

const UserManagement = () => {
  const [loading, setLoading] = useState(false);
  const [users, setUsers] = useState<User[]>([]);
  const [total, setTotal] = useState(0);
  const [page, setPage] = useState(1);
  const [pageSize, setPageSize] = useState(10);
  const [searchForm] = Form.useForm();
  const [modalVisible, setModalVisible] = useState(false);
  const [modalTitle, setModalTitle] = useState('');
  const [editingUser, setEditingUser] = useState<User | null>(null);
  const [form] = Form.useForm();

  useEffect(() => {
    loadUsers();
  }, [page, pageSize]);

  const loadUsers = async () => {
    setLoading(true);
    try {
      const values = searchForm.getFieldsValue();
      const result: any = await getUserList({
        page,
        pageSize,
        keyword: values.keyword
      });
      setUsers(result.list || []);
      setTotal(result.total || 0);
    } catch (error: any) {
      message.error(error.message || '加载用户列表失败');
    } finally {
      setLoading(false);
    }
  };

  const handleSearch = () => {
    setPage(1);
    loadUsers();
  };

  const handleReset = () => {
    searchForm.resetFields();
    setPage(1);
    loadUsers();
  };

  const handleAdd = () => {
    setModalTitle('新增用户');
    setEditingUser(null);
    form.resetFields();
    form.setFieldsValue({
      status: 1,
      roleCode: 'USER'
    });
    setModalVisible(true);
  };

  const handleEdit = (user: User) => {
    setModalTitle('编辑用户');
    setEditingUser(user);
    form.setFieldsValue({
      username: user.username,
      email: user.email,
      phone: user.phone,
      realName: user.realName,
      roleCode: user.roleCode,
      status: user.status
    });
    setModalVisible(true);
  };

  const handleDelete = async (id: number) => {
    try {
      await deleteUser(id);
      message.success('删除成功');
      loadUsers();
    } catch (error: any) {
      message.error(error.message || '删除失败');
    }
  };

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      
      if (editingUser) {
        await updateUser(editingUser.id, values);
        message.success('更新成功');
      } else {
        await createUser(values);
        message.success('创建成功');
      }
      
      setModalVisible(false);
      loadUsers();
    } catch (error: any) {
      if (error.errorFields) return;
      message.error(error.message || '操作失败');
    }
  };

  const columns = [
    {
      title: 'ID',
      dataIndex: 'id',
      key: 'id',
      width: 60
    },
    {
      title: '用户名',
      dataIndex: 'username',
      key: 'username',
      width: 120
    },
    {
      title: '姓名',
      dataIndex: 'realName',
      key: 'realName',
      width: 100
    },
    {
      title: '邮箱',
      dataIndex: 'email',
      key: 'email',
      width: 180,
      ellipsis: true
    },
    {
      title: '电话',
      dataIndex: 'phone',
      key: 'phone',
      width: 120
    },
    {
      title: '角色',
      dataIndex: 'roleCode',
      key: 'roleCode',
      width: 100,
      render: (roleCode: string) => {
        const colorMap: Record<string, string> = {
          ADMIN: 'red',
          SPECIMEN_ADMIN: 'orange',
          USER: 'green'
        };
        const nameMap: Record<string, string> = {
          ADMIN: '管理员',
          SPECIMEN_ADMIN: '标本管理员',
          USER: '普通用户'
        };
        return (
          <Tag color={colorMap[roleCode] || 'default'}>
            {nameMap[roleCode] || roleCode}
          </Tag>
        );
      }
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: 80,
      render: (status: number) => (
        <Tag color={status === 1 ? 'green' : 'red'}>
          {status === 1 ? '启用' : '禁用'}
        </Tag>
      )
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
      width: 150,
      fixed: 'right' as const,
      render: (_: any, record: User) => (
        <Space size="small">
          <Button
            type="link"
            size="small"
            icon={<EditOutlined />}
            onClick={() => handleEdit(record)}
          >
            编辑
          </Button>
          <Popconfirm
            title="确定要删除此用户吗？"
            onConfirm={() => handleDelete(record.id)}
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
        </Space>
      )
    }
  ];

  return (
    <div className="user-management-page">
      <Card className="search-card">
        <Form
          form={searchForm}
          layout="inline"
          onFinish={handleSearch}
          className="search-form"
        >
          <Form.Item name="keyword" label="关键词">
            <Input placeholder="用户名/姓名/邮箱" allowClear style={{ width: 200 }} />
          </Form.Item>
          <Form.Item name="role" label="角色">
            <Select placeholder="全部角色" allowClear style={{ width: 150 }}>
              <Select.Option value="ADMIN">管理员</Select.Option>
              <Select.Option value="SPECIMEN_ADMIN">标本管理员</Select.Option>
              <Select.Option value="USER">普通用户</Select.Option>
            </Select>
          </Form.Item>
          <Form.Item>
            <Space>
              <Button type="primary" htmlType="submit" icon={<SearchOutlined />}>
                搜索
              </Button>
              <Button onClick={handleReset} icon={<ReloadOutlined />}>
                重置
              </Button>
            </Space>
          </Form.Item>
        </Form>
      </Card>

      <Card
        className="table-card"
        title="用户列表"
        extra={
          <Button
            type="primary"
            icon={<PlusOutlined />}
            onClick={handleAdd}
          >
            新增用户
          </Button>
        }
      >
        <Table
          columns={columns}
          dataSource={users}
          rowKey="id"
          loading={loading}
          scroll={{ x: 1000 }}
          pagination={{
            current: page,
            pageSize,
            total,
            showSizeChanger: true,
            showQuickJumper: true,
            showTotal: (total) => `共 ${total} 条`,
            onChange: (p, ps) => {
              setPage(p);
              setPageSize(ps);
            }
          }}
        />
      </Card>

      <Modal
        title={modalTitle}
        open={modalVisible}
        onOk={handleSubmit}
        onCancel={() => setModalVisible(false)}
        okText="确定"
        cancelText="取消"
        width={600}
      >
        <Form
          form={form}
          layout="vertical"
          className="user-form"
        >
          {!editingUser && (
            <Form.Item
              name="username"
              label="用户名"
              rules={[
                { required: true, message: '请输入用户名' },
                { min: 3, max: 20, message: '用户名长度为3-20个字符' }
              ]}
            >
              <Input placeholder="请输入用户名" />
            </Form.Item>
          )}

          {!editingUser && (
            <Form.Item
              name="password"
              label="初始密码"
              rules={[
                { required: true, message: '请输入初始密码' },
                { min: 6, max: 20, message: '密码长度为6-20个字符' }
              ]}
            >
              <Input.Password placeholder="请输入初始密码" />
            </Form.Item>
          )}

          <Form.Item
            name="realName"
            label="真实姓名"
            rules={[{ required: true, message: '请输入真实姓名' }]}
          >
            <Input placeholder="请输入真实姓名" />
          </Form.Item>

          <Form.Item
            name="email"
            label="邮箱"
            rules={[
              { type: 'email', message: '请输入有效的邮箱地址' }
            ]}
          >
            <Input placeholder="请输入邮箱" />
          </Form.Item>

          <Form.Item
            name="phone"
            label="电话"
          >
            <Input placeholder="请输入电话号码" />
          </Form.Item>

          <Form.Item
            name="roleCode"
            label="角色"
            rules={[{ required: true, message: '请选择角色' }]}
          >
            <Select placeholder="请选择角色">
              <Select.Option value="USER">普通用户</Select.Option>
              <Select.Option value="SPECIMEN_ADMIN">标本管理员</Select.Option>
              <Select.Option value="ADMIN">管理员</Select.Option>
            </Select>
          </Form.Item>

          <Form.Item
            name="status"
            label="状态"
            valuePropName="checked"
          >
            <Switch checkedChildren="启用" unCheckedChildren="禁用" />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default UserManagement;
