package com.interviewnotes.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Entity representing individual questions and responses during an interview.
 */
@Entity
@Table(name = "interview_questions")
@EntityListeners(AuditingEntityListener.class)
public class InterviewQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notes_id", nullable = false)
    private InterviewNotes interviewNotes;

    @NotBlank(message = "Question is required")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String question;

    @Column(columnDefinition = "TEXT")
    private String response;

    @Min(value = 1, message = "Score must be at least 1")
    @Max(value = 10, message = "Score must be at most 10")
    @Column
    private Integer score;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "question_type", length = 50)
    private String questionType;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Constructors
    public InterviewQuestion() {}

    public InterviewQuestion(String question, String questionType) {
        this.question = question;
        this.questionType = questionType;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public InterviewNotes getInterviewNotes() {
        return interviewNotes;
    }

    public void setInterviewNotes(InterviewNotes interviewNotes) {
        this.interviewNotes = interviewNotes;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getQuestionType() {
        return questionType;
    }

    public void setQuestionType(String questionType) {
        this.questionType = questionType;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // Utility methods
    public boolean hasScore() {
        return score != null;
    }

    public boolean hasResponse() {
        return response != null && !response.trim().isEmpty();
    }

    public boolean isTechnicalQuestion() {
        return "CODING".equals(questionType) || "ALGORITHM".equals(questionType) || "SYSTEM_DESIGN".equals(questionType);
    }

    public boolean isBehavioralQuestion() {
        return "BEHAVIORAL".equals(questionType) || "LEADERSHIP".equals(questionType) || "TEAMWORK".equals(questionType);
    }

    @Override
    public String toString() {
        return "InterviewQuestion{" +
                "id=" + id +
                ", question='" + (question != null ? question.substring(0, Math.min(question.length(), 50)) + "..." : "null") + '\'' +
                ", questionType='" + questionType + '\'' +
                ", score=" + score +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InterviewQuestion that = (InterviewQuestion) o;
        return id != null && id.equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
} 