package com.herbarium.modules.feature.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class FeatureCompareRequestDTO {

    private List<Long> specimenIds;
    private List<String> features;
}
