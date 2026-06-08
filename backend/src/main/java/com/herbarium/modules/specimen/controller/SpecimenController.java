package com.herbarium.modules.specimen.controller;

import com.herbarium.common.result.PageResult;
import com.herbarium.common.result.Result;
import com.herbarium.modules.specimen.dto.SpecimenCreateDTO;
import com.herbarium.modules.specimen.dto.SpecimenUpdateDTO;
import com.herbarium.modules.specimen.entity.Specimen;
import com.herbarium.modules.specimen.service.SpecimenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Tag(name = "标本管理", description = "植物标本管理相关接口")
@RestController
@RequestMapping("/specimens")
@RequiredArgsConstructor
public class SpecimenController {

    private final SpecimenService specimenService;

    @Operation(summary = "分页查询标本列表")
    @GetMapping
    public Result<PageResult<Specimen>> getSpecimenList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long taxonomyId,
            @RequestParam(required = false) String collector,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(required = false) Integer status) {
        return Result.success(specimenService.getSpecimenList(
                page, pageSize, keyword, taxonomyId, collector, startDate, endDate, status));
    }

    @Operation(summary = "获取标本详情")
    @GetMapping("/{id}")
    public Result<Specimen> getSpecimenById(@PathVariable Long id) {
        return Result.success(specimenService.getSpecimenById(id));
    }

    @Operation(summary = "创建标本")
    @PostMapping
    @PreAuthorize("hasRole('SPECIMEN_ADMIN') or hasRole('ADMIN')")
    public Result<Specimen> createSpecimen(@Valid @RequestBody SpecimenCreateDTO dto) {
        return Result.success(specimenService.createSpecimen(dto));
    }

    @Operation(summary = "更新标本")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SPECIMEN_ADMIN') or hasRole('ADMIN')")
    public Result<Specimen> updateSpecimen(@PathVariable Long id, @Valid @RequestBody SpecimenUpdateDTO dto) {
        return Result.success(specimenService.updateSpecimen(id, dto));
    }

    @Operation(summary = "删除标本")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SPECIMEN_ADMIN') or hasRole('ADMIN')")
    public Result<Void> deleteSpecimen(@PathVariable Long id) {
        specimenService.deleteSpecimen(id);
        return Result.success();
    }

    @Operation(summary = "上传标本图片")
    @PostMapping("/{id}/image")
    @PreAuthorize("hasRole('SPECIMEN_ADMIN') or hasRole('ADMIN')")
    public Result<String> uploadImage(@PathVariable Long id, @RequestParam("file") MultipartFile file) throws IOException {
        String imageUrl = specimenService.uploadImage(file);
        return Result.success(imageUrl);
    }

    @Operation(summary = "上传图片(通用)")
    @PostMapping("/image/upload")
    @PreAuthorize("hasRole('SPECIMEN_ADMIN') or hasRole('ADMIN')")
    public Result<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file) throws IOException {
        String imageUrl = specimenService.uploadImage(file);
        return Result.success(Map.of("url", imageUrl));
    }

    @Operation(summary = "获取标本统计")
    @GetMapping("/stats/count")
    public Result<Long> getTotalCount() {
        return Result.success(specimenService.getTotalCount());
    }

    @Operation(summary = "获取最近录入的标本")
    @GetMapping("/recent")
    public Result<List<Specimen>> getRecentSpecimens(@RequestParam(defaultValue = "10") Integer limit) {
        return Result.success(specimenService.getRecentSpecimens(limit));
    }
}
