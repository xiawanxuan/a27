package com.herbarium.modules.specimen.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "specimen")
public class Specimen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "specimen_no", nullable = false, unique = true, length = 50)
    private String specimenNo;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "latin_name", length = 150)
    private String latinName;

    @Column(name = "taxonomy_id")
    private Long taxonomyId;

    @Transient
    private String taxonomyName;

    @Column(length = 100)
    private String collector;

    @Column(name = "collection_date")
    private LocalDate collectionDate;

    @Column(name = "collection_location", length = 255)
    private String collectionLocation;

    @Column(precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(precision = 10, scale = 7)
    private BigDecimal longitude;

    @Column(length = 255)
    private String habitat;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "creator_id", nullable = false)
    private Long creatorId;

    @Transient
    private String creatorName;

    @Column(columnDefinition = "TINYINT DEFAULT 1")
    private Integer status = 1;

    @Transient
    private List<SpecimenImage> images = new ArrayList<>();

    @Transient
    private FeatureData featureData;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
