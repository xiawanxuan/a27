package com.herbarium.modules.specimen.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "recognition_record")
public class RecognitionRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "specimen_id")
    private Long specimenId;

    @Column(name = "image_url", nullable = false, length = 255)
    private String imageUrl;

    @Column(name = "predicted_name", length = 100)
    private String predictedName;

    @Column(precision = 5, scale = 4)
    private BigDecimal confidence;

    @Column(name = "top_predictions", columnDefinition = "TEXT")
    private String topPredictions;

    @Column(name = "is_confirmed", columnDefinition = "TINYINT DEFAULT 0")
    private Integer isConfirmed = 0;

    @Column(name = "confirmed_by")
    private Long confirmedBy;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
