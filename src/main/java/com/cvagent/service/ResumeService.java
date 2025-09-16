package com.cvagent.service;

import com.cvagent.dto.ResumeDto;
import com.cvagent.model.FileDocument;
import com.cvagent.model.Resume;
import com.cvagent.model.User;
import com.cvagent.repository.ResumeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ResumeService {

    private static final Logger logger = LoggerFactory.getLogger(ResumeService.class);

    @Autowired
    private ResumeRepository resumeRepository;

    @Autowired
    private FileService fileService;

    /**
     * 创建简历
     */
    public Resume createResume(ResumeDto resumeDto, User user) {
        Resume resume = new Resume();
        resume.setTitle(resumeDto.getTitle());
        resume.setSummary(resumeDto.getSummary());
        resume.setEducation(resumeDto.getEducation());
        resume.setExperience(resumeDto.getExperience());
        resume.setSkills(resumeDto.getSkills());
        resume.setCertifications(resumeDto.getCertifications());
        resume.setLanguages(resumeDto.getLanguages());
        resume.setYearsOfExperience(resumeDto.getYearsOfExperience());
        resume.setTargetIndustry(resumeDto.getTargetIndustry());
        resume.setTargetPosition(resumeDto.getTargetPosition());
        resume.setUser(user);

        Resume savedResume = resumeRepository.save(resume);
        logger.info("简历创建成功: {}", savedResume.getId());
        return savedResume;
    }

    /**
     * 获取用户的简历列表
     */
    public List<Resume> getUserResumes(User user) {
        return resumeRepository.findByUserId(user.getId());
    }

    /**
     * 根据ID获取简历
     */
    public Resume getResumeById(String id, User user) {
        return resumeRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new RuntimeException("简历不存在或无权限访问"));
    }

    /**
     * 更新简历
     */
    public Resume updateResume(String id, ResumeDto resumeDto, User user) {
        Resume resume = getResumeById(id, user);

        resume.setTitle(resumeDto.getTitle());
        resume.setSummary(resumeDto.getSummary());
        resume.setEducation(resumeDto.getEducation());
        resume.setExperience(resumeDto.getExperience());
        resume.setSkills(resumeDto.getSkills());
        resume.setCertifications(resumeDto.getCertifications());
        resume.setLanguages(resumeDto.getLanguages());
        resume.setYearsOfExperience(resumeDto.getYearsOfExperience());
        resume.setTargetIndustry(resumeDto.getTargetIndustry());
        resume.setTargetPosition(resumeDto.getTargetPosition());
        resume.onUpdate();

        Resume updatedResume = resumeRepository.save(resume);
        logger.info("简历更新成功: {}", updatedResume.getId());
        return updatedResume;
    }

    /**
     * 删除简历
     */
    public void deleteResume(String id, User user) {
        Resume resume = getResumeById(id, user);
        resumeRepository.delete(resume);
        logger.info("简历删除成功: {}", id);
    }

    /**
     * 根据关键词搜索简历
     */
    public List<Resume> searchResumes(User user, String keyword) {
        List<Resume> allResults = resumeRepository.searchByKeyword(keyword);
        // 过滤出当前用户的简历
        return allResults.stream()
                .filter(resume -> resume.getUser().getId().equals(user.getId()))
                .toList();
    }

    /**
     * 获取最近更新的简历
     */
    public List<Resume> getRecentResumes(User user) {
        return resumeRepository.findLatestByUserId(user.getId());
    }
}