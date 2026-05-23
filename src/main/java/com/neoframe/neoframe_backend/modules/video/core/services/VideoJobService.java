package com.neoframe.neoframe_backend.modules.video.core.services;

import com.neoframe.neoframe_backend.modules.auth.core.domain.User;
import com.neoframe.neoframe_backend.modules.video.core.domain.VideoJob;
import com.neoframe.neoframe_backend.modules.video.core.domain.VideoStatus;
import com.neoframe.neoframe_backend.modules.video.core.ports.in.VideoJobUseCase;
import com.neoframe.neoframe_backend.modules.auth.core.ports.out.UserRepositoryPort;
import com.neoframe.neoframe_backend.modules.video.core.ports.out.VideoJobRepositoryPort;

import java.time.LocalDateTime;
import java.util.UUID;

public class VideoJobService implements VideoJobUseCase {

    private final UserRepositoryPort userRepository;
    private final VideoJobRepositoryPort videoJobRepository;

    public VideoJobService(UserRepositoryPort userRepository, VideoJobRepositoryPort videoJobRepository) {
        this.userRepository = userRepository;
        this.videoJobRepository = videoJobRepository;
    }

    @Override
    public VideoJob createJob(String userEmail, String scriptUrl, String bgMusicUrl, String introUrl, String transitionUrl) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));

        if (user.getProcessingVideosCount() >= user.getPlanLimit()) {
            throw new IllegalArgumentException("Você atingiu o limite de vídeos processando simultaneamente para o seu plano.");
        }

        VideoJob newJob = new VideoJob(
                UUID.randomUUID(),     // id do job
                user.getId(),          // id do usuário
                scriptUrl,             // script base
                VideoStatus.PENDING,   // status inicial
                LocalDateTime.now()    // data de criação
        );

        newJob.setBackgroundMusicUrl(bgMusicUrl);
        newJob.setIntroVideoUrl(introUrl);
        newJob.setTopicTransitionUrl(transitionUrl);

        newJob.validateRequirements(user.getPlan());

        user.incrementProcessingCount();
        userRepository.save(user);

        return videoJobRepository.save(newJob);
    }
}