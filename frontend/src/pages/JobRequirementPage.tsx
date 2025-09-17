import React, { useState, useEffect } from 'react';
import { Layout, Menu, Card, Table, Button, Modal, Form, Input, Select, message, Dropdown, Avatar, Tag } from 'antd';
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
} from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import dayjs from 'dayjs';

const { Header, Sider, Content } = Layout;
const { Option } = Select;

const JobRequirementPage: React.FC = () => {
  const [collapsed, setCollapsed] = useState(false);
  const [jobRequirements, setJobRequirements] = useState<any[]>([]);
  const [loading, setLoading] = useState(false);
  const [modalVisible, setModalVisible] = useState(false);
  const [form] = Form.useForm();
  const navigate = useNavigate();
  const [user, setUser] = useState<any>(null);

  useEffect(() => {
    const userData = localStorage.getItem('user');
    if (userData) {
      setUser(JSON.parse(userData));
      loadJobRequirements();
    } else {
      navigate('/login');
    }
  }, [navigate]);

  const loadJobRequirements = async () => {
    setLoading(true);
    try {
      // 这里应该调用招聘需求API
      const mockRequirements = [
        {
          id: '1',
          title: '高级前端开发工程师',
          company: '阿里巴巴',
          location: '杭州',
          experienceLevel: '高级',
          jobType: '全职',
          salaryRange: '25k-40k',
          skills: ['React', 'TypeScript', 'Node.js', 'Vue'],
          createdAt: '2024-01-15'
        },
        {
          id: '2',
          title: '全栈开发工程师',
          company: '腾讯',
          location: '深圳',
          experienceLevel: '中级',
          jobType: '全职',
          salaryRange: '20k-35k',
          skills: ['Java', 'Spring Boot', 'React', 'MySQL'],
          createdAt: '2024-01-20'
        }
      ];
      setJobRequirements(mockRequirements);
    } catch (error) {
      message.error('加载招聘需求失败');
    } finally {
      setLoading(false);
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
      // 这里应该调用创建招聘需求API
      message.success('招聘需求创建成功');
      setModalVisible(false);
      form.resetFields();
      loadJobRequirements();
    } catch (error) {
      message.error('创建招聘需求失败');
    }
  };

  const userMenu = (
    <Menu>
      <Menu.Item key="profile" icon={<UserOutlined />}>
        个人资料
      </Menu.Item>
      <Menu.Item key="settings" icon={<SettingOutlined />}>
        设置
      </Menu.Item>
      <Menu.Item key="logout" icon={<LogoutOutlined />} onClick={handleLogout}>
        退出登录
      </Menu.Item>
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

  const columns = [
    {
      title: '职位名称',
      dataIndex: 'title',
      key: 'title',
    },
    {
      title: '公司',
      dataIndex: 'company',
      key: 'company',
    },
    {
      title: '地点',
      dataIndex: 'location',
      key: 'location',
    },
    {
      title: '经验要求',
      dataIndex: 'experienceLevel',
      key: 'experienceLevel',
      render: (level: string) => (
        <Tag color={level === '高级' ? 'red' : level === '中级' ? 'blue' : 'green'}>
          {level}
        </Tag>
      ),
    },
    {
      title: '薪资范围',
      dataIndex: 'salaryRange',
      key: 'salaryRange',
    },
    {
      title: '技能要求',
      dataIndex: 'skills',
      key: 'skills',
      render: (skills: string[]) => (
        <div style={{ display: 'flex', flexWrap: 'wrap', gap: 4 }}>
          {skills.map(skill => (
            <Tag key={skill} color="cyan">{skill}</Tag>
          ))}
        </div>
      ),
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
            icon={<EditOutlined />}
            onClick={() => console.log('Edit requirement', record.id)}
          >
            编辑
          </Button>
          <Button
            size="small"
            onClick={() => navigate(`/cover-letters?jobId=${record.id}`)}
          >
            生成求职信
          </Button>
          <Button
            size="small"
            danger
            icon={<DeleteOutlined />}
            onClick={() => console.log('Delete requirement', record.id)}
          >
            删除
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
          defaultSelectedKeys={['job-requirements']}
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
          <h2 style={{ margin: 0 }}>招聘需求管理</h2>
          <div style={{ display: 'flex', alignItems: 'center', gap: 16 }}>
            <Button
              type="primary"
              icon={<PlusOutlined />}
              onClick={() => setModalVisible(true)}
            >
              添加招聘需求
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
            <Table
              dataSource={jobRequirements}
              columns={columns}
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
          </Card>
        </Content>
      </Layout>

      <Modal
        title="添加招聘需求"
        open={modalVisible}
        onCancel={() => setModalVisible(false)}
        footer={null}
        width={600}
      >
        <Form form={form} layout="vertical" onFinish={handleCreate}>
          <Form.Item
            name="title"
            label="职位名称"
            rules={[{ required: true, message: '请输入职位名称' }]}
          >
            <Input placeholder="例如：高级前端开发工程师" />
          </Form.Item>
          <Form.Item
            name="company"
            label="公司名称"
            rules={[{ required: true, message: '请输入公司名称' }]}
          >
            <Input placeholder="例如：阿里巴巴" />
          </Form.Item>
          <Form.Item
            name="description"
            label="职位描述"
            rules={[{ required: true, message: '请输入职位描述' }]}
          >
            <Input.TextArea rows={4} placeholder="请详细描述职位要求和职责..." />
          </Form.Item>
          <Form.Item
            name="location"
            label="工作地点"
            rules={[{ required: true, message: '请输入工作地点' }]}
          >
            <Input placeholder="例如：杭州" />
          </Form.Item>
          <Form.Item
            name="experienceLevel"
            label="经验要求"
            rules={[{ required: true, message: '请选择经验要求' }]}
          >
            <Select placeholder="请选择经验要求">
              <Option value="初级">初级</Option>
              <Option value="中级">中级</Option>
              <Option value="高级">高级</Option>
              <Option value="资深">资深</Option>
            </Select>
          </Form.Item>
          <Form.Item
            name="jobType"
            label="工作类型"
            rules={[{ required: true, message: '请选择工作类型' }]}
          >
            <Select placeholder="请选择工作类型">
              <Option value="全职">全职</Option>
              <Option value="兼职">兼职</Option>
              <Option value="实习">实习</Option>
              <Option value="合同">合同</Option>
            </Select>
          </Form.Item>
          <Form.Item
            name="salaryRange"
            label="薪资范围"
          >
            <Input placeholder="例如：25k-40k" />
          </Form.Item>
          <Form.Item
            name="skills"
            label="技能要求"
            rules={[{ required: true, message: '请输入技能要求' }]}
          >
            <Input placeholder="例如：React, TypeScript, Node.js" />
          </Form.Item>
          <Form.Item>
            <Button type="primary" htmlType="submit" style={{ width: '100%' }}>
              添加
            </Button>
          </Form.Item>
        </Form>
      </Modal>
    </Layout>
  );
};

export default JobRequirementPage;