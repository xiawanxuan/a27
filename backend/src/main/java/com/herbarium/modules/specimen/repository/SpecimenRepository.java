package com.herbarium.modules.specimen.repository;

import com.herbarium.modules.specimen.entity.Specimen;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SpecimenRepository extends JpaRepository<Specimen, Long> {

    Optional<Specimen> findBySpecimenNo(String specimenNo);

    boolean existsBySpecimenNo(String specimenNo);

    Page<Specimen> findByNameContaining(String name, Pageable pageable);

    Page<Specimen> findByTaxonomyId(Long taxonomyId, Pageable pageable);

    Page<Specimen> findByCollectorContaining(String collector, Pageable pageable);

    @Query("SELECT s FROM Specimen s WHERE " +
           "(:keyword IS NULL OR s.name LIKE %:keyword% OR s.specimenNo LIKE %:keyword%) AND " +
           "(:taxonomyId IS NULL OR s.taxonomyId = :taxonomyId) AND " +
           "(:collector IS NULL OR s.collector LIKE %:collector%) AND " +
           "(:startDate IS NULL OR s.collectionDate >= :startDate) AND " +
           "(:endDate IS NULL OR s.collectionDate <= :endDate) AND " +
           "(:status IS NULL OR s.status = :status)")
    Page<Specimen> findByConditions(
            @Param("keyword") String keyword,
            @Param("taxonomyId") Long taxonomyId,
            @Param("collector") String collector,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("status") Integer status,
            Pageable pageable);

    long countByStatus(Integer status);

    long countByTaxonomyId(Long taxonomyId);

    @Query("SELECT COUNT(s) FROM Specimen s WHERE s.createdAt >= :startDate")
    long countCreatedAfter(@Param("startDate") java.time.LocalDateTime startDate);

    List<Specimen> findTop10ByOrderByCreatedAtDesc();
}
