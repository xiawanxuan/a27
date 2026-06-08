package com.herbarium.modules.feature.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
public class FeatureExtractResultDTO {

    private Long featureId;
    private Long specimenId;
    private BigDecimal leafLength;
    private BigDecimal leafWidth;
    private BigDecimal leafArea;
    private BigDecimal leafPerimeter;
    private BigDecimal aspectRatio;
    private String leafShape;
    private String leafMargin;
    private String leafApex;
    private String leafBase;
    private String texture;
    private Map<String, Object> colorFeatures;
    private List<Double> featureVector;
    private String extractedAt;
}
