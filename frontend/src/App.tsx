import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { Layout } from 'antd';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import DashboardPage from './pages/DashboardPage';
import ResumeManagementPage from './pages/ResumeManagementPage';
import ProjectManagementPage from './pages/ProjectManagementPage';
import JobRequirementPage from './pages/JobRequirementPage';
import ResumeOptimizationPage from './pages/ResumeOptimizationPage';
import CoverLetterPage from './pages/CoverLetterPage';
import './App.css';

const { Header, Content } = Layout;

const App: React.FC = () => {
  return (
    <Router>
      <Layout className="app-layout">
        <Header className="app-header">
          <div className="logo">
            <h1 style={{ color: 'white', margin: 0 }}>CV Agent</h1>
          </div>
        </Header>
        <Content className="app-content">
          <Routes>
            <Route path="/" element={<Navigate to="/login" replace />} />
            <Route path="/login" element={<LoginPage />} />
            <Route path="/register" element={<RegisterPage />} />
            <Route path="/dashboard" element={<DashboardPage />} />
            <Route path="/resumes" element={<ResumeManagementPage />} />
            <Route path="/projects" element={<ProjectManagementPage />} />
            <Route path="/job-requirements" element={<JobRequirementPage />} />
            <Route path="/resume-optimization" element={<ResumeOptimizationPage />} />
            <Route path="/cover-letters" element={<CoverLetterPage />} />
          </Routes>
        </Content>
      </Layout>
    </Router>
  );
};

export default App;