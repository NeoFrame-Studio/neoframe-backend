package com.neoframe.neoframe_backend.modules.video.infrastructure.persistence;

import com.neoframe.neoframe_backend.core.domain.VideoStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

import java.time.LocalDateTime;

@Repository
public interface VideoJobJpaRepository extends JpaRepository<VideoJobEntity, UUID> {

    int countByUserIdAndStatusIn(UUID userId, List<VideoStatus> statuses);

    @Query("SELECT v FROM VideoJobEntity v WHERE v.status = :status AND v.createdAt < :thresholdTime")
    List<VideoJobEntity> findStuckJobs(@Param("status") VideoStatus status, @Param("thresholdTime") LocalDateTime thresholdTime);
}
