package com.interviewnotes.model;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import static org.assertj.core.api.Assertions.*;

class InterviewNotesTest {
    @Test
    void testGettersAndSetters() {
        InterviewNotes notes = new InterviewNotes();
        notes.setId(1L);
        notes.setTechnicalNotes("Tech");
        notes.setCodingNotes("Code");
        notes.setCommunicationNotes("Comm");
        notes.setStrengths(new String[]{"Strength1", "Strength2"});
        notes.setWeaknesses(new String[]{"Weak1"});
        notes.setOverallScore(BigDecimal.valueOf(4.5));
        notes.setFeedback("Good");
        notes.setCreatedAt(LocalDateTime.now());
        notes.setUpdatedAt(LocalDateTime.now());
        List<InterviewQuestion> questions = new ArrayList<>();
        notes.setQuestions(questions);
        assertThat(notes.getId()).isEqualTo(1L);
        assertThat(notes.getTechnicalNotes()).isEqualTo("Tech");
        assertThat(notes.getCodingNotes()).isEqualTo("Code");
        assertThat(notes.getCommunicationNotes()).isEqualTo("Comm");
        assertThat(notes.getStrengths()).containsExactly("Strength1", "Strength2");
        assertThat(notes.getWeaknesses()).containsExactly("Weak1");
        assertThat(notes.getOverallScore()).isEqualTo(BigDecimal.valueOf(4.5));
        assertThat(notes.getFeedback()).isEqualTo("Good");
        assertThat(notes.getCreatedAt()).isNotNull();
        assertThat(notes.getUpdatedAt()).isNotNull();
        assertThat(notes.getQuestions()).isSameAs(questions);
    }

    @Test
    void testAddAndRemoveQuestion() {
        InterviewNotes notes = new InterviewNotes();
        InterviewQuestion q = new InterviewQuestion();
        notes.addQuestion(q);
        assertThat(notes.getQuestions()).contains(q);
        notes.removeQuestion(q);
        assertThat(notes.getQuestions()).doesNotContain(q);
    }

    @Test
    void testHasScore() {
        InterviewNotes notes = new InterviewNotes();
        assertThat(notes.hasScore()).isFalse();
        notes.setOverallScore(BigDecimal.ZERO);
        assertThat(notes.hasScore()).isTrue();
        notes.setOverallScore(BigDecimal.valueOf(3.2));
        assertThat(notes.hasScore()).isTrue();
        notes.setOverallScore(null);
        assertThat(notes.hasScore()).isFalse();
    }

    @Test
    void testEqualsAndHashCode() {
        InterviewNotes n1 = new InterviewNotes();
        n1.setId(1L);
        InterviewNotes n2 = new InterviewNotes();
        n2.setId(1L);
        InterviewNotes n3 = new InterviewNotes();
        n3.setId(2L);
        InterviewNotes n4 = new InterviewNotes();
        n4.setId(null);
        
        assertThat(n1).isEqualTo(n2);
        assertThat(n1).hasSameHashCodeAs(n2);
        assertThat(n1).isNotEqualTo(n3);
        assertThat(n1).isNotEqualTo(null);
        assertThat(n1).isNotEqualTo("not notes");
        assertThat(n1).isNotEqualTo(n4); // n4 has null id
        assertThat(n4).isNotEqualTo(n1); // n4 has null id
    }

    @Test
    void testEqualsWithNullId() {
        InterviewNotes n1 = new InterviewNotes();
        n1.setId(null);
        InterviewNotes n2 = new InterviewNotes();
        n2.setId(null);
        InterviewNotes n3 = new InterviewNotes();
        n3.setId(1L);
        
        // When both IDs are null, equals returns false (as per implementation)
        assertThat(n1).isNotEqualTo(n2);
        assertThat(n1).isNotEqualTo(n3);
    }

    @Test
    void testToString() {
        InterviewNotes notes = new InterviewNotes();
        notes.setId(1L);
        notes.setTechnicalNotes("Tech");
        notes.setCodingNotes("Code");
        notes.setCommunicationNotes("Comm");
        notes.setFeedback("Feedback");
        notes.setOverallScore(BigDecimal.valueOf(4.5));
        notes.setStrengths(new String[]{"Strength1"});
        notes.setWeaknesses(new String[]{"Weak1"});
        notes.setCreatedAt(LocalDateTime.now());
        notes.setUpdatedAt(LocalDateTime.now());
        String str = notes.toString();
        assertThat(str).isEqualTo("InterviewNotes{id=1, overallScore=4.5}");
    }

    @Test
    void testEquals_WithNull() {
        InterviewNotes notes1 = new InterviewNotes();
        notes1.setId(1L);
        assertThat(notes1.equals(null)).isFalse();
    }

    @Test
    void testEquals_WithDifferentType() {
        InterviewNotes notes1 = new InterviewNotes();
        notes1.setId(1L);
        String differentObject = "not an InterviewNotes";
        assertThat(notes1.equals(differentObject)).isFalse();
    }

    @Test
    void testEquals_WithDifferentIds() {
        InterviewNotes notes1 = new InterviewNotes();
        notes1.setId(1L);
        InterviewNotes notes2 = new InterviewNotes();
        notes2.setId(2L);
        assertThat(notes1.equals(notes2)).isFalse();
    }

    @Test
    void testEquals_WithBothNullIds() {
        InterviewNotes notes1 = new InterviewNotes();
        notes1.setId(null);
        InterviewNotes notes2 = new InterviewNotes();
        notes2.setId(null);
        assertThat(notes1.equals(notes2)).isFalse();
    }

    @Test
    void testEquals_WithOneNullId() {
        InterviewNotes notes1 = new InterviewNotes();
        notes1.setId(1L);
        InterviewNotes notes2 = new InterviewNotes();
        notes2.setId(null);
        assertThat(notes1.equals(notes2)).isFalse();
        assertThat(notes2.equals(notes1)).isFalse();
    }

    @Test
    void testEquals_BothIdsNull() {
        InterviewNotes notes1 = new InterviewNotes();
        InterviewNotes notes2 = new InterviewNotes();
        assertThat(notes1.equals(notes2)).isFalse();
    }
} 