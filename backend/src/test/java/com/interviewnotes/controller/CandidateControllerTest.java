package com.interviewnotes.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.interviewnotes.dto.CandidateDTO;
import com.interviewnotes.service.CandidateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class CandidateControllerTest {

    @Mock
    private CandidateService candidateService;

    @InjectMocks
    private CandidateController candidateController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private CandidateDTO testCandidateDTO;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(candidateController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        testCandidateDTO = new CandidateDTO();
        testCandidateDTO.setId(1L);
        testCandidateDTO.setFirstName("John");
        testCandidateDTO.setLastName("Doe");
        testCandidateDTO.setEmail("john.doe@example.com");
        testCandidateDTO.setPhone("1234567890");
        testCandidateDTO.setPosition("Software Engineer");
        testCandidateDTO.setExperience(5);
        testCandidateDTO.setResumeUrl("http://example.com/resume.pdf");
        testCandidateDTO.setSource("LinkedIn");
        testCandidateDTO.setNotes("Great candidate");
        testCandidateDTO.setStatus("ACTIVE");
        testCandidateDTO.setCreatedAt(LocalDateTime.now());
        testCandidateDTO.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void createCandidate_Success() throws Exception {
        when(candidateService.createCandidate(any(CandidateDTO.class))).thenReturn(testCandidateDTO);

        mockMvc.perform(post("/api/candidates")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testCandidateDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"));

        verify(candidateService).createCandidate(any(CandidateDTO.class));
    }

    @Test
    void createCandidate_EmailAlreadyExists_ReturnsConflict() throws Exception {
        when(candidateService.createCandidate(any(CandidateDTO.class)))
                .thenThrow(new IllegalArgumentException("Candidate with email already exists"));

        mockMvc.perform(post("/api/candidates")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testCandidateDTO)))
                .andExpect(status().isConflict());

        verify(candidateService).createCandidate(any(CandidateDTO.class));
    }

    @Test
    void createCandidate_InvalidData_ReturnsBadRequest() throws Exception {
        testCandidateDTO.setEmail(""); // Invalid email

        mockMvc.perform(post("/api/candidates")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testCandidateDTO)))
                .andExpect(status().isBadRequest());

        verify(candidateService, never()).createCandidate(any(CandidateDTO.class));
    }

    @Test
    void getCandidateById_Success() throws Exception {
        when(candidateService.getCandidateById(1L)).thenReturn(Optional.of(testCandidateDTO));

        mockMvc.perform(get("/api/candidates/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"));

        verify(candidateService).getCandidateById(1L);
    }

    @Test
    void getCandidateById_NotFound_ReturnsNotFound() throws Exception {
        when(candidateService.getCandidateById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/candidates/1"))
                .andExpect(status().isNotFound());

        verify(candidateService).getCandidateById(1L);
    }

    @Test
    void getCandidateByEmail_Success() throws Exception {
        when(candidateService.getCandidateByEmail("john.doe@example.com")).thenReturn(Optional.of(testCandidateDTO));

        mockMvc.perform(get("/api/candidates/email/john.doe@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"));

        verify(candidateService).getCandidateByEmail("john.doe@example.com");
    }

    @Test
    void getCandidateByEmail_NotFound_ReturnsNotFound() throws Exception {
        when(candidateService.getCandidateByEmail("john.doe@example.com")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/candidates/email/john.doe@example.com"))
                .andExpect(status().isNotFound());

        verify(candidateService).getCandidateByEmail("john.doe@example.com");
    }

    @Test
    void getAllCandidates_Success() throws Exception {
        Page<CandidateDTO> candidatePage = new PageImpl<>(Arrays.asList(testCandidateDTO), PageRequest.of(0, 20), 1);
        when(candidateService.getAllCandidates(any(Pageable.class))).thenReturn(candidatePage);

        mockMvc.perform(get("/api/candidates")
                .param("page", "0")
                .param("size", "20")
                .param("sortBy", "createdAt")
                .param("sortDir", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(candidateService).getAllCandidates(any(Pageable.class));
    }

    @Test
    void getAllCandidates_WithDefaultParams_Success() throws Exception {
        Page<CandidateDTO> candidatePage = new PageImpl<>(Arrays.asList(testCandidateDTO), PageRequest.of(0, 20), 1);
        when(candidateService.getAllCandidates(any(Pageable.class))).thenReturn(candidatePage);

        mockMvc.perform(get("/api/candidates"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1));

        verify(candidateService).getAllCandidates(any(Pageable.class));
    }

    @Test
    void searchCandidates_Success() throws Exception {
        Page<CandidateDTO> candidatePage = new PageImpl<>(Arrays.asList(testCandidateDTO), PageRequest.of(0, 20), 1);
        when(candidateService.searchCandidates(eq("John"), any(Pageable.class))).thenReturn(candidatePage);

        mockMvc.perform(get("/api/candidates/search")
                .param("q", "John")
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1));

        verify(candidateService).searchCandidates(eq("John"), any(Pageable.class));
    }

    @Test
    void getCandidatesByStatus_Success() throws Exception {
        List<CandidateDTO> candidates = Arrays.asList(testCandidateDTO);
        when(candidateService.getCandidatesByStatus("ACTIVE")).thenReturn(candidates);

        mockMvc.perform(get("/api/candidates/status/ACTIVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"));

        verify(candidateService).getCandidatesByStatus("ACTIVE");
    }

    @Test
    void getCandidatesByPosition_Success() throws Exception {
        List<CandidateDTO> candidates = Arrays.asList(testCandidateDTO);
        when(candidateService.getCandidatesByPosition("Software")).thenReturn(candidates);

        mockMvc.perform(get("/api/candidates/position/Software"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].position").value("Software Engineer"));

        verify(candidateService).getCandidatesByPosition("Software");
    }

    @Test
    void getCandidatesByExperience_Success() throws Exception {
        List<CandidateDTO> candidates = Arrays.asList(testCandidateDTO);
        when(candidateService.getCandidatesByExperience(3)).thenReturn(candidates);

        mockMvc.perform(get("/api/candidates/experience/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].experience").value(5));

        verify(candidateService).getCandidatesByExperience(3);
    }

    @Test
    void updateCandidate_Success() throws Exception {
        when(candidateService.updateCandidate(eq(1L), any(CandidateDTO.class))).thenReturn(Optional.of(testCandidateDTO));

        mockMvc.perform(put("/api/candidates/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testCandidateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("John"));

        verify(candidateService).updateCandidate(eq(1L), any(CandidateDTO.class));
    }

    @Test
    void updateCandidate_NotFound_ReturnsNotFound() throws Exception {
        when(candidateService.updateCandidate(eq(1L), any(CandidateDTO.class))).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/candidates/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testCandidateDTO)))
                .andExpect(status().isNotFound());

        verify(candidateService).updateCandidate(eq(1L), any(CandidateDTO.class));
    }

    @Test
    void updateCandidate_EmailAlreadyExists_ReturnsConflict() throws Exception {
        when(candidateService.updateCandidate(eq(1L), any(CandidateDTO.class)))
                .thenThrow(new IllegalArgumentException("Candidate with email already exists"));

        mockMvc.perform(put("/api/candidates/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testCandidateDTO)))
                .andExpect(status().isConflict());

        verify(candidateService).updateCandidate(eq(1L), any(CandidateDTO.class));
    }

    @Test
    void updateCandidate_InvalidData_ReturnsBadRequest() throws Exception {
        testCandidateDTO.setEmail(""); // Invalid email

        mockMvc.perform(put("/api/candidates/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testCandidateDTO)))
                .andExpect(status().isBadRequest());

        verify(candidateService, never()).updateCandidate(anyLong(), any(CandidateDTO.class));
    }

    @Test
    void deleteCandidate_Success() throws Exception {
        when(candidateService.deleteCandidate(1L)).thenReturn(true);

        mockMvc.perform(delete("/api/candidates/1"))
                .andExpect(status().isNoContent());

        verify(candidateService).deleteCandidate(1L);
    }

    @Test
    void deleteCandidate_NotFound_ReturnsNotFound() throws Exception {
        when(candidateService.deleteCandidate(1L)).thenReturn(false);

        mockMvc.perform(delete("/api/candidates/1"))
                .andExpect(status().isNotFound());

        verify(candidateService).deleteCandidate(1L);
    }

    @Test
    void getCandidatesWithInterviews_Success() throws Exception {
        List<CandidateDTO> candidates = Arrays.asList(testCandidateDTO);
        when(candidateService.getCandidatesWithInterviews()).thenReturn(candidates);

        mockMvc.perform(get("/api/candidates/with-interviews"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));

        verify(candidateService).getCandidatesWithInterviews();
    }

    @Test
    void getCandidatesWithoutInterviews_Success() throws Exception {
        List<CandidateDTO> candidates = Arrays.asList(testCandidateDTO);
        when(candidateService.getCandidatesWithoutInterviews()).thenReturn(candidates);

        mockMvc.perform(get("/api/candidates/without-interviews"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));

        verify(candidateService).getCandidatesWithoutInterviews();
    }

    @Test
    void getCandidateStatistics_Success() throws Exception {
        CandidateService.CandidateStatistics statistics = new CandidateService.CandidateStatistics(10L, 8L, 6L, 4L);
        when(candidateService.getCandidateStatistics()).thenReturn(statistics);

        mockMvc.perform(get("/api/candidates/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCandidates").value(10))
                .andExpect(jsonPath("$.activeCandidates").value(8))
                .andExpect(jsonPath("$.candidatesWithInterviews").value(6))
                .andExpect(jsonPath("$.candidatesWithoutInterviews").value(4));

        verify(candidateService).getCandidateStatistics();
    }

    @Test
    void createCandidate_WrongContentType_ReturnsUnsupportedMediaType() throws Exception {
        mockMvc.perform(post("/api/candidates")
                .contentType(MediaType.TEXT_PLAIN)
                .content("invalid content"))
                .andExpect(status().isUnsupportedMediaType());

        verify(candidateService, never()).createCandidate(any(CandidateDTO.class));
    }

    @Test
    void updateCandidate_WrongContentType_ReturnsUnsupportedMediaType() throws Exception {
        mockMvc.perform(put("/api/candidates/1")
                .contentType(MediaType.TEXT_PLAIN)
                .content("invalid content"))
                .andExpect(status().isUnsupportedMediaType());

        verify(candidateService, never()).updateCandidate(anyLong(), any(CandidateDTO.class));
    }

    @Test
    void getAllCandidates_InvalidSortDirection() throws Exception {
        Page<CandidateDTO> page = new PageImpl<>(List.of(testCandidateDTO), PageRequest.of(0, 20), 1);
        when(candidateService.getAllCandidates(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/candidates")
                .param("sortDir", "invalid"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void getAllCandidates_EmptyResult() throws Exception {
        Page<CandidateDTO> page = new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);
        when(candidateService.getAllCandidates(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/candidates"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content").isEmpty());
    }
} 