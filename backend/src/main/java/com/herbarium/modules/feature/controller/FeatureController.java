package com.herbarium.modules.feature.controller;

import com.herbarium.common.result.Result;
import com.herbarium.modules.feature.dto.FeatureCompareRequestDTO;
import com.herbarium.modules.feature.dto.FeatureCompareResultDTO;
import com.herbarium.modules.feature.dto.FeatureExtractResultDTO;
import com.herbarium.modules.feature.service.FeatureService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "特征提取", description = "植物形态特征提取相关接口")
@RestController
@RequestMapping("/feature")
@RequiredArgsConstructor
public class FeatureController {

    private final FeatureService featureService;

    @Operation(summary = "提取形态特征")
    @PostMapping("/extract/{specimenId}")
    public Result<FeatureExtractResultDTO> extractFeatures(@PathVariable Long specimenId) {
        return Result.success(featureService.extractFeatures(specimenId));
    }

    @Operation(summary = "获取标本特征参数")
    @GetMapping("/{specimenId}")
    public Result<FeatureExtractResultDTO> getFeatureBySpecimenId(@PathVariable Long specimenId) {
        return Result.success(featureService.getFeatureBySpecimenId(specimenId));
    }

    @Operation(summary = "多标本特征对比")
    @PostMapping("/compare")
    public Result<FeatureCompareResultDTO> compareFeatures(@RequestBody FeatureCompareRequestDTO request) {
        return Result.success(featureService.compareFeatures(request));
    }
}
