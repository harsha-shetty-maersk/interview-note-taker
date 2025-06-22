package com.interviewnotes.model;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing detailed notes and feedback for an interview round.
 */
@Entity
@Table(name = "interview_notes")
@EntityListeners(AuditingEntityListener.class)
public class InterviewNotes {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "technical_notes", columnDefinition = "TEXT")
    private String technicalNotes;

    @Column(name = "coding_notes", columnDefinition = "TEXT")
    private String codingNotes;

    @Column(name = "communication_notes", columnDefinition = "TEXT")
    private String communicationNotes;

    @Column(columnDefinition = "TEXT[]")
    private String[] strengths;

    @Column(columnDefinition = "TEXT[]")
    private String[] weaknesses;

    @Column(name = "overall_score", precision = 3, scale = 1)
    private BigDecimal overallScore;

    @Column(columnDefinition = "TEXT")
    private String feedback;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "interviewNotes", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<InterviewQuestion> questions = new ArrayList<>();

    // Constructors
    public InterviewNotes() {}

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTechnicalNotes() {
        return technicalNotes;
    }

    public void setTechnicalNotes(String technicalNotes) {
        this.technicalNotes = technicalNotes;
    }

    public String getCodingNotes() {
        return codingNotes;
    }

    public void setCodingNotes(String codingNotes) {
        this.codingNotes = codingNotes;
    }

    public String getCommunicationNotes() {
        return communicationNotes;
    }

    public void setCommunicationNotes(String communicationNotes) {
        this.communicationNotes = communicationNotes;
    }

    public String[] getStrengths() {
        return strengths;
    }

    public void setStrengths(String[] strengths) {
        this.strengths = strengths;
    }

    public String[] getWeaknesses() {
        return weaknesses;
    }

    public void setWeaknesses(String[] weaknesses) {
        this.weaknesses = weaknesses;
    }

    public BigDecimal getOverallScore() {
        return overallScore;
    }

    public void setOverallScore(BigDecimal overallScore) {
        this.overallScore = overallScore;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<InterviewQuestion> getQuestions() {
        return questions;
    }

    public void setQuestions(List<InterviewQuestion> questions) {
        this.questions = questions;
    }

    // Utility methods
    public void addQuestion(InterviewQuestion question) {
        questions.add(question);
        question.setInterviewNotes(this);
    }

    public void removeQuestion(InterviewQuestion question) {
        questions.remove(question);
        question.setInterviewNotes(null);
    }

    public boolean hasScore() {
        return overallScore != null;
    }

    @Override
    public String toString() {
        return "InterviewNotes{" +
                "id=" + id +
                ", overallScore=" + overallScore +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InterviewNotes that = (InterviewNotes) o;
        return id != null && id.equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
} 