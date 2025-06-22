package com.interviewnotes.repository;

import com.interviewnotes.model.Candidate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Candidate entity operations.
 */
@Repository
public interface CandidateRepository extends JpaRepository<Candidate, Long> {

    /**
     * Find candidate by email address.
     */
    Optional<Candidate> findByEmail(String email);

    /**
     * Find candidates by status.
     */
    List<Candidate> findByStatus(String status);

    /**
     * Find candidates by status with pagination.
     */
    Page<Candidate> findByStatus(String status, Pageable pageable);

    /**
     * Find candidates by position containing the given text (case-insensitive).
     */
    List<Candidate> findByPositionContainingIgnoreCase(String position);

    /**
     * Find candidates by position containing the given text with pagination.
     */
    Page<Candidate> findByPositionContainingIgnoreCase(String position, Pageable pageable);

    /**
     * Find candidates by experience greater than or equal to the given value.
     */
    List<Candidate> findByExperienceGreaterThanEqual(Integer experience);

    /**
     * Find candidates by source.
     */
    List<Candidate> findBySource(String source);

    /**
     * Search candidates by name (first name or last name) containing the given text.
     */
    @Query("SELECT c FROM Candidate c WHERE LOWER(c.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(c.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(c.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(c.position) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Candidate> searchCandidates(@Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Find candidates with interviews.
     */
    @Query("SELECT DISTINCT c FROM Candidate c LEFT JOIN FETCH c.interviews")
    List<Candidate> findCandidatesWithInterviews();

    /**
     * Count candidates by status.
     */
    long countByStatus(String status);

    /**
     * Count candidates by position.
     */
    long countByPosition(String position);

    /**
     * Find candidates created in the last N days.
     */
    @Query("SELECT c FROM Candidate c WHERE c.createdAt >= :startDate")
    List<Candidate> findCandidatesCreatedAfter(@Param("startDate") java.time.LocalDateTime startDate);

    /**
     * Find candidates with no interviews.
     */
    @Query("SELECT c FROM Candidate c WHERE c.interviews IS EMPTY")
    List<Candidate> findCandidatesWithoutInterviews();

    /**
     * Find candidates with completed interviews.
     */
    @Query("SELECT DISTINCT c FROM Candidate c JOIN c.interviews i WHERE i.status = 'COMPLETED'")
    List<Candidate> findCandidatesWithCompletedInterviews();

    /**
     * Find candidates by multiple criteria.
     */
    @Query("SELECT c FROM Candidate c WHERE " +
           "(:status IS NULL OR c.status = :status) AND " +
           "(:position IS NULL OR LOWER(c.position) LIKE LOWER(CONCAT('%', :position, '%'))) AND " +
           "(:experience IS NULL OR c.experience >= :experience)")
    Page<Candidate> findCandidatesByCriteria(
            @Param("status") String status,
            @Param("position") String position,
            @Param("experience") Integer experience,
            Pageable pageable
    );
} 