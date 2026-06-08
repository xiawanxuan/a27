package com.herbarium.modules.recognition.service;

import com.herbarium.common.exception.BusinessException;
import com.herbarium.common.result.PageResult;
import com.herbarium.common.result.ResultCode;
import com.herbarium.common.util.JsonUtils;
import com.herbarium.modules.auth.service.AuthService;
import com.herbarium.modules.recognition.dto.RecognitionResultDTO;
import com.herbarium.modules.specimen.entity.RecognitionRecord;
import com.herbarium.modules.specimen.repository.RecognitionRecordRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class RecognitionService {

    private final RecognitionRecordRepository recognitionRecordRepository;
    private final AuthService authService;

    private static final String UPLOAD_PATH = "./uploads/recognition/";

    private static final String[] PLANT_NAMES = {
            "银杏", "水杉", "珙桐", "望天树", "伯乐树",
            "红豆杉", "苏铁", "雪松", "白皮松", "油松",
            "华山松", "侧柏", "圆柏", "龙柏", "垂柳",
            "旱柳", "杨树", "白桦", "榆树", "桑树",
            "构树", "无花果", "榕树", "橡皮树", "牡丹",
            "芍药", "月季", "玫瑰", "蔷薇", "梅花",
            "桃花", "樱花", "海棠", "紫荆", "紫藤",
            "国槐", "刺槐", "合欢", "皂荚", "黄杨",
            "冬青", "大叶黄杨", "小叶黄杨", "女贞", "桂花"
    };

    private static final String[] LATIN_NAMES = {
            "Ginkgo biloba", "Metasequoia glyptostroboides", "Davidia involucrata",
            "Parashorea chinensis", "Bretschneidera sinensis", "Taxus chinensis",
            "Cycas revoluta", "Cedrus deodara", "Pinus bungeana", "Pinus tabuliformis",
            "Pinus armandii", "Platycladus orientalis", "Juniperus chinensis",
            "Sabina chinensis", "Salix babylonica", "Salix matsudana",
            "Populus alba", "Betula platyphylla", "Ulmus pumila", "Morus alba",
            "Broussonetia papyrifera", "Ficus carica", "Ficus microcarpa",
            "Ficus elastica", "Paeonia suffruticosa", "Paeonia lactiflora",
            "Rosa chinensis", "Rosa rugosa", "Rosa multiflora", "Prunus mume",
            "Prunus persica", "Cerasus serrulata", "Malus spectabilis",
            "Cercis chinensis", "Wisteria sinensis", "Sophora japonica",
            "Robinia pseudoacacia", "Albizia julibrissin", "Gleditsia sinensis",
            "Buxus sinica", "Ilex chinensis", "Euonymus japonicus",
            "Buxus microphylla", "Ligustrum lucidum", "Osmanthus fragrans"
    };

    public RecognitionResultDTO identifyImage(MultipartFile file) throws IOException {
        String imageUrl = saveImage(file);

        Random random = new Random();
        int predictionCount = 3 + random.nextInt(3);

        List<RecognitionResultDTO.PredictionItem> topPredictions = new ArrayList<>();
        Set<Integer> usedIndices = new HashSet<>();

        for (int i = 0; i < predictionCount; i++) {
            int index;
            do {
                index = random.nextInt(PLANT_NAMES.length);
            } while (usedIndices.contains(index));
            usedIndices.add(index);

            BigDecimal confidence = BigDecimal.valueOf(0.3 + random.nextDouble() * 0.7)
                    .setScale(4, RoundingMode.HALF_UP);

            RecognitionResultDTO.PredictionItem item = new RecognitionResultDTO.PredictionItem();
            item.setName(PLANT_NAMES[index]);
            item.setLatinName(LATIN_NAMES[index]);
            item.setConfidence(confidence);
            topPredictions.add(item);
        }

        topPredictions.sort((a, b) -> b.getConfidence().compareTo(a.getConfidence()));

        RecognitionRecord record = new RecognitionRecord();
        record.setImageUrl(imageUrl);
        record.setPredictedName(topPredictions.get(0).getName());
        record.setConfidence(topPredictions.get(0).getConfidence());
        record.setTopPredictions(JsonUtils.toJson(topPredictions));
        record.setIsConfirmed(0);
        record = recognitionRecordRepository.save(record);

        RecognitionResultDTO result = new RecognitionResultDTO();
        result.setRecordId(record.getId());
        result.setImageUrl(imageUrl);
        result.setPredictedName(topPredictions.get(0).getName());
        result.setPredictedLatinName(topPredictions.get(0).getLatinName());
        result.setConfidence(topPredictions.get(0).getConfidence());
        result.setTopPredictions(topPredictions);

        return result;
    }

    public PageResult<RecognitionRecord> getHistory(Integer page, Integer pageSize) {
        Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<RecognitionRecord> recordPage = recognitionRecordRepository.findAll(pageable);
        return PageResult.of(recordPage.getContent(), recordPage.getTotalElements(), page, pageSize);
    }

    public RecognitionRecord getRecordById(Long id) {
        return recognitionRecordRepository.findById(id)
                .orElseThrow(() -> new BusinessException("识别记录不存在"));
    }

    @Transactional
    public RecognitionRecord confirmResult(Long id, String confirmedName, Long specimenId) {
        RecognitionRecord record = recognitionRecordRepository.findById(id)
                .orElseThrow(() -> new BusinessException("识别记录不存在"));

        if (confirmedName != null && !confirmedName.isEmpty()) {
            record.setPredictedName(confirmedName);
        }
        if (specimenId != null) {
            record.setSpecimenId(specimenId);
        }
        record.setIsConfirmed(1);
        record.setConfirmedBy(authService.getCurrentUserId());

        return recognitionRecordRepository.save(record);
    }

    private String saveImage(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new BusinessException(ResultCode.FILE_UPLOAD_ERROR);
        }

        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : ".jpg";

        String newFilename = UUID.randomUUID().toString().replace("-", "") + extension;
        String datePath = LocalDate.now().toString().replace("-", "/");
        String fullPath = UPLOAD_PATH + datePath + "/";

        Path path = Paths.get(fullPath);
        if (!Files.exists(path)) {
            Files.createDirectories(path);
        }

        File destFile = new File(fullPath + newFilename);
        file.transferTo(destFile);

        return "/uploads/recognition/" + datePath + "/" + newFilename;
    }

    public List<RecognitionResultDTO.PredictionItem> getTopPredictionsFromRecord(RecognitionRecord record) {
        if (record.getTopPredictions() == null || record.getTopPredictions().isEmpty()) {
            return Collections.emptyList();
        }
        try {
            return JsonUtils.fromJson(record.getTopPredictions(),
                    new TypeReference<List<RecognitionResultDTO.PredictionItem>>() {});
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}
