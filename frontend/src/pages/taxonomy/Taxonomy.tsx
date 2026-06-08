import { useState, useEffect } from 'react';
import {
  Card,
  Tree,
  Button,
  Form,
  Input,
  Modal,
  message,
  Space,
  Popconfirm,
  Select
} from 'antd';
import {
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  ReloadOutlined
} from '@ant-design/icons';
import { getTaxonomyTree, createTaxonomy, updateTaxonomy, deleteTaxonomy } from '../../api/taxonomy';
import { Taxonomy } from '../../types';
import { useUserStore } from '../../store/userStore';
import { RoleCode, hasRole } from '../../utils/auth';
import './Taxonomy.css';

const { TextArea } = Input;

const TaxonomyPage = () => {
  const { userInfo } = useUserStore();
  const [treeData, setTreeData] = useState<Taxonomy[]>([]);
  const [modalVisible, setModalVisible] = useState(false);
  const [modalTitle, setModalTitle] = useState('');
  const [editingNode, setEditingNode] = useState<Taxonomy | null>(null);
  const [parentId, setParentId] = useState<number | null>(null);
  const [form] = Form.useForm();

  const canManage = userInfo?.roleCode && hasRole(userInfo.roleCode, RoleCode.SPECIMEN_ADMIN);

  useEffect(() => {
    loadTree();
  }, []);

  const loadTree = async () => {
    try {
      const data = await getTaxonomyTree();
      setTreeData(Array.isArray(data) ? data : []);
    } catch (error: any) {
      message.error(error.message || '加载分类树失败');
    }
  };

  const handleAdd = (pid?: number) => {
    setModalTitle(pid ? '添加子分类' : '添加根分类');
    setParentId(pid || null);
    setEditingNode(null);
    form.resetFields();
    if (pid) {
      form.setFieldsValue({ parentId: pid });
    }
    setModalVisible(true);
  };

  const handleEdit = (node: Taxonomy) => {
    setModalTitle('编辑分类');
    setEditingNode(node);
    setParentId(node.parentId);
    form.setFieldsValue({
      name: node.name,
      latinName: node.latinName,
      rank: node.rank,
      description: node.description
    });
    setModalVisible(true);
  };

  const handleDelete = async (id: number) => {
    try {
      await deleteTaxonomy(id);
      message.success('删除成功');
      loadTree();
    } catch (error: any) {
      message.error(error.message || '删除失败');
    }
  };

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      
      if (editingNode) {
        await updateTaxonomy(editingNode.id, values);
        message.success('更新成功');
      } else {
        await createTaxonomy({
          ...values,
          parentId: parentId
        });
        message.success('创建成功');
      }
      
      setModalVisible(false);
      loadTree();
    } catch (error: any) {
      if (error.errorFields) return;
      message.error(error.message || '操作失败');
    }
  };

  const renderTitle = (node: Taxonomy) => {
    return (
      <div className="tree-node-title">
        <span className="node-name">{node.name}</span>
        {node.latinName && (
          <span className="node-latin">{node.latinName}</span>
        )}
        {canManage && (
          <div className="node-actions">
            <Button
              type="text"
              size="small"
              icon={<PlusOutlined />}
              onClick={(e) => {
                e.stopPropagation();
                handleAdd(node.id);
              }}
            />
            <Button
              type="text"
              size="small"
              icon={<EditOutlined />}
              onClick={(e) => {
                e.stopPropagation();
                handleEdit(node);
              }}
            />
            <Popconfirm
              title="确定要删除此分类吗？"
              description="删除后子分类也将被删除"
              onConfirm={(e) => {
                e?.stopPropagation();
                handleDelete(node.id);
              }}
              onCancel={(e) => e?.stopPropagation()}
            >
              <Button
                type="text"
                size="small"
                danger
                icon={<DeleteOutlined />}
                onClick={(e) => e.stopPropagation()}
              />
            </Popconfirm>
          </div>
        )}
      </div>
    );
  };

  const transformTreeData = (data: Taxonomy[]): any[] => {
    return data.map(item => ({
      key: item.id,
      title: renderTitle(item),
      children: item.children ? transformTreeData(item.children) : []
    }));
  };

  return (
    <div className="taxonomy-page">
      <Card
        title="分类管理"
        className="taxonomy-card"
        extra={
          <Space>
            <Button
              icon={<ReloadOutlined />}
              onClick={loadTree}
            >
              刷新
            </Button>
            {canManage && (
              <Button
                type="primary"
                icon={<PlusOutlined />}
                onClick={() => handleAdd()}
              >
                添加根分类
              </Button>
            )}
          </Space>
        }
      >
        <div className="tree-container">
          {treeData.length > 0 ? (
            <Tree
              showLine
              defaultExpandAll
              treeData={transformTreeData(treeData)}
              className="taxonomy-tree"
            />
          ) : (
            <div style={{ textAlign: 'center', padding: '40px 0' }}>
              <p style={{ color: '#999' }}>暂无分类数据</p>
              {canManage && (
                <Button
                  type="primary"
                  icon={<PlusOutlined />}
                  onClick={() => handleAdd()}
                >
                  添加第一个分类
                </Button>
              )}
            </div>
          )}
        </div>
      </Card>

      <Modal
        title={modalTitle}
        open={modalVisible}
        onOk={handleSubmit}
        onCancel={() => setModalVisible(false)}
        okText="确定"
        cancelText="取消"
        width={500}
      >
        <Form
          form={form}
          layout="vertical"
          className="taxonomy-form"
        >
          <Form.Item
            name="name"
            label="分类名称"
            rules={[{ required: true, message: '请输入分类名称' }]}
          >
            <Input placeholder="请输入分类名称" />
          </Form.Item>

          <Form.Item
            name="latinName"
            label="拉丁名"
          >
            <Input placeholder="请输入拉丁名" />
          </Form.Item>

          <Form.Item
            name="rank"
            label="分类等级"
            rules={[{ required: true, message: '请选择分类等级' }]}
          >
            <Select placeholder="请选择分类等级">
              <Select.Option value="KINGDOM">界</Select.Option>
              <Select.Option value="PHYLUM">门</Select.Option>
              <Select.Option value="CLASS">纲</Select.Option>
              <Select.Option value="ORDER">目</Select.Option>
              <Select.Option value="FAMILY">科</Select.Option>
              <Select.Option value="GENUS">属</Select.Option>
              <Select.Option value="SPECIES">种</Select.Option>
            </Select>
          </Form.Item>

          <Form.Item
            name="description"
            label="描述"
          >
            <TextArea rows={3} placeholder="请输入分类描述" />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default TaxonomyPage;
