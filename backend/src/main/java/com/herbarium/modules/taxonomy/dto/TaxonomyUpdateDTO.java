package com.herbarium.modules.taxonomy.dto;

import lombok.Data;

@Data
public class TaxonomyUpdateDTO {

    private String name;

    private String latinName;

    private String rank;

    private Integer sortOrder;
}
