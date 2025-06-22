package com.interviewnotes.repository;

import com.interviewnotes.model.Interview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for Interview entity operations.
 */
@Repository
public interface InterviewRepository extends JpaRepository<Interview, Long> {

    /**
     * Find interviews by candidate ID.
     */
    List<Interview> findByCandidateId(Long candidateId);

    /**
     * Find interviews by candidate ID with pagination.
     */
    Page<Interview> findByCandidateId(Long candidateId, Pageable pageable);

    /**
     * Find interviews by status.
     */
    List<Interview> findByStatus(String status);

    /**
     * Find interviews by status with pagination.
     */
    Page<Interview> findByStatus(String status, Pageable pageable);

    /**
     * Find interviews by position containing the given text.
     */
    List<Interview> findByPositionContainingIgnoreCase(String position);

    /**
     * Find interviews by position containing the given text with pagination.
     */
    Page<Interview> findByPositionContainingIgnoreCase(String position, Pageable pageable);

    /**
     * Find interviews scheduled between two dates.
     */
    List<Interview> findByScheduledDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Find interviews scheduled after a specific date.
     */
    List<Interview> findByScheduledDateAfter(LocalDateTime date);

    /**
     * Find interviews scheduled before a specific date.
     */
    List<Interview> findByScheduledDateBefore(LocalDateTime date);

    /**
     * Find interviews by candidate and status.
     */
    List<Interview> findByCandidateIdAndStatus(Long candidateId, String status);

    /**
     * Find completed interviews with overall score.
     */
    @Query("SELECT i FROM Interview i WHERE i.status = 'COMPLETED' AND i.overallScore IS NOT NULL")
    List<Interview> findCompletedInterviewsWithScore();

    /**
     * Count interviews by status.
     */
    long countByStatus(String status);

    /**
     * Count interviews by position.
     */
    long countByPosition(String position);

    /**
     * Find interviews created in the last N days.
     */
    @Query("SELECT i FROM Interview i WHERE i.createdAt >= :startDate")
    List<Interview> findInterviewsCreatedAfter(@Param("startDate") LocalDateTime startDate);

    /**
     * Find interviews with high scores (above threshold).
     */
    @Query("SELECT i FROM Interview i WHERE i.overallScore >= :minScore")
    List<Interview> findInterviewsWithHighScores(@Param("minScore") java.math.BigDecimal minScore);

    /**
     * Find interviews by multiple criteria.
     */
    @Query("SELECT i FROM Interview i WHERE " +
           "(:status IS NULL OR i.status = :status) AND " +
           "(:position IS NULL OR LOWER(i.position) LIKE LOWER(CONCAT('%', :position, '%'))) AND " +
           "(:startDate IS NULL OR i.scheduledDate >= :startDate) AND " +
           "(:endDate IS NULL OR i.scheduledDate <= :endDate)")
    Page<Interview> findInterviewsByCriteria(
            @Param("status") String status,
            @Param("position") String position,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );

    /**
     * Find upcoming interviews (scheduled in the future).
     */
    @Query("SELECT i FROM Interview i WHERE i.scheduledDate > :now AND i.status = 'SCHEDULED' ORDER BY i.scheduledDate ASC")
    List<Interview> findUpcomingInterviews(@Param("now") LocalDateTime now);

    /**
     * Find today's interviews.
     */
    @Query("SELECT i FROM Interview i WHERE DATE(i.scheduledDate) = DATE(:today)")
    List<Interview> findTodaysInterviews(@Param("today") LocalDateTime today);

    Page<Interview> findByInterviewer_Id(Long userId, Pageable pageable);

    /**
     * Find interviews by interviewer ID.
     */
    List<Interview> findByInterviewer_Id(Long userId);
} 