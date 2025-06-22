package com.interviewnotes.controller;

import com.interviewnotes.model.Interview;
import com.interviewnotes.model.Candidate;
import com.interviewnotes.model.User;
import com.interviewnotes.repository.InterviewRepository;
import com.interviewnotes.repository.CandidateRepository;
import com.interviewnotes.repository.UserRepository;
import com.interviewnotes.service.InterviewService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * REST controller for Interview-related operations.
 */
@RestController
@RequestMapping("/api/interviews")
@Tag(name = "Interviews", description = "Interview management APIs")
public class InterviewController {
    @Autowired
    private InterviewService interviewService;
    @Autowired
    private InterviewRepository interviewRepository;
    @Autowired
    private CandidateRepository candidateRepository;
    @Autowired
    private UserRepository userRepository;

    // DTO for Interview
    public static class InterviewDTO {
        public Long id;
        public Long candidateId;
        public String candidateName;
        public String position;
        public String status;
        public Integer duration;
        public java.time.LocalDateTime scheduledDate;
        public java.math.BigDecimal overallScore;
        public String notes;
        public Long interviewerId;
        public String interviewerName;
        public java.time.LocalDateTime createdAt;
        public java.time.LocalDateTime updatedAt;
    }

    private InterviewDTO toDTO(Interview interview) {
        InterviewDTO dto = new InterviewDTO();
        dto.id = interview.getId();
        dto.candidateId = interview.getCandidate() != null ? interview.getCandidate().getId() : null;
        dto.candidateName = interview.getCandidate() != null ? interview.getCandidate().getFirstName() + " " + interview.getCandidate().getLastName() : null;
        dto.position = interview.getPosition();
        dto.status = interview.getStatus();
        dto.duration = interview.getDuration();
        dto.scheduledDate = interview.getScheduledDate();
        dto.overallScore = interview.getOverallScore();
        dto.notes = interview.getNotes();
        dto.interviewerId = interview.getInterviewer() != null ? interview.getInterviewer().getId() : null;
        dto.interviewerName = interview.getInterviewer() != null ? interview.getInterviewer().getFirstName() + " " + interview.getInterviewer().getLastName() : null;
        dto.createdAt = interview.getCreatedAt();
        dto.updatedAt = interview.getUpdatedAt();
        return dto;
    }

    /**
     * Get all interviews (paginated).
     */
    @GetMapping
    @Operation(summary = "Get all interviews", description = "Retrieves all interviews with pagination support")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Interviews retrieved successfully",
                    content = @Content(schema = @Schema(implementation = Page.class)))
    })
    public ResponseEntity<Page<InterviewDTO>> getAllInterviews(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<InterviewDTO> result = interviewService.getAllInterviews(pageable);
        return ResponseEntity.ok(result);
    }

    /**
     * Get interview by ID.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get interview by ID", description = "Retrieves an interview by its unique ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Interview found",
                    content = @Content(schema = @Schema(implementation = InterviewDTO.class))),
            @ApiResponse(responseCode = "404", description = "Interview not found")
    })
    public ResponseEntity<InterviewDTO> getInterviewById(@Parameter(description = "Interview ID", required = true) @PathVariable Long id) {
        return interviewService.getInterviewById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Create a new interview.
     */
    @PostMapping
    @Operation(summary = "Create a new interview", description = "Creates a new interview with the provided information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Interview created successfully",
                    content = @Content(schema = @Schema(implementation = InterviewDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    public ResponseEntity<InterviewDTO> createInterview(@Valid @RequestBody InterviewDTO dto) {
        InterviewDTO created = interviewService.createInterview(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Update an interview.
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update interview", description = "Updates an existing interview's information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Interview updated successfully",
                    content = @Content(schema = @Schema(implementation = InterviewDTO.class))),
            @ApiResponse(responseCode = "404", description = "Interview not found")
    })
    public ResponseEntity<InterviewDTO> updateInterview(@Parameter(description = "Interview ID", required = true) @PathVariable Long id,
                                                       @RequestBody InterviewDTO dto) {
        return interviewService.updateInterview(id, dto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Delete an interview.
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete interview", description = "Deletes an interview by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Interview deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Interview not found")
    })
    public ResponseEntity<Void> deleteInterview(@Parameter(description = "Interview ID", required = true) @PathVariable Long id) {
        boolean deleted = interviewService.deleteInterview(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    /**
     * Get interviews by candidate.
     */
    @GetMapping("/candidate/{candidateId}")
    @Operation(summary = "Get interviews by candidate", description = "Retrieves interviews for a specific candidate")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Interviews retrieved successfully",
                    content = @Content(schema = @Schema(implementation = List.class)))
    })
    public ResponseEntity<List<InterviewDTO>> getInterviewsByCandidate(@Parameter(description = "Candidate ID", required = true) @PathVariable Long candidateId) {
        List<InterviewDTO> result = interviewService.getInterviewsByCandidate(candidateId);
        return ResponseEntity.ok(result);
    }

    /**
     * Get interviews by status.
     */
    @GetMapping("/status/{status}")
    @Operation(summary = "Get interviews by status", description = "Retrieves interviews filtered by status")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Interviews retrieved successfully",
                    content = @Content(schema = @Schema(implementation = List.class)))
    })
    public ResponseEntity<List<InterviewDTO>> getInterviewsByStatus(@Parameter(description = "Interview status", required = true) @PathVariable String status) {
        List<InterviewDTO> result = interviewService.getInterviewsByStatus(status);
        return ResponseEntity.ok(result);
    }

    /**
     * Get interviews by position.
     */
    @GetMapping("/position/{position}")
    @Operation(summary = "Get interviews by position", description = "Retrieves interviews filtered by position")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Interviews retrieved successfully",
                    content = @Content(schema = @Schema(implementation = List.class)))
    })
    public ResponseEntity<List<InterviewDTO>> getInterviewsByPosition(@Parameter(description = "Position title", required = true) @PathVariable String position) {
        List<InterviewDTO> result = interviewService.getInterviewsByPosition(position);
        return ResponseEntity.ok(result);
    }

    /**
     * Get interviews by interviewer.
     */
    @GetMapping("/interviewer/{interviewerId}")
    @Operation(summary = "Get interviews by interviewer", description = "Retrieves interviews assigned to a specific interviewer")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Interviews retrieved successfully",
                    content = @Content(schema = @Schema(implementation = List.class)))
    })
    public ResponseEntity<List<InterviewDTO>> getInterviewsByInterviewer(@Parameter(description = "Interviewer ID", required = true) @PathVariable Long interviewerId) {
        List<InterviewDTO> result = interviewService.getInterviewsByInterviewer(interviewerId);
        return ResponseEntity.ok(result);
    }
} 