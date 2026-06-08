package com.herbarium.modules.export.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "export_task")
public class ExportTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "export_type", nullable = false, length = 50)
    private String exportType;

    @Column(name = "file_name", length = 255)
    private String fileName;

    @Column(name = "file_url", length = 255)
    private String fileUrl;

    @Column(length = 20)
    private String status = "pending";

    @Column(name = "total_count")
    private Integer totalCount = 0;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;
}
