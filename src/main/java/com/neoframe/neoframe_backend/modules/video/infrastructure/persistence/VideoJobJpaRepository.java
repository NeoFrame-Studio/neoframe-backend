package com.neoframe.neoframe_backend.modules.video.infrastructure.persistence;

import com.neoframe.neoframe_backend.modules.video.core.domain.VideoStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface VideoJobJpaRepository extends JpaRepository<VideoJobEntity, UUID> {

    /**
     * Busca jobs que ficaram travados em processamento (ex: se o Worker cair)
     * avaliando se excederam o tempo limite aceitável.
     */
    @Query("SELECT v FROM VideoJobEntity v WHERE v.status = :status AND v.createdAt < :thresholdTime")
    List<VideoJobEntity> findStuckJobs(
            @Param("status") VideoStatus status,
            @Param("thresholdTime") LocalDateTime thresholdTime
    );

    /**
     * Conta a quantidade de vídeos operando em estados específicos para controle de limite do plano.
     */
    int countByUserIdAndStatusIn(UUID userId, List<VideoStatus> statusList);
}