package com.herbarium.modules.recognition.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class RecognitionResultDTO {

    private Long recordId;
    private String imageUrl;
    private String predictedName;
    private String predictedLatinName;
    private BigDecimal confidence;
    private List<PredictionItem> topPredictions;

    @Data
    public static class PredictionItem {
        private String name;
        private String latinName;
        private BigDecimal confidence;
    }
}
