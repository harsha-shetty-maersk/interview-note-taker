package com.interviewnotes.service;

import com.interviewnotes.dto.CandidateDTO;
import com.interviewnotes.model.Candidate;
import com.interviewnotes.model.Interview;
import com.interviewnotes.repository.CandidateRepository;
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

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class CandidateServiceTest {

    @Mock
    private CandidateRepository candidateRepository;

    @InjectMocks
    private CandidateService candidateService;

    private Candidate testCandidate;
    private CandidateDTO testCandidateDTO;

    @BeforeEach
    void setUp() {
        testCandidate = new Candidate();
        testCandidate.setId(1L);
        testCandidate.setFirstName("John");
        testCandidate.setLastName("Doe");
        testCandidate.setEmail("john.doe@example.com");
        testCandidate.setPhone("1234567890");
        testCandidate.setPosition("Software Engineer");
        testCandidate.setExperience(5);
        testCandidate.setResumeUrl("http://example.com/resume.pdf");
        testCandidate.setSource("LinkedIn");
        testCandidate.setNotes("Great candidate");
        testCandidate.setStatus("ACTIVE");
        testCandidate.setCreatedAt(LocalDateTime.now());
        testCandidate.setUpdatedAt(LocalDateTime.now());

        testCandidateDTO = new CandidateDTO();
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
    }

    @Test
    void createCandidate_Success() {
        when(candidateRepository.findByEmail(testCandidateDTO.getEmail())).thenReturn(Optional.empty());
        when(candidateRepository.save(any(Candidate.class))).thenReturn(testCandidate);

        CandidateDTO result = candidateService.createCandidate(testCandidateDTO);

        assertNotNull(result);
        assertEquals(testCandidateDTO.getEmail(), result.getEmail());
        assertEquals(testCandidateDTO.getFirstName(), result.getFirstName());
        verify(candidateRepository).findByEmail(testCandidateDTO.getEmail());
        verify(candidateRepository).save(any(Candidate.class));
    }

    @Test
    void createCandidate_WithNullStatus_ShouldSetDefaultStatus() {
        testCandidateDTO.setStatus(null);
        when(candidateRepository.findByEmail(testCandidateDTO.getEmail())).thenReturn(Optional.empty());
        when(candidateRepository.save(any(Candidate.class))).thenReturn(testCandidate);

        CandidateDTO result = candidateService.createCandidate(testCandidateDTO);

        assertNotNull(result);
        assertEquals("ACTIVE", result.getStatus());
    }

    @Test
    void createCandidate_EmailAlreadyExists_ShouldThrowException() {
        when(candidateRepository.findByEmail(testCandidateDTO.getEmail())).thenReturn(Optional.of(testCandidate));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> candidateService.createCandidate(testCandidateDTO));

        assertEquals("Candidate with email " + testCandidateDTO.getEmail() + " already exists", exception.getMessage());
        verify(candidateRepository).findByEmail(testCandidateDTO.getEmail());
        verify(candidateRepository, never()).save(any(Candidate.class));
    }

    @Test
    void getCandidateById_Success() {
        when(candidateRepository.findById(1L)).thenReturn(Optional.of(testCandidate));

        Optional<CandidateDTO> result = candidateService.getCandidateById(1L);

        assertTrue(result.isPresent());
        assertEquals(testCandidate.getEmail(), result.get().getEmail());
        verify(candidateRepository).findById(1L);
    }

    @Test
    void getCandidateById_NotFound() {
        when(candidateRepository.findById(1L)).thenReturn(Optional.empty());

        Optional<CandidateDTO> result = candidateService.getCandidateById(1L);

        assertFalse(result.isPresent());
        verify(candidateRepository).findById(1L);
    }

    @Test
    void getCandidateByEmail_Success() {
        when(candidateRepository.findByEmail("john.doe@example.com")).thenReturn(Optional.of(testCandidate));

        Optional<CandidateDTO> result = candidateService.getCandidateByEmail("john.doe@example.com");

        assertTrue(result.isPresent());
        assertEquals(testCandidate.getEmail(), result.get().getEmail());
        verify(candidateRepository).findByEmail("john.doe@example.com");
    }

    @Test
    void getCandidateByEmail_NotFound() {
        when(candidateRepository.findByEmail("john.doe@example.com")).thenReturn(Optional.empty());

        Optional<CandidateDTO> result = candidateService.getCandidateByEmail("john.doe@example.com");

        assertFalse(result.isPresent());
        verify(candidateRepository).findByEmail("john.doe@example.com");
    }

    @Test
    void getAllCandidates_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Candidate> candidatePage = new PageImpl<>(Arrays.asList(testCandidate), pageable, 1);
        when(candidateRepository.findAll(pageable)).thenReturn(candidatePage);

        Page<CandidateDTO> result = candidateService.getAllCandidates(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testCandidate.getEmail(), result.getContent().get(0).getEmail());
        verify(candidateRepository).findAll(pageable);
    }

    @Test
    void getCandidatesByStatus_Success() {
        List<Candidate> candidates = Arrays.asList(testCandidate);
        when(candidateRepository.findByStatus("ACTIVE")).thenReturn(candidates);

        List<CandidateDTO> result = candidateService.getCandidatesByStatus("ACTIVE");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testCandidate.getEmail(), result.get(0).getEmail());
        verify(candidateRepository).findByStatus("ACTIVE");
    }

    @Test
    void getCandidatesByStatusWithPagination_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Candidate> candidatePage = new PageImpl<>(Arrays.asList(testCandidate), pageable, 1);
        when(candidateRepository.findByStatus("ACTIVE", pageable)).thenReturn(candidatePage);

        Page<CandidateDTO> result = candidateService.getCandidatesByStatus("ACTIVE", pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testCandidate.getEmail(), result.getContent().get(0).getEmail());
        verify(candidateRepository).findByStatus("ACTIVE", pageable);
    }

    @Test
    void searchCandidates_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Candidate> candidatePage = new PageImpl<>(Arrays.asList(testCandidate), pageable, 1);
        when(candidateRepository.searchCandidates("John", pageable)).thenReturn(candidatePage);

        Page<CandidateDTO> result = candidateService.searchCandidates("John", pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testCandidate.getEmail(), result.getContent().get(0).getEmail());
        verify(candidateRepository).searchCandidates("John", pageable);
    }

    @Test
    void getCandidatesByPosition_Success() {
        List<Candidate> candidates = Arrays.asList(testCandidate);
        when(candidateRepository.findByPositionContainingIgnoreCase("Software")).thenReturn(candidates);

        List<CandidateDTO> result = candidateService.getCandidatesByPosition("Software");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testCandidate.getEmail(), result.get(0).getEmail());
        verify(candidateRepository).findByPositionContainingIgnoreCase("Software");
    }

    @Test
    void getCandidatesByExperience_Success() {
        List<Candidate> candidates = Arrays.asList(testCandidate);
        when(candidateRepository.findByExperienceGreaterThanEqual(3)).thenReturn(candidates);

        List<CandidateDTO> result = candidateService.getCandidatesByExperience(3);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testCandidate.getEmail(), result.get(0).getEmail());
        verify(candidateRepository).findByExperienceGreaterThanEqual(3);
    }

    @Test
    void updateCandidate_Success() {
        when(candidateRepository.findById(1L)).thenReturn(Optional.of(testCandidate));
        when(candidateRepository.save(any(Candidate.class))).thenReturn(testCandidate);

        Optional<CandidateDTO> result = candidateService.updateCandidate(1L, testCandidateDTO);

        assertTrue(result.isPresent());
        assertEquals(testCandidateDTO.getEmail(), result.get().getEmail());
        verify(candidateRepository).findById(1L);
        verify(candidateRepository).save(any(Candidate.class));
    }

    @Test
    void updateCandidate_WithEmailChange_EmailExists_ShouldThrowException() {
        CandidateDTO updatedDTO = new CandidateDTO();
        updatedDTO.setEmail("new.email@example.com");
        updatedDTO.setFirstName("John");
        updatedDTO.setLastName("Doe");

        when(candidateRepository.findById(1L)).thenReturn(Optional.of(testCandidate));
        when(candidateRepository.findByEmail("new.email@example.com")).thenReturn(Optional.of(new Candidate()));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> candidateService.updateCandidate(1L, updatedDTO));

        assertEquals("Candidate with email " + updatedDTO.getEmail() + " already exists", exception.getMessage());
        verify(candidateRepository).findById(1L);
        verify(candidateRepository).findByEmail(updatedDTO.getEmail());
        verify(candidateRepository, never()).save(any(Candidate.class));
    }

    @Test
    void updateCandidate_WithEmailChange_EmailDoesNotExist_ShouldUpdate() {
        CandidateDTO updatedDTO = new CandidateDTO();
        updatedDTO.setEmail("new.email@example.com");
        updatedDTO.setFirstName("John");
        updatedDTO.setLastName("Doe");

        when(candidateRepository.findById(1L)).thenReturn(Optional.of(testCandidate));
        when(candidateRepository.findByEmail("new.email@example.com")).thenReturn(Optional.empty());
        when(candidateRepository.save(any(Candidate.class))).thenReturn(testCandidate);

        Optional<CandidateDTO> result = candidateService.updateCandidate(1L, updatedDTO);

        assertTrue(result.isPresent());
        verify(candidateRepository).findById(1L);
        verify(candidateRepository).findByEmail(updatedDTO.getEmail());
        verify(candidateRepository).save(any(Candidate.class));
    }

    @Test
    void updateCandidate_NotFound() {
        when(candidateRepository.findById(1L)).thenReturn(Optional.empty());

        Optional<CandidateDTO> result = candidateService.updateCandidate(1L, testCandidateDTO);

        assertFalse(result.isPresent());
        verify(candidateRepository).findById(1L);
        verify(candidateRepository, never()).save(any(Candidate.class));
    }

    @Test
    void deleteCandidate_Success() {
        when(candidateRepository.existsById(1L)).thenReturn(true);

        boolean result = candidateService.deleteCandidate(1L);

        assertTrue(result);
        verify(candidateRepository).existsById(1L);
        verify(candidateRepository).deleteById(1L);
    }

    @Test
    void deleteCandidate_NotFound() {
        when(candidateRepository.existsById(1L)).thenReturn(false);

        boolean result = candidateService.deleteCandidate(1L);

        assertFalse(result);
        verify(candidateRepository).existsById(1L);
        verify(candidateRepository, never()).deleteById(anyLong());
    }

    @Test
    void getCandidatesWithInterviews_Success() {
        List<Candidate> candidates = Arrays.asList(testCandidate);
        when(candidateRepository.findCandidatesWithInterviews()).thenReturn(candidates);

        List<CandidateDTO> result = candidateService.getCandidatesWithInterviews();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testCandidate.getEmail(), result.get(0).getEmail());
        verify(candidateRepository).findCandidatesWithInterviews();
    }

    @Test
    void getCandidatesWithoutInterviews_Success() {
        List<Candidate> candidates = Arrays.asList(testCandidate);
        when(candidateRepository.findCandidatesWithoutInterviews()).thenReturn(candidates);

        List<CandidateDTO> result = candidateService.getCandidatesWithoutInterviews();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testCandidate.getEmail(), result.get(0).getEmail());
        verify(candidateRepository).findCandidatesWithoutInterviews();
    }

    @Test
    void getCandidatesWithCompletedInterviews_Success() {
        List<Candidate> candidates = Arrays.asList(testCandidate);
        when(candidateRepository.findCandidatesWithCompletedInterviews()).thenReturn(candidates);

        List<CandidateDTO> result = candidateService.getCandidatesWithCompletedInterviews();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testCandidate.getEmail(), result.get(0).getEmail());
        verify(candidateRepository).findCandidatesWithCompletedInterviews();
    }

    @Test
    void getCandidatesByCriteria_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Candidate> candidatePage = new PageImpl<>(Arrays.asList(testCandidate), pageable, 1);
        when(candidateRepository.findCandidatesByCriteria("ACTIVE", "Software", 3, pageable)).thenReturn(candidatePage);

        Page<CandidateDTO> result = candidateService.getCandidatesByCriteria("ACTIVE", "Software", 3, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testCandidate.getEmail(), result.getContent().get(0).getEmail());
        verify(candidateRepository).findCandidatesByCriteria("ACTIVE", "Software", 3, pageable);
    }

    @Test
    void getCandidateStatistics_Success() {
        List<Candidate> candidatesWithInterviews = Arrays.asList(testCandidate);
        List<Candidate> candidatesWithoutInterviews = Arrays.asList(new Candidate());
        
        when(candidateRepository.count()).thenReturn(10L);
        when(candidateRepository.countByStatus("ACTIVE")).thenReturn(8L);
        when(candidateRepository.findCandidatesWithInterviews()).thenReturn(candidatesWithInterviews);
        when(candidateRepository.findCandidatesWithoutInterviews()).thenReturn(candidatesWithoutInterviews);

        CandidateService.CandidateStatistics result = candidateService.getCandidateStatistics();

        assertNotNull(result);
        assertEquals(10L, result.getTotalCandidates());
        assertEquals(8L, result.getActiveCandidates());
        assertEquals(1L, result.getCandidatesWithInterviews());
        assertEquals(1L, result.getCandidatesWithoutInterviews());
        verify(candidateRepository).count();
        verify(candidateRepository).countByStatus("ACTIVE");
        verify(candidateRepository).findCandidatesWithInterviews();
        verify(candidateRepository).findCandidatesWithoutInterviews();
    }

    @Test
    void convertToDTO_WithInterviews_PopulatesInterviewSummaries() {
        Candidate candidate = new Candidate();
        candidate.setId(1L);
        candidate.setFirstName("John");
        candidate.setLastName("Doe");
        candidate.setEmail("john.doe@example.com");
        candidate.setStatus("ACTIVE");
        candidate.setCreatedAt(LocalDateTime.now());
        candidate.setUpdatedAt(LocalDateTime.now());

        Interview interview1 = new Interview();
        interview1.setId(100L);
        interview1.setPosition("Software Engineer");
        interview1.setStatus("SCHEDULED");
        interview1.setScheduledDate(LocalDateTime.now().plusDays(1));
        interview1.setDuration(60);

        Interview interview2 = new Interview();
        interview2.setId(101L);
        interview2.setPosition("QA Engineer");
        interview2.setStatus("COMPLETED");
        interview2.setScheduledDate(LocalDateTime.now().plusDays(2));
        interview2.setDuration(45);

        candidate.setInterviews(Arrays.asList(interview1, interview2));

        // Use reflection to call private convertToDTO
        try {
            java.lang.reflect.Method method = CandidateService.class.getDeclaredMethod("convertToDTO", Candidate.class);
            method.setAccessible(true);
            Object dtoObj = method.invoke(candidateService, candidate);
            assertNotNull(dtoObj);
            assertTrue(dtoObj instanceof com.interviewnotes.dto.CandidateDTO);
            com.interviewnotes.dto.CandidateDTO dto = (com.interviewnotes.dto.CandidateDTO) dtoObj;
            assertNotNull(dto.getInterviews());
            assertEquals(2, dto.getInterviews().size());
            assertEquals(100L, dto.getInterviews().get(0).getId());
            assertEquals("Software Engineer", dto.getInterviews().get(0).getPosition());
            assertEquals(101L, dto.getInterviews().get(1).getId());
            assertEquals("QA Engineer", dto.getInterviews().get(1).getPosition());
        } catch (Exception e) {
            fail("Reflection failed: " + e.getMessage());
        }
    }

    @Test
    void testConvertToDTO_WithNullInterviews() {
        Candidate candidate = new Candidate();
        candidate.setId(1L);
        candidate.setFirstName("John");
        candidate.setLastName("Doe");
        candidate.setEmail("john.doe@example.com");
        candidate.setInterviews(null); // Null interviews list
        
        when(candidateRepository.findById(1L)).thenReturn(Optional.of(candidate));
        
        Optional<CandidateDTO> result = candidateService.getCandidateById(1L);
        
        assertThat(result).isPresent();
        assertThat(result.get().getInterviews()).isNull();
    }

    @Test
    void testConvertToDTO_WithEmptyInterviews() {
        Candidate candidate = new Candidate();
        candidate.setId(1L);
        candidate.setFirstName("John");
        candidate.setLastName("Doe");
        candidate.setEmail("john.doe@example.com");
        candidate.setInterviews(new ArrayList<>()); // Empty interviews list
        
        when(candidateRepository.findById(1L)).thenReturn(Optional.of(candidate));
        
        Optional<CandidateDTO> result = candidateService.getCandidateById(1L);
        
        assertThat(result).isPresent();
        assertThat(result.get().getInterviews()).isNull(); // Should be null since interviews are empty
    }
} 