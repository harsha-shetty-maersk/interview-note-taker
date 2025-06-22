package com.interviewnotes.model;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.assertj.core.api.Assertions.*;

class InterviewQuestionTest {
    @Test
    void testGettersAndSetters() {
        InterviewQuestion q = new InterviewQuestion();
        q.setId(1L);
        q.setQuestion("What is Java?");
        q.setResponse("A programming language");
        q.setScore(5);
        q.setNotes("Good answer");
        q.setQuestionType("TECHNICAL");
        q.setCreatedAt(LocalDateTime.now());
        InterviewNotes notes = new InterviewNotes();
        q.setInterviewNotes(notes);
        assertThat(q.getId()).isEqualTo(1L);
        assertThat(q.getQuestion()).isEqualTo("What is Java?");
        assertThat(q.getResponse()).isEqualTo("A programming language");
        assertThat(q.getScore()).isEqualTo(5);
        assertThat(q.getNotes()).isEqualTo("Good answer");
        assertThat(q.getQuestionType()).isEqualTo("TECHNICAL");
        assertThat(q.getCreatedAt()).isNotNull();
        assertThat(q.getInterviewNotes()).isSameAs(notes);
    }

    @Test
    void testConstructorWithParameters() {
        InterviewQuestion q = new InterviewQuestion("What is OOP?", "TECHNICAL");
        assertThat(q.getQuestion()).isEqualTo("What is OOP?");
        assertThat(q.getQuestionType()).isEqualTo("TECHNICAL");
    }

    @Test
    void testIsTechnicalQuestion() {
        InterviewQuestion q = new InterviewQuestion();
        q.setQuestionType("CODING");
        assertThat(q.isTechnicalQuestion()).isTrue();
        q.setQuestionType("ALGORITHM");
        assertThat(q.isTechnicalQuestion()).isTrue();
        q.setQuestionType("SYSTEM_DESIGN");
        assertThat(q.isTechnicalQuestion()).isTrue();
        q.setQuestionType("BEHAVIORAL");
        assertThat(q.isTechnicalQuestion()).isFalse();
        q.setQuestionType(null);
        assertThat(q.isTechnicalQuestion()).isFalse();
    }

    @Test
    void testIsBehavioralQuestion() {
        InterviewQuestion q = new InterviewQuestion();
        q.setQuestionType("BEHAVIORAL");
        assertThat(q.isBehavioralQuestion()).isTrue();
        q.setQuestionType("LEADERSHIP");
        assertThat(q.isBehavioralQuestion()).isTrue();
        q.setQuestionType("TEAMWORK");
        assertThat(q.isBehavioralQuestion()).isTrue();
        q.setQuestionType("TECHNICAL");
        assertThat(q.isBehavioralQuestion()).isFalse();
        q.setQuestionType(null);
        assertThat(q.isBehavioralQuestion()).isFalse();
    }

    @Test
    void testHasResponse() {
        InterviewQuestion q = new InterviewQuestion();
        assertThat(q.hasResponse()).isFalse();
        q.setResponse("");
        assertThat(q.hasResponse()).isFalse();
        q.setResponse("   ");
        assertThat(q.hasResponse()).isFalse();
        q.setResponse("Some response");
        assertThat(q.hasResponse()).isTrue();
        q.setResponse("  trimmed response  ");
        assertThat(q.hasResponse()).isTrue();
    }

    @Test
    void testHasScore() {
        InterviewQuestion q = new InterviewQuestion();
        assertThat(q.hasScore()).isFalse();
        q.setScore(null);
        assertThat(q.hasScore()).isFalse();
        q.setScore(0);
        assertThat(q.hasScore()).isTrue();
        q.setScore(5);
        assertThat(q.hasScore()).isTrue();
    }

    @Test
    void testEqualsAndHashCode() {
        InterviewQuestion q1 = new InterviewQuestion();
        q1.setId(1L);
        q1.setQuestion("Q1");
        InterviewQuestion q2 = new InterviewQuestion();
        q2.setId(1L);
        q2.setQuestion("Q1");
        InterviewQuestion q3 = new InterviewQuestion();
        q3.setId(2L);
        q3.setQuestion("Q2");
        InterviewQuestion q4 = new InterviewQuestion();
        q4.setId(null);
        q4.setQuestion("Q4");
        
        assertThat(q1).isEqualTo(q2);
        assertThat(q1).hasSameHashCodeAs(q2);
        assertThat(q1).isNotEqualTo(q3);
        assertThat(q1).isNotEqualTo(null);
        assertThat(q1).isNotEqualTo("not a question");
        assertThat(q1).isNotEqualTo(q4); // q4 has null id
        assertThat(q4).isNotEqualTo(q1); // q4 has null id
    }

    @Test
    void testToString() {
        InterviewQuestion q = new InterviewQuestion();
        q.setId(1L);
        q.setQuestion("This is a very long question that should be truncated in the toString method");
        q.setResponse("A1");
        q.setScore(5);
        q.setNotes("Notes");
        q.setQuestionType("TECHNICAL");
        q.setCreatedAt(LocalDateTime.now());
        String str = q.toString();
        assertThat(str).contains("id=1");
        assertThat(str).contains("question='This is a very long question that should be trunca...");
        assertThat(str).contains("TECHNICAL");
        assertThat(str).contains("score=5");
    }

    @Test
    void testToStringWithNullQuestion() {
        InterviewQuestion q = new InterviewQuestion();
        q.setId(1L);
        q.setQuestion(null);
        q.setQuestionType("TECHNICAL");
        String str = q.toString();
        assertThat(str).contains("question='null'");
    }

    @Test
    void testEqualsWithNullId() {
        InterviewQuestion q1 = new InterviewQuestion();
        q1.setId(null);
        InterviewQuestion q2 = new InterviewQuestion();
        q2.setId(null);
        InterviewQuestion q3 = new InterviewQuestion();
        q3.setId(1L);
        
        // When both IDs are null, equals returns false (as per implementation)
        assertThat(q1).isNotEqualTo(q2);
        assertThat(q1).isNotEqualTo(q3);
    }
} 