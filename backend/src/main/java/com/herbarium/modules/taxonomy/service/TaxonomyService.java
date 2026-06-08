package com.herbarium.modules.taxonomy.service;

import com.herbarium.common.exception.BusinessException;
import com.herbarium.common.result.ResultCode;
import com.herbarium.modules.taxonomy.dto.TaxonomyCreateDTO;
import com.herbarium.modules.taxonomy.dto.TaxonomyUpdateDTO;
import com.herbarium.modules.taxonomy.entity.Taxonomy;
import com.herbarium.modules.taxonomy.repository.TaxonomyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaxonomyService {

    private final TaxonomyRepository taxonomyRepository;

    public List<Taxonomy> getTaxonomyTree() {
        List<Taxonomy> allTaxonomies = taxonomyRepository.findAll();
        return buildTree(allTaxonomies, 0L);
    }

    public List<Taxonomy> getChildren(Long parentId) {
        List<Taxonomy> children = taxonomyRepository.findByParentIdOrderBySortOrderAsc(parentId);
        for (Taxonomy child : children) {
            if (taxonomyRepository.existsByParentId(child.getId())) {
                child.setChildren(getChildren(child.getId()));
            }
        }
        return children;
    }

    public Taxonomy getTaxonomyById(Long id) {
        return taxonomyRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.TAXONOMY_NOT_FOUND));
    }

    public List<Taxonomy> getByRank(String rank) {
        return taxonomyRepository.findByRankOrderBySortOrderAsc(rank);
    }

    public List<Taxonomy> searchByName(String name) {
        return taxonomyRepository.findByNameContainingOrderBySortOrderAsc(name);
    }

    @Transactional
    public Taxonomy createTaxonomy(TaxonomyCreateDTO dto) {
        Taxonomy taxonomy = new Taxonomy();
        taxonomy.setParentId(dto.getParentId());
        taxonomy.setName(dto.getName());
        taxonomy.setLatinName(dto.getLatinName());
        taxonomy.setRank(dto.getRank());
        taxonomy.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0);

        if (dto.getParentId() == null || dto.getParentId() == 0) {
            taxonomy.setLevel(1);
        } else {
            Taxonomy parent = taxonomyRepository.findById(dto.getParentId())
                    .orElseThrow(() -> new BusinessException(ResultCode.TAXONOMY_NOT_FOUND));
            taxonomy.setLevel(parent.getLevel() + 1);
        }

        return taxonomyRepository.save(taxonomy);
    }

    @Transactional
    public Taxonomy updateTaxonomy(Long id, TaxonomyUpdateDTO dto) {
        Taxonomy taxonomy = taxonomyRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.TAXONOMY_NOT_FOUND));

        if (dto.getName() != null) {
            taxonomy.setName(dto.getName());
        }
        if (dto.getLatinName() != null) {
            taxonomy.setLatinName(dto.getLatinName());
        }
        if (dto.getRank() != null) {
            taxonomy.setRank(dto.getRank());
        }
        if (dto.getSortOrder() != null) {
            taxonomy.setSortOrder(dto.getSortOrder());
        }

        return taxonomyRepository.save(taxonomy);
    }

    @Transactional
    public void deleteTaxonomy(Long id) {
        if (!taxonomyRepository.existsById(id)) {
            throw new BusinessException(ResultCode.TAXONOMY_NOT_FOUND);
        }

        if (taxonomyRepository.existsByParentId(id)) {
            throw new BusinessException("该分类下存在子分类，无法删除");
        }

        taxonomyRepository.deleteById(id);
    }

    private List<Taxonomy> buildTree(List<Taxonomy> taxonomies, Long parentId) {
        Map<Long, List<Taxonomy>> childrenMap = new HashMap<>();
        for (Taxonomy taxonomy : taxonomies) {
            Long pid = taxonomy.getParentId() == null ? 0L : taxonomy.getParentId();
            childrenMap.computeIfAbsent(pid, k -> new ArrayList<>()).add(taxonomy);
        }

        List<Taxonomy> roots = childrenMap.getOrDefault(parentId, new ArrayList<>());
        roots.sort((a, b) -> {
            int orderA = a.getSortOrder() != null ? a.getSortOrder() : 0;
            int orderB = b.getSortOrder() != null ? b.getSortOrder() : 0;
            return Integer.compare(orderA, orderB);
        });

        for (Taxonomy root : roots) {
            buildChildren(root, childrenMap);
        }

        return roots;
    }

    private void buildChildren(Taxonomy parent, Map<Long, List<Taxonomy>> childrenMap) {
        List<Taxonomy> children = childrenMap.getOrDefault(parent.getId(), new ArrayList<>());
        children.sort((a, b) -> {
            int orderA = a.getSortOrder() != null ? a.getSortOrder() : 0;
            int orderB = b.getSortOrder() != null ? b.getSortOrder() : 0;
            return Integer.compare(orderA, orderB);
        });
        parent.setChildren(children);

        for (Taxonomy child : children) {
            buildChildren(child, childrenMap);
        }
    }

    public List<Taxonomy> getTaxonomyPath(Long id) {
        List<Taxonomy> path = new ArrayList<>();
        Taxonomy current = taxonomyRepository.findById(id).orElse(null);
        while (current != null) {
            path.add(0, current);
            if (current.getParentId() == null || current.getParentId() == 0) {
                break;
            }
            current = taxonomyRepository.findById(current.getParentId()).orElse(null);
        }
        return path;
    }
}
