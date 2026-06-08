package com.herbarium.modules.specimen.repository;

import com.herbarium.modules.specimen.entity.RecognitionRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecognitionRecordRepository extends JpaRepository<RecognitionRecord, Long> {

    Page<RecognitionRecord> findBySpecimenIdOrderByCreatedAtDesc(Long specimenId, Pageable pageable);

    List<RecognitionRecord> findTop10ByOrderByCreatedAtDesc();

    long countByIsConfirmed(Integer isConfirmed);
}
