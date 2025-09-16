import React, { useState, useEffect } from 'react';
import { Layout, Menu, Card, Table, Button, Modal, Form, Input, Select, message, Dropdown, Avatar, Tag, Progress, Alert } from 'antd';
import {
  DashboardOutlined,
  FileTextOutlined,
  ProjectOutlined,
  BulbOutlined,
  MailOutlined,
  UserOutlined,
  LogoutOutlined,
  SettingOutlined,
  UploadOutlined,
  ThunderboltOutlined,
  EyeOutlined,
} from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import dayjs from 'dayjs';

const { Header, Sider, Content } = Layout;
const { Option } = Select;
const { TextArea } = Input;

const ResumeOptimizationPage: React.FC = () => {
  const [collapsed, setCollapsed] = useState(false);
  const [resumes, setResumes] = useState<any[]>([]);
  const [loading, setLoading] = useState(false);
  const [optimizationModalVisible, setOptimizationModalVisible] = useState(false);
  const [previewModalVisible, setPreviewModalVisible] = useState(false);
  const [selectedResume, setSelectedResume] = useState<any>(null);
  const [optimizationProgress, setOptimizationProgress] = useState(0);
  const [optimizationResults, setOptimizationResults] = useState<any>(null);
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
      // 这里应该调用简历API
      const mockResumes = [
        {
          id: '1',
          title: '前端开发工程师简历',
          qualityScore: 0.75,
          aiOptimized: true,
          createdAt: '2024-01-10',
          status: 'optimized'
        },
        {
          id: '2',
          title: '全栈开发工程师简历',
          qualityScore: 0.45,
          aiOptimized: false,
          createdAt: '2024-01-05',
          status: 'draft'
        }
      ];
      setResumes(mockResumes);
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

  const handleOptimize = async (values: any) => {
    setOptimizationProgress(0);
    setOptimizationResults(null);

    // 模拟优化进度
    const interval = setInterval(() => {
      setOptimizationProgress(prev => {
        if (prev >= 100) {
          clearInterval(interval);
          // 模拟优化结果
          setOptimizationResults({
            originalScore: selectedResume.qualityScore,
            newScore: 0.85,
            improvements: [
              '增加了具体的项目成果数据',
              '优化了技能描述的精确性',
              '改进了工作经历的表述方式',
              '增强了个人总结的针对性'
            ],
            suggestions: [
              '建议添加更多量化指标',
              '可以考虑加入最新的技术栈',
              '建议突出团队协作经验'
            ]
          });
          return 100;
        }
        return prev + 10;
      });
    }, 300);
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
      title: '简历标题',
      dataIndex: 'title',
      key: 'title',
    },
    {
      title: '质量分数',
      dataIndex: 'qualityScore',
      key: 'qualityScore',
      render: (score: number) => (
        <div>
          <Progress
            percent={score * 100}
            size="small"
            strokeColor={score >= 0.8 ? '#52c41a' : score >= 0.6 ? '#faad14' : '#ff4d4f'}
          />
          <span style={{ marginLeft: 8, fontSize: '12px' }}>
            {(score * 100).toFixed(1)}%
          </span>
        </div>
      ),
    },
    {
      title: 'AI优化状态',
      dataIndex: 'aiOptimized',
      key: 'aiOptimized',
      render: (optimized: boolean) => (
        <Tag color={optimized ? 'green' : 'orange'}>
          {optimized ? '已优化' : '待优化'}
        </Tag>
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
            icon={<EyeOutlined />}
            onClick={() => {
              setSelectedResume(record);
              setPreviewModalVisible(true);
            }}
          >
            预览
          </Button>
          <Button
            size="small"
            type="primary"
            icon={<ThunderboltOutlined />}
            onClick={() => {
              setSelectedResume(record);
              setOptimizationModalVisible(true);
            }}
          >
            优化
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
          defaultSelectedKeys={['resume-optimization']}
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
          <h2 style={{ margin: 0 }}>简历优化</h2>
          <Dropdown overlay={userMenu} placement="bottomRight">
            <div style={{ cursor: 'pointer', display: 'flex', alignItems: 'center' }}>
              <Avatar style={{ marginRight: 8 }} icon={<UserOutlined />} />
              <span>{user?.username || '用户'}</span>
            </div>
          </Dropdown>
        </Header>
        <Content style={{ margin: '24px', minHeight: 280 }}>
          <Alert
            message="AI智能优化"
            description="使用先进的AI技术分析您的简历，提供专业的优化建议，帮助您打造更具竞争力的简历。"
            type="info"
            showIcon
            style={{ marginBottom: 24 }}
          />

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

      {/* 优化模态框 */}
      <Modal
        title={`优化简历: ${selectedResume?.title}`}
        open={optimizationModalVisible}
        onCancel={() => {
          setOptimizationModalVisible(false);
          setOptimizationProgress(0);
          setOptimizationResults(null);
        }}
        footer={null}
        width={700}
      >
        <Form form={form} layout="vertical" onFinish={handleOptimize}>
          <Form.Item
            name="optimizationType"
            label="优化类型"
            rules={[{ required: true, message: '请选择优化类型' }]}
          >
            <Select placeholder="请选择优化类型">
              <Option value="comprehensive">全面优化</Option>
              <Option value="content">内容优化</Option>
              <Option value="structure">结构优化</Option>
              <Option value="keywords">关键词优化</Option>
            </Select>
          </Form.Item>

          <Form.Item
            name="targetRole"
            label="目标职位"
          >
            <Input placeholder="例如：高级前端开发工程师" />
          </Form.Item>

          <Form.Item
            name="targetCompany"
            label="目标公司"
          >
            <Input placeholder="例如：阿里巴巴、腾讯等" />
          </Form.Item>

          <Form.Item
            name="additionalRequirements"
            label="额外要求"
          >
            <TextArea rows={3} placeholder="请描述您的特殊要求或需要突出的技能..." />
          </Form.Item>

          {optimizationProgress > 0 && optimizationProgress < 100 && (
            <div style={{ textAlign: 'center', margin: '20px 0' }}>
              <Progress type="circle" percent={optimizationProgress} />
              <p style={{ marginTop: 10 }}>正在优化您的简历，请稍候...</p>
            </div>
          )}

          {optimizationResults && (
            <div style={{ backgroundColor: '#f6ffed', padding: 16, borderRadius: 6, marginBottom: 16 }}>
              <h4>优化完成！</h4>
              <p>质量分数从 {(optimizationResults.originalScore * 100).toFixed(1)}% 提升到 {(optimizationResults.newScore * 100).toFixed(1)}%</p>

              <h5>改进内容：</h5>
              <ul>
                {optimizationResults.improvements.map((item: string, index: number) => (
                  <li key={index}>{item}</li>
                ))}
              </ul>

              <h5>建议：</h5>
              <ul>
                {optimizationResults.suggestions.map((item: string, index: number) => (
                  <li key={index}>{item}</li>
                ))}
              </ul>
            </div>
          )}

          <Form.Item>
            <Button
              type="primary"
              htmlType="submit"
              style={{ width: '100%' }}
              loading={optimizationProgress > 0 && optimizationProgress < 100}
            >
              开始优化
            </Button>
          </Form.Item>
        </Form>
      </Modal>

      {/* 预览模态框 */}
      <Modal
        title={`预览简历: ${selectedResume?.title}`}
        open={previewModalVisible}
        onCancel={() => setPreviewModalVisible(false)}
        footer={null}
        width={800}
      >
        <div style={{ padding: 20, backgroundColor: '#f9f9f9', borderRadius: 6 }}>
          <h3>{selectedResume?.title}</h3>
          <p>质量分数: {(selectedResume?.qualityScore * 100).toFixed(1)}%</p>
          <p>AI优化状态: {selectedResume?.aiOptimized ? '已优化' : '未优化'}</p>
          <Alert
            message="预览功能"
            description="完整的简历预览功能正在开发中，将支持富文本格式和样式预览。"
            type="info"
            showIcon
          />
        </div>
      </Modal>
    </Layout>
  );
};

export default ResumeOptimizationPage;