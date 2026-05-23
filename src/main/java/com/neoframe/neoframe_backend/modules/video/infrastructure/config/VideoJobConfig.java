package com.neoframe.neoframe_backend.modules.video.infrastructure.config;

import com.neoframe.neoframe_backend.modules.video.core.ports.in.VideoJobUseCase;
import com.neoframe.neoframe_backend.modules.auth.core.ports.out.UserRepositoryPort;
import com.neoframe.neoframe_backend.modules.video.core.ports.out.VideoJobRepositoryPort;
import com.neoframe.neoframe_backend.modules.video.core.services.VideoJobService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class VideoJobConfig {

    @Bean
    public VideoJobUseCase videoJobUseCase(
            UserRepositoryPort userRepository,
            VideoJobRepositoryPort videoJobRepository) {

        return new VideoJobService(userRepository, videoJobRepository);
    }
}