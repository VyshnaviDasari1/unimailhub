package com.unimailhub.backend.repository;

import com.unimailhub.backend.entity.Job;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface JobRepository extends JpaRepository<Job, Long> {

    // Find jobs by user email (through source email)
    @Query("SELECT j FROM Job j WHERE j.sourceEmail.toEmail = :email ORDER BY j.createdAt DESC")
    List<Job> findByUserEmail(@Param("email") String email);

    // Search jobs by keyword in title or company
    @Query("SELECT j FROM Job j WHERE j.sourceEmail.toEmail = :email AND " +
           "(LOWER(j.jobTitle) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(j.companyName) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "ORDER BY j.createdAt DESC")
    List<Job> findByUserEmailAndKeyword(@Param("email") String email, @Param("keyword") String keyword);

    // Filter by job type
    @Query("SELECT j FROM Job j WHERE j.sourceEmail.toEmail = :email AND j.jobType = :jobType ORDER BY j.createdAt DESC")
    List<Job> findByUserEmailAndJobType(@Param("email") String email, @Param("jobType") String jobType);

    // Filter by date range
    @Query("SELECT j FROM Job j WHERE j.sourceEmail.toEmail = :email AND j.datePosted >= :startDate ORDER BY j.createdAt DESC")
    List<Job> findByUserEmailAndDatePostedAfter(@Param("email") String email, @Param("startDate") LocalDateTime startDate);

    // Combined search and filters
    @Query("SELECT j FROM Job j WHERE j.sourceEmail.toEmail = :email AND " +
           "(LOWER(j.jobTitle) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(j.companyName) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:jobType IS NULL OR j.jobType = :jobType) AND " +
           "(:startDate IS NULL OR j.datePosted >= :startDate) " +
           "ORDER BY j.createdAt DESC")
    List<Job> findByUserEmailWithFilters(@Param("email") String email,
                                        @Param("keyword") String keyword,
                                        @Param("jobType") String jobType,
                                        @Param("startDate") LocalDateTime startDate);
}