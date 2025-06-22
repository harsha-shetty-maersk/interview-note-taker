package com.interviewnotes.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.interviewnotes.service.InterviewService;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class InterviewControllerTest {

    @Mock
    private InterviewService interviewService;

    @InjectMocks
    private InterviewController interviewController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private InterviewController.InterviewDTO testInterviewDTO;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(interviewController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        testInterviewDTO = new InterviewController.InterviewDTO();
        testInterviewDTO.id = 1L;
        testInterviewDTO.candidateId = 1L;
        testInterviewDTO.candidateName = "John Doe";
        testInterviewDTO.position = "Software Engineer";
        testInterviewDTO.status = "SCHEDULED";
        testInterviewDTO.duration = 60;
        testInterviewDTO.scheduledDate = LocalDateTime.now().plusDays(1);
        testInterviewDTO.overallScore = new BigDecimal("8.5");
        testInterviewDTO.notes = "Great candidate";
        testInterviewDTO.interviewerId = 1L;
        testInterviewDTO.interviewerName = "Jane Smith";
        testInterviewDTO.createdAt = LocalDateTime.now();
        testInterviewDTO.updatedAt = LocalDateTime.now();
    }

    @Test
    void getAllInterviews_Success() throws Exception {
        Page<InterviewController.InterviewDTO> interviewPage = new PageImpl<>(Arrays.asList(testInterviewDTO), PageRequest.of(0, 20), 1);
        when(interviewService.getAllInterviews(any(Pageable.class))).thenReturn(interviewPage);

        mockMvc.perform(get("/api/interviews")
                .param("page", "0")
                .param("size", "20")
                .param("sortBy", "createdAt")
                .param("sortDir", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(interviewService).getAllInterviews(any(Pageable.class));
    }

    @Test
    void getAllInterviews_WithDefaultParams_Success() throws Exception {
        Page<InterviewController.InterviewDTO> interviewPage = new PageImpl<>(Arrays.asList(testInterviewDTO), PageRequest.of(0, 20), 1);
        when(interviewService.getAllInterviews(any(Pageable.class))).thenReturn(interviewPage);

        mockMvc.perform(get("/api/interviews"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1));

        verify(interviewService).getAllInterviews(any(Pageable.class));
    }

    @Test
    void getAllInterviews_WithAscendingSort_Success() throws Exception {
        Page<InterviewController.InterviewDTO> interviewPage = new PageImpl<>(Arrays.asList(testInterviewDTO), PageRequest.of(0, 20), 1);
        when(interviewService.getAllInterviews(any(Pageable.class))).thenReturn(interviewPage);

        mockMvc.perform(get("/api/interviews")
                .param("sortDir", "asc"))
                .andExpect(status().isOk());

        verify(interviewService).getAllInterviews(any(Pageable.class));
    }

    @Test
    void getInterviewById_Success() throws Exception {
        when(interviewService.getInterviewById(1L)).thenReturn(Optional.of(testInterviewDTO));

        mockMvc.perform(get("/api/interviews/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.candidateName").value("John Doe"))
                .andExpect(jsonPath("$.position").value("Software Engineer"));

        verify(interviewService).getInterviewById(1L);
    }

    @Test
    void getInterviewById_NotFound_ReturnsNotFound() throws Exception {
        when(interviewService.getInterviewById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/interviews/1"))
                .andExpect(status().isNotFound());

        verify(interviewService).getInterviewById(1L);
    }

    @Test
    void createInterview_Success() throws Exception {
        when(interviewService.createInterview(any(InterviewController.InterviewDTO.class))).thenReturn(testInterviewDTO);

        mockMvc.perform(post("/api/interviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testInterviewDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.candidateName").value("John Doe"));

        verify(interviewService).createInterview(any(InterviewController.InterviewDTO.class));
    }

    @Test
    void createInterview_WrongContentType_ReturnsUnsupportedMediaType() throws Exception {
        mockMvc.perform(post("/api/interviews")
                .contentType(MediaType.TEXT_PLAIN)
                .content("invalid content"))
                .andExpect(status().isUnsupportedMediaType());

        verify(interviewService, never()).createInterview(any(InterviewController.InterviewDTO.class));
    }

    @Test
    void updateInterview_Success() throws Exception {
        when(interviewService.updateInterview(eq(1L), any(InterviewController.InterviewDTO.class))).thenReturn(Optional.of(testInterviewDTO));

        mockMvc.perform(put("/api/interviews/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testInterviewDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.candidateName").value("John Doe"));

        verify(interviewService).updateInterview(eq(1L), any(InterviewController.InterviewDTO.class));
    }

    @Test
    void updateInterview_NotFound_ReturnsNotFound() throws Exception {
        when(interviewService.updateInterview(eq(1L), any(InterviewController.InterviewDTO.class))).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/interviews/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testInterviewDTO)))
                .andExpect(status().isNotFound());

        verify(interviewService).updateInterview(eq(1L), any(InterviewController.InterviewDTO.class));
    }

    @Test
    void updateInterview_WrongContentType_ReturnsUnsupportedMediaType() throws Exception {
        mockMvc.perform(put("/api/interviews/1")
                .contentType(MediaType.TEXT_PLAIN)
                .content("invalid content"))
                .andExpect(status().isUnsupportedMediaType());

        verify(interviewService, never()).updateInterview(anyLong(), any(InterviewController.InterviewDTO.class));
    }

    @Test
    void deleteInterview_Success() throws Exception {
        when(interviewService.deleteInterview(1L)).thenReturn(true);

        mockMvc.perform(delete("/api/interviews/1"))
                .andExpect(status().isNoContent());

        verify(interviewService).deleteInterview(1L);
    }

    @Test
    void deleteInterview_NotFound_ReturnsNotFound() throws Exception {
        when(interviewService.deleteInterview(1L)).thenReturn(false);

        mockMvc.perform(delete("/api/interviews/1"))
                .andExpect(status().isNotFound());

        verify(interviewService).deleteInterview(1L);
    }

    @Test
    void getInterviewsByCandidate_Success() throws Exception {
        List<InterviewController.InterviewDTO> interviews = Arrays.asList(testInterviewDTO);
        when(interviewService.getInterviewsByCandidate(1L)).thenReturn(interviews);

        mockMvc.perform(get("/api/interviews/candidate/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].candidateId").value(1));

        verify(interviewService).getInterviewsByCandidate(1L);
    }

    @Test
    void getInterviewsByStatus_Success() throws Exception {
        List<InterviewController.InterviewDTO> interviews = Arrays.asList(testInterviewDTO);
        when(interviewService.getInterviewsByStatus("SCHEDULED")).thenReturn(interviews);

        mockMvc.perform(get("/api/interviews/status/SCHEDULED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].status").value("SCHEDULED"));

        verify(interviewService).getInterviewsByStatus("SCHEDULED");
    }

    @Test
    void getInterviewsByPosition_Success() throws Exception {
        List<InterviewController.InterviewDTO> interviews = Arrays.asList(testInterviewDTO);
        when(interviewService.getInterviewsByPosition("Software")).thenReturn(interviews);

        mockMvc.perform(get("/api/interviews/position/Software"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].position").value("Software Engineer"));

        verify(interviewService).getInterviewsByPosition("Software");
    }

    @Test
    void getInterviewsByInterviewer_Success() throws Exception {
        List<InterviewController.InterviewDTO> interviews = Arrays.asList(testInterviewDTO);
        when(interviewService.getInterviewsByInterviewer(1L)).thenReturn(interviews);

        mockMvc.perform(get("/api/interviews/interviewer/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].interviewerId").value(1));

        verify(interviewService).getInterviewsByInterviewer(1L);
    }

    @Test
    void getInterviewById_NullCandidate() throws Exception {
        InterviewController.InterviewDTO dto = new InterviewController.InterviewDTO();
        dto.id = 2L;
        dto.candidateId = null;
        dto.candidateName = null;
        dto.position = "QA Engineer";
        dto.status = "SCHEDULED";
        dto.duration = 45;
        dto.scheduledDate = LocalDateTime.now().plusDays(2);
        dto.overallScore = new BigDecimal("7.0");
        dto.notes = "No candidate";
        dto.interviewerId = 2L;
        dto.interviewerName = "Jane Smith";
        dto.createdAt = LocalDateTime.now();
        dto.updatedAt = LocalDateTime.now();
        when(interviewService.getInterviewById(2L)).thenReturn(Optional.of(dto));

        mockMvc.perform(get("/api/interviews/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.candidateId").doesNotExist())
                .andExpect(jsonPath("$.candidateName").doesNotExist());
    }

    @Test
    void getInterviewById_NullInterviewer() throws Exception {
        InterviewController.InterviewDTO dto = new InterviewController.InterviewDTO();
        dto.id = 6L;
        dto.candidateId = 6L;
        dto.candidateName = "Test Candidate";
        dto.position = "QA";
        dto.status = "SCHEDULED";
        dto.duration = 30;
        dto.scheduledDate = LocalDateTime.now();
        dto.overallScore = new BigDecimal("8.0");
        dto.notes = "No interviewer";
        dto.interviewerId = null;
        dto.interviewerName = null;
        dto.createdAt = LocalDateTime.now();
        dto.updatedAt = LocalDateTime.now();
        when(interviewService.getInterviewById(6L)).thenReturn(Optional.of(dto));

        mockMvc.perform(get("/api/interviews/6"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(6));
    }

    @Test
    void getInterviewById_NullNames() throws Exception {
        InterviewController.InterviewDTO dto = new InterviewController.InterviewDTO();
        dto.id = 7L;
        dto.candidateId = 7L;
        dto.candidateName = null;
        dto.position = null;
        dto.status = null;
        dto.duration = null;
        dto.scheduledDate = null;
        dto.overallScore = null;
        dto.notes = null;
        dto.interviewerId = 7L;
        dto.interviewerName = null;
        dto.createdAt = null;
        dto.updatedAt = null;
        when(interviewService.getInterviewById(7L)).thenReturn(Optional.of(dto));

        mockMvc.perform(get("/api/interviews/7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7));
    }

    @Test
    void getInterviewById_NullFields() throws Exception {
        InterviewController.InterviewDTO dto = new InterviewController.InterviewDTO();
        dto.id = 4L;
        // All other fields null
        when(interviewService.getInterviewById(4L)).thenReturn(Optional.of(dto));

        mockMvc.perform(get("/api/interviews/4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(4));
    }

    @Test
    void getInterviewById_EmptyFields() throws Exception {
        InterviewController.InterviewDTO dto = new InterviewController.InterviewDTO();
        dto.id = 5L;
        dto.candidateId = 5L;
        dto.candidateName = "";
        dto.position = "";
        dto.status = "";
        dto.duration = 0;
        dto.scheduledDate = null;
        dto.overallScore = null;
        dto.notes = "";
        dto.interviewerId = 5L;
        dto.interviewerName = "";
        dto.createdAt = null;
        dto.updatedAt = null;
        when(interviewService.getInterviewById(5L)).thenReturn(Optional.of(dto));

        mockMvc.perform(get("/api/interviews/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5));
    }

    @Test
    void createInterview_WithInvalidJson() throws Exception {
        mockMvc.perform(post("/api/interviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ invalid json }"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateInterview_WithInvalidJson() throws Exception {
        mockMvc.perform(put("/api/interviews/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ invalid json }"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllInterviews_WithInvalidSortDirection() throws Exception {
        Page<InterviewController.InterviewDTO> interviewPage = new PageImpl<>(Arrays.asList(testInterviewDTO), PageRequest.of(0, 20), 1);
        when(interviewService.getAllInterviews(any(Pageable.class))).thenReturn(interviewPage);

        mockMvc.perform(get("/api/interviews")
                .param("sortDir", "invalid"))
                .andExpect(status().isOk());

        verify(interviewService).getAllInterviews(any(Pageable.class));
    }

    @Test
    void toDTO_AllBranches() throws Exception {
        InterviewController controller = new InterviewController();
        java.lang.reflect.Method toDTOMethod = InterviewController.class.getDeclaredMethod("toDTO", com.interviewnotes.model.Interview.class);
        toDTOMethod.setAccessible(true);
        com.interviewnotes.model.Interview interview = new com.interviewnotes.model.Interview();
        // All fields null
        InterviewController.InterviewDTO dto = (InterviewController.InterviewDTO) toDTOMethod.invoke(controller, interview);
        assertThat(dto).isNotNull();
        // Candidate null
        assertThat(dto.candidateId).isNull();
        assertThat(dto.candidateName).isNull();
        // Interviewer null
        assertThat(dto.interviewerId).isNull();
        assertThat(dto.interviewerName).isNull();
        // Set candidate with null names
        com.interviewnotes.model.Candidate candidate = new com.interviewnotes.model.Candidate();
        interview.setCandidate(candidate);
        dto = (InterviewController.InterviewDTO) toDTOMethod.invoke(controller, interview);
        assertThat(dto.candidateId).isNull();
        assertThat(dto.candidateName).isEqualTo("null null");
        // Set interviewer with null names
        com.interviewnotes.model.User interviewer = new com.interviewnotes.model.User();
        interview.setInterviewer(interviewer);
        dto = (InterviewController.InterviewDTO) toDTOMethod.invoke(controller, interview);
        assertThat(dto.interviewerId).isNull();
        assertThat(dto.interviewerName).isEqualTo("null null");
        // Set candidate and interviewer with IDs and names
        candidate.setId(1L);
        candidate.setFirstName("A");
        candidate.setLastName("B");
        interviewer.setId(2L);
        interviewer.setFirstName("X");
        interviewer.setLastName("Y");
        dto = (InterviewController.InterviewDTO) toDTOMethod.invoke(controller, interview);
        assertThat(dto.candidateId).isEqualTo(1L);
        assertThat(dto.candidateName).isEqualTo("A B");
        assertThat(dto.interviewerId).isEqualTo(2L);
        assertThat(dto.interviewerName).isEqualTo("X Y");
    }
} 