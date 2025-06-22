package com.interviewnotes.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Test class for Interview entity.
 * Tests all getters, setters, constructors, and utility methods.
 */
class InterviewTest {

    private Interview interview;
    private Candidate candidate;
    private User interviewer;
    private LocalDateTime testDateTime;

    @BeforeEach
    void setUp() {
        interview = new Interview();
        candidate = new Candidate("John", "Doe", "john@example.com", "Software Engineer");
        interviewer = new User("interviewer", "interviewer@example.com", "password");
        testDateTime = LocalDateTime.now();
    }

    @Test
    void testDefaultConstructor() {
        assertNotNull(interview);
        assertNull(interview.getId());
        assertNull(interview.getCandidate());
        assertNull(interview.getPosition());
        assertNull(interview.getScheduledDate());
        assertNull(interview.getDuration());
        assertEquals("SCHEDULED", interview.getStatus());
        assertNull(interview.getNotes());
        assertNull(interview.getOverallScore());
        assertNull(interview.getCreatedAt());
        assertNull(interview.getUpdatedAt());
        assertNull(interview.getInterviewer());
    }

    @Test
    void testParameterizedConstructor() {
        Interview testInterview = new Interview(candidate, "Software Engineer", testDateTime, 60);
        
        assertEquals(candidate, testInterview.getCandidate());
        assertEquals("Software Engineer", testInterview.getPosition());
        assertEquals(testDateTime, testInterview.getScheduledDate());
        assertEquals(60, testInterview.getDuration());
        assertEquals("SCHEDULED", testInterview.getStatus());
    }

    @Test
    void testIdGetterAndSetter() {
        Long id = 1L;
        interview.setId(id);
        assertEquals(id, interview.getId());
    }

    @Test
    void testCandidateGetterAndSetter() {
        interview.setCandidate(candidate);
        assertEquals(candidate, interview.getCandidate());
    }

    @Test
    void testPositionGetterAndSetter() {
        String position = "Software Engineer";
        interview.setPosition(position);
        assertEquals(position, interview.getPosition());
    }

    @Test
    void testScheduledDateGetterAndSetter() {
        interview.setScheduledDate(testDateTime);
        assertEquals(testDateTime, interview.getScheduledDate());
    }

    @Test
    void testDurationGetterAndSetter() {
        Integer duration = 60;
        interview.setDuration(duration);
        assertEquals(duration, interview.getDuration());
    }

    @Test
    void testStatusGetterAndSetter() {
        // Test default value
        assertEquals("SCHEDULED", interview.getStatus());
        
        // Test setting different statuses
        interview.setStatus("IN_PROGRESS");
        assertEquals("IN_PROGRESS", interview.getStatus());
        
        interview.setStatus("COMPLETED");
        assertEquals("COMPLETED", interview.getStatus());
        
        interview.setStatus("CANCELLED");
        assertEquals("CANCELLED", interview.getStatus());
    }

    @Test
    void testNotesGetterAndSetter() {
        String notes = "Strong technical skills";
        interview.setNotes(notes);
        assertEquals(notes, interview.getNotes());
    }

    @Test
    void testOverallScoreGetterAndSetter() {
        BigDecimal score = new BigDecimal("8.5");
        interview.setOverallScore(score);
        assertEquals(score, interview.getOverallScore());
    }

    @Test
    void testCreatedAtGetterAndSetter() {
        interview.setCreatedAt(testDateTime);
        assertEquals(testDateTime, interview.getCreatedAt());
    }

    @Test
    void testUpdatedAtGetterAndSetter() {
        interview.setUpdatedAt(testDateTime);
        assertEquals(testDateTime, interview.getUpdatedAt());
    }

    @Test
    void testInterviewerGetterAndSetter() {
        interview.setInterviewer(interviewer);
        assertEquals(interviewer, interview.getInterviewer());
    }

    @Test
    void testIsCompleted() {
        // Test when status is COMPLETED
        interview.setStatus("COMPLETED");
        assertTrue(interview.isCompleted());
        
        // Test when status is not COMPLETED
        interview.setStatus("SCHEDULED");
        assertFalse(interview.isCompleted());
        
        interview.setStatus("IN_PROGRESS");
        assertFalse(interview.isCompleted());
        
        interview.setStatus("CANCELLED");
        assertFalse(interview.isCompleted());
    }

    @Test
    void testIsScheduled() {
        // Test when status is SCHEDULED
        interview.setStatus("SCHEDULED");
        assertTrue(interview.isScheduled());
        
        // Test when status is not SCHEDULED
        interview.setStatus("COMPLETED");
        assertFalse(interview.isScheduled());
        
        interview.setStatus("IN_PROGRESS");
        assertFalse(interview.isScheduled());
        
        interview.setStatus("CANCELLED");
        assertFalse(interview.isScheduled());
    }

    @Test
    void testToString() {
        interview.setId(1L);
        interview.setCandidate(candidate);
        interview.setPosition("Software Engineer");
        interview.setScheduledDate(testDateTime);
        interview.setStatus("SCHEDULED");
        
        String result = interview.toString();
        
        assertTrue(result.contains("id=1"));
        assertTrue(result.contains("candidate=" + candidate.getFullName()));
        assertTrue(result.contains("position='Software Engineer'"));
        assertTrue(result.contains("scheduledDate=" + testDateTime));
        assertTrue(result.contains("status=SCHEDULED"));
    }

    @Test
    void testToStringWithNullCandidate() {
        interview.setId(1L);
        interview.setCandidate(null);
        interview.setPosition("Software Engineer");
        interview.setStatus("SCHEDULED");
        
        String result = interview.toString();
        
        assertTrue(result.contains("candidate=null"));
    }

    @Test
    void testEquals() {
        Interview interview1 = new Interview(candidate, "Engineer", testDateTime, 60);
        Interview interview2 = new Interview(candidate, "Engineer", testDateTime, 60);
        Interview interview3 = new Interview(candidate, "Manager", testDateTime, 60);
        
        interview1.setId(1L);
        interview2.setId(1L);
        interview3.setId(2L);
        
        assertEquals(interview1, interview2);
        assertNotEquals(interview1, interview3);
        assertNotEquals(interview1, null);
        assertEquals(interview1, interview1);
        
        // Test with null id
        Interview interview4 = new Interview();
        assertNotEquals(interview1, interview4);
    }

    @Test
    void testHashCode() {
        Interview interview1 = new Interview(candidate, "Engineer", testDateTime, 60);
        Interview interview2 = new Interview(candidate, "Engineer", testDateTime, 60);
        
        interview1.setId(1L);
        interview2.setId(1L);
        
        assertEquals(interview1.hashCode(), interview2.hashCode());
    }

    @Test
    void testNullValues() {
        interview.setId(null);
        interview.setCandidate(null);
        interview.setPosition(null);
        interview.setScheduledDate(null);
        interview.setDuration(null);
        interview.setNotes(null);
        interview.setOverallScore(null);
        interview.setCreatedAt(null);
        interview.setUpdatedAt(null);
        interview.setInterviewer(null);
        
        assertNull(interview.getId());
        assertNull(interview.getCandidate());
        assertNull(interview.getPosition());
        assertNull(interview.getScheduledDate());
        assertNull(interview.getDuration());
        assertNull(interview.getNotes());
        assertNull(interview.getOverallScore());
        assertNull(interview.getCreatedAt());
        assertNull(interview.getUpdatedAt());
        assertNull(interview.getInterviewer());
    }

    @Test
    void testEdgeCases() {
        // Test empty strings
        interview.setPosition("");
        interview.setNotes("");
        interview.setStatus("");
        
        assertEquals("", interview.getPosition());
        assertEquals("", interview.getNotes());
        assertEquals("", interview.getStatus());
        
        // Test zero duration
        interview.setDuration(0);
        assertEquals(0, interview.getDuration());
        
        // Test negative duration
        interview.setDuration(-1);
        assertEquals(-1, interview.getDuration());
        
        // Test very large duration
        interview.setDuration(Integer.MAX_VALUE);
        assertEquals(Integer.MAX_VALUE, interview.getDuration());
        
        // Test BigDecimal edge cases
        interview.setOverallScore(BigDecimal.ZERO);
        assertEquals(BigDecimal.ZERO, interview.getOverallScore());
        
        interview.setOverallScore(new BigDecimal("10.0"));
        assertEquals(new BigDecimal("10.0"), interview.getOverallScore());
        
        interview.setOverallScore(new BigDecimal("0.1"));
        assertEquals(new BigDecimal("0.1"), interview.getOverallScore());
    }

    @Test
    void testStatusTransitions() {
        // Test all possible status values
        String[] statuses = {"SCHEDULED", "IN_PROGRESS", "COMPLETED", "CANCELLED", "NO_SHOW"};
        
        for (String status : statuses) {
            interview.setStatus(status);
            assertEquals(status, interview.getStatus());
        }
    }

    @Test
    void testEquals_WithNull() {
        Interview interview1 = new Interview();
        interview1.setId(1L);
        assertThat(interview1.equals(null)).isFalse();
    }

    @Test
    void testEquals_WithDifferentType() {
        Interview interview1 = new Interview();
        interview1.setId(1L);
        String differentObject = "not an Interview";
        assertThat(interview1.equals(differentObject)).isFalse();
    }

    @Test
    void testEquals_WithDifferentIds() {
        Interview interview1 = new Interview();
        interview1.setId(1L);
        Interview interview2 = new Interview();
        interview2.setId(2L);
        assertThat(interview1.equals(interview2)).isFalse();
    }

    @Test
    void testEquals_WithBothNullIds() {
        Interview interview1 = new Interview();
        interview1.setId(null);
        Interview interview2 = new Interview();
        interview2.setId(null);
        assertThat(interview1.equals(interview2)).isFalse();
    }

    @Test
    void testEquals_WithOneNullId() {
        Interview interview1 = new Interview();
        interview1.setId(1L);
        Interview interview2 = new Interview();
        interview2.setId(null);
        assertThat(interview1.equals(interview2)).isFalse();
        assertThat(interview2.equals(interview1)).isFalse();
    }
} 