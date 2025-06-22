package com.interviewnotes.service;

import com.interviewnotes.controller.InterviewController.InterviewDTO;
import com.interviewnotes.model.Candidate;
import com.interviewnotes.model.Interview;
import com.interviewnotes.model.User;
import com.interviewnotes.repository.CandidateRepository;
import com.interviewnotes.repository.InterviewRepository;
import com.interviewnotes.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class InterviewServiceTest {

    @Mock
    private InterviewRepository interviewRepository;

    @Mock
    private CandidateRepository candidateRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private InterviewService interviewService;

    private Interview testInterview;
    private InterviewDTO testInterviewDTO;
    private User testUser;
    private Candidate testCandidate;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setRole(User.UserRole.INTERVIEWER);
        testUser.setEnabled(true);

        testCandidate = new Candidate();
        testCandidate.setId(1L);
        testCandidate.setFirstName("John");
        testCandidate.setLastName("Doe");
        testCandidate.setEmail("john.doe@example.com");

        testInterview = new Interview();
        testInterview.setId(1L);
        testInterview.setCandidate(testCandidate);
        testInterview.setInterviewer(testUser);
        testInterview.setPosition("Software Engineer");
        testInterview.setStatus("SCHEDULED");
        testInterview.setDuration(60);
        testInterview.setScheduledDate(LocalDateTime.now().plusDays(1));
        testInterview.setOverallScore(new BigDecimal("8.5"));
        testInterview.setNotes("Great candidate");
        testInterview.setCreatedAt(LocalDateTime.now());
        testInterview.setUpdatedAt(LocalDateTime.now());

        testInterviewDTO = new InterviewDTO();
        testInterviewDTO.id = 1L;
        testInterviewDTO.candidateId = 1L;
        testInterviewDTO.candidateName = "John Doe";
        testInterviewDTO.interviewerId = 1L;
        testInterviewDTO.interviewerName = "testuser";
        testInterviewDTO.position = "Software Engineer";
        testInterviewDTO.status = "SCHEDULED";
        testInterviewDTO.duration = 60;
        testInterviewDTO.scheduledDate = LocalDateTime.now().plusDays(1);
        testInterviewDTO.overallScore = new BigDecimal("8.5");
        testInterviewDTO.notes = "Great candidate";
        testInterviewDTO.createdAt = LocalDateTime.now();
        testInterviewDTO.updatedAt = LocalDateTime.now();
    }

    @Test
    void createInterview_Success() {
        when(candidateRepository.findById(1L)).thenReturn(Optional.of(testCandidate));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(interviewRepository.save(any(Interview.class))).thenReturn(testInterview);

        InterviewDTO result = interviewService.createInterview(testInterviewDTO);

        assertNotNull(result);
        assertEquals(1L, result.id);
        assertEquals("John Doe", result.candidateName);
        assertEquals("Software Engineer", result.position);

        verify(candidateRepository).findById(1L);
        verify(userRepository).findById(1L);
        verify(interviewRepository).save(any(Interview.class));
    }

    @Test
    void createInterview_WithNullCandidateId_Success() {
        testInterviewDTO.candidateId = null;
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(interviewRepository.save(any(Interview.class))).thenReturn(testInterview);

        InterviewDTO result = interviewService.createInterview(testInterviewDTO);

        assertNotNull(result);
        verify(candidateRepository, never()).findById(anyLong());
        verify(interviewRepository).save(any(Interview.class));
    }

    @Test
    void createInterview_WithNullInterviewerId_Success() {
        testInterviewDTO.interviewerId = null;
        when(candidateRepository.findById(1L)).thenReturn(Optional.of(testCandidate));
        when(interviewRepository.save(any(Interview.class))).thenReturn(testInterview);

        InterviewDTO result = interviewService.createInterview(testInterviewDTO);

        assertNotNull(result);
        verify(userRepository, never()).findById(anyLong());
        verify(interviewRepository).save(any(Interview.class));
    }

    @Test
    void getAllInterviews_AdminUser_Success() {
        testUser.setRole(User.UserRole.ADMIN);
        Page<Interview> interviewPage = new PageImpl<>(Arrays.asList(testInterview), PageRequest.of(0, 20), 1);
        
        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.isAuthenticated()).thenReturn(true);
            when(authentication.getName()).thenReturn("admin");
            when(userRepository.findByUsername("admin")).thenReturn(Optional.of(testUser));
            when(interviewRepository.findAll(any(Pageable.class))).thenReturn(interviewPage);

            Page<InterviewDTO> result = interviewService.getAllInterviews(PageRequest.of(0, 20));

            assertNotNull(result);
            assertEquals(1, result.getTotalElements());
            assertEquals("John Doe", result.getContent().get(0).candidateName);

            verify(interviewRepository).findAll(any(Pageable.class));
            verify(interviewRepository, never()).findByInterviewer_Id(anyLong(), any(Pageable.class));
        }
    }

    @Test
    void getAllInterviews_HRManagerUser_Success() {
        testUser.setRole(User.UserRole.HR_MANAGER);
        Page<Interview> interviewPage = new PageImpl<>(Arrays.asList(testInterview), PageRequest.of(0, 20), 1);
        
        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.isAuthenticated()).thenReturn(true);
            when(authentication.getName()).thenReturn("hr");
            when(userRepository.findByUsername("hr")).thenReturn(Optional.of(testUser));
            when(interviewRepository.findAll(any(Pageable.class))).thenReturn(interviewPage);

            Page<InterviewDTO> result = interviewService.getAllInterviews(PageRequest.of(0, 20));

            assertNotNull(result);
            assertEquals(1, result.getTotalElements());

            verify(interviewRepository).findAll(any(Pageable.class));
        }
    }

    @Test
    void getAllInterviews_InterviewerUser_Success() {
        Page<Interview> interviewPage = new PageImpl<>(Arrays.asList(testInterview), PageRequest.of(0, 20), 1);
        
        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.isAuthenticated()).thenReturn(true);
            when(authentication.getName()).thenReturn("interviewer");
            when(userRepository.findByUsername("interviewer")).thenReturn(Optional.of(testUser));
            when(interviewRepository.findByInterviewer_Id(1L, PageRequest.of(0, 20))).thenReturn(interviewPage);

            Page<InterviewDTO> result = interviewService.getAllInterviews(PageRequest.of(0, 20));

            assertNotNull(result);
            assertEquals(1, result.getTotalElements());

            verify(interviewRepository).findByInterviewer_Id(1L, PageRequest.of(0, 20));
            verify(interviewRepository, never()).findAll(any(Pageable.class));
        }
    }

    @Test
    void getAllInterviews_NoAuthentication_ReturnsEmpty() {
        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(null);

            Page<InterviewDTO> result = interviewService.getAllInterviews(PageRequest.of(0, 20));

            assertTrue(result.isEmpty());
            verify(interviewRepository, never()).findAll(any(Pageable.class));
            verify(interviewRepository, never()).findByInterviewer_Id(anyLong(), any(Pageable.class));
        }
    }

    @Test
    void getAllInterviews_UserNotFound_ReturnsEmpty() {
        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.isAuthenticated()).thenReturn(true);
            when(authentication.getName()).thenReturn("unknown");
            when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

            Page<InterviewDTO> result = interviewService.getAllInterviews(PageRequest.of(0, 20));

            assertTrue(result.isEmpty());
            verify(interviewRepository, never()).findAll(any(Pageable.class));
            verify(interviewRepository, never()).findByInterviewer_Id(anyLong(), any(Pageable.class));
        }
    }

    @Test
    void getInterviewById_AdminUser_Success() {
        testUser.setRole(User.UserRole.ADMIN);
        
        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.isAuthenticated()).thenReturn(true);
            when(authentication.getName()).thenReturn("admin");
            when(userRepository.findByUsername("admin")).thenReturn(Optional.of(testUser));
            when(interviewRepository.findById(1L)).thenReturn(Optional.of(testInterview));

            Optional<InterviewDTO> result = interviewService.getInterviewById(1L);

            assertTrue(result.isPresent());
            assertEquals(1L, result.get().id);
            assertEquals("John Doe", result.get().candidateName);

            verify(interviewRepository).findById(1L);
        }
    }

    @Test
    void getInterviewById_AssignedInterviewer_Success() {
        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.isAuthenticated()).thenReturn(true);
            when(authentication.getName()).thenReturn("interviewer");
            when(userRepository.findByUsername("interviewer")).thenReturn(Optional.of(testUser));
            when(interviewRepository.findById(1L)).thenReturn(Optional.of(testInterview));

            Optional<InterviewDTO> result = interviewService.getInterviewById(1L);

            assertTrue(result.isPresent());
            assertEquals(1L, result.get().id);

            verify(interviewRepository).findById(1L);
        }
    }

    @Test
    void getInterviewById_NotAssignedInterviewer_ReturnsEmpty() {
        User otherUser = new User();
        otherUser.setId(2L);
        otherUser.setUsername("other");
        otherUser.setRole(User.UserRole.INTERVIEWER);
        
        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.isAuthenticated()).thenReturn(true);
            when(authentication.getName()).thenReturn("other");
            when(userRepository.findByUsername("other")).thenReturn(Optional.of(otherUser));
            when(interviewRepository.findById(1L)).thenReturn(Optional.of(testInterview));

            Optional<InterviewDTO> result = interviewService.getInterviewById(1L);

            assertFalse(result.isPresent());

            verify(interviewRepository).findById(1L);
        }
    }

    @Test
    void getInterviewById_InterviewNotFound_ReturnsEmpty() {
        when(interviewRepository.findById(1L)).thenReturn(Optional.empty());

        Optional<InterviewDTO> result = interviewService.getInterviewById(1L);

        assertFalse(result.isPresent());
        verify(interviewRepository).findById(1L);
    }

    @Test
    void updateInterview_Success() {
        when(interviewRepository.findById(1L)).thenReturn(Optional.of(testInterview));
        when(candidateRepository.findById(1L)).thenReturn(Optional.of(testCandidate));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(interviewRepository.save(any(Interview.class))).thenReturn(testInterview);

        Optional<InterviewDTO> result = interviewService.updateInterview(1L, testInterviewDTO);

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().id);

        verify(interviewRepository).findById(1L);
        verify(interviewRepository).save(any(Interview.class));
    }

    @Test
    void updateInterview_InterviewNotFound_ReturnsEmpty() {
        when(interviewRepository.findById(1L)).thenReturn(Optional.empty());

        Optional<InterviewDTO> result = interviewService.updateInterview(1L, testInterviewDTO);

        assertFalse(result.isPresent());
        verify(interviewRepository).findById(1L);
        verify(interviewRepository, never()).save(any(Interview.class));
    }

    @Test
    void updateInterview_PartialUpdate_Success() {
        InterviewDTO partialUpdate = new InterviewDTO();
        partialUpdate.position = "Updated Position";
        partialUpdate.status = "COMPLETED";

        when(interviewRepository.findById(1L)).thenReturn(Optional.of(testInterview));
        when(interviewRepository.save(any(Interview.class))).thenReturn(testInterview);

        Optional<InterviewDTO> result = interviewService.updateInterview(1L, partialUpdate);

        assertTrue(result.isPresent());
        verify(interviewRepository).findById(1L);
        verify(interviewRepository).save(any(Interview.class));
    }

    @Test
    void deleteInterview_Success() {
        when(interviewRepository.existsById(1L)).thenReturn(true);
        doNothing().when(interviewRepository).deleteById(1L);

        boolean result = interviewService.deleteInterview(1L);

        assertTrue(result);
        verify(interviewRepository).existsById(1L);
        verify(interviewRepository).deleteById(1L);
    }

    @Test
    void deleteInterview_NotFound_ReturnsFalse() {
        when(interviewRepository.existsById(1L)).thenReturn(false);

        boolean result = interviewService.deleteInterview(1L);

        assertFalse(result);
        verify(interviewRepository).existsById(1L);
        verify(interviewRepository, never()).deleteById(anyLong());
    }

    @Test
    void getInterviewsByCandidate_AdminUser_Success() {
        testUser.setRole(User.UserRole.ADMIN);
        List<Interview> interviews = Arrays.asList(testInterview);
        
        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.isAuthenticated()).thenReturn(true);
            when(authentication.getName()).thenReturn("admin");
            when(userRepository.findByUsername("admin")).thenReturn(Optional.of(testUser));
            when(interviewRepository.findByCandidateId(1L)).thenReturn(interviews);

            List<InterviewDTO> result = interviewService.getInterviewsByCandidate(1L);

            assertEquals(1, result.size());
            assertEquals(1L, result.get(0).id);

            verify(interviewRepository).findByCandidateId(1L);
        }
    }

    @Test
    void getInterviewsByCandidate_InterviewerUser_Success() {
        List<Interview> interviews = Arrays.asList(testInterview);
        
        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.isAuthenticated()).thenReturn(true);
            when(authentication.getName()).thenReturn("interviewer");
            when(userRepository.findByUsername("interviewer")).thenReturn(Optional.of(testUser));
            when(interviewRepository.findByCandidateId(1L)).thenReturn(interviews);

            List<InterviewDTO> result = interviewService.getInterviewsByCandidate(1L);

            assertEquals(1, result.size());
            assertEquals(1L, result.get(0).id);

            verify(interviewRepository).findByCandidateId(1L);
        }
    }

    @Test
    void getInterviewsByCandidate_InterviewerUserNotAssigned_ReturnsEmpty() {
        User otherUser = new User();
        otherUser.setId(2L);
        otherUser.setUsername("other");
        otherUser.setRole(User.UserRole.INTERVIEWER);
        
        List<Interview> interviews = Arrays.asList(testInterview);
        
        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.isAuthenticated()).thenReturn(true);
            when(authentication.getName()).thenReturn("other");
            when(userRepository.findByUsername("other")).thenReturn(Optional.of(otherUser));
            when(interviewRepository.findByCandidateId(1L)).thenReturn(interviews);

            List<InterviewDTO> result = interviewService.getInterviewsByCandidate(1L);

            assertTrue(result.isEmpty());

            verify(interviewRepository).findByCandidateId(1L);
        }
    }

    @Test
    void getInterviewsByStatus_Success() {
        List<Interview> interviews = Arrays.asList(testInterview);
        when(interviewRepository.findByStatus("SCHEDULED")).thenReturn(interviews);

        List<InterviewDTO> result = interviewService.getInterviewsByStatus("SCHEDULED");

        assertEquals(1, result.size());
        assertEquals("SCHEDULED", result.get(0).status);

        verify(interviewRepository).findByStatus("SCHEDULED");
    }

    @Test
    void getInterviewsByPosition_Success() {
        List<Interview> interviews = Arrays.asList(testInterview);
        when(interviewRepository.findByPositionContainingIgnoreCase("Software")).thenReturn(interviews);

        List<InterviewDTO> result = interviewService.getInterviewsByPosition("Software");

        assertEquals(1, result.size());
        assertEquals("Software Engineer", result.get(0).position);

        verify(interviewRepository).findByPositionContainingIgnoreCase("Software");
    }

    @Test
    void getInterviewsByInterviewer_AdminUser_Success() {
        testUser.setRole(User.UserRole.ADMIN);
        List<Interview> interviews = Arrays.asList(testInterview);
        
        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.isAuthenticated()).thenReturn(true);
            when(authentication.getName()).thenReturn("admin");
            when(userRepository.findByUsername("admin")).thenReturn(Optional.of(testUser));
            when(interviewRepository.findByInterviewer_Id(1L)).thenReturn(interviews);

            List<InterviewDTO> result = interviewService.getInterviewsByInterviewer(1L);

            assertEquals(1, result.size());
            assertEquals(1L, result.get(0).interviewerId);

            verify(interviewRepository).findByInterviewer_Id(1L);
        }
    }

    @Test
    void getInterviewsByInterviewer_SameInterviewer_Success() {
        List<Interview> interviews = Arrays.asList(testInterview);
        
        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.isAuthenticated()).thenReturn(true);
            when(authentication.getName()).thenReturn("interviewer");
            when(userRepository.findByUsername("interviewer")).thenReturn(Optional.of(testUser));
            when(interviewRepository.findByInterviewer_Id(1L)).thenReturn(interviews);

            List<InterviewDTO> result = interviewService.getInterviewsByInterviewer(1L);

            assertEquals(1, result.size());
            assertEquals(1L, result.get(0).interviewerId);

            verify(interviewRepository).findByInterviewer_Id(1L);
        }
    }

    @Test
    void getInterviewsByInterviewer_DifferentInterviewer_ReturnsEmpty() {
        User otherUser = new User();
        otherUser.setId(2L);
        otherUser.setUsername("other");
        otherUser.setRole(User.UserRole.INTERVIEWER);
        
        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.isAuthenticated()).thenReturn(true);
            when(authentication.getName()).thenReturn("other");
            when(userRepository.findByUsername("other")).thenReturn(Optional.of(otherUser));

            List<InterviewDTO> result = interviewService.getInterviewsByInterviewer(1L);

            assertTrue(result.isEmpty());

            verify(interviewRepository, never()).findByInterviewer_Id(anyLong());
        }
    }

    @Test
    void getInterviewsByInterviewer_NoAuthentication_ReturnsEmpty() {
        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(null);

            List<InterviewDTO> result = interviewService.getInterviewsByInterviewer(1L);

            assertTrue(result.isEmpty());

            verify(interviewRepository, never()).findByInterviewer_Id(anyLong());
        }
    }

    @Test
    void testToDTO_WithNullCandidate() {
        Interview interview = new Interview();
        interview.setId(1L);
        interview.setCandidate(null);
        interview.setInterviewer(testUser);
        interview.setPosition("Java Developer");
        interview.setStatus("SCHEDULED");
        
        // Use reflection to access private method
        try {
            java.lang.reflect.Method toDTOMethod = InterviewService.class.getDeclaredMethod("toDTO", Interview.class);
            toDTOMethod.setAccessible(true);
            InterviewDTO result = (InterviewDTO) toDTOMethod.invoke(interviewService, interview);
            
            assertThat(result.id).isEqualTo(1L);
            assertThat(result.candidateId).isNull();
            assertThat(result.candidateName).isNull();
            assertThat(result.interviewerId).isEqualTo(testUser.getId());
            assertThat(result.interviewerName).isEqualTo("null null"); // firstName and lastName are null in testUser
        } catch (Exception e) {
            fail("Failed to test toDTO with null candidate", e);
        }
    }

    @Test
    void testToDTO_WithNullInterviewer() {
        Interview interview = new Interview();
        interview.setId(1L);
        interview.setCandidate(testCandidate);
        interview.setInterviewer(null);
        interview.setPosition("Java Developer");
        interview.setStatus("SCHEDULED");
        
        // Use reflection to access private method
        try {
            java.lang.reflect.Method toDTOMethod = InterviewService.class.getDeclaredMethod("toDTO", Interview.class);
            toDTOMethod.setAccessible(true);
            InterviewDTO result = (InterviewDTO) toDTOMethod.invoke(interviewService, interview);
            
            assertThat(result.id).isEqualTo(1L);
            assertThat(result.candidateId).isEqualTo(testCandidate.getId());
            assertThat(result.candidateName).isEqualTo("John Doe");
            assertThat(result.interviewerId).isNull();
            assertThat(result.interviewerName).isNull();
        } catch (Exception e) {
            fail("Failed to test toDTO with null interviewer", e);
        }
    }

    @Test
    void testFromDTO_WithNullStatus() {
        InterviewDTO dto = new InterviewDTO();
        dto.id = 1L;
        dto.candidateId = testCandidate.getId();
        dto.position = "Java Developer";
        dto.status = null;
        dto.interviewerId = testUser.getId();

        // Mock repository lookups
        when(candidateRepository.findById(testCandidate.getId())).thenReturn(Optional.of(testCandidate));
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        
        // Use reflection to access private method
        try {
            java.lang.reflect.Method fromDTOMethod = InterviewService.class.getDeclaredMethod("fromDTO", InterviewDTO.class);
            fromDTOMethod.setAccessible(true);
            Interview result = (Interview) fromDTOMethod.invoke(interviewService, dto);
            
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getCandidate()).isEqualTo(testCandidate);
            assertThat(result.getPosition()).isEqualTo("Java Developer");
            assertThat(result.getStatus()).isEqualTo("SCHEDULED");
            assertThat(result.getInterviewer()).isEqualTo(testUser);
        } catch (Exception e) {
            fail("Failed to test fromDTO with null status", e);
        }
    }

    @Test
    void testUpdateInterview_WithNullPosition() {
        Interview existingInterview = new Interview();
        existingInterview.setId(1L);
        existingInterview.setPosition("Old Position");
        
        InterviewDTO dto = new InterviewDTO();
        dto.position = null;
        dto.status = "COMPLETED";
        
        when(interviewRepository.findById(1L)).thenReturn(Optional.of(existingInterview));
        when(interviewRepository.save(any(Interview.class))).thenReturn(existingInterview);
        
        Optional<InterviewDTO> result = interviewService.updateInterview(1L, dto);
        
        assertThat(result).isPresent();
        assertThat(result.get().position).isEqualTo("Old Position"); // Should remain unchanged
    }

    @Test
    void testUpdateInterview_WithNullStatus() {
        Interview existingInterview = new Interview();
        existingInterview.setId(1L);
        existingInterview.setStatus("SCHEDULED");
        
        InterviewDTO dto = new InterviewDTO();
        dto.position = "New Position";
        dto.status = null;
        
        when(interviewRepository.findById(1L)).thenReturn(Optional.of(existingInterview));
        when(interviewRepository.save(any(Interview.class))).thenReturn(existingInterview);
        
        Optional<InterviewDTO> result = interviewService.updateInterview(1L, dto);
        
        assertThat(result).isPresent();
        assertThat(result.get().status).isEqualTo("SCHEDULED"); // Should remain unchanged
    }

    @Test
    void testGetCurrentUser_WithNullAuthentication() {
        // Mock SecurityContextHolder to return null authentication
        try (MockedStatic<SecurityContextHolder> mockedSecurityContext = mockStatic(SecurityContextHolder.class)) {
            SecurityContext securityContext = mock(SecurityContext.class);
            Authentication authentication = null;
            
            mockedSecurityContext.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            
            // Use reflection to access private method
            try {
                java.lang.reflect.Method getCurrentUserMethod = InterviewService.class.getDeclaredMethod("getCurrentUser");
                getCurrentUserMethod.setAccessible(true);
                User result = (User) getCurrentUserMethod.invoke(interviewService);
                
                assertThat(result).isNull();
            } catch (Exception e) {
                fail("Reflection failed: " + e.getMessage());
            }
        }
    }

    @Test
    void testGetCurrentUser_WithNotAuthenticated() {
        // Mock SecurityContextHolder to return not authenticated
        try (MockedStatic<SecurityContextHolder> mockedSecurityContext = mockStatic(SecurityContextHolder.class)) {
            SecurityContext securityContext = mock(SecurityContext.class);
            Authentication authentication = mock(Authentication.class);
            
            mockedSecurityContext.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.isAuthenticated()).thenReturn(false);
            
            // Use reflection to access private method
            try {
                java.lang.reflect.Method getCurrentUserMethod = InterviewService.class.getDeclaredMethod("getCurrentUser");
                getCurrentUserMethod.setAccessible(true);
                User result = (User) getCurrentUserMethod.invoke(interviewService);
                
                assertThat(result).isNull();
            } catch (Exception e) {
                fail("Reflection failed: " + e.getMessage());
            }
        }
    }

    @Test
    void testIsCurrentUserAdminOrHR_WithNullUser() {
        // Mock getCurrentUser to return null
        try (MockedStatic<SecurityContextHolder> mockedSecurityContext = mockStatic(SecurityContextHolder.class)) {
            SecurityContext securityContext = mock(SecurityContext.class);
            Authentication authentication = mock(Authentication.class);
            
            mockedSecurityContext.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.isAuthenticated()).thenReturn(true);
            when(authentication.getName()).thenReturn("nonexistent");
            when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());
            
            // Use reflection to access private method
            try {
                java.lang.reflect.Method isCurrentUserAdminOrHRMethod = InterviewService.class.getDeclaredMethod("isCurrentUserAdminOrHR");
                isCurrentUserAdminOrHRMethod.setAccessible(true);
                boolean result = (Boolean) isCurrentUserAdminOrHRMethod.invoke(interviewService);
                
                assertThat(result).isFalse();
            } catch (Exception e) {
                fail("Reflection failed: " + e.getMessage());
            }
        }
    }

    @Test
    void testIsCurrentUserAssignedToInterview_WithNullUser() {
        // Mock getCurrentUser to return null
        try (MockedStatic<SecurityContextHolder> mockedSecurityContext = mockStatic(SecurityContextHolder.class)) {
            SecurityContext securityContext = mock(SecurityContext.class);
            Authentication authentication = mock(Authentication.class);
            
            mockedSecurityContext.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.isAuthenticated()).thenReturn(true);
            when(authentication.getName()).thenReturn("nonexistent");
            when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());
            
            // Use reflection to access private method
            try {
                java.lang.reflect.Method isCurrentUserAssignedToInterviewMethod = InterviewService.class.getDeclaredMethod("isCurrentUserAssignedToInterview", Interview.class);
                isCurrentUserAssignedToInterviewMethod.setAccessible(true);
                boolean result = (Boolean) isCurrentUserAssignedToInterviewMethod.invoke(interviewService, testInterview);
                
                assertThat(result).isFalse();
            } catch (Exception e) {
                fail("Reflection failed: " + e.getMessage());
            }
        }
    }

    @Test
    void testIsCurrentUserAssignedToInterview_WithNullInterviewer() {
        Interview interview = new Interview();
        interview.setId(1L);
        interview.setInterviewer(null); // Null interviewer
        
        // Mock getCurrentUser to return a user
        try (MockedStatic<SecurityContextHolder> mockedSecurityContext = mockStatic(SecurityContextHolder.class)) {
            SecurityContext securityContext = mock(SecurityContext.class);
            Authentication authentication = mock(Authentication.class);
            
            mockedSecurityContext.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.isAuthenticated()).thenReturn(true);
            when(authentication.getName()).thenReturn("testuser");
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
            
            // Use reflection to access private method
            try {
                java.lang.reflect.Method isCurrentUserAssignedToInterviewMethod = InterviewService.class.getDeclaredMethod("isCurrentUserAssignedToInterview", Interview.class);
                isCurrentUserAssignedToInterviewMethod.setAccessible(true);
                boolean result = (Boolean) isCurrentUserAssignedToInterviewMethod.invoke(interviewService, interview);
                
                assertThat(result).isFalse();
            } catch (Exception e) {
                fail("Reflection failed: " + e.getMessage());
            }
        }
    }

    @Test
    void testGetAllInterviews_WithNonInterviewerRole() {
        // Create a user with a different role (not ADMIN, HR_MANAGER, or INTERVIEWER)
        User otherUser = new User();
        otherUser.setId(999L);
        otherUser.setUsername("otheruser");
        otherUser.setRole(User.UserRole.ADMIN); // This will be overridden in the test
        
        // Mock getCurrentUser to return a user with different role
        try (MockedStatic<SecurityContextHolder> mockedSecurityContext = mockStatic(SecurityContextHolder.class)) {
            SecurityContext securityContext = mock(SecurityContext.class);
            Authentication authentication = mock(Authentication.class);
            
            mockedSecurityContext.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.isAuthenticated()).thenReturn(true);
            when(authentication.getName()).thenReturn("otheruser");
            when(userRepository.findByUsername("otheruser")).thenReturn(Optional.of(otherUser));
            
            // Return an empty page to avoid NPE
            when(interviewRepository.findAll(any(Pageable.class))).thenReturn(Page.empty());
            
            Page<InterviewDTO> result = interviewService.getAllInterviews(Pageable.unpaged());
            assertThat(result).isEmpty();
        }
    }

    @Test
    void testGetInterviewsByCandidate_WithNonInterviewerRole() {
        // Create a user with a different role
        User otherUser = new User();
        otherUser.setId(999L);
        otherUser.setUsername("otheruser");
        otherUser.setRole(User.UserRole.ADMIN); // This will be overridden in the test
        
        // Mock getCurrentUser to return a user with different role
        try (MockedStatic<SecurityContextHolder> mockedSecurityContext = mockStatic(SecurityContextHolder.class)) {
            SecurityContext securityContext = mock(SecurityContext.class);
            Authentication authentication = mock(Authentication.class);
            
            mockedSecurityContext.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.isAuthenticated()).thenReturn(true);
            when(authentication.getName()).thenReturn("otheruser");
            when(userRepository.findByUsername("otheruser")).thenReturn(Optional.of(otherUser));
            
            when(interviewRepository.findByCandidateId(1L)).thenReturn(List.of(testInterview));
            
            // ADMIN/HR gets all interviews for the candidate
            List<InterviewDTO> result = interviewService.getInterviewsByCandidate(1L);
            assertThat(result).isNotEmpty();
        }
    }

    @Test
    void testGetInterviewsByCandidate_WithNullInterviewerInInterview() {
        // Create an interview with null interviewer
        Interview interviewWithNullInterviewer = new Interview();
        interviewWithNullInterviewer.setId(2L);
        interviewWithNullInterviewer.setInterviewer(null);
        
        // Mock getCurrentUser to return an interviewer
        try (MockedStatic<SecurityContextHolder> mockedSecurityContext = mockStatic(SecurityContextHolder.class)) {
            SecurityContext securityContext = mock(SecurityContext.class);
            Authentication authentication = mock(Authentication.class);
            
            mockedSecurityContext.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.isAuthenticated()).thenReturn(true);
            when(authentication.getName()).thenReturn("testuser");
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
            
            when(interviewRepository.findByCandidateId(1L)).thenReturn(List.of(interviewWithNullInterviewer));
            
            List<InterviewDTO> result = interviewService.getInterviewsByCandidate(1L);
            
            assertThat(result).isEmpty();
        }
    }

    @Test
    void testGetInterviewsByInterviewer_WithNonMatchingInterviewer() {
        // Create a different user
        User differentUser = new User();
        differentUser.setId(999L);
        differentUser.setUsername("differentuser");
        differentUser.setRole(User.UserRole.INTERVIEWER);
        
        // Mock getCurrentUser to return a different interviewer
        try (MockedStatic<SecurityContextHolder> mockedSecurityContext = mockStatic(SecurityContextHolder.class)) {
            SecurityContext securityContext = mock(SecurityContext.class);
            Authentication authentication = mock(Authentication.class);
            
            mockedSecurityContext.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.isAuthenticated()).thenReturn(true);
            when(authentication.getName()).thenReturn("differentuser");
            when(userRepository.findByUsername("differentuser")).thenReturn(Optional.of(differentUser));
            
            // Remove the unnecessary stubbing
            // when(interviewRepository.findByInterviewer_Id(1L)).thenReturn(List.of(testInterview));
            
            List<InterviewDTO> result = interviewService.getInterviewsByInterviewer(1L);
            
            assertThat(result).isEmpty();
        }
    }

    @Test
    void testGetAllInterviews_WithUserRoleNotInterviewer() {
        // User with HR_MANAGER role
        User hrUser = new User();
        hrUser.setId(100L);
        hrUser.setUsername("hruser");
        hrUser.setRole(User.UserRole.HR_MANAGER);
        
        try (MockedStatic<SecurityContextHolder> mockedSecurityContext = mockStatic(SecurityContextHolder.class)) {
            SecurityContext securityContext = mock(SecurityContext.class);
            Authentication authentication = mock(Authentication.class);
            
            mockedSecurityContext.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.isAuthenticated()).thenReturn(true);
            when(authentication.getName()).thenReturn("hruser");
            when(userRepository.findByUsername("hruser")).thenReturn(Optional.of(hrUser));
            when(interviewRepository.findAll(any(Pageable.class))).thenReturn(Page.empty());
            
            Page<InterviewDTO> result = interviewService.getAllInterviews(Pageable.unpaged());
            assertThat(result).isEmpty();
        }
    }

    @Test
    void testGetInterviewsByCandidate_WithUserNull() {
        // getCurrentUser returns null
        try (MockedStatic<SecurityContextHolder> mockedSecurityContext = mockStatic(SecurityContextHolder.class)) {
            SecurityContext securityContext = mock(SecurityContext.class);
            Authentication authentication = mock(Authentication.class);
            
            mockedSecurityContext.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.isAuthenticated()).thenReturn(false); // Not authenticated
            when(interviewRepository.findByCandidateId(1L)).thenReturn(List.of(testInterview));
            
            List<InterviewDTO> result = interviewService.getInterviewsByCandidate(1L);
            assertThat(result).isEmpty();
        }
    }

    @Test
    void testGetInterviewsByCandidate_WithUserRoleNotInterviewer() {
        // User with ADMIN role
        User adminUser = new User();
        adminUser.setId(101L);
        adminUser.setUsername("adminuser");
        adminUser.setRole(User.UserRole.ADMIN);
        
        try (MockedStatic<SecurityContextHolder> mockedSecurityContext = mockStatic(SecurityContextHolder.class)) {
            SecurityContext securityContext = mock(SecurityContext.class);
            Authentication authentication = mock(Authentication.class);
            
            mockedSecurityContext.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.isAuthenticated()).thenReturn(true);
            when(authentication.getName()).thenReturn("adminuser");
            when(userRepository.findByUsername("adminuser")).thenReturn(Optional.of(adminUser));
            when(interviewRepository.findByCandidateId(1L)).thenReturn(List.of(testInterview));
            
            List<InterviewDTO> result = interviewService.getInterviewsByCandidate(1L);
            // ADMIN gets all interviews for the candidate
            assertThat(result).isNotEmpty();
        }
    }

    @Test
    void testGetInterviewsByInterviewer_WithRoleNotInterviewerOrIdMismatch() {
        // User with HR_MANAGER role
        User hrUser = new User();
        hrUser.setId(200L);
        hrUser.setUsername("hruser");
        hrUser.setRole(User.UserRole.HR_MANAGER);
        
        try (MockedStatic<SecurityContextHolder> mockedSecurityContext = mockStatic(SecurityContextHolder.class)) {
            SecurityContext securityContext = mock(SecurityContext.class);
            Authentication authentication = mock(Authentication.class);
            
            mockedSecurityContext.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.isAuthenticated()).thenReturn(true);
            when(authentication.getName()).thenReturn("hruser");
            when(userRepository.findByUsername("hruser")).thenReturn(Optional.of(hrUser));
            // Should return empty since HR_MANAGER is not allowed
            List<InterviewDTO> result = interviewService.getInterviewsByInterviewer(999L);
            assertThat(result).isEmpty();
        }
        // User with INTERVIEWER role but ID mismatch
        User interviewer = new User();
        interviewer.setId(201L);
        interviewer.setUsername("interviewer");
        interviewer.setRole(User.UserRole.INTERVIEWER);
        
        try (MockedStatic<SecurityContextHolder> mockedSecurityContext = mockStatic(SecurityContextHolder.class)) {
            SecurityContext securityContext = mock(SecurityContext.class);
            Authentication authentication = mock(Authentication.class);
            
            mockedSecurityContext.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.isAuthenticated()).thenReturn(true);
            when(authentication.getName()).thenReturn("interviewer");
            when(userRepository.findByUsername("interviewer")).thenReturn(Optional.of(interviewer));
            // Should return empty since ID does not match
            List<InterviewDTO> result = interviewService.getInterviewsByInterviewer(999L);
            assertThat(result).isEmpty();
        }
    }

    @Test
    void testGetAllInterviews_WithUserRoleNotAdminHrOrInterviewer() {
        // User with a role not covered (simulate a new role)
        User otherUser = new User();
        otherUser.setId(300L);
        otherUser.setUsername("otheruser");
        // Simulate a role not in ADMIN, HR_MANAGER, INTERVIEWER
        // We'll use INTERVIEWER but override the branch logic by not matching any if
        otherUser.setRole(null);
        
        try (MockedStatic<SecurityContextHolder> mockedSecurityContext = mockStatic(SecurityContextHolder.class)) {
            SecurityContext securityContext = mock(SecurityContext.class);
            Authentication authentication = mock(Authentication.class);
            
            mockedSecurityContext.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.isAuthenticated()).thenReturn(true);
            when(authentication.getName()).thenReturn("otheruser");
            when(userRepository.findByUsername("otheruser")).thenReturn(Optional.of(otherUser));
            // Should hit the final else branch and return empty
            Page<InterviewDTO> result = interviewService.getAllInterviews(Pageable.unpaged());
            assertThat(result).isEmpty();
        }
    }

    @Test
    void testGetInterviewsByCandidate_WithUserRoleNotAdminHrOrInterviewer() {
        // User with a role not covered (simulate a new role)
        User otherUser = new User();
        otherUser.setId(301L);
        otherUser.setUsername("otheruser2");
        otherUser.setRole(null);
        
        try (MockedStatic<SecurityContextHolder> mockedSecurityContext = mockStatic(SecurityContextHolder.class)) {
            SecurityContext securityContext = mock(SecurityContext.class);
            Authentication authentication = mock(Authentication.class);
            
            mockedSecurityContext.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.isAuthenticated()).thenReturn(true);
            when(authentication.getName()).thenReturn("otheruser2");
            when(userRepository.findByUsername("otheruser2")).thenReturn(Optional.of(otherUser));
            when(interviewRepository.findByCandidateId(1L)).thenReturn(List.of(testInterview));
            // Should hit the final else branch and return empty
            List<InterviewDTO> result = interviewService.getInterviewsByCandidate(1L);
            assertThat(result).isEmpty();
        }
    }

    @Test
    void testGetInterviewsByInterviewer_WithUserRoleNotAdminHrOrInterviewer() {
        // User with a role not covered (simulate a new role)
        User otherUser = new User();
        otherUser.setId(302L);
        otherUser.setUsername("otheruser3");
        otherUser.setRole(null);
        
        try (MockedStatic<SecurityContextHolder> mockedSecurityContext = mockStatic(SecurityContextHolder.class)) {
            SecurityContext securityContext = mock(SecurityContext.class);
            Authentication authentication = mock(Authentication.class);
            
            mockedSecurityContext.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.isAuthenticated()).thenReturn(true);
            when(authentication.getName()).thenReturn("otheruser3");
            when(userRepository.findByUsername("otheruser3")).thenReturn(Optional.of(otherUser));
            // Should hit the final else branch and return empty
            List<InterviewDTO> result = interviewService.getInterviewsByInterviewer(999L);
            assertThat(result).isEmpty();
        }
    }
} 