package com.herbarium.modules.specimen.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "feature_data")
public class FeatureData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "specimen_id", nullable = false, unique = true)
    private Long specimenId;

    @Column(name = "leaf_length", precision = 10, scale = 2)
    private BigDecimal leafLength;

    @Column(name = "leaf_width", precision = 10, scale = 2)
    private BigDecimal leafWidth;

    @Column(name = "leaf_area", precision = 10, scale = 2)
    private BigDecimal leafArea;

    @Column(name = "leaf_perimeter", precision = 10, scale = 2)
    private BigDecimal leafPerimeter;

    @Column(name = "aspect_ratio", precision = 5, scale = 2)
    private BigDecimal aspectRatio;

    @Column(name = "leaf_shape", length = 50)
    private String leafShape;

    @Column(name = "leaf_margin", length = 50)
    private String leafMargin;

    @Column(name = "leaf_apex", length = 50)
    private String leafApex;

    @Column(name = "leaf_base", length = 50)
    private String leafBase;

    @Column(length = 50)
    private String texture;

    @Column(name = "color_features", columnDefinition = "TEXT")
    private String colorFeatures;

    @Column(name = "feature_vector", columnDefinition = "TEXT")
    private String featureVector;

    @Column(name = "extracted_at")
    private LocalDateTime extractedAt;
}
