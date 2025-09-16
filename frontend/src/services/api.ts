import axios from 'axios';
import { ApiResponse, LoginRequest, RegisterRequest, AuthResponse } from '../types';

// 创建axios实例
const api = axios.create({
  baseURL: '/api',
  timeout: 10000,
});

// 请求拦截器
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// 响应拦截器
api.interceptors.response.use(
  (response) => {
    return response;
  },
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

// 认证相关API
export const authApi = {
  login: async (data: LoginRequest): Promise<ApiResponse<AuthResponse>> => {
    const response = await api.post('/auth/login', data);
    return response.data;
  },

  register: async (data: RegisterRequest): Promise<ApiResponse<AuthResponse>> => {
    const response = await api.post('/auth/register', data);
    return response.data;
  },

  logout: async (): Promise<void> => {
    await api.post('/auth/logout');
  },

  getCurrentUser: async (): Promise<ApiResponse<any>> => {
    const response = await api.get('/auth/me');
    return response.data;
  },
};

// 简历相关API
export const resumeApi = {
  getResumes: async (userId?: string): Promise<ApiResponse<any[]>> => {
    const params = userId ? { userId } : {};
    const response = await api.get('/resumes', { params });
    return response.data;
  },

  getResumeById: async (id: string): Promise<ApiResponse<any>> => {
    const response = await api.get(`/resumes/${id}`);
    return response.data;
  },

  createResume: async (data: any): Promise<ApiResponse<any>> => {
    const response = await api.post('/resumes', data);
    return response.data;
  },

  updateResume: async (id: string, data: any): Promise<ApiResponse<any>> => {
    const response = await api.put(`/resumes/${id}`, data);
    return response.data;
  },

  deleteResume: async (id: string): Promise<void> => {
    await api.delete(`/resumes/${id}`);
  },

  optimizeResume: async (id: string, options: any): Promise<ApiResponse<any>> => {
    const response = await api.post(`/resumes/${id}/optimize`, options);
    return response.data;
  },

  exportResume: async (id: string, format: string): Promise<Blob> => {
    const response = await api.get(`/resumes/${id}/export`, {
      params: { format },
      responseType: 'blob',
    });
    return response.data;
  },
};

// 求职信相关API
export const coverLetterApi = {
  getCoverLetters: async (params: any): Promise<ApiResponse<any[]>> => {
    const response = await api.get('/cover-letters', { params });
    return response.data;
  },

  getCoverLetterById: async (id: string): Promise<ApiResponse<any>> => {
    const response = await api.get(`/cover-letters/${id}`);
    return response.data;
  },

  createBasicCoverLetter: async (data: any): Promise<ApiResponse<any>> => {
    const response = await api.post('/cover-letters/basic', data);
    return response.data;
  },

  createPersonalizedCoverLetter: async (data: any): Promise<ApiResponse<any>> => {
    const response = await api.post('/cover-letters/personalized', data);
    return response.data;
  },

  optimizeCoverLetter: async (id: string, options: any): Promise<ApiResponse<any>> => {
    const response = await api.post(`/cover-letters/${id}/optimize`, options);
    return response.data;
  },

  customizeCoverLetter: async (id: string, customizations: any): Promise<ApiResponse<any>> => {
    const response = await api.put(`/cover-letters/${id}/customize`, customizations);
    return response.data;
  },

  deleteCoverLetter: async (id: string): Promise<void> => {
    await api.delete(`/cover-letters/${id}`);
  },

  exportCoverLetter: async (id: string, format: string): Promise<Blob> => {
    const response = await api.get(`/cover-letters/${id}/export`, {
      params: { format },
      responseType: 'blob',
    });
    return response.data;
  },

  getTemplates: async (params: any): Promise<ApiResponse<any[]>> => {
    const response = await api.get('/cover-letters/templates', { params });
    return response.data;
  },
};

// 招聘需求相关API
export const jobRequirementApi = {
  getJobRequirements: async (userId?: string): Promise<ApiResponse<any[]>> => {
    const params = userId ? { userId } : {};
    const response = await api.get('/job-requirements', { params });
    return response.data;
  },

  getJobRequirementById: async (id: string): Promise<ApiResponse<any>> => {
    const response = await api.get(`/job-requirements/${id}`);
    return response.data;
  },

  createJobRequirement: async (data: any): Promise<ApiResponse<any>> => {
    const response = await api.post('/job-requirements', data);
    return response.data;
  },

  updateJobRequirement: async (id: string, data: any): Promise<ApiResponse<any>> => {
    const response = await api.put(`/job-requirements/${id}`, data);
    return response.data;
  },

  deleteJobRequirement: async (id: string): Promise<void> => {
    await api.delete(`/job-requirements/${id}`);
  },
};

export default api;