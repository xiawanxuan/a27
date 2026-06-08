package com.herbarium.modules.specimen.repository;

import com.herbarium.modules.specimen.entity.FeatureData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FeatureDataRepository extends JpaRepository<FeatureData, Long> {

    Optional<FeatureData> findBySpecimenId(Long specimenId);

    void deleteBySpecimenId(Long specimenId);

    boolean existsBySpecimenId(Long specimenId);
}
