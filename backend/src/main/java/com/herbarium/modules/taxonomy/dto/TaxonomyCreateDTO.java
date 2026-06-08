package com.herbarium.modules.taxonomy.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TaxonomyCreateDTO {

    @NotNull(message = "父分类ID不能为空")
    private Long parentId;

    @NotBlank(message = "分类名称不能为空")
    private String name;

    private String latinName;

    @NotBlank(message = "分类等级不能为空")
    private String rank;

    private Integer sortOrder = 0;
}
