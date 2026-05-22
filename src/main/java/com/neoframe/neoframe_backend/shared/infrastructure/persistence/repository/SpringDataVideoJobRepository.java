package com.neoframe.neoframe_backend.shared.infrastructure.persistence.repository;

import com.neoframe.neoframe_backend.shared.infrastructure.persistence.entity.VideoJobEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface SpringDataVideoJobRepository extends JpaRepository<VideoJobEntity, UUID> {
    long countByUserIdAndStatusIn(UUID userId, List<String> statuses);
}