package com.herbarium.modules.feature.service;

import com.herbarium.common.exception.BusinessException;
import com.herbarium.common.util.ImageUtils;
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

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
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

    private static final String BASE_UPLOAD_PATH = "./uploads";

    @Transactional
    public FeatureExtractResultDTO extractFeatures(Long specimenId) {
        Specimen specimen = specimenRepository.findById(specimenId)
                .orElseThrow(() -> new BusinessException("标本不存在"));

        List<SpecimenImage> images = specimenImageRepository.findBySpecimenIdOrderBySortOrderAsc(specimenId);
        if (images.isEmpty()) {
            throw new BusinessException("标本没有图片，无法提取特征");
        }

        SpecimenImage firstImage = images.get(0);
        String imageUrl = firstImage.getImageUrl();
        String imagePath = convertUrlToPath(imageUrl);

        BufferedImage image = loadImage(imagePath);

        ImageUtils.LeafFeatures leafFeatures = ImageUtils.extractLeafFeatures(image);

        BigDecimal leafLength = BigDecimal.valueOf(leafFeatures.leafLength)
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal leafWidth = BigDecimal.valueOf(leafFeatures.leafWidth)
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal leafArea = BigDecimal.valueOf(leafFeatures.leafArea)
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal leafPerimeter = BigDecimal.valueOf(leafFeatures.leafPerimeter)
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal aspectRatio = BigDecimal.valueOf(leafFeatures.aspectRatio)
                .setScale(2, RoundingMode.HALF_UP);

        String leafShape = leafFeatures.leafShape;
        String leafMargin = leafFeatures.leafMargin;
        String leafApex = leafFeatures.leafApex;
        String leafBase = leafFeatures.leafBase;
        String texture = leafFeatures.texture;

        Map<String, Object> colorFeatures = leafFeatures.colorFeatures;

        List<Double> featureVector = leafFeatures.featureVector;

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

    private BufferedImage loadImage(String imagePath) {
        try {
            File file = new File(imagePath);
            if (!file.exists()) {
                throw new BusinessException("图片文件不存在: " + imagePath);
            }
            BufferedImage image = ImageIO.read(file);
            if (image == null) {
                throw new BusinessException("无法读取图片文件: " + imagePath);
            }
            return image;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException("读取图片失败: " + e.getMessage());
        }
    }

    private String convertUrlToPath(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            throw new BusinessException("图片路径为空");
        }
        if (imageUrl.startsWith("/uploads/")) {
            return BASE_UPLOAD_PATH + imageUrl.substring("/uploads".length());
        }
        if (imageUrl.startsWith("./")) {
            return imageUrl;
        }
        if (imageUrl.startsWith("http://") || imageUrl.startsWith("https://")) {
            throw new BusinessException("不支持远程图片URL");
        }
        return BASE_UPLOAD_PATH + "/" + imageUrl;
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
