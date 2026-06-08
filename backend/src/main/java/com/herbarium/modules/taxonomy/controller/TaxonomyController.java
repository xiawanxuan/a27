package com.herbarium.modules.taxonomy.controller;

import com.herbarium.common.result.Result;
import com.herbarium.modules.taxonomy.dto.TaxonomyCreateDTO;
import com.herbarium.modules.taxonomy.dto.TaxonomyUpdateDTO;
import com.herbarium.modules.taxonomy.entity.Taxonomy;
import com.herbarium.modules.taxonomy.service.TaxonomyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "分类管理", description = "植物分类管理相关接口")
@RestController
@RequestMapping("/taxonomy")
@RequiredArgsConstructor
public class TaxonomyController {

    private final TaxonomyService taxonomyService;

    @Operation(summary = "获取分类树")
    @GetMapping("/tree")
    public Result<List<Taxonomy>> getTaxonomyTree() {
        return Result.success(taxonomyService.getTaxonomyTree());
    }

    @Operation(summary = "获取子分类")
    @GetMapping("/{id}/children")
    public Result<List<Taxonomy>> getChildren(@PathVariable Long id) {
        return Result.success(taxonomyService.getChildren(id));
    }

    @Operation(summary = "获取分类详情")
    @GetMapping("/{id}")
    public Result<Taxonomy> getTaxonomyById(@PathVariable Long id) {
        return Result.success(taxonomyService.getTaxonomyById(id));
    }

    @Operation(summary = "按等级获取分类")
    @GetMapping("/rank/{rank}")
    public Result<List<Taxonomy>> getByRank(@PathVariable String rank) {
        return Result.success(taxonomyService.getByRank(rank));
    }

    @Operation(summary = "搜索分类")
    @GetMapping("/search")
    public Result<List<Taxonomy>> searchByName(@RequestParam String name) {
        return Result.success(taxonomyService.searchByName(name));
    }

    @Operation(summary = "创建分类")
    @PostMapping
    @PreAuthorize("hasRole('SPECIMEN_ADMIN') or hasRole('ADMIN')")
    public Result<Taxonomy> createTaxonomy(@Valid @RequestBody TaxonomyCreateDTO dto) {
        return Result.success(taxonomyService.createTaxonomy(dto));
    }

    @Operation(summary = "更新分类")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SPECIMEN_ADMIN') or hasRole('ADMIN')")
    public Result<Taxonomy> updateTaxonomy(@PathVariable Long id, @Valid @RequestBody TaxonomyUpdateDTO dto) {
        return Result.success(taxonomyService.updateTaxonomy(id, dto));
    }

    @Operation(summary = "删除分类")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SPECIMEN_ADMIN') or hasRole('ADMIN')")
    public Result<Void> deleteTaxonomy(@PathVariable Long id) {
        taxonomyService.deleteTaxonomy(id);
        return Result.success();
    }

    @Operation(summary = "获取分类路径")
    @GetMapping("/{id}/path")
    public Result<List<Taxonomy>> getTaxonomyPath(@PathVariable Long id) {
        return Result.success(taxonomyService.getTaxonomyPath(id));
    }
}
