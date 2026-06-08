package com.herbarium.modules.specimen.service;

import com.herbarium.common.exception.BusinessException;
import com.herbarium.common.result.PageResult;
import com.herbarium.common.result.ResultCode;
import com.herbarium.common.util.IdUtils;
import com.herbarium.modules.auth.service.AuthService;
import com.herbarium.modules.specimen.dto.SpecimenCreateDTO;
import com.herbarium.modules.specimen.dto.SpecimenUpdateDTO;
import com.herbarium.modules.specimen.entity.Specimen;
import com.herbarium.modules.specimen.entity.SpecimenImage;
import com.herbarium.modules.specimen.repository.SpecimenImageRepository;
import com.herbarium.modules.specimen.repository.SpecimenRepository;
import com.herbarium.modules.taxonomy.entity.Taxonomy;
import com.herbarium.modules.taxonomy.repository.TaxonomyRepository;
import com.herbarium.modules.user.entity.User;
import com.herbarium.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SpecimenService {

    private final SpecimenRepository specimenRepository;
    private final SpecimenImageRepository specimenImageRepository;
    private final TaxonomyRepository taxonomyRepository;
    private final UserRepository userRepository;
    private final AuthService authService;

    private static final String UPLOAD_PATH = "./uploads/specimen/";

    public PageResult<Specimen> getSpecimenList(Integer page, Integer pageSize, String keyword,
                                                 Long taxonomyId, String collector,
                                                 LocalDate startDate, LocalDate endDate, Integer status) {
        Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));

        String searchKeyword = StringUtils.hasText(keyword) ? keyword : null;
        String searchCollector = StringUtils.hasText(collector) ? collector : null;

        Page<Specimen> specimenPage = specimenRepository.findByConditions(
                searchKeyword, taxonomyId, searchCollector, startDate, endDate, status, pageable);

        List<Specimen> specimens = specimenPage.getContent();
        specimens.forEach(this::loadRelatedInfo);

        return PageResult.of(specimens, specimenPage.getTotalElements(), page, pageSize);
    }

    public Specimen getSpecimenById(Long id) {
        Specimen specimen = specimenRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.SPECIMEN_NOT_FOUND));
        loadRelatedInfo(specimen);
        loadImages(specimen);
        return specimen;
    }

    @Transactional
    public Specimen createSpecimen(SpecimenCreateDTO dto) {
        String specimenNo = IdUtils.generateSpecimenNo();
        while (specimenRepository.existsBySpecimenNo(specimenNo)) {
            specimenNo = IdUtils.generateSpecimenNo();
        }

        Specimen specimen = new Specimen();
        specimen.setSpecimenNo(specimenNo);
        specimen.setName(dto.getName());
        specimen.setLatinName(dto.getLatinName());
        specimen.setTaxonomyId(dto.getTaxonomyId());
        specimen.setCollector(dto.getCollector());
        specimen.setCollectionDate(dto.getCollectionDate());
        specimen.setCollectionLocation(dto.getCollectionLocation());
        specimen.setLatitude(dto.getLatitude());
        specimen.setLongitude(dto.getLongitude());
        specimen.setHabitat(dto.getHabitat());
        specimen.setDescription(dto.getDescription());
        specimen.setCreatorId(authService.getCurrentUserId());
        specimen.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);

        specimen = specimenRepository.save(specimen);

        if (dto.getImageUrls() != null && !dto.getImageUrls().isEmpty()) {
            saveSpecimenImages(specimen.getId(), dto.getImageUrls());
        }

        return specimen;
    }

    @Transactional
    public Specimen updateSpecimen(Long id, SpecimenUpdateDTO dto) {
        Specimen specimen = specimenRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.SPECIMEN_NOT_FOUND));

        if (dto.getName() != null) {
            specimen.setName(dto.getName());
        }
        if (dto.getLatinName() != null) {
            specimen.setLatinName(dto.getLatinName());
        }
        if (dto.getTaxonomyId() != null) {
            specimen.setTaxonomyId(dto.getTaxonomyId());
        }
        if (dto.getCollector() != null) {
            specimen.setCollector(dto.getCollector());
        }
        if (dto.getCollectionDate() != null) {
            specimen.setCollectionDate(dto.getCollectionDate());
        }
        if (dto.getCollectionLocation() != null) {
            specimen.setCollectionLocation(dto.getCollectionLocation());
        }
        if (dto.getLatitude() != null) {
            specimen.setLatitude(dto.getLatitude());
        }
        if (dto.getLongitude() != null) {
            specimen.setLongitude(dto.getLongitude());
        }
        if (dto.getHabitat() != null) {
            specimen.setHabitat(dto.getHabitat());
        }
        if (dto.getDescription() != null) {
            specimen.setDescription(dto.getDescription());
        }
        if (dto.getStatus() != null) {
            specimen.setStatus(dto.getStatus());
        }

        specimen = specimenRepository.save(specimen);

        if (dto.getImageUrls() != null) {
            specimenImageRepository.deleteBySpecimenId(id);
            saveSpecimenImages(id, dto.getImageUrls());
        }

        return specimen;
    }

    @Transactional
    public void deleteSpecimen(Long id) {
        if (!specimenRepository.existsById(id)) {
            throw new BusinessException(ResultCode.SPECIMEN_NOT_FOUND);
        }
        specimenImageRepository.deleteBySpecimenId(id);
        specimenRepository.deleteById(id);
    }

    public String uploadImage(MultipartFile file) throws IOException {
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

        return "/uploads/specimen/" + datePath + "/" + newFilename;
    }

    public long getTotalCount() {
        return specimenRepository.count();
    }

    public List<Specimen> getRecentSpecimens(int limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt"));
        List<Specimen> specimens = specimenRepository.findAll(pageable).getContent();
        specimens.forEach(this::loadRelatedInfo);
        return specimens;
    }

    private void loadRelatedInfo(Specimen specimen) {
        if (specimen.getTaxonomyId() != null) {
            taxonomyRepository.findById(specimen.getTaxonomyId()).ifPresent(taxonomy ->
                    specimen.setTaxonomyName(taxonomy.getName())
            );
        }
        if (specimen.getCreatorId() != null) {
            userRepository.findById(specimen.getCreatorId()).ifPresent(user ->
                    specimen.setCreatorName(user.getRealName() != null ? user.getRealName() : user.getUsername())
            );
        }
    }

    private void loadImages(Specimen specimen) {
        List<SpecimenImage> images = specimenImageRepository.findBySpecimenIdOrderBySortOrderAsc(specimen.getId());
        specimen.setImages(images);
    }

    private void saveSpecimenImages(Long specimenId, List<String> imageUrls) {
        List<SpecimenImage> images = new ArrayList<>();
        for (int i = 0; i < imageUrls.size(); i++) {
            SpecimenImage image = new SpecimenImage();
            image.setSpecimenId(specimenId);
            image.setImageUrl(imageUrls.get(i));
            image.setSortOrder(i);
            image.setImageType("specimen");
            images.add(image);
        }
        specimenImageRepository.saveAll(images);
    }
}
