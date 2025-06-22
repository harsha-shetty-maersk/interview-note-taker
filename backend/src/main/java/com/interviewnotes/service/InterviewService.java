package com.interviewnotes.service;

import com.interviewnotes.model.Interview;
import com.interviewnotes.model.Candidate;
import com.interviewnotes.model.User;
import com.interviewnotes.repository.InterviewRepository;
import com.interviewnotes.repository.CandidateRepository;
import com.interviewnotes.repository.UserRepository;
import com.interviewnotes.controller.InterviewController.InterviewDTO;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class InterviewService {
    @Autowired
    private InterviewRepository interviewRepository;
    @Autowired
    private CandidateRepository candidateRepository;
    @Autowired
    private UserRepository userRepository;

    private InterviewDTO toDTO(Interview interview) {
        InterviewDTO dto = new InterviewDTO();
        dto.id = interview.getId();
        dto.candidateId = interview.getCandidate() != null ? interview.getCandidate().getId() : null;
        dto.candidateName = interview.getCandidate() != null ? interview.getCandidate().getFirstName() + " " + interview.getCandidate().getLastName() : null;
        dto.position = interview.getPosition();
        dto.status = interview.getStatus();
        dto.notes = interview.getNotes();
        dto.duration = interview.getDuration();
        dto.scheduledDate = interview.getScheduledDate();
        dto.overallScore = interview.getOverallScore();
        dto.interviewerId = interview.getInterviewer() != null ? interview.getInterviewer().getId() : null;
        dto.interviewerName = interview.getInterviewer() != null ? interview.getInterviewer().getFirstName() + " " + interview.getInterviewer().getLastName() : null;
        dto.createdAt = interview.getCreatedAt();
        dto.updatedAt = interview.getUpdatedAt();
        return dto;
    }

    private Interview fromDTO(InterviewDTO dto) {
        Interview interview = new Interview();
        interview.setId(dto.id);
        if (dto.candidateId != null) {
            Candidate candidate = candidateRepository.findById(dto.candidateId).orElse(null);
            interview.setCandidate(candidate);
        }
        interview.setPosition(dto.position);
        if (dto.status != null) interview.setStatus(dto.status);
        interview.setNotes(dto.notes);
        interview.setDuration(dto.duration);
        interview.setScheduledDate(dto.scheduledDate);
        interview.setOverallScore(dto.overallScore);
        if (dto.interviewerId != null) {
            User interviewer = userRepository.findById(dto.interviewerId).orElse(null);
            interview.setInterviewer(interviewer);
        }
        return interview;
    }

    @Transactional
    public InterviewDTO createInterview(InterviewDTO dto) {
        Interview interview = fromDTO(dto);
        Interview saved = interviewRepository.save(interview);
        return toDTO(saved);
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return null;
        String username = auth.getName();
        return userRepository.findByUsername(username).orElse(null);
    }

    private boolean isCurrentUserAdminOrHR() {
        User user = getCurrentUser();
        return user != null && (user.getRole() == User.UserRole.ADMIN || user.getRole() == User.UserRole.HR_MANAGER);
    }

    private boolean isCurrentUserAssignedToInterview(Interview interview) {
        User user = getCurrentUser();
        return user != null && interview.getInterviewer() != null && interview.getInterviewer().getId().equals(user.getId());
    }

    @Transactional
    public Page<InterviewDTO> getAllInterviews(Pageable pageable) {
        User user = getCurrentUser();
        if (user == null) return Page.empty();
        if (isCurrentUserAdminOrHR()) {
            return interviewRepository.findAll(pageable).map(this::toDTO);
        } else if (user.getRole() == User.UserRole.INTERVIEWER) {
            // Use repository method to get only assigned interviews
            return interviewRepository.findByInterviewer_Id(user.getId(), pageable).map(this::toDTO);
        } else {
            return Page.empty();
        }
    }

    @Transactional
    public Optional<InterviewDTO> getInterviewById(Long id) {
        Optional<Interview> interviewOpt = interviewRepository.findById(id);
        if (interviewOpt.isEmpty()) return Optional.empty();
        Interview interview = interviewOpt.get();
        if (isCurrentUserAdminOrHR() || isCurrentUserAssignedToInterview(interview)) {
            return Optional.of(toDTO(interview));
        } else {
            return Optional.empty();
        }
    }

    @Transactional
    public Optional<InterviewDTO> updateInterview(Long id, InterviewDTO dto) {
        Optional<Interview> existingInterviewOpt = interviewRepository.findById(id);
        if (existingInterviewOpt.isEmpty()) return Optional.empty();
        
        Interview existingInterview = existingInterviewOpt.get();
        
        // Only update fields that are provided in the DTO
        if (dto.candidateId != null) {
            Candidate candidate = candidateRepository.findById(dto.candidateId).orElse(null);
            existingInterview.setCandidate(candidate);
        }
        if (dto.position != null) {
            existingInterview.setPosition(dto.position);
        }
        if (dto.status != null) {
            existingInterview.setStatus(dto.status);
        }
        if (dto.notes != null) {
            existingInterview.setNotes(dto.notes);
        }
        if (dto.duration != null) {
            existingInterview.setDuration(dto.duration);
        }
        if (dto.scheduledDate != null) {
            existingInterview.setScheduledDate(dto.scheduledDate);
        }
        if (dto.overallScore != null) {
            existingInterview.setOverallScore(dto.overallScore);
        }
        if (dto.interviewerId != null) {
            User interviewer = userRepository.findById(dto.interviewerId).orElse(null);
            existingInterview.setInterviewer(interviewer);
        }
        
        Interview saved = interviewRepository.save(existingInterview);
        return Optional.of(toDTO(saved));
    }

    @Transactional
    public boolean deleteInterview(Long id) {
        if (!interviewRepository.existsById(id)) return false;
        interviewRepository.deleteById(id);
        return true;
    }

    @Transactional
    public List<InterviewDTO> getInterviewsByCandidate(Long candidateId) {
        User user = getCurrentUser();
        List<Interview> interviews = interviewRepository.findByCandidateId(candidateId);
        if (isCurrentUserAdminOrHR()) {
            return interviews.stream().map(this::toDTO).collect(Collectors.toList());
        } else if (user != null && user.getRole() == User.UserRole.INTERVIEWER) {
            return interviews.stream().filter(i -> i.getInterviewer() != null && i.getInterviewer().getId().equals(user.getId())).map(this::toDTO).collect(Collectors.toList());
        } else {
            return List.of();
        }
    }

    @Transactional
    public List<InterviewDTO> getInterviewsByStatus(String status) {
        return interviewRepository.findByStatus(status).stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional
    public List<InterviewDTO> getInterviewsByPosition(String position) {
        return interviewRepository.findByPositionContainingIgnoreCase(position).stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional
    public List<InterviewDTO> getInterviewsByInterviewer(Long interviewerId) {
        User user = getCurrentUser();
        if (user == null) return List.of();
        
        // Only allow if user is admin/HR or the requested interviewer
        if (isCurrentUserAdminOrHR() || (user.getRole() == User.UserRole.INTERVIEWER && user.getId().equals(interviewerId))) {
            return interviewRepository.findByInterviewer_Id(interviewerId).stream().map(this::toDTO).collect(Collectors.toList());
        } else {
            return List.of();
        }
    }
} 