package com.herbarium.modules.export.controller;

import com.herbarium.common.result.PageResult;
import com.herbarium.common.result.Result;
import com.herbarium.modules.export.entity.ExportTask;
import com.herbarium.modules.export.service.ExportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@Tag(name = "数据导出", description = "数据导出相关接口")
@RestController
@RequestMapping("/export")
@RequiredArgsConstructor
public class ExportController {

    private final ExportService exportService;

    @Operation(summary = "导出标本数据")
    @PostMapping("/specimens")
    @PreAuthorize("hasRole('SPECIMEN_ADMIN') or hasRole('ADMIN')")
    public Result<ExportTask> exportSpecimens(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long taxonomyId,
            @RequestParam(required = false) String collector,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(required = false) Integer status) {
        return Result.success(exportService.createExportTask(
                "specimens", keyword, taxonomyId, collector, startDate, endDate, status));
    }

    @Operation(summary = "查询导出任务状态")
    @GetMapping("/{taskId}/status")
    public Result<ExportTask> getTaskStatus(@PathVariable Long taskId) {
        return Result.success(exportService.getTaskStatus(taskId));
    }

    @Operation(summary = "获取导出任务列表")
    @GetMapping("/tasks")
    public Result<PageResult<ExportTask>> getTaskList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(exportService.getTaskList(page, pageSize));
    }

    @Operation(summary = "下载导出文件")
    @GetMapping("/{taskId}/download")
    public Result<Map<String, String>> getDownloadUrl(@PathVariable Long taskId) {
        String url = exportService.getDownloadUrl(taskId);
        return Result.success(Map.of("url", url));
    }
}
