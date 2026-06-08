package com.herbarium.modules.taxonomy.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "taxonomy")
public class Taxonomy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "parent_id")
    private Long parentId = 0L;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "latin_name", length = 150)
    private String latinName;

    @Column(nullable = false, length = 20)
    private String rank;

    @Column(nullable = false)
    private Integer level;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Transient
    private List<Taxonomy> children = new ArrayList<>();
}
