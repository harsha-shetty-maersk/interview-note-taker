package com.interviewnotes.controller;

import com.interviewnotes.dto.CandidateDTO;
import com.interviewnotes.service.CandidateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * REST controller for Candidate-related operations.
 */
@RestController
@RequestMapping("/api/candidates")
@Tag(name = "Candidates", description = "Candidate management APIs")
public class CandidateController {

    private final CandidateService candidateService;

    @Autowired
    public CandidateController(CandidateService candidateService) {
        this.candidateService = candidateService;
    }

    /**
     * Create a new candidate.
     */
    @PostMapping
    @Operation(summary = "Create a new candidate", description = "Creates a new candidate with the provided information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Candidate created successfully",
                    content = @Content(schema = @Schema(implementation = CandidateDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "409", description = "Candidate with email already exists")
    })
    public ResponseEntity<CandidateDTO> createCandidate(
            @Parameter(description = "Candidate information", required = true)
            @Valid @RequestBody CandidateDTO candidateDTO) {
        try {
            CandidateDTO createdCandidate = candidateService.createCandidate(candidateDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdCandidate);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    /**
     * Get candidate by ID.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get candidate by ID", description = "Retrieves a candidate by their unique ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Candidate found",
                    content = @Content(schema = @Schema(implementation = CandidateDTO.class))),
            @ApiResponse(responseCode = "404", description = "Candidate not found")
    })
    public ResponseEntity<CandidateDTO> getCandidateById(
            @Parameter(description = "Candidate ID", required = true)
            @PathVariable Long id) {
        Optional<CandidateDTO> candidate = candidateService.getCandidateById(id);
        return candidate.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get candidate by email.
     */
    @GetMapping("/email/{email}")
    @Operation(summary = "Get candidate by email", description = "Retrieves a candidate by their email address")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Candidate found",
                    content = @Content(schema = @Schema(implementation = CandidateDTO.class))),
            @ApiResponse(responseCode = "404", description = "Candidate not found")
    })
    public ResponseEntity<CandidateDTO> getCandidateByEmail(
            @Parameter(description = "Candidate email", required = true)
            @PathVariable String email) {
        Optional<CandidateDTO> candidate = candidateService.getCandidateByEmail(email);
        return candidate.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get all candidates with pagination.
     */
    @GetMapping
    @Operation(summary = "Get all candidates", description = "Retrieves all candidates with pagination support")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Candidates retrieved successfully",
                    content = @Content(schema = @Schema(implementation = Page.class)))
    })
    public ResponseEntity<Page<CandidateDTO>> getAllCandidates(
            @Parameter(description = "Page number (0-based)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field")
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction")
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("asc") ? 
                Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<CandidateDTO> candidates = candidateService.getAllCandidates(pageable);
        return ResponseEntity.ok(candidates);
    }

    /**
     * Search candidates.
     */
    @GetMapping("/search")
    @Operation(summary = "Search candidates", description = "Search candidates by name, email, or position")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Search results retrieved successfully",
                    content = @Content(schema = @Schema(implementation = Page.class)))
    })
    public ResponseEntity<Page<CandidateDTO>> searchCandidates(
            @Parameter(description = "Search term", required = true)
            @RequestParam String q,
            @Parameter(description = "Page number (0-based)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<CandidateDTO> candidates = candidateService.searchCandidates(q, pageable);
        return ResponseEntity.ok(candidates);
    }

    /**
     * Get candidates by status.
     */
    @GetMapping("/status/{status}")
    @Operation(summary = "Get candidates by status", description = "Retrieves candidates filtered by status")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Candidates retrieved successfully",
                    content = @Content(schema = @Schema(implementation = List.class)))
    })
    public ResponseEntity<List<CandidateDTO>> getCandidatesByStatus(
            @Parameter(description = "Candidate status", required = true)
            @PathVariable String status) {
        List<CandidateDTO> candidates = candidateService.getCandidatesByStatus(status);
        return ResponseEntity.ok(candidates);
    }

    /**
     * Get candidates by position.
     */
    @GetMapping("/position/{position}")
    @Operation(summary = "Get candidates by position", description = "Retrieves candidates filtered by position")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Candidates retrieved successfully",
                    content = @Content(schema = @Schema(implementation = List.class)))
    })
    public ResponseEntity<List<CandidateDTO>> getCandidatesByPosition(
            @Parameter(description = "Position title", required = true)
            @PathVariable String position) {
        List<CandidateDTO> candidates = candidateService.getCandidatesByPosition(position);
        return ResponseEntity.ok(candidates);
    }

    /**
     * Get candidates by experience level.
     */
    @GetMapping("/experience/{minExperience}")
    @Operation(summary = "Get candidates by experience", description = "Retrieves candidates with minimum experience level")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Candidates retrieved successfully",
                    content = @Content(schema = @Schema(implementation = List.class)))
    })
    public ResponseEntity<List<CandidateDTO>> getCandidatesByExperience(
            @Parameter(description = "Minimum years of experience", required = true)
            @PathVariable Integer minExperience) {
        List<CandidateDTO> candidates = candidateService.getCandidatesByExperience(minExperience);
        return ResponseEntity.ok(candidates);
    }

    /**
     * Update candidate.
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update candidate", description = "Updates an existing candidate's information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Candidate updated successfully",
                    content = @Content(schema = @Schema(implementation = CandidateDTO.class))),
            @ApiResponse(responseCode = "404", description = "Candidate not found"),
            @ApiResponse(responseCode = "409", description = "Email already exists")
    })
    public ResponseEntity<CandidateDTO> updateCandidate(
            @Parameter(description = "Candidate ID", required = true)
            @PathVariable Long id,
            @Parameter(description = "Updated candidate information", required = true)
            @Valid @RequestBody CandidateDTO candidateDTO) {
        try {
            Optional<CandidateDTO> updatedCandidate = candidateService.updateCandidate(id, candidateDTO);
            return updatedCandidate.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    /**
     * Delete candidate.
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete candidate", description = "Deletes a candidate by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Candidate deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Candidate not found")
    })
    public ResponseEntity<Void> deleteCandidate(
            @Parameter(description = "Candidate ID", required = true)
            @PathVariable Long id) {
        boolean deleted = candidateService.deleteCandidate(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    /**
     * Get candidates with interviews.
     */
    @GetMapping("/with-interviews")
    @Operation(summary = "Get candidates with interviews", description = "Retrieves candidates who have interviews scheduled")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Candidates retrieved successfully",
                    content = @Content(schema = @Schema(implementation = List.class)))
    })
    public ResponseEntity<List<CandidateDTO>> getCandidatesWithInterviews() {
        List<CandidateDTO> candidates = candidateService.getCandidatesWithInterviews();
        return ResponseEntity.ok(candidates);
    }

    /**
     * Get candidates without interviews.
     */
    @GetMapping("/without-interviews")
    @Operation(summary = "Get candidates without interviews", description = "Retrieves candidates who have no interviews scheduled")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Candidates retrieved successfully",
                    content = @Content(schema = @Schema(implementation = List.class)))
    })
    public ResponseEntity<List<CandidateDTO>> getCandidatesWithoutInterviews() {
        List<CandidateDTO> candidates = candidateService.getCandidatesWithoutInterviews();
        return ResponseEntity.ok(candidates);
    }

    /**
     * Get candidate statistics.
     */
    @GetMapping("/statistics")
    @Operation(summary = "Get candidate statistics", description = "Retrieves statistics about candidates")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully",
                    content = @Content(schema = @Schema(implementation = CandidateService.CandidateStatistics.class)))
    })
    public ResponseEntity<CandidateService.CandidateStatistics> getCandidateStatistics() {
        CandidateService.CandidateStatistics statistics = candidateService.getCandidateStatistics();
        return ResponseEntity.ok(statistics);
    }
} 