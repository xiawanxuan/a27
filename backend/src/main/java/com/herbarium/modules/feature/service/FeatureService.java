package com.herbarium.modules.feature.service;

import com.herbarium.common.exception.BusinessException;
import com.herbarium.common.util.JsonUtils;
import com.herbarium.modules.feature.dto.FeatureCompareRequestDTO;
import com.herbarium.modules.feature.dto.FeatureCompareResultDTO;
import com.herbarium.modules.feature.dto.FeatureExtractResultDTO;
import com.herbarium.modules.specimen.entity.FeatureData;
import com.herbarium.modules.specimen.entity.Specimen;
import com.herbarium.modules.specimen.entity.SpecimenImage;
import com.herbarium.modules.specimen.repository.FeatureDataRepository;
import com.herbarium.modules.specimen.repository.SpecimenImageRepository;
import com.herbarium.modules.specimen.repository.SpecimenRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class FeatureService {

    private final FeatureDataRepository featureDataRepository;
    private final SpecimenRepository specimenRepository;
    private final SpecimenImageRepository specimenImageRepository;

    private static final String[] LEAF_SHAPES = {
            "卵形", "椭圆形", "披针形", "心形", "圆形",
            "戟形", "箭形", "楔形", "匙形", "菱形"
    };

    private static final String[] LEAF_MARGINS = {
            "全缘", "锯齿", "重锯齿", "齿状", "波状",
            "浅裂", "深裂", "全裂", "睫毛状", "钝齿"
    };

    private static final String[] LEAF_APEXES = {
            "渐尖", "急尖", "钝形", "圆形", "微凹",
            "倒心形", "尾尖", "芒尖", "凸尖", "截形"
    };

    private static final String[] LEAF_BASES = {
            "楔形", "圆形", "心形", "箭形", "戟形",
            "渐狭", "偏斜", "耳垂形", "盾形", "截形"
    };

    private static final String[] TEXTURES = {
            "革质", "纸质", "肉质", "膜质", "草质",
            "木栓质", "海绵质", "粗糙", "光滑", "被毛"
    };

    @Transactional
    public FeatureExtractResultDTO extractFeatures(Long specimenId) {
        Specimen specimen = specimenRepository.findById(specimenId)
                .orElseThrow(() -> new BusinessException("标本不存在"));

        List<SpecimenImage> images = specimenImageRepository.findBySpecimenIdOrderBySortOrderAsc(specimenId);
        if (images.isEmpty()) {
            throw new BusinessException("标本没有图片，无法提取特征");
        }

        Random random = new Random(specimenId);

        BigDecimal leafLength = BigDecimal.valueOf(30 + random.nextDouble() * 170)
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal leafWidth = BigDecimal.valueOf(10 + random.nextDouble() * 80)
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal leafArea = leafLength.multiply(leafWidth).multiply(BigDecimal.valueOf(0.6 + random.nextDouble() * 0.3))
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal leafPerimeter = BigDecimal.valueOf(2 * (leafLength.doubleValue() + leafWidth.doubleValue()) * 0.8)
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal aspectRatio = leafLength.divide(leafWidth, 2, RoundingMode.HALF_UP);

        String leafShape = LEAF_SHAPES[random.nextInt(LEAF_SHAPES.length)];
        String leafMargin = LEAF_MARGINS[random.nextInt(LEAF_MARGINS.length)];
        String leafApex = LEAF_APEXES[random.nextInt(LEAF_APEXES.length)];
        String leafBase = LEAF_BASES[random.nextInt(LEAF_BASES.length)];
        String texture = TEXTURES[random.nextInt(TEXTURES.length)];

        Map<String, Object> colorFeatures = generateColorFeatures(random);

        List<Double> featureVector = generateFeatureVector(random, 128);

        FeatureData featureData = featureDataRepository.findBySpecimenId(specimenId).orElse(null);
        if (featureData == null) {
            featureData = new FeatureData();
            featureData.setSpecimenId(specimenId);
        }

        featureData.setLeafLength(leafLength);
        featureData.setLeafWidth(leafWidth);
        featureData.setLeafArea(leafArea);
        featureData.setLeafPerimeter(leafPerimeter);
        featureData.setAspectRatio(aspectRatio);
        featureData.setLeafShape(leafShape);
        featureData.setLeafMargin(leafMargin);
        featureData.setLeafApex(leafApex);
        featureData.setLeafBase(leafBase);
        featureData.setTexture(texture);
        featureData.setColorFeatures(JsonUtils.toJson(colorFeatures));
        featureData.setFeatureVector(JsonUtils.toJson(featureVector));
        featureData.setExtractedAt(LocalDateTime.now());

        featureData = featureDataRepository.save(featureData);

        return convertToDTO(featureData);
    }

    public FeatureExtractResultDTO getFeatureBySpecimenId(Long specimenId) {
        FeatureData featureData = featureDataRepository.findBySpecimenId(specimenId)
                .orElseThrow(() -> new BusinessException("该标本暂无特征数据"));
        return convertToDTO(featureData);
    }

    public FeatureCompareResultDTO compareFeatures(FeatureCompareRequestDTO request) {
        if (request.getSpecimenIds() == null || request.getSpecimenIds().size() < 2) {
            throw new BusinessException("请至少选择两个标本进行对比");
        }

        List<String> featureNames = request.getFeatures();
        if (featureNames == null || featureNames.isEmpty()) {
            featureNames = Arrays.asList(
                    "leafLength", "leafWidth", "leafArea", "leafPerimeter", "aspectRatio"
            );
        }

        Map<Long, Map<String, Double>> featureValues = new HashMap<>();
        List<Long> validIds = new ArrayList<>();

        for (Long specimenId : request.getSpecimenIds()) {
            try {
                FeatureData featureData = featureDataRepository.findBySpecimenId(specimenId).orElse(null);
                if (featureData == null) {
                    continue;
                }
                validIds.add(specimenId);
                Map<String, Double> values = new HashMap<>();

                for (String featureName : featureNames) {
                    Double value = getFeatureValue(featureData, featureName);
                    if (value != null) {
                        values.put(featureName, value);
                    }
                }
                featureValues.put(specimenId, values);
            } catch (Exception e) {
                // skip invalid specimens
            }
        }

        if (validIds.size() < 2) {
            throw new BusinessException("所选标本中有效特征数据不足，无法对比");
        }

        FeatureCompareResultDTO result = new FeatureCompareResultDTO();
        result.setSpecimenIds(validIds);
        result.setFeatureNames(featureNames);
        result.setFeatureValues(featureValues);

        List<String> similarSpecimens = new ArrayList<>();
        for (int i = 0; i < validIds.size(); i++) {
            for (int j = i + 1; j < validIds.size(); j++) {
                double similarity = calculateSimilarity(
                        featureValues.get(validIds.get(i)),
                        featureValues.get(validIds.get(j))
                );
                if (similarity > 0.7) {
                    Specimen s1 = specimenRepository.findById(validIds.get(i)).orElse(null);
                    Specimen s2 = specimenRepository.findById(validIds.get(j)).orElse(null);
                    String name1 = s1 != null ? s1.getName() : "标本" + validIds.get(i);
                    String name2 = s2 != null ? s2.getName() : "标本" + validIds.get(j);
                    similarSpecimens.add(name1 + " 与 " + name2 + " 相似度: " +
                            String.format("%.2f%%", similarity * 100));
                }
            }
        }
        result.setSimilarSpecimens(similarSpecimens);

        return result;
    }

    private Map<String, Object> generateColorFeatures(Random random) {
        Map<String, Object> colorFeatures = new HashMap<>();

        Map<String, double[]> dominantColors = new HashMap<>();
        dominantColors.put("green", new double[]{
                34 + random.nextDouble() * 30,
                100 + random.nextDouble() * 100,
                34 + random.nextDouble() * 30
        });
        dominantColors.put("yellowGreen", new double[]{
                154 + random.nextDouble() * 50,
                205 + random.nextDouble() * 50,
                50 + random.nextDouble() * 30
        });
        dominantColors.put("darkGreen", new double[]{
                0 + random.nextDouble() * 20,
                50 + random.nextDouble() * 50,
                0 + random.nextDouble() * 20
        });
        colorFeatures.put("dominantColors", dominantColors);

        Map<String, Double> colorMoments = new HashMap<>();
        colorMoments.put("meanHue", 100 + random.nextDouble() * 40);
        colorMoments.put("meanSaturation", 0.5 + random.nextDouble() * 0.4);
        colorMoments.put("meanValue", 0.4 + random.nextDouble() * 0.5);
        colorMoments.put("varianceHue", 10 + random.nextDouble() * 20);
        colorMoments.put("varianceSaturation", 0.05 + random.nextDouble() * 0.1);
        colorMoments.put("varianceValue", 0.05 + random.nextDouble() * 0.1);
        colorFeatures.put("colorMoments", colorMoments);

        Map<String, Object> histogram = new HashMap<>();
        List<Double> hueHistogram = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            hueHistogram.add(random.nextDouble());
        }
        histogram.put("hue", hueHistogram);

        List<Double> saturationHistogram = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            saturationHistogram.add(random.nextDouble());
        }
        histogram.put("saturation", saturationHistogram);

        colorFeatures.put("histogram", histogram);

        return colorFeatures;
    }

    private List<Double> generateFeatureVector(Random random, int size) {
        List<Double> vector = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            vector.add(random.nextDouble() * 2 - 1);
        }
        return vector;
    }

    private Double getFeatureValue(FeatureData featureData, String featureName) {
        return switch (featureName) {
            case "leafLength" -> featureData.getLeafLength() != null ?
                    featureData.getLeafLength().doubleValue() : null;
            case "leafWidth" -> featureData.getLeafWidth() != null ?
                    featureData.getLeafWidth().doubleValue() : null;
            case "leafArea" -> featureData.getLeafArea() != null ?
                    featureData.getLeafArea().doubleValue() : null;
            case "leafPerimeter" -> featureData.getLeafPerimeter() != null ?
                    featureData.getLeafPerimeter().doubleValue() : null;
            case "aspectRatio" -> featureData.getAspectRatio() != null ?
                    featureData.getAspectRatio().doubleValue() : null;
            default -> null;
        };
    }

    private double calculateSimilarity(Map<String, Double> v1, Map<String, Double> v2) {
        if (v1 == null || v2 == null || v1.isEmpty() || v2.isEmpty()) {
            return 0.0;
        }

        Set<String> commonKeys = new HashSet<>(v1.keySet());
        commonKeys.retainAll(v2.keySet());

        if (commonKeys.isEmpty()) {
            return 0.0;
        }

        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        for (String key : commonKeys) {
            Double val1 = v1.get(key);
            Double val2 = v2.get(key);
            if (val1 != null && val2 != null) {
                dotProduct += val1 * val2;
                norm1 += val1 * val1;
                norm2 += val2 * val2;
            }
        }

        if (norm1 == 0 || norm2 == 0) {
            return 0.0;
        }

        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }

    private FeatureExtractResultDTO convertToDTO(FeatureData featureData) {
        FeatureExtractResultDTO dto = new FeatureExtractResultDTO();
        dto.setFeatureId(featureData.getId());
        dto.setSpecimenId(featureData.getSpecimenId());
        dto.setLeafLength(featureData.getLeafLength());
        dto.setLeafWidth(featureData.getLeafWidth());
        dto.setLeafArea(featureData.getLeafArea());
        dto.setLeafPerimeter(featureData.getLeafPerimeter());
        dto.setAspectRatio(featureData.getAspectRatio());
        dto.setLeafShape(featureData.getLeafShape());
        dto.setLeafMargin(featureData.getLeafMargin());
        dto.setLeafApex(featureData.getLeafApex());
        dto.setLeafBase(featureData.getLeafBase());
        dto.setTexture(featureData.getTexture());

        if (featureData.getColorFeatures() != null) {
            try {
                dto.setColorFeatures(JsonUtils.fromJson(featureData.getColorFeatures(),
                        new TypeReference<Map<String, Object>>() {}));
            } catch (Exception e) {
                dto.setColorFeatures(new HashMap<>());
            }
        }

        if (featureData.getFeatureVector() != null) {
            try {
                dto.setFeatureVector(JsonUtils.fromJson(featureData.getFeatureVector(),
                        new TypeReference<List<Double>>() {}));
            } catch (Exception e) {
                dto.setFeatureVector(new ArrayList<>());
            }
        }

        if (featureData.getExtractedAt() != null) {
            dto.setExtractedAt(featureData.getExtractedAt()
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }

        return dto;
    }
}
