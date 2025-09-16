package com.cvagent.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 增强版简历实体类
 * 用于支持更复杂的简历结构和生成功能
 */
@Document(collection = "enhanced_resumes")
public class EnhancedResume {

    @Id
    private String id;

    private String userId;
    private String baseResumeId; // 关联的基础简历ID
    private String jobRequirementId; // 关联的招聘需求ID

    // 基本信息
    private String name;
    private String email;
    private String phone;
    private String location;
    private String website;
    private String linkedin;
    private String title;
    private String summary;
    private String objective;

    // 详细信息结构
    private PersonalInfo personalInfo;
    private WorkExperience workExperience;
    private Education education;
    private Skills skills;
    private Projects projects;
    private Certifications certifications;
    private Languages languages;
    private Interests interests;

    // 匹配和优化信息
    private Double matchScore;
    private String optimizationStatus;
    private Map<String, Object> optimizationMetrics;
    private List<String> optimizationSuggestions;
    private String aiGeneratedSummary;
    private String optimizationReport;
    private LocalDateTime optimizedAt;

    // 模板和格式信息
    private String templateId;
    private String format; // PDF, DOC, HTML
    private String version;
    private Boolean isPublic;
    private Integer viewCount;

    // 生成信息
    private String generatedBy; // AI_GENERATED, MANUAL_OPTIMIZED
    private LocalDateTime generatedAt;
    private LocalDateTime lastModifiedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * 个人信息内部类
     */
    public static class PersonalInfo {
        private String firstName;
        private String lastName;
        private String fullName;
        private String address;
        private String city;
        private String state;
        private String zipCode;
        private String country;
        private String photoUrl;

        // Getters and Setters
        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }
        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }
        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }
        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }
        public String getState() { return state; }
        public void setState(String state) { this.state = state; }
        public String getZipCode() { return zipCode; }
        public void setZipCode(String zipCode) { this.zipCode = zipCode; }
        public String getCountry() { return country; }
        public void setCountry(String country) { this.country = country; }
        public String getPhotoUrl() { return photoUrl; }
        public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }

        public String getName() { return fullName; }
        public void setName(String name) { this.fullName = name; }

        public String getEmail() {
            // 简化实现，实际应该从父类获取
            return "";
        }
        public void setEmail(String email) {
            // 简化实现
        }

        public String getPhone() {
            // 简化实现
            return "";
        }
        public void setPhone(String phone) {
            // 简化实现
        }

        public String getLocation() {
            // 简化实现
            return "";
        }
        public void setLocation(String location) {
            // 简化实现
        }

        public String getSummary() {
            // 简化实现
            return "";
        }
        public void setSummary(String summary) {
            // 简化实现
        }
    }

    /**
     * 工作经验内部类
     */
    public static class WorkExperience {
        private List<ExperienceItem> experiences;

        public static class ExperienceItem {
            private String id;
            private String company;
            private String position;
            private String location;
            private LocalDateTime startDate;
            private LocalDateTime endDate;
            private Boolean isCurrent;
            private String description;
            private List<String> achievements;
            private List<String> technologies;
            private Double relevanceScore; // 与目标职位的相关性分数

            // Getters and Setters
            public String getId() { return id; }
            public void setId(String id) { this.id = id; }
            public String getCompany() { return company; }
            public void setCompany(String company) { this.company = company; }
            public String getPosition() { return position; }
            public void setPosition(String position) { this.position = position; }
            public String getLocation() { return location; }
            public void setLocation(String location) { this.location = location; }
            public LocalDateTime getStartDate() { return startDate; }
            public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }
            public LocalDateTime getEndDate() { return endDate; }
            public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }
            public Boolean getIsCurrent() { return isCurrent; }
            public void setIsCurrent(Boolean isCurrent) { this.isCurrent = isCurrent; }
            public String getDescription() { return description; }
            public void setDescription(String description) { this.description = description; }
            public List<String> getAchievements() { return achievements; }
            public void setAchievements(List<String> achievements) { this.achievements = achievements; }
            public List<String> getTechnologies() { return technologies; }
            public void setTechnologies(List<String> technologies) { this.technologies = technologies; }
            public Double getRelevanceScore() { return relevanceScore; }
            public void setRelevanceScore(Double relevanceScore) { this.relevanceScore = relevanceScore; }
        }

        // Getters and Setters
        public List<ExperienceItem> getExperiences() { return experiences; }
        public void setExperiences(List<ExperienceItem> experiences) { this.experiences = experiences; }
    }

    /**
     * 教育背景内部类
     */
    public static class Education {
        private List<EducationItem> educations;

        public static class EducationItem {
            private String id;
            private String institution;
            private String degree;
            private String major;
            private String minor;
            private LocalDateTime startDate;
            private LocalDateTime endDate;
            private Double gpa;
            private String description;
            private List<String> coursework;
            private List<String> honors;
            private Double relevanceScore;

            // Getters and Setters
            public String getId() { return id; }
            public void setId(String id) { this.id = id; }
            public String getInstitution() { return institution; }
            public void setInstitution(String institution) { this.institution = institution; }
            public String getDegree() { return degree; }
            public void setDegree(String degree) { this.degree = degree; }
            public String getMajor() { return major; }
            public void setMajor(String major) { this.major = major; }
            public String getMinor() { return minor; }
            public void setMinor(String minor) { this.minor = minor; }
            public LocalDateTime getStartDate() { return startDate; }
            public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }
            public LocalDateTime getEndDate() { return endDate; }
            public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }
            public Double getGpa() { return gpa; }
            public void setGpa(Double gpa) { this.gpa = gpa; }
            public String getDescription() { return description; }
            public void setDescription(String description) { this.description = description; }
            public List<String> getCoursework() { return coursework; }
            public void setCoursework(List<String> coursework) { this.coursework = coursework; }
            public List<String> getHonors() { return honors; }
            public void setHonors(List<String> honors) { this.honors = honors; }
            public Double getRelevanceScore() { return relevanceScore; }
            public void setRelevanceScore(Double relevanceScore) { this.relevanceScore = relevanceScore; }
        }

        // Getters and Setters
        public List<EducationItem> getEducations() { return educations; }
        public void setEducations(List<EducationItem> educations) { this.educations = educations; }
    }

    /**
     * 技能内部类
     */
    public static class Skills {
        private List<String> technicalSkills;
        private List<String> programmingLanguages;
        private List<String> frameworks;
        private List<String> databases;
        private List<String> tools;
        private List<String> softSkills;
        private List<String> languages;
        private Map<String, Integer> skillLevels; // 技能名称-熟练度等级
        private Map<String, Double> skillRelevanceScores; // 技能与目标职位的相关性分数

        // Getters and Setters
        public List<String> getTechnicalSkills() { return technicalSkills; }
        public void setTechnicalSkills(List<String> technicalSkills) { this.technicalSkills = technicalSkills; }
        public List<String> getProgrammingLanguages() { return programmingLanguages; }
        public void setProgrammingLanguages(List<String> programmingLanguages) { this.programmingLanguages = programmingLanguages; }
        public List<String> getFrameworks() { return frameworks; }
        public void setFrameworks(List<String> frameworks) { this.frameworks = frameworks; }
        public List<String> getDatabases() { return databases; }
        public void setDatabases(List<String> databases) { this.databases = databases; }
        public List<String> getTools() { return tools; }
        public void setTools(List<String> tools) { this.tools = tools; }
        public List<String> getSoftSkills() { return softSkills; }
        public void setSoftSkills(List<String> softSkills) { this.softSkills = softSkills; }
        public List<String> getLanguages() { return languages; }
        public void setLanguages(List<String> languages) { this.languages = languages; }
        public Map<String, Integer> getSkillLevels() { return skillLevels; }
        public void setSkillLevels(Map<String, Integer> skillLevels) { this.skillLevels = skillLevels; }
        public Map<String, Double> getSkillRelevanceScores() { return skillRelevanceScores; }
        public void setSkillRelevanceScores(Map<String, Double> skillRelevanceScores) { this.skillRelevanceScores = skillRelevanceScores; }
    }

    /**
     * 项目经验内部类
     */
    public static class Projects {
        private List<ProjectItem> projects;

        public static class ProjectItem {
            private String id;
            private String name;
            private String description;
            private String role;
            private LocalDateTime startDate;
            private LocalDateTime endDate;
            private String status;
            private List<String> technologies;
            private List<String> achievements;
            private String url;
            private String githubUrl;
            private Double relevanceScore;

            // Getters and Setters
            public String getId() { return id; }
            public void setId(String id) { this.id = id; }
            public String getName() { return name; }
            public void setName(String name) { this.name = name; }
            public String getDescription() { return description; }
            public void setDescription(String description) { this.description = description; }
            public String getRole() { return role; }
            public void setRole(String role) { this.role = role; }
            public LocalDateTime getStartDate() { return startDate; }
            public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }
            public LocalDateTime getEndDate() { return endDate; }
            public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }
            public String getStatus() { return status; }
            public void setStatus(String status) { this.status = status; }
            public List<String> getTechnologies() { return technologies; }
            public void setTechnologies(List<String> technologies) { this.technologies = technologies; }
            public List<String> getAchievements() { return achievements; }
            public void setAchievements(List<String> achievements) { this.achievements = achievements; }
            public String getUrl() { return url; }
            public void setUrl(String url) { this.url = url; }
            public String getGithubUrl() { return githubUrl; }
            public void setGithubUrl(String githubUrl) { this.githubUrl = githubUrl; }
            public Double getRelevanceScore() { return relevanceScore; }
            public void setRelevanceScore(Double relevanceScore) { this.relevanceScore = relevanceScore; }
        }

        // Getters and Setters
        public List<ProjectItem> getProjects() { return projects; }
        public void setProjects(List<ProjectItem> projects) { this.projects = projects; }
    }

    /**
     * 证书认证内部类
     */
    public static class Certifications {
        private List<CertificationItem> certifications;

        public static class CertificationItem {
            private String id;
            private String name;
            private String issuer;
            private LocalDateTime issueDate;
            private LocalDateTime expiryDate;
            private String credentialId;
            private String credentialUrl;
            private Boolean isExpired;
            private Double relevanceScore;

            // Getters and Setters
            public String getId() { return id; }
            public void setId(String id) { this.id = id; }
            public String getName() { return name; }
            public void setName(String name) { this.name = name; }
            public String getIssuer() { return issuer; }
            public void setIssuer(String issuer) { this.issuer = issuer; }
            public LocalDateTime getIssueDate() { return issueDate; }
            public void setIssueDate(LocalDateTime issueDate) { this.issueDate = issueDate; }
            public LocalDateTime getExpiryDate() { return expiryDate; }
            public void setExpiryDate(LocalDateTime expiryDate) { this.expiryDate = expiryDate; }
            public String getCredentialId() { return credentialId; }
            public void setCredentialId(String credentialId) { this.credentialId = credentialId; }
            public String getCredentialUrl() { return credentialUrl; }
            public void setCredentialUrl(String credentialUrl) { this.credentialUrl = credentialUrl; }
            public Boolean getIsExpired() { return isExpired; }
            public void setIsExpired(Boolean isExpired) { this.isExpired = isExpired; }
            public Double getRelevanceScore() { return relevanceScore; }
            public void setRelevanceScore(Double relevanceScore) { this.relevanceScore = relevanceScore; }
        }

        // Getters and Setters
        public List<CertificationItem> getCertifications() { return certifications; }
        public void setCertifications(List<CertificationItem> certifications) { this.certifications = certifications; }
    }

    /**
     * 语言能力内部类
     */
    public static class Languages {
        private List<LanguageItem> languages;

        public static class LanguageItem {
            private String id;
            private String name;
            private String proficiency; // 初级、中级、高级、母语
            private String certification;
            private Double relevanceScore;

            // Getters and Setters
            public String getId() { return id; }
            public void setId(String id) { this.id = id; }
            public String getName() { return name; }
            public void setName(String name) { this.name = name; }
            public String getProficiency() { return proficiency; }
            public void setProficiency(String proficiency) { this.proficiency = proficiency; }
            public String getCertification() { return certification; }
            public void setCertification(String certification) { this.certification = certification; }
            public Double getRelevanceScore() { return relevanceScore; }
            public void setRelevanceScore(Double relevanceScore) { this.relevanceScore = relevanceScore; }
        }

        // Getters and Setters
        public List<LanguageItem> getLanguages() { return languages; }
        public void setLanguages(List<LanguageItem> languages) { this.languages = languages; }
    }

    /**
     * 兴趣爱好内部类
     */
    public static class Interests {
        private List<String> hobbies;
        private List<String> activities;
        private List<String> volunteerWork;

        // Getters and Setters
        public List<String> getHobbies() { return hobbies; }
        public void setHobbies(List<String> hobbies) { this.hobbies = hobbies; }
        public List<String> getActivities() { return activities; }
        public void setActivities(List<String> activities) { this.activities = activities; }
        public List<String> getVolunteerWork() { return volunteerWork; }
        public void setVolunteerWork(List<String> volunteerWork) { this.volunteerWork = volunteerWork; }
    }

    // 构造函数
    public EnhancedResume() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.lastModifiedAt = LocalDateTime.now();
        this.generatedAt = LocalDateTime.now();
        this.isPublic = false;
        this.viewCount = 0;
        this.version = "1.0";
        this.personalInfo = new PersonalInfo();
        this.workExperience = new WorkExperience();
        this.education = new Education();
        this.skills = new Skills();
        this.projects = new Projects();
        this.certifications = new Certifications();
        this.languages = new Languages();
        this.interests = new Interests();
        this.generatedBy = "system";
        this.optimizationStatus = "DRAFT";
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getBaseResumeId() { return baseResumeId; }
    public void setBaseResumeId(String baseResumeId) { this.baseResumeId = baseResumeId; }

    public String getJobRequirementId() { return jobRequirementId; }
    public void setJobRequirementId(String jobRequirementId) { this.jobRequirementId = jobRequirementId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }

    public String getLinkedin() { return linkedin; }
    public void setLinkedin(String linkedin) { this.linkedin = linkedin; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public String getObjective() { return objective; }
    public void setObjective(String objective) { this.objective = objective; }

    public PersonalInfo getPersonalInfo() { return personalInfo; }
    public void setPersonalInfo(PersonalInfo personalInfo) { this.personalInfo = personalInfo; }

    public WorkExperience getWorkExperience() { return workExperience; }
    public void setWorkExperience(WorkExperience workExperience) { this.workExperience = workExperience; }

    public Education getEducation() { return education; }
    public void setEducation(Education education) { this.education = education; }

    public Skills getSkills() { return skills; }
    public void setSkills(Skills skills) { this.skills = skills; }

    public Projects getProjects() { return projects; }
    public void setProjects(Projects projects) { this.projects = projects; }

    public Certifications getCertifications() { return certifications; }
    public void setCertifications(Certifications certifications) { this.certifications = certifications; }

    public Languages getLanguages() { return languages; }
    public void setLanguages(Languages languages) { this.languages = languages; }

    public Interests getInterests() { return interests; }
    public void setInterests(Interests interests) { this.interests = interests; }

    public Double getMatchScore() { return matchScore; }
    public void setMatchScore(Double matchScore) { this.matchScore = matchScore; }

    public String getOptimizationStatus() { return optimizationStatus; }
    public void setOptimizationStatus(String optimizationStatus) { this.optimizationStatus = optimizationStatus; }

    public Map<String, Object> getOptimizationMetrics() { return optimizationMetrics; }
    public void setOptimizationMetrics(Map<String, Object> optimizationMetrics) { this.optimizationMetrics = optimizationMetrics; }

    public List<String> getOptimizationSuggestions() { return optimizationSuggestions; }
    public void setOptimizationSuggestions(List<String> optimizationSuggestions) { this.optimizationSuggestions = optimizationSuggestions; }

    public String getAiGeneratedSummary() { return aiGeneratedSummary; }
    public void setAiGeneratedSummary(String aiGeneratedSummary) { this.aiGeneratedSummary = aiGeneratedSummary; }

    public String getOptimizationReport() { return optimizationReport; }
    public void setOptimizationReport(String optimizationReport) { this.optimizationReport = optimizationReport; }

    public LocalDateTime getOptimizedAt() { return optimizedAt; }
    public void setOptimizedAt(LocalDateTime optimizedAt) { this.optimizedAt = optimizedAt; }

    public String getTemplateId() { return templateId; }
    public void setTemplateId(String templateId) { this.templateId = templateId; }

    public String getFormat() { return format; }
    public void setFormat(String format) { this.format = format; }

    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }

    public Boolean getIsPublic() { return isPublic; }
    public void setIsPublic(Boolean isPublic) { this.isPublic = isPublic; }

    public Integer getViewCount() { return viewCount; }
    public void setViewCount(Integer viewCount) { this.viewCount = viewCount; }

    public String getGeneratedBy() { return generatedBy; }
    public void setGeneratedBy(String generatedBy) { this.generatedBy = generatedBy; }

    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }

    public LocalDateTime getLastModifiedAt() { return lastModifiedAt; }
    public void setLastModifiedAt(LocalDateTime lastModifiedAt) { this.lastModifiedAt = lastModifiedAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public void updateTimestamp() {
        this.updatedAt = LocalDateTime.now();
        this.lastModifiedAt = LocalDateTime.now();
    }

    public void incrementViewCount() {
        this.viewCount = this.viewCount + 1;
    }

    /**
     * 获取简历的文本内容，用于关键词匹配和分析
     */
    public String getTextContent() {
        StringBuilder content = new StringBuilder();

        // 基本信息
        if (name != null) content.append(name).append(" ");
        if (title != null) content.append(title).append(" ");
        if (summary != null) content.append(summary).append(" ");
        if (objective != null) content.append(objective).append(" ");

        // 个人信息
        if (personalInfo != null) {
            if (personalInfo.getFullName() != null) content.append(personalInfo.getFullName()).append(" ");
            if (personalInfo.getAddress() != null) content.append(personalInfo.getAddress()).append(" ");
        }

        // 工作经验
        if (workExperience != null && workExperience.getExperiences() != null) {
            for (WorkExperience.ExperienceItem exp : workExperience.getExperiences()) {
                if (exp.getCompany() != null) content.append(exp.getCompany()).append(" ");
                if (exp.getPosition() != null) content.append(exp.getPosition()).append(" ");
                if (exp.getDescription() != null) content.append(exp.getDescription()).append(" ");
                if (exp.getAchievements() != null) {
                    for (String achievement : exp.getAchievements()) {
                        content.append(achievement).append(" ");
                    }
                }
                if (exp.getTechnologies() != null) {
                    for (String tech : exp.getTechnologies()) {
                        content.append(tech).append(" ");
                    }
                }
            }
        }

        // 教育背景
        if (education != null && education.getEducations() != null) {
            for (Education.EducationItem edu : education.getEducations()) {
                if (edu.getInstitution() != null) content.append(edu.getInstitution()).append(" ");
                if (edu.getDegree() != null) content.append(edu.getDegree()).append(" ");
                if (edu.getMajor() != null) content.append(edu.getMajor()).append(" ");
                if (edu.getDescription() != null) content.append(edu.getDescription()).append(" ");
            }
        }

        // 技能
        if (skills != null) {
            if (skills.getTechnicalSkills() != null) {
                for (String skill : skills.getTechnicalSkills()) {
                    content.append(skill).append(" ");
                }
            }
            if (skills.getProgrammingLanguages() != null) {
                for (String lang : skills.getProgrammingLanguages()) {
                    content.append(lang).append(" ");
                }
            }
            if (skills.getFrameworks() != null) {
                for (String framework : skills.getFrameworks()) {
                    content.append(framework).append(" ");
                }
            }
        }

        // 项目经验
        if (projects != null && projects.getProjects() != null) {
            for (Projects.ProjectItem project : projects.getProjects()) {
                if (project.getName() != null) content.append(project.getName()).append(" ");
                if (project.getDescription() != null) content.append(project.getDescription()).append(" ");
                if (project.getTechnologies() != null) {
                    for (String tech : project.getTechnologies()) {
                        content.append(tech).append(" ");
                    }
                }
            }
        }

        return content.toString().trim();
    }
}