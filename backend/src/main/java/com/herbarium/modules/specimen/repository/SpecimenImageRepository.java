package com.herbarium.modules.specimen.repository;

import com.herbarium.modules.specimen.entity.SpecimenImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpecimenImageRepository extends JpaRepository<SpecimenImage, Long> {

    List<SpecimenImage> findBySpecimenIdOrderBySortOrderAsc(Long specimenId);

    void deleteBySpecimenId(Long specimenId);
}
