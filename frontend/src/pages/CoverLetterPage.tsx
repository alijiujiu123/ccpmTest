import React, { useState, useEffect } from 'react';
import { Layout, Menu, Card, Table, Button, Modal, Form, Input, Select, message, Dropdown, Avatar, Tag, Tabs, Radio } from 'antd';
import {
  DashboardOutlined,
  FileTextOutlined,
  ProjectOutlined,
  BulbOutlined,
  MailOutlined,
  UserOutlined,
  LogoutOutlined,
  SettingOutlined,
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  DownloadOutlined,
  EyeOutlined,
  ThunderboltOutlined,
} from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import dayjs from 'dayjs';

const { Header, Sider, Content } = Layout;
const { Option } = Select;
const { TextArea } = Input;
const { TabPane } = Tabs;

const CoverLetterPage: React.FC = () => {
  const [collapsed, setCollapsed] = useState(false);
  const [coverLetters, setCoverLetters] = useState<any[]>([]);
  const [templates, setTemplates] = useState<any[]>([]);
  const [loading, setLoading] = useState(false);
  const [modalVisible, setModalVisible] = useState(false);
  const [activeTab, setActiveTab] = useState('cover-letters');
  const [form] = Form.useForm();
  const navigate = useNavigate();
  const [user, setUser] = useState<any>(null);

  useEffect(() => {
    const userData = localStorage.getItem('user');
    if (userData) {
      setUser(JSON.parse(userData));
      loadCoverLetters();
      loadTemplates();
    } else {
      navigate('/login');
    }
  }, [navigate]);

  const loadCoverLetters = async () => {
    setLoading(true);
    try {
      // 这里应该调用求职信API
      const mockCoverLetters = [
        {
          id: '1',
          title: '前端开发工程师求职信',
          companyName: '阿里巴巴',
          position: '高级前端开发工程师',
          generatedBy: 'ai_generated',
          aiOptimized: true,
          qualityScore: 0.88,
          matchScore: 0.92,
          status: 'ready',
          createdAt: '2024-01-15'
        },
        {
          id: '2',
          title: '全栈开发工程师求职信',
          companyName: '腾讯',
          position: '全栈开发工程师',
          generatedBy: 'template_based',
          aiOptimized: false,
          qualityScore: 0.65,
          matchScore: 0.78,
          status: 'draft',
          createdAt: '2024-01-18'
        }
      ];
      setCoverLetters(mockCoverLetters);
    } catch (error) {
      message.error('加载求职信失败');
    } finally {
      setLoading(false);
    }
  };

  const loadTemplates = async () => {
    try {
      // 这里应该调用模板API
      const mockTemplates = [
        {
          id: '1',
          name: '通用技术岗位模板',
          category: '技术',
          style: '专业',
          description: '适用于技术岗位的通用模板'
        },
        {
          id: '2',
          name: '管理岗位模板',
          category: '管理',
          style: '正式',
          description: '适用于管理岗位的正式模板'
        }
      ];
      setTemplates(mockTemplates);
    } catch (error) {
      message.error('加载模板失败');
    }
  };

  const handleMenuClick = (key: string) => {
    navigate(`/${key}`);
  };

  const handleLogout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    navigate('/login');
  };

  const handleCreate = async (values: any) => {
    try {
      // 这里应该调用创建求职信API
      message.success('求职信创建成功');
      setModalVisible(false);
      form.resetFields();
      loadCoverLetters();
    } catch (error) {
      message.error('创建求职信失败');
    }
  };

  const handleExport = async (id: string, format: string) => {
    try {
      // 这里应该调用导出API
      const blob = new Blob(['求职信内容'], { type: 'text/plain' });
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `cover-letter.${format}`;
      a.click();
      window.URL.revokeObjectURL(url);
      message.success('导出成功');
    } catch (error) {
      message.error('导出失败');
    }
  };

  const userMenu = (
    <Menu>
      <Item key="profile" icon={<UserOutlined />}>
        个人资料
      </Item>
      <Item key="settings" icon={<SettingOutlined />}>
        设置
      </Item>
      <Item key="logout" icon={<LogoutOutlined />} onClick={handleLogout}>
        退出登录
      </Item>
    </Menu>
  );

  const menuItems = [
    {
      key: 'dashboard',
      icon: <DashboardOutlined />,
      label: '仪表板',
    },
    {
      key: 'resumes',
      icon: <FileTextOutlined />,
      label: '简历管理',
    },
    {
      key: 'projects',
      icon: <ProjectOutlined />,
      label: '项目管理',
    },
    {
      key: 'job-requirements',
      icon: <BulbOutlined />,
      label: '招聘需求',
    },
    {
      key: 'resume-optimization',
      icon: <BulbOutlined />,
      label: '简历优化',
    },
    {
      key: 'cover-letters',
      icon: <MailOutlined />,
      label: '求职信',
    },
  ];

  const coverLetterColumns = [
    {
      title: '标题',
      dataIndex: 'title',
      key: 'title',
    },
    {
      title: '公司',
      dataIndex: 'companyName',
      key: 'companyName',
    },
    {
      title: '职位',
      dataIndex: 'position',
      key: 'position',
    },
    {
      title: '生成方式',
      dataIndex: 'generatedBy',
      key: 'generatedBy',
      render: (type: string) => {
        const typeMap = {
          'ai_generated': { color: 'blue', text: 'AI生成' },
          'manual_edited': { color: 'green', text: '手动编辑' },
          'template_based': { color: 'orange', text: '模板生成' }
        };
        const { color, text } = typeMap[type as keyof typeof typeMap] || { color: 'default', text: type };
        return <Tag color={color}>{text}</Tag>;
      },
    },
    {
      title: '质量分数',
      dataIndex: 'qualityScore',
      key: 'qualityScore',
      render: (score: number) => (
        <span style={{ color: score >= 0.8 ? 'green' : score >= 0.6 ? 'orange' : 'red' }}>
          {(score * 100).toFixed(1)}%
        </span>
      ),
    },
    {
      title: '匹配度',
      dataIndex: 'matchScore',
      key: 'matchScore',
      render: (score: number) => (
        <span style={{ color: score >= 0.8 ? 'green' : score >= 0.6 ? 'orange' : 'red' }}>
          {(score * 100).toFixed(1)}%
        </span>
      ),
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      render: (status: string) => {
        const statusMap = {
          'draft': { color: 'orange', text: '草稿' },
          'ready': { color: 'green', text: '就绪' },
          'sent': { color: 'blue', text: '已发送' },
          'archived': { color: 'default', text: '归档' }
        };
        const { color, text } = statusMap[status as keyof typeof statusMap] || { color: 'default', text: status };
        return <Tag color={color}>{text}</Tag>;
      },
    },
    {
      title: '创建时间',
      dataIndex: 'createdAt',
      key: 'createdAt',
      render: (date: string) => dayjs(date).format('YYYY-MM-DD'),
    },
    {
      title: '操作',
      key: 'actions',
      render: (_, record: any) => (
        <div style={{ display: 'flex', gap: 8 }}>
          <Button
            size="small"
            icon={<EyeOutlined />}
            onClick={() => console.log('Preview cover letter', record.id)}
          >
            预览
          </Button>
          <Button
            size="small"
            icon={<EditOutlined />}
            onClick={() => console.log('Edit cover letter', record.id)}
          >
            编辑
          </Button>
          <Button
            size="small"
            icon={<DownloadOutlined />}
            onClick={() => handleExport(record.id, 'html')}
          >
            导出
          </Button>
          <Button
            size="small"
            type="primary"
            icon={<ThunderboltOutlined />}
            onClick={() => console.log('Optimize cover letter', record.id)}
            disabled={record.aiOptimized}
          >
            优化
          </Button>
        </div>
      ),
    },
  ];

  const templateColumns = [
    {
      title: '模板名称',
      dataIndex: 'name',
      key: 'name',
    },
    {
      title: '分类',
      dataIndex: 'category',
      key: 'category',
      render: (category: string) => <Tag color="blue">{category}</Tag>,
    },
    {
      title: '风格',
      dataIndex: 'style',
      key: 'style',
      render: (style: string) => <Tag color="green">{style}</Tag>,
    },
    {
      title: '描述',
      dataIndex: 'description',
      key: 'description',
    },
    {
      title: '操作',
      key: 'actions',
      render: (_, record: any) => (
        <div style={{ display: 'flex', gap: 8 }}>
          <Button
            size="small"
            type="primary"
            onClick={() => {
              form.setFieldsValue({ templateId: record.id, templateName: record.name });
              setActiveTab('create');
            }}
          >
            使用模板
          </Button>
        </div>
      ),
    },
  ];

  return (
    <Layout style={{ minHeight: 'calc(100vh - 64px)' }}>
      <Sider
        collapsible
        collapsed={collapsed}
        onCollapse={setCollapsed}
        style={{
          overflow: 'auto',
          height: 'calc(100vh - 64px)',
          position: 'fixed',
          left: 0,
          top: 64,
          bottom: 0,
        }}
      >
        <div style={{ height: 32, margin: 16, background: 'rgba(255, 255, 255, 0.2)' }} />
        <Menu
          theme="dark"
          mode="inline"
          defaultSelectedKeys={['cover-letters']}
          items={menuItems}
          onClick={({ key }) => handleMenuClick(key)}
        />
      </Sider>
      <Layout style={{ marginLeft: collapsed ? 80 : 200, transition: 'margin-left 0.2s' }}>
        <Header style={{
          background: '#fff',
          padding: '0 24px',
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          boxShadow: '0 2px 8px rgba(0, 0, 0, 0.1)'
        }}>
          <h2 style={{ margin: 0 }}>求职信管理</h2>
          <div style={{ display: 'flex', alignItems: 'center', gap: 16 }}>
            <Button
              type="primary"
              icon={<PlusOutlined />}
              onClick={() => setModalVisible(true)}
            >
              创建求职信
            </Button>
            <Dropdown overlay={userMenu} placement="bottomRight">
              <div style={{ cursor: 'pointer', display: 'flex', alignItems: 'center' }}>
                <Avatar style={{ marginRight: 8 }} icon={<UserOutlined />} />
                <span>{user?.username || '用户'}</span>
              </div>
            </Dropdown>
          </div>
        </Header>
        <Content style={{ margin: '24px', minHeight: 280 }}>
          <Card>
            <Tabs activeKey={activeTab} onChange={setActiveTab}>
              <TabPane tab="我的求职信" key="cover-letters">
                <Table
                  dataSource={coverLetters}
                  columns={coverLetterColumns}
                  rowKey="id"
                  loading={loading}
                  pagination={{
                    pageSize: 10,
                    showSizeChanger: true,
                    showQuickJumper: true,
                    showTotal: (total, range) =>
                      `第 ${range[0]}-${range[1]} 条，共 ${total} 条`,
                  }}
                />
              </TabPane>
              <TabPane tab="模板库" key="templates">
                <Table
                  dataSource={templates}
                  columns={templateColumns}
                  rowKey="id"
                  loading={loading}
                  pagination={{
                    pageSize: 10,
                    showSizeChanger: true,
                    showQuickJumper: true,
                    showTotal: (total, range) =>
                      `第 ${range[0]}-${range[1]} 条，共 ${total} 条`,
                  }}
                />
              </TabPane>
            </Tabs>
          </Card>
        </Content>
      </Layout>

      <Modal
        title="创建求职信"
        open={modalVisible}
        onCancel={() => setModalVisible(false)}
        footer={null}
        width={700}
      >
        <Form form={form} layout="vertical" onFinish={handleCreate}>
          <Form.Item
            name="creationType"
            label="创建方式"
            rules={[{ required: true, message: '请选择创建方式' }]}
          >
            <Radio.Group>
              <Radio value="basic">基础创建</Radio>
              <Radio value="personalized">个性化创建</Radio>
              <Radio value="template">基于模板</Radio>
            </Radio.Group>
          </Form.Item>

          <Form.Item
            name="title"
            label="求职信标题"
            rules={[{ required: true, message: '请输入求职信标题' }]}
          >
            <Input placeholder="例如：前端开发工程师求职信" />
          </Form.Item>

          <Form.Item
            name="companyName"
            label="公司名称"
            rules={[{ required: true, message: '请输入公司名称' }]}
          >
            <Input placeholder="例如：阿里巴巴" />
          </Form.Item>

          <Form.Item
            name="position"
            label="申请职位"
            rules={[{ required: true, message: '请输入申请职位' }]}
          >
            <Input placeholder="例如：高级前端开发工程师" />
          </Form.Item>

          <Form.Item
            name="templateId"
            label="选择模板"
          >
            <Select placeholder="请选择模板（可选）">
              {templates.map(template => (
                <Option key={template.id} value={template.id}>
                  {template.name} - {template.style}
                </Option>
              ))}
            </Select>
          </Form.Item>

          <Form.Item
            name="recipientName"
            label="收件人姓名"
          >
            <Input placeholder="例如：HR经理" />
          </Form.Item>

          <Form.Item
            name="recipientTitle"
            label="收件人职位"
          >
            <Input placeholder="例如：招聘经理" />
          </Form.Item>

          <Form.Item
            name="content"
            label="求职信内容"
            rules={[{ required: true, message: '请输入求职信内容' }]}
          >
            <TextArea rows={8} placeholder="请输入您的求职信内容..." />
          </Form.Item>

          <Form.Item>
            <Button type="primary" htmlType="submit" style={{ width: '100%' }}>
              创建求职信
            </Button>
          </Form.Item>
        </Form>
      </Modal>
    </Layout>
  );
};

export default CoverLetterPage;