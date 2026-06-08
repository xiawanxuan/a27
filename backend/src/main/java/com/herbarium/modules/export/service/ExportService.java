package com.herbarium.modules.export.service;

import com.herbarium.common.exception.BusinessException;
import com.herbarium.common.result.PageResult;
import com.herbarium.common.util.DateUtils;
import com.herbarium.modules.auth.service.AuthService;
import com.herbarium.modules.export.entity.ExportTask;
import com.herbarium.modules.export.repository.ExportTaskRepository;
import com.herbarium.modules.specimen.entity.Specimen;
import com.herbarium.modules.specimen.repository.SpecimenRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ExportService {

    private final ExportTaskRepository exportTaskRepository;
    private final SpecimenRepository specimenRepository;
    private final AuthService authService;

    private static final String EXPORT_PATH = "./uploads/export/";

    @Transactional
    public ExportTask createExportTask(String exportType, String keyword,
                                       Long taxonomyId, String collector,
                                       LocalDate startDate, LocalDate endDate, Integer status) {
        Long userId = authService.getCurrentUserId();

        ExportTask task = new ExportTask();
        task.setUserId(userId);
        task.setExportType(exportType);
        task.setStatus("pending");
        task = exportTaskRepository.save(task);

        ExportTask finalTask = task;
        new Thread(() -> {
            try {
                doExport(finalTask.getId(), exportType, keyword, taxonomyId,
                        collector, startDate, endDate, status);
            } catch (Exception e) {
                updateTaskStatus(finalTask.getId(), "failed", 0, null, null);
            }
        }).start();

        return task;
    }

    private void doExport(Long taskId, String exportType, String keyword,
                          Long taxonomyId, String collector,
                          LocalDate startDate, LocalDate endDate, Integer status) {
        updateTaskStatus(taskId, "processing", 0, null, null);

        try {
            List<Specimen> specimens = specimenRepository.findByConditions(
                    keyword, taxonomyId, collector, startDate, endDate, status,
                    PageRequest.of(0, 10000)
            ).getContent();

            String fileName = "标本数据_" + DateUtils.formatDate(LocalDate.now()) + "_" +
                    UUID.randomUUID().toString().substring(0, 8) + ".xlsx";

            Path exportDir = Paths.get(EXPORT_PATH);
            if (!Files.exists(exportDir)) {
                Files.createDirectories(exportDir);
            }

            String filePath = EXPORT_PATH + fileName;
            generateExcel(specimens, filePath);

            String fileUrl = "/uploads/export/" + fileName;

            updateTaskStatus(taskId, "completed", specimens.size(), fileName, fileUrl);

        } catch (Exception e) {
            updateTaskStatus(taskId, "failed", 0, null, null);
        }
    }

    @Transactional
    protected void updateTaskStatus(Long taskId, String status, Integer totalCount,
                                   String fileName, String fileUrl) {
        ExportTask task = exportTaskRepository.findById(taskId).orElse(null);
        if (task != null) {
            task.setStatus(status);
            if (totalCount != null) {
                task.setTotalCount(totalCount);
            }
            if (fileName != null) {
                task.setFileName(fileName);
            }
            if (fileUrl != null) {
                task.setFileUrl(fileUrl);
            }
            if ("completed".equals(status) || "failed".equals(status)) {
                task.setCompletedAt(LocalDateTime.now());
            }
            exportTaskRepository.save(task);
        }
    }

    private void generateExcel(List<Specimen> specimens, String filePath) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("标本数据");

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            String[] headers = {
                    "标本编号", "中文名", "拉丁名", "分类", "采集人",
                    "采集日期", "采集地点", "纬度", "经度", "生境",
                    "描述", "状态", "创建时间"
            };

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowNum = 1;
            for (Specimen specimen : specimens) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(specimen.getSpecimenNo() != null ? specimen.getSpecimenNo() : "");
                row.createCell(1).setCellValue(specimen.getName() != null ? specimen.getName() : "");
                row.createCell(2).setCellValue(specimen.getLatinName() != null ? specimen.getLatinName() : "");
                row.createCell(3).setCellValue(specimen.getTaxonomyName() != null ? specimen.getTaxonomyName() : "");
                row.createCell(4).setCellValue(specimen.getCollector() != null ? specimen.getCollector() : "");
                row.createCell(5).setCellValue(specimen.getCollectionDate() != null ?
                        DateUtils.formatDate(specimen.getCollectionDate()) : "");
                row.createCell(6).setCellValue(specimen.getCollectionLocation() != null ?
                        specimen.getCollectionLocation() : "");
                row.createCell(7).setCellValue(specimen.getLatitude() != null ?
                        specimen.getLatitude().toString() : "");
                row.createCell(8).setCellValue(specimen.getLongitude() != null ?
                        specimen.getLongitude().toString() : "");
                row.createCell(9).setCellValue(specimen.getHabitat() != null ? specimen.getHabitat() : "");
                row.createCell(10).setCellValue(specimen.getDescription() != null ?
                        specimen.getDescription() : "");
                row.createCell(11).setCellValue(specimen.getStatus() != null && specimen.getStatus() == 1 ?
                        "正常" : "禁用");
                row.createCell(12).setCellValue(specimen.getCreatedAt() != null ?
                        DateUtils.formatDateTime(specimen.getCreatedAt()) : "");
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            try (FileOutputStream outputStream = new FileOutputStream(filePath)) {
                workbook.write(outputStream);
            }
        }
    }

    public ExportTask getTaskStatus(Long taskId) {
        return exportTaskRepository.findById(taskId)
                .orElseThrow(() -> new BusinessException("导出任务不存在"));
    }

    public PageResult<ExportTask> getTaskList(Integer page, Integer pageSize) {
        Long userId = authService.getCurrentUserId();
        Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<ExportTask> taskPage = exportTaskRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        return PageResult.of(taskPage.getContent(), taskPage.getTotalElements(), page, pageSize);
    }

    public String getDownloadUrl(Long taskId) {
        ExportTask task = exportTaskRepository.findById(taskId)
                .orElseThrow(() -> new BusinessException("导出任务不存在"));

        if (!"completed".equals(task.getStatus())) {
            throw new BusinessException("导出任务尚未完成，无法下载");
        }

        return task.getFileUrl();
    }
}
