package com.neoframe.neoframe_backend.modules.video.infrastructure.persistence;

import com.neoframe.neoframe_backend.core.domain.VideoStatus;
// CORREÇÃO: Importa a entidade unificada do shared
import com.neoframe.neoframe_backend.shared.infrastructure.persistence.entity.VideoJobEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface VideoJobJpaRepository extends JpaRepository<VideoJobEntity, UUID> {

    // CORREÇÃO: Garante que o retorno do método use a entidade correta do shared
    @Query("SELECT v FROM VideoJobEntity v WHERE v.status = :status AND v.createdAt < :thresholdTime")
    List<VideoJobEntity> findStuckJobs(
            @Param("status") VideoStatus status,
            @Param("thresholdTime") LocalDateTime thresholdTime
    );

    int countByUserIdAndStatusIn(UUID userId, List<VideoStatus> statusList);
}