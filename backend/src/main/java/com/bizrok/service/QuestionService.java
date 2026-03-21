package main.java.com.bizrok.service;

import main.java.com.bizrok.model.dto.QuestionDto;
import main.java.com.bizrok.model.entity.*;
import main.java.com.bizrok.repository.GroupRepository;
import main.java.com.bizrok.repository.QuestionRepository;
import main.java.com.bizrok.repository.OptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Question Service for dynamic question system
 * Provides questions based on model selection and previous answers
 */
@Service
public class QuestionService {
    
    @Autowired
    private GroupRepository groupRepository;
    
    @Autowired
    private QuestionRepository questionRepository;
    
    @Autowired
    private OptionRepository optionRepository;
    
    /**
     * Get all questions for a model with their options
     * This is used for the frontend question flow
     */
    public List<QuestionDto> getQuestionsForModel() {
        List<Group> groups = groupRepository.findActiveGroupsWithSubGroupsAndQuestions();
        
        List<QuestionDto> questionDtos = new ArrayList<>();
        
        for (Group group : groups) {
            for (Question question : group.getQuestions()) {
                if (question.getIsActive()) {
                    QuestionDto.QuestionDto questionDto = convertToQuestionDto(question, group);
                    questionDtos.add(questionDto);
                }
            }
        }
        
        return questionDtos;
    }
    
    /**
     * Get questions by group
     */
    public List<QuestionDto> getQuestionsByGroup(Long groupId) {
        List<Question> questions = questionRepository.findActiveQuestionsWithOptionsByGroup(groupId);
        
        return questions.stream()
                .map(question -> convertToQuestionDto(question, question.getGroup()))
                .collect(Collectors.toList());
    }
    
    /**
     * Get questions by sub-group
     */
    public List<QuestionDto> getQuestionsBySubGroup(Long subGroupId) {
        List<Question> questions = questionRepository.findActiveQuestionsBySubGroup(subGroupId);
        
        return questions.stream()
                .map(question -> convertToQuestionDto(question, question.getGroup()))
                .collect(Collectors.toList());
    }
    
    /**
     * Get question by ID with options
     */
    public QuestionDto getQuestionWithOptions(Long questionId) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Question not found"));
        
        return convertToQuestionDto(question, question.getGroup());
    }
    
    /**
     * Get all active groups
     */
    public List<Group> getAllGroups() {
        return groupRepository.findByIsActiveTrueOrderBySortOrderAsc();
    }
    
    /**
     * Get options for a question
     */
    public List<QuestionDto.OptionDto> getOptionsForQuestion(Long questionId) {
        List<Option> options = optionRepository.findActiveOptionsByQuestion(questionId);
        
        return options.stream()
                .map(this::convertToOptionDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Convert Question entity to DTO
     */
    private QuestionDto.QuestionDto convertToQuestionDto(Question question, Group group) {
        QuestionDto.QuestionDto dto = new QuestionDto.QuestionDto();
        
        dto.setId(question.getId());
        dto.setText(question.getText());
        dto.setSlug(question.getSlug());
        dto.setGroupName(group.getName());
        dto.setSubGroupName(question.getSubGroup() != null ? question.getSubGroup().getName() : null);
        dto.setQuestionType(question.getQuestionType().name());
        dto.setRequired(question.getIsRequired());
        dto.setActive(question.getIsActive());
        dto.setSortOrder(question.getSortOrder());
        
        // Convert options
        List<Option> options = optionRepository.findActiveOptionsByQuestion(question.getId());
        List<QuestionDto.OptionDto> optionDtos = options.stream()
                .map(this::convertToOptionDto)
                .collect(Collectors.toList());
        
        dto.setOptions(optionDtos);
        
        return dto;
    }
    
    /**
     * Convert Option entity to DTO
     */
    private QuestionDto.OptionDto convertToOptionDto(Option option) {
        QuestionDto.OptionDto dto = new QuestionDto.OptionDto();
        
        dto.setId(option.getId());
        dto.setText(option.getText());
        dto.setSlug(option.getSlug());
        dto.setDeductionValue(option.getDeductionValue());
        dto.setDeductionType(option.getDeductionType().name());
        dto.setImageUrl(option.getImageUrl());
        dto.setActive(option.getIsActive());
        dto.setSortOrder(option.getSortOrder());
        
        return dto;
    }
    
    /**
     * Validate user answers
     */
    public boolean validateAnswers(List<QuestionDto> questions, List<Long> selectedOptionIds) {
        // Check if all required questions have answers
        List<Long> requiredQuestionIds = questions.stream()
                .filter(QuestionDto::getRequired)
                .map(QuestionDto::getId)
                .collect(Collectors.toList());
        
        // This is a simplified validation
        // In a real implementation, you would check if the selected options are valid for the questions
        
        return true;
    }
    
    /**
     * Get question flow progress
     */
    public QuestionFlowProgress getQuestionFlowProgress(List<QuestionDto> allQuestions, List<Long> answeredQuestionIds) {
        int totalQuestions = allQuestions.size();
        int answeredQuestions = answeredQuestionIds.size();
        int progressPercentage = totalQuestions > 0 ? (answeredQuestions * 100) / totalQuestions : 0;
        
        return new QuestionFlowProgress(totalQuestions, answeredQuestions, progressPercentage);
    }
    
    /**
     * Question Flow Progress
     */
    public static class QuestionFlowProgress {
        private final int totalQuestions;
        private final int answeredQuestions;
        private final int progressPercentage;
        
        public QuestionFlowProgress(int totalQuestions, int answeredQuestions, int progressPercentage) {
            this.totalQuestions = totalQuestions;
            this.answeredQuestions = answeredQuestions;
            this.progressPercentage = progressPercentage;
        }
        
        // Getters
        public int getTotalQuestions() { return totalQuestions; }
        public int getAnsweredQuestions() { return answeredQuestions; }
        public int getProgressPercentage() { return progressPercentage; }
    }
}