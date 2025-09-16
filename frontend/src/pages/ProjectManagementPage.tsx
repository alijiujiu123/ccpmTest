import React, { useState, useEffect } from 'react';
import { Layout, Menu, Card, Table, Button, Modal, Form, Input, DatePicker, message, Dropdown, Avatar, Tag } from 'antd';
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
const { Item } = Menu;
const { RangePicker } = DatePicker;

const ProjectManagementPage: React.FC = () => {
  const [collapsed, setCollapsed] = useState(false);
  const [projects, setProjects] = useState<any[]>([]);
  const [loading, setLoading] = useState(false);
  const [modalVisible, setModalVisible] = useState(false);
  const [form] = Form.useForm();
  const navigate = useNavigate();
  const [user, setUser] = useState<any>(null);

  useEffect(() => {
    const userData = localStorage.getItem('user');
    if (userData) {
      setUser(JSON.parse(userData));
      loadProjects();
    } else {
      navigate('/login');
    }
  }, [navigate]);

  const loadProjects = async () => {
    setLoading(true);
    try {
      // 这里应该调用项目API
      const mockProjects = [
        {
          id: '1',
          name: '电商前端重构',
          description: '使用React重构电商平台前端界面',
          technologies: ['React', 'TypeScript', 'Ant Design'],
          startDate: '2024-01-01',
          endDate: '2024-03-01',
          status: 'completed'
        },
        {
          id: '2',
          name: '移动端APP开发',
          description: '开发跨平台移动应用',
          technologies: ['React Native', 'Redux', 'Node.js'],
          startDate: '2024-03-15',
          status: 'in-progress'
        }
      ];
      setProjects(mockProjects);
    } catch (error) {
      message.error('加载项目失败');
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
      // 这里应该调用创建项目API
      message.success('项目创建成功');
      setModalVisible(false);
      form.resetFields();
      loadProjects();
    } catch (error) {
      message.error('创建项目失败');
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

  const columns = [
    {
      title: '项目名称',
      dataIndex: 'name',
      key: 'name',
    },
    {
      title: '描述',
      dataIndex: 'description',
      key: 'description',
    },
    {
      title: '技术栈',
      dataIndex: 'technologies',
      key: 'technologies',
      render: (techs: string[]) => (
        <div style={{ display: 'flex', flexWrap: 'wrap', gap: 4 }}>
          {techs.map(tech => (
            <Tag key={tech} color="blue">{tech}</Tag>
          ))}
        </div>
      ),
    },
    {
      title: '时间',
      key: 'duration',
      render: (_, record: any) => (
        <span>
          {dayjs(record.startDate).format('YYYY-MM-DD')}
          {record.endDate ? ` ~ ${dayjs(record.endDate).format('YYYY-MM-DD')}` : ' ~ 至今'}
        </span>
      ),
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      render: (status: string) => {
        const statusMap = {
          'completed': { color: 'green', text: '已完成' },
          'in-progress': { color: 'blue', text: '进行中' },
          'planned': { color: 'orange', text: '计划中' },
        };
        const { color, text } = statusMap[status as keyof typeof statusMap] || { color: 'default', text: status };
        return <Tag color={color}>{text}</Tag>;
      },
    },
    {
      title: '操作',
      key: 'actions',
      render: (_, record: any) => (
        <div style={{ display: 'flex', gap: 8 }}>
          <Button
            size="small"
            icon={<EditOutlined />}
            onClick={() => console.log('Edit project', record.id)}
          >
            编辑
          </Button>
          <Button
            size="small"
            danger
            icon={<DeleteOutlined />}
            onClick={() => console.log('Delete project', record.id)}
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
          defaultSelectedKeys={['projects']}
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
          <h2 style={{ margin: 0 }}>项目管理</h2>
          <div style={{ display: 'flex', alignItems: 'center', gap: 16 }}>
            <Button
              type="primary"
              icon={<PlusOutlined />}
              onClick={() => setModalVisible(true)}
            >
              添加项目
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
              dataSource={projects}
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
        title="添加新项目"
        open={modalVisible}
        onCancel={() => setModalVisible(false)}
        footer={null}
      >
        <Form form={form} layout="vertical" onFinish={handleCreate}>
          <Form.Item
            name="name"
            label="项目名称"
            rules={[{ required: true, message: '请输入项目名称' }]}
          >
            <Input placeholder="例如：电商平台开发" />
          </Form.Item>
          <Form.Item
            name="description"
            label="项目描述"
            rules={[{ required: true, message: '请输入项目描述' }]}
          >
            <Input.TextArea rows={4} placeholder="请描述项目的主要功能和目标..." />
          </Form.Item>
          <Form.Item
            name="technologies"
            label="技术栈"
            rules={[{ required: true, message: '请输入技术栈' }]}
          >
            <Input placeholder="例如：React, TypeScript, Node.js" />
          </Form.Item>
          <Form.Item
            name="duration"
            label="项目时间"
            rules={[{ required: true, message: '请选择项目时间' }]}
          >
            <RangePicker style={{ width: '100%' }} />
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

export default ProjectManagementPage;