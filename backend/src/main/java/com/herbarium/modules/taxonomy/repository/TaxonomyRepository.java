package com.herbarium.modules.taxonomy.repository;

import com.herbarium.modules.taxonomy.entity.Taxonomy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaxonomyRepository extends JpaRepository<Taxonomy, Long> {

    List<Taxonomy> findByParentIdOrderBySortOrderAsc(Long parentId);

    List<Taxonomy> findByRankOrderBySortOrderAsc(String rank);

    Optional<Taxonomy> findByNameAndRank(String name, String rank);

    List<Taxonomy> findByNameContainingOrderBySortOrderAsc(String name);

    boolean existsByParentId(Long parentId);

    long countByParentId(Long parentId);
}
