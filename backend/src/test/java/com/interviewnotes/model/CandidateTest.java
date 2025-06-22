package com.interviewnotes.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Test class for Candidate entity.
 * Tests all getters, setters, constructors, and utility methods.
 */
class CandidateTest {

    private Candidate candidate;
    private LocalDateTime testDateTime;

    @BeforeEach
    void setUp() {
        candidate = new Candidate();
        testDateTime = LocalDateTime.now();
    }

    @Test
    void testDefaultConstructor() {
        assertNotNull(candidate);
        assertNull(candidate.getId());
        assertNull(candidate.getFirstName());
        assertNull(candidate.getLastName());
        assertNull(candidate.getEmail());
        assertNull(candidate.getPhone());
        assertNull(candidate.getPosition());
        assertNull(candidate.getExperience());
        assertNull(candidate.getResumeUrl());
        assertNull(candidate.getSource());
        assertNull(candidate.getNotes());
        assertEquals("ACTIVE", candidate.getStatus());
        assertNull(candidate.getCreatedAt());
        assertNull(candidate.getUpdatedAt());
        assertNotNull(candidate.getInterviews());
        assertTrue(candidate.getInterviews().isEmpty());
    }

    @Test
    void testParameterizedConstructor() {
        Candidate testCandidate = new Candidate("John", "Doe", "john.doe@example.com", "Software Engineer");
        
        assertEquals("John", testCandidate.getFirstName());
        assertEquals("Doe", testCandidate.getLastName());
        assertEquals("john.doe@example.com", testCandidate.getEmail());
        assertEquals("Software Engineer", testCandidate.getPosition());
        assertEquals("ACTIVE", testCandidate.getStatus());
        assertNotNull(testCandidate.getInterviews());
        assertTrue(testCandidate.getInterviews().isEmpty());
    }

    @Test
    void testIdGetterAndSetter() {
        Long id = 1L;
        candidate.setId(id);
        assertEquals(id, candidate.getId());
    }

    @Test
    void testFirstNameGetterAndSetter() {
        String firstName = "John";
        candidate.setFirstName(firstName);
        assertEquals(firstName, candidate.getFirstName());
    }

    @Test
    void testLastNameGetterAndSetter() {
        String lastName = "Doe";
        candidate.setLastName(lastName);
        assertEquals(lastName, candidate.getLastName());
    }

    @Test
    void testEmailGetterAndSetter() {
        String email = "john.doe@example.com";
        candidate.setEmail(email);
        assertEquals(email, candidate.getEmail());
    }

    @Test
    void testPhoneGetterAndSetter() {
        String phone = "+1234567890";
        candidate.setPhone(phone);
        assertEquals(phone, candidate.getPhone());
    }

    @Test
    void testPositionGetterAndSetter() {
        String position = "Software Engineer";
        candidate.setPosition(position);
        assertEquals(position, candidate.getPosition());
    }

    @Test
    void testExperienceGetterAndSetter() {
        Integer experience = 5;
        candidate.setExperience(experience);
        assertEquals(experience, candidate.getExperience());
    }

    @Test
    void testResumeUrlGetterAndSetter() {
        String resumeUrl = "https://example.com/resume.pdf";
        candidate.setResumeUrl(resumeUrl);
        assertEquals(resumeUrl, candidate.getResumeUrl());
    }

    @Test
    void testSourceGetterAndSetter() {
        String source = "LinkedIn";
        candidate.setSource(source);
        assertEquals(source, candidate.getSource());
    }

    @Test
    void testNotesGetterAndSetter() {
        String notes = "Strong technical background";
        candidate.setNotes(notes);
        assertEquals(notes, candidate.getNotes());
    }

    @Test
    void testStatusGetterAndSetter() {
        // Test default value
        assertEquals("ACTIVE", candidate.getStatus());
        
        // Test setting different statuses
        candidate.setStatus("INACTIVE");
        assertEquals("INACTIVE", candidate.getStatus());
        
        candidate.setStatus("HIRED");
        assertEquals("HIRED", candidate.getStatus());
        
        candidate.setStatus("REJECTED");
        assertEquals("REJECTED", candidate.getStatus());
    }

    @Test
    void testCreatedAtGetterAndSetter() {
        candidate.setCreatedAt(testDateTime);
        assertEquals(testDateTime, candidate.getCreatedAt());
    }

    @Test
    void testUpdatedAtGetterAndSetter() {
        candidate.setUpdatedAt(testDateTime);
        assertEquals(testDateTime, candidate.getUpdatedAt());
    }

    @Test
    void testInterviewsGetterAndSetter() {
        List<Interview> interviews = new ArrayList<>();
        Interview interview1 = new Interview();
        Interview interview2 = new Interview();
        interviews.add(interview1);
        interviews.add(interview2);
        
        candidate.setInterviews(interviews);
        assertEquals(interviews, candidate.getInterviews());
        assertEquals(2, candidate.getInterviews().size());
    }

    @Test
    void testGetFullName() {
        candidate.setFirstName("John");
        candidate.setLastName("Doe");
        assertEquals("John Doe", candidate.getFullName());
    }

    @Test
    void testGetFullNameWithNullValues() {
        // Test with null firstName
        candidate.setFirstName(null);
        candidate.setLastName("Doe");
        assertEquals("null Doe", candidate.getFullName());
        
        // Test with null lastName
        candidate.setFirstName("John");
        candidate.setLastName(null);
        assertEquals("John null", candidate.getFullName());
        
        // Test with both null
        candidate.setFirstName(null);
        candidate.setLastName(null);
        assertEquals("null null", candidate.getFullName());
    }

    @Test
    void testAddInterview() {
        Interview interview = new Interview();
        candidate.addInterview(interview);
        
        assertEquals(1, candidate.getInterviews().size());
        assertTrue(candidate.getInterviews().contains(interview));
        assertEquals(candidate, interview.getCandidate());
    }

    @Test
    void testRemoveInterview() {
        Interview interview = new Interview();
        candidate.addInterview(interview);
        assertEquals(1, candidate.getInterviews().size());
        
        candidate.removeInterview(interview);
        assertEquals(0, candidate.getInterviews().size());
        assertFalse(candidate.getInterviews().contains(interview));
        assertNull(interview.getCandidate());
    }

    @Test
    void testToString() {
        candidate.setId(1L);
        candidate.setFirstName("John");
        candidate.setLastName("Doe");
        candidate.setEmail("john.doe@example.com");
        candidate.setPosition("Software Engineer");
        candidate.setStatus("ACTIVE");
        
        String result = candidate.toString();
        
        assertTrue(result.contains("id=1"));
        assertTrue(result.contains("firstName='John'"));
        assertTrue(result.contains("lastName='Doe'"));
        assertTrue(result.contains("email='john.doe@example.com'"));
        assertTrue(result.contains("position='Software Engineer'"));
        assertTrue(result.contains("status='ACTIVE'"));
    }

    @Test
    void testEquals() {
        Candidate candidate1 = new Candidate("John", "Doe", "john@example.com", "Engineer");
        Candidate candidate2 = new Candidate("John", "Doe", "john@example.com", "Engineer");
        Candidate candidate3 = new Candidate("Jane", "Doe", "jane@example.com", "Engineer");
        
        candidate1.setId(1L);
        candidate2.setId(1L);
        candidate3.setId(2L);
        
        assertEquals(candidate1, candidate2);
        assertNotEquals(candidate1, candidate3);
        assertNotEquals(candidate1, null);
        assertEquals(candidate1, candidate1);
    }

    @Test
    void testHashCode() {
        Candidate candidate1 = new Candidate("John", "Doe", "john@example.com", "Engineer");
        Candidate candidate2 = new Candidate("John", "Doe", "john@example.com", "Engineer");
        
        candidate1.setId(1L);
        candidate2.setId(1L);
        
        assertEquals(candidate1.hashCode(), candidate2.hashCode());
    }

    @Test
    void testNullValues() {
        candidate.setId(null);
        candidate.setFirstName(null);
        candidate.setLastName(null);
        candidate.setEmail(null);
        candidate.setPhone(null);
        candidate.setPosition(null);
        candidate.setExperience(null);
        candidate.setResumeUrl(null);
        candidate.setSource(null);
        candidate.setNotes(null);
        candidate.setCreatedAt(null);
        candidate.setUpdatedAt(null);
        candidate.setInterviews(null);
        
        assertNull(candidate.getId());
        assertNull(candidate.getFirstName());
        assertNull(candidate.getLastName());
        assertNull(candidate.getEmail());
        assertNull(candidate.getPhone());
        assertNull(candidate.getPosition());
        assertNull(candidate.getExperience());
        assertNull(candidate.getResumeUrl());
        assertNull(candidate.getSource());
        assertNull(candidate.getNotes());
        assertNull(candidate.getCreatedAt());
        assertNull(candidate.getUpdatedAt());
        assertNull(candidate.getInterviews());
    }

    @Test
    void testEdgeCases() {
        // Test empty strings
        candidate.setFirstName("");
        candidate.setLastName("");
        candidate.setEmail("");
        candidate.setPhone("");
        candidate.setPosition("");
        candidate.setResumeUrl("");
        candidate.setSource("");
        candidate.setNotes("");
        candidate.setStatus("");
        
        assertEquals("", candidate.getFirstName());
        assertEquals("", candidate.getLastName());
        assertEquals("", candidate.getEmail());
        assertEquals("", candidate.getPhone());
        assertEquals("", candidate.getPosition());
        assertEquals("", candidate.getResumeUrl());
        assertEquals("", candidate.getSource());
        assertEquals("", candidate.getNotes());
        assertEquals("", candidate.getStatus());
        
        // Test zero experience
        candidate.setExperience(0);
        assertEquals(0, candidate.getExperience());
        
        // Test negative experience
        candidate.setExperience(-1);
        assertEquals(-1, candidate.getExperience());
    }

    @Test
    void testEquals_WithNull() {
        Candidate candidate1 = new Candidate();
        candidate1.setId(1L);
        assertThat(candidate1.equals(null)).isFalse();
    }

    @Test
    void testEquals_WithDifferentType() {
        Candidate candidate1 = new Candidate();
        candidate1.setId(1L);
        String differentObject = "not a Candidate";
        assertThat(candidate1.equals(differentObject)).isFalse();
    }

    @Test
    void testEquals_WithDifferentIds() {
        Candidate candidate1 = new Candidate();
        candidate1.setId(1L);
        Candidate candidate2 = new Candidate();
        candidate2.setId(2L);
        assertThat(candidate1.equals(candidate2)).isFalse();
    }

    @Test
    void testEquals_WithBothNullIds() {
        Candidate candidate1 = new Candidate();
        candidate1.setId(null);
        Candidate candidate2 = new Candidate();
        candidate2.setId(null);
        assertThat(candidate1.equals(candidate2)).isFalse();
    }

    @Test
    void testEquals_WithOneNullId() {
        Candidate candidate1 = new Candidate();
        candidate1.setId(1L);
        Candidate candidate2 = new Candidate();
        candidate2.setId(null);
        assertThat(candidate1.equals(candidate2)).isFalse();
        assertThat(candidate2.equals(candidate1)).isFalse();
    }
} 