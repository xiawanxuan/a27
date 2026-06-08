package com.herbarium.modules.feature.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class FeatureCompareResultDTO {

    private List<Long> specimenIds;
    private List<String> featureNames;
    private Map<Long, Map<String, Double>> featureValues;
    private List<String> similarSpecimens;
}
