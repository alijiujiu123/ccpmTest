// 用户相关类型
export interface User {
  id: string;
  username: string;
  email: string;
  role: 'user' | 'admin';
  createdAt: string;
}

// 登录请求
export interface LoginRequest {
  username: string;
  password: string;
}

// 注册请求
export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
}

// 认证响应
export interface AuthResponse {
  token: string;
  user: User;
}

// 简历相关类型
export interface Resume {
  id: string;
  userId: string;
  title: string;
  content: ResumeContent;
  skills: ResumeSkills;
  projects: Project[];
  education: Education[];
  experience: Experience[];
  qualityScore: number;
  aiOptimized: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface ResumeContent {
  personalInfo: PersonalInfo;
  summary: string;
  experience: string;
  education: string;
  skills: string;
  projects: string;
}

export interface PersonalInfo {
  name: string;
  email: string;
  phone: string;
  location: string;
  linkedin?: string;
  github?: string;
}

export interface ResumeSkills {
  technicalSkills: string[];
  softSkills: string[];
  languages: Language[];
  certifications: Certification[];
}

export interface Language {
  name: string;
  proficiency: '基础' | '中级' | '高级' | '母语';
}

export interface Certification {
  name: string;
  issuer: string;
  date: string;
}

export interface Project {
  id: string;
  name: string;
  description: string;
  technologies: string[];
  startDate: string;
  endDate?: string;
  achievements: string[];
}

export interface Education {
  id: string;
  institution: string;
  degree: string;
  major: string;
  startDate: string;
  endDate?: string;
  gpa?: string;
}

export interface Experience {
  id: string;
  company: string;
  position: string;
  startDate: string;
  endDate?: string;
  responsibilities: string[];
  achievements: string[];
}

// 求职信相关类型
export interface CoverLetter {
  id: string;
  userId: string;
  resumeId?: string;
  jobRequirementId?: string;
  templateId?: string;
  title: string;
  companyName: string;
  position: string;
  content: CoverLetterContent;
  generatedBy: 'ai_generated' | 'manual_edited' | 'template_based';
  aiOptimized: boolean;
  qualityScore?: number;
  matchScore?: number;
  status: 'draft' | 'ready' | 'sent' | 'archived';
  createdAt: string;
  updatedAt: string;
}

export interface CoverLetterContent {
  salutation: string;
  openingParagraph: string;
  bodyParagraphs: string;
  closingParagraph: string;
  signature: string;
  contactInfo?: string;
  postscript?: string;
}

// 招聘需求相关类型
export interface JobRequirement {
  id: string;
  userId: string;
  title: string;
  company: string;
  description: string;
  requirements: string[];
  skills: string[];
  experienceLevel: '初级' | '中级' | '高级' | '资深';
  salaryRange?: string;
  location: string;
  jobType: '全职' | '兼职' | '实习' | '合同';
  createdAt: string;
  updatedAt: string;
}

// API响应类型
export interface ApiResponse<T> {
  success: boolean;
  data?: T;
  message?: string;
  error?: string;
}

export interface PaginatedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}