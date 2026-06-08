package com.herbarium.modules.specimen.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class SpecimenCreateDTO {

    @NotBlank(message = "标本名称不能为空")
    private String name;

    private String latinName;

    private Long taxonomyId;

    private String collector;

    private LocalDate collectionDate;

    private String collectionLocation;

    private BigDecimal latitude;

    private BigDecimal longitude;

    private String habitat;

    private String description;

    private List<String> imageUrls;

    private Integer status = 1;
}
