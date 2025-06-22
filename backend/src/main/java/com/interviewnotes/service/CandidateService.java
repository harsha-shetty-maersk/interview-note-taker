package com.interviewnotes.service;

import com.interviewnotes.dto.CandidateDTO;
import com.interviewnotes.model.Candidate;
import com.interviewnotes.repository.CandidateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service class for Candidate-related business operations.
 */
@Service
@Transactional
public class CandidateService {

    private final CandidateRepository candidateRepository;

    @Autowired
    public CandidateService(CandidateRepository candidateRepository) {
        this.candidateRepository = candidateRepository;
    }

    /**
     * Create a new candidate.
     */
    public CandidateDTO createCandidate(CandidateDTO candidateDTO) {
        // Check if candidate with email already exists
        Optional<Candidate> existingCandidate = candidateRepository.findByEmail(candidateDTO.getEmail());
        if (existingCandidate.isPresent()) {
            throw new IllegalArgumentException("Candidate with email " + candidateDTO.getEmail() + " already exists");
        }

        Candidate candidate = new Candidate();
        candidate.setFirstName(candidateDTO.getFirstName());
        candidate.setLastName(candidateDTO.getLastName());
        candidate.setEmail(candidateDTO.getEmail());
        candidate.setPhone(candidateDTO.getPhone());
        candidate.setPosition(candidateDTO.getPosition());
        candidate.setExperience(candidateDTO.getExperience());
        candidate.setResumeUrl(candidateDTO.getResumeUrl());
        candidate.setSource(candidateDTO.getSource());
        candidate.setNotes(candidateDTO.getNotes());
        candidate.setStatus(candidateDTO.getStatus() != null ? candidateDTO.getStatus() : "ACTIVE");

        Candidate savedCandidate = candidateRepository.save(candidate);
        return convertToDTO(savedCandidate);
    }

    /**
     * Get candidate by ID.
     */
    @Transactional(readOnly = true)
    public Optional<CandidateDTO> getCandidateById(Long id) {
        return candidateRepository.findById(id)
                .map(this::convertToDTO);
    }

    /**
     * Get candidate by email.
     */
    @Transactional(readOnly = true)
    public Optional<CandidateDTO> getCandidateByEmail(String email) {
        return candidateRepository.findByEmail(email)
                .map(this::convertToDTO);
    }

    /**
     * Get all candidates with pagination.
     */
    @Transactional(readOnly = true)
    public Page<CandidateDTO> getAllCandidates(Pageable pageable) {
        return candidateRepository.findAll(pageable)
                .map(this::convertToDTO);
    }

    /**
     * Get candidates by status.
     */
    @Transactional(readOnly = true)
    public List<CandidateDTO> getCandidatesByStatus(String status) {
        return candidateRepository.findByStatus(status)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get candidates by status with pagination.
     */
    @Transactional(readOnly = true)
    public Page<CandidateDTO> getCandidatesByStatus(String status, Pageable pageable) {
        return candidateRepository.findByStatus(status, pageable)
                .map(this::convertToDTO);
    }

    /**
     * Search candidates by term.
     */
    @Transactional(readOnly = true)
    public Page<CandidateDTO> searchCandidates(String searchTerm, Pageable pageable) {
        return candidateRepository.searchCandidates(searchTerm, pageable)
                .map(this::convertToDTO);
    }

    /**
     * Get candidates by position.
     */
    @Transactional(readOnly = true)
    public List<CandidateDTO> getCandidatesByPosition(String position) {
        return candidateRepository.findByPositionContainingIgnoreCase(position)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get candidates by experience level.
     */
    @Transactional(readOnly = true)
    public List<CandidateDTO> getCandidatesByExperience(Integer minExperience) {
        return candidateRepository.findByExperienceGreaterThanEqual(minExperience)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Update candidate.
     */
    public Optional<CandidateDTO> updateCandidate(Long id, CandidateDTO candidateDTO) {
        return candidateRepository.findById(id)
                .map(candidate -> {
                    // Check if email is being changed and if it already exists
                    if (!candidate.getEmail().equals(candidateDTO.getEmail())) {
                        Optional<Candidate> existingCandidate = candidateRepository.findByEmail(candidateDTO.getEmail());
                        if (existingCandidate.isPresent()) {
                            throw new IllegalArgumentException("Candidate with email " + candidateDTO.getEmail() + " already exists");
                        }
                    }

                    candidate.setFirstName(candidateDTO.getFirstName());
                    candidate.setLastName(candidateDTO.getLastName());
                    candidate.setEmail(candidateDTO.getEmail());
                    candidate.setPhone(candidateDTO.getPhone());
                    candidate.setPosition(candidateDTO.getPosition());
                    candidate.setExperience(candidateDTO.getExperience());
                    candidate.setResumeUrl(candidateDTO.getResumeUrl());
                    candidate.setSource(candidateDTO.getSource());
                    candidate.setNotes(candidateDTO.getNotes());
                    if (candidateDTO.getStatus() != null) {
                        candidate.setStatus(candidateDTO.getStatus());
                    }

                    Candidate savedCandidate = candidateRepository.save(candidate);
                    return convertToDTO(savedCandidate);
                });
    }

    /**
     * Delete candidate.
     */
    public boolean deleteCandidate(Long id) {
        if (candidateRepository.existsById(id)) {
            candidateRepository.deleteById(id);
            return true;
        }
        return false;
    }

    /**
     * Get candidates with interviews.
     */
    @Transactional(readOnly = true)
    public List<CandidateDTO> getCandidatesWithInterviews() {
        return candidateRepository.findCandidatesWithInterviews()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get candidates without interviews.
     */
    @Transactional(readOnly = true)
    public List<CandidateDTO> getCandidatesWithoutInterviews() {
        return candidateRepository.findCandidatesWithoutInterviews()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get candidates with completed interviews.
     */
    @Transactional(readOnly = true)
    public List<CandidateDTO> getCandidatesWithCompletedInterviews() {
        return candidateRepository.findCandidatesWithCompletedInterviews()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get candidates by multiple criteria.
     */
    @Transactional(readOnly = true)
    public Page<CandidateDTO> getCandidatesByCriteria(String status, String position, Integer experience, Pageable pageable) {
        return candidateRepository.findCandidatesByCriteria(status, position, experience, pageable)
                .map(this::convertToDTO);
    }

    /**
     * Get candidate statistics.
     */
    @Transactional(readOnly = true)
    public CandidateStatistics getCandidateStatistics() {
        long totalCandidates = candidateRepository.count();
        long activeCandidates = candidateRepository.countByStatus("ACTIVE");
        long candidatesWithInterviews = candidateRepository.findCandidatesWithInterviews().size();
        long candidatesWithoutInterviews = candidateRepository.findCandidatesWithoutInterviews().size();

        return new CandidateStatistics(totalCandidates, activeCandidates, candidatesWithInterviews, candidatesWithoutInterviews);
    }

    /**
     * Convert Candidate entity to DTO.
     */
    private CandidateDTO convertToDTO(Candidate candidate) {
        CandidateDTO dto = new CandidateDTO();
        dto.setId(candidate.getId());
        dto.setFirstName(candidate.getFirstName());
        dto.setLastName(candidate.getLastName());
        dto.setEmail(candidate.getEmail());
        dto.setPhone(candidate.getPhone());
        dto.setPosition(candidate.getPosition());
        dto.setExperience(candidate.getExperience());
        dto.setResumeUrl(candidate.getResumeUrl());
        dto.setSource(candidate.getSource());
        dto.setNotes(candidate.getNotes());
        dto.setStatus(candidate.getStatus());
        dto.setCreatedAt(candidate.getCreatedAt());
        dto.setUpdatedAt(candidate.getUpdatedAt());

        // Convert interviews to summary DTOs
        if (candidate.getInterviews() != null && !candidate.getInterviews().isEmpty()) {
            List<CandidateDTO.InterviewSummaryDTO> interviewSummaries = candidate.getInterviews()
                    .stream()
                    .map(interview -> new CandidateDTO.InterviewSummaryDTO(
                            interview.getId(),
                            interview.getPosition(),
                            interview.getStatus(),
                            interview.getScheduledDate(),
                            interview.getDuration()
                    ))
                    .collect(Collectors.toList());
            dto.setInterviews(interviewSummaries);
        }

        return dto;
    }

    /**
     * Statistics class for candidate data.
     */
    public static class CandidateStatistics {
        private final long totalCandidates;
        private final long activeCandidates;
        private final long candidatesWithInterviews;
        private final long candidatesWithoutInterviews;

        public CandidateStatistics(long totalCandidates, long activeCandidates, 
                                 long candidatesWithInterviews, long candidatesWithoutInterviews) {
            this.totalCandidates = totalCandidates;
            this.activeCandidates = activeCandidates;
            this.candidatesWithInterviews = candidatesWithInterviews;
            this.candidatesWithoutInterviews = candidatesWithoutInterviews;
        }

        // Getters
        public long getTotalCandidates() {
            return totalCandidates;
        }

        public long getActiveCandidates() {
            return activeCandidates;
        }

        public long getCandidatesWithInterviews() {
            return candidatesWithInterviews;
        }

        public long getCandidatesWithoutInterviews() {
            return candidatesWithoutInterviews;
        }
    }
} 