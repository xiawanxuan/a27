package com.herbarium.modules.export.repository;

import com.herbarium.modules.export.entity.ExportTask;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExportTaskRepository extends JpaRepository<ExportTask, Long> {

    Page<ExportTask> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    List<ExportTask> findTop10ByUserIdOrderByCreatedAtDesc(Long userId);

    List<ExportTask> findByStatusOrderByCreatedAtAsc(String status);
}
