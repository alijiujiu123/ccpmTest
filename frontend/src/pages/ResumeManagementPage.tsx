import React, { useState, useEffect } from 'react';
import { Layout, Menu, Card, Table, Button, Modal, Form, Input, Upload, message, Dropdown, Avatar } from 'antd';
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
  UploadOutlined,
  EditOutlined,
  DeleteOutlined,
  DownloadOutlined,
} from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import { resumeApi } from '../services/api';

const { Header, Sider, Content } = Layout;
const { Item } = Menu;

const ResumeManagementPage: React.FC = () => {
  const [collapsed, setCollapsed] = useState(false);
  const [resumes, setResumes] = useState<any[]>([]);
  const [loading, setLoading] = useState(false);
  const [modalVisible, setModalVisible] = useState(false);
  const [form] = Form.useForm();
  const navigate = useNavigate();
  const [user, setUser] = useState<any>(null);

  useEffect(() => {
    const userData = localStorage.getItem('user');
    if (userData) {
      setUser(JSON.parse(userData));
      loadResumes();
    } else {
      navigate('/login');
    }
  }, [navigate]);

  const loadResumes = async () => {
    setLoading(true);
    try {
      const response = await resumeApi.getResumes();
      if (response.success && response.data) {
        setResumes(response.data);
      }
    } catch (error) {
      message.error('加载简历失败');
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
      const response = await resumeApi.createResume(values);
      if (response.success) {
        message.success('简历创建成功');
        setModalVisible(false);
        form.resetFields();
        loadResumes();
      }
    } catch (error) {
      message.error('创建简历失败');
    }
  };

  const handleDelete = async (id: string) => {
    try {
      await resumeApi.deleteResume(id);
      message.success('删除成功');
      loadResumes();
    } catch (error) {
      message.error('删除失败');
    }
  };

  const handleExport = async (id: string, format: string) => {
    try {
      const blob = await resumeApi.exportResume(id, format);
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `resume.${format}`;
      a.click();
      window.URL.revokeObjectURL(url);
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

  const columns = [
    {
      title: '标题',
      dataIndex: 'title',
      key: 'title',
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
      title: 'AI优化',
      dataIndex: 'aiOptimized',
      key: 'aiOptimized',
      render: (optimized: boolean) => (
        <span style={{ color: optimized ? 'green' : 'red' }}>
          {optimized ? '已优化' : '未优化'}
        </span>
      ),
    },
    {
      title: '创建时间',
      dataIndex: 'createdAt',
      key: 'createdAt',
      render: (date: string) => new Date(date).toLocaleDateString(),
    },
    {
      title: '操作',
      key: 'actions',
      render: (_, record: any) => (
        <div style={{ display: 'flex', gap: 8 }}>
          <Button
            size="small"
            icon={<EditOutlined />}
            onClick={() => navigate(`/resumes/${record.id}`)}
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
            danger
            icon={<DeleteOutlined />}
            onClick={() => handleDelete(record.id)}
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
          defaultSelectedKeys={['resumes']}
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
          <h2 style={{ margin: 0 }}>简历管理</h2>
          <div style={{ display: 'flex', alignItems: 'center', gap: 16 }}>
            <Button
              type="primary"
              icon={<PlusOutlined />}
              onClick={() => setModalVisible(true)}
            >
              创建简历
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
              dataSource={resumes}
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
        title="创建新简历"
        open={modalVisible}
        onCancel={() => setModalVisible(false)}
        footer={null}
      >
        <Form form={form} layout="vertical" onFinish={handleCreate}>
          <Form.Item
            name="title"
            label="简历标题"
            rules={[{ required: true, message: '请输入简历标题' }]}
          >
            <Input placeholder="例如：前端开发工程师简历" />
          </Form.Item>
          <Form.Item
            name="content"
            label="简历内容"
            rules={[{ required: true, message: '请输入简历内容' }]}
          >
            <Input.TextArea rows={10} placeholder="请输入您的简历内容..." />
          </Form.Item>
          <Form.Item>
            <Button type="primary" htmlType="submit" style={{ width: '100%' }}>
              创建
            </Button>
          </Form.Item>
        </Form>
      </Modal>
    </Layout>
  );
};

export default ResumeManagementPage;