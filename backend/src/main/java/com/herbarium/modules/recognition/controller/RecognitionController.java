package com.herbarium.modules.recognition.controller;

import com.herbarium.common.result.PageResult;
import com.herbarium.common.result.Result;
import com.herbarium.modules.recognition.dto.RecognitionResultDTO;
import com.herbarium.modules.recognition.service.RecognitionService;
import com.herbarium.modules.specimen.entity.RecognitionRecord;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Tag(name = "图像识别", description = "植物图像识别相关接口")
@RestController
@RequestMapping("/recognition")
@RequiredArgsConstructor
public class RecognitionController {

    private final RecognitionService recognitionService;

    @Operation(summary = "单张图像识别")
    @PostMapping("/identify")
    public Result<RecognitionResultDTO> identifyImage(@RequestParam("file") MultipartFile file) throws IOException {
        return Result.success(recognitionService.identifyImage(file));
    }

    @Operation(summary = "获取识别历史记录")
    @GetMapping("/history")
    public Result<PageResult<RecognitionRecord>> getHistory(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(recognitionService.getHistory(page, pageSize));
    }

    @Operation(summary = "获取识别记录详情")
    @GetMapping("/{id}")
    public Result<RecognitionRecord> getRecordById(@PathVariable Long id) {
        return Result.success(recognitionService.getRecordById(id));
    }

    @Operation(summary = "确认识别结果")
    @PostMapping("/{id}/confirm")
    public Result<RecognitionRecord> confirmResult(
            @PathVariable Long id,
            @RequestParam(required = false) String confirmedName,
            @RequestParam(required = false) Long specimenId) {
        return Result.success(recognitionService.confirmResult(id, confirmedName, specimenId));
    }

    @Operation(summary = "获取预测详情")
    @GetMapping("/{id}/predictions")
    public Result<List<RecognitionResultDTO.PredictionItem>> getTopPredictions(@PathVariable Long id) {
        RecognitionRecord record = recognitionService.getRecordById(id);
        return Result.success(recognitionService.getTopPredictionsFromRecord(record));
    }
}
